$(function($) {
	$("div.iconhide").click( 
		function () { 
			$(this).parent().prev().toggle();
			$(this).toggleClass("iconshow");
		}
	);

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
		}
	);
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

//打印方法
function printCon(con){
	document.body.innerHTML=con;
	window.doPrinted=true;
	window.print();
}