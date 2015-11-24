
(function(){
if(window.isSpecial){
	// 专题编辑中插入图片组时，图片尺寸会在当前脚本的前一个脚本通过currentImgGroupWidth，currentImgGroupHeight描述。
	var imgMaxWidth=currentImgGroupWidth,
		imgMaxHeight=currentImgGroupHeight;
}else{
	var scripts = document.getElementsByTagName('script'),
	script = scripts[scripts.length - 1],
	jspath = script.hasAttribute ? script.src : script.getAttribute('src', 4); 
	var imgMaxWidth=jspath.substring(jspath.indexOf("imgW=")+5,jspath.indexOf("&")); // 获得传递过来的图片最大宽度
	// var imgMaxHeight=jspath.substring(jspath.indexOf("imgH=")+5);
	// //获得传递过来的图片最大高度
	var imgMaxHeight;  // 获得传递过来的图片最大高度
	var match=/imgH=([^&#]+)/i.exec(jspath);
	if(match && match[1]){
		imgMaxHeight=match[1];
	}
}

// 图片调整的js方法
window.setImgMaxSize=window.setImgMaxSize||function(targetImg,maxWidth,maxHeight){
	if(!targetImg)return;
	var img=new Image();
	img.src=targetImg.src;
	var rate=Math.max(img.width/maxWidth,img.height/maxHeight);
	targetImg.style.width=img.width/Math.max(1,rate)+'px';
	// 为了兼容IE,IE会给图片添加width和height属性，必须通过style覆盖掉height，否则可能出现比例不对。
	targetImg.style.height=img.height/Math.max(1,rate)+'px';
	
};

var filesadded="" // 保存已经绑定文件名字的数组变量
function checkloadjscssfile(filename, filetype){ 
if (filesadded.indexOf("["+filename+"]")==-1){// indexOf判断数组里是否有某一项
  loadjscssfile(filename, filetype) 
  filesadded+="["+filename+"]" // 把文件名字添加到filesadded
} 
else 
  alert("file already added!")// 如果已经存在就提示
}
function loadjscssfile(filename, filetype){ 
if ( filetype=="js" ){ // 判断文件类型
  var fileref=document.createElement('script')// 创建标签
  fileref.setAttribute("type","text/javascript")// 定义属性type的值为text/javascript
  fileref.setAttribute("src", filename)// 文件的地址
} 
else if ( filetype=="css" ){ // 判断文件类型
  var fileref=document.createElement("link") 
  fileref.setAttribute("rel", "stylesheet") 
  fileref.setAttribute("type", "text/css")  
  fileref.setAttribute("href", filename) 
} 
if ( typeof fileref!="undefined" ) 
  document.getElementsByTagName("head")[0].appendChild(fileref) 
}  

checkloadjscssfile("http://i1.dds.com/cms/carmall/css/img_gallery.css", "css") // success
checkloadjscssfile("http://i3.dds.com/cms/carmall/js/imggallery.js", "js") // success

function initDiv(parentDom,images,info,href,title,publishdate,summary){
	var wrap = document.createElement("div");
	wrap.className = "img_gallery_wrap";
	
	// 标题
	var h1 = document.createElement("h1");
	h1.className="img_gallery_title";
	var imgnum = document.createElement("span");
	imgnum.className="img_gallery_num";
	imgnum.innerHTML="(1/"+images.length +")";
	h1.innerHTML=title +"&nbsp";
	h1.appendChild(imgnum);
	wrap.appendChild(h1);
	// 提示
	var tip = document.createElement("div");
	tip.className="img_gallery_tipWrap";
	var play = document.createElement("span");
	play.className = "img_gallery_play";
	play.innerHTML = "幻灯播放";
	tip.appendChild(play);
	
	var toLookImg = document.createElement("a");
	toLookImg.setAttribute("target","_blank");
	toLookImg.setAttribute("title","查看大图");
	toLookImg.className="lookbigimg";
	toLookImg.innerHTML = "查看大图";
	tip.appendChild(toLookImg);
	
	var lrtip = document.createElement("div");
	lrtip.className = "img_gallery_tip";
	lrtip.innerHTML = "提示：支持键盘翻页 ←左 右→";
	
	tip.appendChild(lrtip);
	wrap.appendChild(tip);
	
	// 主体
	var con = document.createElement("div");
	con.className = "img_gallery_con";
	var bigimg_info = document.createElement("div");
	bigimg_info.className = "bigimg_info";
	var bigimg_wrap = document.createElement("div");
	bigimg_wrap.className = "bigimg_wrap";
	if(imgMaxHeight){bigimg_wrap.style.height=imgMaxHeight+'px';}
	var _blank = document.createElement("div");
	
	var tempDiv=document.createElement('div');
	tempDiv.innerHTML='<table style="table-layout:fixed;height:100%;width:100%;" border="0"><tr><td width="100%" height="100%" valign="middle" style="vertical-align:middle;text-align:center;padding:0;position:relative;top:0;left:0;"></td></tr></table>'		
	
	var td=tempDiv.getElementsByTagName('td')[0];

	for (var i=0;i<images.length;i++){
		var _img = document.createElement("img");

		if(imgMaxWidth && imgMaxHeight && /^\d+$/.test(imgMaxWidth) && /^\d+$/.test(imgMaxHeight)){
			_img.setAttribute("onload",'setImgMaxSize(this,'+imgMaxWidth+','+imgMaxHeight+')');
		}
		// 路径处理
		_img.src = images[i].substring(0,images[i].lastIndexOf("."))+"_500x500"+images[i].substring(images[i].lastIndexOf("."));
		_img.setAttribute("bigsrc",images[i]);
		
		var _a = document.createElement("a");
		_a.setAttribute("target","_blank");
		_a.href = "#;";
		var _div = document.createElement("div");
		_div.className = "bigimg";
		if(i==0){
			_div.style.display="block";
		}else{
			_div.style.display="none";
		}
		_a.appendChild( _img );
		_div.appendChild( _a );
		_blank.appendChild( _div );
	}
	var snext = document.createElement("div");
	snext.className = "snext";
	var sprev = document.createElement("div");
	sprev.className = "sprev";
	
	td.appendChild(_blank);
	
	bigimg_wrap.appendChild(tempDiv.firstChild);
	tempDiv=null;
	
	bigimg_wrap.appendChild(snext);
	bigimg_wrap.appendChild(sprev);
	
	
	
	var bigimg_description = document.createElement("div");
	bigimg_description.innerHTML = summary;
	bigimg_description.className = "bigimg_description";
	
	
	var bigimg_pdate = document.createElement("div");
	bigimg_pdate.innerHTML = publishdate;
	bigimg_pdate.className = "bigimg_pdate";
	
	bigimg_info.appendChild(bigimg_wrap);
	bigimg_info.appendChild(bigimg_description);
	bigimg_info.appendChild(bigimg_pdate);
	
	var img_gallery_Box = document.createElement("div");
	img_gallery_Box.className = "img_gallery_Box";
	
	var img_gallery_nav = document.createElement("div");
	img_gallery_nav.className = "img_gallery_nav";
	
	var img_back = document.createElement("div");
	img_back.className = "img_back";
	
	var img_thumbs = document.createElement("div");
	img_thumbs.className = "img-thumbs";
	
	var img_thumb_list = document.createElement("ul");
	img_thumb_list.className = "img-thumb-list";
	
	for(var i=0;i<images.length;i++){
		var _li = document.createElement("li");
		if(i==0){
			_li.className="now";
		}
		var _img = document.createElement("img");
		_img.setAttribute("info",info[i]);
		_img.src = images[i].substring(0,images[i].lastIndexOf("."))+"_120x120"+images[i].substring(images[i].lastIndexOf("."));
		_li.appendChild(_img);
		img_thumb_list.appendChild(_li);
	}
	img_thumbs.appendChild(img_thumb_list);
	img_gallery_nav.appendChild(img_back);
	img_gallery_nav.appendChild(img_thumbs);
	
	var img_forward = document.createElement("div");
	img_forward.className = "img_forward";
	
	img_gallery_nav.appendChild(img_forward);
	img_gallery_Box.appendChild(img_gallery_nav);
	
	con.appendChild(bigimg_info);
	con.appendChild(img_gallery_Box);
	
	wrap.appendChild(con);

	parentDom.appendChild(wrap);
}



var images = ["http://i2.dds.com/cms/carmall/upload/resources/image/2015/05/27/31913.png","http://i1.dds.com/cms/carmall/upload/resources/image/2015/05/27/31914.png","http://i2.dds.com/cms/carmall/upload/resources/image/2015/05/27/31916.png","http://i1.dds.com/cms/carmall/upload/resources/image/2015/05/27/31917.png","http://i3.dds.com/cms/carmall/upload/resources/image/2015/05/27/31915.png"];
var info = ["适合过日子的车","适合过日子的车","适合过日子的车","适合过日子的车","适合过日子的车"];
var title = "分期购车";
var publishdate = "2015-05-27&#32;17:50:31";
var summary = "";
initDiv(document.getElementById('imagegroupjs_508422'),images,info,"",title,publishdate,summary);

document.getElementById('imagegroupjs_508422').innerHTML=document.getElementById('imagegroupjs_508422').innerHTML+"<script type='text/javascript'>	galleryScroll({imgMaxHeight:imgMaxHeight,containerId:'imagegroupjs_508422',		changeObj : '#imagegroupjs_508422 .bigimg_wrap .bigimg',		changeObj_wrap:'#imagegroupjs_508422 .bigimg_wrap',		thumbObj_wrap:'#imagegroupjs_508422 .img-thumbs',  		thumbObj : '#imagegroupjs_508422 ul.img-thumb-list li',		botPrev : '#imagegroupjs_508422 .img_gallery_wrap .sprev',		botNext : '#imagegroupjs_508422 .img_gallery_wrap .snext',		description:'#imagegroupjs_508422 div.bigimg_description',		allowObj:'#imagegroupjs_508422 div.img_gallery_con',		imgcurNum:'#imagegroupjs_508422 span.img_gallery_num',		thumbPrev:'#imagegroupjs_508422 div.img_back',		thumbNext:'#imagegroupjs_508422 div.img_forward',		autoChangeObj: '#imagegroupjs_508422 div.img_gallery_tipWrap span',		lookBigImgObj: '#imagegroupjs_508422 div.img_gallery_tipWrap a'	});</script>";

setTimeout(function(){
if(document.getElementById('imagegroupjs_508422').getElementsByTagName('script')[1]){
	eval(document.getElementById('imagegroupjs_508422').getElementsByTagName('script')[1].text);
}else{
	eval(document.getElementById('imagegroupjs_508422').getElementsByTagName('script')[0].text);
}
},1000);
}())
