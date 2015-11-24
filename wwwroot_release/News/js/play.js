var isIE = navigator.userAgent.toLowerCase().indexOf("msie") != -1;
var isIE6 = navigator.userAgent.toLowerCase().indexOf("msie 6.0") != -1;
var isGecko = navigator.userAgent.toLowerCase().indexOf("gecko") != -1;
// JavaScript Document
var CONTEXTPATH = '/ZCMS2.X/';
var PLAYERPATH='template/index-playlist.htm';
var NOPIC120='images/nopicture1.jpg';
var NOPIC90='images/nopicture2.jpg';
var scripts = document.getElementsByTagName("script");
var PUBPATH;
for(var i=0;i<scripts.length;i++){
	if(/.*\/play\.js$/g.test(scripts[i].getAttribute("src"))){
		var jsPath = scripts[i].getAttribute("src").replace(/\/play\.js$/g,'');
		if(jsPath.indexOf("/")==0||jsPath.indexOf("://")>0){
			CONTEXTPATH = jsPath.substring(0,jsPath.lastIndexOf('/')+1);
			break;
		}
		var arr1 = jsPath.split("/");
		var path = window.location.href;
		if(path.indexOf("?")!=-1){
			path = path.substring(0,path.indexOf("?"));
		}
		var arr2 = path.split("/");
		arr2.splice(arr2.length-1,1);
		for(var i=0;i<arr1.length;i++){
			if(arr1[i]==".."){
				arr2.splice(arr2.length-1,1);
			}
		}
		CONTEXTPATH = arr2.join('/')+'/';
		break;
	}
}

if(CONTEXTPATH.indexOf("://")>0){
	PUBPATH=CONTEXTPATH;
}else{
   PUBPATH=CONTEXTPATH.substring(0,CONTEXTPATH.lastIndexOf('/')+1);
}
	console.log(PUBPATH);
function getSelected(id){
	var lis = document.getElementById(id).getElementsByTagName('input');
	var mp3Lis = new Array();
	for(var i=0; i<lis.length; i++) {
		if(lis[i].checked) {
			mp3Lis.push(lis[i].value.replace(/(\.\.\/)+/g,"../"));
		}
	}
	var str = mp3Lis.join('||');
	return str;
}

getFlashCallbak.c=0;
function getFlashCallbak(re){
	if(re)getFlashCallbak.c=0;
	if(getFlashCallbak.c<9999){
		if(playerWin.getFlashObj&&playerWin.getFlashObj('musicplayer').calladd&&playerWin.addmp3toplayer){
			log(playerWin.getFlashObj('musicplayer').calladd)
			var mp3Str = playStr.split('||');
			playerWin.addmp3toplayer(mp3Str);
		}else{
			 setTimeout(getFlashCallbak,20);
			 return;
		}
		getFlashCallbak.c++;
	}else{
		alert("播放器打开失败")
	}
}
var playerWin;
var playStr;
function openWinPlay(playStr) {
	if(playStr) {
		if(isGecko){
			if(playerWin&&playerWin.closed==false){playerWin.close();}
		}else{
			if(playerWin&&playerWin.closed==false)playerWin.location.reload();
		}
		if(!playerWin||playerWin.closed==true)
			playerWin = window.open(PUBPATH+PLAYERPATH, "playerWin", "width=340,height=300");
		if(isIE){
			getFlashCallbak(1);
		}else{
			playerWin.onload=function(){getFlashCallbak(1)};
		}
	}else{
		alert('你还没有选择音频');
	}
}
function play(str,isAdd){;
	if(str) {
		playStr = str.replace(/(\.\.\/)+/g,"../");
	}else{
		playStr=getSelected('listaudio');
	}
	if(!playStr)
		return alert('你还没有选择音频');
	if(isAdd){
		if(playerWin&&playerWin.closed==false){
			var mp3Str = playStr.split('||');
			playerWin.addmp3toplayer(mp3Str);
			var added=true;
		}
		var oldstr=Cookie.get('saveList');
		playStr = oldstr?oldstr+'||'+playStr:playStr;
	}
	Cookie.set("saveList", playStr);
	if(!added)openWinPlay(playStr);
	
}

function getSaveList() {
	playStr = Cookie.get('saveList');
	openWinPlay(playStr);
}

function allSelect(e) {
	var lis = document.getElementById('listaudio').getElementsByTagName('input');
	for(var i=0; i<lis.length; i++) {
		lis[i].checked = e.checked;
	}
}

function log(){
	try{
		console.log.apply(console,arguments);
	}catch(e){}	
}

function onloadPic(e) {
	var width = e.getAttribute("width");
	var height = e.getAttribute("height");

	if(width == height) {
		e.src = PUBPATH + NOPIC90;
	}else{
		e.src = PUBPATH + NOPIC120;
	}
}
