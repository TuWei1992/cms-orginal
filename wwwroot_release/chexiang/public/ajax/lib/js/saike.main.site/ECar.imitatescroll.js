/** Copyright (c) 2011 Brandon Aaron (http://brandonaaron.net)
 * Licensed under the MIT License (LICENSE.txt).
 *
 * Thanks to: http://adomas.org/javascript-mouse-wheel/ for some pointers.
 * Thanks to: Mathias Bank(http://www.mathias-bank.de) for a scope bug fix.
 * Thanks to: Seamus Leahy for adding deltaX and deltaY
 *
 * Version: 3.0.6
 * 
 * Requires: 1.2.2+
 * 
 * jQuery 滚轮事件
 */

;(function($) {

var types = ['DOMMouseScroll', 'mousewheel'];

if ($.event.fixHooks) {
    for ( var i=types.length; i; ) {
        $.event.fixHooks[ types[--i] ] = $.event.mouseHooks;
    }
}

$.event.special.mousewheel = {
    setup: function() {
        if ( this.addEventListener ) {
            for ( var i=types.length; i; ) {
                this.addEventListener( types[--i], handler, false );
            }
        } else {
            this.onmousewheel = handler;
        }
    },
    
    teardown: function() {
        if ( this.removeEventListener ) {
            for ( var i=types.length; i; ) {
                this.removeEventListener( types[--i], handler, false );
            }
        } else {
            this.onmousewheel = null;
        }
    }
};

$.fn.extend({
    mousewheel: function(fn) {
        return fn ? this.bind("mousewheel", fn) : this.trigger("mousewheel");
    },
    
    unmousewheel: function(fn) {
        return this.unbind("mousewheel", fn);
    }
});


function handler(event) {
    var orgEvent = event || window.event, args = [].slice.call( arguments, 1 ), delta = 0, returnValue = true, deltaX = 0, deltaY = 0;
    event = $.event.fix(orgEvent);
    event.type = "mousewheel";
    
    // Old school scrollwheel delta
    if ( orgEvent.wheelDelta ) { delta = orgEvent.wheelDelta/120; }
    if ( orgEvent.detail     ) { delta = -orgEvent.detail/3; }
    
    // New school multidimensional scroll (touchpads) deltas
    deltaY = delta;
    
    // Gecko
    if ( orgEvent.axis !== undefined && orgEvent.axis === orgEvent.HORIZONTAL_AXIS ) {
        deltaY = 0;
        deltaX = -1*delta;
    }
    
    // Webkit
    if ( orgEvent.wheelDeltaY !== undefined ) { deltaY = orgEvent.wheelDeltaY/120; }
    if ( orgEvent.wheelDeltaX !== undefined ) { deltaX = -1*orgEvent.wheelDeltaX/120; }
    
    // Add event and delta to the front of the arguments
    args.unshift(event, delta, deltaX, deltaY);
    
    return ($.event.dispatch || $.event.handle).apply(this, args);
}

})(jQuery);


/**
 * Copyright (C), 2013-2013, 上海汽车集团股份有限公司
 * Author:   许祥成
 * Date:     2013-12-20
 * Description: 自定义滚动条
 * 
 */              
;(function($,ECar){
ECar.imitatescroll=function(options){
    var def = {
        id:null,            //ID
        direction:"top",   //滚动方向;left：水平方向;top:垂直方向
        handle:".scrollbar-handler",      //滚动条元件
        moveDom:".scroll-content",    //滚动内容元件
        conWidth:1520,      //内容宽度
        showWidth:600,       //显示宽度
        scrollScale: 10		// 滚动精度
    };
    if (arguments.length > 0 && typeof(arguments[0]) == 'object') {
		$.extend(def, options);
	} else {
		return false;
	}
    var _id = $(def.id);
    var _handle = _id.find(def.handle);
    var _move = _id.find(def.moveDom);
    var oBarM = _id.find(".scrollbar-bg");
    var oBarL = _id.find(".scrollbar-narrow-up");
    var oBarR = _id.find(".scrollbar-narrow-down");
    var imitatescrollfn={
        //初始化
        init:function(isReset){
            this.iScale = 0;this.timer=null;this.disX=0,this.scrollable=true;
            if(def.direction=="left"){
            	def.conWidth=_move.width();
            	def.showWidth=_id.width();
            	oBarM.css({
            		width:oBarM.parent().width()-oBarL.width()-oBarR.width(),
            		margin:"0 "+oBarR.width()+"px 0 "+oBarL.width()+"px"
            	});
            	_handle.width(oBarM.width()*def.showWidth/def.conWidth);
                this.maxL=oBarM.outerWidth()-_handle.outerWidth();this.realWidth=def.conWidth-def.showWidth;
            }else{
            	_move.width(_move.parent().width()-16);
            	def.conWidth=_move.height();
            	def.showWidth=_id.height();
            	oBarM.css({
            		height:oBarM.parent().height()-oBarL.height()-oBarR.height(),
            		margin:oBarR.height()+"px 0 "+oBarL.height()+"px 0"
            	});
            	_handle.height(oBarM.height()*Math.min(1,def.showWidth/def.conWidth));
                 this.maxL=oBarM.outerHeight()-_handle.outerHeight();this.realWidth=def.conWidth-def.showWidth;
            }
            _handle.css(def.direction,0);
            if(!isReset){
	            this.dragH();
	            this.pressH();
	            this.clickH();
	            this.wheelH();
	            _handle.click(function(e){
	                e.stopPropagation();
	            });
            }
            _handle.show();
        	if(def.conWidth<=def.showWidth){
        		//oBarM.parent().hide();
        		this.scrollable=false;
        	}else{
        		//oBarM.parent().show();
        		this.scrollable=true;
        	}
            return this;
        },
        //滚动条的拖拽
        dragH:function(){
            var that = this;
            _handle.bind("mousedown",function(e){
                that.disX = (def.direction=="left"?e.pageX:e.pageY)-_handle.position()[def.direction];
                $(document).bind("mousemove",function(e){
                    var iL=(def.direction=="left"?e.pageX:e.pageY)-that.disX;
                    iL <= 0 && (iL = 0);
                    iL >= that.maxL && (iL = that.maxL);
                    _handle.css(def.direction,iL);
                    that.iScale = iL / that.maxL;
                    _move.css(def.direction,-that.realWidth*that.iScale);
			        return false;
                });

                $(document).bind("mouseup",function(){
                    that.stopDrap();
                });
                return false;
            });
        },
        //前后控制按钮的鼠标长按控制
        pressH:function(){
            var that = this;
            var speed = 0;
            oBarR.bind("mousedown",function(){
                speed = 5;
                that.timer=setInterval(function(){
                    that.togetherMove(parseInt(_handle.css(def.direction))+speed);
                },10);
            });
            oBarL.bind("mousedown",function(){
                speed = -5;
                that.timer=setInterval(function(){
                    that.togetherMove(parseInt(_handle.css(def.direction))+speed);
                },10);
            });
            oBarL.bind("mouseup",function(){
                clearInterval(that.timer);
            });
            oBarR.bind("mouseup",function(){
                clearInterval(that.timer);
            });
        },
        //前后控制按钮的鼠标点击控制
        clickH:function(){
            var that = this;
            oBarM.bind("click",function(e){
                var iTarget = (def.direction=="left"?e.pageX:e.pageY)-_id.offset()[def.direction]-$(this).position()[def.direction]-_handle.outerWidth()/2;
                that.togetherMove(iTarget);
            });
        },
        //滚轮控制
        wheelH:function(){
            var that = this;
            _id.bind("mousewheel",function(e,delta){
            	if(that.scrollable)
            		that.togetherMove(parseInt(_handle.css(def.direction))-delta*def.scrollScale); // 原来是10
                return false;
            });
        },
        //滚动主体的位移控制
        togetherMove:function(iTarget){
            //console.log(iTarget)
            if (iTarget <= 0){
                this.timer && clearInterval(this.timer);
                iTarget = 0;
            }
            else if (iTarget >= this.maxL){
                this.timer && clearInterval(this.timer);
                iTarget	= this.maxL;
            }
            this.iScale=iTarget/this.maxL;
            _move.css(def.direction,-this.realWidth*this.iScale);
            _handle.css(def.direction,iTarget);
        },
        //停止拖拽
        stopDrap:function(){
            $(document).unbind("mousemove").unbind("mouseup");
        },
        scrollTo:function(height){
        	var top = parseInt(_move.css("top"));
        	if(-top+_move.parent().height()>height&&-top<height)return;
        	this.togetherMove(height*this.maxL/this.realWidth);
        },
        //重置
        reset:function(){
        	_move.removeAttr("style");
        	_handle.removeAttr("style");
        	this.init(true);
        }
    };
    return imitatescrollfn.init();
};
})(jQuery,window.ECar||(window.ECar={}));