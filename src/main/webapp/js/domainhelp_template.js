var $domain = function() {
	var getDomain = function() {
		var domain = location.href.match(new RegExp("${DOMAIN_REG}"));
		if (domain == null || domain == undefined) {
			alert("对不起，域名不正确。");
		}
		return domain[1];
	};
	var obj = new Object();
	obj.getDomain = getDomain;
	return obj;
};
var $domain = new $domain();