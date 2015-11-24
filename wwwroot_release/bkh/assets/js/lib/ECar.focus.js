/*
 * Copyright (C), 2013-2013, 上海汽车集团股份有限公司
 * Author:   方彬
 * Date:     2013-11-19
 * Description: 焦点图功能。品牌详情页的焦点图功能。
 * History: //修改记录
 * <author>		<time>			<desc>
 * 方彬          2014-05-09       修复自动轮播BUG
 */

;(function($, ECar) {
;var ECar = ECar||{};
ECar.focus = function(options){

	// 取$block，并设置延迟时间
	var $block = $('.c-focus'),
		$slides = $('.c-focus-list', $block),
		_width = $block.width(),
		$li = $('li', $slides),
		_animateSpeed = 990,
		//加入计时器，轮播时间，控制开关
		timer, _showSpeed = 2000, _stop = false;

	//生成li
	var _str = '';
	for(var i=0, j=$li.length;i<j;i++){
		var imgObj=$li.eq(i).find('img'),imgWidth=imgObj.width()-_width>0?imgObj.width()-_width:0;
		_str += '<li class="playerControl_' + (i+1) + '"></li>';
		imgObj.css('margin-left',-imgWidth/2);
	}

	$slides.width(_width*$li.length);
	$li.width(_width);
	
	//return;
	window.onresize=function(){
		location.reload();
	}
	

    //生成ul>li
    var $playerControl = $('<ul class="playerControl"></ul>').html(_str).appendTo($slides.parent()).css('left', function(){
        //.playerControl定位
        return (_width - $(this).width()) / 2;
    });

    //生成ul>li，li绑定mouseover事件
    $('<ul class="playerControl"></ul>').find('li').mouseover(function(){
                var $this = $(this);
                $this.addClass('current').siblings('.current').removeClass('current');
                //找到相应序号
                $slides.stop().animate({
                    left: _width * $this.index() * -1
                }, _animateSpeed);

                return false;
    }).eq(0).mouseover();

	//li绑定click事件
	var $playerControlLi = $playerControl.find('li').click(function(){
		var $this = $(this);
		$this.addClass('current').siblings('.current').removeClass('current');
		clearTimeout(timer);
		//找到相应序号
		$slides.stop().animate({
			left: _width * $this.index() * -1
		}, _animateSpeed, function(){
			//轮播到正确位置后，依照判断来启动计时器
			if(!_stop) timer = setTimeout(move, _showSpeed);
		});
 
		return false;
	}).eq(0).click().end();
 
	//hover事件
	$block.hover(function(){
		//关闭开关及清除计时器
		_stop = true;
		clearTimeout(timer);
	}, function(){
		//移出 $block时
		//开启开关及清除计时器
		_stop = false;
		timer = setTimeout(move, _showSpeed);
	});
 
	//轮播
	function move(){
		var _index = $('.playerControl .current').index();
		$playerControlLi.eq((_index + 1) % $playerControlLi.length).click();
	}
};
})(jQuery, window.ECar||(window.ECar={}));
