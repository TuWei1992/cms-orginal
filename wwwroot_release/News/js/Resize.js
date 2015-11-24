/**
 * 拖拽缩放类
 * 参考自 http://img.jb51.net/online/Resize/Resize.htm
 **/
var Resize=function(obj, options){
	this.init(obj, options)
}
Resize.prototype = {
  init:function(obj, options) {
	this._obj = typeof(obj) == 'string'?document.getElementById(obj):obj;

	this._styleWidth = this._styleHeight = this._styleLeft = this._styleTop = 0;//样式参数
	this._sideRight = this._sideDown = this._sideLeft = this._sideUp = 0;//坐标参数
	this._fixLeft = this._fixTop = 0;//定位参数
	this._scaleLeft = this._scaleTop = 0;//定位坐标

	this._mxSet = function(){};//范围设置程序
	this._mxRightWidth = this._mxDownHeight = this._mxUpHeight = this._mxLeftWidth = 0;//范围参数
	this._mxScaleWidth = this._mxScaleHeight = 0;//比例范围参数

	this._fun = function(){};//缩放执行程序
	
	var _style = this._obj.currentStyle;
	this._borderX = (parseInt(_style.borderLeftWidth) || 0) + (parseInt(_style.borderRightWidth) || 0);//获取边框宽度
	this._borderY = (parseInt(_style.borderTopWidth) || 0) + (parseInt(_style.borderBottomWidth) || 0);
	
	var me=this;
	this._fR = function(evt){me.Resize(evt)};//事件对象(用于绑定移除事件)
	this._fS = function(){me.Stop()};

	this.SetOptions(options);
	/**范围限制**/
	this.Max = !!this.options.Max;
	this._mxContainer = (typeof(obj) == 'string'?document.getElementById(this.options.mxContainer):this.options.mxContainer) || null;
	this.mxLeft = Math.round(this.options.mxLeft);
	this.mxRight = Math.round(this.options.mxRight);
	this.mxTop = Math.round(this.options.mxTop);
	this.mxBottom = Math.round(this.options.mxBottom);
	/**宽高限制**/
	this.Min = !!this.options.Min;
	this.minWidth = Math.round(this.options.minWidth);
	this.minHeight = Math.round(this.options.minHeight);
	//按比例缩放
	this.Scale = !!this.options.Scale;
	this.Ratio = Math.max(this.options.Ratio, 0);
	this._wrap=this.options.wrap?(typeof(this.options.wrap) == 'string'?document.getElementById(this.options.wrap):this.options.wrap):this._obj;
	this.onResize = this.options.onResize;
	this.endResize=this.options.endResize;
	this.proxy=this.options.proxy;
	if(this.options.proxy){
		this._proxy = document.createElement('div');
		this._proxy.style.cssText=this._wrap.style.cssText;
		this._proxy.className = 'rProxy';
		this._proxy.style.height = this._wrap.clientHeight + 'px';
		this._proxy.style.width = this._wrap.clientWidth + 'px';
		this._proxy.style.display =  'none';
		this._wrap.parentNode.appendChild(this._proxy);
	}else{
		this._proxy=this._wrap;
	}
	if(this.options.attachObj){
		this.attachObj=[];
		for(var i=0,len=this.options.attachObj.length;i<len;i++){
			var tmpobj=this.options.attachObj[i];
			tmpobj=typeof(tmpobj)=='string'?document.getElementById(tmpobj):tmpobj;
			if(tmpobj)
				this.attachObj.push(tmpobj);
		}
	}
	
	if(this._obj.currentStyle.position != "absolute"){//如果不为绝对定位，则只能向右缩放
		this._obj.style.position = "relative";
		this.static=true;
	}else{
		this.static=this.options.static;
	}
	/**附加缩放变换控件**/
	this.idPrefix=obj.id+'_';
	var f=document.createElement('span');
	f.innerHTML='<a href="javascript:;" id="@rHandleRightDown" class="rHandle rHandleRightDown" drag="false"> </a>\
		<a href="javascript:;" id="@rHandleLeftDown" class="rHandle rHandleLeftDown"> </a>\
		<a href="javascript:;" id="@rHandleRightUp" class="rHandle rHandleRightUp"> </a>\
		<a href="javascript:;" id="@rHandleLeftUp" class="rHandle rHandleLeftUp"> </a>\
		<a href="javascript:;" id="@rHandleRight" class="rHandle rHandleRight"> </a>\
		<a href="javascript:;" id="@rHandleDown" class="rHandle rHandleDown"></a>\
		<a href="javascript:;" id="@rHandleLeft" class="rHandle rHandleLeft"> </a>\
		<a href="javascript:;" id="@rHandleUp" class="rHandle rHandleUp"> </a>'.replace(/@/gm,this.idPrefix);
	this._wrap.appendChild(f);
	if(!this.static){
		this.Set(this.idPrefix+"rHandleLeftDown", "left-down");
		this.Set(this.idPrefix+"rHandleRightUp", "right-up");
		this.Set(this.idPrefix+"rHandleLeftUp", "left-up");
		this.Set(this.idPrefix+"rHandleLeft", "left");
		this.Set(this.idPrefix+"rHandleUp", "up");
	}
	this.Set(this.idPrefix+"rHandleRight", "right");
	this.Set(this.idPrefix+"rHandleRightDown", "right-down");
	this.Set(this.idPrefix+"rHandleDown", "down");
  },
  /**设置默认属性**/
  SetOptions: function(options) {
    this.options = {//默认值
		Max:		false,//是否设置范围限制(为true时下面mx参数有用)
		mxContainer:"",//指定限制在容器内
		mxLeft:		0,//左边限制
		mxRight:	9999,//右边限制
		mxTop:		0,//上边限制
		mxBottom:	9999,//下边限制
		Min:		true,//是否最小宽高限制(为true时下面min参数有用)
		minWidth:	50,//最小宽度
		minHeight:	30,//最小高度
		Scale:		false,//是否按比例缩放
		Ratio:		0,//缩放比例(宽/高)
		onResize:	null,//缩放时执行
		attachObj:	[],//同时改变尺寸的其它元素
		proxy:		false,//拖拽时修改一个代理元素尺寸来代替实际元素
		wrap:		null,//拖拽手柄的容器
		static:		false//只能向右下拖拽
    };
	if(options)
		for (var property in options) {
			this.options[property] = options[property];
		}
  },
  /**设置触发对象**/
  Set: function(resizeHandle, side) {
	var resizeHandle = document.getElementById(resizeHandle), fun;
	if(!resizeHandle) return;
	switch (side.toLowerCase()) {
	case "up" :
		fun = this.Up;
		break;
	case "down" :
		fun = this.Down;
		break;
	case "left" :
		fun = this.Left;
		break;
	case "right" :
		fun = this.Right;
		break;
	case "left-up" :
		fun = this.LeftUp;
		break;
	case "right-up" :
		fun = this.RightUp;
		break;
	case "left-down" :
		fun = this.LeftDown;
		break;
	case "right-down" :
	default :
		if(this.proxy)this._proxy.style.cursor='nw-resize';
		fun = this.RightDown;
	};
	var me=this;
	$.event.add(resizeHandle,"mousedown", function(evt){me.Start(evt,fun)});
  },
  /**准备缩放**/
  Start: function(e, fun) {
	//防止冒泡(跟拖放配合时设置)
	e.stopPropagation ? e.stopPropagation() : (e.cancelBubble = true);
	
	this._fun = fun;//设置执行程序
	
	this._styleWidth = this._startWidth =  this._obj.clientWidth;//样式参数值
	this._styleHeight = this._startHeight = this._obj.clientHeight;
	this._styleLeft = this._startLeft = this._obj.offsetLeft;
	this._styleTop = this._startTop = this._obj.offsetTop;
	
	this._sideLeft = e.clientX - this._styleWidth;//四条边定位坐标
	this._sideRight = e.clientX + this._styleWidth;
	this._sideUp = e.clientY - this._styleHeight;
	this._sideDown = e.clientY + this._styleHeight;
	
	this._fixLeft = this._styleWidth + this._styleLeft;//top和left定位参数
	this._fixTop = this._styleHeight + this._styleTop;
	/**缩放比例**/
	if(this.Scale){
		this.Ratio = Math.max(this.Ratio, 0) || this._styleWidth / this._styleHeight;
		this._scaleLeft = this._styleLeft + this._styleWidth / 2;
		this._scaleTop = this._styleTop + this._styleHeight / 2;
	};
	/**范围限制**/
	if(this.Max){
		//设置范围参数
		var mxLeft = this.mxLeft, mxRight = this.mxRight, mxTop = this.mxTop, mxBottom = this.mxBottom;
		/**如果设置了容器，再修正范围参数**/
		if(!!this._mxContainer){
			mxLeft = Math.max(mxLeft, 0);
			mxTop = Math.max(mxTop, 0);
			mxRight = Math.min(mxRight, this._mxContainer.clientWidth);
			mxBottom = Math.min(mxBottom, this._mxContainer.clientHeight);
		};
		/**根据最小值再修正**/
		mxRight = Math.max(mxRight, mxLeft + (this.Min ? this.minWidth : 0) + this._borderX);
		mxBottom = Math.max(mxBottom, mxTop + (this.Min ? this.minHeight : 0) + this._borderY);
		/**由于转向时要重新设置所以写成function形式**/
		this._mxSet = function(){
			this._mxRightWidth = mxRight - this._styleLeft - this._borderX;
			this._mxDownHeight = mxBottom - this._styleTop - this._borderY;
			this._mxUpHeight = Math.max(this._fixTop - mxTop, this.Min ? this.minHeight : 0);
			this._mxLeftWidth = Math.max(this._fixLeft - mxLeft, this.Min ? this.minWidth : 0);
		};
		this._mxSet();
		/**有缩放比例下的范围限制**/
		if(this.Scale){
			this._mxScaleWidth = Math.min(this._scaleLeft - mxLeft, mxRight - this._scaleLeft - this._borderX) * 2;
			this._mxScaleHeight = Math.min(this._scaleTop - mxTop, mxBottom - this._scaleTop - this._borderY) * 2;
		};
	};
	if(this.proxy)this._proxy.style.display='block';//如果有改变尺寸替代元素，则显示之
	/**mousemove时缩放 mouseup时停止**/
	$.event.add(document,"mousemove", this._fR);
	$.event.add(document,"mouseup", this._fS);
	if($.browser.msie){
		$.event.add(this._wrap,"losecapture", this._fS);
		this._wrap.setCapture();
	}else{
		$.event.add(window,"blur", this._fS);
		e.preventDefault();
	};
  },
  /**缩放**/
  Resize: function(e) {
	
	window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();//清除选择
	
	this._fun(e);//执行缩放程序
	this._proxy.style.width = this._styleWidth + "px"; //设置样式，变量必须大于等于0否则ie出错
	this._proxy.style.height = this._styleHeight + "px";
	if(!this.static){
		this._proxy.style.top = this._styleTop + "px"; 
		this._proxy.style.left = this._styleLeft + "px";
	}
	if(this.attachObj&&this.attachObj.length>0){
		for(var i=0,len=this.attachObj.length;i<len;i++){
			var attachObj;
			this.attachObj[i].style.width=	this._proxy.style.clientWidth+this._styleWidth-this._startWidth+'px'
			this.attachObj[i].style.height=	this._proxy.style.clientHeight+this._styleHeight-this._startHeight+'px'
		}
	}
	this.onResize&&this.onResize();//执行附加程序
  },
  /**缩放程序**/
  Up: function(e) {
	this.RepairY(this._sideDown - e.clientY, this._mxUpHeight);
	this.RepairTop();
  },
  Down: function(e) {
	this.RepairY(e.clientY - this._sideUp, this._mxDownHeight);
  },
  Right: function(e) {
	this.RepairX(e.clientX - this._sideLeft, this._mxRightWidth);
  },
  Left: function(e) {
	this.RepairX(this._sideRight - e.clientX, this._mxLeftWidth);
	this.RepairLeft();
  },
  RightDown: function(e) {
	this.RepairAngle(
		e.clientX - this._sideLeft, this._mxRightWidth,
		e.clientY - this._sideUp, this._mxDownHeight
	);
  },
  RightUp: function(e) {
	this.RepairAngle(
		e.clientX - this._sideLeft, this._mxRightWidth,
		this._sideDown - e.clientY, this._mxUpHeight
	);
	this.RepairTop();
  },
  LeftDown: function(e) {
	this.RepairAngle(
		this._sideRight - e.clientX, this._mxLeftWidth,
		e.clientY - this._sideUp, this._mxDownHeight
	);
	this.RepairLeft();
  },
  LeftUp: function(e) {
	this.RepairAngle(
		this._sideRight - e.clientX, this._mxLeftWidth,
		this._sideDown - e.clientY, this._mxUpHeight
	);
	this.RepairTop(); this.RepairLeft();
  },
  /**修正程序**/
  /**水平方向**/
  RepairX: function(iWidth, mxWidth) {
	iWidth = this.RepairWidth(iWidth, mxWidth);
	if(this.Scale){
		var iHeight = this.RepairScaleHeight(iWidth);
		if(this.Max && iHeight > this._mxScaleHeight){
			iHeight = this._mxScaleHeight;
			iWidth = this.RepairScaleWidth(iHeight);
		}else if(this.Min && iHeight < this.minHeight){
			var tWidth = this.RepairScaleWidth(this.minHeight);
			if(tWidth < mxWidth){ iHeight = this.minHeight; iWidth = tWidth; }
		}
		this._styleHeight = iHeight;
		this._styleTop = this._scaleTop - iHeight / 2;
	}
	this._styleWidth = iWidth;
  },
  /**垂直方向**/
  RepairY: function(iHeight, mxHeight) {
	iHeight = this.RepairHeight(iHeight, mxHeight);
	if(this.Scale){
		var iWidth = this.RepairScaleWidth(iHeight);
		if(this.Max && iWidth > this._mxScaleWidth){
			iWidth = this._mxScaleWidth;
			iHeight = this.RepairScaleHeight(iWidth);
		}else if(this.Min && iWidth < this.minWidth){
			var tHeight = this.RepairScaleHeight(this.minWidth);
			if(tHeight < mxHeight){ iWidth = this.minWidth; iHeight = tHeight; }
		}
		this._styleWidth = iWidth;
		this._styleLeft = this._scaleLeft - iWidth / 2;
	}
	this._styleHeight = iHeight;
  },
  /**对角方向**/
  RepairAngle: function(iWidth, mxWidth, iHeight, mxHeight) {
	iWidth = this.RepairWidth(iWidth, mxWidth);
	if(this.Scale){
		iHeight = this.RepairScaleHeight(iWidth);
		if(this.Max && iHeight > mxHeight){
			iHeight = mxHeight;
			iWidth = this.RepairScaleWidth(iHeight);
		}else if(this.Min && iHeight < this.minHeight){
			var tWidth = this.RepairScaleWidth(this.minHeight);
			if(tWidth < mxWidth){ iHeight = this.minHeight; iWidth = tWidth; }
		}
	}else{
		iHeight = this.RepairHeight(iHeight, mxHeight);
	}
	this._styleWidth = iWidth;
	this._styleHeight = iHeight;
  },

  RepairTop: function() {
	this._styleTop = this._fixTop - this._styleHeight;
  },
  RepairLeft: function() {
	this._styleLeft = this._fixLeft - this._styleWidth;
  },
  RepairHeight: function(iHeight, mxHeight) {
	iHeight = Math.min(this.Max ? mxHeight : iHeight, iHeight);
	iHeight = Math.max(this.Min ? this.minHeight : iHeight, iHeight, 0);
	return iHeight;
  },
  RepairWidth: function(iWidth, mxWidth) {
	iWidth = Math.min(this.Max ? mxWidth : iWidth, iWidth);
	iWidth = Math.max(this.Min ? this.minWidth : iWidth, iWidth, 0);
	return iWidth;
  },
  RepairScaleHeight: function(iWidth) {
	return Math.max(Math.round((iWidth + this._borderX) / this.Ratio - this._borderY), 0);
  },
  RepairScaleWidth: function(iHeight) {
	return Math.max(Math.round((iHeight + this._borderY) * this.Ratio - this._borderX), 0);
  },
  /**停止缩放**/
  Stop: function() {
	if(this.proxy)this._proxy.style.display='none';
	$.event.remove(document,"mousemove", this._fR);
	$.event.remove(document,"mouseup", this._fS);
	if($.browser.msie){
		$.event.remove(this._wrap,"losecapture", this._fS);
		this._wrap.releaseCapture();
	}else{
		$.event.remove(window,"blur", this._fS);
	}
	if(this.endResize&&typeof(this.endResize)=='function'){
		this.endResize({height: this._styleHeight, width: this._styleWidth, top: this._styleTop, left: this._styleLeft });	
	}
  }
};
