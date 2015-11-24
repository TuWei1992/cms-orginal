//@ sourceURL=articleQuickEditor-ttttttt.js
var curcatalogID = locals['articleQuickEditor'].CatalogID;
var PAGE_SPLIT ="<!--_ZVING_PAGE_BREAK_-->";
var TITLE_SPLIT ="|";
var pageTitles=[]; //页面数组
var contents=[]; //页面内容数组
var editors=[];

var isQuickEdit=true;//快速编辑标识
var isQuickAdd=false;//快递新建标识
var currentID;//当前文章ID

var copyType = 0;// 引用复制类型
var copyID;
var titleStyle; //文章标题样式



var editor; //UEditor编辑器
var statusName=locals['articleQuickEditor'].StatusName;//文章状态
var oldTitle;//历史
var oldBodyText;//历史内容
var linkFlag = locals['articleQuickEditor'].LinkFlag=="Y";

//在页面中显示文章内容
function setContent(content){
	
	if(!$("#contentFrame").length){
		$('#Content').html('<ifr'+'ame src="about:blank" width="100%"  frameBorder="0"  id="contentFrame" scrolling="auto" ></'+'ifra'+'me>');
		
		
	}
	var contentFrame=$("#contentFrame")[0];
	var doc=contentFrame.contentWindow.document;
	
	$('#contentFrame').height(parseInt($('#editorWrap').height()));
	//console.log(content);
	//页面中不能出现多余的body标签(和body闭合标签)，必须把body标签拆开，否则会影响page.js页面提取子页面信息（当前也是作为ajax返回的内容页面，page.js会提取内容信息）
	var html='<!DOCTYPE html><html><head>'+
	'<link type="text/css" rel="stylesheet" href="'+location.protocol + '//' + location.host+CONTEXTPATH+'editor/themes/iframe.css" />'+
	(locals['articleQuickEditor'].ArticleUEEditorImportCSS?'<link type="text/css" rel="stylesheet" href="'+Url.documentUrl.domain+locals['articleQuickEditor'].ArticleUEEditorImportCSS+'"/>':'')+ 			
			'</head><'+'body style="background-color:#F9F9F9;">';
	doc.open();
	var bodyText=content.replace(new RegExp(PAGE_SPLIT,'g'), '<p style="border-top: 3px #666 dashed;margin: 10px;"></p>');
	doc.write(html+bodyText+'<'+'/body></html>');
	doc.close();
	doc.ondblclick=function(){
		$('#BtnSave2').click();
	}
	
	//var doc=contentFrame.contentWindow.document;
	//doc.body.innerHTML=content.replace(new RegExp(PAGE_SPLIT,'g'), '<p style="border-top: 3px #666 dashed;margin: 10px;"></p>')
	
	try{//在ready事件中异步加载页面局部内容时，在ff中存在onload和onReady中添加的方法执行顺序混乱问题（ajax请求的页面中的onLoad中方法可能在onReady中方法之前执行，
		//例如当异步加载的内容返回后，页面还没进入loaded状态但已经过了ready状态时）
		Zving.ComponentManager.getByDom('Toolbar','Toolbar').fitToSize();
	}catch(ex){
		var timer=setInterval(function(){
			try{				
				Zving.ComponentManager.getByDom('Toolbar','Toolbar').fitToSize();
				clearInterval(timer);
			}catch(ex){
				
			}			
		},50);
	}
	//调整尺寸;
	//contentFrame.style.height=Math.max(doc.documentElement.scrollHeight,doc.body.scrollHeight)+'px';
}
function clearDetail(){
	$('#tdTitle').html("&nbsp;");
	$('#tdTitle').attr("style","&nbsp;");
	$('#tdAddUser').html('&nbsp;');
	$('#tdPublishDate').html("&nbsp;");
	$('#ID').val("");
	$('#Content').html("&nbsp;");
	$('#ArticleID').val("");
	//$('#CatalogID').val("&nbsp;");
	//$('#SiteID').val("&nbsp;");
	$('#LogoFile').val("");
	$("#trCopyCatalogName").hide();
	$("#tdCopyCatalogName").html("");
	$('#trToolbar a').disable();
}

var editStatus = false,
	editorHeight=0,
	editorWrap_height,
	EditorPanel_height;

var autoSaveTimer;
// 如果有编辑器实例存在，则销毁所有编辑器实例
function destroyEditors(){
	UE.utils.each(UE.instants,function(editor,index,instants){
		if(editor.hasOwnProperty('key') && editor.key && editor.key.indexOf("MetaValue_") == -1){
			editor.destroy();
	 	    //instants[index].destroy();
		}
	});
	$("textarea[name='editorValue']").remove(); 
	UE.instants = {};
	editors.length = 0;
}

// editor编辑器实例化成功后更改相应外观进
function updateEditorUI(){
	var editorToolbarboxEl = editor.ui.getDom("toolbarbox");
	$("#editorToolbarWrap").append(editorToolbarboxEl); // 将编辑工具条移到目标区域中
	//$("#EditorPanel").css("border","1px solid #eee");
}

// 切换编辑状态
function edit(){
	//zq 2015/05/20 enable扩展字段
	$('#contentform').enable();
	
	UE.utils.each(UE.instants,function(editor,index,instants){
		if(editor.hasOwnProperty('key') && editor.key && editor.key.indexOf("MetaValue_") != -1){
			editor.enable();
		}
	});
	
    var minus=31;
    if($G('editorWrap').clientWidth < 790){
        minus=55;
    }
    $('#CName').disable();
	if(!editStatus){
 	   $('#overflowPannel').height($G('EditorPanel').clientHeight - minus);
		if(!linkFlag) {				
				if(editor){ // 如果有编辑器实例存在，则销毁所有编辑器实例
					destroyEditors();
				}
				//console.log("$G('EditorPanel').clientHeight",$G('EditorPanel').clientHeight)
				editor = UE.getEditor('Content_1', {
					initialContent:contents[0], //初始化编辑器的内容
					wordCount:false, //关闭字数统计
					minFrameHeight:300, // 无内容时编辑器的原始高度
					initialFrameWidth: 'auto',  //初始化编辑器宽度
					iframeCssUrlOuter: locals['articleQuickEditor'].ArticleUEEditorImportCSS ? Url.documentUrl.domain+locals['articleQuickEditor'].ArticleUEEditorImportCSS:'',//给编辑器内部引入一个css文件
			        initialFrameHeight: $G('EditorPanel').clientHeight - minus-1-2,//这一个像素为编辑器下面的table的上边框宽度，2为edui-editor的垂直方向的边框占的高度。
			        //initialFrameHeight: $G('overflowPannel').clientHeight - minus,
			        initialStyle:'body{font-size:14px;line-height:1.6;}',
			        elementPathEnabled:false, //关闭elementPath
			        autoFloatEnabled:false, //是否保持toolbar的位置不动,默认true
			        lang: locals['articleQuickEditor'].UserLanguage,
			        toolbars:[
	                  [	'source', '|', 'undo', 'redo', '|',
		                 'bold', 'italic', 'underline', 'forecolor', 'fontfamily', 'fontsize', '|',
		                 'indent','justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'insertorderedlist', 'insertunorderedlist', '|',
		                 'zcomment', 'zimage', 'inserttable', 'deletetable','|','autotypeset', 'link', 'unlink'
	                  ]
			        ],
			        catalogID : locals['articleQuickEditor'].CatalogID,
					dataType : "Article",
					dataID : locals['articleQuickEditor'].ContentID,
					imageWidth : 500,
					imageHeight : 500,
					sourceEditorFirst:false,
					page : 1
			    });
				editors.push(editor);
				editor.ready(function(){
					var me = this;
					UE.dom.domUtils.on(me.window,'focus',function(){ // 根据当前focus的编辑器来切换显示/隐藏工具栏
						$("#editorToolbarWrap .edui-editor-toolbarbox").hide(); 
						$(me.ui.getDom("toolbarbox")).show(); 
					});
					UE.dom.domUtils.on(me.container,'click',function(){ // 可能在源码模式下
						$("#editorToolbarWrap .edui-editor-toolbarbox").hide(); 
						$(me.ui.getDom("toolbarbox")).show();
					});
					updateEditorUI();
					initPages();
					//ff中的图片不能调整尺寸，必须切换到源码模式后再切换回来才可以。
					if(UE.browser.gecko){
						editor.execCommand('source');
						editor.execCommand('source');
					}
				});
				$("#trTitle_input").show();
				$("#trTitle_text").hide();
				$('#Content').hide();
			//文章自动保存
			if($V("#OpenArticleAutoSave") == "Y"){
				autoSaveTimer=setInterval(articleAutoSave, 1000*60);//一分钟一次
			}
			
		}else{
			$('#Content').hide();
			$('#DivRedirect').show();
			$('#CName').enable();
			$('#CName').attr('verify','NotNull')
			//取消文章自动保存
			if(autoSaveTimer){
				clearInterval(autoSaveTimer);
			}
		}
 		$("#BtnSave2").hide();
 		var tempSave = $("#Workflow_CommitAction-1");
 		if(tempSave.length){
 	 		tempSave.show();
 	 		tempSave.enable();
 		}else{
 			$("#BtnSave").show();
 		}
 		$('#overflowPannel').show();
	}else{  
		if(!linkFlag) {
			if(editor){
			  	$('#editorWrap').height(editorWrap_height || (editorWrap_height=$G('#editorWrap').clientHeight-25));
				//$('#EditorPanel').height(EditorPanel_height || (EditorPanel_height=$G('#editorWrap').clientHeight-25));
				if(isIE6){
					$('#EditorPanel').height(EditorPanel_height);
				}else{
					$('#EditorPanel').css('min-height',EditorPanel_height);
				}
				$('#editorToolbarWrap').html("");
				setContent(getPageData());
				//$('#Content').html(getPageData().replace(new RegExp(PAGE_SPLIT,'g'), '<p style="border-top: 3px #666 dashed;margin: 10px;"></p>'));
				$(".divPageTitle").hide();
				$("#UEditorContainer").html("<div id='Content_1'></div>");
			}
	 		$('#Content').show();
		}else{
			$('#Content').html($('#CName').val());
			$('#Content').show();
	 		$('#DivRedirect').hide();
	 		$('#CName').disable();
			$('#CName').removeAttr('verify');
	 		
		}
		$("#tdTitle").html($V("#Title"));
		$('#tdTitle').attr("style",$V("#TitleStyle"));
		$("#trTitle_input").hide();
		$("#trVerifyTitle").hide();
		$("#trTitle_text").show();
		$('#PageTitle_1').html($V("#PageTitle"));
 		$("#BtnSave2").show();	
 		$("#BtnSave").hide();
 		$('#overflowPannel').hide();
	}
	
	editStatus = !editStatus;
	//Zving.ComponentManager.getByDom('Toolbar','Toolbar').fitToSize();
}

function save(func, dontShowAlert/*不要弹出提示*/) {
	var title = editStatus ? $('#Title').val() : $('#tdTitle').text();
	var pTitles, bodyText;
	if(editStatus && editor) {
		pTitles = getPageTitles();
	 	bodyText = getPageData();
	}
	if(!editStatus) {
		if(func) {
			func(new DataCollection(),function(){DataList.loadData("list1");});
		}
		return; // 非编辑状态直接返回
	}
	var bodyText;
	var dc = Form.getData("#contentform");
	dc.PageTitles = getPageTitles();
	if(linkFlag){
		dc.RedirectURL = $V('#RedirectURL');
		dc.LinkFlag = "Y";
	} else {
		bodyText = getPageData();
		dc.BodyText = bodyText;
		if(bodyText.trim().length==0){
			if(!dontShowAlert){
				Dialog.alert( Lang.get('Article.CheckContentEmpty')+"!" ) ;
			}
			return ;
		}
	}
	if(Verify.hasError()){
		return;
	}

	var method="Article.quickEditSave";
	if(isQuickAdd){
		method = "Article.save";
		dc = initData;
		dc.ArticleID=dc.ID;
		dc.Status=0;
		dc.Title=title;
		dc.PageTitles=pTitles;
		dc.BodyText=bodyText;
	}
	$("#EditorPanel").css("border",0);
	Server.sendRequest(method, dc, function(response){
			if(response.Status == 1) {
				
				oldTitle = $V("#Title");
				oldBodyText = $("_Contents").html();
				if(!linkFlag){
				contents = bodyText.split(PAGE_SPLIT);
				$("_Contents").html(bodyText);
				articleAutoSave.lastPageTitles=pTitles;
				setContent(bodyText);
				//$("#Content").html(bodyText.replace(new RegExp(PAGE_SPLIT,'g'), '<p style="border-top: 3px #666 dashed;margin: 10px;"></p>'));
				}
				DataList("contentlist").recordsSelected=[];
				DataList.loadData("contentlist",function(){
						if(func) {
							func(response);
						}else{
							MsgPop(response.Message);
						}
						$G('SelectedID_'+response.ContentID).click();	
				});
			} else {
				Dialog.warn(response.Message);
			}
	});
}

var firstlayout=true;
Page.onLayout(function(){
	if(firstlayout){
		if(isIE6){
			$('#EditorPanel').height($G('#editorWrap').clientHeight-(isIE?35:31));
		}else{
			$('#EditorPanel').css('min-height',$G('#editorWrap').clientHeight-(isIE?35:31));
		}
		editorWrap_height=$('#editorWrap').height();
		EditorPanel_height=$('#EditorPanel').height();
		firstlayout=false;
	}
})


//分页初始化
function initPages() {
	var e = editor;
	for(var i = 1; i < contents.length; i++) {
		var e = ZPageBreak.addPageBlock(e, i+1); // 得到分页的编辑器实例
		e.addListener('ready', (function(content){ // 给编辑器设置分页内容
			return function(type, evt) {
				ZPageBreak.boolbarSwitch(this,content);
			}
		})(contents[i]));
		$S("PageTitle_" + (i+1), pageTitles[i]); // 得到分页的标题
	}
}

/**
 * 获取所有分页编辑器内容合并值
 */
function getPageData() {
	var c = "";
	var arr = $(".divPageTitle");
	for(var i = 0; arr && i < arr.length; i++) {
		var index =  arr[i].id.split("_")[1]; // 截取分页标题的序号：分页标题的id（divPageTitle_1、divPageTitle_2...）
		var e = UE.getEditor("Content_" + index);
		if(c.length > 0) {
			c += PAGE_SPLIT;
		}
		c += e.getContent();
	}
	return c;
}

// 获得分页标题合并值
function getPageTitles() {
	return $NV("PageTitle").join(TITLE_SPLIT);
}

// 新建
function quickadd(){
	if(editStatus){
		Dialog.alert(Lang.get('Article.QuickEditor.SaveFirst'));
		return;
	}	
	Server.sendRequest("Article.initQuickEditor",{CatalogID:curcatalogID},function(response){
			if(response.Status==1){
				initadd=true;
				initData=response
			}
		}, null, null, "json");
	
	editStatus=true;
	isQuickAdd =true;
	currentID = 0;
	clearDetail();
	resetPriv1(curcatalogID);
	
    var minus=31;
    if($G('editorWrap').clientWidth < 790){
        minus=55;
    }
    $('#EditorPanel').height('100%');
    $('#overflowPannel').height($G('EditorPanel').clientHeight - minus);
	$('#overflowPannel').show();
	
	//zq 2015/10/10 
	$('#contentform').enable();
	UE.utils.each(UE.instants,function(editor,index,instants){
		if(editor.hasOwnProperty('key') && editor.key && editor.key.indexOf("MetaValue_") != -1){
			editor.enable();
		}
	});
	
    if(editor){ // 如果有编辑器实例存在，则销毁所有编辑器实例
    	destroyEditors();
	}
	editor = UE.getEditor('Content_1', {
		wordCount:false, //关闭字数统计
		minFrameHeight:300, // 无内容时编辑器的原始高度
		initialFrameWidth: '100%',  //初始化编辑器宽度
        initialFrameHeight: $G('EditorPanel').clientHeight -minus-1,
        initialStyle:'body{font-size:14px;line-height:1.6;}',
		iframeCssUrlOuter: locals['articleQuickEditor'].ArticleUEEditorImportCSS ? Url.documentUrl.domain+locals['articleQuickEditor'].ArticleUEEditorImportCSS:'',
        elementPathEnabled:false, //关闭elementPath
        autoFloatEnabled:false,
        toolbars:[
           ['source', '|', 'undo', 'redo', '|',
            'bold', 'italic', 'underline', 'forecolor', 'fontfamily', 'fontsize', '|',
            'indent','justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'insertorderedlist', 'insertunorderedlist', '|',
            'zcomment', 'zimage', 'inserttable', 'deletetable', '|', 'link', 'unlink'
           ]
        ],
        catalogID : locals['articleQuickEditor'].CatalogID,
		dataType : "Article",
		dataID : locals['articleQuickEditor'].ContentID,
		imageWidth : 500,
		imageHeight : 500,
		sourceEditorFirst:false,
		page : 1
    });
    
	editors.push(editor);
	editor.ready(function(){
		var me = this;
		UE.dom.domUtils.on(me.window,'focus',function(){ // 根据当前focus的编辑器来切换显示/隐藏工具栏
			$("#editorToolbarWrap .edui-editor-toolbarbox").hide(); 
			$(me.ui.getDom("toolbarbox")).show(); 
		});
		UE.dom.domUtils.on(me.container,'click',function(){ // 可能在源码模式下
			$("#editorToolbarWrap .edui-editor-toolbarbox").hide(); 
			$(me.ui.getDom("toolbarbox")).show();
		});
		updateEditorUI();
		//ff中的图片不能调整尺寸，必须切换到源码模式后再切换回来才可以。
		if(UE.browser.gecko){
			editor.execCommand('source');	
			editor.execCommand('source');		
		}
	});
	$('#Content').hide();
	$('#CName').disable();

	$('#tdTitle').html("<input type='text' class='inputText' verify='NotNull' id='Title' style='width:90%;"+titleStyle+"' value='"+$G("tdTitle").innerText+"'>");
	$('#PageTitle_1').html('<input name="PageTitle" type="text" size="50" value="'+$("#tdPageTitle").html()+'" />');
	$("#BtnSave2").hide();
	//$("#BtnSave").attr("priv","com.zving.cms.Catalog.Content.Add[locals['articleQuickEditor'].CatalogID]");
	$("#BtnSave").enable();
	$("#BtnSave").show();
	//处理工作流加载
	if(window.loadButtons) {
		if(!$V("#CopyType_"+$V("#ContentID")) || $V("#CopyType_"+$V("#ContentID")) <=1){
			var dc = {ConfigProps:locals['articleQuickEditor'].$ConfigProps,Status:locals['articleQuickEditor'].Status,CatalogID:locals['articleQuickEditor'].CatalogID};
			loadButtons(dc);		
		}
	}
	$("#trToolbar a.z-btn").each(function() {
 			$(this).disable();
 	}); 
 	$("#BtnSave").enable();
}

function resetPriv1(catalogid) {
	$("#trToolbar a.z-btn").each(function() {
	 	var p = this.getAttribute("priv");
	 	if (p) {
		 	p = p.substring(0, p.lastIndexOf("."));
 			$(this).attr("priv",p+".$"+"{CatalogID}");
	 	}
 	}); 
	Application.setPrivParam("CatalogID",catalogid);
	Application.setAllPriv();
}

function isDirty(){
	
}

//自动保存
function articleAutoSave() {
	var title = editStatus ? $('#Title').val() : $('#tdTitle').text();
	var pTitles, bodyText;
	if($("#BtnSave").prop("disabled")){
		return;//保存按钮不可用则不自动保存
	}
	if(editStatus && editor) {
		pTitles = getPageTitles();
	 	bodyText = getPageData();;
	}else{
		return;//非编辑状态不需要保存	
	}
	if(pTitles==articleAutoSave.lastPageTitles&&bodyText==$("#_Contents").val()){
		console.log('未修改');
		return;
	}
	var bodyText;
	var dc = Form.getData("#contentform");
	dc.PageTitles = getPageTitles();
	if(linkFlag){
		dc.RedirectURL = $V('#RedirectURL');
		dc.LinkFlag = "Y";
	} else {
		bodyText = getPageData();
		dc.BodyText = bodyText;
		if(bodyText.trim().length==0){
			//Dialog.alert( Lang.get('Article.CheckContentEmpty')+"!" ) ;
			return ;
		}
	}
	if(Verify.hasError(null,null,true)){
		return;
	}
	var method="Article.quickEditSave";
	if(isQuickAdd){
		method = "Article.save";
		dc = initData;
		dc.ArticleID=dc.ID;
		dc.Status=0;
		dc.Title=title;
		dc.PageTitles=pTitles;
		dc.BodyText=bodyText;		
	}
	$("#EditorPanel").css("border",0);
	Server.sendRequest(method, dc, function(response){
			if(response.Status == 1) {
				oldTitle = $V("#Title");
				oldBodyText = $("_Contents").html();
				if(!linkFlag){
				contents = bodyText.split(PAGE_SPLIT);
				$("#_Contents").val(bodyText);
				articleAutoSave.lastPageTitles=pTitles;
				setContent(bodyText);
				//$("#Content").html(bodyText.replace(new RegExp(PAGE_SPLIT,'g'), '<p style="border-top: 3px #666 dashed;margin: 10px;"></p>'));
				}
				DataList.loadData("contentlist");
			} else {
				//Dialog.warn(response.Message);
			}
	});
}
Page.onReady(function(){
	$("#BtnSave").hide();//要尽量早地隐藏按钮，使toolbar执行fitToSize()方法时，能正常地布局
})
Page.onLoad(function() {
	onQuickEditorLoad();
	//zq 2015/05/20 disable扩展字段
	$('#contentform').disable();
	$("#trExtend").css("visibility", "visible");
	
	
	$("#ShortTitle + div").remove();
	resetPriv();
	if($("#TitleStyle").length) {
		$S("#TitleStyle", locals['articleQuickEditor'].TitleStyle);
	}
	if($("#ShortTitleStyle").length) {
		$S("#ShortTitleStyle", locals['articleQuickEditor'].ShortTitleStyle);
	}
	$("#ShortTitle + div").remove();
	var contentID = locals['articleQuickEditor'].ContentID;

	if(!isEmpty(contentID)){
		$('#Toolbar').enable();
	} else {
		$('#Toolbar').disable();
	}
	
	var _content = $("#_Contents").val();
	
	if(!isEmpty(_content)) {
		contents = _content.split(PAGE_SPLIT);
	}
	var _pageTitles = $V("#_PageTitles");
	articleAutoSave.lastPageTitles=_pageTitles||'';//记录最近一次保存后的标题
	if(!isEmpty(_pageTitles)) {
		pageTitles = _pageTitles.split(TITLE_SPLIT);
		$S("#PageTitle", pageTitles[0]);
	}
	lastData = Form.getData("contentform");
	if(locals['articleQuickEditor'].LinkFlag!=='Y' && !editStatus) {
		setContent(_content);
		//$("#Content").html($("#Content").html().replace(new RegExp(PAGE_SPLIT,'g'), '<p style="border-top: 3px #666 dashed;margin: 10px;"></p>'));
	}
	setTimeout(function(){
		//页面有其他代码修改button状态为可用，设置延时100毫秒执行判断内容是否发布再隐藏
		if(statusName!=Lang.get('Article.Search.Type.Published')){
			$("#btn_RecommendToBlock").disable();
			$("#BtnSendV").disable();
		}
	},100);
	
	//判断文档状态，如果是已发布则默认为浏览
	if (locals['contentScript'].Status == 30){
		var viewSelectBtn = $("#BtnPreview").getComponent('Button');
	    viewSelectBtn.setActiveItemById("View",false);
	}else{
		var viewSelectBtn = $("#BtnPreview").getComponent('Button');
		viewSelectBtn.setActiveItemById("Preview",false);
	}
});


