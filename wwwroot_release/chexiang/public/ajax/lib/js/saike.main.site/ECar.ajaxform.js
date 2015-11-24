/**
 * Copyright (C), 2013-2013, 上海汽车集团股份有限公司
 * Author:   许祥成
 * Date:     2013-12-20
 * Description: form异步提交。
 * 
 */

;(function(){
	/** 
	 * @params form dom或selector
	 * @params settings xhr的setting 
	 */
ECar.ajaxForm=function(){
    var options={};
	options.form=$(arguments[0]);
	options.url=options.form.attr("action");
	options = $.extend(this.defaults,options,arguments.length>1?arguments[1]:{});
    this.init(options);
};
ECar.ajaxForm.prototype={
	defaults:{
		dataType:"json",	//返回数据类型
		type:"post",	//post or get
		url:""			//提交地址
	},
	init:function(options){
		var options=this.options=options,
			form=this.form = options.form;
//		form.on("submit",(function(o){
//			return function(e){
//				e.preventDefault();
//				o.submit();
//			};
//		})(this));
	},
	submit:function(){
		var params=this.serialParams(),xhrSettings={data:params};
		xhrSettings = $.extend({},xhrSettings,this.options);
		return $.ajax(xhrSettings);
	},
	serialParams:function(){
		var params={};
		$("select,:text,:password,input:hidden,input:checked,textarea",this.form).each(function(){
			params[this.name]=this.value;
		});
		return params;
	}
};
})(jQuery,window.ECar||(window.ECar={}));