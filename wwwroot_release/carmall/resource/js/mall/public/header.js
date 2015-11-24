//Cookie Common Class
function _getCookie(key) {
	var result = document.cookie.match(new RegExp("(^| )" + key + "=([^;]*)"));
	return result != null ? decodeURIComponent(result[2]) : null;
}
function _setCookie(key, value, expireDay) {
	if (expireDay) {
		var date = new Date();
		date.setTime(date.getTime() + expireDay * 24 * 3600 * 1000);
		document.cookie = key + "=" + encodeURIComponent(value) + ";path=/;expires=" + date.toGMTString();
	} else {
		document.cookie = key + "=" + encodeURIComponent(value) + ";path=/";
	}
}
// 存储cookie
function cityCookie(cityId, cityName){
	_setCookie("MALL_CITY_ID",cityId,365);
	_setCookie("MALL_CITY_NAME",cityName,365);
}
// 城市切换
function city(){
	var cityShow = $('#currentCity'),					// 显示城市
		cityHandle = $('#cityPos'),	// 点击下拉区域
		cityNow  = cityShow.text(),						// 当前城市名称
		cityList = $('#citychage');			     	  	// 城市列表
	// 选中城市
	cityList.on('click',"a", function(){
		var cityid = $(this).attr('cityid'),	// 城市id
			cityname = $(this).attr('name');	// 城市name
	 
		cityCookie(cityid, cityname);
		// 更新cookie
		/*
		cityShow.attr({'cityid':cityid, 'name':cityname}).text(cityname);
		cityList.hide();
		*/
		window.location.href=cityLinks[cityid][0];					// 刷新页面

	});
};

function _arrayIsContain(arr,value){
	var isContain = false;
	var tempVal;
	$.each(arr,function(index){
		if($.isArray(arr)){
			tempVal = arr[index];
		}else{
			tempVal = index;
		}
		if(value === tempVal){
			isContain = true;
			return false;  // 结束循环
		}
	})
	return isContain;
}
// 读取cookie
function getCookie(){
	var cityShow = $('#currentCity');		// 显示城市
	var cityId = _getCookie("MALL_CITY_ID");
	var cityName = _getCookie("MALL_CITY_NAME");
	// 没有在cookie中获取到城市则在线获取
	
	if (cityId && cityName) {
		if (isSiteIndex()){
			window.location.href=cityLinks[cityId][0];
		}
		cityShow.attr({'cityid':cityId, 'name':cityName}).text(cityName);
	}else{
		cityShow.attr({'cityid':"310100", 'name':"上海"}).text("上海");
		setCityCookie();
	}
}
// 设置当前用户公网ip所在的城市，如果不在8城市站则返回上海
function setCityCookie(){
	//ZQ local
	if (window.location.host.indexOf('localhost') != -1) {
		cityCookie('310100', '上海');
	}

	var mallCity = ['上海','苏州','宁波','杭州','南京','成都'],mallProvince = {
		"上海":['上海'],
		"江苏":['南京'],
		"浙江":['杭州'],
		"四川":['成都']
	};
	// 获取所在城市
	$.getJSON("http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=&callback=?", function(data) {
		if(!data || data.length ==0 || data.status !=0) return;
		var cityInfo = data.address.split("|");
		var city = cityInfo[2],
			province = cityInfo[1];
		if(_arrayIsContain(mallCity,city)){
		}else if(_arrayIsContain(mallProvince,province)){
			city = mallProvince[province][0];
		}else{
			city = "上海";
		}
		// 根据城市名获取id
		$.getJSON("http://carmall.dds.com/common/cityid.htm?cityName="+encodeURIComponent(city), function(data) {
			if(data){
				cityCookie(data.cityId, data.cityName);
			}
		});
	});
}
function getCityList(){
	var cityId = _getCookie("MALL_CITY_ID");
	 var frg=$(document.createDocumentFragment());
	 if(!cityLinks){
		 return false;
	 }
	 $.each(cityLinks,function(key,item){
		 if(key&&item[1]){
			 var curStr="";
			 if(key == cityId){
				curStr=" class=\"cur\"";
			 }
			 //var cityName=item.name.replace("市","").replace("地区","").replace("自治州","");
			 var tmp=$('<a href="javascript:void(0);" target="_self" cityid="'+key+'" name="'+item[1]+'" class="city-name"><span'+ curStr +'>'+item[1]+'</span></a>');
			 tmp.appendTo(frg);
		 }
	 });
	 frg.appendTo($("#citychage"));
}
$(document.body).on('click',function(e){
	$('#cityPos').removeClass('city-cur');
	$('#citychage').hide();
});
$('#cityPos').on('click',function(){
	if($(this).hasClass('city-cur')){
		$(this).removeClass('city-cur');
		$('#citychage').slideUp();
		return false;
	}
	$(this).addClass('city-cur');
	$('#citychage').slideDown();
	return false;
});
function isSiteIndex() {
	var url = window.location.href.replace(/\/+/g,'/');
	var indexF = url.indexOf("/", url.indexOf(window.location.host));
	if (indexF == -1) {
		return true;
	}
	var indexL = url.lastIndexOf("/");
	return indexF==indexL;
}
if (!isSiteIndex() && typeof(pageCityID) != 'undefined' && pageCityID) {
	cityCookie(pageCityID, pageCityName);
}
// set city
getCookie();
// 运行
city();
getCityList();

$(document).ready(function(){
	// 判断用户是否已登录
	$.ajax({
		url:"/common/head.htm",
		dataType: "json",
		cache: false,
		success: function(data) {
			if(data){
				var favorNode = $("#mall-my");
				favorNode.attr({"href":favorNode.attr("login-href"),"target":"_blank"});
				$("#box01").hide().siblings("#box02").show();
				$("#box02").children().eq(0).html(data.greetings + '，<span title="'+data.greetings+","+data.memberCenterInfo.defaultAccount+'">'+data.realName+'</span>');
				var loginoutUrl = mainBase + "/common/account/logout.htm?backUrl=" + encodeURIComponent(window.location.href);
				$("#userCenter .group").append("<a href='"+loginoutUrl+"' target='_self'>退出</a>");
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

	function showDrop(handleDom,child,onClass,dropDom){
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
});