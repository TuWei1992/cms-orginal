var agt =   window.navigator.userAgent;
var isQuirks = document.compatMode == "BackCompat";
var isStrict = document.compatMode == "CSS1Compat";
var isOpera = agt.toLowerCase().indexOf("opera") != -1;
var isChrome = agt.toLowerCase().indexOf("chrome") != -1;
var isIE = agt.toLowerCase().indexOf("msie") != -1 && !isOpera;
var isIE8 = agt.toLowerCase().indexOf("msie 8") != -1 && !!window.XDomainRequest && !!document.documentMode;
var isIE7 = agt.toLowerCase().indexOf("msie 7") != -1 && !isIE8;
var isIE6 = isIE && !window.XMLHttpRequest;
var isGecko = agt.toLowerCase().indexOf("gecko") != -1;
var isBorderBox = isIE && isQuirks;


function getEl(id){
	return typeof(id) == 'string'?document.getElementById(id):id;
}

/** domReady(func) 注册在dom载入后要执行的方法 **/
(function(){
	var isReady=false; //判断onDOMReady方法是否已经被执行过
	var readyList= [];//把需要执行的方法先暂存在这个数组里
	var timer;//定时器句柄
	window.domReady=function(fn) {
		if (isReady )
			fn.call( document);
		else
			readyList.push( function() { return fn.call(this);});
		return this;
	}
	var onDOMReady=function(){
		for(var i=0;i<readyList.length;i++){
			readyList[i].apply(document);
		}
		readyList = null;
	}
	var bindReady = function(evt){
		if(isReady) return;
		isReady=true;
		onDOMReady.call(window);
		if(document.removeEventListener){
			document.removeEventListener("DOMContentLoaded", bindReady, false);
		}else if(document.attachEvent){
			document.detachEvent("onreadystatechange", bindReady);
			if(window == window.top){
				clearInterval(timer);
				timer = null;
			}
		}
	};
	if(document.addEventListener){
		document.addEventListener("DOMContentLoaded", bindReady, false);
	}else if(document.attachEvent){
		document.attachEvent("onreadystatechange", function(){
			if((/loaded|complete/).test(document.readyState))
				bindReady();
		});
		if(window == window.top){
			timer = setInterval(function(){
				try{
					isReady||document.documentElement.doScroll('left');//在IE下用能否执行doScroll判断dom是否加载完毕
				}catch(e){
					return;
				}
				bindReady();
			},5);
		}
	}
})();

function marquee(boxid,innerid,cloneid){
/*HTML结构
<div id="marqueeBox" style="width:98%;overflow:hidden;">
  <table>
	<tr>
	  <td id="marqueeInner" nowrap>
		  <img src="../images/photo.gif">
		  <img src="../images/photo.gif">
		  <img src="../images/photo.gif">
		  <img src="../images/photo.gif">
		  <img src="../images/photo.gif">
	  </td>
	  <td id="marqueeClone" nowrap>&nbsp;</td>
	</tr>
  </table>
</div>
< script >
	domReady(function(){
			marquee('marqueeBox','marqueeInner','marqueeClone')
	});
< / script >
*/
	var speed=20; //数字越大速度越慢
	var myMarquee;
	var box=getEl(boxid);
	var inner=getEl(innerid);
	var clone=getEl(cloneid);
	clone.innerHTML=inner.innerHTML;
	var move=function(){
		if(clone.offsetWidth-box.scrollLeft<=0){
			box.scrollLeft-=inner.offsetWidth;
		}else{
			box.scrollLeft++;
		}
	}
	box.onmouseover=function() {
		clearInterval(myMarquee);
	}
	box.onmouseout=function() {
		myMarquee=setInterval(move,speed)
	};
	if(inner.offsetWidth>box.offsetWidth){
		box.onmouseout();
	}
}

var monthMap = { "1" : "January", "2" : "February", "3" : "March", "4" : "April", "4" : "May", "5" : "June", "6" : "July", "7" : "August", "8" : "September", "9" : "October", "11" : "November", "12" : "December"
}

function CalConv() {
	FIRSTYEAR = 1998;
	LASTYEAR = 2031;

	today = new Date();
	SolarYear = today.getFullYear();
	SolarMonth = today.getMonth() + 1;
	SolarDate = today.getDate();
	Weekday = today.getDay();
	LunarCal = [
	new tagLunarCal(27, 5, 3, 43, 1, 0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1), new tagLunarCal(46, 0, 4, 48, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1), /* 88 */
	new tagLunarCal(35, 0, 5, 53, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1), /* 89 */
	new tagLunarCal(23, 4, 0, 59, 1, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1), new tagLunarCal(42, 0, 1, 4, 1, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1), new tagLunarCal(31, 0, 2, 9, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0), new tagLunarCal(21, 2, 3, 14, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1), /* 93 */
	new tagLunarCal(39, 0, 5, 20, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1), new tagLunarCal(28, 7, 6, 25, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 1), new tagLunarCal(48, 0, 0, 30, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1, 1), new tagLunarCal(37, 0, 1, 35, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1), /* 97 */
	new tagLunarCal(25, 5, 3, 41, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1), new tagLunarCal(44, 0, 4, 46, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1), new tagLunarCal(33, 0, 5, 51, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1), new tagLunarCal(22, 4, 6, 56, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0), /* 101 */
	new tagLunarCal(40, 0, 1, 2, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0), new tagLunarCal(30, 9, 2, 7, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1), new tagLunarCal(49, 0, 3, 12, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1), new tagLunarCal(38, 0, 4, 17, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0), /* 105 */
	new tagLunarCal(27, 6, 6, 23, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1), new tagLunarCal(46, 0, 0, 28, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 0), new tagLunarCal(35, 0, 1, 33, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0), new tagLunarCal(24, 4, 2, 38, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1), /* 109 */
	new tagLunarCal(42, 0, 4, 44, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1), new tagLunarCal(31, 0, 5, 49, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0), new tagLunarCal(21, 2, 6, 54, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1), new tagLunarCal(40, 0, 0, 59, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1), /* 113 */
	new tagLunarCal(28, 6, 2, 5, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0), new tagLunarCal(47, 0, 3, 10, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1), new tagLunarCal(36, 0, 4, 15, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 1), new tagLunarCal(25, 5, 5, 20, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0), /* 117 */
	new tagLunarCal(43, 0, 0, 26, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1), new tagLunarCal(32, 0, 1, 31, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0), new tagLunarCal(22, 3, 2, 36, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0)]; /* 民国年月日 Codes by / */
	SolarCal = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
	SolarDays = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365, 396, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366, 397];


	if (SolarYear <= FIRSTYEAR || SolarYear > LASTYEAR) return 1;
	sm = SolarMonth - 1;
	if (sm < 0 || sm > 11) return 2;
	leap = GetLeap(SolarYear);
	if (sm == 1) d = leap + 28;
	else d = SolarCal[sm];
	if (SolarDate < 1 || SolarDate > d) return 3;
	y = SolarYear - FIRSTYEAR;
	acc = SolarDays[leap * 14 + sm] + SolarDate;
	kc = acc + LunarCal[y].BaseKanChih;
	Kan = kc % 10;
	Chih = kc % 12;

	Age = kc % 60;
	if (Age < 22) Age = 22 - Age;
	else Age = 82 - Age;

	if (acc <= LunarCal[y].BaseDays) {
		y--;
		LunarYear = SolarYear - 1;
		leap = GetLeap(LunarYear);
		sm += 12;
		acc = SolarDays[leap * 14 + sm] + SolarDate;
	} else LunarYear = SolarYear;
	l1 = LunarCal[y].BaseDays;
	for (i = 0; i < 13; i++) {
		l2 = l1 + LunarCal[y].MonthDays[i] + 29;
		if (acc <= l2) break;
		l1 = l2;
	}
	LunarMonth = i + 1;
	LunarDate = acc - l1;
	im = LunarCal[y].Intercalation;
	if (im != 0 && LunarMonth > im) {
		LunarMonth--;
		if (LunarMonth == im) LunarMonth = -im;
	}
	if (LunarMonth > 12) LunarMonth -= 12;
	today = new Date();

	function initArray() {
		this.length = initArray.arguments.length
		for (var i = 0; i < this.length; i++)
		this[i + 1] = initArray.arguments[i]
	}
	var d = new initArray(" Sunday"," Monday"," Tuesday"," Wednesday"," Thursday"," Friday"," Saturday");
	var ret= monthMap[today.getMonth()+1] + " " +  today.getDate() + "th, " + today.getFullYear(); //today.getFullYear()+ "年"+ (today.getMonth() + 1)+ "月"+ today.getDate()+ "日&nbsp;"+ d[today.getDay() + 1];

	months = ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"];

	days = ["初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"];
	//ret += "&nbsp;农历" + months[LunarMonth - 1] + "月" + days[LunarDate - 1];
	return ret;
} /* 是否有闰年, 0 平年, 1 闰年 */

function GetLeap(year) {
	if (year % 400 == 0) return 1;
	else if (year % 100 == 0) return 0;
	else if (year % 4 == 0) return 1;
	else
	return 0;
}

function tagLunarCal(d, i, w, k, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13) {
	this.BaseDays = d;
	this.Intercalation = i; /* 0代表此年沒有闰月 */
	this.BaseWeekday = w; /* 民国1月1日星期減 1 */
	this.BaseKanChih = k; /* 民国1月1日干支序号减 1 */
	this.MonthDays = [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13]; /* 此農曆年每月之大小, 0==小月(29日), 1==大月(30日) */
}

domReady(function(){
	if(document.getElementById("h_getDay")){
		document.getElementById("h_getDay").innerHTML=CalConv();
	}
});

Cookie = window.Cookie || {};

Cookie.set = function(name, value, expires, path, domain, secure) {
	if (expires) {
		expires = new Date(new Date().getTime() + expires * 1000 * 60 * 60).toGMTString();
	}
	document.cookie = name + "=" + encodeURIComponent(value) + ";" + ((expires) ? " expires=" + expires + ";" : "") + ((path) ? "path=" + path + ";" : "") + ((domain) ? "domain=" + domain + ";" : "") + ((secure && secure != 0) ? "secure" : "");
};

Cookie.get = function(name) {
	var arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
	if (arr != null) {
		return decodeURIComponent(arr[2]);
	}
	return null;
};

Cookie.remove = function(name) {
	var expires = new Date();
	expires.setTime(expires.getTime() - 1);
	var cookieValue = Cookie.get(name);
	if (cookieValue != null) {
		document.cookie = name + "=" + cookieValue + ";expires=" + expires.toGMTString();
	}
};


//进行异步请求
function loadJs(url, onsuccess) {
	var head = document.getElementsByTagName('head')[0] || document.documentElement,
		script = document.createElement('script'),
		done = false;
	script.src = url;
	script.onerror = script.onload = script.onreadystatechange = function() {
		if (!done && (!this.readyState || this.readyState === "loaded" || this.readyState === "complete")) {
			done = true;
			if (onsuccess) {
				onsuccess();
			}
			script.onerror = script.onload = script.onreadystatechange = null;
			head.removeChild(script);
		}
	};
	head.appendChild(script);
}

/*
 * 获得form的所有elements并把value转换成由'&'连接的键值字符串
 * 参数filter 为过滤函数,会被循环调用传递给item作参数要求返回布尔值判断是否过滤
 */
function getQueryString(formid, filter) {
	var el = document.getElementById(formid);
	if (!el) {
		alert("查找表单元素失败!");
		return;
	}
	filter = filter ||
	function(el) {
		return false;
	};
	var result = [],
		els = el.elements,
		l = els.length,
		i, _push = function(name, value) {
			result.push(encodeURIComponent(name) + '=' + encodeURIComponent(value + ''));
		};
	for (i = 0; i < l; ++i) {
		el = els[i];
		var nameOrId = el.name || el.id;
		if (!el.type || !nameOrId || filter(el)) {
			continue;
		}
		switch (el.type) {
		case "text":
		case "hidden":
		case "password":
		case "textarea":
			_push(nameOrId, el.value);
			break;
		case "radio":
		case "checkbox":
			if (el.name && el.checked) {
				_push(nameOrId, el.value);
			}
			break;
		case "select-one":
			if (el.selectedIndex > -1) {
				_push(nameOrId, el.value);
			}
			break;
		case "select-multiple":
			var opts = el.options;
			for (var j = 0; j < opts.length; ++j) {
				if (opts[j].selected) {
					_push(nameOrId, opts[j].value);
				}
			}
			break;
		case 'button':
			break;
		default:
			if (el.name && el.id && el.value) { //同时拥有type,name,id,value的元素也视为表单元素
				_push(nameOrId, el.value);
			}
			break;
		}
	}
	return result.join("&");
}