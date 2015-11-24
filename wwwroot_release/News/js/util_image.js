
var Html5FullScreen = {
	browserPrefix:'',
	supportsFullScreen:false,
	isFullScreen: function() { 
		switch (this.browserPrefix){
			case '':
				return document.fullScreen;
			case 'webkit':
				return document.webkitIsFullScreen;
			default:
				return document[this.browserPrefix+'FullScreen'];
		}
	},
	fullscreenchange: function() { 
		return (this.browserPrefix === '') ? 'fullscreenchange' : this.browserPrefix + 'fullscreenchange';
	},
	requestFullScreen: function(el){
		return (this.browserPrefix === '') ? el.requestFullScreen() : el[this.browserPrefix + 'RequestFullScreen']();
	},
	cancelFullScreen: function(el){
		return (this.browserPrefix === '') ? el.cancelFullScreen() : el[this.browserPrefix+ 'CancelFullScreen']();
	},
	zoomInIcon: function(imgWrapEl){
		switch (this.browserPrefix){
			case '':
				imgWrapEl.style.cursor='pointer';
			case 'webkit':
				imgWrapEl.style.cursor='-webkit-zoom-in';
			default:
				imgWrapEl.style.cursor='-moz-zoom-in';
		} 
	},
	zoomOutIcon: function(imgWrapEl){
		switch (this.browserPrefix){
			case '':
				imgWrapEl.style.cursor='pointer';
			case 'webkit':
				imgWrapEl.style.cursor='-webkit-zoom-out';
			default:
				imgWrapEl.style.cursor='-moz-zoom-out';
		}   	
	},
	init: function(){
		if(typeof document.cancelFullScreen!='undefined'){
			this.supportsFullScreen=true;
		}else{
			var pfxArr = ["webkit", "moz", "ms", "o", "khtml"];
			for(var i=0, len=pfxArr.length; i<len; i++){
				this.browserPrefix = pfxArr[i];
				if (typeof document[this.browserPrefix + 'CancelFullScreen' ] != 'undefined' ) {
					this.supportsFullScreen=true;
					break;
				}
				
			}
		}
	}
}; 


Observable={
	_events:{},
	addEvent:function(evtType,handler){
		if(!this._events[evtType]){
			this._events[evtType]=[];
		}
		this._events[evtType].push(handler);
	},
	fireEvent:function(evtType){
		var args =  Array.prototype.slice.call(arguments, 1);
		if(this._events[evtType] && this._events[evtType].length>0){
			for(var i=0;i<this._events[evtType].length;i++){
				this._events[evtType][i].apply(this||window,args);
			}
		}
	},
	removeEvent:function(evtType,handler){
		if(!handler){
			this._events[evtType]=[];
		}else{
			for(var i=0;i<this._events[evtType].length;i++){
				this._events[evtType][i]==handler;
				this._events[evtType].splice(i,1);
			}
		}
	}
};

/*
http://www.cnblogs.com/cloudgamer/archive/2009/01/06/Tween.html
t: current time（当前时间）；
b: beginning value（初始值）；
c: change in value（变化量）；
d: duration（持续时间）。
*/
var Tween = {
	easeIn: function(t,b,c,d){	// 宽度递减公式(先慢后快)
        return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
    },
	easeOut: function(t,b,c,d){ // 宽度递增公式(先快后慢)
		return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
	}
}

var Animator = {
	getSetInterval:function(element, interval, duration, start, end, style, easing, callback){
		var tween=Tween[easing||'easeOut'];
		var startTime=+new Date(), t=0, d=duration+t;
		if(!element.effect)element.effect = {fade:0, move:0, resize:0};
		clearInterval(element.effect[interval]);
		element.effect[interval] = setInterval(function(){
			if(t<d){
				for(i=0;i<style.length;i++){
					var val = tween(t, start[style[i]], end[style[i]]-start[style[i]], d);
					element.style[style[i]] = Math.round(val) +'px';
				}
				t=+new Date()-startTime;
			}else{
				for(i=0;i<style.length;i++){
					element.style[style[i]] = end[style[i]] +'px';
				}
				clearInterval(element.effect[interval]);
				if(callback)
					callback.call(element);
			}
		}, 20);
	},
	move:function(element, position, duration, easing, callback){//移动到指定位置，position:移动到指定left及top 格式{left:200}或{left:200, top:250}；duration:耗时，毫秒数
		var duration=duration||250;
		element.style.position = "absolute";
		var startPosition={
			left:element.offsetLeft,
			top:element.offsetTop
		}
		var arrStyle=[];
		if(typeof position.left == 'number')arrStyle.push("left");
		if(typeof position.top == 'number')arrStyle.push("top");
		Animator.getSetInterval(element, "move", duration, startPosition, position, arrStyle, easing, callback);
	},
	size:function(element, size, duration, easing, callback){//长宽渐变：size:要改变到的尺寸 格式 {width:400}或{width:400, height:250}；duration:耗时，毫秒数
		var duration=duration||500;
		element.style.overflow = "hidden";
		var startSize={
			width:element.offsetWidth,
			height:element.offsetHeight
		}
		var arrStyle=[];
		if(typeof size.width == 'number')arrStyle.push("width");
		if(typeof size.height == 'number')arrStyle.push("height");
		Animator.getSetInterval(element, "size", duration, startSize, size, arrStyle, easing, callback);
	}
	
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

// 兼容处理获取元素当前样式的属性currentStyle
if(window.HTMLElement && !HTMLElement.prototype.attachEvent){ // 如果是非ie
	HTMLElement.prototype.__defineGetter__("currentStyle", function() { // 自定义元素的currentStyle属性
		return this.ownerDocument.defaultView.getComputedStyle(this, null);
	});
}


var $ = function(selector, context) { 
	if (!(this instanceof $)) {
		return new $(selector, context);
	}
	this.init(selector, context);
 };
$.trim= function( text ) {
	return (text || "").replace( /^\s+|\s+$/g, "" );
};
$.browser={
	opera : /opera/i.test(navigator.userAgent),
	msie : !/opera/i.test(navigator.userAgent) && /msie/i.test(navigator.userAgent),
	webkit : /webkit/i.test(navigator.userAgent),
	mozilla : !/webkit/i.test(navigator.userAgent) && /mozilla/i.test(navigator.userAgent)
};

$.each = function(object, callback) {
	if (typeof object.length == 'number') {
		for (var i = 0; i < object.length; i++) {
			if (callback.call(object[i], i, object[i]) === false) {
				break;
			}
		}
	} else {
		for (var key in object) {
			if (callback.call(object[key], key, object[key]) === false) {
				break;
			}
		}
	}
	return object;
};
$.unique=(function() {
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

$.selector=miniSelector;
$.fn = $.prototype = {
	init: function(selector,context) {
		this.els = [];
		this.length = 0;
		if (selector.nodeName && selector.nodeType) {
			return this.push(selector); //存储获取到的Elements      
		}
		if (typeof selector == "string") {
			return this.push($.selector(selector, context));
		}
		if(selector instanceof $ || typeof selector.length=='number' && selector.els &&typeof selector.els.length=='number'){//如果是一个$对象
			return this.push(selector.els);
		}
		if(selector.constructor == Array || selector != window && !selector.nodeType && selector.length && selector[0] && selector[0].nodeType){//如果是一个数组或元素集合
			return this.push(selector);
		}
		return this.push([selector]);
	},
	push: function(els) {
		if (typeof els.length != 'number') {
			els = [els];
		}
		this.els=this.els.concat(els);
		this.length=this.els.length;
	},
	each: function(fn) {
		$.each(this.els, fn);
		return this;
	},
	find : function(expr) {
		var result = [];
		this.each(function() {
			result = result.concat($.selector(expr, this));
		});
		return $($.unique(result));
	},
	get: function(index) {
		return index == undefined ? this.els : this.els[index];
	}
};
$.extend = function(obj, srcObj) {
	for (p in srcObj) {
		if (srcObj.hasOwnProperty(p)) {
			obj[p] = srcObj[p];
		}
	}
	return obj;
};
$.fn.extend = function(p) {
	return $.extend($.prototype,p);
};
$.fn.extend({
	val: function(val) {
		if (val == undefined) 
			return this.els[0].value;
		this.each(function() { this.value = val; });
		return this;
	},
	html: function(val) {
		if (val == undefined) 
			return this.els[0].innerHTML;
		this.each(function(){ this.innerHTML = val;});
		return this;
	},
	attr: function(attr, val) {
		if (val == undefined) {
			if(attr == "href"){
				return this.els[0].getAttribute(attr, 2);
			}
			return this.els[0].getAttribute(attr);
		}
		this.each(function(){ this.setAttribute(attr, attrVal);});
		return this;
	},
	css: function(attr, val) {
		if (val == undefined) 
			return this.els[0].currentStyle[attr];
		this.each(function(){ this.style[attr] = val;});
		return this;
	},
	hide: function(val) {
		this.each(function() { 
			this._bak_display = this._bak_display || this.currentStyle.display;
			this.style.display = 'none';
		});
		return this;
	},
	show: function(val) {
		this.each(function() {
			this.style.display = this._bak_display || '';
			if(this.currentStyle.display=='none')
				this.style.display = 'block';
		});
		return this;
	},
	hasClass:function(className) {
		className = className.replace(/\-/g, "\\-");
		return new RegExp('(?:^|\\s)' + className + '(?:\\s|$)').test(this.els[0].className);
	},
	addClass : function(className) {
		var fClassName = className.replace(/\-/g, "\\-");
		var regex=new RegExp('(?:^|\\s)' + fClassName + '(?:\\s|$)');
		this.each(function() {
			if(!regex.test(this.className)){
				this.className = this.className + ' ' + className;
			}
		});
		return this;
	},
	removeClass : function(className) {
		className = className.replace(/\-/g, "\\-");
		this.each(function() {
			var classToRemove = new RegExp(("(^|\\s)" + className + "(?=\\s|$)"), "i");
			this.className = this.className.replace(classToRemove, "").replace(/^\s+|\s+$/g, "");
		});
		return this;
	}

});

$.noop=function(){};

$.fn.extend({
	remove:function(){
		this.each(function() {
			if (this.parentNode) {
				this.parentNode.removeChild(this);
			}
		});
		return this;
	},
	on:function(evtType,func){
		this.each(function() {
			$.event.add(this, evtType, func);
		});
		return this;
	},
	off:function(evtType,func){
		this.each(function() {
			$.event.remove(this, evtType, func);
		});
		return this;
	}

});

$.event = {
	fix: function(evt){ 
		evt= evt || window.event;
		if (typeof evt.preventDefault === 'undefined') {
			evt.preventDefault = function() {
				evt.returnValue = false;
			};
		}
		if (typeof evt.stopPropagation === 'undefined') {
			evt.stopPropagation = function() {
				evt.cancelBubble = true;
			};
		}
/* 
		Firefox与mousewheel同等事件为DOMMouseScroll，并且事件没有wheelDelta属性值
		注意chrome/safari支持mousewheel事件，并且有detail属性，但值总是为0
		*/
		if (evt.type == 'DOMMouseScroll' && typeof evt.detail != 'undefined' && typeof evt.wheelDelta == 'undefined') {
			evt.wheelDelta = -evt.detail * 40;
		}
		return evt;
	},
	add: function(ele,evtType,handler){
		evtType.replace(/^on/,'');
		if ($.browser.msie) {
		    ele.attachEvent("on" + evtType, handler);
		} else {
			ele.addEventListener(evtType, handler, false);
		}
	},
	remove: function(ele,evtType,handler){
		evtType.replace(/^on/,'');
		if ($.browser.msie) {
		    ele.detachEvent("on" + evtType, handler);
		} else {
			ele.removeEventListener(evtType, handler, false);
		}
	}
};

(function(){
	var tipTimer;
	Tip = function(str, timeOut) {
		var $tipbox = $('#_tipBox');
		if(!$tipbox.length){
			var div=document.createElement('div');
			div.id='_tipBox';
			div.style.cssText='padding: 0.1em 0.8em; text-align:center;font-size:12px;border: 1px solid #DDDDDD; background: #FFF6BF; color: #9C6500; position:absolute; top:0; left:0;_width:98%;';
			document.body.appendChild(div);
			$tipbox = $('#_tipBox');
		}
		$tipbox.html(str);
		if (tipTimer) {
			clearTimeout(tipTimer);
		}
		$tipbox.show();
		if (timeOut) {
			tipTimer = setTimeout(function() {
				$tipbox.hide();
			}, timeOut * 1000);
		}
	};
	Tip.close = function() {
		if (tipTimer) {
			clearTimeout(tipTimer);
		}
		$tipbox.hide();
	};
})();