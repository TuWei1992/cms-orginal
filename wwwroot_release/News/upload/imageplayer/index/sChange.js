/**
 sChange 1.1 20110331
 **/
if (!top.execScript && HTMLElement) {
	HTMLElement.prototype.__defineGetter__("currentStyle", function() {
		return this.ownerDocument.defaultView.getComputedStyle(this, null);
	});
}
if (!Array.prototype.each) {
	Array.prototype.each = function(func, scope) {
		var len = this.length;
		for (var i = 0; i < len; i++) {
			try {
				func.call(scope || this[i], this[i], i, this);
			} catch (ex) {}
		}
	};
}
if (!String.prototype.trim) {
	String.prototype.trim = function() {
		return this.replace(/(^\s*)|(\s*$)/g, "");
	};
}
/**
 * "mini" Selector Engine by James Padolsey
 * 迷你元素选择器，支持以下选择方式
 * tag
 * tag > .className
 * tag > tag
 * #id > tag.className
 * .className tag
 * tag, tag, #id
 * tag#id.className
 * .className
 * span > * > b
 */
var miniSelector = (function() {
	var snack = /(?:[\w\-\\.#]+)+(?:\[\w+?=([\'"])?(?:\\\1|.)+?\1\])?|\*|>/ig,
		exprClassName = /^(?:[\w\-_]+)?\.([\w\-_]+)/,
		exprId = /^(?:[\w\-_]+)?#([\w\-_]+)/,
		exprNodeName = /^([\w\*\-_]+)/,
		na = [null, null];

	function _find(selector, context) {
		context = context || document;
		var simple = /^[\w\-_#]+$/.test(selector);
		if (!simple && context.querySelectorAll) {
			return realArray(context.querySelectorAll(selector));
		}
		if (selector.indexOf(',') > -1) {
			var split = selector.split(/,/g),
				ret = [],
				sIndex = 0,
				len = split.length;
			for (; sIndex < len; ++sIndex) {
				ret = ret.concat(_find(split[sIndex], context));
			}
			return unique(ret);
		}
		var parts = selector.match(snack),
			part = parts.pop(),
			id = (part.match(exprId) || na)[1],
			className = !id && (part.match(exprClassName) || na)[1],
			nodeName = !id && (part.match(exprNodeName) || na)[1],
			collection;
		if (className && !nodeName && context.getElementsByClassName) {
			collection = realArray(context.getElementsByClassName(className));
		} else {
			collection = !id && realArray(context.getElementsByTagName(nodeName || '*'));
			if (className) {
				collection = filterByAttr(collection, 'className', RegExp('(^|\\s)' + className + '(\\s|$)'));
			}
			if (id) {
				var byId = context.getElementById(id);
				return byId ? [byId] : [];
			}
		}
		return parts[0] && collection[0] ? filterParents(parts, collection) : collection;
	}

	function realArray(c) {
		try {
			return Array.prototype.slice.call(c);
		} catch (e) {
			var ret = [],
				i = 0,
				len = c.length;
			for (; i < len; ++i) {
				ret[i] = c[i];
			}
			return ret;
		}
	}

	function filterParents(selectorParts, collection, direct) {
		var parentSelector = selectorParts.pop();
		if (parentSelector === '>') {
			return filterParents(selectorParts, collection, true);
		}
		var ret = [],
			r = -1,
			id = (parentSelector.match(exprId) || na)[1],
			className = !id && (parentSelector.match(exprClassName) || na)[1],
			nodeName = !id && (parentSelector.match(exprNodeName) || na)[1],
			cIndex = -1,
			node, parent, matches;
		nodeName = nodeName && nodeName.toLowerCase();
		while ((node = collection[++cIndex])) {
			parent = node.parentNode;
			do {
				matches = !nodeName || nodeName === '*' || nodeName === parent.nodeName.toLowerCase();
				matches = matches && (!id || parent.id === id);
				matches = matches && (!className || RegExp('(^|\\s)' + className + '(\\s|$)').test(parent.className));
				if (direct || matches) {
					break;
				}
			} while ((parent = parent.parentNode));
			if (matches) {
				ret[++r] = node;
			}
		}
		return selectorParts[0] && ret[0] ? filterParents(selectorParts, ret) : ret;
	}
	var unique = (function() {
		var uid = +new Date();
		var data = (function() {
			var n = 1;
			return function(elem) {
				var cacheIndex = elem[uid],
					nextCacheIndex = n++;
				if (!cacheIndex) {
					elem[uid] = nextCacheIndex;
					return true;
				}
				return false;
			};
		})();
		return function(arr) {
			var length = arr.length,
				ret = [],
				r = -1,
				i = 0,
				item;
			for (; i < length; ++i) {
				item = arr[i];
				if (data(item)) {
					ret[++r] = item;
				}
			}
			uid += 1;
			return ret;
		};
	})();

	function filterByAttr(collection, attr, regex) {
		/**
		 * Filters a collection by an attribute.
		 */
		var i = -1,
			node, r = -1,
			ret = [];
		while ((node = collection[++i])) {
			if (regex.test(node[attr])) {
				ret[++r] = node;
			}
		}
		return ret;
	}
	return _find;
})();
/*
 *	图片轮换
 *	实现思路源自bujichong的jQuery对象切换插件sGallery
 *	http://www.ceshile.cn/lxProject/ceshi/sGallery/sGallery.html
 */
var sChange = (function() {
	var agt = window.navigator.userAgent;
	var isQuirks = document.compatMode == "BackCompat";
	var isStrict = document.compatMode == "CSS1Compat";
	var isOpera = agt.toLowerCase().indexOf("opera") != -1;
	var isChrome = agt.toLowerCase().indexOf("chrome") != -1;
	var isIE = agt.toLowerCase().indexOf("msie") != -1 && !isOpera;
	var isIE6 = isIE && !window.XMLHttpRequest;
	var isGecko = agt.toLowerCase().indexOf("gecko") != -1;

	function extra(o, c) { //复制对象c的成员到对象o
		if (!o) {
			o = {};
		}
		if (o && c && typeof c == 'object') {
			for (var p in c) {
				o[p] = c[p];
			}
		}
		return o;
	}

	function setOpacity(elem, alpha) {
		if (isIE) { //isIE
			elem.style.filter = 'alpha(opacity=' + alpha + ')';
			if(isIE6 && alpha==100){
				elem.style.filter = '';
			}
		} else {
			elem.style.opacity = alpha / 100;
		}
	}

	function getOpacity(elem) {
		var alpha;
		if (isIE) { //isIE
			alpha = elem.currentStyle.filter.indexOf("opacity=") >= 0 ? (parseFloat(elem.currentStyle.filter.match(/opacity=([^)]*)/)[1])) + '' : '100';
		} else {
			alpha = 100 * elem.ownerDocument.defaultView.getComputedStyle(elem, null)['opacity'];
		}
		setOpacity(elem, alpha);
		return alpha;
	}

	function fade(element, transparency, speed, callback) { //透明度渐变：transparency:透明度 0(全透)-100(不透)；speed:速度1-100，默认为1
		speed = speed || 1;
		if (typeof(element) == 'string') {element = document.getElementById(element);}
		if (!element.effect) {
			element.effect = {};
			element.effect.fade = 0;
		}
		clearInterval(element.effect.fade);
		var start = getOpacity(element);
		var timeout = isIE ? 40 : 20;
		speed = isIE ? Math.min(2 * speed, 100) : speed;

		element.effect.fade = setInterval(function() {
			start = start < transparency ? Math.min(start + speed, transparency) : Math.max(start - speed, transparency);
			setOpacity(element, start);
			if (Math.round(start) == transparency) {
				setOpacity(element, transparency);
				clearInterval(element.effect.fade);
				if (callback) {callback.call(element);}
			}
		}, timeout);
	}

	function $(el) {
		extra(el, {
			hasClass: function(c) {
				return (' ' + this.className + ' ').indexOf(' ' + c + ' ') != -1;
			},
			addClass: function(c) {
				if (!this.hasClass(c)) {
					this.className += " " + c;
				}
				return this;
			},
			removeClass: function(c) {
				if (this.hasClass(c)) {
					this.className = (" " + this.className + " ").replace(" " + c + " ", " ").trim();
					return this;
				}
			},
			hide: function() {
				if (this.currentStyle && this.currentStyle.display != 'none') {
					this._display = this.currentStyle.display;
				} else {
					this._display = 'block';
				}
				this.style.display = 'none';
				return this;
			},
			show: function() {
				this.style.display = this._display ? this._display : '';
				return this;
			},
			opacity: function(a) {
				if (a !== undefined) {
					setOpacity(this, a);
					return this;
				} else {
					return getOpacity(this);
				}
			},
			fade: function(transparency, speed, callback) {
				fade(this, transparency, speed, callback);
			}
		});
		return el;
	}

	function _change(o) {
		o = extra({
			changeObj: null,
			//切换对象
			thumbObj: null,
			//导航对象
			botPrev: null,
			//按钮上一个
			botNext: null,
			//按钮下一个
			thumbNowClass: 'now',
			//导航对象当前的class,默认为now
			thumbOverEvent: true,
			//鼠标经过thumbObj时是否切换对象，默认为true，为false时，只有鼠标点击thumbObj才切换对象
			slideTime: 1000,
			//平滑过渡时间，默认为1000ms
			autoChange: true,
			//是否自动切换，默认为true
			clickFalse: true,
			//导航对象如果有链接，点击是否链接无效，即是否返回return false，默认是return false链接无效，当thumbOverEvent为false时，此项必须为true，否则鼠标点击事件冲突
			overStop: true,
			//鼠标经过切换对象时，切换对象是否停止切换，并于鼠标离开后重启自动切换，前提是已开启自动切换
			changeTime: 5000,
			//自动切换时间
			delayTime: 300 //鼠标经过时对象切换迟滞时间，推荐值为300ms
		}, o || {});
		var changeObjs = miniSelector(o.changeObj);
		var thumbObjs;
		var amount = changeObjs.length;
		var nowIndex = 0; //定义全局指针
		var index; //定义全局指针
		var startRun; //预定义自动运行参数
		var delayRun; //预定义延迟运行参数
		if (amount == 0) {
			return null;
		} 
		
		/**主切换函数**/
		function fadeAB() {
			if (nowIndex != index) {
				if (o.thumbObj != null) {
					thumbObjs = miniSelector(o.thumbObj);
					if(thumbObjs.length<1){//如果播放器相关dom元素已移除
						clearInterval(startRun);
						return;
					}
					if (thumbObjs.length > amount) {
						thumbObjs.slice(0, amount);
					}
					thumbObjs.each(function(thumbElm) {
						thumbElm.removeClass(o.thumbNowClass);
					});
					if (thumbObjs[index] == undefined) {alert([o.changeObj, o.thumbObj, '切换对象与导航对象数量不符']);}
					thumbObjs[index].addClass(o.thumbNowClass);
				}
				if (o.slideTime <= 0) {
					changeObjs[nowIndex].hide();
					changeObjs[index].show();
				} else {
					changeObjs[nowIndex].fade(0, 2000 / o.slideTime, function() {
						this.hide();
					});
					changeObjs[index].opacity(0);
					changeObjs[index].show();
					changeObjs[index].fade(100, 2000 / o.slideTime);
				}
				nowIndex = index;
				if (o.autoChange == true) {
					clearInterval(startRun); //重置自动切换函数
					startRun = setInterval(runNext, o.changeTime);
				}
			}
		} 
		
		/**切换到下一个**/
		function runNext() {
			index = (nowIndex + 1) % amount;
			fadeAB();
		} 
		
		/**初始化**/
		changeObjs.each(function(changElm) {
			$(changElm).hide();
		});
		changeObjs[0].show(); 
		/**点击任一图片**/
		if (o.thumbObj != null) { 
			/**初始化thumbObj**/
			thumbObjs = miniSelector(o.thumbObj);
			if (thumbObjs.length > amount) {
				thumbObjs.slice(0, amount);
			}
			thumbObjs.each(function(thumbElm) {
				$(thumbElm).removeClass(o.thumbNowClass);
			});
			thumbObjs[0].addClass(o.thumbNowClass);
			thumbObjs.each(function(thumbElm, i) {
				thumbElm._index = i;
				thumbElm.onclick = function() {
					index = this._index;
					fadeAB();
					if (o.clickFalse == true) {
						return false;
					}
				};
				if (o.thumbOverEvent == true) {
					thumbElm.onmouseover = function() {
						index = this._index;
						delayRun = setTimeout(fadeAB, o.delayTime);
					};
					thumbElm.onmouseout = function() {
						clearTimeout(delayRun);
					};
				}
			});
		} 
		/**点击上一个**/
		if (o.botNext != null) {
			miniSelector(o.botNext)[0].onclick = function() {
				if (changeObjs.length > 1) {
					runNext();
				}
				return false;
			};
		} 
		/**点击下一个**/
		if (o.botPrev != null) {
			miniSelector(o.botPrev)[0].onclick = function() {
				if (changeObjs.length > 1) {
					index = (nowIndex + amount - 1) % amount;
					fadeAB();
				}
				return false;
			};
		} 
		/**是否自动运行**/
		if (o.autoChange == true) {
			startRun = setInterval(runNext, o.changeTime);
			if (o.overStop == true) {
				changeObjs.each(function(changeObj) {
					changeObj.onmouseover = function() {
						clearInterval(startRun); //重置自动切换函数
					};
					changeObj.onmouseout = function() {
						startRun = setInterval(runNext, o.changeTime);
					};
				});
			}
		}
	}
	return _change;
})();
