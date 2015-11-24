
var cid = locals['imageList'].CatalogID;
var ContentID=locals['imageList'].ContentID;
var siteID=locals['imageList'].SiteID;
var oldTitle, oldInfo;
var statusName = locals['imageList'].StatusName;

var editStatus = false;

function showImgGroupDetail(response) {
	$('#tdAddUser').html(response.AddUser);
	$('#tdAddTime').html(response.AddTime);
	$('#tdPublishDate').html(response.PublishDate);
	$('#tdStatusName').html(response.StatusName);
	if(response.TopFlag>0){
		$("#BtnSetTop").hide();
		$("#BtnSetNotTop").show();
	}else{
		$("#BtnSetTop").show();
		$("#BtnSetNotTop").hide();
	}
	$('#Title').val(response.Title);
	$('#Summary').val(response.Summary);
	$('#Tag').val(response.Tag);
	$('#Author').val(response.Author);
	$('#Editor').val(response.Editor);
	for(var key in response){
		if(key.startsWith("MetaValue_")) {
			if($("#" + key).length > 0) {
				$S("#" + key, response[key]);	
			} else {
				$NS(key, response[key]);	
			}
		}
	}
	if(locals['imageList'].DisableEdit=='true')
    {
    $('#trToolbar a').disable();
    }
	else{
	$('#trToolbar a').enable();
	}
	Application.setPrivParam("CatalogID",$V("CatalogID"));
	Application.setAllPriv();
}

function save(func) {
	if(Verify.hasError()){
		return;
	}
	if(!ContentID || ContentID == null) {
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	
	/*
	for(var editorKey in UE.instants) {
		if(UE.instants[editorKey].key.indexOf("MetaValue_") == 0) {
			$S(editorKey, UE.instants[editorKey].getContent());	
		}
	}
	*/
	
	
	var dc = Form.getData('form1');
	Server.sendRequest("ImageGroup.quickEditSave", dc, function(response){ 
		if(response.Status == 1) {
			oldTitle = $('#Title').val();
			oldInfo = $('#Summary').val();
			if(func) {
				func(response, refreshParentDataList);
			} else {
				MsgPop(response.Message);
				refreshParentDataList();
			}
		} else {
			Dialog.alert(response.Message);
		}
	});
}

function refreshParentDataList() {
	if($G("#contentlist")) {
		DataList.loadData("contentlist");
	}
}

function upload(){
	if(!ContentID || ContentID == null) {
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var groupID = ContentID
	var diag = new Dialog("uploadImageDiag");
	diag.width = 700;
	diag.height = 300;
	diag.title = Lang.get('Media.UploadImage');
	diag.url = "imageUpload.zhtml?CatalogID="+$V("CatalogID")+"&GroupID=" + groupID;
	diag.onOk = function() {
		$DW.uploadSave();
	};
	diag.onLoad = function(){
		
	};
	diag.show();
}


function uploadZip(){
	if(!ContentID || ContentID == null) {
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var groupID = ContentID
	var diag = new Dialog("uploadImageDiag");
	diag.width = 530;
	diag.height = 160;
	diag.title = Lang.get('Media.UploadImage');
	diag.url = "imageZipUploadDialog.zhtml?CatalogID="+$V("CatalogID")+"&GroupID=" + groupID;
	diag.onOk = function() {
		$DW.uploadSave();
	};
	diag.onLoad = function(){
		
	};
	diag.show();
}

function clickEdit(imageid) {
	var diag = new Dialog("editImageDiag");
	diag.width = 700;
	diag.height = 420;
	diag.title = Lang.get('Media.Dialog.EditImage');
	diag.url = "imageUpload.zhtml?GroupID=" + ContentID + "&ID=" + imageid;
	diag.onOk = function(){
		$DW.uploadSave();
	};
	diag.show();
}

function init(){
	if(locals['imageList'].DisableEdit=='true' || !Application.hasPriv("com.zving.cms.Catalog.Content.Edit."+locals['imageList'].CatalogID)){
		$("div.cxc").hide();
		$("div.potopic img").attr("onclick","");
    }
	onQuickEditorLoad();
	resetPriv();
	setTimeout(function(){
		//页面有其他代码修改button状态为可用，设置延时100毫秒执行判断内容是否发布再隐藏
		if(statusName!=Lang.get('Article.Search.Type.Published')){
			$("#btn_RecommendToBlock").disable();
		}
	},100);
}

function clickDelete(imageIDs) {
	//zq 增加GroupID参数以便删除图片后更新图集状态
	var dc = {ImageIDs:imageIDs,CatalogID:$V("CatalogID"),GroupID:ContentID};
	Dialog.confirm(Lang.get('Common.ConfirmDelete'), function() {
		Server.sendRequest("ImageUpload.del",dc,function(response){
			if(response.Status == 1) {
				MsgPop(response.Message);
				DataList.loadData("imgList",init);
				DataList.loadData("contentlist");
			} else {
				Dialog.alert(response.Message);
			}
		});
	});
}

function setCover(imageid) {
	var dc = {GroupID:ContentID,ID:imageid,CatalogID:$V("CatalogID")};
	Server.sendRequest("ImageList.setCover",dc,function(response){
		if(response.Status == 1) {
			MsgPop(response.Message);
			DataList.loadData("contentlist");
			DataList.loadData("imgList",init);
		} else {
			Dialog.alert(response.Message);
		}
	});
}


function isDirty() {
	if($('#Title').val() == oldTitle && $('#Summary').val() == oldInfo) {
		return false;
	}
	return true;
}



function clickView(id){
	zoom(CONTEXTPATH + "media/imageView.zhtml?ID="+id+"&CatalogID="+$('#CatalogID').val());
}

function zoom(url){
	var doc = rootWin.document;
	var sw = Math.max(doc.documentElement.scrollWidth, doc.body.scrollWidth);;
	var sh = Math.max(doc.documentElement.scrollHeight, doc.body.scrollHeight);;
	var cw = doc.compatMode == "BackCompat"?doc.body.clientWidth:doc.documentElement.clientWidth;
	var ch = doc.compatMode == "BackCompat"?doc.body.clientHeight:doc.documentElement.clientHeight;
	var zoomdiv = rootWin.$("#_zoomdiv");
	if(!zoomdiv.length){
		zoomdiv = rootWin.document.createElement("div");	
		zoomdiv.id = "_zoomdiv";
	 	rootWin.document.getElementsByTagName("body")[0].appendChild(zoomdiv);
		zoomdiv.innerHTML="\
			<div id='_zoomBGDiv' style='background-color:#222;position:absolute;top:0px;left:0px;opacity:0.9;filter:alpha(opacity=90);width:" + sw + "px;height:" + sh + "px;z-index:800'></div>\
			<iframe id='_zoomIframe' src='picturezoom.htm' frameborder='0' allowtransparency='true' style='background-color:#transparent;position:absolute;top:0px;left:0px;width:" + sw + "px;height:" + sh + "px;z-index:820;'></iframe>\
			";
		$(zoomdiv).hide();
	}
	rootWin._imagelistWindow=window;
	rootWin.$G("_zoomIframe").src=url;
	$(zoomdiv).show();
}

function cutting(id, path) {
	var diag = new Dialog("CuttingDiag");
	diag.width = 800;
	diag.height = 500;
	diag.title = Lang.get('Contentcore.ImageCutting.Title');
	diag.url = "../contentcore/commonImageCuttingDialog.zhtml?SiteID="+siteID+"&Path="+path;
	diag.onOk = function(){cuttingSave(id)};
	diag.show();
}

function cuttingSave(id) {
	var dc = {ID:id,CatalogID:$V("CatalogID")};
	Server.sendRequest("ImageUpload.cuttingSave", dc, function(response){
			if(response.Status == 1) {
				MsgPop(response.Message);
				DataList.loadData("imgList",init);
				DataList.loadData("contentlist");
				$D.close();
			} else {
				Dialog.warn(response.Message);
			}
	});
}

function Edit(func) {
	var diag = new Dialog("EiditDialog");
	diag.width = 750;
	diag.height = 500;
	diag.title = Lang.get('Contentcore.Edit');
	diag.url = CONTEXTPATH+"media/imageDialog.zhtml?CatalogID=" + $V("CatalogID") + "&ContentID="+ContentID+"&ContentType=" + locals['imageList'].ContentType;
	diag.onLoad = function(){
		$DW.$('#Title').focus();
	};
	if(locals['imageList'].DisableEdit=='true'){
		diag.onCancel = function(){
			$D.close();
		};
		diag.cancelText = Lang.get('Common.Close');
	}else{
		if(($V("IsLock")=='Y' && $V("LockUser")==$V("UserName"))||$V("IsLock")!='Y'){
			diag.onOk = function(){	
				if($DW.Verify.hasError()){
					return;
				}
			/*zq 2015/05/22
				for(var editorKey in $DW.UE.instants) {
					if(UE.instants[editorKey].key.indexOf("MetaValue_") == 0) {
						$DW.$S(editorKey,$DW.UE.instants[editorKey].getData());	
					}
				}
			*/
				var dc =$DW.Form.getData('form2');
				Server.sendRequest("ImageGroup.quickEditSave", dc, function(response){ 
					if(response.Status == 1) {
						oldTitle = $('#Title').val();
						oldInfo = $('#Summary').val();
						if(func) {
							func(response, refreshParentDataList);
						} else {
							MsgPop(response.Message);
							refreshParentDataList();
							diag.close();
						}
					} else {
						Dialog.alert(response.Message);
					}
				});
			}
		}
	}
	diag.show();	
}
function ImageafterRowSortEnd(evt, ui, ele){
	if(locals['imageList'].DisableEdit!='true' && Application.hasPriv("com.zving.cms.Catalog.Content.Edit."+locals['imageList'].CatalogID)){
		var oldRowIndex, newRowIndex;
	    var $row=ui.item;
	    oldRowIndex=$row.data('originalIndex');
	    newRowIndex=DataList.getRowIndex(ele,$row);
	    var pageIndex=DataList.getParam(ele, Constant.PageIndex);
	    //console.log('当前页码:',pageIndex,' 拖拽前行索引:',oldRowIndex,' 拖拽后行索引:',newRowIndex);
	    if(newRowIndex==oldRowIndex){
	        return;
	    }
		var dc = {oldRowIndex:oldRowIndex,newRowIndex:newRowIndex,pageIndex:pageIndex,ID:$row.find('input[name=ImageID]').val()};
	
	    /* 如果有排序字段，把排序字段的值也传到后台 */
	    var orderFlag = $row.find('input[name=OrderFlag]').val();//要求在当前行的隐藏域有保存排序权值
	    var listNodes = DataList.getListNodes(ele);
	    var targetOrderFlag = DataList("imgList").$listNodes.eq(newRowIndex > oldRowIndex ? newRowIndex-1 : newRowIndex+1).find('input[name=OrderFlag]').val();//要排到这一行的后面
	    dc.TargetOrderFlag=targetOrderFlag;
	    dc.OrderFlag=orderFlag;
	    //console.log('OrderFlag:',dc.OrderFlag,' TargetOrderFlag:',dc.TargetOrderFlag,' Position:',dc.Position);
	
	    DataList.showLoading(ele);
	    dc.CatalogID=$V('#CatalogID');
		Server.sendRequest("ImageList.dl1DataSort",dc,function(response){
			if(response.Status==1){
				MsgPop(response.Message);
	               DataList.loadData(ele);
			} else {
				Dialog.warn(response.Message);
			}
		});
	}
}

function uploadLogo(){
	var diag = new Dialog();
	diag.width = 805;
	diag.height = 450;
	diag.title = Lang.get('Contentcore.Content.UploadLogo');
	diag.url = "../contentcore/resourceDialog.zhtml?InputType=radio&SiteID="+$V("#SiteID")+"&DataType=File&DataID="+$V("#ContentID")+"&CatalogID="+$V("#CatalogID")+"&ResourceType=image&ImageWidth=120&ImageHeight=120";
	diag.onReady = function() {
		$DW.$( document ).on("pageinit", ".ui-page", function() {
			$DW.$("#trSourceType").hide();
		});
	}
	diag.onOk = function(){
		$DW.getImage(function(src,path,resourceID){
			var dc = {CatalogID:$V("CatalogID")};
			dc.resourceID=resourceID;
			dc.LogoFile = path;
			dc.GroupID = ContentID;
			Server.sendRequest("ImageGroup.saveLogo", dc, function(response){
				if(response.Status == 1) {
					MsgPop(response.Message);
					DataList.loadData("imgList",init);
					DataList.loadData("contentlist");
				} else {
					Dialog.warn(response.Message);
				}
			});
		});
	};
	diag.show();	
}

Page.onLoad(init);

