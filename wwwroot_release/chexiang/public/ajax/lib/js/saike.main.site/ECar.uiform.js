/**
 * Copyright (C), 2013-2013, 上海汽车集团股份有限公司
 * Author:   许祥成
 * Date:     2013-12-20
 * Description: 表单美化，含select和checkbox。
 * todo:radio
 */
;(function(){
if(ECar["uiform"])return;
ECar.uiform=function(){
    var options,
		form=$(arguments[0]);
    	options = $.extend({},this.defaults,arguments[arguments.length-1]);
    this.init(form,options);
};
ECar.uiform.prototype={
	defaults:{
		selectClass:"form-select",
		selectHoverClass:"",
		selectIgnoreFirstOption:true,
		inputCheckboxClass:"form-checkbox",
		inputCheckboxHoverClass:"",
		inputRadioClass:"form-radio",
		inputRadioHoverClass:"",
		selectMaxHeight:200,
		selectOptionShowSpeed:100,
		checkboxClass:"form-checkbox"
	},
	init:function(form,options){
		this.options=options;
		form.find("input:text, input:password, textarea").focus(function(){
			$(this).addClass('focus');
		}).blur(function(){
			$(this).removeClass('focus');
		});
		
		if(form.is("form")){
			form=form.find("select,:radio,:checkbox");
		}
		form.each(function(){
			var item=$(this),ui;
			if(item.is("select")){
				new prettySelect(this,options);
			}else if(item.is(":checkbox")){
				new prettyCheckbox(this,options);
			}else if(item.is(":radio")){
				new prettyRadio(this,options);
			}
		});
		}
};

function prettySelect(dom,options){
	var dom=this.dom=$(dom), ui=this.ui=$("<div>").addClass(options.selectClass).css("float",dom.css("float")),
		handle=this.handle=$("<span>").addClass("form-select-handle").appendTo(ui),
		label=this.label=$("<span>").appendTo(handle),
		triangle=this.triangle=$("<i>").appendTo(handle), // triangle, added by cd
		optionWrapper=this.optionWrapper=$("<div>").addClass("form-select-options").appendTo(ui),
		optionScroller=this.optionScroller=$("<div>").addClass("form-select-scroll").appendTo(optionWrapper),
		option=this.option=$("<ul>").appendTo(optionScroller);
		options=this.options=options,
		this.prompt=dom.data("title");
		this.scroller=null;
	ui.insertAfter(dom);
	dom.css("position","absolute").css("visibility","hidden").appendTo(ui);
	
	this.render();
	
	handle.bind("click",(function(o){
		return function(e){
			if(!o.isDisabled())
				o.isOpen()?o.close():o.open();
		};
	})(this));
	dom.bind("update",(function(o){
		return function(e){
			o.render();
		};
	})(this)).bind("change",(function(o){
		return function(e){
			o.change();
		};
	})(this));
	optionWrapper.on("click","li",(function(o){
		return function(e){
			o.select(e.target);
			o.label.removeClass("form-select-unselected");
		};
	})(this));

	$(document.body).on("click",(function(o){
		return function(e){
			if(!$.contains(o.ui[0],e.target))
			o.close();
		};
	})(this));
}
prettySelect.prototype={
	render:function (){
		var option=this.option,
			label=this.label,
			handle=this.handle,
			triangle=this.triangle,
			optionWrapper=this.optionWrapper,
			ui=this.ui,
			dom=this.dom,
			settings=this.options,
			optionScroller=this.optionScroller;
		this.scroller=null;
		option.empty();
		label.empty();
		optionScroller.removeAttr("style");
		optionScroller.removeClass("scroll-wrapper");
		option.removeClass("scroll-content");
		optionScroller.find(">.scrollbar").remove();
		var fg=$(document.createDocumentFragment());
		var options=dom.find("option"+(settings.selectIgnoreFirstOption?":gt(0)":"")+"").each(function(){
			var $this=$(this), text=$this.text(),value=$this.val(),li=$("<li>").data("value",value).text(text);
			fg.append(li);
			if($this.prop("selected")){
				label.text(text);
				li.addClass("form-option-selected");
			}
		});
		ui.width(dom.outerWidth());
		optionWrapper.width(ui.width());
		option.width(optionWrapper.width());
		optionScroller.width(option.width());
		var rightSpace = triangle.outerWidth()+parseInt(triangle.css("marginRight"))+parseInt(triangle.css("right"))+15+5; // added by CD, 15---左边距离, 5---裕量
		label.width(ui.width()-rightSpace);////////////// 应该减去小三角所占宽度＋右边距, original code: -35;
		if(label.text()===""){
			label.text(this.prompt||dom.find("option").first().text()).addClass("form-select-unselected");
		}else{
			label.removeClass("form-select-unselected");
		}
		fg.appendTo(option);
		var p = optionWrapper.parent();
		optionWrapper.appendTo(document.body);
		if(settings.selectMaxHeight&&optionWrapper.height()>settings.selectMaxHeight){
			optionScroller.height(settings.selectMaxHeight);
			optionScroller.addClass("scroll-wrapper");
			option.addClass("scroll-content");
			$('<div class="scrollbar">'+
					'<div class="scrollbar-narrow scrollbar-narrow-up"></div>'+
					'<div class="scrollbar-bg">'+
						'<div class="scrollbar-handler"><div class="scrollbar-handler-top"></div><div class="scrollbar-handler-bottom"></div></div>'+
					'</div>'+
					'<div class="scrollbar-narrow scrollbar-narrow-down"></div>'+
				'</div>').appendTo(optionScroller);
			this.scroller=ECar.imitatescroll({
				id:optionScroller
			});
		}

		optionWrapper.appendTo(p);
	},
	select:function(li){
		var li=$(li), value=li.data("value");
		this.dom
			.val(value)
			.trigger("change");
		this.close();
	},
	change:function(){
		var option = this.dom.find("option:selected"),val=option.val(),li,that=this;
		this.label.text(option.text());
		li=$.grep(this.option.find("li").toArray(),function(item){
			return $(item).data("value")==val;
		});
		this.option.find(".form-option-selected").removeClass("form-option-selected");
		$.each(li,function(){
			$(this).addClass("form-option-selected");
		});
	},
	open:function(){
		var handle=this.handle,optionWrapper=this.optionWrapper,$win=$(window);
		if(handle.hasClass("form-select-expand"))return;
		if(!this.scroller){
			this.scroller=ECar.imitatescroll({
				id:this.optionScroller
			});
		}
		this.scroller.reset();
		optionWrapper.css("visibility","hidden").show();
		if($win.height()-(handle.offset().top+Math.min(handle.height(),this.options.selectMaxHeight)-$win.scrollTop())<optionWrapper.height()){
			optionWrapper.addClass("form-select-options-above");
		}else{
			optionWrapper.removeClass("form-select-options-above");
		}
		optionWrapper.css("visibility","hidden").hide();
		optionWrapper.css("visibility","visible");
		optionWrapper.slideDown(this.options.selectOptionShowSpeed,(function(o){
			return function(){
//				if(!o.scroller){
//					o.scroller=ECar.imitatescroll({
//						id:o.optionScroller
//					});
//				}
				o.scroller&&o.scroller.reset();
			};
		})(this));
		handle.addClass("form-select-expand");
		this.dom.trigger("select.open",[this.dom]);
		//this.optionScroller&&this.optionScroller.reset();
	},
	close:function(){
		var handle=this.handle,optionWrapper=this.optionWrapper;
		if(!handle.hasClass("form-select-expand"))return;
		optionWrapper.slideUp(this.options.selectOptionShowSpeed);
		handle.removeClass("form-select-expand");
	},
	isOpen:function(){
		return this.handle.hasClass("form-select-expand");
	},
	isDisabled:function(){
		return this.dom.prop("disabled");
	}
};
function prettyCheckbox(dom,options){
	var dom=this.dom=$(dom), ui=this.ui=$("<div>").addClass(options.checkboxClass).css("float",dom.css("float")),
	handle=this.handle=$("<div>").addClass("form-checkbox-inner").appendTo(ui);
	ui.insertAfter(dom);
	dom.hide().appendTo(ui);
	dom.bind("change",(function(o){
		return function(){
			o.update();
		};
	})(this));
	ui.click((function(o){
		if(/MSIE\s[7|8]./.test(navigator.appVersion)){
			return function(e){
				e.stopPropagation();
				o.dom[0].click();
			};
		}else{
			return function(){
				dom.trigger("change");
			};
		}
	})(this));

	if(/MSIE\s[7|8]./.test(navigator.appVersion)){
		var label=dom.parents("label");
		if(dom.attr("id")){
			label.add($("label[for="+dom.attr("id")+"]"));
		}
		label.click((function(o){
			return function(){
				o.dom[0].click();
			};
		})(this));
	}
	if(dom.prop("checked")){
		dom.triggerHandler("change");
	}
}
prettyCheckbox.prototype={
	update:function(){
		var dom=this.dom,handle=this.handle;
		(dom.prop("checked")?handle.addClass:handle.removeClass).call(handle,"form-checkbox-checked");
	}
};
function prettyRadio(dom,options){
	
}
})(jQuery,window.ECar||(window.ECar={}));