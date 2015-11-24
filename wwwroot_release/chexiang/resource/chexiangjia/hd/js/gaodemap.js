	var storeInfoData=null;
	var pageSize=8;
	var maxEndIndex=null;
	
	//城市和区域的关系
	var cityDistRelMap=new Map();
	
	//区域和店铺的关系
	var distStoreRelMap=new Map();
	
	//地图
	var mapObj = null;
	var windowsArr =null;  
	var marker =null;
	
	
	
	$(function(){
		initMapControl();
		queryStoreInfo('http://weixin.achezhan.com/wemall/restful/getStoreForPC.htm');
		
		
		
	});
	
	/**
	*从A车站中获取 店铺信息
	*/
	function queryStoreInfo(url){
		$.ajax({
			type : "get",
			async : false,
			url : url,
			dataType : "jsonp",
			success : function(data){
				storeStoreInfoData(data);
			},
			error:function(){
				alert("请刷新");
			}
		});
	};
	
	function initMapControl(){
		mapObj = new AMap.Map("mapContainer", {
			resizeEnable: true,
			view: new AMap.View2D({
				center: new AMap.LngLat(121.472644,31.231706),
				zoom: 11
			})
		});
		windowsArr = new Array();  
		marker = new Array();
		
		mapObj.plugin(['AMap.ToolBar'],function(){
			//设置地位标记为自定义标记
			var toolBar = new AMap.ToolBar(); 
			mapObj.addControl(toolBar);		
		});	
		//mapObj.setZoomAndCenter(11, new AMap.LngLat(121.472644,31.231706));
	
	}
	
	
	function displayInMap(store){
		   //添加marker
		    var lngX = store.localX
		    var latY = store.localY;
		    var markerOption = {
		        map:mapObj,						
		        icon:"http://webapi.amap.com/images/0.png",  
		        position:new AMap.LngLat(lngX, latY)  
		    };            
		    var mar =new AMap.Marker(markerOption); 
		    marker.push(new AMap.LngLat(lngX, latY));
			
			 //添加infowindow
		    var infoWindow = new AMap.InfoWindow({
		        content:"<h3>" + store.storeName +"</h3>"+
		        		"<div class='map_txt_cn'>" + 
						"<p>地址：" + store.address + "</p>" +
						"<p>营业时间：<em>" + store.businesstime + "</em></p>" +
						"<p>联系电话：" + store.telephone + "</p>" +
						"</div>",
		        size:new AMap.Size(370,0),
		        autoMove:true, 
		        offset:{x:0, y:-20}
		    });
			 windowsArr.push(infoWindow);
			 var aa = function(e){infoWindow.open(mapObj,mar.getPosition());}; 
			 AMap.event.addListener(mar, "mouseover", aa);

			
	}
	
		function openMarkerTipById1(pointid,thiss){  //根据id打开搜索结果点tip  
		    thiss.style.background='#CAE1FF';  
		    windowsArr[pointid].open(mapObj,marker[pointid]);   
		}  
		function onmouseout_MarkerStyle(pointid,thiss) { //鼠标移开后点样式恢复  
		   thiss.style.background="";  
		}
	

	
	/**
	*分析店铺信息，并存储，方便根据城市和地区进行搜索
	*
	*/
	function storeStoreInfoData(data){
		storeInfoData=data
		maxEndIndex=storeInfoData.length-1;
		
		for(var i=0;i<storeInfoData.length;i++){
			var cityName=storeInfoData[i].cityName;
			var distName=storeInfoData[i].distName;
			
			if(StringUtil.isNotEmpty(distName)){
				//添加城市和地区的关系
				if(cityDistRelMap.containsKey(cityName)){//包含此城市，提取value
					var distArr=cityDistRelMap.get(cityName);
					
					if(distArr[distName]==null){//不存在此地区，添加
						distArr.push(distName);
						cityDistRelMap.put(cityName,distArr);
					}
				}else{//不包含此城市，添加dist value
					var distArr=new Array();
					distArr.push(distName);
					cityDistRelMap.put(cityName,distArr);	
				}
				
				//添加城市和店的关系 方便查询城市下所有的店
				if(distStoreRelMap.containsKey(cityName)){//存在城市,
					var storeArr=distStoreRelMap.get(cityName);
					storeArr.push(i);
					distStoreRelMap.put(cityName,storeArr);
				}else{//不存在城市
					var storeArr=new Array();
					storeArr.push(i);
					distStoreRelMap.put(cityName,storeArr);
				}

				//添加地区和店的关系
				var city_dist_name=cityName+"_"+distName;//防止区相同
				if(distStoreRelMap.containsKey(city_dist_name)){//存在地区,
					var storeArr=distStoreRelMap.get(city_dist_name);
					storeArr.push(i);
					distStoreRelMap.put(city_dist_name,storeArr);
				}else{//不存在地区
					var storeArr=new Array();
					storeArr.push(i);
					distStoreRelMap.put(city_dist_name,storeArr);
				}
			}
			
		}
		
		//显示城市列表 默认显示上海
		displayCityData("上海市");
		

	}
	
	/*
	*根据cityName 显示选中的city 
	*
	*/
	function displayCityData(cityName){
			var keys=cityDistRelMap.keys;
			
		var obj=$("#js_dealer_city .city-tb ul");//城市列表区域	
			
		for(var i=0;i<keys.length;i++){
			
			if(cityName==keys[i]){
					$("#js_dealer_city .city-th .js-city-name").html(cityName);
					$(obj).append("<li><a class='cur' href='javascript:;'>"+cityName+"</a></li>");
			}else{
				$(obj).append("<li><a href='javascript:;'>"+keys[i]+"</a></li>");
			}
			
		}
		
		
		displayDistDataAndMap(cityName);
		
			//给城市列表加事件
		$(obj).find("a").click(function(){
				$(this).parents("ul").find("a").removeClass("cur");
				$(this).addClass("cur");
				
				var cityName=$(this).html();
				$("#js_dealer_city .city-th .js-city-name").html($(this).html());
				
				displayDistDataAndMap(cityName);
		});
			
	}
	
	/*
	*根据cityName 显示地区列表
	*
	*/
	function displayDistDataAndMap(cityName){
			var distArr=cityDistRelMap.get(cityName);
			
			var obj=$("#seek .seek_cn .area_cn");
			//默认显示全部
			$(obj).html("<a href='javascript:;' class='cur'>全部</a>");
			for(var i=0;i<distArr.length;i++){
				(obj).append("<a href='javascript:;' >"+distArr[i]+"</a>");	
			}	
			
			$(obj).find("a").click(function(){
				$(this).parent().find("a").removeClass("cur");
				$(this).addClass("cur");
				
				var dist=$(this).html();
				if(dist=="全部"){
						dist="";
					}
				searchStoreByCityAndDist($("#js_dealer_city .city-th .js-city-name").html(),dist);
			});
			
		
			searchStoreByCityAndDist(cityName,'');
	}
	
	
	

	
	/**
		通过城市和区域搜索店铺
		并将相关信息显示在地图上
	*/
	function searchStoreByCityAndDist(cityName,distName){
		
		
		if(!StringUtil.isNotEmpty(cityName)){
			return;
		}
		var city_dist_name=cityName;
		if(StringUtil.isNotEmpty(distName)){
			city_dist_name=city_dist_name+"_"+distName;
		}
		
		var storeIndexArr=distStoreRelMap.get(city_dist_name);
		if(StringUtil.isEmpty(storeIndexArr)){
			return;
		}
			
		initMapControl();

		for(var i=0;i<storeIndexArr.length;i++){
			var sotreIndex=storeIndexArr[i];
			var store=storeInfoData[sotreIndex];
			displayInMap(store);
								 
			 if(i==0){ 		
			 		mapObj.setZoomAndCenter(11, new AMap.LngLat(store.localX,store.localY));
			 	}
			
			 
		}
		
		
	}
	
	
	/*
		js实现 java的Map功能
	*/
	
	function Map() {
		/** 存放键的数组(遍历用到) */
		this.keys = new Array();
		/** 存放数据 */
		this.data = new Object();
		
		/**
		 * 放入一个键值对
		 * @param {String} key
		 * @param {Object} value
		 */
		this.put = function(key, value) {
			if(this.data[key] == null){
				this.keys.push(key);
			}
			this.data[key] = value;
		};
		
		/**
		 * 获取某键对应的值
		 * @param {String} key
		 * @return {Object} value
		 */
		this.get = function(key) {
			return this.data[key];
		};
		
		/**
		 * 删除一个键值对
		 * @param {String} key
		 */
		this.remove = function(key) {
			this.keys.remove(key);
			this.data[key] = null;
		};
		
		/**
		 * 遍历Map,执行处理函数
		 * 
		 * @param {Function} 回调函数 function(key,value,index){..}
		 */
		this.each = function(fn){
			if(typeof fn != 'function'){
				return;
			}
			var len = this.keys.length;
			for(var i=0;i<len;i++){
				var k = this.keys[i];
				fn(k,this.data[k],i);
			}
		};
		
		/**
		 * 获取键值数组(类似Java的entrySet())
		 * @return 键值对象{key,value}的数组
		 */
		this.entrys = function() {
			var len = this.keys.length;
			var entrys = new Array(len);
			for (var i = 0; i < len; i++) {
				entrys[i] = {
					key : this.keys[i],
					value : this.data[i]
				};
			}
			return entrys;
		};
		
		/**
		 * 判断Map是否为空
		 */
		this.isEmpty = function() {
			return this.keys.length == 0;
		};
		
		/**
		 * 获取键值对数量
		 */
		this.size = function(){
			return this.keys.length;
		};
		
		// 是否存在某键值
		this.containsKey = function (key) {
			if(this.data[key] == null){
				return false;
			}else{
				return true;
			}
		};
		
		// 是否存在某值
		this.containsValue = function (value) {
			for (var temp in this.data) {
				if (this.data[temp] == value) {
					return true;
				}
			}
			
			return false;
		};
		
		/**
		 * 重写toString 
		 */
		this.toString = function(){
			var s = "{";
			for(var i=0;i<this.keys.length;i++,s+=','){
				var k = this.keys[i];
				s += k+"="+this.data[k];
			}
			s+="}";
			return s;
		};
	}

	StringUtil={
		isNotEmpty:function(str){
			if(str!=null && str!=""){
				return true;
			}else{
				return false;
			}
		},
		isEmpty:function(str){
			if(str!=null && str!=""){
				return false;
			}else{
				return true;
			}
		}
	}
	
	
