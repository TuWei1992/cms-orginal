function setHigh(){
	var curtitle=$("div.tree dl .current a").text();
	var eles=$("div.rect input");
	$("div.blankBg .rectCur").removeClass("rectCur");
	for(var i=0;i<eles.length;i++){
		if($(eles[i]).attr("value").indexOf(curtitle) != -1){
			$(eles[i]).parent().addClass("rectCur");
			return;
		}
	}	
}

//打印方法
function printCon(con){
	document.body.innerHTML=con;
	window.doPrinted=true;
	window.print();
}

Page.onLoad(function(){
	 
	 //展开、收起效果
	 $("div.tree dl span").click( 
		function () { 
			$(this).parent().nextAll("dd").toggle();
			$(this).toggleClass("more");
		}
	);
	 
	 //高亮
	 $("div.tree dl a").click( 
		function () { 
			$("div.tree dl .current").removeClass("current");
			$(this).parent().addClass("current");
			setHigh();
		}
	);
	 
	window.onresize=function(){
		new Paper('drawPaper',edit_callbackFunc,$('#hotArea').get(0).value,false); 
		setHigh();
	};
	
	//滚动效果
	  var wrap_w=$(".imgs").outerWidth(true);
	  var ele_w=$(".imgs ul li").outerWidth(true);
	  var i = Math.floor(wrap_w/ele_w); 
	  //容器可现实的图片元素的个数
	  var m = Math.floor(wrap_w/ele_w); 
	  //用于计算的变量
    var $content = $(".imgs ul");
	  var count = $content.find("li").length;//总共的存放图片元素的个数
	  if(count <= i) { 
	  	$("div.next").hide(); $("div.prev").hide();
	  	return;
	  }else{

		  //下一期
		  $("div.next").live("click",function(){
				var $scrollableDiv = $(".imgs").find("ul");
				
				if( !$scrollableDiv.is(":animated")){  //判断元素是否正处于动画，如果不处于动画状态，则追加动画。
					if(m<count){  //判断 i 是否小于总的个数
						m++;
						$scrollableDiv.animate({left: "-="+ele_w+"px"}, 600);
						$("div.prev").removeClass("prevno");
					}
					
					if(m==count){
						$(this).addClass("nextno");
					}
				}
				return false;
		  });
		   //上一期
		  $("div.prev").live("click",function(){
				var $scrollableDiv = $(".imgs").find("ul");
				if( !$scrollableDiv.is(":animated")){
					if(m>i){ //判断 i 是否小于总的个数
						m--;
						$scrollableDiv.animate({left: "+="+ele_w+"px"}, 600);
						$("div.next").removeClass("nextno");
					}
					if(m==i){
						$(this).addClass("prevno");
					}
				}
				return false;
		  });
	 }

})




/**
 * 报纸热区绘制类
 * zving)wangzhaohui
 **/
 
/*************************报纸类****************************/
//id为图片的id或图片对象，hotareaHash为初始化时要预置的热区的JSON字符串数据，func为双击报纸热区时要调用的方法，drawable为是否可在报纸上绘制热区。
var Paper=function(id,func,hotareaData,drawable){
	this.init(id,func,hotareaData,drawable);
}

Paper.getInstance=function(id){
	return $('#'+id).get(0).PaperInstance;
};

Paper.prototype={
	init:function(id,func,hotareaData,drawable){
		var ele=$('#'+id).get(0);
		if(!ele){
			return alert('不存在元素'+id);
		}
		if(ele.PaperInstance){
			document.getElementsByTagName('BODY')[0].removeChild(ele.PaperInstance.paperEl.parentNode);
			ele.PaperInstance=null;
		}
		ele.PaperInstance=this;
		if(drawable){
			this.drawable=true;
		}
		var pos=getElPos(ele);
		var posDiv=document.createElement('div');
		posDiv.style.cssText='position:absolute;border:solid black 1px;left:'+pos.x+'px;top:'+pos.y+'px;';
		posDiv.innerHTML='<div id="_paper_'+ele.id+'" class="blankBg" style="position:relative;cursor:default;width:'+ele.width+'px;height:'+ele.height+'px;-moz-user-select:-moz-none;"></div>';
		posDiv.oncontextmenu = function(){return false;};
		document.getElementsByTagName('BODY')[0].appendChild(posDiv);
		this.callFunc=func;
		this.paperEl=$('#_paper_'+ele.id).get(0);
		this.rectArray=[];
		if(hotareaData){
			this.initFromJOSN(hotareaData);
		}
		this.attachEvent();
	},
	initFromJOSN : function (hotareaData) { // 根据表单的json数据初始化报纸
		if(typeof(hotareaData)=='string')
			var hotareaData=JSONA.evaluate(hotareaData);
		for(var i=0,len=hotareaData.length;i<len;i++){
			var hotarea=hotareaData[i];
			var rect=new Rect();
			if(this.drawable){
				rect.editable=true;
			}
			rect.container=this.paperEl;
			rect.callFunc=this.callFunc;
			rect.rectArray=this.rectArray;
			var ele=rect.create(hotarea.jsonData);
			if(this.drawable){
				ele.className='editableRect';
				rect.addTitle();
			}else{
				ele.className='rect';
			}
			ele.style.cssText=hotarea.css;
		}
	},
	attachEvent : function () {
		var me=this;
		if(this.drawable){
			$.event.add(this.paperEl,"mousedown", function(evt){me.mouseDownBehavior(evt);});
		}
	},
	mouseDownBehavior:function(evt){
		var evt=$.event.fix(evt);
		var target = evt.target;
		//console.log(target);
		if(this.paperEl==target){ //如果鼠标是点在报纸上，而非其它热区上，就开始画热区
			var rect=new Rect();
			if(this.drawable){rect.editable=true;}
			rect.container=this.paperEl;
			rect.callFunc=this.callFunc;
			rect.rectArray=this.rectArray;
			rect.start(evt);  //开始画矩形
		}
	},
	hotarea2JSON : function () { // 获取报纸当前热区的json数据
		var tempObj=[];
		for(var i=0,len=this.rectArray.length;i<len;i++){
			if(this.rectArray[i]==0){continue;}
			var r=this.rectArray[i].elem;
			tempObj.push({id:r.id,css:r.style.cssText,jsonData:JSONA.evaluate($('#'+r.id+'_jsonData').get(0).value)});
		}
		var json2Str=JSONA.toString(tempObj);
		return json2Str;
	}
}

/**** 矩形类 ****/
var Rect=function(){
	this.init();
}
Rect.selected=null;
Rect.prototype={
	init:function(){
		this.rectArray = null;
		this.container=null;
		this.callFunc=null;
		this.editable=false;
	},
	start:function(evt){  // 鼠标点在报纸上开始画热区
		Rect.selected && Rect.selected.blur();
		var pos=this.container.pos=getElPos(this.container);
		var evtPos=getEvtPos(evt);
		var ele=this.create();  // 创建热区
		ele.className='editableRect';
		ele.style.left=evtPos.x-pos.x+'px';
		ele.style.top=evtPos.y-pos.y+'px';
		ele.style.height = 1 + "px";
		ele.style.width = 1 + "px";
		ele.mouseBeginX = evtPos.x; //mouseBeginX，mouseBeginY是辅助变量，记录下鼠标按下时的位置
		ele.mouseBeginY = evtPos.y;
		ele.className='editableRect drawing';
		var me=this;
		this._fMove=function(evt){me.move(evt)}
		this._fFinish=function(evt){me.finish(evt)}
		$.event.add(document,"mousemove", this._fMove);
		$.event.add(document,"mouseup", this._fFinish);
	},
	create:function(_jsonData){ // 创建热区  
		this.id = this.rectArray.length + "";
		var ele=this.elem = document.createElement('div');
		ele.style.position = "absolute";
		ele.style.padding = "0";
		ele.style.zIndex = "10";
		ele.id='_rect'+this.id;
		if(this.editable){
			ele.innerHTML='<input type="hidden" id="'+ele.id+'_jsonData" value="{title:\'双击编辑热区\'}" /><a href="javascript:;" class="rHandle closeBtn" title="删除热区">X</a>';
		}else{
			ele.innerHTML='<input type="hidden" id="'+ele.id+'_jsonData" value="{title:\'双击编辑热区\'}" />';
		}
		this.container.appendChild(ele);		
		this.jsonDataEl=$('#'+ele.id+'_jsonData').get(0);
		if(_jsonData){  
			this.jsonDataEl.value=typeof(_jsonData)=='string'?_jsonData:JSONA.toString(_jsonData); // 将json数据赋值给隐藏域
		}
		this.attachBehaviors();
		this.rectArray.push(this);
		this.$elem=$(ele);
		return ele;
	},
	move:function(evt){
		var evt=$.event.fix(evt);
		var ele = this.elem;
		//dx，dy是鼠标移动的距离
		var dx = getEvtPos(evt).x - ele.mouseBeginX;
		var dy = getEvtPos(evt).y - ele.mouseBeginY;
		//如果dx，dy <0,说明鼠标朝左上角移动，需要做特别的处理
		var el=getEvtPos(evt).x-this.container.pos.x;
		var et=getEvtPos(evt).y-this.container.pos.y;
		if(dx<0&&el>0){
			ele.style.left = el+'px';
			ele.style.width = Math.abs(dx)+'px';
		}else if(dx>0&&ele.offsetLeft+dx<this.container.clientWidth){
			ele.style.width = Math.abs(dx)+'px';
		}
		if(dy<0&&et>0){
			ele.style.top = et+'px';
			ele.style.height = Math.abs(dy)+'px';
		}else if(dy>0&&ele.offsetTop+dy<this.container.clientHeight){
			ele.style.height = Math.abs(dy)+'px';
		}
	},
	finish:function(evt){
		var evt=$.event.fix(evt);
		var me=this;
		//onmouseup时释放onmousemove，onmouseup事件句柄
		$.event.remove(document,"mousemove", this._fMove);
		$.event.remove(document,"mouseup", this._fFinish);
		if(this.elem.clientWidth<30&&this.elem.clientHeight<20){
			this.remove();
			return;
		}		
		this.$elem.removeClass('drawing');
		this.addTitle();
		this.focus();
	},
	attachBehaviors:function(){
		var me=this;
		if(this.editable){ // 热区在编辑状态下各事件触发时的行为
			if (window.Drag){
				new Drag(this.elem,{Limit:true, mxContainer:this.container});//注册拖拽方法
			}
			if (window.Resize){
				new Resize(this.elem,{ Max: true, mxContainer:this.container});//注册为可调整大小
			}
			this.$elem=this.$elem || $(this.elem);
			var $closeBtn = this.$elem.find("a.closeBtn");
			$.event.add(this.elem,"dblclick", function(){me.callFunc(this,me.jsonDataEl);});
			$.event.add(this.elem,"mousedown", function(){me.focus();});
			$.event.add(document,"keydown", function(evt){me.keyDownBehavior(evt);});
			$closeBtn.on('click',function(){if(Rect.selected){ Rect.selected.remove();}});
		}else{
			$.event.add(this.elem,"click", function(){me.callFunc(me.jsonDataEl);});
			$.event.add(this.elem,"mouseover", function(evt){me.focus();me.tipOn(evt);});
			$.event.add(this.elem,"mousemove", function(evt){me.focus();me.tipMove(evt);});
			$.event.add(this.elem,"mouseout", function(evt){me.blur(evt);me.tipOff();});
		}
	},
	remove:function(){
		this.container.removeChild(this.elem);
		Rect.selected=null;
		for(var i=0,len=this.rectArray.length;i<len;i++){
			if(this == this.rectArray[i]){
				this.rectArray[i]=0;
			}
		}
	},
	addTitle:function(){
		var ele=this.titleDiv=document.createElement('div');
		ele.className='titleArea';
		ele.innerHTML='<span id="'+this.elem.id+'_title">双击编辑热区</span>';
		this.elem.appendChild(ele);
		this.spanEle=$('#'+this.elem.id+'_title').get(0);
		var me=this;
		if($.browser.msie){
			$.event.add(this.jsonDataEl,"propertychange", function(){me.rerurnJsonDataChange();});
		}else{
			$.event.add(this.jsonDataEl,"DOMAttrModified", function(){me.rerurnJsonDataChange();});
		}
		this.rerurnJsonDataChange();
	},
	rerurnJsonDataChange:function(){
		this.spanEle.innerHTML=JSONA.evaluate(this.jsonDataEl.value).title; // 解析隐藏域的json数据的标题属性值赋值给span元素
	},
	keyDownBehavior: function (evt) {  //Delete键删除选中的热区
		var evt=$.event.fix(evt);
		if (evt.keyCode == 46) { 
			var target = evt.target;
			if(target.tagName=='INPUT'){//如果在输入状态中
				return;
			}
			if(Rect.selected){
				Rect.selected.remove();
			}
		}
	},
	focus:function(){
		Rect.selected && Rect.selected.blur();
		if(this.editable){
			this.elem.className='editableRectActive';
		}else{
			if(this.elem.className == 'rect'){
				this.elem.className+=' rectHover';
			}
		}
		Rect.selected=this;
	},
	blur:function(){
		if(this.editable){
			this.elem.className='editableRect';
		}else{
			if(/rectCur/.test(this.elem.className)){
				this.elem.className='rect rectCur';	
			}else{
				this.elem.className='rect';	
			}
		}
		Rect.selected=null;
	},
	tipOn:function(evt){
		var evt=$.event.fix(evt);
		var tiper=this.tiper=$('#paper_tip').get(0);
		if(!tiper){
			tiper=this.tiper=document.createElement('div');
			tiper.id='paper_tip';
			tiper.style.position='absolute';
			document.getElementsByTagName('BODY')[0].appendChild(tiper);
		}
		tiper.style.display='block';
		tiper.innerHTML=JSONA.evaluate(this.jsonDataEl.value).title;
		this.tipMove(evt);
	},
	tipMove:function(evt){
		if(!this.tiper)return;
		var evt=$.event.fix(evt);
		var evtPos=getEvtPos(evt);
		this.tiper.style.left=evtPos.x+10+"px";
		this.tiper.style.top=evtPos.y+"px";
	},
	tipOff:function(){
		$('#paper_tip').get(0).style.display="none";
	}
}

/**** 拖拽类 ****/
var Drag=function(drag, options){
	this.init(drag, options)
}
Drag.prototype = {
  //拖放对象
  init: function(drag, options) {
	this.Drag = drag;//拖放对象
	this._x = this._y = 0;//记录鼠标相对拖放对象的位置
	this._marginLeft = this._marginTop = 0;//记录margin
	var me=this;
	this._fM = function(evt){me.Move(evt)};
	this._fS = function(){me.Stop()};

	this.SetOptions(options);

	this.Limit = !!this.options.Limit;
	this.mxLeft = parseInt(this.options.mxLeft);
	this.mxRight = parseInt(this.options.mxRight);
	this.mxTop = parseInt(this.options.mxTop);
	this.mxBottom = parseInt(this.options.mxBottom);

	this.LockX = !!this.options.LockX;
	this.LockY = !!this.options.LockY;
	this.Lock = !!this.options.Lock;

	this.onStart = this.options.onStart;
	this.onMove = this.options.onMove;
	this.onStop = this.options.onStop;
	this._Handle = this.options.Handle || this.Drag;
	this._mxContainer = this.options.mxContainer || null;

	this.Drag.style.position = "absolute";
	//透明
	if($.browser.msie && !!this.options.Transparent){
		//填充拖放对象
		with(this._Handle.appendChild(document.createElement("div")).style){
			width = height = "100%"; backgroundColor = "#fff"; filter = "alpha(opacity:0)"; fontSize = 0;
		}
	}
	//修正范围
	this.Repair();
	$.event.add(this._Handle,"mousedown", function(evt){me.Start(evt)});
  },
  //设置默认属性
  SetOptions: function(options) {
	this.options = {//默认值
		Handle:			"",//设置触发对象（不设置则使用拖放对象）
		Limit:			false,//是否设置范围限制(为true时下面参数有用,可以是负数)
		mxLeft:			0,//左边限制
		mxRight:		9999,//右边限制
		mxTop:			0,//上边限制
		mxBottom:		9999,//下边限制
		mxContainer:	"",//指定限制在容器内
		LockX:			false,//是否锁定水平方向拖放
		LockY:			false,//是否锁定垂直方向拖放
		Lock:			false,//是否锁定
		Transparent:	false,//是否透明
		onStart:		function(){},//开始移动时执行
		onMove:			function(){},//移动时执行
		onStop:			function(){}//结束移动时执行
	};
	if(options){
		for (var property in options) {
			this.options[property] = options[property];
		}
	}
  },
  //准备拖动
  Start: function(evt) {
	if(this.Lock){ return; }
	var evt=$.event.fix(evt);
	this.Repair();
	//记录鼠标相对拖放对象的位置
	this._x = evt.clientX - this.Drag.offsetLeft;
	this._y = evt.clientY - this.Drag.offsetTop;
	//记录margin
	this._marginLeft = parseInt(this.Drag.currentStyle.marginLeft) || 0;
	this._marginTop = parseInt(this.Drag.currentStyle.marginTop) || 0;
	//mousemove时移动 mouseup时停止
	$.event.add(document,"mousemove", this._fM);
	$.event.add(document,"mouseup", this._fS);
	if($.browser.msie){
		//焦点丢失
		$.event.add(this._Handle,"losecapture", this._fS);
		//设置鼠标捕获
		this._Handle.setCapture();
	}else{
		//焦点丢失
		$.event.add(window,"blur", this._fS);
		//阻止默认动作
		evt.preventDefault();
	};
	//附加程序
	this.onStart();
  },
  //修正范围
  Repair: function() {
	if(this.Limit){
		//修正错误范围参数
		this.mxRight = Math.max(this.mxRight, this.mxLeft + this.Drag.offsetWidth);
		this.mxBottom = Math.max(this.mxBottom, this.mxTop + this.Drag.offsetHeight);
		//如果有容器必须设置position为relative或absolute来相对或绝对定位，并在获取offset之前设置
		!this._mxContainer || this._mxContainer.currentStyle.position == "relative" || this._mxContainer.currentStyle.position == "absolute" || (this._mxContainer.style.position = "relative");
	}
  },
  //拖动
  Move: function(evt) {
	//判断是否锁定
	if(this.Lock){ this.Stop(); return; };
	var evt=$.event.fix(evt);
	//清除选择
	window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
	//设置移动参数
	var iLeft = evt.clientX - this._x, iTop = evt.clientY - this._y;
	//设置范围限制
	if(this.Limit){
		//设置范围参数
		var mxLeft = this.mxLeft, mxRight = this.mxRight, mxTop = this.mxTop, mxBottom = this.mxBottom;
		//如果设置了容器，再修正范围参数
		if(!!this._mxContainer){
			mxLeft = Math.max(mxLeft, 0);
			mxTop = Math.max(mxTop, 0);
			mxRight = Math.min(mxRight, this._mxContainer.clientWidth);
			mxBottom = Math.min(mxBottom, this._mxContainer.clientHeight);
		};
		//修正移动参数
		iLeft = Math.max(Math.min(iLeft, mxRight - this.Drag.offsetWidth), mxLeft);
		iTop = Math.max(Math.min(iTop, mxBottom - this.Drag.offsetHeight), mxTop);
	}
	//设置位置，并修正margin
	if(!this.LockX){ this.Drag.style.left = iLeft - this._marginLeft + "px"; }
	if(!this.LockY){ this.Drag.style.top = iTop - this._marginTop + "px"; }
	//附加程序
	this.onMove();
  },
  //停止拖动
  Stop: function() {
	//移除事件
	$.event.remove(document,"mousemove", this._fM);
	$.event.remove(document,"mouseup",  this._fS);
	if($.browser.msie){
		$.event.remove(this._Handle,"losecapture", this._fS);
		this._Handle.releaseCapture();
	}else{
		$.event.remove(window,"blur", this._fS);
	};
	//附加程序
	this.onStop();
  }
};