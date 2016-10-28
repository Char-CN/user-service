(function(factory){if(typeof define==='function'&&define.amd){define(['jquery'],factory)}else if(typeof exports==='object'){factory(require('jquery'))}else{factory(jQuery)}}(function($){var pluses=/\+/g;function encode(s){return config.raw?s:encodeURIComponent(s)}function decode(s){return config.raw?s:decodeURIComponent(s)}function stringifyCookieValue(value){return encode(config.json?JSON.stringify(value):String(value))}function parseCookieValue(s){if(s.indexOf('"')===0){s=s.slice(1,-1).replace(/\\"/g,'"').replace(/\\\\/g,'\\')}try{s=decodeURIComponent(s.replace(pluses,' '));return config.json?JSON.parse(s):s}catch(e){}}function read(s,converter){var value=config.raw?s:parseCookieValue(s);return $.isFunction(converter)?converter(value):value}var config=$.cookie=function(key,value,options){if(value!==undefined&&!$.isFunction(value)){options=$.extend({},config.defaults,options);if(typeof options.expires==='number'){var days=options.expires,t=options.expires=new Date();t.setTime(+t+days*864e+5)}return(document.cookie=[encode(key),'=',stringifyCookieValue(value),options.expires?'; expires='+options.expires.toUTCString():'',options.path?'; path='+options.path:'',options.domain?'; domain='+options.domain:'',options.secure?'; secure':''].join(''))}var result=key?undefined:{};var cookies=document.cookie?document.cookie.split('; '):[];for(var i=0,l=cookies.length;i<l;i++){var parts=cookies[i].split('=');var name=decode(parts.shift());var cookie=parts.join('=');if(key&&key===name){result=read(cookie,value);break}if(!key&&(cookie=read(cookie))!==undefined){result[name]=cookie}}return result};config.defaults={};$.removeCookie=function(key,options){if($.cookie(key)===undefined){return false}$.cookie(key,'',$.extend({},options,{expires:-1}));return!$.cookie(key)}}));
var $userservice = function(systemName) {
//	var systemName = "user-service";
	var _url = "http://bigdata.blazer.org:8030/user";
//	var _checkuser = _url + "/userservice/checkuser.do";
	var _checkurl = _url + "/userservice/checkurl.do";
//	var _getuser = _url + "/userservice/getuser.do";
	var _getlogin = _url + "/login.html";
	var userName = null;
	var userNameCn = null;
	var session_key = "US_SESSION_ID";
	// 如：.blazer.org
	var domain = location.href.match(new RegExp("[http|https]://.*([.][a-zA-Z0-9]*[.][a-zA-Z0-9]*)/*.*"))[1];
//	var init = function() {
////		 alert(checkuser);
//		$.ajax({
//			url : _checkuser,
//			type : "GET",
//			async : false,
//			data : {
//				session_key : $.cookie(session_key)
//			},
//			success : function(data) {
//				try {
//					var datas = data.split(",", 2);
//					if (data != undefined && datas[0] == "false") {
//						alert("对不起，您没有登录，请您登录。");
//						location.href = _getlogin + "?url=" + encodeURIComponent(location.href);
//					} else if (data != undefined && datas[0] == "true") {
//						var expires = new Date();
//						expires.setTime(expires.getTime() + (30 * 60 * 1000));
////						$.cookie(session_key, datas[1], { path: "/", expires: expires, domain : domain});
//					}
//				} catch (e) {
//					alert("出现未知错误：" + e);
//				}
//			},
//			error : function(XMLHttpRequest, textStatus, errorThrown) {
//				alert('status：' + XMLHttpRequest.status + ',state：' + XMLHttpRequest.readyState + ',text：' + (textStatus || errorThrown));
//			}
//		});
//		$.ajax({
//			url : _getuser,
//			type : "GET",
//			async : false,
//			data : {
//				session_key : $.cookie(session_key)
//			},
//			success : function(data) {
//				var datas = data.split(",", 6);
//				userName = datas[2];
//				userNameCn = datas[3];
//			},
//			error : function(XMLHttpRequest, textStatus, errorThrown) {
////				alert(XMLHttpRequest.status);
////				alert(XMLHttpRequest.readyState);
////				alert(textStatus);
//				alert('status：' + XMLHttpRequest.status + ',state：' + XMLHttpRequest.readyState + ',text：' + (textStatus || errorThrown));
//			}
//		});
//	};

	var init = function() {
		userName = decodeURIComponent($.cookie("US_USER_NAME"));
		userNameCn = decodeURIComponent($.cookie("US_USER_NAME_CN"));
	};

	var logout = function() {
		var r=confirm("您确定退出登录吗？");
		if (r) {
			$.cookie(session_key, "", { path: "/", expires: -1, domain : domain});
			location.href = _getlogin + "?url=" + encodeURIComponent(location.href);
		}
	};

	var checkurl = function(url) {
		var flag = false;
		$.ajax({
			url : _checkurl,
			type : "GET",
			async : false,
			data : {
				session_key : $.cookie(session_key),
				systemName : systemName,
				url : url
			},
			success : function(data) {
				try {
					var datas = data.split(",", 3);
					if (data != undefined && datas[0] == "false") {
						alert("对不起，您没有登录，请您登录。");
						location.href = _getlogin + "?url=" + encodeURIComponent(location.href);
						return;
					} else if (data != undefined && datas[1] == "false") {
						// 没有权限
						return;
					}
					flag = true;
					var expires = new Date();
					expires.setTime(expires.getTime() + (30 * 60 * 1000));
//					$.cookie(session_key, datas[2], { path: "/", expires: expires, domain : domain});
				} catch (e) {
					alert("出现未知错误：" + e);
				}
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				alert('status：' + XMLHttpRequest.status + ',state：' + XMLHttpRequest.readyState + ',text：' + (textStatus || errorThrown));
			}
		});
		return flag;
	};

	init(); // 初始化

	var obj = new Object();
	obj.userName = userName;
	obj.userNameCn = userNameCn;
	obj.logout = logout;
	obj.checkurl = checkurl;
	return obj;
};
