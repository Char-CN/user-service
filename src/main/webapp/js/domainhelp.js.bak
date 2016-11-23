var $domain = function() {
	var getDomain = function() {
		var reg_str = "[http|https]://.*([.][a-zA-Z0-9]*[.][a-zA-Z0-9]*)/*.*";
		var domain = location.href.match(new RegExp(reg_str))[1];
		return domain;
	};
	var obj = new Object();
	obj.getDomain = getDomain;
	return obj;
};
var $domain = new $domain();