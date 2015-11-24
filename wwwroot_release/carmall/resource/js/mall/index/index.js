/**
 * Copyright (C), 2015, 上海赛可电子商务有限公司
 * Author:   张卫明
 * Date:     2015-4-28
 * lever:    page
 * Description: 首页js
 */

;(function($,window) {
	
	homePage = {
			
		init:function(){
			
			//图文互动效果
			this.productMove();
			
			//首页搜索切换
			this.searchSwitch();
			
			//大图切换
			var optionStaging= {
				    navNode: '',
				    curNavCls:'',
				    pannelNode: '.home-staging-l-ul li',
				    curPannelCls:'cul',
				    preNode: '.btn-staging-l',
				    nextNode: '.btn-staging-r',
				    duration: 5000,
				    event: 'click',
				    navMaskNode: '',
				    navMaskCssObject:{},
				    curMaskNavCssObject:{},
				    duration:'0'
				};
			var optionCash= {
				    navNode: '',
				    curNavCls:'',
				    pannelNode: '.home-cash-l-ul li',
				    curPannelCls:'cul',
				    preNode: '.btn-cash-l',
				    nextNode: '.btn-cash-r',
				    duration: 5000,
				    event: 'click',
				    navMaskNode: '',
				    navMaskCssObject:{},
				    curMaskNavCssObject:{}
				};
			//ECar.tab(optionCash);
			//ECar.tab(optionStaging);
			
			//加tooltip
			ECar.tooltip();
			
			//全屏轮播
			ECar.fullSlide({'ele':'banner-wrap','showmarkers':false,'showcontrols':true,'autoplay':true,'animspeed':'7000'});
			
			//图片懒加载
			$("img.lazy").lazyload({effect: "fadeIn"});
			
		},
		productMove:function(){
			
			var $provideLi = $('.car-detail','.home-provide-content'),
				$cashLi = $('.car-detail','.home-r-ul'),
				_this = this;
			
			$provideLi.hover(function(){
				$(this).stop().animate({'margin-top':'-5px'},300).addClass('shadow')
		  	},function(){
				$(this).stop().animate({'margin-top':'0px'},300).removeClass('shadow')
			});
			
			$cashLi.hover(function(){
				$cashLi.find('.caption-box').hide();
				$(this).find('.caption-box').fadeIn('100');
		  	},function(){
		  		$(this).find('.caption-box').fadeOut('100');
			});
			
		},
		searchSwitch:function(){
			var $sSel = $('.home-search-text'),$selBox = $('.home-search-box'),
				$sText = $('.search-text',$sSel),
				$sJt=$('.search-jt',$sSel),
				$sList=$('.search-home-list'),
				$sLists=$('a','.search-home-list'),
				$sbox=$('.search-box-long'),
				$sBtn=$(".search-submit-home"),$attendPrice = $('#attendPrice'),$priceUnit = $('.price-unit');
			
			//显示掩藏类型框
			$sSel.on('click',function(){
				if($sList.css('display')=='block'){
					hideList();
				} else {
					$sList.show();
					$sJt.attr('class','search-jt search-jt-sel');
					
				}
			});
			
			//上路价只能输入数字
			$attendPrice.on('focus',function(){
				$priceUnit.show();
			});
			
			$attendPrice.on('keyup',function(){
				this.value=this.value.replace(/\D/g,'');
				
				//是否显示单位
				if(this.value!=''){
					$priceUnit.show();
				} else {
					$priceUnit.hide();
				}
			});
			$attendPrice.on('afterpaste',function(){
				this.value=this.value.replace(/\D/g,'');
				
				//是否显示单位
				if(this.value!=''){
					$priceUnit.show();
				} else {
					$priceUnit.hide();
				}
			});
			
			//鼠标移开类型框 类型框 隐藏
			$selBox.on('mouseleave',function(){
				if($sList.css('display')=='block'){
					hideList();
				}
			});
			
			function hideList(){
				$sList.hide();
				$sJt.attr('class','search-jt');
			}
			
			//选择类型事件
			$sLists.on('click',function(){
				var $tShow = $(this).attr('showSearch');
				$sbox.hide();
				$('#'+$tShow+'Div').show();
				$sText.html($(this).html());
				hideList();
			});
			
			//提交搜索 事件
			$sBtn.click(function(){
				
				var searchText = $("#attendPrice").val();
				if(searchText=="请输入购车预算"){
					
					$("#attendPrice").val('');
					
				}
				
				//$(this).parents('form').attr('action','search.htm?cat=0&kwd='+encodeURI($("#attendPrice").val()));
				
				return true;
			});
			
		}
		
	};
	
	homePage.init();
	
})(jQuery,window);

