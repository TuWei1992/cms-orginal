/**
 * Copyright (C), 2013-2013, 上海汽车集团股份有限公司
 * Author:   许祥成
 * Date:     2013-12-20
 * Description: placeholder，使不支持placeholder的浏览器，显示placeholder效果。
 * 
 */

;(function($,ECar){	
	if(ECar.placeholder)return;
	/**
	 * @params item 需要支持的dom或selector
	 * 			options 其它配置（可空）
	 */
ECar.placeholder = function(item, options){
	if("placeholder" in document.createElement('input')||!$(item).is("input,textarea")){
		return;
	}
	var def = {
        nullClass : "clr-gray9"
	};
	$(item).each(function(){
		new Placeholder($.extend({dom:$(this)},def,options||{}));
	});
	return item;
	
};
function Placeholder(options){
	this.options = options;
	this.init();
}
Placeholder.prototype={
	init:function(){
		var options = this.options,
			dom=options.dom, 
			wrapper,
			dp,
			tipCon=$("<span>").css({
				position:"absolute",left:0,top:0,
				"font-size":dom.css("font-size"),
				"padding-left":parseInt(dom.css("padding-left"))+parseInt(dom.css("border-left-width")),
				"padding-right":parseInt(dom.css("padding-right"))+parseInt(dom.css("border-right-width")),
				"padding-top":parseInt(dom.css("padding-top"))+parseInt(dom.css("border-top-width")),
				"padding-bottom":parseInt(dom.css("padding-bottom"))+parseInt(dom.css("border-bottom-width")),
				"margin":dom.css("margin"),
				"line-height":dom.css("line-height"),
				"vertical-align":dom.css("vertical-align")
			}).addClass(options.nullClass),
			leie7=/MSIE [6,7]\./i.test(navigator.appVersion);
		this.tip =dom.attr("placeholder");
		dom.wrap("<div></div>");
		dp=dom.parent();
		dp.css({
			"display":dom.css("display"),
			"width":dom.outerWidth(),
			"height":dom.outerHeight(),
			"position":dom.css("position")=="static"?"relative":dom.css("position")
		});
		if(dom.css("position")!="static"){
			dp.css({
				"top":dom.css("top"),
				"bottom":dom.css("bottom"),
				"left":dom.css("left"),
				"right":dom.css("right")
			});
		}
		if(/inline/.test(dom.css("display"))&&leie7){
			dp.css({
				"display":"inline",
				"zoom":1
			})
		}
		tipCon.text(this.tip);
		if(dom.val().length>0){
			tipCon.hide();
		}
		tipCon.appendTo(dp).click(function(){
			$(this).hide();
			dom.focus();
		});
		dom.focus(function(){
			tipCon.hide();
		}).blur(function(){
			if($(this).val().length==0){
				tipCon.show();
			}
		});
	}
};
})(jQuery,window.ECar||(window.ECar={}));
