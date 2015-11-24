/**
 * Copyright (C), 2015, 上海赛可电子商务有限公司
 * Author:   康明飞
 * Date:     2015-4-29
 * Description: 通用的js逻辑
 */
;(function(){
	var arg = {
	    text: "请输入品牌、级别、车系、车型",
	    header: null,
	    body: ["kw"],
	    max: 8,
	    target: "_blank",
	    dataurl: "/mall/searchNotice.htm?prefix={#KEY}&jsonP={#NAME}",
	    hotSearchList: [{"kw":"别克"},{"kw":"荣威"},{"kw":"雪佛兰"}],
	    callback: function(arg) {}
	};
	var mallSuggest = new MallSuggest("attendSymbol", arg);
	var suggestNode = document.getElementById("attendSymbol");
	suggestNode.onkeyup = suggestNode.onafterpaste = function(){
		var ignoreChars = ["<", ">", "?", "\uff1f"];
		var regIgnoreChars = new RegExp("[" + ignoreChars.join(",") + "]", "gi");
		this.value = this.value.replace(regIgnoreChars,'');
	};
})();

/**
 *  Author:   张卫明
 *  Date:     2015-5-22
 *  Description: 通用 工具类
*/

;(function($,window,ECar){

	ECar.Tool = {};

	/**
	 * 判断是否为IE
	 */
	
	ECar.Tool.isIE6 = /msie 6/i.test(navigator.userAgent);
	ECar.Tool.isIE7 = /msie 7/i.test(navigator.userAgent);
	ECar.Tool.isIE8 = /msie 8/i.test(navigator.userAgent);
	ECar.Tool.isIE = /msie/i.test(navigator.userAgent);
	
	/**
	 * indexOf 兼容IE
	 */
	if(!Array.indexOf)
	{
	    Array.prototype.indexOf = function(obj)
	    {             
	        for(var i=0; i<this.length; i++)
	        {
	            if(this[i]==obj)
	            {
	                return i;
	            }
	        }
	        return -1;
	    }
	}

})(jQuery,window,window.ECar||(window.ECar={}));

/**
 *  Author:   张卫明
 *  Date:     2015-5-7
 *  Description: slidebar 侧边栏 js
*/

var feedbookEle = $('#feedbook'),feedbookHtml = feedbookEle.html();
;(function($,window,ECar){
	// Script for start IM functions.

    window.sidebar = {

		config: {

			scrollTop: 	'0',

			duration: 	1000,
			
			minHeight:	380,
			
			minWidth:	1230,

			easing:		'swing',
			
			mainSlide:	$('#mall-sidebar'),
			
			sideAbs:	$('.side-abs'),
			
			sideFix:	$('.side-fix'),

			csim:		$('.mainsite-im'),

			csimUrl:	'http://kf1.chexiang.com/new/client.php?unique_id=&unique_name=&arg=admin&style=2&l=zh-cn&lytype=0&charset=gbk&referer=http%3A%2F%2Fwww.chexiang.com%2F&isvip=bcf14bbb85a346c2fb52e8cea8822cce&identifier=&keyword=&tfrom=1&tpl=crystal_blue',

			eleToTop:	$('.backtoTop'),
			
			eleFeed:	$('.cmp-enter'),
			
			mallMy:	$('#mall-my'),
			
			showAnimate:true
		},

		init: function () {
			
			var _this = this;
			
			this.sizeWindow();
			
			this.jumpForwardToCS();

			this.scrollToTop();
			
			this.submitFeed();
			
			this.myCollect();
			
			$(window).resize(function() {
				_this.sizeWindow();
			});
			
			
		},

		jumpForwardToCS: function () {

			var _this;

			_this = this;

			this.config.csim.on('click', function () {

				window.open(_this.config.csimUrl, "车享客服", "height=573, width=803, top=80, left=300,toolbar=no, menubar=no, scrollbars=no, resizable=yes, location=n o, status=no");

			});

		},

		scrollToTop: function () {

			var _config;

			_config = this.config;

			this.config.eleToTop.on('click', function (e) {

				var win, doc;

				win = $(window);

				doc = $('html, body');

				if (win.scrollTop() <= 0) {

					e.preventDefault();

					e.stopPropagation();

				} else {

					doc.stop().animate({

						'scrollTop': _config.scrollTop

					}, _config.duration, _config.easing);

				}

			});
			
			//mouseover 动画
			this.eleAnimate(this.config.eleToTop);
			
		},
		myCollect:	function(){
			// 调用快速登录
			this.config.mallMy.on('click',function(){
				backCallName = 'sidebarMyCollect';
				if( typeof(checkLogin)!='undefined' && typeof(checkLogin) == 'function'){
					checkLogin(MALL.accountQuickBase,'B');
				}
			});
		},
		_myCollect :function(){
			if($.trim(this.config.mallMy.attr('href')) == 'javascript:void(0)'){
				var _base = MALL.base;
				window.location.href = _base+'/member/myCollect.htm';
			}
		},
		
		submitFeed: function(){
			var _this = this;
			//feedbookEle.remove();
			
			this.config.eleFeed.on('click', function () {
                _this.openFeedbook();
			});
			
			//mouseover 动画
			this.eleAnimate(this.config.eleFeed);

		},
        openFeedbook:function(){
        	// 调用快速登录
        	backCallName = 'sidebarFeedbook';
        	if( typeof(checkLogin)!='undefined' && typeof(checkLogin) == 'function'){
        		checkLogin(MALL.accountQuickBase,'B');
        	}
        },
        //提交按钮事件
        openFun:function(){
            document.getElementById('submitFeed').onclick = function(){

                var $feedTypeValue = getRadioValue('feedbackType'),$questionValue = $.trim($('#question').val());

                if(!$feedTypeValue){
                    $('.submit-error').html('请选择反馈类型');
                    return false;
                }

                if($questionValue == ''){
                    $('.submit-error').html('请填写意见');
                    return false;
                }

                var $data = {
                    'feedbackType' : $feedTypeValue,
                    'question'		: $questionValue
                };

                $.ajax({
                    url	:'/member/addFeedback.htm',
                    type:'post',
                    data:$data,
                    success:function(data){
                        if(data == 'true'){
                            ECar.easyDialog.close();
                            ECar.easyDialog.open({
                                container : {
                                    header : '提交反馈',
                                    content : '<div style="text-align:center">提交成功</div>'
                                },
                                autoClose:3000,
                                callback:function(){
                                	window.location.reload(true);
                                }
                            });
                        } else {
                            ECar.easyDialog.close();
                            ECar.easyDialog.open({
                                container : {
                                    header : '提交反馈',
                                    content : '<div style="text-align:center">提交失败</div>'
                                },
                                autoClose:3000,
                                callback:function(){
                                	window.location.reload(true);
                                }
                            });
                        }
                    },
                    error:function(){

                    }
                });

            };

            //获取反馈类型
            function getRadioValue(name){
                var radioes = document.getElementsByName(name);
                for(var i=0;i<radioes.length;i++){
                    if(radioes[i].checked){
                        return radioes[i].value;
                    }
                }
                return false;
            }

        },
		eleAnimate:function(_ele){
			
			_ele.hover(function(){
				$('.advice-pop',this).show();
				$('.advice-pop',this).css({'opacity':'0'});
				$('.advice-pop',this).animate({'right':'40px','opacity':'1'});
			},function(){
				var _this = this;
				$('.advice-pop',this).animate({'right':'50px','opacity':'0'},function(){
					$('.advice-pop',_this).hide();
				});
			});
			
		},
		sizeWindow:function(){
			
			//屏幕宽度发生变化
			this.sizeWindowWidth();
			
			//屏幕宽度高度变化
			this.sizeWindowHeight();
			
		},
		sizeWindowHeight:function(){
			var _this = this;
			
			if($(window).height() <= this.config.minHeight){
				
				if(!this.config.sideAbs.hasClass('min-side-abs')){
					this.config.sideAbs.addClass('min-side-abs');
				}
				
			} else {
				
				if(this.config.sideAbs.hasClass('min-side-abs')){
					this.config.sideAbs.removeClass('min-side-abs');
				}
				
			}
			
			$('li',this.config.sideAbs).unbind('mouseenter mouseleave');
			
			if($(window).height() <= this.config.minHeight){
				$('li',this.config.sideAbs).each(function(){
					_this.eleAnimate($(this));
				});
			}
			
		},
		sizeWindowWidth:function(){
			
			/*var _this = this,_right = this.config.mainSlide.css('right').replace('px','')*1,_timer = 0;
			
			this.config.mainSlide.unbind('mouseenter mouseleave');
			this.config.sideFix.unbind('mouseenter mouseleave');
			
			
			if($(window).width() > this.config.minWidth){
				
				if(_right<0 && this.config.showAnimate == true){
					animateMove('show');
				}
				
				return;
			}
			
			if(_right>=0 && this.config.showAnimate == true){
				_timer = setTimeout(function(){animateMove('hide');},1000);
			}
			
			this.config.sideFix.hover(function(){
				if(_this.config.showAnimate == true){
					animateMove('show');
				}
			},function(){});
			
			this.config.mainSlide.hover(function(){
				if(_timer != 0){
					clearTimeout(_timer);
				}
			},function(){
				if(_this.config.showAnimate == true){
					_timer = setTimeout(function(){animateMove('hide');},1000);
				}
			});
			
			function animateMove(_type){
								
				if(_timer != 0){
					clearTimeout(_timer);
				}
				
				if(_type == 'show'){
					var moveRight = '0px';
				} else {
					var moveRight = '-35px';
				}
				
				_this.config.showAnimate = false;
				
				_this.config.mainSlide.animate({'right':moveRight},function(){
					_this.config.showAnimate = true;
				});
				
			};
			
			*/
			var _this = this,_right = this.config.mainSlide.css('right').replace('px','')*1,_timer = 0;
			
			if($(window).width() > this.config.minWidth){
				
				if(_right<0 && this.config.showAnimate == true){
					animateMove('show');
				}
				
				return;
			}
			
			window.onmousemove=function(e){
				e=e? e:window.event;
				
				if($(window).width() <= e.clientX+35){
					
					clearTimeout(_timer);
					_timer = setTimeout(function(){
						if(e.clientX==e.clientX){
							if(_this.config.showAnimate == true){
								animateMove('show');
							}
						};
					},500);
					
				} else if($(window).width() > e.clientX){
					
					if(_right>=0 && _this.config.showAnimate == true){
						animateMove('hide');
					}
					
				}
			
			};
			
			function animateMove(_type){
				
				if(_timer != 0){
					clearTimeout(_timer);
				}
				
				if(_type == 'show'){
					var moveRight = '0px';
				} else {
					var moveRight = '-35px';
				}
				
				_this.config.showAnimate = false;
				
				_this.config.mainSlide.animate({'right':moveRight},200,function(){
					_this.config.showAnimate = true;
				});
				
			};
			
		}
		
	};

    window.sidebar.init();
	
})(jQuery,window,window.ECar||(window.ECar={}));


/* Author:   张卫明
* Date:     2015-6-1
* Description: tooltip js
*/
;(function($,window,ECar){

	ECar.tooltip = function(){
		
		//生成tooltip结构
		var $tool = $("*[tooltip]"),
			$toolHtml = '<div class="tooltip" id="tooltip"><p id="tooltip-content"></p><div class="tooltip-i"><i></i></div></div>';
		$('body').append($toolHtml);
		
		var _tooltip = $('#tooltip'),
			_content = $('#tooltip-content'),
			_toolArrow = $('.tooltip-i','#tooltip'),
			TipAutoHideID = 0;
		
		//为tooltip添加事件
		$tool.each(function(){
			var elem = $(this);
			
			$(this).hover(function(event) { //show
				
				updateContent(elem);
				
                updatePosition(event,elem);
                
                _tooltip.show().animate({opacity:0.9});
                
				if (TipAutoHideID>0){
					clearTimeout(TipAutoHideID);
					TipAutoHideID = 0;
				}
				TipAutoHideID = setTimeout(function(){
					_tooltip.hide().css({opacity:0.3});
				},100000);
				
            },function() { 					// Hide
            	
				updateContentBack(elem);
				_tooltip.hide().css({opacity:0.3});
				if (TipAutoHideID>0){
					clearTimeout(TipAutoHideID);
					TipAutoHideID = 0;
				}
				
            });
			
		});
		
		var setPosition = function(posX, posY) {
			_tooltip.css({ left: posX, top: posY });
        };
		
		/*更新 tooltip 位置 */
		var updatePosition = function(event,elem) {
			
			var $thisArrow = (elem.attr('arrow') =='bottom' ? 'bottom':'top'),
				$thisHeight = elem.height(),
				tooltipWidth = _tooltip.outerWidth(),
				tooltipHeight = _tooltip.outerHeight(),
				$window = $(window),
				windowWidth = $window.width() + $window.scrollLeft(),
				windowHeight = $window.height() + $window.scrollTop(),
				posX = elem.offset().left+elem.innerWidth()/2 - tooltipWidth/2;
            
            if($thisArrow == 'bottom'){
            	var posY = elem.offset().top + $thisHeight+6;
				_toolArrow.attr('class','tooltip-i tooltip-i-b');
			} else {
            	var posY = elem.offset().top - tooltipHeight-6;
				_toolArrow.attr('class','tooltip-i tooltip-i-t');
			}
            
            setPosition(posX,posY);
            
        };
		
		/*构建 tooltip内容 */
		var updateContent= function(elem) {
			_content.html(elem.attr('tooltip'));
		};
		
		/*清除 tooltip内容 */
		var updateContentBack = function(elem) {
			_content.html('');
		};
		
	};
	
})(jQuery,window,window.ECar||(window.ECar={}));

/* Author:   张卫明
* Date:     2015-5-22
* Description: 快速登录 js
*/

window.backCallName = '';
/**快速注册登录成功后的回调函数*/

window.backCallFun = function(obj){
	
	//注册登录成功后的后续操作
	
	switch (backCallName) {
		/* 详情页 */
		case 'goPreOrder':
			detailPage.goPreOrderDetail();
			break;
		case 'goAddCollection':
			detailPage.goAddCollectionDetail();
			break;
		case 'goCancelCollection':
			detailPage.goCancelCollectionDetail();
			break;
		/* 通用侧边栏  */
		case 'sidebarFeedbook':
			ECar.easyDialog.open({
                container : {
                    header : '提交反馈',
                    content : feedbookEle
                },
                success: sidebar.openFun
            });
			
			break;
		case 'sidebarMyCollect':
			window.sidebar._myCollect();
			break;
		default:
			window.location.reload(true);
			break;
	};
	
};


/*豆腐块 标签垂直居中*/

;(function($,window,ECar){
	
	if(ECar.Tool.isIE7 || ECar.Tool.isIE6){
		var $lab = $('.lab-zj');
		$lab.each(function(){
			var $labHtml=$(this).html();
			$(this).html('<span class="ieLab">'+$labHtml+'</span>');
			
		});
		
		$('.ieLab').each(function(){
			var ieHe = $(this).height();
			$(this).css({'top':'50%','margin-top':-1*ieHe/2+'px'});
		});
	};
	
})(jQuery,window,window.ECar||(window.ECar={}));

