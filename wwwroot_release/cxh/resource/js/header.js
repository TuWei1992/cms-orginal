$(function(){
	// getCookie();
	// city();
	// getCityList();	
// 判断用户是否已登录
	$.ajax({
		url:(typeof isOrder!="undefined"&&isOrder?orderBase:mallBase) + "/common/head.htm",
		dataType: "json",
		cache: false,
		success: function(data) {
			if(data){
				$("#box01").hide().siblings("#box02").show();
                if(data.memberCenterInfo.nickName==null ||  data.memberCenterInfo.nickName=="") {
                    $("#box02").children().eq(0).html(data.greetings + '，<span title="' + data.greetings + "," + data.memberCenterInfo.defaultAccount + '">' + data.realName + '</span>');
                }else{
                    $("#box02").children().eq(0).html(data.greetings + '，<span title="' + data.greetings + "," + data.memberCenterInfo.nickName + '">' + data.realName + '</span>');

                }
                    if(data.unreadMsgCount == 0){
					$('.gotomsg-item').hide();
				}else if(data.unreadMsgCount > 0){
					$(".hidden-num").text(data.unreadMsgCount);
					$(".gotomsg").text(data.unreadMsgCount).show();
					if(data.unreadMsgCount>99){
						$(".hidden-num").text('99+');
						$(".gotomsg").text('99+');
					}

				}
				// checkSign();

			}else{
				$("#box01").show().siblings("#box02").hide();
			}
		}
	});


	$(".login-bind").on("click", function(){
		var backUrl = window.location.href;
		window.location.href = accountBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);
		return false;
	});

	$(".register-bind").on("click", function(){
		var backUrl = window.location.href;
		window.location.href = accountBase + "/account/m_register.htm?backUrl=" + encodeURIComponent(backUrl);
		return false;
	});
	showDrop($('#userCenter'),$('.user-hd'),"user-cur",$('.group'));

	// aniScore();
	
	// $(document.body).on('click',function(e){
	// 	$('#cityPos').removeClass('city-cur');
	// 	$('#citychage').hide();
	// });

	// $('#cityPos').on('click',function(){
	// 	if($(this).hasClass('city-cur')){
	// 		$(this).removeClass('city-cur');
	// 		$('#citychage').slideUp();
	// 		return false;
	// 	}
	// 	$(this).addClass('city-cur');
	// 	$('#citychage').slideDown();
	// 	return false;
	// });
});

;function showDrop(handleDom,child,onClass,dropDom){
var timeID=null;
handleDom.mouseover(function(){
	clearTimeout(timeID);
	child.addClass(onClass);
	dropDom.show();
}).mouseout(function(){
	timeID=setTimeout(function(){
		dropDom.slideUp(200, function(){
		child.removeClass(onClass);
	});
	},500);
});
}
// 城市切换
;function city(){
	var cityShow = $('#currentCity'),					// 显示城市
		cityHandle = $('#cityPos'),	// 点击下拉区域
		cityNow  = cityShow.text(),						// 当前城市名称
		cityList = $('#citychage');			     	  	// 城市列表

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
};

// 存储cookie
;function cityCookie(cityId, cityName,domain){
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
;function getCookie(){
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
;function setCityCookie(){
	// 获取所在城市
	$.getJSON("http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=&callback=?", function(data) {
		var cityName = "";
		if(data){
			if(data.status==0) {
				cityName = data.address.split("|")[2];
			}
		}
		// 根据城市名获取id
		$.getJSON(mallBase+ "/common/cityid.htm?cityName="+cityName, function(data) {
			if(data){
				cityCookie(data.cityId, data.cityName,data.domain);
				//$('#currentCity').attr({'cityid':data.cityId, 'name':data.cityName}).text(data.cityName);
			}
		});
	});
}


//(typeof isOrder!="undefined"&&isOrder?orderBase:base)
;function getCityList(){
	 $.getJSON("" + "/common/cityList.htm",function(data){
		 var frg=$(document.createDocumentFragment());
		 $.each(data,function(i,item){
			 var tmp=$('<a href="javascript:void(0);" target="_self" cityid="'+item.code+'" name="'+item.name+'" class="city-name"><span>'+item.name+'</span></a>');
			 tmp.appendTo(frg);
		 });
		 frg.appendTo($("#citychage"));
	 });
}
;function checkSign(){
		$.ajax({
			url:(typeof isOrder!="undefined"&&isOrder?orderBase:mallBase) + "/hadCheckIn.htm",
			dataType: "json",
			cache: false,
			success: function(data) {
				if(data.hadCheckIn){
					$('.mark-count').hide();
					$('.marked-sucess').show();
				}else{
					$('.marked-sucess').hide();
					$('.mark-count').show();
				}
			}
		});
}

;function aniScore(){
	var markCount =$('.mark-count'),
		addFiveScore = $('.add-score'),
		markedSucess = $('.marked-sucess');
		markCount.on('click',function(){
			$.ajax({
				url:(typeof isOrder!="undefined"&&isOrder?orderBase:mallBase) + "/hadCheckIn.htm",
				dataType: "json",
				cache: false,
				success: function(data) {
					if(data.loginStatus == 'false'){
						var backUrl = window.location.href;
						window.location.href = accountBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);
						return false;
					}

					if(data.hadCheckIn == 'true'){
						$('.mark-count').hide();
						$('.marked-sucess').show();	
					}else{
						$('.marked-sucess').hide();
						$('.mark-count').show();

						$.ajax({
								url:(typeof isOrder!="undefined"&&isOrder?orderBase:mallBase) + "/userCheckIn.htm",
								dataType: "json",
								cache: false,
								success: function(data) {
									if(data.checkInFlag){						
										addFiveScore.fadeIn('fast').animate({fontSize:"18px",top:"-6",left:"-6"},300,function(){
											addFiveScore.animate({opacity:0},300);
											setTimeout(function(){
												markedSucess.show();
												$('.mark-count').hide();
												$('.marked-sucess').show();	
											},200);
										});
									}else{
										markCount.show();
									}
								}
							});
					}
				}
			});
		return false;
	});	
}

