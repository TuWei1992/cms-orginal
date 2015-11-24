// JavaScript Document

/* 首页图片播放器 */
$('#change_2 .a_bigImg').soChange({
		thumbObj:'#change_2 .ul_change_a1 li',//导航图标，选择器直接指向图标对象
		//slideTime:5,
		botPrev:'#change_2 .a_last',
		botNext:'#change_2 .a_next'
});

/* blockboxB 左侧折叠展开 */
jQuery(function(jq){
	var cls = "nowCol";
	var rd = jq('#columnID');
	var divs = rd.find('.column');
	var curEl = null,
	  dur = 300,
	  h = {min:61, max:165};
		divs.mouseover(function(el){
			el = jq(this);
	if (el === curEl) return;
	curEl = el;
			divs.not(el).removeClass(cls).find('> .conB').stop().animate({'height':h.min},dur,'linear');
			el.find('> .conB').stop().animate({'height':h.max},dur,'linear');
			el.addClass(cls);
		});
	divs.find('> .conB').css({'height':h.min}).eq(0).css({'height':h.max});
		curEl = divs.eq(0).addClass(cls);
});

/* 焦点专题选项卡切换 */
$(function(){
	$('.br_tabs li a').hover(function(){
		var index=$(this).index();
		$(this).addClass('current').siblings().removeClass('current');
		$(".br_con").eq(index).show().siblings().hide();
	});		   
});

/* 视频选项卡切换 */
$(function(){
	$('.zbrtab li').hover(function(){
		var index=$(this).index();
		$(this).addClass('current').siblings().removeClass('current');
		$(".zbrcon").eq(index).show().siblings().hide();
	});		   
});

/* 图刊大观图片播放器 */
$('#change_s .a_bigImg').soChange({
		thumbObj:'#change_s .ul_change_s1 li',//导航图标，选择器直接指向图标对象
		slideTime:5,
		botPrev:'#change_s .a_last',
		botNext:'#change_s .a_next'
});

/* 图说军事图片播放器 */
$('#change_rs .a_bigImg').soChange({
		thumbObj:'#change_rs .ul_change_rs1 li',//导航图标，选择器直接指向图标对象
		//slideTime:5,
		botPrev:'#change_rs .a_last',
		botNext:'#change_rs .a_next'
});

/* 社会心情排行榜 */
$('#soci_moodplayer div').soChange({
	thumbObj:'#soci_moodplayer h3',
	slideTime:0,
	//thumbOverEvent:false,
	autoChange:true//自动切换为 false，默认为true
});

/* 理财生活图片播放器 */
$('#change_financial .a_bigImg').soChange({
		thumbObj:'#change_financial .ul_change_s1 li',//导航图标，选择器直接指向图标对象
		slideTime:5,
		botPrev:'#change_financial .a_last',
		botNext:'#change_financial .a_next'
});

/* 天下财经图片播放器 */
$('#change_rfinancial .a_bigImg').soChange({
		thumbObj:'#change_rfinancial .ul_change_rs1 li',//导航图标，选择器直接指向图标对象
		//slideTime:5,
		botPrev:'#change_rfinancial .a_last',
		botNext:'#change_rfinancial .a_next'
});

/* 科技数码排行榜 */
$('#financial_moodplayer div').soChange({
	thumbObj:'#financial_moodplayer h3',
	slideTime:0,
	//thumbOverEvent:false,
	autoChange:true//自动切换为 false，默认为true
});

/* 明星美图图片播放器 */
$('#change_espn .a_bigImg').soChange({
		thumbObj:'#change_espn .ul_change_s1 li',//导航图标，选择器直接指向图标对象
		slideTime:5,
		botPrev:'#change_espn .a_last',
		botNext:'#change_espn .a_next'
});

/* 资讯选项卡切换 */
$(function(){
	$('.br_lifeinfotab li').hover(function(){
		var index=$(this).index();
		$(this).addClass('current').siblings().removeClass('current');
		$(".br_lifeinfocon .br_videocon").eq(index).show().siblings().hide();
	});		   
});

/* 生活资讯翻牌 */
$(function(){
	$(".blockboxF_left ul li").hover(function(){
         $(this).children("a").stop(false,true).animate({top:0},500);
		 },function(){ 
         $(this).children("a").stop(false,true).animate({top:250},500);
		 });
});