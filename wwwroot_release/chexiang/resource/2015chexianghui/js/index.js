(function ($) {
	var fns;
	fns = {
		// Script for initialization.
		init: function () {
			this._set.menu();
			this._set.fade();
			this._set.actImg();
			this._set.showQRCode();
			this.userinfo();
		},
		userinfo: function(){
			$.ajax({
			      type : "POST",
			      url : mallBase+"/member/simpleInfo.htm",
			      dataType : "json",
			      success : function(response) {
			        var flag =true;//true代表省下架了 false代表正常
			        if (response.isLogin) {	         
			          	var info='<dl class="logined-box"><dt><p class="avatar-box">';
			          	info = info + '<img src="'+urlHead.replace('images/aaaaa',("img2/"+response.photoUrl))+'" width="80" height="80" alt="">';
						info = info + '<span class="avator-skin"></span><span class="member-mark">'+renzhuangstatus(response.verification)+'</span></p><p>Hi！'+response.userName+'</p></dt><dd class="member-info"><span class="arrow"></span>';
						info = info + '<p class="grade">我的积分：<strong>'+response.memberInfoCredit+'</strong></p>';
						info = info + '<p class="task">快来哦，做任务赚积分<a class="go" href="'+memberBase+'/member/member.htm  " target="_blank">GO&gt;</a></dd>';
			          info = info + '<dd class="car-info"><span>我的爱车：</span>';
					  if(response.velBrandName){
						 info = info + '<p>'+response.velBrandName +'-'+response.velSeriesName+'</p><p>'+ renzhen(response.verification)+'</p></dd></dl>';
					  }else{
						 info = info + '<p>暂无爱车信息</p></dd></dl>';
					  }
						$('#userinfo').html(info);  
			          	$('#joinbox').hide();
			          	$('#hintbox').show();
						$('#slideownbox').addClass("slide-own-box");
			          
			         	var quanyi='<ul class="data-wrapper">';
			            $.each(response.memberRights, function (n, value) {
			              quanyi +='<li><a href="'+memberBase+'/member/benefits/'+ value.httpUrl+'" target="_blank"><span class="img-box"><i class=""></i>'; 
			              quanyi +='<img src="'+urlHead.replace('images/aaaaa',(value.isHaveRight?value.rightIcon:value.invalidIcon))+'" alt=""></span><span class="tit">'+value.rightName+'</span>';
			              quanyi +='<span>&nbsp;</span></a>';
			              if(!value.isHaveRight){
			                quanyi +='<p class="get-box"><i class="arrow"></i><a href="'+memberBase+'/member/member.htm" target="_blank">会员认证后<br/>可获得此权益</a></p>';
			               }
			              quanyi +='</li>';
			         	}); 
			          	quanyi += '</ul>';
			         	$('#chezhuquanyi').html(quanyi); 
			          
			        }else{
					  var info='<dl class="unlogin-box"><dt><p class="icon-avator"></p><p>Hi！您好~</p></dt><dd><a class="login-bind" href="'+accountBase+'/account/login.htm?backUrl='+mallBase+'">登 录</a>';
			         		info = info + '<a class="register-bind" href="'+accountBase+'/account/m_register.htm?backUrl='+mallBase+'">还没有账户？注册一个吧~</a></dd>';
			           		info = info + '<dd class="desc">登录即可查看我的积分及当前可享受的权益</dd></dl>';
						$('#userinfo').html(info); 
			          	$('#joinbox').show();
			          	$('#hintbox').hide();
			          	$('#slideownbox').removeClass("slide-own-box");

						var quanyi='<ul class="data-wrapper">';
			            $.each(response.memberRights, function (n, value) {
			              quanyi +='<li><a href="'+memberBase+'/member/benefits/'+ value.httpUrl+'" target="_blank"><span class="img-box"><i class=""></i>'; 
			              quanyi +='<img src="'+urlHead.replace('images/aaaaa',value.rightIcon)+'" alt=""></span><span class="tit">'+value.rightName+'</span>';
			              quanyi +='<span></span></a>';
			              quanyi +='</li>';
			         	}); 
			          	quanyi += '</ul>';
			         	$('#chezhuquanyi').html(quanyi);          
			        }

					fns._set.scrollEffect();
			        
			        	
			      }
			    });
			},
		_set: {
			// Script for start menu effect.
			menu:function(){
				$('.header .main-nav li').hover(function(){
					$(this).find('.menu-box').show();
				},function(){
					$(this).find('.menu-box').hide();
				});

				$('.header .menu-box').mouseenter(function(){
					$(this).show();
				});
			},
			// Script for start banner effect.
			fade: function(){
					var index = 0,
						MyTime,
					    maximg = $('#j-banner-content li').length;
					    if(maximg<2){					
					    	return false;
					    }
					    ShowjQueryFade(0);
					    index=1;
					$("#j-banner-num span").hover(function(){
						if(MyTime){
							clearTimeout(MyTime);
						}
						index  =  $("#j-banner-num span").index(this);
						var idx = $('#j-banner-content .current').index();
						if(index !=idx){							
							MyTime = setTimeout(function(){
								//$('#j-banner-content').stop();
								ShowjQueryFade(index);
								index++;
								if(index==maximg){
										index=0;
									}
							} , 400);
						}

					}, function(){
						clearTimeout(MyTime);
						autoPlay();
					});
					//滑入 停止动画，滑出开始动画.
					 $('#j-banner-content').hover(function(){
							if(MyTime){
								clearTimeout(MyTime);
							}
					 },function(){
						autoPlay();
					 });
					//自动播放
					autoPlay();

					function autoPlay(){
						MyTime = setTimeout(function(){
								clearTimeout(MyTime);
								ShowjQueryFade(index);
								index++;
								if(index==maximg){
									index=0;
								}
								autoPlay();
						} , 4000);
					}
				function ShowjQueryFade(i) {		
					$("#j-banner-content li").eq(i).animate({'opacity': 'show'},1000).css({'z-index':1}).addClass('current').siblings().removeClass('current').animate({'opacity': 'hide'},1000).css({'z-index':0});
					$("#j-banner-num span").eq(i).addClass("cur").siblings().removeClass("cur");
				}
			},
			// Script for start scroll effect.
			scrollEffect:function(){
					$.fn.infiniteCarousel = function () {
					function repeat(str, num) {
						return new Array( num + 1 ).join( str );
					}
				    return this.each(function () {
						var $wrapper = $('.data-wrap', this).css('overflow', 'hidden'),
				            $slider = $wrapper.find('ul'),
				            $items = $slider.find('> li'),
				            $single = $items.filter(':first'),
				            
				            singleWidth = $single.outerWidth(), 
				            visible = Math.ceil($wrapper.innerWidth() / singleWidth), // note: doesn't include padding or border
				            currentPage = 1,
				            pages = Math.ceil($items.length / visible),
				            cur=1;        

						// // 1. Pad so that 'visible' number will always be seen, otherwise create empty items
						// if (($items.length % visible) != 0) {
						// $slider.append(repeat('<li class="empty" />', visible - ($items.length % visible)));
						// $items = $slider.find('> li');
						// }

						// // 2. Top and tail the list with 'visible' number of items, top has the last section, and tail has the first
						// $items.filter(':first').before($items.slice(- visible).clone().addClass('cloned'));
						// $items.filter(':last').after($items.slice(0, visible).clone().addClass('cloned'));
						// $items = $slider.find('> li'); // reselect

						// 3. Set the left position to the first 'real' item
						var w = singleWidth * $items.length;
						$slider.width(w);
						$wrapper.scrollLeft(0);
				        // 4. paging function
				        function gotoPage(page) {
				            var dir = page < currentPage ? -1 : 1,
				                n = Math.abs(currentPage - page),
				                left = singleWidth * dir * visible * n;

				            $wrapper.filter(':not(:animated)').animate({
				                scrollLeft : '+=' + left
				            }, 500, function () {
				                currentPage = page;
				            });
				            return false;
				        }

						$('.btn-left', this).addClass('btn-left-none');
						$('.btn-left', this).click(function () {
							$(this).next().removeClass('btn-right-none');
							if(--cur <= 1){
								$(this).addClass('btn-left-none');								
								cur = 1;
							}
				            return gotoPage(currentPage - 1);
				        });
				        
				        $('.btn-right', this).click(function () {
				        	$(this).prev().removeClass('btn-left-none');
						   	if(++cur >= pages){
								$(this).addClass('btn-right-none');
								cur=pages;
							}
				            return gotoPage(currentPage + 1);
				        });
				    });
				};				
				$('.slide-box').infiniteCarousel();

			},
			// Script for start act effect.
			actImg: function(){
					var elem = $('.mod-side .icon-arrow-left').parents('.mod-th').next(),
						imgNum = elem.find('li').length,
						curIndex,
						nextIndex;
					if(imgNum<1){
						$('.mod-side .icon-arrow-left').parent().hide();
					}
					$('.mod-side .icon-arrow-left').on('click',function(){				
						curIndex = elem.find('.show').index();
						if(curIndex > 0){
							nextIndex = curIndex - 1;
						}else if(curIndex === 0){
							nextIndex = curIndex - 1 + imgNum;
						}

						indexChange(elem,nextIndex);
						return false;
					});

					$('.mod-side .icon-arrow-right').on('click', function() {
							curIndex = elem.find('.show').index();
							if(curIndex < imgNum - 1){
								nextIndex = curIndex + 1;
							}else if(curIndex === imgNum - 1){
								nextIndex = curIndex + 1 -imgNum;
							}

							indexChange(elem,nextIndex);
							return false;
					});

					function indexChange(elem,i){
						elem.find('.show').removeClass('show');
						elem.find('li').eq(i).addClass('show');		
					}
			},
			showQRCode: function () {

					var h = $('html,body').height(),
							sidebar = $('#j-sidebar');
						sidebarPos();
						$('.close',sidebar).on('click',function(){
							sidebar.hide();
							return false;
						});
						$(window).resize(function(){
							sidebarPos();
						})

					function sidebarPos(){
						var w = $(window).width();
							if(w<1567){
								sidebar.css({'left':'auto','right':0,'marginLeft':'auto'});
							}
					}
				}

		}

	};

	$(function(){
		fns.init();
	});
    
	  function renzhen(status){
	    switch (status){
	      case 1:return "未验证";break;
	      case 2:return "验证中";break;
	      case 3:return "已验证";break;
	      case 4:return "验证未通过";break;
		  default:return "未验证";break;
	    }
	  }

	 function renzhuangstatus(status){
	    switch (status){
	      case 1:return "普通会员";break;
	      case 2:return "普通会员";break;
	      case 3:return "认证会员";break;
	      case 4:return "普通会员";break;
		  default:return "普通会员";break;
	    }
	  }

	
} (jQuery));
