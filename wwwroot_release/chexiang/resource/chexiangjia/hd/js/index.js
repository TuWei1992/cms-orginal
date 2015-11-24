function initCityFn(){
	var obj = $("#js_dealer_city");
	obj.click(function(){
		if( !obj.hasClass("dealer-city-sel") ){ //show
			obj.addClass("dealer-city-sel");
		}else{
			obj.removeClass("dealer-city-sel");
		}
	});
	$(document).bind("click",function(e) {
		var target = $(e.target);
		var oCn = $("#js_dealer_city");		
		if (target.closest("#js_dealer_city").length == 0) {
			obj.removeClass("dealer-city-sel");
		}
	});
}
var minWidth = 1570;
function setBanner(){
	if($(window).width() < minWidth){
		$(".side_wrapper").hide();
	}else{
		$(".side_wrapper").show();	
	}
}

var timeout1 = 1200; 
var timeout2 = 3000;  

var iTotalNum = 6;
var T2;	
var iCurNum = 0;

var oDiv,oLis;

function change_content(){
	clearTimeout(T2);
	
	var oDiv = $("#js_scroll_cn .m-c");
	var oLis = $("#js_scroll_pages .m-t");


 	for (var i=0; i<iTotalNum; i++){	 		
		$(oLis[i]).removeClass("cur");
	  	$(oDiv[i]).hide();
  	}		
  	$(oLis[iCurNum]).addClass("cur");
	$(oDiv[iCurNum]).fadeIn(timeout1);
	iCurNum++;
	if(iCurNum > iTotalNum-1){
		iCurNum = 0;
	}
  	T2 = setTimeout('change_content()', timeout2);

}

function initnavTab(){
	var showObj = $("#js_scroll_cn .m-c");  
	var tatObj =  $("#js_scroll_pages .m-t");  
	var btnNext = $("#js_scroll_cn .next");
	var btnPrev = $("#js_scroll_cn .prev");
	
	tatObj.each(function(i){
		$(this).hover(
			function(){
				clearTimeout(T2);
				tatObj.each(function(j,domEle){
					if(i==j){
						$(domEle).addClass("cur");
						iCurNum=i+1;
						if(iCurNum>iTotalNum-1){
							iCurNum=0;
						}
					}else{
						$(domEle).removeClass("cur");
					}
				});
				showObj.each(function(k,domEle){
					if(i==k){
						$(domEle).fadeIn(timeout1);
					}else{
						$(domEle).hide();
					}
				});
			},function(){
				T2=setTimeout('change_content()', timeout2);
			}
		);
	});

	btnNext.hover(
		function(){
			clearTimeout(T2);
		},
		function(){
			T2 = setTimeout('change_content()', timeout2);
		}
	).click(function(){
		clearTimeout(T2);
		for (var i=0; i<iTotalNum; i++){	 		
			$(tatObj[i]).removeClass("cur");
		  	$(showObj[i]).hide();
	  	}		
	  	$(tatObj[iCurNum]).addClass("cur");
		$(showObj[iCurNum]).fadeIn(timeout1);
		iCurNum += 1;	
		if(iCurNum > iTotalNum-1){
			iCurNum = 0;
		}
		T2 = setTimeout('change_content()', timeout2);
	});
	btnPrev.hover(
		function(){
			clearTimeout(T2);
		},
		function(){
			T2 = setTimeout('change_content()', timeout2);
		}
	).click(function(){
		clearTimeout(T2);		
		for (var i=0; i<iTotalNum; i++){	 		
			$(tatObj[i]).removeClass("cur");
		  	$(showObj[i]).hide();
	  	}		
	  	$(tatObj[iCurNum]).addClass("cur");
		$(showObj[iCurNum]).fadeIn(timeout1);
		iCurNum -= 1 ;
		if(iCurNum < 0){
			iCurNum = iTotalNum-1;
		}
		T2 = setTimeout('change_content()', timeout2);
	});
}


$(window).resize(function() {
	setBanner();
});

$(function(){
	initCityFn();
	setBanner();

	change_content();
	initnavTab();
});