var headerOption = {
	'city': true,
	'sign': true
};
;(function($){
	$(function(){
		if(headerOption.city){
			// set city
			getCookie();
			// 运行
			city();
			getCityList();

			$(document.body).on('click',function(e){
				$('#cityPos').removeClass('city-cur');
				$('#citychage').hide();
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
		}

		// 判断用户是否已登录
		$.ajax({
			url:(typeof isOrder!="undefined"&&isOrder?orderBase:base) + "/common/head.htm",
			dataType: "json",
			cache: false,
			success: function(data) {
				if(data){
					var fullname = data.memberCenterInfo.nickName ? data.memberCenterInfo.nickName:data.memberCenterInfo.defaultAccount;
					$("#box01").hide().siblings("#box02").show();
					$("#box02").children().eq(0).html(data.greetings + '，<span title="'+data.greetings+","+fullname+'">'+data.realName+'</span>');
					if(data.unreadMsgCount == 0){
						$('.gotomsg-item').hide();
					}else if(data.unreadMsgCount > 0){
						$(".hidden-num").text(data.unreadMsgCount);
						$(".gotomsg").text(data.unreadMsgCount).show();
						if(data.unreadMsgCount>99){
							$(".hidden-num").text('99+');
							$(".gotomsg").text('99+');
						}

					}
					if(headerOption.sign){
						checkSign();
					}

				}else{
					$("#box01").show().siblings("#box02").hide();
				}
			}
		});
		// 右侧栏浮动窗口 用户反馈
	 	 $(".cmp-enter,.backtoTop").hover(function(){
	        $(this).children(".advice-pop").show().animate({right: 44}, 200);
	     },
	     function(){
	         $(this).children(".advice-pop").hide().animate({right: 50}, 200);
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
		showDrop($('#userCenter'),$('.user-hd'),"user-cur",$('.group'));

		if(headerOption.sign){
			aniScore();
		}
		
	});
})(jQuery);

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
		$.getJSON(mallBase+ "/common/cityid.htm?cityName="+cityName, function(data) {
			if(data){
				cityCookie(data.cityId, data.cityName,data.domain);
				//$('#currentCity').attr({'cityid':data.cityId, 'name':data.cityName}).text(data.cityName);
			}
		});
	});
}


//(typeof isOrder!="undefined"&&isOrder?orderBase:base)
;function getCityList(){
	 $.getJSON("" + "/common/cityList.htm",function(data){
		 var frg=$(document.createDocumentFragment());
		 $.each(data,function(i,item){
			 var tmp=$('<a href="javascript:void(0);" target="_self" cityid="'+item.code+'" name="'+item.name+'" class="city-name"><span>'+item.name+'</span></a>');
			 tmp.appendTo(frg);
		 });
		 frg.appendTo($("#citychage"));
	 });
}
;function checkSign(){
		$.ajax({
			url:(typeof isOrder!="undefined"&&isOrder?orderBase:base) + "/hadCheckIn.htm",
			dataType: "json",
			cache: false,
			success: function(data) {
				if(data.hadCheckIn){
					$('.mark-count').hide();
					$('.marked-sucess').show();
				}else{
					$('.marked-sucess').hide();
					$('.mark-count').show();
				}
			}
		});
}

;function aniScore(){
	var markCount =$('.mark-count'),
		addFiveScore = $('.add-score'),
		markedSucess = $('.marked-sucess');
		markCount.on('click',function(){
			$.ajax({
				url:(typeof isOrder!="undefined"&&isOrder?orderBase:base) + "/hadCheckIn.htm",
				dataType: "json",
				cache: false,
				success: function(data) {
					if(data.loginStatus == 'false'){
						var backUrl = window.location.href;
						window.location.href = accountBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);
						return false;
					}

					if(data.hadCheckIn == 'true'){
						$('.mark-count').hide();
						$('.marked-sucess').show();	
					}else{
						$('.marked-sucess').hide();
						$('.mark-count').show();

						$.ajax({
								url:(typeof isOrder!="undefined"&&isOrder?orderBase:base) + "/userCheckIn.htm",
								dataType: "json",
								cache: false,
								success: function(data) {
									if(data.checkInFlag){						
										addFiveScore.fadeIn('fast').animate({fontSize:"18px",top:"-6",left:"-6"},300,function(){
											addFiveScore.animate({opacity:0},300);
											setTimeout(function(){
												markedSucess.show();
												$('.mark-count').hide();
												$('.marked-sucess').show();	
											},200);
										});
									}else{
										markCount.show();
									}
								}
							});
					}
				}
			});
		return false;
	});	
}


;(function ($, window, ECar) {

	var fns;

	fns = {

		// Options.
		config: {
			docSrlBar: false
		},

		// Script for initialization.
		init: function () {

			var _body;

			_body = $('body');

			this._set.topAd();

			this._set.im.init();

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

		_set: {
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

					this.advice();

				},

				jumpForwardToCS: function () {

					var _this;

					_this = this;

					this.config.csim.on('click', function () {

						window.open(_this.config.csimUrl.replace(/.chexiang./g, "."+imBase+"."), "车享客服", "height=573, width=803, top=80, left=300,toolbar=no, menubar=no, scrollbars=no, resizable=yes, location=n o, status=no");

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

					var h = $(window).height(),
						isIE6 = !-[1,] && !window.XMLHttpRequest,
						sidebar = $('#j-sidebar'),
						_body = $('body');
						if(isIE6){
							sidebar.height(h);
						}

						$('.side-weixin',sidebar).on('click',function(){							
							$('.side-weixin',sidebar).removeClass('weixin-show');
							$(this).toggleClass('weixin-show');
							return false;
						});
						_body.on('click.chexiang.im', function (e) {

						if (!$(e.target).hasClass('side-weixin')) {
							$('.side-weixin',sidebar).removeClass('weixin-show');
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

			}
		}

	};

	$(function(){
		fns.init();
	});

} (jQuery, window, window.ECar));