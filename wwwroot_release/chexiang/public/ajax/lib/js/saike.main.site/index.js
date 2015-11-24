

(function ($, window, ECar) {

	var fns;

	fns = {

		/*
				 (__)
				 (oo)
		  /-------\/
		 / |     ||----> Options.
		*  ||----||
		  ___/  ___/
		*/
		config: {
			docSrlBar: false
		},

		/*
				 (__)
				 (oo)
		  /-------\/
		 / |     ||----> Script for initialization.
		*  ||----||
		  ___/  ___/
		*/
		init: function () {

			var _body;

			_body = $('body');

			this._effect.caps();

			this._effect.mouseOffset();

			if (this.config.docSrlBar) {
				
				this._set.docScrollBar();
				
			}

			this._set.topAd();

			this._set.mixture();

			this._set.slider();

			this._set.tab();

			this._set.caption();

			this._set.imgLazyLoad();
          
          	this._set.imgOffset();

			this._set.monitor(_body);

			this._set.im.init();

			this._set.slt();

			this._set.sltOptSrl();

			this._set._form.init();

		},

		/*
				 (__)
				 (oo)
		  /-------\/
		 / |     ||----> Utils.
		*  ||----||
		  ___/  ___/
		*/
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

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for defining caption effect.
			*  ||----||
			  ___/  ___/
			*/
			caps: function () {

				$.fn.showCap = function(heightEnd, hightBegin) {

					var $picWindow;

					$picWindow = this;

					$picWindow.on("mouseover", function() {

						var cap;

						cap = $(this).find(".gallery-banner");

						cap.stop().animate({

							height: heightEnd

						}, 100);

					}).on("mouseleave", function() {

						var cap;

						cap = $(this).find(".gallery-banner");

						cap.stop().animate({

							height: hightBegin

						}, 100);

					});

				};

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for defining mouse offset effect.
			*  ||----||
			  ___/  ___/
			*/
			mouseOffset: function () {

				$.fn.imgOffset = function(options) {

					var $picWindows = this,

						sensitiveness = options.sensitiveness,

						picSelector = options.picSelector,

						unnatual = options.unnatual||false;

					$picWindows.each(function(idx, ele) {

						var $picWindow = $(ele),

							$pic = $picWindow.find(picSelector),

							windowWidth = $picWindow.innerWidth(),

							picWidth = $pic.innerWidth(),

							ratio = (sensitiveness * (picWidth - windowWidth)) / windowWidth,

							moveRange = {

								leftMost: windowWidth - picWidth,

								rightMost: 0

							},

							startPosition = {

								pageX: null,

								pageY: null,

								leftDst: null

							};

						$picWindow.on("mouseenter", function(e) {

							startPosition.pageX = e.pageX;

							startPosition.pageY = e.pageY;

							startPosition.leftDst = parseFloat($pic.css("left"));

						});

						$picWindow.on("mousemove", function(e) {

							var currentPosition = {

									pageX: e.pageX,

									pageY: e.pageY

								},

								deltaX = currentPosition.pageX - startPosition.pageX;

							if (unnatual) {

								deltaX *= -1;

							}

							// 目标位置
							var destPos = startPosition.leftDst + deltaX * ratio;

							// update startPosition
							startPosition.pageX = currentPosition.pageX;
							startPosition.pageY = currentPosition.pageY;

							// 再移动就超出了
							if (outOfRange(moveRange, destPos, deltaX, unnatual)) {

								return;

							}

							// update only when the $pic moves
							$pic.css("left", startPosition.leftDst=destPos);

						});

						function outOfRange(moveRange, destPos, deltaX/*, unnatual*/) {

							return (deltaX > 0 && destPos > moveRange["rightMost"])/*往右移动*/ ||
								(deltaX < 0 && destPos < moveRange["leftMost"]);/*往左移动*/

						}

						//TODO[]: When 'mouseleave', reset image to initial position.
						$picWindow.on("mouseleave", $.noop);

					});

				};

			}

		},

		_set: {

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Redefinition for the scroll bar of document.
			*  ||----||
			  ___/  ___/
			*/
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

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for Top Sliding Ad.
			*  ||----||
			  ___/  ___/
			*/
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

					});

				} else {

					if ($holidayAdbanner.length > 0) {

						$doubleEightAd.show();

					}

				}

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start mixture functions.
			*  ||----||
			  ___/  ___/
			*/
			mixture: function () {

				var f4ImgLinker;

				f4ImgLinker = $('.col-grid-cars').find('img');

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||---->
				*  ||----||
				  ___/  ___/
				*/
				$.ajaxSetup({

					cache: false

				});

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||----> Smooth scrolling to internal links.
				*  ||----||
				  ___/  ___/
				*/
				$('a[href^="#"]').on('click',function (e) {

					var _hash;

					_hash = this.hash;

					if (!_hash == '') {

						e.preventDefault();

						var _target;

						_target = $(_hash);

						$('html, body').stop().animate({

							'scrollTop': _target.offset().top

						}, 1000, 'swing', function () {

							window.location.hash = _hash;

						});

					}

				});

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||---->
				*  ||----||
				  ___/  ___/
				*/
				f4ImgLinker.on(fns.helpers.clickOrTouch(), function () {

					var that;

					that = $(this);

					that.next('a').get(0).click();

				});

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||----> 首页会员积分权益兑换
				*  ||----||
				  ___/  ___/
				*/
				$("#PakageList a.btn").on('click', function (e) {

					e.preventDefault();

					$.ajax({

						url: $(this).data("href"),

						cache: false,

						dataType: "jsonp",

						jsonp: "onJSONPLoad",

						jsonpCallback: "onJSONPLoad"

					});

				});

				window.onJSONPLoad = function (data) {

					if(data.errorType=='nologin'){

						var backUrl = window.location.href;

						window.location.href = mainBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);

					} else if(data.errorType=='false'){

						ECar.dialog.alert(data.info);

					} else {

						ECar.dialog.confirm({message: '<i class="icon icon-alert-l"></i>只能兑换一次！确认兑换吗?', callback: function() {

			                location.href = memberBase + "/member/benefits/preorderSubmit.htm?typePackage=" + data.packageType;

			            }});

					}

				};

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start slides effect.
			*  ||----||
			  ___/  ___/
			*/
			slider: function () {

				var mainPromo, thirdFloorPromo, atnCloseAds, sideAds_Left;

				mainPromo = $("#J_arrowSlider");

				thirdFloorPromo = $("#banner3F");
				
				atnCloseAds = $(".bannerclose", "#leftsideAd");
				
				sideAds_Left = $("#leftsideAd");

				mainPromo.slider({

					type:"rectangle"

				});

				thirdFloorPromo.slider({

					type: "rectangle",

					showArrow: false,

					noArrow: true

				});
				
				atnCloseAds.on("click", function() {
					
					sideAds_Left.hide();
					
				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start tab effect.
			*  ||----||
			  ___/  ___/
			*/
			tab: function () {

				var ajaxCache;

				ajaxCache = {};
				
				ECar.tab({

					selector: ".model-contents-tabs",

					event: "click",

					delay: 200,

					callback: function(id) {

						// var $models = $("#"+id)
						if(id in ajaxCache) {

							return;

						}

						ajaxCache[id] = true;

						//$("#"+id).load(base + '/indexHotCarByType.htm', {enumIndexHotCarType:id}, function() {

							var $contentBlocks = $("#"+id),

								$models = $("#"+id).children(".model"),

								blockHeight = 217;

							// blockHeight = $models.eq(0).outerHeight()+parseFloat($models.eq(0).css("margin-bottom"))+parseFloat($models.eq(0).css("margin-top"));
							if($models.length<=4) {

								$contentBlocks.height(blockHeight);

							} else {

								$contentBlocks.height(blockHeight*2);

								$contentBlocks.css("overflow", "hidden");

							}

						//});

						//$(".model-contents-models").load(base + '/indexHotCarByType.htm',{enumIndexHotCarType:id}); // plz return HTML fragments back

					}

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start caption effect.
			*  ||----||
			  ___/  ___/
			*/
			caption: function () {

				var elesWithCap;

				elesWithCap = $('.show-cap');

				elesWithCap.showCap(40, 0);

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for mouse offset effect.
			*  ||----||
			  ___/  ___/
			*/
			imgOffset: function () {

				var gallery, dealer;

				gallery = $(".gallery-window");

				dealer = $(".dealerlist-pic-window");

				gallery.imgOffset({

					sensitiveness: 3,

					unnatual:true,

					picSelector: ".gallery-img"

				});

				dealer.imgOffset({

					sensitiveness: 3,

					unnatual:true,

					picSelector: ".dealerlist-pic"

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start image lazy load effect.
			*  ||----||
			  ___/  ___/
			*/
			imgLazyLoad: function () {

				var imgs;

				imgs = $('.lazy');

				imgs.lazyload({

					effect: "fadeIn",

					threshold: 200

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start '秒针' monitor.
			*  ||----||
			  ___/  ___/
			*/
			monitor: function (container) {

				var _this, monitorHref;

				_this = fns;
				
				// monitorHref = container.find("a");
				
				// 精美图库广告监控
				var $beautyPicDiv = $("#kan div.gallery-content.clearfix");
				
				var exposureUrlFromVelSpread = $beautyPicDiv.find("a[href*='miaozhen']");

				exposureUrlFromVelSpread.each(function() {

					var href, $img, src, patt, patt_08, patt_09, patt_10, pIndex, pCode, dmpIndex, dmpCode;

					href = $(this).attr('href');

					$img = $(this).children("img");

					src = $img.attr("src");

					patt = /miaozhen/;

					patt_08 = /1011308/;

					patt_09 = /1011309/;

					patt_10 = /1011310/;

					pIndex;

					pCode;

					dmpIndex;

					dmpCode;

					if (patt.test(href)) {

						if (patt_08.test(href)) {

							var srcPre, img;

							pCode = _this.helpers.Arg('p', href);

							dmpIndex = href.lastIndexOf("DMP-");

							dmpCode = href.substr(dmpIndex + 4, 3);

							//发送监控请求，本地图片正常加载
							srcPre = "http://g.cn.miaozhen.com/x.gif?k=1011308&p=" + pCode + "&rt=2&ns=[M_ADIP]&ni=[M_IESID]&na=[M_MAC]&v=[M_LOC]&o=http://sgm.dmp.miaozhen.com/x.gif?k=DMP-39&p=DMP-" + dmpCode + "&rt=2&o=";

							img = new Image();

							img.src = srcPre;

						}

						if (patt_09.test(href)) {

							var srcPre, img;

							pCode = _this.helpers.Arg('p', href);

							//发送监控请求，本地图片正常加载
							srcPre = "http://g.cn.miaozhen.com/x.gif?k=1011309&p=" + pCode + "&rt=2&ns=[M_ADIP]&ni=[M_IESID]&na=[M_MAC]&v=[M_LOC]&o=";

							img = new Image();

							img.src = srcPre;

						}

						if (patt_10.test(href)) {

							var srcPre, img;

							pCode = _this.helpers.Arg('p', href);

							//发送监控请求，本地图片正常加载
							srcPre = "http://g.cn.miaozhen.com/x.gif?k=1011310&p=" + pCode + "&rt=2&ns=[M_ADIP]&ni=[M_IESID]&na=[M_MAC]&v=[M_LOC]&o=";

							img = new Image();

							img.src = srcPre;

						}

					}

				});
				
				// 除精美图库之外的广告监控
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

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start IM functions.
			*  ||----||
			  ___/  ___/
			*/
			im: {

				config: {

					scrollTop: '30px',

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

					var btn, _body;

					btn = $('.btnShowQRCode');

					_body = $('body');

					btn.on('click', function () {

						var that, code, _siblings, _siblingsCode;

						that = $(this);

						code = that.children('div');

						_siblings = that.siblings('.btnShowQRCode');

						_siblingsCode = _siblings.children('div');

						_siblingsCode.hide();

						_siblings.removeClass('highlight');

						that.addClass('highlight');

						code.show();

					});

					_body.on('click.chexiang.im', function (e) {

						if (!$(e.target).hasClass('btnShowQRCode')) {

							btn.removeClass('highlight');

							btn.children('div').hide();

						}

					});

				}

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Redefinition for the select element.
			*  ||----||
			  ___/  ___/
			*/
			slt: function () {

				var _slt;

				_slt = $('#frm-sell').find('select');

				_slt.selecter({

					label: '车辆所在地',

					cover: false,

					callback: function (val, idx) {}

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Redefinition for the scroll bar of select options panel.
			*  ||----||
			  ___/  ___/
			*/
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

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start form validation functions.
			*  ||----||
			  ___/  ___/
			*/
			_form: {

				init: function () {

					this.setFocusAssist();

					this.setMessage();

					this.setCustomMethod();

					this.setDefault();

					this._vali();

				},

				setFocusAssist: function () {

					var field;

					field = $('.f4-field');

					field.on('click.chexiang.form.field', function (e) {

						if (e.target.nodeName == 'DIV') {

							$(this).children('input').focus();

						}

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

							$(element).closest('.f4-field').removeClass('_error');

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

							errerArea = $('.error-area');

							if (frmRegisterValior.numberOfInvalids() > 0) {

								fisrtErrorMessage = errorList[0].message;

								firstErrorElement = $(errorList[0].element);

								firstErrorElement.closest('.f4-field').addClass('_error');
								fisrtErrorMessage === $.validator.messages.required ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : firstErrorElement.attr('id') === 'iptModel' ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : firstErrorElement.attr('id') === 'iptUser' ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : errerArea.html(fisrtErrorMessage);
								errerArea.removeClass('hide');

								this.defaultShowErrors();

							} else {

								errerArea.addClass('hide');

							}

						},

						submitHandler: function(form, event) {

							var fnClean, btnSubmit, makeBtnUseful;

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

							if ($('html').hasClass('ie7') || $('html').hasClass('ie8')) {

								$(form).valid();

								if (frmRegisterValior.numberOfInvalids() === 0) {

									form.submit();

									makeBtnUseful(btnSubmit, true);
									
									setTimeout(function() {
										
										fnClean($('.f4-field'), $(form).find('select'));
										
									},0);

									//fnClean($('.f4-field'), $(form).find('select'));

									makeBtnUseful(btnSubmit, false);

								} else {

									frmRegisterValior.focusInvalid();

									return false;

								}

							} else {

								form.submit();

								makeBtnUseful(btnSubmit, true);
								
								setTimeout(function() {
									
									fnClean($('.f4-field'), $(form).find('select'));
									
								},0);

								//fnClean($('.f4-field'), $(form).find('select'));

								makeBtnUseful(btnSubmit, false);

							}
						}

						});
				}

			}

		}

	};

	$(fns.init());

} (jQuery, window, window.ECar));
