/*
 * Copyright (C), 2013-2013, 上海汽车集团股份有限公司
 * Author:   童颖
 * Date:     2013-12-11
 * Description: 邀请好友页面。邮箱及复制功能，复制支持部分浏览器。
 * History: //修改记录
 * <author>		<time>			<desc>
 * 方彬			2014-01-03		增加城市切换
 * lingweifeng  2014-02-19      会员中心全局导航用户名过长时截取显示
 */

//通用下拉弹窗
function showDrop(handleDom,onClass,dropDom){
	var timeID=null;
	handleDom.mouseover(function(){
		clearTimeout(timeID);
		$(this).addClass(onClass);
		dropDom.show();
	}).mouseout(function(){
		timeID=setTimeout(function(){
			dropDom.slideUp(200, function(){
			handleDom.removeClass(onClass);
		});
		},500);
	});
}
;(function ($) {
// 放这里就ok了
	$(function(){
		//投诉建议
		$('.cmp-enter a').on('mouseenter mouseleave',function(){
				$(this).toggleClass('cmp-hover-bg');
				$('.cmp-link,.cmp-hover').toggleClass('hide');
		});
		
		//投诉建议查找入口链接

		var linkFirst = window.location.href;

		if(linkFirst.indexOf('complaint.shtml') != -1){
			var cmplinkFirst ="http://" + window.location.host  + window.location.pathname;
		}else{
			var cmplinkFirst = linkFirst;
			
		}

		var newlinkFirst =	cmplinkFirst.replace(/shtml/g,'shtm123escape');
		var linkFirstURL =  encodeURIComponent(newlinkFirst);

		$('.cmp-enter a,.cmpLocation').each(function(){
			var newhref = $(this).attr("href") + "?sourceUrl=" + linkFirstURL;
			$(this).attr("href",newhref);
		});


		// 判断用户是否已登录
		$.ajax({
			url:(typeof isOrder!="undefined"&&isOrder?orderBase:base) + "/common/head.htm",
			dataType: "json",
			cache: false,
			success: function(data) {
				if(data){
					$("#box01").hide().siblings("#box02").show();
					$("#box02").children().eq(0).text(data.greetings + '，');
					$("#userCenter").find(".current").text(data.realName);
					if(data.unreadMsgCount == 0){
						$(".no-readline").hide().next(".gotomsg").hide();
					}else if(data.unreadMsgCount > 0){
						$(".no-readline").show().next(".gotomsg").show();

					}
				}else{
					$("#box01").show().siblings("#box02").hide();
				}
			}
		});

		$(".login-bind").on("click", function(){
			var backUrl = window.location.href;
			window.location.href = accountBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);
		});

		$(".register-bind").on("click", function(){
			var backUrl = window.location.href;
			window.location.href = accountBase + "/account/m_register.htm?backUrl=" + encodeURIComponent(backUrl);
		});

		//设置选中城市样式
		function citySel(){
			var cityShow = $('#currentCity'),
				cityidfirst = $('div.city').find('.city-name');
			cityidfirst.each(function(){
				if($(this).attr('cityid') == cityShow.attr('cityid')){
					$(this).addClass('sel');
				}
			});
		};

		// 城市切换
		function city(){

			var cityShow = $('#currentCity'),					// 显示城市
				cityHandle = $('#currentCity').parent("h3"),	// 点击下拉区域
				cityNow  = cityShow.val(),						// 当前城市名称
				cityList = $('div.city');			     	  	// 城市列表

			// 选中城市
			cityList.on('click',"a", function(){
				var cityid = $(this).attr('cityid'),	// 城市id
					cityname = $(this).attr('name');	// 城市name
				cityCookie(cityid, cityname);			// 更新cookie
				cityShow.attr({'cityid':cityid, 'name':cityname}).text(cityname);
				cityList.hide();
				getCookie();
				window.location.reload(true);						// 刷新页面

			});
			// 收起城市列表
			showDrop(cityHandle,"on",cityList);
		};

		// 存储cookie
		function cityCookie(cityId, cityName,domain){
			var expires = new Date();
			if(!domain){
				domain=document.cookie.match(/(^| )city\.domain=([^;]*)(;|$)/);
				domain&&(domain=domain[2]);
			}
			/* 三个月 x 一个月当作 30 天 x 一天 24 小时 x 一小时 60 分 x 一分 60 秒 x 一秒 1000 毫秒 */
			expires.setTime(expires.getTime() + 12 * 30 * 24 * 60 * 60 * 1000);
			var domainStr = domain&&domain!="null"?(";domain="+domain):"";
			document.cookie = "cityId="+encodeURIComponent(cityId)+";expires="+expires.toGMTString()+";path=/"+domainStr;
			document.cookie = "city.id="+encodeURIComponent(cityId)+";expires="+expires.toGMTString()+";path=/"+domainStr;
			document.cookie = "city.name="+encodeURIComponent(cityName)+";expires="+expires.toGMTString()+";path=/"+domainStr;
			document.cookie = "city_id="+encodeURIComponent(cityId)+";expires="+expires.toGMTString()+";path=/"+domainStr;
			document.cookie = "city_name="+encodeURIComponent(cityName)+";expires="+expires.toGMTString()+";path=/"+domainStr;
			document.cookie = "city.domain="+encodeURIComponent(domain)+";expires="+expires.toGMTString()+";path=/"+domainStr;
		};

		// 读取cookie
		function getCookie(){
			var cookie = {},
				setCookie = document.cookie,
				cityId = '',
				cityName = '上海',
				cityShow = $('#currentCity');		// 显示城市
			if( setCookie ==="" ){
				// 默认城市--上海
				cityCookie('310100', '上海');
				cityShow.attr({'cityid':'310100', 'name':'上海'}).text('上海');

			} else{
				var list = setCookie.split("; ");
				for( var i=0; i<list.length; i++ ){
					var cookie = list[i];
					var p = cookie.indexOf("=");
					var name = cookie.substring(0,p);
					var value = cookie.substring(p+1);
					try {
						value = decodeURIComponent(value);
					} catch(e) {
						continue;
					}
					cookie[name] = value;

					// 获取cookie值
					if (name == "city.id" || name == "city_id") cityId = value;
					if (name == "city.name" || name == "city_name") cityName = value;

				}

				// 没有在cookie中获取到城市则在线获取
				if (cityId == '' || cityName == '') {
					setCityCookie();
				}

				cityShow.attr({'cityid':cityId, 'name':cityName}).text(cityName);
			}
		};

		// 设置当前用户公网ip所在的城市，如果不在8城市站则返回上海
		function setCityCookie(){
			// 获取所在城市
			$.getJSON("http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=&callback=?", function(data) {
				var cityName = "";
				if(data){
					if(data.status==0) {
						cityName = data.address.split("|")[2];
					}
				}
				// 根据城市名获取id
				$.getJSON(base + "/common/cityid.htm?cityName="+cityName, function(data) {
					if(data){
						cityCookie(data.cityId, data.cityName,data.domain);
						//$('#currentCity').attr({'cityid':data.cityId, 'name':data.cityName}).text(data.cityName);
					}
				});
			});
		}

		if($('.city-t').length > 0){
			getCookie();
			// 运行
			city();
			//获取选中城市样式
			setTimeout(function(){
				citySel();
			},500);
			getCityList();
		};

		// 个人中心下拉弹窗
		showDrop($('#userCenter'),"usercenter-on",$('#useGroup'));






	});

	//(typeof isOrder!="undefined"&&isOrder?orderBase:base)
	function getCityList(){
		 $.getJSON("" + "/cityList.shtml",function(data){
			 var frg=$(document.createDocumentFragment());
			 $.each(data,function(i,item){
				 var tmp=$('<a href="javascript:void(0);" target="_self" cityid="'+item.code+'" name="'+item.name+'" class="city-name"><span>'+item.name+'</span></a>');
				 tmp.appendTo(frg);
			 });
			 frg.appendTo($("#citychage"));
		 });
	}



}) (jQuery);