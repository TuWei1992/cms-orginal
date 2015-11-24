var path ="";
function getUrlParameterAdv(){
	path = document.getElementById('imagegroupjs_382991').getAttribute("path")
 }
var filesadded="" //保存已经绑定文件名字的数组变量 
function checkloadjscssfile(filename, filetype){ 
if (filesadded.indexOf("["+filename+"]")==-1){// indexOf判断数组里是否有某一项 
  loadjscssfile(filename, filetype) 
  filesadded+="["+filename+"]" //把文件名字添加到filesadded 
} 
else 
  alert("file already added!")//如果已经存在就提示 
}
function loadjscssfile(filename, filetype){ 
if ( filetype=="js" ){ //判断文件类型 
  var fileref=document.createElement('script')//创建标签 
  fileref.setAttribute("type","text/javascript")//定义属性type的值为text/javascript 
  fileref.setAttribute("src", filename)//文件的地址 
} 
else if ( filetype=="css" ){ //判断文件类型 
  var fileref=document.createElement("link") 
  fileref.setAttribute("rel", "stylesheet") 
  fileref.setAttribute("type", "text/css")  
  fileref.setAttribute("href", filename) 
} 
if ( typeof fileref!="undefined" ) 
  document.getElementsByTagName("head")[0].appendChild(fileref) 
}  

getUrlParameterAdv();

checkloadjscssfile(path+"/design/img_gallery.css", "css") //success 
checkloadjscssfile(path+"/design/imggallery.js", "js") //success 



function initDiv(parentDom,images,info,href,title){
	var wrap = document.createElement("div");
	wrap.className = "img_gallery_wrap";
	
	//标题
	var h1 = document.createElement("h1");
	h1.className="img_gallery_title";
	var imgnum = document.createElement("span");
	imgnum.className="img_gallery_num";
	imgnum.innerHTML="(1/"+images.length +")";
	h1.innerHTML=title +"&nbsp";
	h1.appendChild(imgnum);
	wrap.appendChild(h1);
	//提示
	var tip = document.createElement("div");
	tip.className="img_gallery_tipWrap";
	var play = document.createElement("span");
	play.className = "img_gallery_play";
	play.innerHTML = "幻灯播放";
	tip.appendChild(play);
	var lrtip = document.createElement("div");
	lrtip.className = "img_gallery_tip";
	lrtip.innerHTML = "提示：支持键盘翻页 ←左 右→";
	tip.appendChild(lrtip);
	wrap.appendChild(tip);
	
	//主体
	var con = document.createElement("div");
	con.className = "img_gallery_con";
	var bigimg_info = document.createElement("div");
	bigimg_info.className = "bigimg_info";
	var bigimg_wrap = document.createElement("div");
	bigimg_wrap.className = "bigimg_wrap";
	var _blank = document.createElement("div");
	
	for (var i=0;i<images.length;i++){
		var _img = document.createElement("img");
		//路径处理
		_img.src = 	path + "/" + images[i];
		var _a = document.createElement("a");
		_a.setAttribute("target","_blank");
		_a.href = "#;";
		var _div = document.createElement("div");
		_div.className = "bigimg";
		_a.appendChild( _img );
		_div.appendChild( _a );
		_blank.appendChild( _div );
	}
	var snext = document.createElement("div");
	snext.className = "snext";
	var sprev = document.createElement("div");
	sprev.className = "sprev";
	
	bigimg_wrap.appendChild(_blank);
	bigimg_wrap.appendChild(snext);
	bigimg_wrap.appendChild(sprev);
	
	
	var bigimg_description = document.createElement("div");
	bigimg_description.innerHTML = "测试";
	bigimg_description.className = "bigimg_description";
	
	
	var bigimg_pdate = document.createElement("div");
	bigimg_pdate.innerHTML = "2012年12月12日 07:07"
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
		var _img = document.createElement("img");
		_img.setAttribute("info",info[i]);
		_img.src = path + "/" + images[i];
		var _li = document.createElement("li");
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



var images = ["upload/gnxw/tp/1413734.jpg","upload/gnxw/tp/1421360.jpg","upload/gnxw/tp/1438483.jpg","upload/gnxw/tp/1446109.jpg","upload/gnxw/tp/1454237.jpg","upload/gnxw/tp/1461862.jpg","upload/gnxw/tp/1478985.jpg"];
var info = ["","","长期计划生育政策导致的非均衡生育问题促使中国快速老龄化。而对老年人的生活、医疗、精神等照料明显落后，养老问题日趋严峻。","","","",""];
var title = "中国人口老龄化问题日趋严峻";
initDiv(document.getElementById('imagegroupjs_382991'),images,info,"",title);

document.getElementById('imagegroupjs_382991').innerHTML=document.getElementById('imagegroupjs_382991').innerHTML+"<script type='text/javascript'>	galleryScroll({		changeObj : '.bigimg_wrap .bigimg',		changeObj_wrap:'.bigimg_wrap',		thumbObj_wrap:'.img-thumbs',  		thumbObj : 'ul.img-thumb-list li',		botPrev : '.img_gallery_wrap .sprev',		botNext : '.img_gallery_wrap .snext',		description:'div.bigimg_description',		allowObj:'div.img_gallery_con',		imgcurNum:'span.img_gallery_num',		thumbPrev:'div.img_back',		thumbNext:'div.img_forward',		autoChangeObj: 'div.img_gallery_tipWrap span'	});</script>";

setTimeout(function(){
if(document.getElementById('imagegroupjs_382991').getElementsByTagName('script')[1]){
	eval(document.getElementById('imagegroupjs_382991').getElementsByTagName('script')[1].text);
}else{
	eval(document.getElementById('imagegroupjs_382991').getElementsByTagName('script')[0].text);
}
},1000);

