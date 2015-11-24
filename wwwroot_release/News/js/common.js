// JavaScript Document

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

var WEATHER = {
	RunGLNL: function() {
		var c = new Date();
		var a = c.getDate(),
			b = c.getMonth() + 1,
			j = c.getFullYear();
		var h = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
		var e = h[c.getDay()];
		var g = j + "年" + b + "月" + a + "日 " + e;
		var f = this.CnDateofDateStr(c);
		document.getElementById("ztime").innerHTML = g +' '+ f;
	},
	DaysNumberofDate: function(a) {
		return parseInt((Date.parse(a) - Date.parse(a.getFullYear() + "/1/1")) / 86400000, 10) + 1
	},
	CnDateofDate: function(q) {
		var h = [22, 42, 218, 0, 131, 73, 182, 5, 14, 100, 187, 0, 25, 178, 91, 0, 135, 106, 87, 4, 18, 117, 43, 0, 29, 182, 149, 0, 138, 173, 85, 2, 21, 85, 170, 0, 130, 85, 108, 7, 13, 201, 118, 0, 23, 100, 183, 0, 134, 228, 174, 5, 17, 234, 86, 0, 27, 109, 42, 0, 136, 90, 170, 4, 20, 173, 85, 0, 129, 170, 213, 9, 11, 82, 234, 0, 22, 169, 109, 0, 132, 169, 93, 6, 15, 212, 174, 0, 26, 234, 77, 0, 135, 186, 85, 4];
		var g = [];
		var j = [];
		var k;
		var a;
		var l = [];
		var n;
		var b;
		var f;
		var m;
		var d;
		var p;
		var e = q.getFullYear();
		var c = q.getMonth() + 1;
		var o = q.getDate();
		if (e < 100) {
			e += 1900
		}
		if ((e < 1997) || (e > 2020)) {
			return 0
		}
		l[0] = h[(e - 1997) * 4];
		l[1] = h[(e - 1997) * 4 + 1];
		l[2] = h[(e - 1997) * 4 + 2];
		l[3] = h[(e - 1997) * 4 + 3];
		if ((l[0] & 128) !== 0) {
			g[0] = 12
		} else {
			g[0] = 11
		}
		k = (l[0] & 127);
		b = l[1];
		b = b << 8;
		b = b | l[2];
		a = l[3];
		for (n = 15; n >= 0; n--) {
			j[15 - n] = 29;
			if (((1 << n) & b) !== 0) {
				j[15 - n]++
			}
			if (g[15 - n] === a) {
				g[15 - n + 1] = -a
			} else {
				if (g[15 - n] < 0) {
					g[15 - n + 1] = -g[15 - n] + 1
				} else {
					g[15 - n + 1] = g[15 - n] + 1
				}
				if (g[15 - n + 1] > 12) {
					g[15 - n + 1] = 1
				}
			}
		}
		f = this.DaysNumberofDate(q) - 1;
		if (f <= (j[0] - k)) {
			if ((e > 1901) && (this.CnDateofDate(new Date((e - 1) + "/12/31")) < 0)) {
				d = -g[0]
			} else {
				d = g[0]
			}
			p = k + f
		} else {
			m = j[0] - k;
			n = 1;
			while ((m < f) && (m + j[n] < f)) {
				m += j[n];
				n++
			}
			d = g[n];
			p = f - m
		}
		if (d > 0) {
			return d * 100 + p
		} else {
			return d * 100 - p
		}
	},
	CnYearofDate: function(a) {
		var b = a.getFullYear();
		var d = a.getMonth() + 1;
		var c = parseInt(Math.abs(this.CnDateofDate(a)) / 100, 10);
		if (b < 100) {
			b += 1900
		}
		if (c > d) {
			b--
		}
		b -= 1864;
		return this.CnEra(b) + "年"
	},
	CnMonthofDate: function(b) {
		var c = ["零", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"];
		var a;
		a = parseInt(this.CnDateofDate(b) / 100, 10);
		if (a < 0) {
			return "闰" + c[-a] + "月"
		} else {
			return c[a] + "月"
		}
	},
	CnDayofDate: function(b) {
		var a = ["零", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"];
		var c;
		c = (Math.abs(this.CnDateofDate(b))) % 100;
		return a[c]
	},
	CnEra: function(c) {
		var a = ["甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"];
		var b = ["子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"];
		return a[c % 10] + b[c % 12]
	},
	CnDateofDateStr: function(a) {
		if (this.CnMonthofDate(a) === "零月") {
			return "　请调整您的计算机日期!"
		} else {
			return "农历" + this.CnYearofDate(a) + " " + this.CnMonthofDate(a) + this.CnDayofDate(a)
		}
	}
};
domReady(function(){
	WEATHER.RunGLNL();
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

//搜索框提示
function soClick(obj){
	if(obj.value=="请输入检索关键字") obj.value =""; 
}
function soBlur(obj){
	if(obj.value=="") obj.value ="请输入检索关键字"; 
}

//加入收藏
function addFavorite(url,title)
{
	if(url=="http:///") return false;

	try{
		window.external.addFavorite(url,title); 
	}catch(e){
		try{
			window.sidebar.addPanel(title,url, ""); 
		}catch(e){
			alert("加入收藏失败,请按Ctrl+D进行收藏");
		}
	}		
} 
