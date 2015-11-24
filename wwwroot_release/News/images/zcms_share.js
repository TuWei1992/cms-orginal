//复制功能
function zcms_tryCopy(id){
	var tempCode=document.getElementById(id);
	tempCode.select();
	if(window.clipboardData){
		var codeValue=tempCode.value;
		var ok=window.clipboardData.setData("Text",codeValue);
		if(ok) alert("复制成功。现在您可以粘贴（Ctrl+v）发给你的好友们吧 ！");
	}else{
		alert("您使用的浏览器不支持此复制功能，请使用Ctrl+C或鼠标右键。");
	}
}

function zcms_share(sFlashvar,sPlayerUrl,sTitle,sType){
	sTitle = sTitle||document.title; //分享的标题
	sType = sType||"video"; //默认类型为分享视频，还可以是分享文章
	var purl=window.location.href; //当前页面地址
	var HtmlCode;  //html代码
	var viewWidth=document.compatMode == "BackCompat" ? document.body.clientWidth : document.documentElement.clientWidth;	//可视范围的宽度
	var viewHeight=document.compatMode == "BackCompat" ? document.body.clientHeight : document.documentElement.clientHeight;	//可视范围的高度
	var shareUrl= '<a href="http://s.jiathis.com/?webid={sitename}&url='+encodeURIComponent(purl)+'&title='+encodeURIComponent((sType=="video"?'视频':'')+'《'+sTitle+'》')+'" class="{sitename}" target="_blank">';
    var dialogTem ='<div class="shareWrap">';
	dialogTem+='<div class="shareTitle"><div class="shareClose" onClick="document.getElementById(\'shareWrap\').parentNode.removeChild(document.getElementById(\'shareWrap\'))"></div>分享</div>';
	dialogTem+='<div class="shareCon"><div class="share" id="shareComCode"><strong>一键转帖</strong>';
	dialogTem+= shareUrl.replace(/\{sitename\}/g,'tsina')+'新浪微博</a>';
	dialogTem+= shareUrl.replace(/\{sitename\}/g,'renren')+'人人网</a>';
	dialogTem+= shareUrl.replace(/\{sitename\}/g,'qzone')+'QQ空间</a>';
	dialogTem+= shareUrl.replace(/\{sitename\}/g,'tsohu')+'搜狐微博</a>';
	dialogTem+='</div>';
	if(sType=="video"){
		HtmlCode='<embed width="480" height="360" wmode="transparent" type="application/x-shockwave-flash" src="'+sPlayerUrl+'" quality="high" flashvars="'+ sFlashvar +'&stretching=fill" allowfullscreen="true"></embed>';
		HtmlCode=HtmlCode.replace(/\"/g, "&quot;");
		dialogTem+='<div class="share shareCom" id="shareVideoCode">';
		dialogTem+='<strong style="padding-top:6px;">普通转帖</strong>';
		dialogTem+='<div><table border="0" cellpadding="0" cellspacing="0"><tr><td>页面地址：</td>';
		dialogTem+='<td><input type="text" class="text" id="video_url"  value="'+purl+'" /></td>';
		dialogTem+='<td><input type="button" class="copybtn" value="复制" onClick="zcms_tryCopy(\'video_url\')"/></td></tr>';
		//dialogTem+='<tr><td>视频地址：</td>';
		//dialogTem+='<td><input type="text" class="text" id="video_flash" value="'+sPlayerUrl+"?"+sFlashvar+'"/></td>';
		//dialogTem+='<td><input type="button" class="copybtn" value="复制" onClick="zcms_tryCopy(\'video_flash\')"/></td></tr>';
		dialogTem+='<tr><td>html代码：</td>';
		dialogTem+='<td><input type="text" class="text" id="video_html" value="'+ HtmlCode +'"/></td>';
		dialogTem+='<td><input type="button" class="copybtn" value="复制" onClick="zcms_tryCopy(\'video_html\')"/></td></tr></table></div>';
		dialogTem+='</div>';
	}                    
    dialogTem+='</div></div>';
	var dialogEle=document.createElement("div");
	dialogEle.innerHTML=dialogTem; 
	dialogEle.style.position="absolute";
	dialogEle.id="shareWrap";
	document.body.appendChild(dialogEle);
	dialogEle.style.display="block"; //显示弹出层
	var dialogW=dialogEle.offsetWidth;  //弹出层的宽度
	var dialogH=dialogEle.offsetHeight; //弹出层的高度
	var hleft=document.documentElement.scrollLeft || document.body.scrollLeft; //被卷去的宽度
	var htop=document.documentElement.scrollTop || document.body.scrollTop; //被卷去的高度
	var left= (viewWidth-dialogW)/2 + hleft;
	var top = (viewHeight-dialogH)/2 + htop;
	dialogEle.style.left=left+"px";  //将弹出层定位
	dialogEle.style.top=top+"px";
}