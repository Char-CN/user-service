var $userservice = function(systemName) {
	/**
	 * 自定义修改，jQuery Cookie Plugin v1.4.1
	 */
	var pluses = /\+/g;
	function encode(s) {
		return config.raw ? s : encodeURIComponent(s)
	}
	function decode(s) {
		return config.raw ? s : decodeURIComponent(s)
	}
	function stringifyCookieValue(value) {
		return encode(config.json ? JSON.stringify(value) : String(value))
	}
	function parseCookieValue(s) {
		if (s.indexOf('"') === 0) {
			s = s.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\')
		}
		try {
			s = decodeURIComponent(s.replace(pluses, ' '));
			return config.json ? JSON.parse(s) : s
		} catch (e) {
		}
	}
	function read(s, converter) {
		var value = config.raw ? s : parseCookieValue(s);
		return $.isFunction(converter) ? converter(value) : value
	}
	var config = function(key, value, options) {
		if (value !== undefined && !$.isFunction(value)) {
			options = $.extend({}, config.defaults, options);
			if (typeof options.expires === 'number') {
				var days = options.expires, t = options.expires = new Date();
				t.setTime(+t + days * 864e+5)
			}
			return (document.cookie = [ encode(key), '=', stringifyCookieValue(value), options.expires ? '; expires=' + options.expires.toUTCString() : '',
					options.path ? '; path=' + options.path : '', options.domain ? '; domain=' + options.domain : '', options.secure ? '; secure' : '' ]
					.join(''))
		}
		var result = key ? undefined : {};
		var cookies = document.cookie ? document.cookie.split('; ') : [];
		for (var i = 0, l = cookies.length; i < l; i++) {
			var parts = cookies[i].split('=');
			var name = decode(parts.shift());
			var cookie = parts.join('=');
			if (key && key === name) {
				result = read(cookie, value);
				break
			}
			if (!key && (cookie = read(cookie)) !== undefined) {
				result[name] = cookie
			}
		}
		return result
	};
	config.defaults = {};
	var removeCookie = function(key, options) {
		if (config(key) === undefined) {
			return false
		}
		config(key, '', $.extend({}, options, {
			expires : -1
		}));
		return !config(key)
	}

	/**
	 * UserServiceJs核心内容
	 */
	var s_url = "http://test.blazerhe.org:8030";
	var s_checkurl = s_url + "/userservice/checkurl.do";
	var s_getlogin = s_url + "/login.html";
	var userName = null;
	var userNameCn = null;

	//	var domain = location.href.match(new RegExp("[http|https]://.*([.][a-zA-Z0-9]*[.][a-zA-Z0-9]*)/*.*"))[1];
	// 如：.blazer.org
	var getDomain = function() {
		var _domain = location.href.match(new RegExp("[http|https]://.*([.][a-zA-Z0-9]*[.][a-zA-Z0-9]*)/*.*"));
		if (_domain == null || _domain == undefined) {
			alert("对不起，域名不正确。");
			return null;
		}
		return _domain[1];
	};

	var init = function() {
		userName = decodeURIComponent(config("US_USER_NAME"));
		userNameCn = decodeURIComponent(config("US_USER_NAME_CN"));
	};

	var domain = getDomain();
	var logout = function(_url) {
		var r=confirm("您确定退出登录吗？");
		if (r) {
			config("US_SESSION_ID", "", { path: "/", expires: -1, domain : domain});
			config("US_USER_NAME", "", { path: "/", expires: -1, domain : domain});
			config("US_USER_NAME_CN", "", { path: "/", expires: -1, domain : domain});
			location.href = s_getlogin + "?url=" + encodeURIComponent(_url ? _url : location.href);
		}
	};

	var checkurl = function(_url) {
		var flag = false;
		$.ajax({
			url : s_checkurl,
			type : "GET",
			async : false,
			data : {
				"US_SESSION_ID" : config("US_SESSION_ID"),
				systemName : systemName,
				url : _url
			},
			success : function(data) {
				try {
					var datas = data.split(",", 3);
					if (data != undefined && datas[0] == "false") {
						alert("对不起，您没有登录，请您登录。");
						location.href = s_getlogin + "?url=" + encodeURIComponent(location.href);
						return;
					} else if (data != undefined && datas[1] == "false") {
						// 没有权限
						return;
					}
					flag = true;
				} catch (e) {
					flag = false;
					alert("出现未知错误：" + e);
				}
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				flag = false;
				alert('status：' + XMLHttpRequest.status + ',state：' + XMLHttpRequest.readyState + ',text：' + (textStatus || errorThrown));
			}
		});
		return flag;
	};

	init(); // 初始化

	var obj = new Object();
	obj.sessionKey = "US_SESSION_ID";
	obj.nameKey = "US_USER_NAME";
	obj.nameCnKey = "US_USER_NAME_CN";
	obj.userName = userName;
	obj.userNameCn = userNameCn;
	obj.logout = logout;
	obj.checkurl = checkurl;
	obj.getDomain = getDomain;
	return obj;
};
