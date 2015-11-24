/* 将编辑器中的JS集中到本JS文件的目的是为了加快编辑器打开速度。 */
/* 占位符对应的值都存储在命名空间locals['articleEditor']对应的属性中；占位字段名到属性的转换关系为：占位字符串中单词之间的点号转换为"_"，如果占位符中有括号，则以"$"为属性前缀；
 * ；转换规则示例如下
 * ${Class.prop} --> locals['articleEditor'].Class_prop
 * ${(Class.prop)} --> locals['articleEditor'].$Class_prop
 */

//segment:
var PAGE_SPLIT ="<!--_ZVING_PAGE_BREAK_-->";
var TITLE_SPLIT ="|";
var CatalogID = locals['articleEditor'].CatalogID;//为imageselector插件准备
var InnerCode = locals['articleEditor'].InnerCode;//栏目内部编码
var pages = locals['articleEditor'].Pages; //页数
var currentPage = 1;//当前页
var pageTitles=[]; //页面标题数组
var contents; //页面内容数组
var editor; //UEditor编辑器
var statusName=locals['articleEditor'].StatusName;//文章状态
var isQuickEdit=false;
var editors = [];
var isFullsSreen;//编辑是否进入了全屏模式
function initUEditor(){
	if(editors.length > 0){
		return;
	}
	contents = $('#_Contents').val().split(PAGE_SPLIT);
	pageTitles = $V('_PageTitles').split(TITLE_SPLIT);
	if(pageTitles && pageTitles.length > 0) {
		$S("PageTitle", pageTitles[0]);
	}
	if(!locals['articleEditor'].HasEditPriv || locals['articleEditor'].HasEditPriv==='false'){
		//editor_extraPlugins = "autogrow";
	}
	if(editor){ // 如果编辑器已存在，则先销毁
		 editor.destroy();
	}
	editor = new UE.ui.Editor({ 
		bodyFontFamily:'inherit',
		sourceEditorFirst:false,
		initialContent:contents[0], //初始化编辑器的内容
		wordCount:false, //关闭字数统计
		minFrameHeight:350, // 无内容时编辑器的原始高度
		initialFrameWidth:690,  //初始化编辑器宽度,默认680
		initialFrameHeight:400, //初始化编辑器高度,默认400
		initialStyle:'body{font-size:14px;line-height:1.6;}',
		elementPathEnabled:false, //关闭elementPath
		autoFloatEnabled:false, //是否保持toolbar的位置不动,默认true
		iframeCssUrlOuter: locals['articleEditor'].ArticleUEEditorImportCSS!=''? Url.documentUrl.domain+locals['articleEditor'].ArticleUEEditorImportCSS:undefined,//给编辑器内部引入一个css文件
		catalogID : CatalogID,
		dataType : "Article",
		dataID : locals['articleEditor'].ContentID,
		imageWidth : locals['articleEditor'].ArticleImageWidth,
		imageHeight : locals['articleEditor'].ArticleImageHeight,
		allowAudioType : locals['articleEditor'].AllowAudioType,
		selfCatalog : 'Y',
		lang: locals['articleEditor'].UserLanguage,
		//langPath:window.UEDITOR_CONFIG.UEDITOR_HOME_URL+'lang/'+locals['articleEditor'].UserLanguage+'/'+locals['articleEditor'].UserLanguage+'.js',
		contextPath : CONTEXTPATH,
		page : 1,
		toolbars: [
				['undo', 'redo' , 'fullscreen'],
				['source', 'zcheckwords', 'selectall'],
				['fontfamily', 'fontsize'],
				['bold', 'italic', 'underline', 'forecolor', 'backcolor'],
				['customstyle'],
				['paragraph'],
				['justifyleft', 'justifycenter', 'justifyright', 'justifyjustify'],
				['autotypeset', 'indent'],
				['link', 'unlink','zcomment', 'zimage', 'zvideo', 'zaudio', 'zfile'], 
				['zcontent', 'zcatalog','zvote', 'zadvertise', 'zimagegroup', 'zpagebreak'],
				['touppercase', 'tolowercase', 'strikethrough'],
				['superscript', 'subscript','fontborder'],
				['insertorderedlist', 'insertunorderedlist'],
				['rowspacingtop', 'rowspacingbottom', 'lineheight'],
				['removeformat', 'formatmatch'],
				['cleardoc','searchreplace'],
				['directionalityltr', 'directionalityrtl','zwritingmode','zbodyfontfamily'],
				['imagenone', 'imageleft', 'imageright', 'imagecenter'],
				['date', 'time', 'map', 'gmap', 'zweiboshow'],
				['emotion', 'insertframe', 'spechars', 'anchor', 'horizontal'],
				['inserttable', 'deletetable','insertparagraphbeforetable'],
				['insertrow', 'deleterow', 'insertcol', 'deletecol'],
				['mergecells', 'mergeright', 'mergedown'], 
				['splittocells', 'splittorows', 'splittocols']
			],
			renderToolbarBoxHtml: function (){  // 重写ui.Editor的renderToolbarBoxHtml方法
			    return '<div class="zving-toolbarTab">'
					+	 '<div class="zving-toolbarTab-item item1 current">'+Lang.get('Article.Common')+'</div>'
					+	 '<div class="zving-toolbarTab-item item2">'+Lang.get('Article.Expanded')+'</div>'
					+  '</div>'
					+ '<div class="zving-toolbarCon">'
					+	 '<div class="zving-toolbarCon-item show">' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[0].renderHtml() + '</td></tr>' 
					+  				'<tr><td>' + this.toolbars[1].renderHtml() + '</td></tr>'
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Manipulate')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[2].renderHtml() + '</td></tr>' 
					+  				'<tr><td>' + this.toolbars[3].renderHtml() + '</td></tr>'
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Font')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[4].renderHtml() + '</td></tr>' 
					+  				'<tr><td>' + this.toolbars[5].renderHtml() + '</td></tr>'
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.HeadOrParagraph')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[6].renderHtml() + '</td></tr>' 
					+  				'<tr><td>' + this.toolbars[7].renderHtml() + '</td></tr>'
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Display')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[8].renderHtml() + '</td></tr>' 
					+  				'<tr><td>' + this.toolbars[9].renderHtml() + '</td></tr>'
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Insert')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[14].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[15].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Tools')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+  	 '</div>' 
					+	 '<div class="zving-toolbarCon-item">' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[10].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[11].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Font')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[12].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[13].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Paragraph')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[16].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[17].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Surround')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[18].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[19].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Insert')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[20].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[21].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.RowAndCell')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>'
					+	 	'<div class="zving-toolbarCon-item-separate">' 
					+			'<table border="0" cellpadding="2" cellspacing="0">' 
					+				'<tr><td>' + this.toolbars[22].renderHtml() + '</td></tr>' 
					+				'<tr><td>' + this.toolbars[23].renderHtml() + '</td></tr>' 
					+				'<tr><td class="zving-toolbarCon-item-separate-label">'+Lang.get('Article.Cell')+'</td></tr>' 
					+			'</table>'
					+	 	'</div>' 
					+  	 '</div>' 
					+ '</div>'
			}
	});
	editor.render('Content_1');
	//删除表示工具条中功能按钮的a元素中的href属性，防止IE中触发beforeunload
	/*
	if(isIE&& ieVersion<9){
		$('#Toolbar_body a.z-btn ').removeAttr('href');
	}
	*/
	//
	editors.push(editor);
	var $toolbarWrap=$("#editorToolbarWrap");
	editor.ready(function(){
		
		var me = this;
		UE.dom.domUtils.on(me.window,'focus',function(){ // 根据当前focus的编辑器来切换显示/隐藏工具栏
			$("#editorToolbarWrap .edui-editor-toolbarbox").hide(); 
			$(me.ui.getDom("toolbarbox")).show();
		});
		UE.dom.domUtils.on(me.container,'click',function(){ // 根据当前focus的编辑器来切换显示/隐藏工具栏
			$("#editorToolbarWrap .edui-editor-toolbarbox").hide(); 
			$(me.ui.getDom("toolbarbox")).show();
		});
		updateEditorUI();
		initPages();
		
		setEditorContainerHeight();
		fixedInWin($toolbarWrap);
		/*
		$(".zving-toolbarTab-item").on('click', function(){
			editor.focus();
		});
		*/
		// 页签切换
		$('#editorToolbarWrap').on('click','.zving-toolbarTab > .zving-toolbarTab-item',function(evt){ 
			$(this).addClass("current").siblings().removeClass("current").parents(".edui-editor-toolbarboxinner").find(".zving-toolbarCon-item").hide().end().find(".zving-toolbarCon-item:eq("+ $(this).index() +")").show();
		});
		//ff中的图片不能调整尺寸，必须切换到源码模式后再切换回来才可以。
		if(UE.browser.gecko){
			editor.execCommand('source');
			editor.execCommand('source');
		}
		lastData = getArticleDataCollection();
	});
	//维护全屏切换时的尺寸
	var positionBackup={};
	editor.addListener('fullscreenchanged', function (e, fullScreen) {
		if(fullScreen){
			isFullsSreen=true;
			positionBackup.toobarWrap_width=$toolbarWrap.css('width');
			positionBackup.toobarWrap_top=$toolbarWrap.css('top');
			positionBackup.toobarWrap_left=$toolbarWrap.css('left');
			positionBackup.toobarWrap_position=$toolbarWrap.css('position');
			$toolbarWrap.css({top:0,left:0, width:'100%',position:'absolute',zIndex:1333});
			editor.ui.getDom('toolbarboxouter').style.width='auto';
			editor.ui.getDom('iframeholder').style.marginTop=$toolbarWrap.height()+'px';
		}else{
			isFullsSreen=false;
			$toolbarWrap.css({
				top:positionBackup.toobarWrap_top,
				left:positionBackup.toobarWrap_left,
				 width:positionBackup.toobarWrap_width,
				 position:positionBackup.toobarWrap_position,
				 zIndex:1333
			});
			editor.ui.getDom('iframeholder').style.marginTop='0';
			editor.ui.getDom('iframeholder').style.width='680px';
			editor.ui.getDom().style.width='680px';
		}
	});
	//维护源码模式切换时的尺寸
	var iframeholderHeight;
	editor.addListener('sourcemodechanged', function (e, sourceMode) {
		if(sourceMode){
			iframeholderHeight=editor.ui.getDom('iframeholder').style.height;
			//editor.ui.getDom('iframeholder').style.height=$('.CodeMirror-gutter').height()+'px';
		}else{
			editor.ui.getDom('iframeholder').style.height=iframeholderHeight;
		}
	});
}


// editor编辑器实例化成功后更改相应外观进
function updateEditorUI(){
	// 将编辑工具条移到目标区域中
	var editorToolbarboxEl = editor.ui.getDom("toolbarbox");
	$("#editorToolbarWrap").append(editorToolbarboxEl); 

	/*
	// 页签切换
	$(".zving-toolbarTab > .zving-toolbarTab-item").click(function(){ 
		$(this).addClass("current").siblings().removeClass("current").parents(".edui-editor-toolbarboxinner").find(".zving-toolbarCon-item").hide().end().find(".zving-toolbarCon-item:eq("+ $(this).index() +")").show();
	});
	*/
}

// 编辑工具栏固定在视窗中
function fixedInWin($toolbarWrap){
	$("#_DivContainer").scroll(function(){
		var $eduiPopup = $(".edui-popup");
		if($eduiPopup.length && $eduiPopup.is(":visible")){ // 解决【弹出下拉层】 只能监听window对象滚动隐藏的bug
			$eduiPopup.hide();
		}
		
	});
}

Page.onLoad(function(){
	resetPriv();
	//工作流按钮
	if(window.loadButtons) {
		var dc = {ConfigProps:locals['articleEditor'].$ConfigProps,Status:locals['articleEditor'].Status,CatalogID:locals['articleEditor'].CatalogID};
		//dc.add("ConfigProps", locals['articleEditor'].$ConfigProps);
		//dc.add("Status", locals['articleEditor'].Status);
		//dc.add("CatalogID", locals['articleEditor'].CatalogID);
		loadButtons(dc);
	}
	if(!Application.hasPriv("com.zving.cms.Catalog.Content.Copy."+locals['articleEditor'].CatalogID)) {
		$("#trQuoteChannel a").css("color", "#aaa").each(function(){this.onclick=null;});
	}
	if(!Application.hasPriv("com.zving.cms.Catalog.Content.Edit."+locals['articleEditor'].CatalogID)) {
		$("#SummaryAutoExtract").hide();
	}
},1);

function showMember(){
	var diag = new Dialog("会员列表","../contentcore/contentMember.zhtml",600,400);
	diag.onOk =selectMember;
	if(!Application.hasPriv("com.zving.cms.Catalog.Content.Copy."+locals['articleEditor'].CatalogID)) {
		$("#trQuoteChannel a").css("color", "#aaa").each(function(){this.onclick=null;});
	}
	if(!Application.hasPriv("com.zving.cms.Catalog.Content.Edit."+locals['articleEditor'].CatalogID)) {
		$("#KeywordAutoExtract").disable();
		$("#SummaryAutoExtract").hide();
	}
}


//页面加载完成之后，判断当前内容是否发布，未发布就禁用需要发布才能用的按钮
if(window.addEventListener){
	window.addEventListener("load", onLoadDisable, false);
}else{
	window.attachEvent('onload', onLoadDisable);
}
function onLoadDisable(){
	var statusName = $V("StatusName");
	if(statusName!=Lang.get('Article.Search.Type.Published')){
		$("#btn_RecommendToBlock").disable();
		$("#BtnSendV").disable();
	}
	var ShowSubTitleAndShortTitle = locals['articleEditor'].User_ShowSubTitleAndShortTitle;
	var ShortTitle = $V("ShortTitle").trim();
	var SubTitle = $V("SubTitle").trim();
	if(ShowSubTitleAndShortTitle=="N"){
		if(ShortTitle!=""||ShortTitle.length!=0){
			$("#trShortTitle").toggle();
			$("#ShowShortTitle").prop('checked',true);
		}
		if(SubTitle!=""||SubTitle.length!=0){
			$("#trSubTitle").toggle();
			$("#ShowSubTitle").prop('checked',true);
		}
	}
}

function endTopDate(){
	var topFlag=$NV("TopFlag");
	if(topFlag=="Y"){
	var diag = new Dialog("setTopDialog");
	diag.width = 400;
	diag.height = 100;
	diag.title = Lang.get('Contentcore.SetTop');
	diag.url = CONTEXTPATH+"contentcore/contentTopDialog.zhtml";
	diag.onOk = function(){
		var endTopDate=$DW.$V("TopDate");
		$S("TopDate",endTopDate);	
		diag.close();
		}
	diag.show();
	}
	var json1=JSON.stringify(detaliArry);
	$("#jsoncontentmember").attr("value",json1);
	$D.close();
}
function trShortTitleToggle(){
	$("#trShortTitle").toggle();
}
function trSubTitleToggle(){
	$("#trSubTitle").toggle();
}
//segment:
var lastData;//上一次保存的数据
// 替换敏感词标记
function replaceBadWordTag(contents) {
	var res = contents.match(/<span style="background-color:\s*yellow;?">(.*?)<\/span>/gi);
	if(!isEmpty(res)) {
		for(var i = 0; i < res.length; i++) {;
			var reg = /<span style="background-color:\s*yellow;?">(.*?)<\/span>/gi;
			var res2 = reg.exec(res[i]);
			console.log("replaceBadWordTag:" + res2[0]+"--"+res2[1]);
			contents = contents.replace(res2[0], res2[1]);
		}
	}
	return contents;
}

//获取当前编辑器的数据集合
function getArticleDataCollection(){
	// 富文本扩展字段问题,把对应的textarea的name设置为后端接收的字段名即可
	/*
	for(var instantsId in UE.instants) {
		var editorKey=UE.instants[instantsId].key;
		if(editorKey.indexOf("MetaValue_") == 0) {
			//$S(editorKey, UE.instants[instantsId].getContent());	
		}
	}
	*/
	var dc = Form.getData("form1");
	if(editor){
		//校验文章内容非空
		dc.PageTitles = getPageTitles();
		dc.BodyText = getPageData();
		dc.BodyText = replaceBadWordTag(dc.BodyText);
		delete dc.TextColor;
		delete dc._Contents;
		delete dc.editorValue;
	}
	return dc;
}

//检测数据集合中的内容字段是否为空
function isContentEmpty(dc){
	var content = dc.BodyText;
	content = content.replace(/<br \/>/g,"");
	content = content.replace(/<p>\&nbsp;<\/p>/g,"");
	if(content.trim()==""){
		return true;
	}
	return false;
}

//刷新父级列表页面
function refreshList(){
	var w = opener;
	if(w) {//全屏编辑器
		w.AllDocumentsEvent.fire('updatecontentlist');
	} else {//快速编辑模式
		AllDocumentsEvent.fire('updatecontentlist');
	}
}

//刷新数据到父级页面
function refreshData(response){
	refreshList();
	var w;
	w = opener;
	while(1){
		if(w) {//全屏编辑器
			if(!w.$G("contentlist")){
				w = w.parent;
			}	
					
			if(w && w.$G("contentlist")) {
				w.DataList.loadData("contentlist");		
				
				//zq 2015/05/14 解决清除扩展内容
				w.location.hash = CONTEXTPATH + "article/articleQuickEditor.zhtml?ContentID="+$V("#ContentID")+"&CatalogID="+ $V("#CatalogID") + "&t=" + new Date();
				
				w.$G('SelectedID_'+$V("#ContentID")).click();
			}
			
		} else {//快速编辑模式
			break	
		}
		w = w.opener;
	}
}

//保存文章,如果返回数据的Status字段为1，则调用func参数
function save(func, dontShowAlert/*不要弹出提示*/){
	if(Verify.hasError(null,null,dontShowAlert?true:false)){
		//$('#tips').html(Lang.get("Verify.HasError"))
		return;
	}
	var dc = getArticleDataCollection();
	// 增加权限校验，避免使用快捷保存的时候，出现越权操作。
	if(dc.Method == 'ADD'){
		if(!Application.hasPriv("com.zving.cms.Catalog.Content.Add."+locals['articleEditor'].CatalogID)) {
			if(!dontShowAlert){
				Dialog.alert(Lang.get('Contentcore.NeedAddPriv'));
			}else{
				$('#tips').html(Lang.get('Contentcore.NeedAddPriv'))
			}
			return;
		}
	}else if(dc.Method == 'UPDATE'){
		if(!Application.hasPriv("com.zving.cms.Catalog.Content.Edit."+locals['articleEditor'].CatalogID)) {
			if(!dontShowAlert){
				Dialog.alert(Lang.get('Contentcore.NeedContentEditPriv'));
			}else{
				$('#tips').html(Lang.get('Contentcore.NeedContentEditPriv'))
			}
			return;
		}
	}
	if($V('#LinkFlag')!="Y"){
		if(isContentEmpty(dc)){
			if(!dontShowAlert){
				Dialog.alert(Lang.get('Article.CheckContentEmpty'));
			}else{
				$('#tips').html(Lang.get('Article.CheckContentEmpty'))
			}
			return;
		}
	}
	//判断自动下载远程图片，如果为空负值为N
	if($V('#CopyImageFlag')=='Y'){
		dc.CopyImageFlag='Y';
	}else{
		dc.CopyImageFlag='N';
	}
	Server.sendRequest("Article.checkBadWord", dc, function(response){
			if(response.Status == 1) {
				saveToServer(dc,function(response){
					if(response.Status&&func){//如果有传递函数参数，则不弹出alert信息
						func(response);	
					}else{
						/*
						if(!dontShowAlert){
							Dialog.alert(response.Message);
						}
						*/
						if(response.Status == 0){
							Dialog.alert(response.Message);
						} else {
							MsgPop(response.Message);
						}
					}
				},dontShowAlert);
			} else if(response.HasBadWord){
				var confirm = Dialog.confirm(Lang.get('Article.ConfirmBadWord'), function(){
						saveToServer(dc,function(response){
							if(response.Status&&func){//如果有传递函数参数，则不弹出alert信息
								func(response);	
							}else{
								if(!dontShowAlert){
									Dialog.alert(response.Message,null,response.Status);
								}else{
									$('#tips').html(response.Message)
								}
							}
						},dontShowAlert);
					}, function(){
						dc.ReplaceBadWord="Y";
						saveToServer(dc,function(response){
							if(response.Status&&func){//如果有传递函数参数，则不弹出alert信息
								func(response);	
							}else{
								window.onbeforeunload=null;
								if(!dontShowAlert){
									Dialog.alert(response.Message,function(){window.location.href="articleEditor.zhtml?CatalogID="+$V('#CatalogID')+"&ContentID="+$V('#ContentID');},response.Status);
								}
							}
						},dontShowAlert);
					});
				confirm.okButton.innerHTML=Lang.get('Common.Save');
				confirm.cancelButton.innerHTML=Lang.get('Article.ReplaceBadWord')
				confirm.addButton("btnReplaceBadWord", Lang.get('Common.Cancel'), function(){
						confirm.close();
						var badWords = response.Message.split(" ");
						contents[currentPage-1] = editor.getContent(false);
						for(var j = 0; j < contents.length; j++) {
							for(var i = 0; i < badWords.length; i++) {
								contents[j] = contents[j].replace(new RegExp(badWords[i], "gmi"), '<span style="background-color:yellow;">'+badWords[i]+"</span>");
							}
						}
						editor.setContent(contents[currentPage-1]);
					});
				if(dontShowAlert){
					confirm.onOk();
				}
			}else{
				if(!dontShowAlert){
					Dialog.alert(response.Message,null,response.Status);
				}else{
					$('#tips').html(response.Message)
				}
			}
		});
	
}

//调用服务器端的文章保存方法，服务器端返回后以func(response)的方式调用参数方法
function saveToServer(dc,func,dontShowAlert){
	if(!dontShowAlert){
		var diag = Dialog.wait(Lang.get('Article.Processing')+"...");
	}
	Server.sendRequest("Article.save",dc,function(response){
			if(diag){
				diag.close();
			}
			
			if(response.Status==1){
				var st = response.SaveTime;
				if($("#SaveTime").length){
					$('#SaveTime').html(st);
					$S('Method',"UPDATE"); //保存后将状态设置为更新，
					/*这个会导致在新建文章中第一次保存后的第一次判断是否有脏数据时，
					lastData和页面获取的dc的值不同(lastData.Method==='ADD',而dc.Method==='UPDATE'),
					所以在判断是否有脏数据时，不考虑Method的值，因为这个值并不是数据，只是用于和后端交互操作的需要的状态数据。
					*/
					
					
				}
				$S("#Keyword",response.Keyword);
				lastData = dc;//更新最后保存数据				
				lastData['Method']='UPDATE';
				lastData['Keyword']=response.Keyword;
				if(response.TopFlag >0){
					$S('OldTopFlag',"Y");
					lastData['OldTopFlag']="Y";
				}else{
					$S('OldTopFlag',"N");
					lastData['OldTopFlag']="N";
				}
				refreshData(response);
			}
			if(func){
				func(response);
			}
		});
}

//文章转为待发布状态
function topublishArt(saveFirstFlag){//saveFirstFlag表示是否先保存文章
	if(saveFirstFlag && isDirty(getArticleDataCollection())&&!$("#BtnSave").prop("disabled")){
		save(function(){
			topublishArt(false);
		});
		return;
	}
	// 调用contentScript.zhtml中通用待发布方法
	_topublish($V("#ContentID"), $V("CatalogID"), refreshList);
}

//发布文章
function publishArt(saveFirstFlag){//saveFirstFlag表示是否先保存文章
	var DownlineDate=$V("#DownlineDate");
	var PublishDate=$V("#PublishDate");
	if(DownlineDate && PublishDate && DateTime.parseDate(DownlineDate).getTime()<DateTime.parseDate(PublishDate).getTime()){
		Dialog.alert(Lang.get('Comment.OfflineTimeLessReleaseTime')); 
		return;
	}
	
	if(saveFirstFlag && isDirty(getArticleDataCollection())&&!$("#BtnSave").prop("disabled")){
		save(function(){
			publishArt(false);
		});
		return;
	}
	_publish($V("#ContentID"), $V("#CatalogID"), function(response){
			refreshData(response);
			if(locals['articleEditor'].User_CloseWindowAfterPublish=='Y') {
				closeX();
			}
		});
}

function historyAutoSave(){
	if($V("Method") == 'ADD' && $V("#OpenArticleAutoSave") != "Y") {
		save(historyAutoSave);
		return;
	}
	var dc = {ContentID:$V('#ContentID'),CatalogID:$V("#CatalogID")};
	Server.sendRequest("ArticleHistory.historyAutoSave",dc,function(response){
		/*
		if(response.Status==1){
			MsgPop(response.Message);
		}
		*/
	});
}

//添加引用
function setReferName(ele){
   $S('ReferName',$G(ele).value);
}

//复制
function copyDialog(){
	if($V("Method") == 'ADD') {
		save(function(){
				copyDialog();
			});
		return;
	}
	_copy($V("ContentID"), "Article", $V("CatalogID"), document.getElementById("LinkFlag").checked);
}

function version(){
	if($V("Method") == 'ADD') {
		save(version);
		return;
	}
	var diag = new Dialog({
			title:Lang.get('Article.Version.History'),
			url:CONTEXTPATH + "article/articleVersion.zhtml?CatalogID=" + $V("CatalogID") + "&ArticleID="+$V('#ContentID'),
			width:690,
			height:380
		});
	diag.show();
}

function closeX(){
	window.close();
}

function note(){
	_note("Article", $V("ContentID"), $V("CatalogID"));
}

function addRelaArticle(){
	if($V("Method") == 'ADD') {
		save(function(){
				addRelaArticle();
			});
		return;
	}
	_relative($V("ContentID"), $V("CatalogID"), $V("RelativeContent"), function(response){
			$S('RelativeContent', response.RelativeContent);
		});
}
 
function htmlDecode(str) {
	return str.replace(/\&quot;/g,"\"").replace(/\&lt;/g,"<").replace(/\&gt;/g,">").replace(/\&nbsp;/g," ").replace(/\&amp;/g,"&");
}

function changeDocType(){
	initUEditor();
	if($V('LinkFlag')=="Y"){
		$('#DivRedirect').show();
		$('#DivContent').hide();
		editor.setContent(null);  //勾选标题新闻，则清空编辑器内容
	} else {
		$('#DivRedirect').hide();
		$('#DivContent').show();
	}
}

Page.onLoad(function(){
	onEidtorLoad();
	
	changeDocType();

	if($V('LinkFlag')!="Y"){
		pageTitles = $V('_PageTitles').split(TITLE_SPLIT);
		if(pageTitles && pageTitles.length > 0) {
			$S("PageTitle", pageTitles[0]);
		}
		if(Application.hasPriv("com.zving.cms.Catalog.Content.Edit."+locals['articleEditor'].CatalogID)) {
			if($V("#OpenArticleAutoSave") == "Y"){
				setInterval(articleAutoSave, 1000*60);//设置文章自动保存，每分钟执行一次
			}
		}
	}
	setEditorContainerHeight();
},2);
EventManager.onWindowResize(setEditorContainerHeight);
function setEditorContainerHeight(){
	var height = (window.document.body.clientHeight-$("#_DivContainer").offset().top);
	$('#_DivContainer').css('height', height);
	//console.log(height)
	$G('#_Td1').height=height-(isFirefox?22:72);
}

function preview(){
	if($V('Method')=="ADD"){
		Dialog.alert(Lang.get('Article.CannotPreview'));
		return;
	}else{
	  window.open("../contentcore/preview.zhtml?path="+CONTEXTPATH+"content/preview&ContentType=Article&ID="+$V('ContentID'));
  }
}

function create(){
	var width  = screen.availWidth-10;
	var height = screen.availHeight-50;
	var leftm  = 0;
	var topm   = 0;
	var args = "toolbar=0,location=0,maximize=1,directories=0,status=0,menubar=0,scrollbars=1, resizable=1,left=" + leftm+ ",top=" + topm + ", width="+width+", height="+height;
	var url = "articleEditor.zhtml?CatalogID="+$V('CatalogID');
	
	if($V('Method')=="ADD"){
		Dialog.confirm(Lang.get('Article.ConfirmCreateNew')+"？",function(){
			var w = window.open(url,"",args);
			if(!w){
				Dialog.alert(Lang.get('Common.BrowserTip')) ;
				return ;
			}
		});
	}else{
		var w = window.open(url,"",args);
		if(!w){
			Dialog.alert(Lang.get('Common.BrowserTip')) ;
			return ;
		}
	}
}

function getKeywordOrSummary(type){
	var dc = {};
	var content = "";
	if("Y"!=$V("#LinkFlag")){
		content = getPageData();		
		if(isEmpty(content)) {
			Dialog.warn(Lang.get('Article.CheckContentEmpty'));
			return;
		}
	}
	dc.Content = content;
	dc.Title = $V('Title');
	dc.Type = type;
	dc.CatalogID = $V('CatalogID');
	dc.SiteID = $V('SiteID');
	dc.LinkFlag = $V("#LinkFlag");
	Server.sendRequest("Article.getKeywordOrSummary",dc,function(response){
		if(response.Status==0){
			Dialog.alert(response.Message);
		}else{
			$S(type,response.Text);
			$G(type).focus();
		}
	});
}

function checkMandatory(ele){
	var defaultValue = locals['articleEditor'].defaultImageValue;
	var imagevalue = $V(ele);
	if (!ele || imagevalue==defaultValue) {
		return false;
	}
	return true;
}

function changeCatalog(saveFirstFlag) {
	if($V("Method") == 'UPDATE' && !Application.hasPriv("com.zving.cms.Catalog.Content.Move."+locals['articleEditor'].CatalogID)) { //编辑文章，修改文章所属栏目，需要拥有栏目下内容转移的权限
		Dialog.alert(Lang.get('Article.ChangeCatalog.NoPriv'));
		return;
	}
	var diag = new Dialog("diagChangeCatalog");
	diag.width = 430;
	diag.height = 450;
	diag.title = Lang.get('Article.SelectViewCatalog');
	diag.url = CONTEXTPATH+"contentcore/catalogSelector.zhtml?ContentType=Article&Type=Default,Link&InputType=radio&CatalogID="+$V('CatalogID');
	var oldCatalogID = $V("CatalogID");
	diag.onOk = function() {
		var catalogIDs = $DW.Tree('tree1').getCheckedData('cid');
		if(!catalogIDs || catalogIDs.length == 0) {
			Dialog.alert(Lang.get('Article.SelectCatalogFirst'));
			return;
		}
		//新建文章，修改文章栏目，保存并刷新页面
		if($V("Method") == 'ADD') {
			if(!Application.hasPriv("com.zving.cms.Catalog.Content.Add."+catalogIDs[0])) { //新建文章，修改文章所属栏目，需要拥有目标栏目新建内容的权限
				Dialog.alert(Lang.get('Article.NeedAddPriv'));
				return;
			}
			var dc = getArticleDataCollection();
			if($V('LinkFlag')!="Y"){
				if(isContentEmpty(dc)){
					Dialog.alert(Lang.get('Article.CheckContentEmpty'));
					return;
				}
			}
			$S("CatalogID",catalogIDs[0]);
			save(function(response){
				if(response.Status == 1) {
					MsgPop(response.Message);
					$D.close();
					refreshList();
					location.href=CONTEXTPATH+"article/articleEditor.zhtml?ContentID=" + $V('#ContentID') +"&CatalogID="+catalogIDs[0];
				} else {
					Dialog.alert(response.Message);
				}
			});
			return;
		}
		//编辑文章，修改文章栏目，先保存内容，然后将文章转移到目标栏目并刷新页面
		if(saveFirstFlag){
			save(function(){
				_moveSave($V('ContentID'), catalogIDs.join(), $V("CatalogID"), function(response){
						refreshList();
						location.href = CONTEXTPATH+"article/articleEditor.zhtml?ContentID=" + $V('ContentID') +"&CatalogID="+catalogIDs[0];
					});
			});
		}
	};
	diag.show();
}

function articleAutoSave(){
	
	if($("#BtnSave").prop("disabled")){
		return;//保存按钮不可用则不自动保存
	}
	
	if(isDirty(getArticleDataCollection())){
		if(Verify.hasError(null,null,true)){
			//$('#tips').html(Lang.get("Verify.HasError"))
			return;
		}
		save(function(response){
			if(response.Status == 1) {
				$('#tips').html(Lang.get('Article.LastSaveTime')+'：'+DateTime.getCurrentDate()+" "+DateTime.getCurrentTime());
			}
		}, true);
	}
}

// 分页重构
/**
 * 初始化分页编辑器
 */
function initPages() {
	var e = editor;
	initPages.pageSize = contents.length;
	initPages.reayPageAmount = 1;
	for(var i = 1; i < contents.length; i++) {
		var e = ZPageBreak.addPageBlock(e, i+1); // 得到分页的编辑器实例
		e.addListener('ready', (function(content){ // 给编辑器设置分页内容
			return function(type, evt) {
				initPages.reayPageAmount++;
				ZPageBreak.boolbarSwitch(this,content);
				//ff中的图片不能调整尺寸，必须切换到源码模式后再切换回来才可以。
				if(UE.browser.gecko){
					this.execCommand('source');
					this.execCommand('source');
				}
				if(initPages.reayPageAmount==initPages.pageSize){
					lastData = getArticleDataCollection();//当有分页时，加载完最后一个分页后重新获取初始数据
				}
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
	try{
		var arr = $(".divPageTitle");
		for(var i = 0; arr && i < arr.length; i++) {
			var index =  arr[i].id.split("_")[1]; // 截取分页标题的序号：分页标题的id（divPageTitle_1、divPageTitle_2...）
			var e = UE.getEditor("Content_" + index);
			if(c.length > 0) {
				c += PAGE_SPLIT;
			}
			c += e.getContent();
		}
	}catch(ex){}
	return c;
}

/**
 * 获得分页标题合并值
 */
function getPageTitles() {
	return $NV("PageTitle").join(TITLE_SPLIT);
}
var Confirm=true;//版本切换时，不提示，直接刷新。
window.onbeforeunload = function(){
	if(!Confirm)return;
	return onbeforeunloadHandler();
}
function onbeforeunloadHandler(){
	if(isDirty(getArticleDataCollection())){
		return Lang.get('Article.OnBeforeUnloadMessage');
	}
}

function uploadLogo(){
	var diag = new Dialog();
	diag.width = 805;
	diag.height = 450;
	diag.title = Lang.get('Article.UploadLogoImage');
	diag.url = "../contentcore/resourceDialog.zhtml?InputType=radio&SiteID="+$V("SiteID")+"&DataType=Article&DataID="+$V("ContentID")+"&CatalogID="+$V("CatalogID")+"&ResourceType=image&ImageType=logo&ImageWidth=120&ImageHeight=120&ResourceID="+$V("#ResourceID");
	diag.onReady = function() {
		$DW.$( document ).on("pageinit", ".ui-page", function() {
			$DW.$("#trSourceType").hide();
		});
	}
	diag.onOk = function(){
		$DW.getImage(function(src,path,resourceID){
				$("#LogoSrc").attr("src",src);
				$S("#LogoFile",path);
				canCutting()
		});
	};
	diag.show();
	//isIE6 && stopEvent()
}
//segment:
function removeSizeSuffix(url){
	var i1 = url.lastIndexOf("_");
	var i2 = url.lastIndexOf("x",url.lastIndexOf("."));	
	if(i2>0&&i1>0&&i2>i1){
		url = url.substring(0,i1)+url.substring(url.lastIndexOf("."));	
	}
	var firstChar = url.substring(0,1);
	if(firstChar == "/"){
		url = url.substring(1);
	}
	return url;	
}
function cleanLogo(){
		$("#LogoSrc").attr("src",'../platform/images/addpicture.png');
		$S("#LogoFile",'clean');
		if(isIE && ieVersion<9){
			window.onbeforeunload = null;
		}
}
function editLogo() {
	var diag = new Dialog("CuttingDiag");
	diag.width = 800;
	diag.height = 500;
	diag.title = Lang.get('Contentcore.ImageCutting.Title');
	var path = $V("LogoFile");
	if(!path){
		return;
	}
	path = removeSizeSuffix(path);
	diag.url = "../contentcore/commonImageCuttingDialog.zhtml?SiteID="+$V("SiteID")+"&Path="+path;
	diag.onOk = function(){
		var dc = {SiteID:$V("SiteID"),Path:path};
		var wait = Dialog.wait(Lang.get('Contentcore.WaitingTip'));
		Server.sendRequest("CatalogResources.cuttingSave", dc, function(response){
			wait.close();
			if(response.Status == 1) {
				$D.close();
				var src = $("#LogoSrc").attr("src");
				if(src.indexOf("?") > 0) {
					src = src.substring(0, src.indexOf("?"));
				}
				src += "?" + new Date().getTime();
				$("#LogoSrc").attr("src", src);
			} else {
				Dialog.warn(response.Message);
			}
		});
	};
	diag.show();	
}
function canCutting(){
	var src = $V("#LogoFile");
	if(src && src.toLowerCase().startsWith("http")) {
		$("img.btn_cutting").hide();
	}else{
		$("img.btn_cutting").show();
	}
}
Page.onLoad(function(){
	canCutting();
	
	//判断文档状态，如果是已发布则默认为浏览
	if (locals['contentScript'].Status == 30){
		var viewSelectBtn = $("#BtnPreview").getComponent('Button');
	    viewSelectBtn.setActiveItemById("View",false);
	}else{
		var viewSelectBtn = $("#BtnPreview").getComponent('Button');
		viewSelectBtn.setActiveItemById("Preview",false);
	}});