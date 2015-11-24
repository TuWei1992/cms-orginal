PageFlip = function(options) {
	//画册容器元素
	this.$pageWrapEl=$(options.pageWrap);
	this.pageWrapEl=this.$pageWrapEl.get(0);
	this.id=this.pageWrapEl.id || 'pageflip' + (+new Date);
	//画册图片及音频视频的路径数据
	this.pagesData=options.pagesData;
	//当前页,以右侧页面为准
	this.currentIndex=1; 
	this.zIndex=9999;
	this.movePos=0;
	// 翻页导航一小节的宽度，翻页导航初始化时会再次确定
	this.navbarJoint_width=10;
	this.init();
};
$.extend(PageFlip.prototype,Observable);
$.extend(PageFlip.prototype,{
	blankGif: "data:image/gif;base64,R0lGODlhAQABAID/AMDAwAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==",
	template: '<div class="page_btmLayer">'
			+'	<div class="playerWrap_l" style="height: 20px;display:none"><object id="{id}_player_l" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="400" height="20"><param name="movie" value="../images/playerimage.swf"><param name="allowscriptaccess" value="always"><param name="AllowNetworking" value="all"><param name="flashvars" value="file=../images/blank.mp3&height=20&width=400"><embed name="{id}_player_l" src="../images/playerimage.swf" width="400" height="20" flashvars="file=../images/blank.mp3&height=20&width=400" allowScriptAccess="always" allowNetworking="all" type="application/x-shockwave-flash"></embed></object></div>'
			+'	<div class="pageLayer_l">'
			+'		<img />'
			+'	</div>'
			+'	<div class="playerWrap_r" style="height: 20px;"></div>'
			+'	<div class="pageLayer_r">'
			+'		<img />'
			+'	</div>'
			+'</div>'
			+'<div class="page_flipLayer"></div>'
			+'<div class="page_hotLayer">'
			+'	<div class="hotArea_l hotArea_l_disable">'
			+'		<div class="currentPage_l"></div>'
			+'	</div>'
			+'	<div class="changeSize_l"></div>'
			+'	<div class="changeSize_r"></div>'
			+'	<div class="hotArea_r hotArea_r_active">'
			+'		<div class="currentPage_r"></div>'
			+'	</div>'
			+'</div>'
	,
	init: function() {   
		var me=this;
		
		this.pageWrapEl.innerHTML+=this.template.replace(/\{id\}/g,this.id);
		
		//把图片添加到页面内，以作缓存
		var imgsWrapEl=document.createElement('div');
		imgsWrapEl.style.display='none';
		var imgsHtml=[];
		for(var i=0;i<this.pagesData.length;i++){
			imgsHtml.push('<img src="'+this.pagesData[i].image+'" />');
		}
		imgsWrapEl.innerHTML=imgsHtml.join('');
		document.body.appendChild(imgsWrapEl);
		
		this.pagesData.unshift({image:this.blankGif});
		if(this.pagesData.length%2!=0){
			this.pagesData.push({image:this.blankGif});
		}
		
		//底层，页面层
		this.btmLayerEl=this.$pageWrapEl.find('div.page_btmLayer').get(0);
		
		//翻页效果层
		this.flipLayerEl=this.$pageWrapEl.find('div.page_flipLayer').get(0);
		
		//感应点击的热区层
		this.hotLayerEl=this.$pageWrapEl.find('div.page_hotLayer').get(0);
		
		
		var $btm_divList=$('div',this.btmLayerEl);
		var $btm_imgList=$('img',this.btmLayerEl);
		
		//左边的播放器
		this.playerWrapEl_l=$btm_divList.get(0);
		this.player_l=document[this.id+'_player_l'] || document.getElementById(this.id+'_player_l');
		//左边的页面Div
		this.pageCtEl_l=$btm_divList.get(1);
		//右边的播放器，只用一个播放器了
		this.playerWrapEl_r=$btm_divList.get(2);
		//this.player_r=document[this.id+'_player_r'] || document.getElementById(this.id+'_player_r');
		//右边的页面Div
		this.pageCtEl_r=$btm_divList.get(3);
		//左边的页面图片
		this.pageImageEl_l=$btm_imgList.get(0);
		//右边的页面图片
		this.pageImageEl_r=$btm_imgList.get(1);
		
		
		this.pageImageEl_l.src=this.pagesData[0].image;
		this.pageImageEl_r.src=this.pagesData[1].image;
		this.pageWidth=this.btmLayerEl.clientWidth/2; 
		this.pageHeight=this.btmLayerEl.clientHeight; 
		this.pageImageEl_l.width=this.pageImageEl_r.width=this.pageWidth;
		this.pageImageEl_l.height=this.pageImageEl_r.height=this.pageHeight;
		
		var $hot_divList=$(this.hotLayerEl);
		this.$hotArea_l=$hot_divList.find('div.hotArea_l');
		this.$hotArea_r=$hot_divList.find('div.hotArea_r');
		this.changeSize_l=$hot_divList.find('div.changeSize_l').get(0);
		this.changeSize_r=$hot_divList.find('div.changeSize_r').get(0);
		this.currentPage_l=$hot_divList.find('div.currentPage_l').get(0);
		this.currentPage_r=$hot_divList.find('div.currentPage_r').get(0);
		this.currentPage_l.innerHTML="";   
		this.currentPage_r.innerHTML=1;

		this.doZoom(); // 全屏阅读
		this.changeSize_l.style.cursor='';
		this.$hotArea_l.on("click",function(){me.flipToPrevPage();});
		this.$hotArea_r.on("click",function(){me.flipToNextPage();});
		$.event.add(this.hotLayerEl,"mousewheel",function(evt){me.filpByMouseWheel(evt);});
		$.event.add(this.hotLayerEl,"DOMMouseScroll",function(evt){me.filpByMouseWheel(evt);});
		$.event.add(document,"keydown",function(evt){me.flipByDirKey(evt);});
		
		// 添加全局方法：返回当前要全屏查看的图片的地址（为flash全屏提供方法接口）
		window['getLeftPictureUrl_'+this.id]=function(){
			if(me.currentIndex>0){
				return me.pageImageEl_l.src;
			}
			return '';
		}
		window['getRightPictureUrl_'+this.id]=function(){
			if(me.currentIndex<me.pagesData.length){
				return me.pageImageEl_r.src;
			}
			return '';
		}
	},
	flipToFirstPage: function(){ // 翻到封面
		this.seekToTargetPage(1);
	},
	flipToNotePage: function(){ // 翻到目录页
		this.seekToTargetPage(2);
	},
	flipToLastPage: function(){ // 翻到封底
		this.seekToTargetPage(this.pagesData.length-1);
	},
	flipToPrevPage: function() { //翻到上一页
		this.seekToTargetPage(this.currentIndex-2);
	},
	flipToNextPage: function() { //翻到下一页
		this.seekToTargetPage(this.currentIndex+2);
	},
	seekToTargetPage: function(targetIndex) { // 翻页通用方法
		if(targetIndex<0){
			targetIndex=1;
		}
		if(targetIndex>=this.pagesData.length){
			targetIndex=this.pagesData.length-1;
		}
		// 总是从奇数页翻到奇数页
		if(targetIndex%2==0){
			targetIndex++;
		}
		if(targetIndex==this.currentIndex){
			return;
		}
		if(!this.pagesData[targetIndex-1].audio && !this.pagesData[targetIndex].audio){
			this.stopAudioInPage();
		}
		this._doFlip(this.currentIndex,targetIndex);
		this.playAudioInPage(targetIndex); //播放指定页的音乐
		
		// 翻页操作时，首末广告页需隐藏object标签
		if(this.currentIndex==1){
			$("#leftPicture").css('display','none');
		}else if(this.currentIndex==this.pagesData.length-1){
			$("#rightPicture").css('display','none');
		}else{
			$("#leftPicture").css('display','block');
			$("#rightPicture").css('display','block');
		}
	},
	_doFlip:function(currentIndex,targetIndex){
		var me=this;
		//从左向右翻，从右向左翻都由此方法负责
		//使用变量配置，来减少if语句的使用
		var dirs={
			'r2l':{
				updateNumberEl_begin:this.currentPage_r,//最开始要被更新页码的元素
				updateNumberEl_end:this.currentPage_l,//翻完要被更新页码的元素
				pageFrontEl:this.pageCtEl_r,//要被翻的页
				pageBack_Index:targetIndex-1,//被翻的页的背面页码
				underPage_imgEl:this.pageImageEl_r,//被翻的页的图
				underPage_Index:targetIndex,//被翻的页的下面露出的页码
				secondHalf_class:"pageLayer_l",//翻过中场后，添加的div样式
				$hotArea:this.$hotArea_l, //翻完了那边的热区肯定能点了
				$hotAreaActive_class:"hotArea_l_active",
				$hotAreaDisable_class:"hotArea_l_disable",
				toBeChange_imgEl:this.pageImageEl_l //要更新的图
			},
			'l2r':{
				updateNumberEl_begin:this.currentPage_l,
				updateNumberEl_end:this.currentPage_r,
				pageFrontEl:this.pageCtEl_l,
				pageBack_Index:targetIndex,
				underPage_imgEl:this.pageImageEl_l,
				underPage_Index:targetIndex-1,
				secondHalf_class:"pageLayer_r",
				$hotArea:this.$hotArea_r,		
				$hotAreaActive_class:"hotArea_r_active",
				$hotAreaDisable_class:"hotArea_r_disable",
				toBeChange_imgEl:this.pageImageEl_r
			}
		}
		var direction=targetIndex>currentIndex?'r2l':'l2r';// 往左翻 还是往右翻
		var oo=dirs[direction];
		if(this._fliping_direction && this._fliping_direction != direction){
			return;//正在向某方向进行翻页效果时不进行反方向的翻页
		}
		//克隆要翻动的页面(包括其中的图片)
		var cloneDiv,cloneImg,cDiv2,cImg2;
		//动画结束
		var _doFlip_end=function(){
			oo.updateNumberEl_end.innerHTML=oo.pageBack_Index;
			oo.$hotArea.removeClass(oo.$hotAreaDisable_class).addClass(oo.$hotAreaActive_class); 
			oo.toBeChange_imgEl.src=cImg2.src;
			me.flipLayerEl.removeChild(cDiv2);
			if(me.flipLayerEl.childNodes.length==0){ // 当翻页层中没有图片元素时
				me._fliping_direction=false;
			}
		};
		
		// 翻过中场后
		var _doFlip_secondHalf=function(){

			// 翻页层删除当前页，并插入当前页的上一页或下一页，翻页层回调动画函数
			me.flipLayerEl.removeChild(cloneDiv);// 删除翻页层中的页面
			cDiv2=document.createElement('div'); 
			cImg2=document.createElement('img');
			cDiv2.className=oo.secondHalf_class;
			cImg2.src=me.pagesData[oo.pageBack_Index].image;
			cImg2.width = 0;
			cImg2.height = me.pageHeight;
			cDiv2.appendChild(cImg2); // 将创建图片对象追加到新创建的页面中
			me.flipLayerEl.appendChild(cDiv2);// 将新创建的页面追加到翻页层
			
			// 翻页层图片执行动画函数
			Animator.size(cImg2,{width:me.pageWidth},null,cImg2.offsetWidth < me.pageWidth?'easeOut':'easeIn',_doFlip_end);

		};
		
		//前半场
		var _doFlip_firstHalf=function(){
			//克隆要翻动的页面(包括其中的图片)
			cloneDiv=oo.pageFrontEl.cloneNode(true); 
			cloneImg=cloneDiv.children[0];
			cloneDiv.style.zIndex = me.zIndex;
			me.zIndex--;
			me.flipLayerEl.appendChild(cloneDiv);// 移动底层页面到翻页层
			oo.underPage_imgEl.src=me.pagesData[oo.underPage_Index].image;
			//开始走动画
			Animator.size(cloneImg, {width:0},null,cloneImg.offsetWidth < me.pageWidth?'easeOut':'easeIn',_doFlip_secondHalf);
		};
		
		//翻页前
		var _doFlip_begin=function(){
			me.currentIndex=targetIndex;
			me.fireEvent('beforeflip',{targetIndex:targetIndex});
			me._fliping_direction=direction;
			oo.updateNumberEl_begin.innerHTML=oo.underPage_Index;
			if(targetIndex==me.pagesData.length-1){
				me.$hotArea_r.removeClass("hotArea_r_active").addClass("hotArea_r_disable"); 
				me.changeSize_r.style.cursor=''
				me.currentPage_r.innerHTML="";
			}else{
				Html5FullScreen.zoomInIcon(me.changeSize_r);
			}
			
			if(targetIndex==1){
				me.$hotArea_l.removeClass("hotArea_l_active").addClass("hotArea_l_disable"); 
				me.changeSize_l.style.cursor=''
				me.currentPage_l.innerHTML="";
			}else{
				Html5FullScreen.zoomInIcon(me.changeSize_l); 
			}
			_doFlip_firstHalf();
		};
		_doFlip_begin();
	},
	playAudioInPage:function(pageIndex){
		if(pageIndex%2==1){
			pageIndex--;
		}
		var audioSrc1=this.pagesData[pageIndex].audio;
		var audioSrc2=this.pagesData[pageIndex].audio;
		var playList=[];
		if(audioSrc1 || audioSrc2){
			if(audioSrc1){
				playList.push({file: audioSrc1});
			}
			if(audioSrc2){
				playList.push({file: audioSrc2});
			}
			//this.playerWrapEl_l.style.visibility='visible';
			if(/file:\/\//.test(location.href)){
				Tip('只有通过http协议访问本页面，音频才能正常播放',2);
				return;
			}
			if(this.player_l.jwLoad){
				this.player_l.jwLoad(playList);
				this.player_l.jwPlay();
			}
		}
	
	},
	stopAudioInPage:function(){
		if(/file:\/\//.test(location.href)){
			Tip('只有通过http协议访问本页面，音频才能正常播放',2);
			return;
		}
		if(this.player_l.jwStop){
			this.player_l.jwStop();
		}
		this.playerWrapEl_l.style.visibility='hidden';
	},
	filpByMouseWheel: function(evt){ //鼠标滚轮事件
		var evt=$.event.fix(evt);
		evt.preventDefault();
		var flag=evt.wheelDelta; // 返回正负120
		if(flag > 0){
			this.flipToPrevPage();
		}else{
			this.flipToNextPage();
		}
	},
	flipByDirKey: function(evt){ // 键盘方向键执行翻页
		var evt=$.event.fix(evt);
		if (evt.keyCode==39){
			this.flipToNextPage();
		}else if(evt.keyCode==37){
			this.flipToPrevPage();
		}
	},
	flipByCurrentPos: function(currPos){
		this.movePos=this.limitedVal(currPos);
		var targetIndex = Math.round(this.movePos/this.navbarJoint_width*2);
		this.seekToTargetPage(targetIndex);	
	},
	limitedVal: function(val){ //滚动条极值
		if(typeof val!='number'){
			return;
		}
		return (val = Math.min(this.maxPos,Math.max(0,val))); 
	},
	html5FullScreen: function(){ // html5全屏方法
		var me = this;
		Html5FullScreen.init();
		if (Html5FullScreen.supportsFullScreen) {
			Html5FullScreen.zoomInIcon(this.changeSize_l); 
			Html5FullScreen.zoomInIcon(this.changeSize_r);
			var zoomHandler=function(){
				// 广告页禁止进入全屏
				if(me.currentIndex==1 && $(this).hasClass('changeSize_l') || me.currentIndex==me.pagesData.length -1 && $(this).hasClass('changeSize_r')){
					return;
				}
				var zoomImgSrc=$(this).hasClass('changeSize_r')?me.pageImageEl_r.src:me.pageImageEl_l.src; // 获取底层图片的路径
				if (Html5FullScreen.isFullScreen()){ // 如果是全屏，就退出全屏
					Html5FullScreen.zoomInIcon(this);
					Html5FullScreen.cancelFullScreen(document);
					me.playerWrapEl_l.style.visibility='visible';
					this.style.marginTop = 40 + 'px';
					this.innerHTML='';
				}else { // 否则进入全屏
					Html5FullScreen.zoomOutIcon(this);
					Html5FullScreen.requestFullScreen(this);
					me.playerWrapEl_l.style.visibility='hidden'; // 兼容chrome下 全屏后object标签不能被遮挡
					this.style.marginTop = 0;  // 兼容chrome下全屏后外边距依然存在
					var zoomImg=document.createElement('img');
					zoomImg.src=zoomImgSrc;
					this.appendChild(zoomImg);
				}      
			};
			var zoomKey = function(){
				//console.log(document.mozFullScreenElement) 
				var _this = $(me.hotLayerEl).find("img").get(0).parentNode;
				if (!Html5FullScreen.isFullScreen()) { 
					Html5FullScreen.zoomInIcon(_this);
					Html5FullScreen.cancelFullScreen(document);
					//me.playerWrapEl_l.style.visibility='visible';
					//_this.style.marginTop = 40 + 'px';
					_this.innerHTML='';
				}
			};
			$.event.add(document,Html5FullScreen.fullscreenchange(),zoomKey); // ESC键退出全屏，监听全屏改变事件
			$.event.add(this.changeSize_l,"click",zoomHandler);
			$.event.add(this.changeSize_r,"click",zoomHandler);
		}
	},           
	flashFullScreen: function(){
		this.changeSize_l.innerHTML = '<object id="leftPicture" style="display:none;" width="420" height="552" type="application/x-shockwave-flash" data="../images/fullscreen.swf"><param name="movie" value="../images/fullscreen.swf"><param name="src" value="../images/fullscreen.swf"><param name="quality" value="high"><param name="allowscriptaccess" value="always"><param name="allowfullscreen" value="true"><param name="allownetworking" value="all"><param name="wmode" value="transparent"><param name="scale" value="noborder"><param name="flashvars" value="jscall=getLeftPictureUrl_'+ this.id + '"></object>';
		this.changeSize_r.innerHTML = '<object id="rightPicture" width="420" height="552" type="application/x-shockwave-flash" data="../images/fullscreen.swf"><param name="movie" value="../images/fullscreen.swf"><param name="src" value="../images/fullscreen.swf"><param name="quality" value="high"><param name="allowscriptaccess" value="always"><param name="allowfullscreen" value="true"><param name="allownetworking" value="all"><param name="wmode" value="transparent"><param name="scale" value="noborder"><param name="flashvars" value="jscall=getRightPictureUrl_'+ this.id + '"></object>';
	},
	doZoom: function(){
		if($.browser.msie){
			var swfObj = new ActiveXObject('ShockwaveFlash.ShockwaveFlash');
			if(!swfObj){
				Tip('您还未安装浏览器flash player插件，请下载安装',2);
				this.html5FullScreen();
			}
			this.flashFullScreen();
		}else{
			var _swfObj = navigator.plugins['Shockwave Flash'];
			if(!_swfObj){
				Tip('您还未安装浏览器flash player插件，请下载安装',2);
				this.html5FullScreen();
			}
			this.flashFullScreen();
		}
	}
});

BookReader=function(opts){
	this.pageFlip = new PageFlip(opts); // 杂志主页面初始化
	this.pagesNavigationInit(); // 杂志翻页条初始化
};
BookReader.prototype={
	pagesNavigationInit: function(){
		var me = this;
		var pageFlip =this.pageFlip;
		// 创建翻页导航条
		var navigationLayer= document.createElement('div');
		navigationLayer.className='page_navigationLayer';
		navigationLayer.innerHTML='<div class="scrollBar"></div><div class="slider"></div>';
		pageFlip.pageWrapEl.appendChild(navigationLayer);
		//翻页导航条
		pageFlip.scrollBarEl=pageFlip.$pageWrapEl.find('div.scrollBar').get(0);
		pageFlip.sliderEl=pageFlip.$pageWrapEl.find('div.slider').get(0);
		pageFlip.scrollBarEl.style.width=pageFlip.btmLayerEl.clientWidth-20+'px';
		pageFlip.navbarJoint_width=Math.round(pageFlip.scrollBarEl.clientWidth/pageFlip.pagesData.length)*2;
		pageFlip.sliderEl.style.width=pageFlip.navbarJoint_width+'px';
		pageFlip.maxPos=pageFlip.scrollBarEl.clientWidth-pageFlip.sliderEl.clientWidth;
		var arr=[];
		for(var i=1, len=pageFlip.pagesData.length; i<len; i++){
			var oSpan=document.createElement('span');
			arr.push(oSpan);
			if(i%2==0){
				oSpan.className='scale';
				oSpan.style.left=pageFlip.navbarJoint_width*(i/2)+'px';
			}else{
				oSpan.className='range';
				oSpan.style.left= pageFlip.navbarJoint_width*((i-1)/2)+'px';
				oSpan.style.width= pageFlip.navbarJoint_width +'px';
				
			}
			pageFlip.scrollBarEl.appendChild(oSpan);
		}

		// 创建缩略图层
		var oWrap,oImgWrap,oIndexWrap,oImg_left,oImg_right,oIndex_left,oIndex_right;
		oImg_left=document.createElement("img");
		oImg_right=document.createElement("img");
		oIndex_left=document.createElement("span");
		oIndex_right=document.createElement("span");
		oImgWrap=document.createElement("div");
		oIndexWrap=document.createElement("div");
		oWrap=document.createElement("div");
		oImgWrap.className="smallPicLayer";
		oIndexWrap.className="smallPicIndexLayer";
		oWrap.className="smallPicLayerWrap";
		oImgWrap.appendChild(oImg_left);
		oImgWrap.appendChild(oImg_right);
		oIndexWrap.appendChild(oIndex_left);
		oIndexWrap.appendChild(oIndex_right);
		oWrap.appendChild(oImgWrap);
		oWrap.appendChild(oIndexWrap);
		pageFlip.scrollBarEl.parentNode.parentNode.appendChild(oWrap);
		var $spanNodes=$('span.range',pageFlip.scrollBarEl);
		$spanNodes.each(function(i){
			this._index=i;
			this.onmouseover=function(){
				var n=this._index;
				var l=n*2,r=n*2+1;
				oImg_left.src=pageFlip.pagesData[n*2].image;
				oIndex_left.innerHTML=n*2;
				oIndex_left.style.display="inline-block";
				oImg_right.src=pageFlip.pagesData[n*2+1].image;
				oIndex_right.innerHTML=n*2+1;
				oIndex_right.style.display="inline-block";
				oImg_left.style.display="";
				oImg_right.style.display="";
				oWrap.style.display='block';
				oWrap.style.width = oImg_left.clientWidth + oImg_right.clientWidth + 8 + 'px';
				if(l==0){
					oImg_left.style.display="none";
					oIndex_left.style.display="none";
					oWrap.style.width = oImg_right.clientWidth + 8 + 'px';
				}
				if(r==pageFlip.pagesData.length-1){
					oImg_right.style.display="none";
					oIndex_right.style.display="none";
					oWrap.style.width = oImg_left.clientWidth + 8 + 'px';
				}
				
				if(this.clientWidth>oWrap.clientWidth){
					oWrap.style.left=Math.round(this.offsetLeft+(this.clientWidth-oWrap.clientWidth)/2)+ 100 + 'px';
				}else{
					oWrap.style.left=Math.round(this.offsetLeft-(oWrap.clientWidth-this.clientWidth)/2)+ 100 + 'px';
				}
				
			};
			this.onmouseout=function(){
				oWrap.style.display='none';
			};
			this.onclick=function(evt){ // 点击翻页
				var evt=$.event.fix(evt);
				pageFlip.movePos=this.offsetLeft;
				pageFlip.flipByCurrentPos(pageFlip.movePos);

			};
		});
		
		// 注册事件
		$.event.add(pageFlip.sliderEl,"mousedown",function(evt){me.filpBySilderMove(evt);});

		pageFlip.addEvent('beforeflip',function(evtData){// 根据当前页码滚动滚动条的位置
			var targetIndex=evtData.targetIndex;
			if(targetIndex%2==1){ // 翻页通用方法中当前页的页码为永远是奇数（1、3、5...）
				targetIndex-=1;
			}
			var targetPos = Math.round(targetIndex*this.navbarJoint_width/2);
			Animator.move(this.sliderEl,{left:targetPos});
		});
	},
	filpBySilderMove: function(evt){ // 滑块滑动翻页
		var pageFlip=this.pageFlip;
		var evt=$.event.fix(evt);
		var startClientX=evt.clientX;
		var startSliderElX=pageFlip.sliderEl.offsetLeft;
		document.onmousemove=function (evt){
			var evt=$.event.fix(evt);
			var distance=evt.clientX-startClientX;//已经移动过的距离
			pageFlip.movePos=startSliderElX+distance;
			pageFlip.movePos = pageFlip.limitedVal(pageFlip.movePos);
			var targetIndex=Math.round(pageFlip.movePos/pageFlip.navbarJoint_width);
			var targetPos=Math.round(targetIndex*pageFlip.navbarJoint_width);
			if(pageFlip.sliderEl.offsetLeft != targetPos){
				pageFlip.sliderEl.style.left=targetPos+"px";
			}
		};		
		document.onmouseup = function (){
			document.onmousemove=null;
			document.onmouseup=null;
			pageFlip.flipByCurrentPos(pageFlip.movePos);
		};
	}, 
	adjustMovePos: function(currPos){ // 根据当前移到的位置调整到目标位置
		var pageFlip=this.pageFlip;
		pageFlip.movePos=pageFlip.limitedVal(currPos);
		var scrollStep=0;
		if(pageFlip.movePos < (pageFlip.sliderEl.offsetLeft-pageFlip.navbarJoint_width/2)){
			scrollStep=-this.navbarJoint_width;
		}else if(pageFlip.movePos >= (pageFlip.sliderEl.offsetLeft+pageFlip.navbarJoint_width/2)){
			scrollStep=pageFlip.navbarJoint_width;
		}
		return (pageFlip.movePos=pageFlip.sliderEl.offsetLeft+scrollStep);
	}
}
function playerReady() {//播放器初始化后隐藏之
	$('div.playerWrap_l').css('visibility','hidden');//IE下jwplayer的初始化需要其为可见
};
document.onselectstart=function(){
	return false;
};