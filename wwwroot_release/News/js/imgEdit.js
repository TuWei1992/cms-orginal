imgEditStaet={
   src:'',
   apply:{
      top:0,left:0,width:0,height:0
   },
   scale:{
      x:1,
      y:1
   },
   waterMake:{
      imgSrc:'',
      left:100,
      top:100
  },
   text:{
      text:'title',
      fontSize:'14',
      left:20,
      top:20,
      color:'#f00',
      fontFamily:'宋体',
      fontWeight:'',
	  textdecoration:'none',
	  fontstyle:'normal',
	  opicty:'1'
   }
}

var imgW;
var imgH;	

//添加文字的一些属性
var fontText={font:'',size:'',fcolor:'',transparency:'',fbold:'',Weight:'',Italic:'',underline:'',text:'',top:'',left:''}

var _move=false;//移动标记

function docClick(){
	var clickImg=0;	
	$(document).click(function(e){
		var evt = e ? e : (window.event ? window.event : null); //此方法为了在firefox中的兼容
		var node = evt.srcElement ? evt.srcElement : evt.target; //evt.target在火狐上才能识别用的
		if($(node).attr("id")=="photo"){
			if(clickImg==0){
				_move=false;
				var left=e.pageX-parseInt($("#photo"). offset().left);
				var top=e.pageY-parseInt($("#photo").offset().top);	
				fontText.top=top;
				fontText.left=left;
				addText();	
				clickImg=1;
				move();
			}else if(clickImg==1){
				if(!!$(".TextBox").length){
					if($(".TextBox").text().trim() == "" ){
						$("#imgBox div").remove(".TextBox");
						$("#photo").css("cursor","text");
						docClick();
						return;
					}
				}
				$(".TextBox").attr("contentEditable","false");
				$(".TextBox").css("cursor","move");	
				$(".TextBox").blur();	
				}
			}else{
				if( $(node).attr("class") != "TextBox" ){
					if(!!$(".TextBox").length){
						if($(".TextBox").text().trim() == "" ){
							$("#imgBox div").remove(".TextBox");
							$("#photo").css("cursor","text");
							docClick();
							return;
						}
					}
					$(".TextBox").attr("contentEditable","false");
					$(".TextBox").css("cursor","move");	
					$(".TextBox").blur();	
				}
			}
				
		})
}
function setOptions(){
	fontText.font=$("#font > option:selected").val();
	fontText.fcolor=$("#color > option:selected").val();
	fontText.transparency=$("#transportant > option:selected").val();
	fontText.size=$("#fontsize > option:selected").val();
	if($("#bold").attr("checked") == "checked"){
		fontText.fbold="bold";
	}else{
		fontText.fbold="normal";
	}
	if($("#Italic").attr("checked") == "checked"){
		fontText.Italic="italic";
	}else{
		fontText.Italic="normal";
	}
	if($("#underline").attr("checked") == "checked"){
		fontText.underline="underline";
	}else{
		fontText.underline="none";
	}
	var imgBox=$("#imgBox");
	if(imgBox){
	$("#imgBox .TextBox").css({"color":fontText.fcolor,"font-size":fontText.size+"px","font-style":fontText.Italic,"font-weight":fontText.fbold,"opacity":fontText.transparency,"text-decoration":fontText.underline,"font-family":fontText.font,"top":fontText.top+"px","left":fontText.left+"px"});
	}
}
function addText(){
	var newImgW =$("#photo").width(); //获得新图片的宽度
	setOptions();
	var imgBox=$("#imgBox");
	if(imgBox){
		if(!$(".TextBox").length){
		   imgBox.css({"position":"relative","width":newImgW,"margin":"0 auto"});
		   imgBox.append("<div class='TextBox' id='TextBox'>&nbsp;</div>");
		   $(".TextBox").attr("contentEditable","true");
		   $(".TextBox").css("cursor","text");
		   $(".TextBox").focus();
		   $(".TextBox").keyup(function(){
				fontText.text=$(this).text();
				imgEditStaet.text.text=fontText.text;
		  })
		}
		
		$("#imgBox .TextBox").css({"color":fontText.fcolor,"font-size":fontText.size+"px","font-style":fontText.Italic,"font-weight":fontText.fbold,"opacity":fontText.transparency,"text-decoration":fontText.underline,"font-family":fontText.font,"top":fontText.top+"px","left":fontText.left+"px"});
		$("#photo").css("cursor","default");
		
	}

	imgEditStaet.text.color=fontText.fcolor;
	imgEditStaet.text.fontFamily=fontText.font;
	imgEditStaet.text.fontSize=fontText.size;
	imgEditStaet.text.fontstyle=fontText.Italic;
	imgEditStaet.text.fontWeight=fontText.fbold;
	imgEditStaet.text.opicty=fontText.transparency;
	imgEditStaet.text.textdecoration=fontText.underline;
	imgEditStaet.text.text=fontText.text;
	imgEditStaet.text.top=fontText.top;
	imgEditStaet.text.left=fontText.left;
}


function move(){
	var _x,_y;//鼠标离控件左上角的相对位置
	$(".TextBox").click(function(){	
		}).mousedown(function(e){
			if($(".TextBox").attr("contentEditable")== "false"){
				_move=true;
				e.preventDefault(); //阻止默认事件
			}
		_x=e.pageX-parseInt($(".TextBox").css("left"));
		_y=e.pageY-parseInt($(".TextBox").css("top"));
	});
	
	$(".TextBox").dblclick(function(){
		_move=false;
		$(".TextBox").attr("contentEditable","true");
		$(".TextBox").css("cursor","text");
	    $(".TextBox").focus();
	});
	
	$(document).mousemove(function(e){
		if(_move){
			e.preventDefault();
			var x=e.pageX-_x;//移动时根据鼠标位置计算控件左上角的绝对位置
			var y=e.pageY-_y;
			var leftwidth=$("#photo").width()-$(".TextBox").width();
			var topwidth=$("#photo").height()-$(".TextBox").height();
			if(x<0){
				x=0;
			}else if( x > leftwidth ){
				x=leftwidth;
			}
			if(y<0){
				y=0;
			}else if(y > topwidth){
				y=topwidth;
			}
			$(".TextBox").css({top:y,left:x});//控件新位置
			fontText.left=x;
			fontText.top=y;
			imgEditStaet.text.top=y;
			imgEditStaet.text.left=x;
		}
		}).mouseup(function(){
			_move=false;
	  });	
		

}

//水印图片的移动
function moveWaterImg(){
	var _x,_y;//鼠标离控件左上角的相对位置
	var moveW=false;
	$(".warterImg").click(function(){	
		moveW=false;
		}).mousedown(function(e){
		moveW=true;
		_x=e.pageX-parseInt($(".warterImg").css("left"));
		_y=e.pageY-parseInt($(".warterImg").css("top"));
		e.preventDefault();
	});
	
	$(document).mousemove(function(e){
		if(moveW){
			e.preventDefault();
			var x=e.pageX-_x;//移动时根据鼠标位置计算控件左上角的绝对位置
			var y=e.pageY-_y;
			var leftwidth=$("#photo").width()-$(".warterImg").width();
			var topwidth=$("#photo").height()-$(".warterImg").height();
			if(x<0){
				x=0;
			}else if( x > leftwidth ){
				x=leftwidth;
			}
			if(y<0){
				y=0;
			}else if(y > topwidth){
				y=topwidth;
			}
			$(".warterImg").css({top:y,left:x});//控件新位置
			imgEditStaet.waterMake.top=y;
			imgEditStaet.waterMake.left=x;
		}
		}).mouseup(function(){
			moveW=false;
	  });			
}
function ImgAreaSelect(){
	this.maxW;
	this.maxH;
	this.imgW;
	this.imgH;
	this.selectW;
	this.selectH;
	this.handles=true;
	this.x1=0;
	this.y1=0;
	this.x2;
	this.y2;
	this.aspectRatio="";
}

ImgArea=new ImgAreaSelect();

function ImgSelect(){
	$("#Imgwidth").val("");
	$("#Imgheight").val("");
	var currentimgW=$("#photo").width();
	var currentimgH=$("#photo").height();
	ImgArea.imgW=currentimgW;
	ImgArea.imgH=currentimgH;
	ImgArea.maxW=currentimgW;
	ImgArea.maxH=currentimgH;
	ImgArea.selectW=currentimgW;
	ImgArea.selectH=currentimgH;
	setaspectRatio();	
}
	
//判断约束比例是否勾选，如果勾选得到预设比例的值
function setaspectRatio(){
	var ischeck=$("#ischeck").attr("checked");
	var aspectRatio=$("#proportion input:checked").val();
	if(ischeck==""||ischeck==null){
		ImgArea.aspectRatio="";
	}
	if( ischeck=="checked" && aspectRatio){
	ImgArea.aspectRatio=aspectRatio;
	}	
	setSelect();
}

function setSelect(){
	
return	$('#photo').imgAreaSelect({x1:"",y1:"",x2:"",y2:"", maxWidth:ImgArea.maxW, maxHeight: ImgArea.maxH, handles: ImgArea.handles,aspectRatio:ImgArea.aspectRatio,instance:ImgArea.handles,onSelectChange: setoptions});	
	
}

//修改选区结束后重新设定对应值
function setoptions(img, selection){
	$("div.imgareaselect-outer").css({"background":"#000000","opacity":0.6});
	ImgArea.selectW=selection.width;
	ImgArea.selectH=selection.height;
	ImgArea.x1=selection.x1;
	ImgArea.y1=selection.y1;
	ImgArea.x2=selection.x2;
	ImgArea.y2=selection.y2;
	$("#Imgwidth").val(ImgArea.selectW);
	$("#Imgheight").val(ImgArea.selectH);
	
	//设定选框区域与原图上、下、左、右的距离
	imgEditStaet.apply.left=ImgArea.x1;
	imgEditStaet.apply.top=ImgArea.y1;
	imgEditStaet.apply.width=ImgArea.selectW;
	imgEditStaet.apply.height=ImgArea.selectH;
}

function setSelectblock(){
	ImgArea.selectW=$("#Imgwidth").val(); //重新获得输入框内的宽度值
	ImgArea.selectH=$("#Imgheight").val();//重新获得输入框内的高度值
	var ias=$('#photo').data('imgAreaSelect');
	if(ias.getSelection().width > 0){
	
		var w=parseInt(ImgArea.x1)+parseInt(ImgArea.selectW); //需要的宽度值
		var h=parseInt(ImgArea.y1)+parseInt(ImgArea.selectH); //需要的高度值
		
		ImgArea.x2=w;
		ImgArea.y2=h;
		if(w > ImgArea.maxW){
			var x_width=ImgArea.maxW-ImgArea.x1; //右侧可容纳宽度
			var x_sideWidth;      //需要增加的宽度
			if( ImgArea.selectW > x_width){
				x_sideWidth=w-ImgArea.maxW; 
				ImgArea.x2=ImgArea.imgW;
				if(x_sideWidth > ImgArea.x1){
					ImgArea.x1=0;
				}else{
					ImgArea.x1=ImgArea.x1-x_sideWidth;
				}
			}
		}else if(h > ImgArea.maxH){
			var y_width=ImgArea.maxH-ImgArea.y1; //下面可容纳高度
			var y_sideWidth;      //需要增加的高度	
		
			if( ImgArea.selectH > y_width){
				y_sideWidth=h-ImgArea.maxH;
				ImgArea.y2=ImgArea.imgH;
				if(y_sideWidth > ImgArea.y1){
					ImgArea.y1=0;
				}else{
					ImgArea.y1=ImgArea.y1-y_sideWidth;
				}
			}
		}
		imgEditStaet.apply.left=ImgArea.x1;
		imgEditStaet.apply.top=ImgArea.y1;
		imgEditStaet.apply.width=ImgArea.selectW;
		imgEditStaet.apply.height=ImgArea.selectH;
		ias.setSelection(ImgArea.x1,ImgArea.y1,ImgArea.x2,ImgArea.y2); //重新设定选区范围
		ias.update();
	}else{
		if($("#Imgwidth").val()=="" || $("#Imgwidth").val()==null){
			return;
		}
		if($("#Imgheight").val()=="" || $("#Imgheight").val()==null){
			return;
		}
		$("div.imgareaselect-outer").css({"background":"#000000","opacity":0.6});	
		$("#photo").imgAreaSelect({x1:0,y1:0,x2:$("#Imgwidth").val(),y2:$("#Imgheight").val()}); //重新设定选区范围
	}
	
}



function apply(){
	$("div.imgareaselect-outer").css({"background":"#ffffff","opacity":1});	
	var ias=setSelect();
	ias.setOptions({movable:false,resizable:false,handles:false});
	ias.update();
}

waterMarkOk=false;
function applyWaterMark(){
	waterMarkOk=true;
}
appTextOk=false;
function applyText(){
	appTextOk=true;
}

$(function() { 
		   	
	// 点击菜单对应左侧内容的显示
	 $("#menus > a").click(function(){
		
		var num=$(this).prevAll().length;
		if(num!=0 && num>0){
			var ias=$('#photo').data('imgAreaSelect');
			$('#photo').width(ImgArea.imgW);
			if(ias.getSelection().width > 0 ){
				alert("是否进行裁剪");
				ias.setOptions({"movable":false})
			}
			$(".imgareaselect-selection").parent().hide();
			$(".imgareaselect-outer").hide();
			ias.setOptions({"disable":true});
			ias.update();	
			if(num==1){
				waterMarkOk=false;
				if( !appTextOk && (!!$(".TextBox").length)){
					alert("是否添加文字");
				}
				$("#photo").unbind("click"); //注销事件
				$("#photo").css("cursor","default");
				if(!!$("#imgBox > .TextBox").length){
					$("#imgBox div").remove(".TextBox");
				}
			}
		    if(num==2){
				appTextOk=false;
				if( !waterMarkOk && (!!$(".warterImg").length)){
					alert("是否添加水印");
		  		}
				if(!!$("#imgBox > .warterImg").length){
					$("#imgBox img").remove(".warterImg");
					$(".waterImgWrap > img.current").removeClass("current");
				}
				
				if(!!$("#imgBox > .TextBox").length){
					$("#photo").css("cursor","default");	
				}else{
					$("#photo").css("cursor","text");	
					docClick();
				}
			}
		}else{
			if( !waterMarkOk && (!!$(".warterImg").length)){
				alert("是否添加水印");
			}
			if( !appTextOk && (!!$(".TextBox").length)){
				alert("是否添加文字");
			}
			$("#photo").unbind("click"); //注销事件
			$("#photo").css("cursor","default");
			 ss.value=50;
			 setScale();
			 $(".z-slider-thumb").css("left",ss.value+"px");
			$(".imgareaselect-selection").parent().show();	
			$(".imgareaselect-outer").show();
			
			if(!!$("#imgBox > .TextBox").length){
				$("#imgBox div").remove(".TextBox");
			}
			if(!!$("#imgBox > .warterImg").length){
				$("#imgBox img").remove(".warterImg");
				$(".waterImgWrap > img.current").removeClass("current");
			}
			$("div.imgareaselect-outer").css({"background":"#000000","opacity":0});				
			ImgSelect();
			var ias=$('#photo').data('imgAreaSelect');
			ias.setOptions({movable:true,resizable:true,handles:true});
			ias.setOptions({"enable":true});
			ias.update();
		}
		
		$(this).addClass("current").siblings("a").removeClass("current");
		$("#leftCon > div").eq(num).show().siblings("div").hide();	
		
	  })
	 
	 $("#proportion input,#ischeck").click(function(){
			var ischeck=$("#ischeck").attr("checked");
			var aspectRatio=$("#proportion input:checked").val();
			if(ischeck==""||ischeck==null){
				ImgArea.aspectRatio="";
			}
			if( ischeck=="checked" && aspectRatio){
			ImgArea.aspectRatio=aspectRatio;
			}	//判断设置预设比例	
			
			$('#photo').imgAreaSelect({aspectRatio:ImgArea.aspectRatio});	
	 })
	 
	 $("#Imgwidth,#Imgheight").keyup(function(){
		setSelectblock();	//计算设置选框区域							
	 })

	 
	 $("#font,#color,#fontsize,#transportant").click(function(){
		setOptions();	 //选择对应属性后及时更新样式			  
	 })
	 
	 $("#bold,#Italic,#underline").change(function(){
		setOptions();	 //选择对应属性后及时更新样式			  
	 })

	//放大图片
	 $("#larger").click(function(){						 
		if( ss.value==100){
			return;
		}
		 ss.value=ss.value+1;
		 var nowVlaue=ss.value;
		 setScale();
		 $(".z-slider-thumb").css("left",ss.value+"px");	
	 })
	 
	 //放大图片
	 $("#narrow").click(function(){						 								 
		if( ss.value==0){
			return;
		}
		
		 ss.value=ss.value-1;
		 var nowVlaue=ss.value;
		 setScale();
		 $(".z-slider-thumb").css("left",ss.value+"px");	
	 })
	 
	 //查看实际大小
	  $("#oldimgWid").click(function(){						 								 
		 ss.value=50;
		 setScale();
		 $(".z-slider-thumb").css("left",ss.value+"px");	
	 })
	 
	 $(".waterImgWrap > img").click(function(){
		var newImgW =$("#photo").width(); //获得新图片的宽度
		var wartersrc=$(this).attr("src"); //水印图片的图片地址
		$(this).addClass("current").siblings("img").removeClass("current");
		var warterwid=$(this).width();
		var warterhg=$(this).height();
		imgEditStaet.waterMake.imgSrc=wartersrc;
		if(!!$(".warterImg").length){
			$(".warterImg").attr("src",wartersrc);	
			$(".warterImg").attr("width",warterwid);	
			$(".warterImg").attr("height",warterhg);	
		}else{
			
			$("#imgBox").css({"position":"relative","width":newImgW,"margin":"0 auto"});
			$("#imgBox").append("<img  class='warterImg' src='"+wartersrc+"' />");
		}
		moveWaterImg();
	 })
	 
})
$(window).load(function(){
	if($("#photo")){
		imgW=$("#photo").width();
		imgH=$("#photo").height();
//		$("#Imgwidth").val(imgW);
//	    $("#Imgheight").val(imgH);
		imgEditStaet.src=$("#photo").attr("src");
		imgEditStaet.apply.width=imgW;
	    imgEditStaet.apply.height=imgH;
		$("#sliderCon label#scaleImg").text(imgW+" * "+imgH);
		$("#sliderCon label#scale").text("100%");
	}
	
 
	
	ImgSelect();
	$("div.imgareaselect-outer").css({"background":"#000000","opacity":0});
	//ss=new Slider().render($G("sliderContent"));
//	 $(".z-slider-thumb").css("left","50px");
//	 ss.value=50;
//	 ss.on('slide',function(){
//	   setScale();
//	 });
	 	 
})

function setScale(){
	   var v=(Math.round(ss.value))/50;
	   $('#photo').width(Math.round(imgW*v));
	   $("#sliderCon label#scale").text(Math.round(v*100)+"%");
	   
	   var imgWid=$('#photo').width();
	   var imgHid=$('#photo').height();
	  $("#sliderCon label#scaleImg").text(imgWid+"*"+imgHid);	
	   var ias=$('#photo').data('imgAreaSelect');
		ias.setOptions({
					 imageHeight:$('#photo').height(),
					 imageWidth:$('#photo').width(),
					 maxHeight:$('#photo').height(),
					 maxWidth:$('#photo').width()
					 });	   
	   ias.update();
	   ImgArea.maxW=$('#photo').width();
	   ImgArea.maxH=$('#photo').height();
	   imgEditStaet.scale.x=v;
	   imgEditStaet.scale.y=v;	
}

//取消裁剪
function cancel(){	
	ImgSelect();
	$("div.imgareaselect-outer").css({"background":"#000000","opacity":0});	
}

//取消水印应用
function cancelWaterMark(){
	if(!!$("#imgBox > .warterImg").length){
		$("#imgBox img").remove(".warterImg");
	}
	
	if($(".waterImgWrap > img.current").length > 0){
	  $(".waterImgWrap > img.current").removeClass("current");
	}
}

//取消文字应用
function cancelText(){
	if(!!$("#imgBox > .TextBox").length){
		$("#imgBox div").remove(".TextBox");
	}
	$("#photo").css("cursor","text");
	docClick();
}