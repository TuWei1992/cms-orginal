// added by CD, 屏蔽ESC导致弹出层关闭，2013/12/18

;
(function($, ECar) {
	(function($) {
		//alert(1)
		if (!$) return;
		$.extend(String.prototype, {
			'sliceBefore': function(str) {
				return (this.indexOf(str) >= 0) ? this.substring(0, this.indexOf(str)) : this.toString();
			},
			'substitute': function(data) {
				if (data && typeof(data) == 'object') {
					return this.replace(/\{([^{}]+)\}/g, function(match, key) {
						var value = data[key];
						return (value !== undefined) ? '' + value : '';
					});
				} else {
					return this.toString();
				}
			}
		});
	})(jQuery);
	(function($) {
		if (!$) return;
		if (!window.ECar) window.ECar = {};
		ECar.namespace = function(name, sep) {
			var s = name.split(sep || '.'),
				d = {},
				o = function(a, b, c) {
					if (c < b.length) {
						if (!a[b[c]]) {
							a[b[c]] = {};
						}
						d = a[b[c]];
						o(a[b[c]], b, c + 1);
					}
				};
			o(window, s, 0);
			return d;
		};

		ECar.timestat = {};
		ECar.timestat.libs = {};
		ECar.timestat.loadTime = (typeof(_ECar_page_loadtime) == 'number') ? _ECar_page_loadtime : new Date().getTime();
		ECar.timestat.add = function(name) {
			this.libs[name] = new Date().getTime() - this.loadTime;
		};
		ECar.timestat.get = function(name) {
			return this.libs[name] || 0;
		};
		ECar.lang = {};
		ECar.lang.language = 'zh-cn';
		ECar.lang.text = {};
		ECar.lang.get = function(dataset, name) {
			if (name) {
				if (this.text[dataset]) {
					return this.text[dataset][name] || '';
				} else {
					return '';
				}
			} else {
				return this.text[dataset] || null;
			}
		};
		ECar.lang.set = function(dataset, name, value) {
			if (!this.text[dataset]) {
				this.text[dataset] = {};
			}
			if (value) {
				this.text[dataset][name] = value;
			} else {
				this.text[dataset] = name;
			}
		};
		ECar.lang.extend = function(dataset, data) {
			if (!this.text[dataset]) {
				this.text[dataset] = {};
			}
			$.extend(this.text[dataset], data);
		};
	})(jQuery);

	(function($) {
		if (!$ || !window.ECar) return;

		ECar.namespace('ECar.page');

		ECar.page.keyHandler = {};
		ECar.page.keyHandler.events = {};
		ECar.page.keyHandler.keys = {
			'ESC': 27,
			'PAGEUP': 33,
			'PAGEDOWN': 34,
			'END': 35,
			'HOME': 36,
			'LEFT': 37,
			'TOP': 38,
			'RIGHT': 39,
			'DOWN': 40,
			'INSERT': 45,
			'DELETE': 46,
			'F1': 112,
			'F2': 113,
			'F3': 114,
			'F4': 115,
			'F5': 116,
			'F6': 117,
			'F7': 118,
			'F8': 119,
			'F9': 120,
			'F10': 121,
			'F11': 122,
			'F12': 123
		};
		ECar.page.keyHandler.add = function(doc, key, eventItem, eventCallback) {
			this.events[eventItem] = function(e) {
				try {
					var code = e.which || e.keyCode || 0;
					if (code == ECar.page.keyHandler.keys[key]) {
						eventCallback();
					}
				} catch (err) {}
			};
			$(doc).bind('keydown', this.events[eventItem]);
		};
		ECar.page.keyHandler.remove = function(doc, eventItem) {
			$(doc).unbind('keydown', this.events[eventItem]);
			this.events[eventItem] = null;
		};

	})(jQuery);


	// ECar.overlayer
	// --------------------------------
	(function($) {
		if (!$ || !window.ECar) return;
		// ----------------------------
		ECar.namespace('ECar.utilStyles');
		$.extend(ECar.utilStyles, {
			'util-overlayer': 'z-index:9000;display:none;width:100%;height:100%;position:absolute;top:0;left:0;right:0;bottom:0;',
			'util-recyclebin': 'position:absolute;top:-10000px;left:-10000px;width:1000px;height:1000px;overflow:hidden;'
		});
		// ----------------------------
		ECar.namespace('ECar.util');
		// ----------------------------
		// 回收站 ECar.recyclebin
		ECar.recyclebin = {};
		ECar.recyclebin.id = 'util-recyclebin';
		ECar.recyclebin.container = $('#util-recyclebin');
		ECar.recyclebin.init = function() {
			if ($('#' + this.id).size() == 0) {
				this.container = $('<div id="' + this.id + '" style="' + ECar.utilStyles[this.id] + '"></div>');
				$('body').append(this.container);
			}
		};
		ECar.recyclebin.add = function(elm) {
			this.container.append(elm);
		};
		ECar.recyclebin.find = function(id) {
			return (this.container.children('#' + id).size() > 0) ? true : false;
		};
		ECar.recyclebin.clear = function() {
			this.container.html('');
		};
		// ----------------------------
		// 遮盖层插件 ECar.overlayer
		ECar.overlayer = {};
		ECar.overlayer.id = 'util-overlayer';
		ECar.overlayer.status = false;
		// 显示遮盖层
		ECar.overlayer.showLayer = function(op, bg) {
			if (this.status) return false;
			op = (typeof(op) != 'undefined') ? op : 0.5;
			bg = bg || '#000';
			if ($('#' + this.id).length == 0) $('body').append('<div id="' + this.id + '" style="' + ECar.utilStyles[this.id] + '"></div>');
			$('#' + this.id).css({
				background: bg,
				filter: ('alpha(opacity=' + op * 100 + ')'),
				opacity: op
			});
			this.setSize();
			this.status = true;
		};
		// 关闭遮盖层
		ECar.overlayer.hideLayer = function() {
			if (this.status) {
				$('#' + this.id).fadeOut();
				this.status = false;
			}
		};
		// 设置遮盖层大小
		ECar.overlayer.setSize = function() {
			$('#' + this.id).css({
				width: '100%',
				height: $(document).height() + 'px',
				display: 'block'
			});
		};
		// 自适应屏幕调整遮盖层大小
		ECar.overlayer.resize = function() {
			if (this.status) {
				$('#' + this.id).css('display', 'none');
				this.setSize();
			}
		};
		// ----------------------------
		$(window).resize(function() {
			ECar.overlayer.resize();
		});
		// ----------------------------
		ECar.timestat.add('load-util-overlayer');
		//ECar.debug('overlayer.js', '初始化成功');
	})(jQuery);

	// --------------------------------
	// ECar.dialog
	// --------------------------------
	(function($) {
		if (!$ || !window.ECar) return;
		// ----------------------------
		ECar.namespace('ECar.util');
		// ----------------------------
		if (ECar.dialog) return;
		// ----------------------------
		var utilId = 'c-dialog';
		var utilText = ECar.lang.get(utilId) || {
			'close': '关闭',
			'closeTips': '关闭浮层',
			'loading': '正在加载中……',
			'loadingError': '加载失败',
			'confirm': '确定',
			'cancel': '取消',
			'alertTitle': '消息提示',
			'confirmTitle': '确认提示',
			'promptTitle': '输入提示'
		};
		utilText.id = utilId;
		// ----------------------------
		ECar.dialog = {};
		ECar.dialog.id = utilId;
		ECar.dialog.status = false;
		ECar.dialog.resizeStatus = false;
		// 弹窗加载完成时回调方法
		ECar.dialog.finishCallback = null;
		// 关闭弹出窗时回调方法
		ECar.dialog.closeCallback = null;
		// 弹窗模板
		ECar.dialog.tmpls = {
			'frameset': [
				'<div id="{id}">',
				'<div class="pdb-bgm"></div>',
				'<div class="pdb-main">',
				'<div class="pdb-title"><h4></h4><div class="border-out"><p class="border-in"><span class="close icon icon-close" onclick="ECar.dialog.close();" title="{closeTips}">{close}</span></p></div></div>',
				'<div class="pdb-content"><div class="pdb-contentframe"></div></div>',
				'<div class="pdb-bottom"></div>',
				'</div>',
				'</div>'
			].join(''),
			'titletab': '<span data-url="{url}" data-width="{width}" data-height="{height}">{title}</span>',
			'bottom': '<p class="pdb-bottom-action"><a class="dialog-btn btn-action"><em>{confirm}</em></a><a class="dialog-btn btn-cancel"><em>{cancel}</em></a></p><p class="pdb-bottom-text"></p>',
			'loading': '<p class="txt-loading-32" style="margin-top:{marginTop}px;">{loadingText}</p>',
			'alert': '<div class="msg-{type} f14">{message}</div><div class="tac pb20"><a class="dialog-btn btn-cancel btn-ok" onclick="ECar.dialog.close();"><em>确定</em></a></div>',
			'confirm': '<div class="msg-confirm f14">{message}</div>',
			'prompt': '<div class="p20 f14">{content}</div>',
			'inputelm': '<dl class="prompt clearfix"><dt>{title}</dt><dd><input type="text" class="txt-input" /></dd></dl>',
			'iframe': '<iframe id="{iframeId}" name="{iframeId}" src="about:blank" width="100%" height="{height}px" scrolling="auto" frameborder="0" marginheight="0" marginwidth="0"></iframe>',
			'error': '<div class="p20"><p class="txt-error">{loadingError}</p></div>',
			'ajaxload': '<p class="txt-loading-32" style="margin-top:{height}px;">&nbsp;</p>'
		};
		// 初始化弹出窗
		ECar.dialog.init = function() {
			if ($('#' + this.id).size() == 0) {
				$('body').append(this.tmpls.frameset.substitute(utilText));
				this.container = $('#' + this.id);
			}
		};
		// 打开弹出窗
		ECar.dialog.open = function(options) {
			if (this.status && this.lastContentId) {
				$('body').append($('#' + this.lastContentId));
				$('#' + this.lastContentId).hide();
				this.lastContentId = null;
			}
			/*
			if (options.closeButtonOn === false) { // added by CD, if close button is not needed
				ECar.dialog.tmpls['frameset'] = [
					'<div id="{id}">',
					'<div class="pdb-bgm"></div>',
					'<div class="pdb-main">',
					'<div class="pdb-title"><h4></h4><div class="border-out" style="display: none"><p class="border-in"><span class="close icon icon-close" onclick="ECar.dialog.close();" title="{closeTips}">{close}</span></p></div></div>',
					'<div class="pdb-content"><div class="pdb-contentframe"></div></div>',
					'<div class="pdb-bottom"></div>',
					'</div>',
					'</div>'
				].join('')
			}
			*/
			ECar.overlayer.showLayer(options.op, options.overbg);
			this.init();
			this.options = options;
			if (options.callback) this.finishCallback = options.callback;
			if (options.closeFunction) this.closeCallback = options.closeFunction;
			if (options.title) {
				this.container.find('.pdb-title').show();
				this.setTitle(options.title, options.tabs);
			} else {
				this.container.find('.pdb-title').hide();
			}
			if (options.bottom) {
				this.container.find('.pdb-bottom').show();
				this.setBottom(options.bottom);
			} else {
				this.container.find('.pdb-bottom').hide();
			}
			var width = options.width || 400;
			var height = options.height || 300;
			var _this = this;
			switch (options.mode || 'text') {
				case 'ajax':
					var contentframe = this.container.find('.pdb-contentframe');
					$.ajax({
						type: options.content.type || 'get',
						url: options.content.url,
						data: options.content.param || '',
						error: function() {
							_this.setContent(options.content.error || _this.tmpls.error.substitute(utilText));
							_this.setAutoHeight(60);
							_this.finish();
						},
						success: function(html) {
							_this.setContent(html);
							setTimeout(function(){_this.setAutoHeight();},100);
							options.ajaxComplete&&options.ajaxComplete.call(_this);
						},
						beforeSend:function(xhr,status){
							var loading_html = _this.tmpls.ajaxload.substitute({
								height: (height - 32) / 2
							});
							contentframe.html(options.content.loading || loading_html);
							_this.setAutoHeight();
						}
					});
					break;
				case 'text':
					this.setContent(options.content);
					break;
				case 'id':
					this.setContent('');
					$('#' + options.id).appendTo('#' + this.id + ' .pdb-contentframe');
					$('#' + options.id).removeClass('hidden').show();
					this.lastContentId = options.id;
					break;
				case 'iframe':
					this.iframeId = 'iframe_' + (new Date()).getTime();
					this.setContent(this.tmpls.iframe.substitute({
						iframeId: this.iframeId,
						height: height
					}));
					this.setIframe(options.url);
					break;
			}
			this.container.find('.pdb-main').css('width', width + 'px');
			this.container.find('.pdb-content').css('height', height + 'px');
			this.container.css('width', width + 8 + 'px').show();
			var height_2 = height + ((options.title) ? this.container.find('.pdb-title').height() : 0) + ((options.bottom) ? 55 : 0);
			this.container.find('.pdb-bgm, .pdb-main').css('height', height_2 + 'px');
			this.container.attr('class', options.css || '');
			this.status = true;
			this.resizeStatus = true;
			this.resize();
			if (options.mode != 'ajax') this.finish();
			if (options.supportESC !== false) { // added by CD, esc
				ECar.page.keyHandler.add(document, 'ESC', 'esc-dialog', function() {
					ECar.dialog.close();
				});
			}
			this.handlePopupCloseButton(options.closeButtonOn);// added by CD, close button
		};

		// added by CD, handle the close up on/off
		ECar.dialog.handlePopupCloseButton = function(closeButtonOn) {
			var $popUpCloseButton = $('#c-dialog .border-out');
			if ($popUpCloseButton.length) {
				if (closeButtonOn === false) {
					$popUpCloseButton.hide();
				} else {
					$popUpCloseButton.show();
				}
			}
		};

		ECar.dialog.finish = function() {
			if (this.finishCallback) this.finishCallback(this.container.find('.pdb-contentframe'));
		};
		// 调整弹出窗大小
		ECar.dialog.resize = function() {
			if (this.status && this.resizeStatus) {
				var _left = ($(window).width() - this.container.width()) / 2;
				var _top = (this.options.top) ? this.options.top : ($(window).height() - this.container.height()) / 2;
				this.container.css({
					left: $(document).scrollLeft() + Math.max(_left, 20) + 'px',
					top: $(document).scrollTop() + Math.max(_top, 20) + 'px'
				});
				if ($.browser.isIE6) this.container.bindECarUI('HideOverElements', 'select,object');
			}
		};
		// 关闭弹出窗
		ECar.dialog.close = function(effect) {
			if (!this.status) return;
			switch (effect) {
				case 'fade':
					this.container.fadeOut();
					break;
				case 'slide':
					this.container.slideUp();
					break;
				default:
					this.container.hide();
			}
			ECar.overlayer.hideLayer();
			this.status = false;
			if (this.lastContentId) {
				$('body').append($('#' + this.lastContentId));
				$('#' + this.lastContentId).hide();
				this.lastContentId = null;
			}
			if ($.browser.isIE6) this.container.bindECarUI('ShowOverElements', 'select,object');
			if (this.closeCallback) {
				setTimeout(function() {
					ECar.dialog.closeCallback();
					ECar.dialog.closeCallback = null;
				}, 0);
			}
			ECar.page.keyHandler.remove(document, 'esc-dialog');
		};
		// 设置关闭回调函数, added by cd
		ECar.dialog.setCloseCallback = function (callback) {
			if (Object.prototype.toString.call(callback) !== "[object Function]") {
				return;
			}
			this.closeCallback = callback;
		}
		// 更换弹出窗标题(新增了tabs的支持)
		ECar.dialog.setTitle = function(title, tabs) {
			var titleElm = this.container.find('.pdb-title h4');
			if (tabs) {
				titleElm.addClass('tab').html('');
				for (var i = 0; i < tabs.length; i++) {
					titleElm.append(this.tmpls.titletab.substitute(tabs[i]));
				}
				titleElm.find('span:first').addClass('on');
				titleElm.find('span').click(function() {
					if ($(this).hasClass('on')) return;
					$(this).siblings('.on').removeClass('on');
					$(this).addClass('on');
					ECar.dialog.setIframe($(this).attr('data-url'));
					ECar.dialog.setWidth(parseInt($(this).attr('data-width')));
					ECar.dialog.setHeight(parseInt($(this).attr('data-height')));
				});
			} else {
				titleElm.removeAttr('class').html(title);
			}
		};
		ECar.dialog.setStyle = function(options) {
			if (options.titlecolor) this.container.find('.pdb-title').css('color', options.titlecolor);
			if (options.bgcolor) this.container.find('.pdb-title').css('background-color', options.bgcolor);
			if (options.bordercolor) this.container.find('.pdb-main').css('border-color', options.bordercolor);
		};
		// 更换弹出窗内容
		ECar.dialog.setContent = function(html) {
			this.container.find('.pdb-contentframe').html(html);
		};
		// 更换Iframe页内容
		ECar.dialog.setIframe = function(url) {
			$('#' + this.iframeId).attr('src', url);
		};
		// 更换弹出窗底部内容 { html, noteHtml, actionHtml, actionBtnText, cancelBtnText, callback }
		ECar.dialog.setBottom = function(options) {
			if (!options) return;
			if (options.html) {
				this.container.find('.pdb-bottom').html(options.html);
			} else {
				this.container.find('.pdb-bottom').html(this.tmpls.bottom.substitute(utilText));
				if (options.noteHtml) this.container.find('.pdb-bottom .pdb-bottom-text').html(options.noteHtml);
				if (options.actionHTML) {
					this.container.find('.pdb-bottom .pdb-bottom-action').html(options.actionHTML);
				} else {
					if (options.actionBtnText) this.container.find('.pdb-bottom .btn-action em').html(options.actionBtnText);
					if (options.cancelBtnText) this.container.find('.pdb-bottom .btn-cancel em').html(options.cancelBtnText);
				}
			}
			if (options.callback) {
				this.container.find('.pdb-bottom .btn-action').click(function() {
					options.callback();
				});
			} else {
				this.container.find('.pdb-bottom .btn-action').click(function() {
					ECar.dialog.close();
				});
			}
			if (options.cancel) {
				this.container.find('.pdb-bottom .btn-cancel').click(function() {
					options.cancel();
				});
			} else {
				this.container.find('.pdb-bottom .btn-cancel').click(function() {
					ECar.dialog.close();
				});
			}
		};
		// 重设弹窗宽度
		ECar.dialog.setWidth = function(width) {
			if (!width) return;
			var width = parseInt(width);
			this.container.find('.pdb-main').css('width', width + 'px');
			this.container.css('width', (width + 8) + 'px');
			this.resize();
		};
		// 重设弹层高度
		ECar.dialog.setHeight = function(height) {
			if (!height) return;
			var height = parseInt(height);
			var _height = this.container.find('.pdb-content').height();
			var _height2 = this.container.find('.pdb-main').height();
			this.container.find('.pdb-bgm, .pdb-main').css('height', (height - _height + _height2) + 'px');
			this.container.find('.pdb-content').css('height', height + 'px');
			this.container.find('.pdb-content iframe').attr('height', height);
			this.resize();
		};
		ECar.dialog.setAutoHeight = function(min) {
			var height = Math.max($('.pdb-contentframe').height(), min || 100);
			this.setHeight(height);
		};
		// 显示加载进度条
		ECar.dialog.showLoading = function(params) {
			params.mode = 'text';
			params.height = 100;
			params.marginTop = (params.height - 56) / 2;
			params.loadingText = params.loadingText || utilText.loading;
			params.content = this.tmpls.loading.substitute(params);
			this.open(params);
			return false;
		};
		// 显示弹出窗内容
		ECar.dialog.show = function(params) {
			params.mode = 'id';
			this.open(params);
			this.setAutoHeight();
			return false;
		};
		// 打开弹出窗页面
		ECar.dialog.pop = function(params) {
			params.mode = 'iframe';
			this.open(params);
			return false;
		};
		// 打开Ajax页面
		ECar.dialog.ajax = function(params) {
			params.mode = 'ajax';
			params.content = {
				url: params.url
			};
			this.open(params);
			return false;
		};
		// 打开提醒框
		ECar.dialog.alert = function(params) {
			if (typeof(params) == 'string') {
				params = {
					message: params
				};
			}
			params.close = utilText.close;
			params.type = params.type || 'alert';
			var content = this.tmpls.alert.substitute(params);
			this.open({
				mode: 'text',
				title: params.title || utilText.alertTitle,
				content: content,
				width: params.width || 350,
				height: 100,
				top: params.top,
				closeFunction: params.callback
			});
			this.setAutoHeight();
			return false;
		};
		// 打开确认框
		ECar.dialog.confirm = function(params) {
			if (typeof(params) == 'string') {
				params = {
					message: params
				};
			}
			var content = this.tmpls.confirm.substitute(params);
			var callback = function() {
				ECar.dialog.close();
				if (params.callback && $.isFunction(params.callback)) params.callback();
			};
			var cancel = function() {
				ECar.dialog.close();
				if (params.cancel && $.isFunction(params.cancel)) params.cancel();
			};
			this.open({
				mode: 'text',
				title: params.title || utilText.confirmTitle,
				css:params.css,
				content: content,
				bottom: {
					callback: callback,
					cancel: cancel
				},
				width: params.width || 350,
				height: 60,
				top: params.top
			});
			this.setAutoHeight(60);
			return false;
		};
		// 打开输入框
		ECar.dialog.prompt = function(params) {
			if (!$.isArray(params.message)) {
				params.message = [params.message];
			}
			var formelement = [];
			for (var i = 0; i < params.message.length; i++) {
				formelement.push(this.tmpls.inputelm.substitute({
					title: params.message[i]
				}));
			}
			var content = this.tmpls.prompt.substitute({
				content: formelement.join('')
			});
			var callback = function() {
				var inputValues = [];
				ECar.dialog.container.find('dl.prompt input').each(function() {
					inputValues.push($(this).val().trim());
				});
				params.callback(inputValues);
				ECar.dialog.close();
			};
			var cancel = function() {
				ECar.dialog.close();
			};
			this.open({
				mode: 'text',
				title: params.title || utilText.promptTitle,
				content: content,
				bottom: {
					callback: callback,
					cancel: cancel
				},
				width: params.width || 350,
				height: 60,
				top: params.top
			});
			this.setAutoHeight(60);
			return false;
		};
		// 自适应调整弹出窗位置
		$(window).resize(function() {
			ECar.dialog.resize();
		});
		// ----------------------------
		ECar.timestat.add('load-util-dialog');
		//ECar.debug('dialog.js', '初始化成功');
	})(jQuery);

})(jQuery, window.ECar || (window.ECar = {}));