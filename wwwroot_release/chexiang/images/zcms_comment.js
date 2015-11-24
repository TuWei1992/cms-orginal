﻿﻿if(!window.localsForComment){
	throw "不存在变量variablesForComment";
}
var	cmtConfig={
	elIds:{		
	
	}
};
//Comment命名空间
var Comment=(function(cmtConfig){

	var clicked = false;
	//----start module全局变量,初始化后只读----//
	var config={
			suffix:'_reply',
			elIds:{		
	
				textarea:'cmt_content',	
				counter:'cmt_content_count',
				
				checkbox:'cmt_CmntCheckbox',
				submitBtn:'cmtSubmit',
				
				loginContainer:'cmt_Login',
				username:'cmt_uname',
				password:'cmt_pass',
				authCode:'cmt_AuthCode',
				authImg:'AuthCodeImg',		
				loginBtn:'cmtfrmbutton',
				
				commentCount:'cmt_commtotal',
				personCount:'cmt_commusertotal',
				
				//logoutBtn:'',
				registerBtn:'cmtReg',	
			
				parent:'cmt_ParentID',			
				isNeedLogin:'cmt_IsNeedLogin',
				isLogin:'cmt_IsLogin',
				
				score:'cmt_ContentScore',
				checkbox:'cmt_CmntCheckbox'
			}
	},
	elIds=config.elIds,
	suffix;
	if(typeof cmtConfig==='object'&& typeof cmtConfig.elIds==='object'){
		for(var key in cmtConfig.elIds){
			if(cmtConfig.elIds.hasOwnProperty(key)){
				elIds[key]=cmtConfig.elIds[key];
			}
		}
	}
	
	suffix=config.suffix;
	//----end module全局变量(elIds,suffix),初始化后只读----//
	
	//星级评定
	var $ele=function (id) {
		return typeof id == 'string'?document.getElementById(id):id;
	};
	
	function Rating(el,val){
		var self=this;
		this.hiddenInput= document.getElementById("cmt_ContentScore");
		this.viewRating=$ele(el).getElementsByTagName('LI')[0];
		this.links=$ele(el).getElementsByTagName('A');
		this.starWidth=this.links[0].offsetWidth;
		for(var i=0,len=this.links.length; i<len; i++){
			this.links[i].value=i+1;
			this.links[i].onclick=function(){
				self.setRate(this.value);
			};
			this.links[i].onmouseover=function(){
				for(var a=0; a < this.value;a++){
					self.links[a].className=self.links[a].className.replace(/ hcolor/g,"");
					self.links[a].className+=" hcolor";
				}
				for(var a=this.value; a < 5;a++){
					self.links[a].className=self.links[a].className.replace(/ hcolor/g,"");
				}
			};
			this.links[i].onmouseout=function(){
				for(var a=0; a < this.value;a++){
					self.links[a].className=self.links[a].className.replace(/ hcolor/g,"");
				}
			};
		}
		if(val == undefined){
			this.setRate(this.hiddenInput.value);
		}else{
			this.setRate(val);
		}
	}
	Rating.prototype={
		setRate:function(val){
			this.hiddenInput.value=val;
			this.viewRating.style.width=val*this.starWidth+0+'px';
			if( this.hiddenInput.value != 0){
				for(var a=0;a<this.hiddenInput.value;a++){
					this.links[a].className=this.links[a].className.replace(/ select/g,"");
					this.links[a].className+=" select";
				}
				for(var a=this.hiddenInput.value; a < 5;a++){
					this.links[a].className=this.links[a].className.replace(/ select/g,"");
				}
			}
		},
		getRate:function(){
			return this.hiddenInput.value;
		}
	}
	////
	CommCountData={},//评论人数和评论数量时时抓取
	jsonpData={}, //${commentServicesUrl}返回的js语句应该改写该全局变量
	CommentCommitData={};//评论提交后返回结果数据
	
	//避免使用main.js中的方法，使用独立的方法
	var $G=function(id){
		if(typeof id !=='string'){return id;};
		id=id.replace(/^#+/,'');
		return document.getElementById(id);
	};
	var $V=function(id){id=id.replace(/^#+/,'');var el=document.getElementById(id); return el.type!=='checkbox'?el.value:el.checked;};
	//框架中字符串的扩展方法tmpl
	var applyTmpl=function(str, obj, st, urlencode) {
			return str.replace(/\{([\w_$]+)\}/g, function(c, $1) {
				var a = obj[$1];
				if (a === undefined || a === null) {
					if (st === undefined || st === null) {
						return '';
					}
					switch (st) {
					case 0:
						return '';
					case 1:
						return $1;
					default:
						return c;
					}
				}
				if ('[object Function]' == Object.prototype.toString.call(a)) {
					a = a($1);
				}
				return urlencode ? encodeURIComponent(a) : a;
			});
	};
	var parseDate=function(str) { //解析形如yyyy-MM-dd hh:mm:ss的日期字符串为Date对象
			if (!str) {
				alert("dateTime.js # parseDate : 没有传入参数！");
				return null;
			}
			var regex = /^(\d{4})-(\d{1,2})-(\d{1,2})((?:\s|\xa0)(\d{1,2}):(\d{1,2})(:(\d{1,2})(\.\d{1,2})?)?)?$/;
			if (!regex.test(str)) {
				alert("dateTime.js # parseDate : 参数错误 " + str);
				return false;
			}
			regex.exec(str);
			var y = RegExp.$1;
			var M = RegExp.$2;
			var d = RegExp.$3;
			var h = RegExp.$5;
			var m = RegExp.$6;
			var s = RegExp.$8;
			var date = new Date(y, M - 1, d);
			if (!h) {
				h = 0;
				m = 0;
			}
			date.setHours(h);
			date.setMinutes(m);
			if (!s) {
				s = 0;
			}
			date.setSeconds(s);
			return date;
	};
	/**
	* 加载jsonp脚本
	* @method loadJsonp
	* @static
	* @param { String } url Javascript文件路径
	* @param { Function } onsuccess (Optional) jsonp的回调函数
	* @param { Option } options (Optional) 配置选项，目前除支持loadJs对应的参数外，还支持：
	{RegExp} callbackReplacer (Optional) 回调函数的匹配正则。默认是：/%callbackfun%/ig；如果url里没找到匹配，则会添加“callback=%callbackfun%”在url后面
	*/
	var loadJsonp = function(url, onsuccess, options) {
		var seq = +(new Date());
		options=options||{};
		var funName = "_jsonpcallback" + seq++,
		callbackReplacer = options.callbackReplacer || /%callbackfun%/ig;
		window[funName] = function(data) {
		if (onsuccess) {
		onsuccess(data);
		}
		window[funName] = null;
		};
		if (callbackReplacer.test(url)) {
		url = url.replace(callbackReplacer, funName);
		} else {
		url += (/\?/.test(url) ? "&" : "?") + "callback=" + funName;
		}
		loadJs(url, null);
	};
	
	var loadJS,loadJs;
	loadJS=loadJs=function (url,onsuccess){
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
				//head.removeChild(script);
			}
		};
		head.appendChild(script);
	}
	
	var Event=window.Event||{};
	Event.addEventListener=function(el,type,listener){
		if(document.addEventListener){
			el.addEventListener(type,listener);
		}else if(document.attachEvent){
			el.attachEvent('on'+type,listener);
		}
	}
	Event.removeEventListener=function(el,type,listener){
		if(document.removeEventListener){
			el.removeEventListener(type,listener);
		}else if(document.attachEvent){
			el.detachEvent('on'+type,listener);
		}
	}
	Array.prototype.forEach=Array.prototype.forEach||function(fn){
		for(var i=0;i<this.length;i++){
			fn(this[i],i,this);
		}
	};
	var Cookie = window.Cookie || {};
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
	
	var JSON=window.JSON||{};
	if(!JSON.parse){
		JSON.parse=function(json){return eval('('+json+')')}
	}
	
	if(window.XMLHttpRequest===undefined){
		window.XMLHttpRequest=function(){
			try{
				return new ActiveXObject('Msxml2.XMLHTTP.6.0');
			}catch(e1){
				try{
					return new ActiveXObject('Msxml2.XMLHTTP.3.0');
				}catch(e2){
					throw new Error('XMLHttpRequest is not supported.')
				}
			}
		}
	}
	
	var $=window.$||{};
	$.ajax=$.ajax||(function(opt){//此处简单处理
		if(opt.dataType==='jsonp'){
			loadJS(opt.url,opt.onsuccess);
			return;
		}
		var request=new XMLHttpRequest();
		request.open(opt.type.toUpperCase(),opt.url)
		request.onreadystatechange=function(){
			if(request.readyState===4&& request.status===200){
				if(opt.onsuccess){
					opt.onsuccess(request.responseText);
				}
			}
		}
		request.send(opt.data||null);
	});
	
	function appendReply(){
		var loginContainer = document.getElementById(elIds.loginContainer);
		if(loginContainer){
			loginContainer.innerHTML = localsForComment.LoginForm;
			document.getElementById(elIds.isLogin).value="false";
		}
		var commentLoginBoxReply=document.getElementById('commentLoginBox_reply'),
		commentLoginBox=document.getElementById('commentLoginBox');
		if(commentLoginBoxReply){//如果页面中有用于回复的评论块，则进行替换操作
			var ids=[];
			for(var key in elIds){
				if(elIds.hasOwnProperty(key)){
					ids.push(elIds[key]);
				}
			}
			//IE中innerHTML返回的元素的属性值可能没有在双引号中。
			//由于一些元素的id可能为另一些id的前缀，所以为了尽可能多的匹配，长的id放前面优先匹配。
			var reg=new RegExp('(\\s+id=([\'"]|))('+ids.sort(function(a,b){return b.length-a.length;}).join('|')+')(\\2)','gi');
			commentLoginBoxReply.innerHTML=commentLoginBox.innerHTML.replace(reg,function($0,$1,$2,$3,$4){
				return $1+$3+suffix+$4;
			});
		};
	}
	
	function showLoginForm(){
		appendReply();
		//登录，注册事件处理
		var loginBtns=[elIds.loginBtn,elIds.loginBtn+suffix];
		loginBtns.forEach(function(id){
			var el=$G(id);
			if(!el){return};
			Event.addEventListener(el,'click',commentLogin);
		});
		var registerBtns=[elIds.registerBtn,elIds.registerBtn+suffix];
		registerBtns.forEach(function(id){
			var el=$G(id);
			if(!el){return};
			Event.addEventListener(el,'click',register);
		});
	}
	
	function showLogined(userName){
		appendReply();
		var loginBtns=[elIds.loginBtn,elIds.loginBtn+suffix];
		loginBtns.forEach(function(id){
			var el=$G(id);
			if(!el){return};
			Event.removeEventListener(el,'click',commentLogin);
		});
		var registerBtns=[elIds.registerBtn,elIds.registerBtn+suffix];
		registerBtns.forEach(function(id){
			var el=$G(id);
			if(!el){return};
			Event.removeEventListener(el,'click',register);
		});
		var loginContainer = document.getElementById(elIds.loginContainer),
		loginContainer_reply=document.getElementById(elIds.loginContainer+suffix);
		var html = '<span id="spanLogout" style="float:right;margin-bottom:6px;margin-right:5px;">' + 
		'<a href="'+ localsForComment.FrontAppContext +'member/info?SiteID='+ localsForComment.SiteID +'&Current=PersonalInfo" target="_blank">' + 
		userName + '</a>&nbsp;|&nbsp;<a href="#;" onclick="Comment.commentLogout();return false;">' + 
		localsForComment.Application_Logout+'</a>';
		//底部评论框显示的处理			
		if(loginContainer){
			loginContainer.innerHTML = html;
			document.getElementById(elIds.isLogin).value="true";
		}
		//回复评论框的显示处理
		if(loginContainer_reply){
			loginContainer_reply.innerHTML = html;
			document.getElementById(elIds.isLogin+suffix).value="true";
		}
	}
	
	function getCommentCount(){
		var url =localsForComment.FrontAppContext+'comment/count?contentid='+localsForComment.ContentID+'&timestamp='+new Date().getTime();
		loadJs(url, function initCommentCount(){
			document.getElementById(elIds.personCount).innerHTML = CommCountData.PersonTotal||0 ;
			document.getElementById(elIds.commentCount).innerHTML = CommCountData.CommentTotal||0;	
		});
	}
	/////
	//页面加载之后进行显示的处理(登录和非登录用户的显示不同)
	function initCommentMemberInfo(){
		var cBeginTime = localsForComment.CommentStartTime||'',
			cEndTime = localsForComment.CommentEndTime||'',
			now=new Date().getTime();
			
	  	cBeginTime=cBeginTime.length ? parseDate(cBeginTime).getTime() : Number.MIN_VALUE;
	  	cEndTime=cEndTime.length ? parseDate(cEndTime).getTime() : Number.MAX_VALUE;
	
		if(cBeginTime<=now && now<=cEndTime){
			$G("comment2").style.display = "block";
	    	$G("checkstar").style.display = "inline";
	    	$G("checkMood").style.display = "inline";
		}else{
			return;
		}
		getCommentCount();
		new Rating('rating1');
	
		var url = localsForComment.FrontAppContext+'member/logined?t='+new Date().getTime();
		loadJsonp(url, function(result){
			var isDisplayCommentButton = $G("isDisplay");
			isDisplayCommentButton.style.display = (localsForComment.isDisplayCommentButton=='true'||localsForComment.isDisplayCommentButton===true)?"":"none" 
			if(result.IsLogin){
				Cookie.set("ZVING_MEMBER_LOGIN_Name",result.UserName);
				showLogined(result.UserName);
			}else{
				Cookie.remove("ZVING_MEMBER_LOGIN_Name");
				showLoginForm();
			}
		});
		var authCodeImg=document.getElementById(elIds.authImg),
			authCodeImg_reply=document.getElementById(elIds.authImg+suffix),
			imgcodeurl=localsForComment.FrontAppContext+'authCode.zhtml?Height=21&Width=50&'+new Date().getTime();
		if(authCodeImg!=null){
			authCodeImg.style.background='url('+ imgcodeurl +') no-repeat center center';
		}
		if(authCodeImg_reply!=null){
			authCodeImg_reply.style.background='url('+ imgcodeurl +') no-repeat center center';
		}
	}
	
	//登录处理
	function commentLogin(event){
		var event=event||window.event,
			target=event.target||event.srcElement;
		
		var url=localsForComment.FrontAppContext+'member/ajaxlogin?SiteID='+localsForComment.SiteID+'&action=login&UserName=',
			usernameElId,
			passwordElId,
			authCodeElId,
			siteIDElId,
			isReply=target.id!==elIds.loginBtn;
		if(!isReply){//针对文章的评论容器中的登录处理
			usernameElId=elIds.username;
			passwordElId=elIds.password;
			authCodeElId=elIds.authCode;
		}else{//回复容器中的登录处理
			usernameElId=elIds.username+suffix;
			passwordElId=elIds.password+suffix;
			authCodeElId=elIds.authCode+suffix;
		}
		url+=document.getElementById(usernameElId).value;
		url+="&Password="+document.getElementById(passwordElId).value;
		var needAuthCode=localsForComment.Site_MemberLoginVerifyCodeEnable==='Y';
		if(needAuthCode){
			url+="&AuthCode=" + document.getElementById(authCodeElId).value;
		}
		url+="&t=" + new Date().getTime();
		loadJsonp(url,function(jsonpData){
			var isLogin = jsonpData.login;
			if(!isLogin){
				alert(jsonpData.error);
				//可能配置没有验证码
				if(needAuthCode){
					document.getElementById(authCodeElId).click();
				}
				return;
			}
			Cookie.set("ZVING_MEMBER_LOGIN_Name",jsonpData.userName);
			showLogined(jsonpData.userName);
		});
	}
	
	//退出处理
	function commentLogout(){
		var url = localsForComment.FrontAppContext+'member/ajaxlogout?action=logout' + '&t=' + new Date().getTime();
		loadJsonp(url,function(jsonpData){
			showLoginForm();
		}); 
	}
	
	//评论提交检查
	function formSubmit(event){
		var event=event||window.event,
			target=event.target||event.srcElement;
		var	isReply=target.id!==elIds.submitBtn,
			
			contentId=elIds.textarea,
			isNeedLoginId=elIds.isNeedLogin,
			isLoginId=elIds.isLogin,
			checkboxId=elIds.checkbox,		
	
			commtiurl =localsForComment.CommitUrl+'?ContentID='+localsForComment.ContentID+'&ContentType='+localsForComment.Catalog_ContentType+'&CatalogID='+localsForComment.CatalogID+'&SiteID='+localsForComment.SiteID,
	
			content,
			isNeedLogin,
			isLogin;
			
		if(isReply){
			contentId+=suffix;
			isNeedLoginId+=suffix;
			isLoginId+=suffix;
			checkboxId+=suffix;
		}
			
		isNeedLogin = document.getElementById(isNeedLoginId).value==="true"?true:false;
		if(isNeedLogin){
			isLogin = document.getElementById(isLoginId).value==="true"?true:false;
			if(!isLogin){
				alert(localsForComment.Comment_CommentLoginFirst);
				return false;
			}
		}
		content=document.getElementById(contentId);
		if(content.value.trim() == "" || !clicked && content.value.trim() ==localsForComment.Comment_CivilizedComments){
		   alert(localsForComment.Comment_CommentContentEmpty);
		   content.focus();
		   return false;
		}else if(String.getlength(content.value) >200){
			   alert(localsForComment.Comment_ContentMore);
			   return false;
		  }
		commtiurl = commtiurl +"&CmntContent="+ encodeURI(encodeURIComponent(content.value));
		commtiurl = commtiurl +"&CmntCheckbox="+ $V(checkboxId);
		if(isReply){
			commtiurl = commtiurl +"&ParentID="+ $V(elIds.parent+suffix);
		}else{
			commtiurl = commtiurl +"&ContentScore="+ $V(elIds.score);
		}
		loadJs(commtiurl,function commentCommitResult(){
			var result = CommentCommitData.result;
			if(!result){
				alert(CommentCommitData.message);
				return;
			}
			alert(CommentCommitData.message);
			$("#cmt_content").val("");
		}); 
	}
	
	/**
	 * 向评论列表中追加新评论
	 */
	function appendNewComment(newComment){
		var newCommentHtml="";
		newCommentHtml(mew)
		var commentArray=$("#commentListDiv > .textCon");
		var pageSize=$("#commentPageSize").val();
		if(!commentArray||!pageSize){
			return;
		}
		var firstComment=commentArray[0];
		var lastComment=commentArray[commentArray.length-1];
		newCommentHtml=firstComment.cloneNode(true);
		$(firstComment).before(newCommentHtml);
		if(pageSize<(commentArray.length+1)){
			$(lastComment).remove();
		}
	}
	
	//用户注册
	function register(){
		var url=localsForComment.FrontAppContext+'member/register?SiteID='+localsForComment.SiteID;
		var w = window.open(url,"_blank","");
		if(!w){
			alert(localsForComment.Contentcore_WindowBlocked) ;
			return ;
		}
	}
	
	var showresult=function(response){
		var monthmessage=response.json;
		var obj=JSON.parse(monthmessage);
		if(obj.Status==0){
			alert(obj.Message);
		}
		else if(obj&&obj.Status==1){
			initfaceVote();
		}
	};
	
	var submitMoodInit=function(obj){
		var  isNeedLogin = document.getElementById(elIds.isNeedLogin).value;
		if(isNeedLogin && "true"==isNeedLogin){
			var isLogin = document.getElementById(elIds.isLogin).value;
			if("true"!=isLogin){
				alert(localsForComment.Comment_CommentVoteFirstLogin);
				return false;
			}
		}
		  var cc=encodeURIComponent(obj.innerText);
		  var URL =localsForComment.FrontAppContext+'CommentFaceVote/add?SiteID='+localsForComment.SiteID+'&CmntCheckbox='+$V("#cmt_CmntCheckbox")+'&ContentID='+localsForComment.ContentID+'&CatalogID='+localsForComment.CatalogID+'&ContentType='+localsForComment.ContentType+'&VoteMood='+encodeURIComponent(obj.innerText)+'&jsaddvote=Comment.showresult';
		  $.ajax({
		        type:"post",
		        dataType:"jsonp",
		        url:URL 
		  });	
	};
	
	function initfaceVote(){
		  var URL = localsForComment.FrontAppContext+'comment/initFaceVote?SiteID='+localsForComment.SiteID+'&ContentID='+localsForComment.ContentID+'&CatalogID='+localsForComment.CatalogID+'&jsVotecallback=Comment.showVote';
		  $.ajax({
		        type:"post",
		        dataType:"jsonp",
		        url:URL 
		  });
	}
	
	var showVote=function(response){
		var monthmessage=response.json;
		var obj=JSON.parse(monthmessage);
		var data=obj.data;///data的数据结构:[{VoteResult:'',Percent:'',srcurl:'',VoteMood:''}...]
		var listvotehtml="";
		var tmp=[];
		
		tmp.push('<li>');
		tmp.push(	'<div style="display: block;" class="result">');
		tmp.push(		'<div class="ft">{voteResult}</div>');
		tmp.push(		'<div class="bar">');
		tmp.push(			'<div style="height:{percent}%;_visibility:{visibility};" class="bg"></div>');
		tmp.push(		'</div>');
		tmp.push(	'</div>');
		tmp.push(	'<div>');
		tmp.push(		'<a hidefocus href="#;" onClick="Comment.submitMoodInit(this);"><img src="{src}" width="24" height="24"/><br/>{voteMood}</a>');
		tmp.push(	'</div>');
		tmp.push('</li>');
		tmp=tmp.join('');
		
		var dataItem={};
		for(var j=0;j<data.length;j++ )
		{
			//适配
			dataItem = {
				voteResult:data[j].VoteResult,
				percent:data[j].Percent,			
				src:localsForComment.Prefix+data[j].srcurl,
				voteMood:data[j].VoteMood,
				visibility:parseFloat(data[j].Percent)?'visible':'hidden'
			};
			listvotehtml+=applyTmpl(tmp,(dataItem));
		}
		$G('ul_Moods').innerHTML=listvotehtml;
	};
	
	//点击评论列表中回复按钮调用的函数
	var reply=function(el){
		var id=el.getAttribute('data');
		document.getElementById(elIds.parent+suffix).value=id;
		var replyWrap=document.getElementById('reply_'+id);
		var replyBox=document.getElementById('replyBox');
		if(replyWrap.childNodes.length<1){
			replyWrap.appendChild(document.getElementById('replyBox'));
			replyBox.style.display='block';
		}else{
			replyBox.style.display=replyBox.style.display=='none'?'block':'none';
		}
	};
	//
	commsupportData = {};
	
	//处理对评论列表中的评论的"支持"、"反对"
	var commSupport = function(type,ID){
		var url =localsForComment.FrontAppContext+'comment/support?ID='+ID+"&Type="+type;
		loadJs(url,function commSuppSucss(){
			var result = commsupportData.CommSupport;
			alert(commsupportData.Message);
			if(result){
				var eleID = commsupportData.Span;
				var ele = document.getElementById(eleID);
				var value = ele.innerHTML;
				if(value){
					ele.innerHTML =new Number(value) +1;
				}else{
					ele.innerHTML = 1;
				}
			}
		});
	}
	
	var initOnLoad=function(){
		initfaceVote();
		//隐藏回复
		var commentLoginBoxReply=document.getElementById('commentLoginBox_reply');
		if(commentLoginBoxReply){
			document.getElementById('replyBox').style.display='none';
		}
		//文本域事件处理
		var textareaIds=[elIds.textarea,elIds.textarea+suffix];
		textareaIds.forEach(function(id){	
			var el=$G(id);
			if(!el){return};
			Event.addEventListener(el,'keyup',function(){
				var len = String.getlength(el.value);
				var counterEl=document.getElementById(el.id===elIds.textarea ? elIds.counter : elIds.counter+suffix);
				if(len > 200){
					counterEl.innerHTML = len + "&nbsp;&nbsp;"+localsForComment.Comment_OverCharacters+(len-200);
				}else{
					counterEl.innerHTML = len;
				}
			});
			Event.addEventListener(el,'click',function(){
				if (el.innerHTML == localsForComment.Comment_CivilizedComments) {
					el.innerHTML="";
					clicked=true;
				}
			});
		});
		//评论提交按钮事件处理
		var submitBtns=[elIds.submitBtn,elIds.submitBtn+suffix];
		submitBtns.forEach(function(id){
			var el=$G(id);
			if(!el){return};
			Event.addEventListener(el,'click',formSubmit);
		});
	};
	if(document.readyState === "loaded" || document.readyState === "complete"){
		setTimeout(function(){initCommentMemberInfo();initOnLoad();},0);
	}else{
		Event.addEventListener(window,'load',initCommentMemberInfo)
		Event.addEventListener(window,'load',initOnLoad);
	}
	//用于jsonp的回调函数的集合
	return {
		reply:reply,//处理评论列表中的回复点击事件（展开评论编辑块）
		showVote:showVote,//初始化页面时显示表情投票信息
		submitMoodInit:submitMoodInit,//点击表情图标时触发的jsonp中的callback
		showresult:showresult,//表情投票后刷新结果
		commentLogout:commentLogout,//登出
		commSupport:commSupport//响应评论列表中的支持或反对
	};
}(cmtConfig));