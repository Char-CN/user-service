jQuery(document).ready(function() {
	var arrs = [4,5,6,7,8];
	var slides = [];
	for (var i in arrs) {
		slides.push({
			image : 'images/login/' + arrs[i] + '.jpg',
			title : arrs[i]
		});
	}
	$.supersized({
		random : 1, //Randomize slide order (Ignores start slide)
		slide_interval : 1, //Length between transitions
		transition_speed : 5000,
		slides : slides
	});
});