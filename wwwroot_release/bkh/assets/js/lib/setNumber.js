  /**
 * Copyright (C), 2014, 上海汽车集团股份有限公司
 * Author:   王景亮   qq:546172171
 * Date:     2014-12-18
 * Description: 别克商城 
 */ 
  //设置商品数量
  var setNumber={
    init:function(obj,inputObj,max,min,stock){
      var type=obj.attr('rel-type'),
        flag=type=="down"?true:false,
        value=parseInt(inputObj.val()),
        max=max>stock?stock:max;
      this.inputObj=inputObj;
      this.max=max;
      this.min=min;
      if(!type)return;
      if(value>=max && !flag) {alert('库存不足');return;}
      if(value<=min && flag)return;
      this[''+type+''](parseInt(inputObj.val()),obj);
    },
    down:function(value,obj){
      value--;
      if(value<=this.min){
        obj.removeClass('curr');
      }

      var siblings=obj.siblings('span');
      if(!siblings.hasClass('curr')&&value<this.max){
        siblings.addClass('curr');
      }     
      this.inputObj.val(value);     
    },
    add:function(value,obj){
      value++;
      if(value>=this.max){
        obj.removeClass('curr');
      }     
      var siblings=obj.siblings('span');
      if(!siblings.hasClass('curr')&&value>this.min){
        siblings.addClass('curr');
      }

      this.inputObj.val(value);
    }
  } 