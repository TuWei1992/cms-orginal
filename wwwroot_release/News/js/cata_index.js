// JavaScript Document


/* 军事专题选项卡切换 */
$(function(){
	$('.br_tabs li a').hover(function(){
		var index=$(this).index();
		$(this).addClass('current').siblings().removeClass('current');
		$(".war_subtabcon").eq(index).show().siblings().hide();
	});		   
});

/* 精彩视频选项卡切换 */
$(function(){
	$('.zbrtab li a').hover(function(){
		var index=$(this).index();
		$(this).addClass('current').siblings().removeClass('current');
		$(".zbrcon").eq(index).show().siblings().hide();
	});		   
});

/* 航空航天要闻图片播放器 */
$('#change_s .a_bigImg').soChange({
		thumbObj:'#change_s .ul_change_s1 li',//导航图标，选择器直接指向图标对象
		slideTime:5,
		botPrev:'#change_s .a_last',
		botNext:'#change_s .a_next'
});

/* 航空摄影图片播放器 */
$('#change_rs .a_bigImg').soChange({
		thumbObj:'#change_rs .ul_change_rs1 li',//导航图标，选择器直接指向图标对象
		//slideTime:5,
		botPrev:'#change_rs .a_last',
		botNext:'#change_rs .a_next'
});

/* 航空摄影选项卡 */
$('#soci_moodplayer div').soChange({
	thumbObj:'#soci_moodplayer h3',
	slideTime:0,
	//thumbOverEvent:false,
	autoChange:true//自动切换为 false，默认为true
});

/* 军事热图播放器 */
$(document).ready(function(){
	var lb = $("#limit-buy"),
		lb_cur = 1,
		lbp_w = lb.find(".products").width();
		lb_timer = null;
	t = 1;
	function showLimitBuyProducts(){
		if(lb_cur < 1){
			lb_cur = 4;
		} else if(lb_cur > 4){
			lb_cur = 1;
		}
		$("#J-lbcp").html(lb_cur);
		var products = $("#limit-buy .products").hide().eq(lb_cur-1).show(),
			ta = products.find("textarea");
			
		if(ta.length){
			products.html(ta.val());
		}
	}
	lb_timer = setInterval(function(){
		lb_cur++;
		showLimitBuyProducts();
	}, 4000);
	
	$("#J-lbn .prev, #J-lb .btn-prev").click(function(){
		lb_cur--;
		showLimitBuyProducts();
	});
	$("#J-lbn .next, #J-lb .btn-next").click(function(){
		lb_cur++;
		showLimitBuyProducts();
	});
	$("#J-lb").hover(function(){
			clearInterval(lb_timer);
			lb_timer = null;
			$("#J-lb .btn-prev, #J-lb .btn-next").show();
		}, function(){
			lb_timer = setInterval(function(){
				lb_cur++;
				showLimitBuyProducts();
			}, 10000);
			$("#J-lb .btn-prev, #J-lb .btn-next").hide();
	});
});