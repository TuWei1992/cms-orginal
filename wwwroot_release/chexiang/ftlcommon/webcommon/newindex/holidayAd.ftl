<div id="doubleEightAd" style="width: 990px;height:50px;margin:0 auto;display:none;">
	
</div>
<script>
	(function ($) {
		var $adbanner = $("#adbanner"),
			$topbanner = $("#topbanner"),
			$doubleEightAd = $("#doubleEightAd");
		$doubleEightAd.load("${base}/common/holidayad.htm", function () {
			if($adbanner.length===0 || ($adbanner.length>0 && $topbanner.data("slideup")===true)) {
				//$doubleEightAd.show();
				$doubleEightAd.find("img").on("load", function () {
					$doubleEightAd.slideDown(300);
				});
			} else {
				$doubleEightAd.hide();
			}
		});
	} (jQuery));
</script>