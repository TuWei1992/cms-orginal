$(function(){				
	//set city
	getCookie();
	// 运行
	city();
	getCityList();	

	// 判断用户是否已登录
	$.ajax({
		url:(typeof isOrder!="undefined"&&isOrder?orderBase:base) + "getUserInfo.htm",
		dataType: "json",
		cache: false,
		success: function(data) {
			var unLogin = $("#js-unlogin-box"),
				loginIn = $("#js-login-box");
			if(data.loginStatus){
				if(data.getMember){
					$('.js-nickkname',loginIn).text(data.displayName);
					if(data.photoUrl){
						$('.js-avatar',loginIn).attr('src',window.img2Base + data.shortPhotoUrl);
					}
					$('.js-grade-num',loginIn).text(data.balance);
					if(data.isCertified){
						$('.js-certified',loginIn).css('display','block');
					}else{
						$('.js-un-certified',loginIn).css('display','block');
					}
				}
				unLogin.hide();
				loginIn.show();

// 				 if(data.memberCenterInfo.nickName==null || data.memberCenterInfo.nickName=="")
//                 {
//                 	$("#js-login-box").find('.js-nickname').text(data.greetings + '，'+data.realName).attr("title",data.greetings + '，'+data.memberCenterInfo.defaultAccount);
//                 }else{
//                     $("#js-login-box").find('.js-nickname').text(data.greetings + '，'+data.realName).attr("title",data.greetings + '，'+data.memberCenterInfo.nickName);
//                 }
				// if(data.unreadMsgCount == 0){
				// 	$('.gotomsg-item').hide();
				// }else if(data.unreadMsgCount > 0){
				// 	$(".hidden-num").text(data.unreadMsgCount);
				// 	$(".gotomsg").text(data.unreadMsgCount).show();
				// 	if(data.unreadMsgCount>99){
				// 		$(".hidden-num").text('99+');
				// 		$(".gotomsg").text('99+');
				// 	}

				// }
			

			}else{
				unLogin.show();
				loginIn.hide();
			}
		}
	});

	
	$(".login-bind").on("click", function(){
		var backUrl = window.location.href;
		window.location.href = accountBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);
		return false;
	});

	$(".register-bind").on("click", function(){
		var backUrl = window.location.href;
		window.location.href = accountBase + "/account/m_register.htm?backUrl=" + encodeURIComponent(backUrl);
		return false;
	});

	$(document.body).on('click',function(e){
		if (!$(e.target).closest('.js-city').length) {
			$('#cityPos').removeClass('city-cur');
			$('#citychage').hide();
		}
	});

	$('#cityPos').on('click',function(){
		if($(this).hasClass('city-cur')){
			$(this).removeClass('city-cur');
			$('#citychage').slideUp();
			return false;
		}
		
		$(this).addClass('city-cur');
		$('#citychage').slideDown();
		return false;
	});


	showDrop($('#userCenter'),$('.user-hd'),"user-cur",$('.group'));

	//aniScore();


	$('#js-login-box').hover(function(){
		$(this).addClass('user-detail');
	},function(){
		$(this).removeClass('user-detail');
	});

	navEffect();
	 
	popWinOp();
	setAnimation();
});
// error img
$('.brand-data img').error(function(){
	$(this).attr('src',"http://i1.cximg.com/images/210x140/1/p.jpg");
});

// header

//通用下拉弹窗
;function showDrop(handleDom,child,onClass,dropDom){
	var timeID=null;
	handleDom.mouseover(function(){
		clearTimeout(timeID);
		child.addClass(onClass);
		dropDom.show();
	}).mouseout(function(){
		timeID=setTimeout(function(){
			dropDom.slideUp(200, function(){
			child.removeClass(onClass);
		});
		},500);
	});
}
// 城市切换
;function city(){
	var cityShow = $('#currentCity'),					// 显示城市
		cityHandle = $('#cityPos'),	// 点击下拉区域
		cityNow  = cityShow.text(),						// 当前城市名称
		cityList = $('#citychage');			     	  	// 城市列表

	// 选中城市
	cityList.on('click',"a", function(){
		var cityid = $(this).attr('cityid'),	// 城市id
			cityname = $(this).attr('name');	// 城市name
		cityCookie(cityid, cityname);			// 更新cookie
		cityShow.attr({'cityid':cityid, 'name':cityname}).text(cityname);
		cityList.hide();
		getCookie();
		window.location.reload(true);						// 刷新页面

	});
};

// 存储cookie
;function cityCookie(cityId, cityName,domain){
	var expires = new Date();
	if(!domain){
		domain=document.cookie.match(/(^| )city\.domain=([^;]*)(;|$)/);
		domain&&(domain=domain[2]);
	}
	/* 三个月 x 一个月当作 30 天 x 一天 24 小时 x 一小时 60 分 x 一分 60 秒 x 一秒 1000 毫秒 */
	expires.setTime(expires.getTime() + 12 * 30 * 24 * 60 * 60 * 1000);
	var domainStr = domain&&domain!="null"?(";domain="+domain):"";
	document.cookie = "cityId="+encodeURIComponent(cityId)+";expires="+expires.toGMTString()+";path=/"+domainStr;
	document.cookie = "city.id="+encodeURIComponent(cityId)+";expires="+expires.toGMTString()+";path=/"+domainStr;
	document.cookie = "city.name="+encodeURIComponent(cityName)+";expires="+expires.toGMTString()+";path=/"+domainStr;
	document.cookie = "city_id="+encodeURIComponent(cityId)+";expires="+expires.toGMTString()+";path=/"+domainStr;
	document.cookie = "city_name="+encodeURIComponent(cityName)+";expires="+expires.toGMTString()+";path=/"+domainStr;
	document.cookie = "city.domain="+encodeURIComponent(domain)+";expires="+expires.toGMTString()+";path=/"+domainStr;
};

// 读取cookie
;function getCookie(){
	var cookie = {},
		setCookie = document.cookie,
		cityId = '',
		cityName = '上海',
		cityShow = $('#currentCity');		// 显示城市
	if( setCookie ==="" ){
		// 默认城市--上海
		cityCookie('310100', '上海');
		cityShow.attr({'cityid':'310100', 'name':'上海'}).text('上海');

	} else{
		var list = setCookie.split("; ");
		for( var i=0; i<list.length; i++ ){
			var cookie = list[i];
			var p = cookie.indexOf("=");
			var name = cookie.substring(0,p);
			var value = cookie.substring(p+1);
			try {
				value = decodeURIComponent(value);
			} catch(e) {
				continue;
			}
			cookie[name] = value;

			// 获取cookie值
			if (name == "city.id" || name == "city_id") cityId = value;
			if (name == "city.name" || name == "city_name") cityName = value;

		}

		// 没有在cookie中获取到城市则在线获取
		if (cityId == '' || cityName == '') {
			setCityCookie();
		}

		cityShow.attr({'cityid':cityId, 'name':cityName}).text(cityName);
	}
};

// 设置当前用户公网ip所在的城市，如果不在8城市站则返回上海
;function setCityCookie(){
	// 获取所在城市
	$.getJSON("http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=&callback=?", function(data) {
		var cityName = "";
		if(data){
			if(data.status==0) {
				cityName = data.address.split("|")[2];
			}
		}
		// 根据城市名获取id
		$.getJSON(base + "/common/cityid.htm?cityName="+cityName, function(data) {
			if(data){
				cityCookie(data.cityId, data.cityName,data.domain);
				//$('#currentCity').attr({'cityid':data.cityId, 'name':data.cityName}).text(data.cityName);
			}
		});
	});
}


//(typeof isOrder!="undefined"&&isOrder?orderBase:base)
;function getCityList(){
	 $.getJSON("" + "/cityList.shtml",function(data){
		 var frg=$(document.createDocumentFragment());
		 $.each(data,function(i,item){
			 var tmp=$('<a href="javascript:void(0);" target="_self" cityid="'+item.code+'" name="'+item.name+'" class="city-name"><span>'+item.name+'</span></a>');
			 tmp.appendTo(frg);
		 });
		 frg.appendTo($("#citychage"));
	 });
}


;function navEffect(){
		var navTimeID,titTimeID,
			navAllBarnd = $('#j-nav-all-brand');

			$('.brand-box-con',navAllBarnd).each(function(){
				var conItem = $(this).find('.con-item'),
					n = conItem.length;
					w = conItem.outerWidth() * n;
				$(this).css('width',w);
			})

			navAllBarnd.hover(function(){
				clearTimeout(navTimeID);
				$(this).addClass('nav-brand-info');
			},function(){
				var that = $(this);
				navTimeID=setTimeout(function(){
					that.removeClass('nav-brand-info');
				},500);
			});

			$('.brand-tit-item',navAllBarnd).hover(function(){
				var that = $(this),brandId= that.data('brand');
					clearTimeout(titTimeID);
					$('.brand-tit-item',navAllBarnd).removeClass('tit-item-cur');
					$('.brand-box-con',navAllBarnd).hide();
					that.addClass('tit-item-cur');
					$('.brand-box-con[data-brand='+brandId+']').show();				
			},function(){
				titTimeID=setTimeout(function(){
					$('.brand-tit-item',navAllBarnd).removeClass('tit-item-cur');
					$('.brand-box-con',navAllBarnd).hide();
				},500);
			});

			$('.brand-box-con',navAllBarnd).hover(function(){
				var that = $(this),brandId= that.data('brand');
				clearTimeout(titTimeID);
				$('.brand-tit-item',navAllBarnd).removeClass('tit-item-cur');
				$('.brand-box-con',navAllBarnd).hide();
				$('.brand-tit-item[data-brand='+brandId+']',navAllBarnd).addClass('tit-item-cur');
				$('.brand-box-con[data-brand='+brandId+']',navAllBarnd).show();
			},function(){
				titTimeID=setTimeout(function(){
					$('.brand-tit-item',navAllBarnd).removeClass('tit-item-cur');
					$('.brand-box-con',navAllBarnd).hide();
				},500);
			})
}
function Fn(){}
Fn.prototype.setCookie = function(name,value,path)
{
	var exp = new Date();
	if(!path){
		path='/';
	}
	var Days = 30*12; //此 cookie 将被保存天数
    var exp  = new Date(); 
    exp.setTime(exp.getTime() + Days*24*60*60*1000);

	document.cookie = name + "="+ encodeURIComponent(value) + ";expires=" + exp.toGMTString()+";path="+path+";";
};

Fn.prototype.getCookie = function(name)       
{
	var arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
	if (arr != null) return decodeURIComponent(arr[2]); return null;

};

Fn.prototype.delCookie = function(name)
{
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	var cval=getCookie(name);
	if(cval!=null) document.cookie= name + "="+cval+";expires="+exp.toGMTString();
};
Fn = new Fn();
;function popWinOp(){
	var popMask = $('#j-pop-mask'),
		popWin = $('#j-pop-win'),
		pageId = Fn.getCookie("indexBlockOrder");
		if(!pageId){
			popMask.show();
			popWin.show();
		}
		if(pageId == 2){
			$('.cx-hui').prependTo($('.js-mod-placeholder').eq(0));	
			$('.js-mod-placeholder').eq(1).html($('.cx-mall'));
			// $('.cx-hui').insertBefore($('.cx-mall'));
			// $('.cx-mall').insertAfter($('.act-box').eq(1));
		}

		$('.pop-close,.a-buy,.a-owner,.a-more',popWin).click(function(){
			var pageId = $(this).data('id');
			Fn.setCookie("indexBlockOrder",pageId, '');			
			if(pageId == 2){
				$('.cx-hui').prependTo($('.js-mod-placeholder').eq(0));		
				$('.js-mod-placeholder').eq(1).html($('.cx-mall'));
			}
			popMask.hide();
			popWin.hide();


		});
}
;function setAnimation(){
	var index = 0, MyTime;
	var oLis = $('#js-hui-show li');
	var oDots = $("#js-hui-page span");
	var oPrev = $(".js-prev");
	var oNext = $(".js-next");
	var maximg = oLis.length;
		if(maximg<2){
		   oLis.eq(0).addClass('cur');						
		   	return false;
		}
		ShowjQueryFade(0);
		//index = 1;
		oDots.hover(function(){
			if(MyTime){
				clearTimeout(MyTime);
			}
			index  =  oDots.index(this);
			var idx = $('#js-hui-show .cur').index();
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
		$('#js-hui-show').hover(function(){
			if(MyTime){
				clearTimeout(MyTime);
			}
		},function(){
			autoPlay();
		});

		// prev click 事件
		oPrev.click(function(){
			if(MyTime){
				clearTimeout(MyTime);
			}
			index = $("#js-hui-page .cur").index();
			index--;
			if(index < 0 ){
				index = maximg -1;
			}
			ShowjQueryFade(index);
		}).hover(function(){
			clearTimeout(MyTime);
		},function(){
			autoPlay();
		});

		// next click 事件
		oNext.click(function(){
			
			if(MyTime){
				clearTimeout(MyTime);
			}
			index = $("#js-hui-page .cur").index();
			index++;
			if(index==maximg){
				index=0;
			}
			ShowjQueryFade(index);
		}).hover(function(){
			clearTimeout(MyTime);
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
				if(index == maximg){
					index = 0;
				}
				autoPlay();
			} , 4000);
		}
		
		function ShowjQueryFade(i) {
			oLis.eq(i).animate({'opacity': 'show'},1000).css({'z-index':1}).addClass('cur').siblings().removeClass('cur').animate({'opacity': 'hide'},1000).css({'z-index':0});
			if( oLis.filter(':not(:animated)').length < maximg ){
				oLis.filter(':not(:animated)').removeClass('cur');
			}
			oDots.eq(i).addClass("cur").siblings().removeClass("cur");
		}
}

(function ($, window, ECar) {

	var fns;

	fns = {

		// Options.
		config: {
			docSrlBar: false,
			minWidth:	1235,
			sidebar:	$('#j-sidebar'),
			showAnimate:true
		},

		// Script for initialization.
		init: function () {

			var _body = $('body'),
				_this = this;


			// if (this.config.docSrlBar) {

			// 	this._set.docScrollBar();

			// }

			this._set.topAd();

			this._set.fade();
			this._set.modFade();
			this._set.tab();
			this._set.imgEffect();

			this._set.imgLazyLoad();


			this._set.monitor(_body);

			this._set.im.init();

			this._set.slt();

			this._set.sltOptSrl();

			this._set._form.init();

		},

		// Utils.
		helpers: {

			pdControl: function (e) {

				e.stopPropagation();

				e.preventDefault();

			},

			clickOrTouch: function (e) {

				return Modernizr.touch ? 'touchstart' : 'click';

			},

			getQueryString: function (url) {

				return url.substring(url.indexOf("?")+1);

			},

			Arg: function (name, url) {

				var parameters, pos, _key, _val;

				parameters = this.getQueryString(url).split("&");

				for(var i = 0; i < parameters.length; i++)  {

					pos = parameters[i].indexOf('=');

					if (pos == -1) {

						continue;

					}

					_key = parameters[i].substring(0, pos);

					_val = parameters[i].substring(pos + 1);

					if(_key == name) {

						return unescape(_val.replace(/\+/g, " "));

					}

				}

			}

		},

		_effect: {

		},
		_set: {

			// Redefinition for the scroll bar of document.
			docScrollBar: function () {

				var _root;

				_root = $(':root');

				_root.niceScroll({

					cursorcolor: "#999",

			        cursoropacitymin: 0,

			        cursoropacitymax: 1,

			        cursorwidth: 12,

			        cursorborder: "0 solid #fff",

			        cursorborderradius: 0,

			        zindex: 9999,

			        scrollspeed: 60,

			        mousescrollstep: 40,

			        touchbehavior: false,

			        hwacceleration: true,

			        boxzoom: false,

			        dblclickzoom: false,

			        gesturezoom: false,

			        grabcursorenabled: false,

			        autohidemode: true,

			        background: "",

			        iframeautoresize: true,

			        cursorminheight: 20,

			        preservenativescrolling: true,

			        railoffset: { top: 0, left: -5 },

			        bouncescroll: true,

			        spacebarenabled: true,

			        railpadding: { top: 0, right: 0, left: 0, bottom: 0 },

			        disableoutline: true,

			        horizrailenabled: false,

			        railalign: 'right',

			        railvalign: 'bottom',

			        enabletranslate3d: true,

			        enablemousewheel: true,

			        enablekeyboard: true,

			        smoothscroll: true,

			        sensitiverail: true,

			        enablemouselockapi: true,

			        cursorfixedheight: false,

			        hidecursordelay: 400,

			        directionlockdeadzone: 6,

			        nativeparentscrolling: true,

			        enablescrollonselection: true,

			        rtlmode: false,

			        cursordragontouch: true,

			        oneaxismousemode: "auto",

			        scriptpath: ""

				});

			},

			// Script for Top Sliding Ad.
			topAd: function () {

				var $adBlock, $adBanner, $closeBtn, $doubleEightAd, $holidayAdbanner,
					adBlockExists, _slideUp, hideAd;

				$adBlock = $('#topbanner');

				$adBanner = $('#adbanner');

				$closeBtn = $('.bannerclose',$adBlock);

				$doubleEightAd = $('#doubleEightAd');

				$holidayAdbanner = $("#holidayAdbanner");

				adBlockExists = $adBanner.length > 0 ? true : false;

				setTimeout(function () {

					$doubleEightAd.load($.trim($doubleEightAd.data("source")), function() {

						if ($adBanner.length === 0 || ($adBanner.length > 0 && $adBlock.data("slideup") === true)) {

							//$doubleEightAd.show();

							$doubleEightAd.find("img").on("load", function() {

								$doubleEightAd.slideDown(300);

							});

						} else {

							$doubleEightAd.hide();

						}

					});

				}, 0);




				_slideUp = function () {

					$adBlock.animate({

						height: 0

					}, {

						duration: 1000,

						done: function() {

							$adBlock.hide();

							$adBlock.data("slideup", true);

							if ($("#holidayAdbanner").length > 0) {

								$doubleEightAd.slideDown(500);

							}

						}

					});

				};

				hideAd = function () {

					setTimeout(function() {

						if ($adBlock.is(":visible") && !$adBlock.is(":animated")) {

							_slideUp();

						}

						$closeBtn.off();

					}, 3000);

				};

				if (adBlockExists) {

					$adBlock.slideDown({

						duration: 1000,

						always: hideAd()

					});

					// close button
					$closeBtn.one('click', function() {

						_slideUp();
						return false;

					});

				} else {

					if ($holidayAdbanner.length > 0) {

						$doubleEightAd.show();

					}

				}

			},

			// Script for start banner effect.
			fade: function(){
					var index = 0,
						MyTime,
						conWrap = $('#j-banner-content'),
						numWrap = $('#j-banner-num'),
						conItemWrap = conWrap.find('li'),
						numItemWrap = numWrap.find('span'),
					    maximg = conWrap.find('li').length;
					    if(maximg<2){
					    	numWrap.hide();
					    	return false;
					    }
					    ShowjQueryFade(0);
					    index=1;
					numItemWrap.hover(function(){
						if(MyTime){
							clearTimeout(MyTime);
						}
						index  =  numItemWrap.index(this);
						var idx = conWrap.find('.cur').index();
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
					 conWrap.hover(function(){
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
						conItemWrap.eq(i).animate({'opacity': 'show'},1000).css({'z-index':1}).addClass('cur').siblings().removeClass("cur").animate({'opacity': 'hide'},1000).css({'z-index':0});
						if(conItemWrap.filter(':not(:animated)').length<maximg){conItemWrap.filter(':not(:animated)').removeClass('cur');}
						numItemWrap.eq(i).addClass("cur").siblings().removeClass("cur");
					}
			},
			// Script for start mod-play effect.
			modFade: function(){

				$.fn.sliderEeffect = function () {

				    return this.each(function () {				        
				        	 if ($(this).size() == 0) {
					            return;
					        }
					        var modPlay = $(this);
					        var showWrap = modPlay.find(".show-wrap");
					        var pageWrap = modPlay.find(".page-num .page");
					        var itemWrap = showWrap.children();
					        var num = pageWrap.children();
					        var itemNum = itemWrap.length;
					        var itemWidth = itemWrap.eq(0).width();
					        var idx = 0;
					        var t = 0;
					        var timer = 5000;
					        if (itemNum <= 1) {
					            modPlay.find(".page").hide();
					            return
					        }
					        modPlay.find(".page li:gt(0) span").width(0);
					        num.bind("mouseenter",
					        function() {
					            idx = $(this).index();
					            sliderEffect();
					            pageCur();
					        });
					        modPlay.bind({
					            mouseenter: function() {
					                clearInterval(t);
					                pageCur();
					            },
					            mouseleave: function() {
					                autoPlay();
					                pageEffect();
					            }
					        });
					        function sliderEffect() {
					            showWrap.stop().animate({
					                left: -idx * itemWidth
					            });
					            num.removeClass("cur").eq(idx).addClass("cur");
					            pageEffect();
					        }
					        function autoPlay() {
					            t = setInterval(function() {
					                idx++;
					                if (idx > itemNum - 1) {
					                    idx = 0
					                }
					                sliderEffect();
					            },
					            timer);
					            pageEffect();
					        }
					        function pageEffect() {
					            var numCur = modPlay.find(".page li.cur");
					            modPlay.find(".page li span").stop().css("width", 0);
					            numCur.find("span").width(0).animate({
					                width: "30px"
					            },
					            timer,
					            function() {
					                $(this).width(0);
					            })
					        }
					        function pageCur() {
					            var numCur = modPlay.find(".page li.cur");
					            modPlay.find(".page li span").stop().css("width", 0);
					            numCur.find("span").stop().width("100%")
					        }
					        autoPlay();
					    });
					};
				
				$('.js-mod-play').sliderEeffect();
			},
			// Script for start tab effect.
			tab: function () {
				$.fn.infiniteCarousel = function () {
					function repeat(str, num) {
						return new Array( num + 1 ).join( str );
					}
				    return this.each(function () {
				        var $wrapper = $('.data-wrap', this).css('overflow', 'hidden'),
				            $slider = $wrapper.find('> ul'),
				            $items = $slider.find('> li'),
				            $single = $items.filter(':first'),
				            
				            singleWidth = $single.outerWidth(), 
				            visible = Math.ceil($wrapper.innerWidth() / singleWidth), // note: doesn't include padding or border
				            currentPage = 1,
				            pages = Math.ceil($items.length / visible),
				            cur=1;

				        // 1. Pad so that 'visible' number will always be seen, otherwise create empty items
				        if (($items.length % visible) != 0) {
				            $slider.append(repeat('<li class="empty" />', visible - ($items.length % visible)));
				            $items = $slider.find('> li');
				        }
				        var w = singleWidth * $items.length;
						$slider.width(w);
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

						$('.btn-left', this).addClass('btn-left-none');
						$wrapper.data('cur',1);
						$('.btn-left', this).click(function () {
							$(this).next().removeClass('btn-right-none');

							cur = $wrapper.data('cur') - 1;
							if(cur <= 1){
								cur = 1;
								$(this).addClass('btn-left-none');
							}
							$wrapper.data('cur',cur);
				            return gotoPage(currentPage - 1);
				        });
				        
				        $('.btn-right', this).click(function () {
				        	$(this).prev().removeClass('btn-left-none');
				        	cur = $wrapper.data('cur')*1 + 1;
				        	// console.log(cur+'--'+pages);
						   	if(cur >= pages){
								$(this).addClass('btn-right-none');
								cur=pages;
							}
							$wrapper.data('cur',cur);
				            return gotoPage(currentPage + 1);
				        });
				    });
				};

				$('.infiniteCarousel').infiniteCarousel();
				var itemShow = Math.floor(Math.random()*10),
					brandShow= $('.js-brand-show');
					$('.tab_menu li',brandShow).eq(itemShow).addClass('current').siblings().removeClass('current');
					$('.tab_item',brandShow).eq(itemShow).removeClass('hide').siblings().addClass('hide');

					$('.brand-wrapper').Tabs({
						timeout:500,
						callback:scrollPic
					});
				function scrollPic(container){
					$('.infiniteCarousel .data-wrap').data('cur',1)
					$('.infiniteCarousel .data-wrap').scrollLeft(0);
					$('.infiniteCarousel .btn-left').addClass('btn-left-none');
					$('.infiniteCarousel .btn-right').removeClass('btn-right-none');
				}
			},
			// Script for start img effect.
			imgEffect:function(){
				$('.js-mall-list img,.act-box-list img').hover(function(){
					$(this).stop().animate({marginTop:-5},300);
			  	},function(){
					$(this).stop().animate({marginTop:0},300);
				});
				$('.act-list li,.banner-mask-con .img-txt').hover(function(){
					$(this).children('dl').stop().animate({top:-5},300);
			  	},function(){
					$(this).children('dl').stop().animate({top:0},300);
				});

				$('#js-search-form').submit(function(){
					var sVal = $.trim( $("#keywords").val() );
					if(sVal){
						// sVal = sVal.replace(/<[^>]+>/g,"");
						// var sReg = ["<",">","?","\uff1f"];
						// var reg = new RegExp("["+sReg.join(",")+"]","gi");
						// sVal = sVal.replace(reg,"");
						// sVal = encodeURIComponent(encodeURIComponent(sVal));
						// var sUrl = carmallBase + "/multisearch/search.htm?car=1&kwd=" + sVal;
						var sUrl = carmallBase + "/multisearch/search.htm";
						
						$(this).attr('method','get');
						$(this).attr('action',sUrl);
					}else{
						var sUrl = carmallBase + "/list/tg-0-0-0-0-0-0-0-0-0-0-0-0-0-1-1.html";
						$(this).attr('method','post');
						$(this).attr('action',sUrl);
					}
				});
				$("#keywords").keyup(function(){
					var sReg = ["<",">","?","\uff1f"];
					var reg = new RegExp("["+sReg.join(",")+"]","gi");
					var oInput = $("#keywords");
					var sVal = $.trim( oInput.val() );
					if(reg.test(sVal)){
						oInput.get(0).value = oInput.get(0).value.replace(reg,"");
					}
				});
			},
			// Script for start image lazy load effect
			imgLazyLoad: function () {

				var imgs;

				imgs = $('.lazy');

				imgs.lazyload({

					effect: "fadeIn",

					threshold: 200

				});

			},

			// Script for start '秒针' monitor.
			monitor: function (container) {

				var _this, monitorHref;

				_this = fns;

				// monitorHref = container.find("a");

				var exposureUrlFromAdvert = $("body a[exposureUrl*='miaozhen']");
				exposureUrlFromAdvert.each(function(){
					$this = $(this);
					var exposureUrl = $this.attr("exposureUrl");
					if(exposureUrl){
						var img = new Image();
						img.src = exposureUrl;
					}
				});

			},

			// Script for start IM functions.
			im: {

				config: {

					scrollTop: '0',

					duration: 1000,

					easing: 'swing',

					csim: $('.mainsite-im'),

					csimUrl: 'http://kf1.chexiang.com/new/client.php?unique_id=&unique_name=&arg=admin&style=2&l=zh-cn&lytype=0&charset=gbk&referer=http%3A%2F%2Fwww.chexiang.com%2F&isvip=bcf14bbb85a346c2fb52e8cea8822cce&identifier=&keyword=&tfrom=1&tpl=crystal_blue',

					eleToTop: $('.backtoTop')

				},

				init: function () {

					this.jumpForwardToCS();

					this.scrollToTop();

					this.showQRCode();

					// this.advice();

				},

				jumpForwardToCS: function () {

					var _this = this,
						sidebar = $('#js-sidebar'),
						_body = $('body');

					$('.icon-client',sidebar).on('click',function(){
						$('.js-weixin',sidebar).removeClass('weixin-show');
						$(this).parents('.js-weixin').addClass('weixin-show');
						return false;
					});
					
					this.config.csim.on('click', function () {

						window.open(_this.config.csimUrl.replace(/.chexiang./g, "."+imBase+"."), "车享客服", "height=573, width=803, top=80, left=300,toolbar=no, menubar=no, scrollbars=no, resizable=yes, location=n o, status=no");
						return false;
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

				},

				showQRCode: function () {

					var h = $('html,body').height(),
							sidebar = $('#js-sidebar'),
							_body = $('body');
						// sidebar.height(h);

						$('.icon-weixin',sidebar).on('click',function(){
							$('.js-weixin',sidebar).removeClass('weixin-show');
							$(this).parents('.js-weixin').addClass('weixin-show');
							return false;
						});
						$('.js-weixin .close').on('click',function(){
							$(this).parents('.js-weixin').removeClass('weixin-show');
						});
						_body.on('click.chexiang.im', function (e) {

							if (!$(e.target).closest('.js-weixin').length) {
								$('.js-weixin',sidebar).removeClass('weixin-show');
							}

						});

				},

				advice:function(){
					//投诉建议查找入口链接
					var linkFirst = window.location.href;
					if(linkFirst.indexOf('complaint.shtml') != -1){
						var cmplinkFirst ="http://" + window.location.host  + window.location.pathname;
					}else{
						var cmplinkFirst = linkFirst;
					}
					var newlinkFirst =	cmplinkFirst.replace(/shtml/g,'shtm123escape');
					// var linkFirstURL = encodeURIComponent(newlinkFirst);
					var linkFirstURL = newlinkFirst;

					$('.cmp-enter a,.cmpLocation').each(function(){
						var newhref = $(this).attr("href") + "?sourceUrl=" + linkFirstURL;
						$(this).attr("href",newhref);
					});
				}

			},

			// Redefinition for the select element.
			slt: function () {

				var _slt;

				_slt = $('#frm-sell').find('select');
				_slt.selecter({

					label: '车辆所在地',

					cover: false,

					callback: function (val, idx) {}

				});
				
			

				$('#btn-submit-frm-sell').removeClass('btn-disabled');

			},

			// Redefinition for the scroll bar of select options panel.
			sltOptSrl: function () {

				var sltOpts;

				sltOpts = $('.selecter-options');

				sltOpts.niceScroll({

					cursorcolor: "#ccc",

			        cursoropacitymin: 0,

			        cursoropacitymax: 1,

			        cursorwidth: 5,

			        cursorborder: "0 solid #fff",

			        cursorborderradius: 0,

			        zindex: 9999,

			        scrollspeed: 60,

			        mousescrollstep: 40,

			        touchbehavior: false,

			        hwacceleration: true,

			        boxzoom: false,

			        dblclickzoom: false,

			        gesturezoom: false,

			        grabcursorenabled: false,

			        autohidemode: true,

			        background: "",

			        iframeautoresize: true,

			        cursorminheight: 20,

			        preservenativescrolling: true,

			        railoffset: { top: -5, left: -5 },

			        bouncescroll: true,

			        spacebarenabled: true,

			        railpadding: { top: 0, right: 0, left: 0, bottom: 0 },

			        disableoutline: true,

			        horizrailenabled: false,

			        railalign: 'right',

			        railvalign: 'bottom',

			        enabletranslate3d: true,

			        enablemousewheel: true,

			        enablekeyboard: true,

			        smoothscroll: true,

			        sensitiverail: true,

			        enablemouselockapi: true,

			        cursorfixedheight: false,

			        hidecursordelay: 400,

			        directionlockdeadzone: 6,

			        nativeparentscrolling: true,

			        enablescrollonselection: true,

			        rtlmode: false,

			        cursordragontouch: true,

			        oneaxismousemode: "auto",

			        scriptpath: ""

				});

			},

			// Script for start form validation functions.
			_form: {

				init: function () {
					this.setFocusAssist();

					this.setMessage();

					this.setCustomMethod();

					this.setDefault();

					this._vali();

				},

				setFocusAssist: function () {

					var field,fieldInp;

					field = $('.field-item'),
					fieldInp = $('.field-item input');

					field.on('click.chexiang.form.field', function (e) {

						if (e.target.nodeName == 'DIV') {

							$(this).children('input').focus();

						}

					});
					fieldInp.focus(function(){
						$(this).parent().addClass('field-item-focus');
					}).blur(function(){
						$(this).parent().removeClass('field-item-focus');
					});

				},

				setMessage: function () {

					$.extend($.validator.messages, {

						required: '必须填写',

						remote: '请修正此栏位',

						email: '请输入有效的电子邮件',

						url: '请输入有效的网址',

						date: '请输入有效的日期',

						dateISO: '请输入有效的日期 (YYYY-MM-DD)',

						number: '请输入正确的数字',

						digits: '只可输入数字',

						creditcard: '请输入有效的信用卡号码',

						equalTo: '你的输入不相同',

						extension: '请输入有效的后缀',

						maxlength: $.validator.format('最多 {0} 个字'),

						minlength: $.validator.format('最少 {0} 个字'),

						rangelength: $.validator.format('请输入长度为 {0} 至 {1} 之間的字串'),

						range: $.validator.format('请输入 {0} 至 {1} 之间的数值'),

						max: $.validator.format('请输入不大于 {0} 的数值'),

						min: $.validator.format('请输入不小于 {0} 的数值')

					});

				},

				setCustomMethod: function () {

					$.validator.addMethod('nowhitespace', function(value, element) {

						return this.optional(element) || /^\S+$/i.test(value);

					}, '不许存在空格。');

					$.validator.addMethod('phone', function(value, element) {

						return this.optional(element) || /^0?(13[0-9]|15[012356789]|18[0-9]|14[57])[0-9]{8}$/i.test(value);

					}, '请输入正确的手机号码。');

					$.validator.addMethod('notEqual', function(value, element, param) {

						return this.optional(element) || value !== $(param).val();

					}, '不可填写与左边相同的内容。');

				},

				setDefault: function () {

					$.validator.setDefaults({

						debug: true,

						onfocusin: false,

						onfocusout: false,

						onkeyup: false,

						focusInvalid: true,

						focusCleanup: false,

						success: function(error, element) {

							$(element).closest('.field-item').removeClass('_error');

						},

						errorPlacement: $.noop

					});

				},

				_vali: function () {

					var frmRegisterValior;

					frmRegisterValior = $('#frm-sell').validate({

						rules: {

							cityId: {

								required: true

							},

							carInfo: {

								required: true,
								maxlength: 25

							},

							userName: {

								required: true,
								maxlength: 10

							},

							mobilePhone: {

								required: true,

								phone: true,

								digits: true

							}

						},

						showErrors: function (errorMap, errorList) {

							var fisrtErrorMessage, firstErrorElement, errerArea;

							errerArea = $('#frm-sell .msg-txt');

							if (frmRegisterValior.numberOfInvalids() > 0) {

								fisrtErrorMessage = errorList[0].message;

								firstErrorElement = $(errorList[0].element);

								firstErrorElement.closest('.field-item').addClass('_error');
								fisrtErrorMessage === $.validator.messages.required ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : firstErrorElement.attr('id') === 'iptModel' ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : firstErrorElement.attr('id') === 'iptUser' ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : errerArea.html(fisrtErrorMessage);
								errerArea.parent().removeClass('hide');

								this.defaultShowErrors();

							} else {

								errerArea.parent().addClass('hide');

							}

						},

						submitHandler: function(form, event) {

							var fnClean, btnSubmit, makeBtnUseful,url;
							
							event.preventDefault();

							btnSubmit = $(form).find('input[type=submit]');

							fnClean = function (fld, slt) {

								fld.removeClass('_error');

								fld.find('input').val('');

								slt.find('option').eq(0).attr('selected', true);

								slt.selecter('update');

							};

							makeBtnUseful = function (btn, _if) {

								btn.prop('disabled', _if);

							};

							url = $('#frm-sell').attr('action');
							$('#frm-sell').attr('action',url+'?source=pc_index');
							if ($('html').hasClass('ie7') || $('html').hasClass('ie8')) {

								$(form).valid();

								if (frmRegisterValior.numberOfInvalids() === 0) {

									form.submit();

									makeBtnUseful(btnSubmit, true);

									setTimeout(function() {

										fnClean($('.field-item'), $(form).find('select'));

									},0);

									//fnClean($('.field-item'), $(form).find('select'));

									makeBtnUseful(btnSubmit, false);

								} else {

									frmRegisterValior.focusInvalid();

									return false;

								}

							} else {

								form.submit();

								makeBtnUseful(btnSubmit, true);

								setTimeout(function() {

									fnClean($('.field-item'), $(form).find('select'));

								},0);

								//fnClean($('.field-item'), $(form).find('select'));

								makeBtnUseful(btnSubmit, false);

							}
						}

					});
				}

			}

		}

	};
	

	$(function(){
		fns.init();
	});

} (jQuery, window, window.ECar));