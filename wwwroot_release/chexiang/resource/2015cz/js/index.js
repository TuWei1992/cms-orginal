(function ($) {

	var fns;

	fns = {

		// Script for initialization.
		init: function () {

			var _body;

			_body = $('body');

			this._set.fade();
		},

		_set: {

			// Script for start banner effect.
			fade: function(){
					var index = 0,
						MyTime,
					    maximg = $('#j-banner-content li').length;
					    if(maximg<2){
					    	$("#j-banner-content li").eq(0).addClass('ani-scale');						
					    	return false;
					    }
					    ShowjQueryFade(0);
					    index=1;
					$("#j-banner-num span").hover(function(){
						if(MyTime){
							clearTimeout(MyTime);
						}
						index  =  $("#j-banner-num span").index(this);
						var idx = $('#j-banner-content .current').index();
						if(index !=idx){							
							MyTime = setTimeout(function(){
								//$('#j-banner-content').stop();
								ShowjQueryFade(index);
								index++;
								if(index==maximg){
										index=0;
									}
							} , 400);
						}

					}, function(){
						clearTimeout(MyTime);
						autoPlay();
					});
					//滑入 停止动画，滑出开始动画.
					 $('#j-banner-content').hover(function(){
							if(MyTime){
								clearTimeout(MyTime);
							}
					 },function(){
						autoPlay();
					 });
					//自动播放
					autoPlay();

					function autoPlay(){
						MyTime = setTimeout(function(){
								clearTimeout(MyTime);
								ShowjQueryFade(index);
								index++;
								if(index==maximg){
									index=0;
								}
								autoPlay();
						} , 4000);
					}
				function ShowjQueryFade(i) {		
						$("#j-banner-content li").eq(i).removeClass('ani-scale').animate({'opacity': 'show'},1000).css({'z-index':1}).addClass('current ani-scale').siblings().removeClass('current').animate({'opacity': 'hide'},1000).css({'z-index':0});
						if($("#j-banner-content li").filter(':not(:animated)').length<maximg){$("#j-banner-content li").filter(':not(:animated)').removeClass('cur ani-scale');}
						$("#j-banner-num span").eq(i).addClass("cur").siblings().removeClass("cur");
					}
			}

		}

	};

	$(function(){
		fns.init();
		//视频
	  	var videoList=$(".con-side li");
	  	$(".con-side li").click(function(index){
	  		$(".con-side .hover").removeClass("hover");
	  		$(this).addClass("hover");
	  		$(".video-box iframe").attr("src",$(this).attr("video-src")); 
	  		$(".video-box iframe").attr("src",$(this).attr("video-src")); 	
	  		$(".c-list ul").addClass('displayN').eq($(this).index()).removeClass('displayN');
	  		$(".c-title").addClass('displayN').eq($(this).index()).removeClass('displayN');         
	  	})
	  	$(".button-box .button").click(function(){
	  		var commentInfo=$(".c-list textarea").val();
	  		if(commentInfo==""){
	  			$(".submit-msg").html("内容不能为空！").show();
	  		}else{
				$.ajax({url:"",async:false,info:commentInfo});
				$(".submit-msg").html("提交成功！").show();
				$(".c-list textarea").val("")
			}
	  	});
	  	var titleTops;
		var navTitles=$(".mod-tit");
		function initNavTitlesTop(){
			titleTops=[];
			navTitles.each(function(){
				titleTops.push($(this).offset().top);
			});
		}
	 	window.onload=function(){
	 		initNavTitlesTop();
	 		//是否被点击
	 		var flagClick=false;
			var fixedNavs=$(".slide-nav li:not(.not)");
			var carTabIndex=0;
			var carFixedNav=$(".slide-nav");
			var fiexedNavTop=500;
			$(window).scroll(function() {
				var windowTop=$(document).scrollTop();
				if(windowTop>fiexedNavTop){
					carFixedNav.show();
				}else{
					carFixedNav.hide();
				}
				if(!flagClick){
					var scrollTop=windowTop;
					var fixedSelectedNav=$(".slide-nav li.cur");
					for(var i=0;i<titleTops.length;i++){
						if((i!=titleTops.length-1&&scrollTop>=titleTops[i]&&scrollTop<titleTops[i+1])||(i==titleTops.length-1&&scrollTop>=titleTops[i])){
							if(fixedNavs.eq(i)!=fixedSelectedNav){
								fixedSelectedNav.removeClass("cur");
								fixedNavs.eq(i).addClass("cur");
								carTabIndex=i;
							}
							break;
						}
					}
				}
			});
			/*固定表头*/
			fixedNavs.each(function(i){
				$(this).click(function(){
					if(i!=carTabIndex){
						carTabIndex=i;
						$(".slide-nav li.cur").removeClass("cur");
						fixedNavs.eq(i).addClass("cur");
						flagClick=true;
						window.scrollTo(0,titleTops[i]);
						setTimeout(function(){flagClick=false;},1000);
					}
				})
			});
	 	}
	});

} (jQuery));