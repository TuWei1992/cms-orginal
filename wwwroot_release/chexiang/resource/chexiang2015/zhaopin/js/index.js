(function ($) {
	var fns;
	fns = {
		// Script for initialization.
		init: function () {
			this._set.fade();
			this._set.scrollEffect();
			this._set.faqEffect();
		},
		_set: {
			// Script for start banner effect.
			fade: function(){
					var index = 0,
						MyTime,
					    maximg = $('#j-banner-content li').length;
					    if(maximg<2){
					    	$('#j-banner-num').hide();
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
					$("#j-banner-content li").eq(i).animate({'opacity': 'show'},1000).css({'z-index':1}).addClass('current').siblings().removeClass('current').animate({'opacity': 'hide'},1000).css({'z-index':0});
					$("#j-banner-num span").eq(i).addClass("cur").siblings().removeClass("cur");
				}
			},
			// Script for start scroll effect.
			scrollEffect:function(){
					$.fn.infiniteCarousel = function () {
					function repeat(str, num) {
						return new Array( num + 1 ).join( str );
					}
				    return this.each(function () {
						var $wrapper = $('.data-wrap', this),
				            $slider = $wrapper.find('.data-wrapper'),
				            $items = $slider.find('.txt-list'),
				            $single = $items.filter(':first'),
				            
				            singleWidth = $single.outerWidth(), 
				            visible = Math.ceil($wrapper.innerWidth() / singleWidth), // note: doesn't include padding or border
				            currentPage = 1,
				            len = $items.length,
				            pages = Math.ceil(len / visible),
				            cur=1;
				            if(len<2){
				            	$('.btn-area', this).hide();
				            }

						// // 1. Pad so that 'visible' number will always be seen, otherwise create empty items
						// if (($items.length % visible) != 0) {
						// $slider.append(repeat('<li class="empty" />', visible - ($items.length % visible)));
						// $items = $slider.find('> li');
						// }

						// // 2. Top and tail the list with 'visible' number of items, top has the last section, and tail has the first
						// $items.filter(':first').before($items.slice(- visible).clone().addClass('cloned'));
						// $items.filter(':last').after($items.slice(0, visible).clone().addClass('cloned'));
						// $items = $slider.find('> li'); // reselect

						// 3. Set the left position to the first 'real' item
						var w = singleWidth * $items.length;
						$slider.width(w);
						$wrapper.scrollLeft(0);
				        // 4. paging function
				        function gotoPage(page) {
				            var dir = page < currentPage ? -1 : 1,
				                n = Math.abs(currentPage - page),
				                left = singleWidth * dir * visible * n;

				            $wrapper.filter(':not(:animated)').animate({
				                scrollLeft : '+=' + left
				            }, 500, function () {
				                currentPage = page;
				            });
				            return false;
				        }

						$('.icon-arrow-left', this).click(function () {
							if(--cur <= 1){							
								cur = 1;
							}
				            return gotoPage(currentPage - 1);
				        });
				        
				        $('.icon-arrow-right', this).click(function () {
						   	if(++cur >= pages){
								cur=pages;
							}
				            return gotoPage(currentPage + 1);
				        });
				    });
				};				
				$('#j-mod-news').infiniteCarousel();

			},
			// script for faq
			faqEffect:function(){
				$('.mod-faq dt').click(function(){
					$(this).parent().toggleClass('cur');
				});
			}

		}

	};

	$(function(){
		fns.init();
	});    
	
} (jQuery));
