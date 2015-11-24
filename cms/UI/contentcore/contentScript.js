// 内容列表用通用js方法
function selectAll(el){
	var status=el.checked;
	$N("SelectedID") && $N("SelectedID").each(function(ele){
		if(ele.checked!==status){ele.checked=status;$(ele).trigger('change');}
	});
	el.checked=status;
}

function unSelectAll(){
    $G('sel').checked=false;
    $N("SelectedID").each(function(ele){
        ele.checked = false;
    });
}

function onRowClick(ele, notSyncQuickEditor){
	if(isFirefox){//在firefox下，单击事件会取消掉双击事件，导致双击失效
		window.setTimeout(_onRowClick,100)
	}else{
		_onRowClick();
	}
	function _onRowClick(){
	 	var id = ele.getAttribute("id").substring(3);
	 	var cb = $G("SelectedID_"+id);
	
	    if(notSyncQuickEditor){
	        return;
	    }
	    $(cb).trigger('change');
	 	if(cb.checked){
			showInQuickEditor(id);
	 	}else{
			cleanAndDisableQuickEditor();
	 	}
	}
 	
}

function showInQuickEditor(id){
	var editorURL = $V("QuickEditURL_"+id);
	if(isEmpty(editorURL)) {
		editorURL = "contentcore/contentQuickEditor.zhtml?ContentID={ID}&CatalogID={CatalogID}";
	}
	editorURL = editorURL.tmpl({ ID : id, CatalogID : locals['contentScript'].CatalogID });
	location.hash = CONTEXTPATH + editorURL + "&t=" + new Date().getTime();
} 

function cleanAndDisableQuickEditor(id){
	if(window.clearDetail)
		clearDetail();

}

/**
 * 列表页通用标题验证方法
 */
function verifySameTitle(){
	_verifySameTitle($V("Title"), $V("ContentID"), $V("CatalogID"), function(response){
		if(response.Status==1){
			$('#trVerifyTitle').show();
			if(response.TitleCheckType == "Site"){
				$('#sitenotice').show();
				$('#catalognotice').hide();
			}else{
				$('#catalognotice').show();
				$('#sitenotice').hide();
			}
		}else{
			$('#trVerifyTitle').hide();
		}
	});
}

/**
 * 通用标题重复验证方法
 * @param 标题
 * @param 内容ID
 * @param 栏目ID
 */
function _verifySameTitle(title, contentid, catalogid, func){
	if(!isEmpty(title)){
		var dc = { ContentID:contentid, Title:title, CatalogID:catalogid };
		Server.sendRequest("Content.verifySameTitle",dc,function(response){
			if(func){
				func(response);
			}
		});	 
	}
}

function add(catalogid){
	var diag = new Dialog("addDialog");
	diag.width = 750;
	diag.height = 500;
	diag.title = Lang.get('Common.New');
	diag.url = CONTEXTPATH+"contentcore/contentDialog.zhtml?CatalogID=" + catalogid + "&ContentType=" + locals['contentScript'].ContentType ;
	diag.onLoad = function(){
		$DW.$('#Title').focus();
	};
	diag.onOk = function(){	
			if($DW.Verify.hasError()){
				return;
			}
			var dc = $DW.Form.getData("contentform");
			saveContent(dc, function(){
					$D.close();
					DataList.loadData("contentlist");
					AllDocumentsEvent.fire({type:'updatecatalogtree',cid:catalogid}); //触发所有iframe内的updatecatalogtree事件,更新栏目内容数量
				});
		};
	diag.show();
}

/**
 * 切换编辑状态：普通状态/链接内容状态
 */
function changeEditorType(linkflag, func){
	if(linkflag=="Y"){
		$('#DivRedirect').show();
		$('#DivContent').hide();
 	} else {
		$('#DivRedirect').hide();
		$('#DivContent').show();
 	}
 	if(func) {
		func();
 	}
}

/**
 * 内容列表页通用删除方法
 */
function batchDel(catalogid) {
	var arr = $NV("SelectedID");
	if(!arr || arr.length == 0) {
		Dialog.alert(Lang.get('Common.PleaseSelectToDeleteRowFirst'));
		return;
	}
	if($G("#sel")){
		if($G("sel").checked){
			$G("sel").checked = false;
		}
	}
	del(arr, catalogid, function(){
		DataList.loadData("contentlist");
		//clearQuickEditor();
	});
}

/**
 * 数据是否被改变过
 */
var lastData;
function isDirty(dc) {
	for (var i in dc){
		var v1=dc[i];
		var v2=lastData[i];
		if(v2 == undefined){
			v2="";
		}
		if(!(v1===v2)){
			return true;
		}
	}
	return false;
}

function del(contentids, catalogid, func) {
	var dc = {IDs:contentids,CatalogID:catalogid};
	var confirmMsg = Lang.get('Common.ConfirmDelete');
	Server.sendRequest("ContentList.checkRecommendBlock",dc,function(response){
		if(response.Status == 1 && response.BlockMessage != null){
			confirmMsg = response.BlockMessage+", "+Lang.get('Contentcore.Del.Confirm2');
		}
		Dialog.confirm(confirmMsg, function() {
			Server.sendRequest("ContentList.del",dc,function(response){
				var taskID = response.TaskID;
				var p = new Progress(taskID,Lang.get('Contentcore.Catalog.Deleting')+"...",500,150);
				p.show(function(){
					$D.close();
					if(response.Status == 1) {
						MsgPop(response.Message);
						AllDocumentsEvent.fire({type:'updatecatalogtree',cid:catalogid}); //触发所有iframe内的updatecatalogtree事件,更新栏目内容数量
						if(func) {
							func(response);
						}
					} else {
						Dialog.warn(response.Message);
					}
				});
				Node.hide(p.Dialog.okButton);
				Node.hide(p.Dialog.cancelButton);
				p.Dialog.cancelButton.onclick = function(){};
			});
		});
	});
}

/**
 * 内容列表通用发布方法
 * 要求内容ID选择通过$NV("SelectedID")获取
 */
function batchPublish(catalogid){
	var ids = $NV("SelectedID");
	if(!ids||ids.length==0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	_publish(ids.join(), catalogid, function(){
			DataList.loadData("contentlist");
			//showInQuickEditor(ids[0]);
		});
}

/**
 * 内容快速编辑页面通用发布方法
 * 要求页面中存在ID名称为ContentID和CatalogID的隐藏域提供ContentID和CatalogID参数
 * 发布成功后更新页面中id为span_publishdate的<span>标签内容，重新加载id为contentlist的DataList控件的数据
 */
function publish() {
	var downlineDate=$V("#DownlineDate");
	var publishDate=$V("#PublishDate");
	if(downlineDate && publishDate && DateTime.parseDate(downlineDate).getTime()<DateTime.parseDate(publishDate).getTime()){
		Dialog.alert(Lang.get('Comment.OfflineTimeLessReleaseTime')); 
		return;
	}
	//添加是否有此方法判断
	try{
		if(typeof(save)==="string"){
			eval(save);
		}
		if(typeof(save)=='function'&&!$("#BtnSave").prop("disabled")){
			save(function(){
				_publish($V("ContentID"), $V("CatalogID"), function(response){
					DataList.loadData("contentlist");
					$("#span_publishdate").html(response.PublishDate);
				});
			});
			return;
		}
		_publish($V("ContentID"), $V("CatalogID"), function(response){
			DataList.loadData("contentlist");
			$("#span_publishdate").html(response.PublishDate);
		});
	}catch(e){};
}

/**
 * 通用发布方法，根据传递的参数发布相关内容
 */
function _publish(contentids, catalogid, func){
	if(isEmpty(contentids)) {
		Dialog.alert("请先选择一条记录");
		return;
	}
	var waitDiag = Dialog.wait(Lang.get('Contentcore.WaitingTip'));
	var dc = {IDs:contentids,CatalogID:catalogid};
	Server.sendRequest("ContentPublish.publish",dc,function(response){
		waitDiag.close();
		if(response.Status == 1) {
			MsgPop(response.Message);
			if(func) {
				func(response)
			}
		} else {
			Dialog.alert(response.Message);
		}
	},null,null,"json");
}

/**
 * 内容列表通用待发布方法
 * 要求内容ID选择通过$NV("SelectedID")获取
 */
function batchToPublish(catalogid){
	var ids = $NV("SelectedID");
	if(!ids||ids.length==0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	_topublish(ids.join(), catalogid, function(){
			DataList.loadData("contentlist");
			//showInQuickEditor(ids[0]);
		});
}

/**
 * 内容待发布
 * 要求页面中存在ID名称为ContentID和CatalogID的隐藏域提供ContentID和CatalogID参数
 */
function topublish() {
	//添加是否有此方法判断
	try{
		if(typeof(save)==="string"){
			eval(save);
		}
		if(typeof(save)=='function'&&!$("#BtnSave").prop("disabled")){
			save(function(){
				_topublish($V("#ContentID"), $V("#CatalogID"), function(response){
					DataList.loadData("contentlist");
				});
			});
			return;
		}
		_topublish($V("#ContentID"), $V("#CatalogID"), function(response){
			DataList.loadData("contentlist");
		});
	}catch(e){};
	
}

/**
 * 通用待发布方法，根据传递的参数发布相关内容
 */
function _topublish(contentids, catalogid, func) {
	if(isEmpty(contentids)) {
		Dialog.alert("请先选择一条内容");
		return;
	}
	var dc = {IDs:contentids,CatalogID:catalogid};
	Server.sendRequest("ContentPublish.toPublish",dc,function(response){
		if(response.Status == 1) {
			MsgPop(response.Message);
			if(func) {
				func();
			}
		} else {
			Dialog.alert(response.Message);
		}
	});
}

/**
 * 内容列表通用方法：下线
 */
function offLine(){
	var ids = $NV("SelectedID");
	if(!ids||ids.length==0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var dc = { IDs:ids.join(), CatalogID:locals['contentScript'].CatalogID };
	var waitDiag = Dialog.wait(Lang.get('Contentcore.WaitingTip'));
	Server.sendRequest("ContentPublish.offLine",dc,function(response){
		waitDiag.close();
		if(response.Status == 1) {
			DataList.loadData("contentlist");
			MsgPop(response.Message);
			//showInQuickEditor(ids[0]);
		} else {
			Dialog.alert(response.Message);
		}
	});
}

/**
 * 内容列表通用方法：置顶
 * 需要当前页面可以获取到$V("ContentID"), $V("CatalogID")
 */
function setTop() {
	_setTop($V("ContentID"), $V("CatalogID"), function(){
		DataList.loadData("contentlist");
		location.hash = location.hash+"&DateTime = "+new Date().getTime();
		$("#BtnSetTop").toggle();
		$("#BtnSetNotTop").toggle();
	});
}

/**
 * 内容置顶
 * @param contentids 内容ID串，逗号分隔
 * @param catalogid 用于内容操作权限判断
 * @param func
 */
function _setTop(contentids, catalogid, func){	
	if(isEmpty(contentids)) {
		Dialog.alert("请先选择一条内容记录");
		return;
	}
	var diag = new Dialog("setTopDialog");
	diag.width = 400;
	diag.height = 100;
	diag.title = Lang.get('Contentcore.SetTop');
	diag.url = CONTEXTPATH+"contentcore/contentTopDialog.zhtml";
	diag.onOk = function(){
		var dc = {IDs:contentids, CatalogID:catalogid, TopDate:$DW.$V("TopDate")};
		Server.sendRequest("ContentList.setTop", dc, function(response){
			MsgPop(response.Message);
			if(response.Status==1){
				setTimeout(function(){diag.close();},10);
				if(func) {
					func(response);
				}
			}
		});
	};
	diag.show();
}

/**
 * 内容列表页通用方法：取消置顶
 */
function setNotTop() {
	_setNotTop($V("ContentID"), $V("CatalogID"), function(){
		DataList.loadData("contentlist");
		location.hash = location.hash+"&DateTime = "+new Date().getTime();
		$("#BtnSetTop").toggle();
		$("#BtnSetNotTop").toggle();
	});
}

/**
 * 内容取消置顶
 * @param contentids 内容ID串，逗号分隔
 * @param catalogid 用于内容操作权限判断
 * @param func
 */
function _setNotTop(contentids, catalogid, func){	
	if(isEmpty(contentids)) {
		Dialog.alert("请先选择一条内容");
		return;
	}
	var dc = {IDs:contentids,CatalogID:catalogid};
	Server.sendRequest("ContentList.setNotTop",dc,function(response){
		if(response.Status==1){
			MsgPop(Lang.get('Common.ExecuteSuccess'));
			if(func) {
				func(response); 
			}
		} else {
			Dialog.warn(Lang.get('Common.ExecuteFailed'));
		}
	});
}

function saveContent(dc, func) {
	Server.sendRequest("Content.save", dc, function(response) {
		if(response.Status == 1) {
			MsgPop(response.Message);
			if(func) {
				func(response);
			}
		} else {
			Dialog.warn(response.Message);
		}
	});
}
/**
 * 内容列表通用方法：排序
 */
function sortOrder(){
	if(currentOrderType!='Default'){
		Dialog.alert(Lang.get('Contentcore.SortTip'));
		return;
	}
	var arr = $NV("SelectedID");
	if(!arr || arr.length == 0) {
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var diag = new Dialog({
		width : 700,
		height : 390,
		title : Lang.get('Contentcore.Sort'),
		url : CONTEXTPATH+"contentcore/contentSortDialog.zhtml?CatalogID="+locals['contentScript'].CatalogID,
		onOk :  function(){
				var dt = $DW.DataGrid.getSelectedData("dg1");
				if(!dt||dt.Rows.length<1){
					Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
					return;
				}
				var dc = { TopFlag:"false", Target:dt.get(0,"OrderFlag") };
				if(dt.get(0, "TopFlag") >0 ){
					dc.TopFlag = "true";
				}
				dc.IDs = arr.join(",");
				dc.Type = "Before";
				dc.CatalogID = locals['contentScript'].CatalogID;
				var waitDialog = Dialog.wait(Lang.get('Contentcore.SortWait'));
				Server.sendRequest("ContentList.sort",dc,function(response){
					waitDialog.close();
					if(response.Status == 1){
						$D.close();
						DataList.loadData("contentlist");
					} else {
						Dialog.warn(response.Message);
					}
				});
			}
	});
	diag.show();
}


/**
 * 内容列表页通用方法：拖动排序
 */
function afterRowSortEnd(evt, ui, ele){
    if(!Application.hasPriv("com.zving.cms.Catalog.Content.Sort." + $V("CatalogID"))) {
		return;
    }
	var oldRowIndex, newRowIndex;
    var $row=ui.item;
    oldRowIndex=$row.data('originalIndex');
    newRowIndex=DataList.getRowIndex(ele,$row);
    var pageIndex=DataList.getParam(ele, Constant.PageIndex);
    //console.log('当前页码:',pageIndex,' 拖拽前行索引:',oldRowIndex,' 拖拽后行索引:',newRowIndex);
    if(newRowIndex==oldRowIndex){
        return;
    }
	var dc = {oldRowIndex:oldRowIndex,newRowIndex:newRowIndex,pageIndex:pageIndex,ID:$row.find('input[name=ID]').val()};

    /* 如果有排序字段，把排序字段的值也传到后台 */
    var orderFlag = $row.find('input[name=OrderFlag]').val();//要求在当前行的隐藏域有保存排序权值
    var targetOrderFlag = DataList("contentlist").$listNodes.eq(newRowIndex > oldRowIndex ? newRowIndex-1 : newRowIndex+1).find('input[name=OrderFlag]').val();//要排到这一行的后面
	var topFlag = DataList("contentlist").$listNodes.eq(newRowIndex > oldRowIndex ? newRowIndex-1 : newRowIndex+1).find('input[name=TopFlag]').val();
    dc.TargetOrderFlag=targetOrderFlag;
    dc.OrderFlag=orderFlag;
    dc.TopFlag = topFlag;
    //console.log('OrderFlag:',dc.OrderFlag,' TargetOrderFlag:',dc.TargetOrderFlag,' Position:',dc.Position,' TopFlag:',dc.TopFlag);

    DataList.showLoading(ele);
    dc.CatalogID= locals['contentScript'].CatalogID;
	Server.sendRequest("ContentList.dragSort",dc,function(response){
			if(response.Status==1){
				MsgPop(response.Message);
                DataList.loadData(ele);
			} else {
				Dialog.warn(response.Message);
			}
	});
}

/**
 * 内容列表通用方法：推荐属性设置
 */
function setRecommended(){
	var ids = $NV("SelectedID");
	if(!ids || ids.length == 0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var dc = {IDs:ids.join(),CatalogID:locals['contentScript'].CatalogID};
	Server.sendRequest("ContentList.setRecommended",dc,function(response){
		if(response.Status == 1){
			MsgPop(response.Message);
			DataList.loadData("contentlist");
		}
	});
}

/**
 * 内容列表通用方法：取消推荐属性
 */
function cancelRecommended(){	
	var ids = $NV("SelectedID");
	if(!ids || ids.length == 0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var dc = {IDs:ids.join(),CatalogID:locals['contentScript'].CatalogID};
	Server.sendRequest("ContentList.CancelRecommended",dc,function(response){
		if(response.Status == 1){
			MsgPop(response.Message);
			DataList.loadData("contentlist");
		}
	});
}

function setHot(){
	var ids = $NV("SelectedID");
	if(!ids || ids.length == 0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var dc = {IDs:ids.join(),CatalogID:locals['contentScript'].CatalogID};
	Server.sendRequest("ContentList.setHot",dc,function(response){
		if(response.Status == 1){
			MsgPop(response.Message);
			DataList.loadData("contentlist");
		}
	});
}


function cancelHot(){	
	var ids = $NV("SelectedID");
	if(!ids || ids.length == 0){
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	var dc = {IDs:ids.join(),CatalogID:locals['contentScript'].CatalogID};
	Server.sendRequest("ContentList.cancelHot",dc,function(response){
		if(response.Status == 1){
			MsgPop(response.Message);
			DataList.loadData("contentlist");
		}
	});
}

/**
 * 通用列表页复制方法
 */
function listCopy(contentType, catalogid, ignore) {
	var arr = $NV("SelectedID");
	if(!arr || arr.length == 0) {
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	if(!ignore) {
		for(var i = 0; i < arr.length; i++) {
			if($V("#LinkFlag_"+arr[i]) == "Y") {
				Dialog.confirm("选中的内容中存在标题内容，标题内容不允许链接或映射，只能独立复制，确认继续操作吗？", function() {
						listCopy(contentType, catalogid, true);
					});
				return;
			}
		}
	}
	_copy(arr.join(), contentType, catalogid, ignore, function(response){
			DataList.loadData("contentlist");
		});
}

/**
 * 通用复制方法
 * @param contentids 需要复制的内容ID
 * @param contentType 需要复制的内容类型
 * @param ignore 是否只支持独立复制
 * @param func 后置方法
 */
function _copy(contentids, contentType, catalogid, ignore, func) {
	var diag = new Dialog("diagCopy");
	diag.width = 430;
	diag.height = 450;
	diag.title = Lang.get('Contentcore.SelectCatalog');
	diag.url = CONTEXTPATH+"contentcore/catalogListForCopy.zhtml?ContentType="+contentType +"&Type=Default,Link&InputType=checkbox&CatalogID="+catalogid;
	diag.onReady = function(){
		if(ignore) {
			$DW.$("#trToolbar").hide();
		}else{
			$DW.$("#trToolbar").show();
		}
	};
	diag.onOk = function() {
		var catalogIDs = $DW.Tree('tree1').getCheckedData('cid');
		if(!catalogIDs || catalogIDs.length == 0) {
			Dialog.alert(Lang.get('Contentcore.selectcatalogsFirst'));
			return;
		}
		var dc = {CopyType:$DW.$NV("CopyType").join(),ContentIDs:contentids,CatalogIDs:catalogIDs.join(),CatalogID:catalogid};
		Server.sendRequest("ContentList.copy", dc, function(response){
			if(response.Status == 1) {
				MsgPop(response.Message);
				$D.close();
				if(func) {
					func(response);
				}
			} else {
				Dialog.alert(response.Message);
			}
		});
	};
	diag.show();
}

/**
 * 通用列表页转移方法
 */
function listMove(contentType, catalogid) {
	var arr = $NV("SelectedID");
	if(!arr || arr.length == 0) {
		Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
		return;
	}
	_move(arr.join(), contentType, catalogid, function(response){
			DataList.loadData("contentlist");
		});
}

/**
 * 通用内容转移方法
 */
function _move(contentids, contentType, catalogid, func) {
	var diag = new Dialog("diagCatalog");
	diag.width = 430;
	diag.height = 450;
	diag.title = Lang.get('Contentcore.SelectCatalog');
	diag.url = CONTEXTPATH+"contentcore/catalogListForCopy.zhtml?ContentType="+contentType+"&Type=Default,Link&InputType=radio&CatalogID="+catalogid;
	diag.onReady = function() {
		$DW.$("#trToolbar").hide();
	}
	diag.onOk = function() {
		var catalogIDs = $DW.Tree('tree1').getCheckedData('cid');
		if(!catalogIDs || catalogIDs.length == 0) {
			Dialog.alert(Lang.get('Contentcore.selectcatalogsFirst'));
			return;
		}
		_moveSave(contentids, catalogIDs.join(), catalogid, func);
	};
	diag.show();
}

/**
 * 通用内容转移保存方法
 */
function _moveSave(contentids, targetCatalogIDs, catalogid, func) {
	var dc = {ContentIDs:contentids,CatalogIDs:targetCatalogIDs,CatalogID:catalogid};
	Server.sendRequest("ContentList.move", dc, function(response){
		if(response.Status == 1) {
			MsgPop(response.Message);
			if($D) {
				$D.close();
			}
			if(func) {
				func(response);
			}
		} else {
			Dialog.alert(response.Message);
		}
	});
}

/**
 * 通用内容列表初始化方法
 */
function onListPageLoad(){
	// 添加列表右键菜单
    var contextMenuList={};
    contextMenuList[Lang.get('Common.Delete')]=true;
	contextMenuList[Lang.get('Contentcore.Publish')]=true;
	contextMenuList[Lang.get('Contentcore.Copy')]=true;
	contextMenuList[Lang.get('Contentcore.Catalog.Move')]=true;
	contextMenuList[Lang.get('Contentcore.Sort')]=true;
	contextMenuList[Lang.get('Contentcore.WaitToPublish')]=true;
	contextMenuList[Lang.get('Contentcore.Offline')]=true;
	contextMenuList[Lang.get('Contentcore.Recommend')]=true;
	contextMenuList[Lang.get('Contentcore.CancelRecommend')]=true;
	contextMenuList[Lang.get('Contentcore.SetHot')]=true;
	contextMenuList[Lang.get('Contentcore.CancelHot')]=true;
	//
    var contextMenuItems=[];
    var toolbarItems = $('#ContentListToolbar').getComponent('Toolbar').items;
    for(var name in contextMenuList){
	    toolbarItems.each(function(item, i){
	        if(item.text.trim()==name){
				var btnOpts=
	            contextMenuItems.push({
	                icon:item.icon,
	                iconCls:item.iconCls,
	                text:item.text,
					disabled: item.disabled,
	                handler:function(btn,evt){
	                   item.$el.click();
	                }
	            })
	        }
	    }); 	
    }
    var cMenu=new DropMenu({
        id:'contextMenu1',
        items:contextMenuItems
    });
    $('#dl1_wrap').on('contextmenu','#dl1 > tbody >tr',function(evt,ui){
        evt.preventDefault();
        //onRowClick(evt.currentTarget);//2014-03-26 wangych认为右击时刷新编辑区很别扭？！
        var framXY=Dom.getFrameXY();
        cMenu.showAt({x:framXY.x+evt.pageX,y:framXY.y+evt.pageY});
		var constrainToXY=Node.getConstrainToXY(cMenu.el,cMenu.ownerDocument);//返回一个约束在页面矩形坐标范围内的x,y值
		if(constrainToXY){
			Node.setPosition(cMenu.el,constrainToXY);
		}
    });
}

/**
 * 映射链接文章需要重置按钮权限
 */
function resetPriv(){
	var copyType = locals['contentScript'].CopyType;
	if(copyType > 1) {
		var $btns = $("a.z-btn", $("#Toolbar"));
		$btns.each(function() {
			var p = this.getAttribute("priv");
			if (p) {
				p = p.substring(0, p.lastIndexOf(".") + 1) + locals['contentScript'].CopyCatalogID; 
				if (Application.hasPriv(p)) {
					$(this).enable();
				} else {
					$(this).disable();
				}
			}
		});
	}
}

function onQuickEditorLoad() {
	if($G("#Method") && $V("#Method") == "ADD" || $G("#ContentID") && isEmpty($V("#ContentID"))) {
		var runTimer=0;
		var disableToolbar=function(){
			if(!isEmpty($V("#ContentID")) && $V("Method") != "ADD") {
				$('#trToolbar a').enable();
				$('#contentform').enable();
			} else {
				$('#trToolbar a').disable();
				$('#contentform').disable();
			}
			runTimer++;
			if(runTimer<5){
				setTimeout(disableToolbar,200);
			}
		}
		disableToolbar();
		return;
	}
	onEidtorLoad();
}

/**
 * 快速编辑页面通用初始化方法
 */
function onEidtorLoad() {
	if(!isEmpty($V("#Title"))) {
		$('#titleLength').html($V("#Title").length);
	}
	if($G('Title')){
		$('#Title').on('keyup blur change paste',function(){
			$('#titleLength').html($V('#Title').length);
		});
		var st = new StyleToolbar('TitleStyle',$G('Title'),'FontColor');
		st.show();
		$S("TitleStyle", locals['contentScript'].TitleStyle);
	}
	//添加摘要字数显示
	if(!isEmpty($V("#Summary"))) {
		$('#summaryLength').html($V("#Summary").length);
	}
	if($G('Summary')){
		$('#Summary').on('keyup blur change paste',function(){
			$('#summaryLength').html($V('#Summary').length);
		});
	}
	if($G('ShortTitle')) {
		st = new StyleToolbar('ShortTitleStyle',$G('ShortTitle'),"FontColor,Bold,Italic,UnderLine");
		st.show();
		$S("ShortTitleStyle", locals['contentScript'].ShortTitleStyle);
		
		$('#ShortTitleStyle_Bold').parents().click(function(){//当标题框内字体变粗时，修改字间距以保证字距和标题框背景上的标尺刻度一致
			if($('#ShortTitle').css('fontWeight') == 'bold')
				$('#ShortTitle').css('letterSpacing', '-1px');
			else
				$('#ShortTitle').css('letterSpacing', '');
			return true;
		});	
	}

	// 初始化权重编辑权限
	if($("#trWeight").length && !Application.hasPriv("com.zving.cms.Catalog.Content.Weight."+locals['contentScript'].CatalogID)) {
		$("#trWeight").remove();
	}
	
	showTemplate();
	showStaticFileName();
	//处理工作流加载
	if(window.loadButtons) {
		if(!$V("#CopyType_"+$V("#ContentID")) || $V("#CopyType_"+$V("#ContentID")) <=1){
			var dc = {ConfigProps:locals['contentScript'].$ConfigProps,Status:locals['contentScript'].Status,CatalogID:locals['contentScript'].CatalogID};
			loadButtons(dc,function(){
				var tempSave = $("#Workflow_CommitAction-1");
				tempSave.disable();
				tempSave.hide();
			});
		}
	}
}

function showTemplate(){
	if($("#spanTemplate").length) {
		if($NV("TemplateFlag")=="Y"){
			Node.show("spanTemplate");
		} else {
			Node.hide("spanTemplate");
			$S('Template',"");
		}
	}
	
	$("[id^=PlatformContentTemplateFlag]").each(function(){
		if(this.checked){
			Node.show("spanTemplate_" + this.value);
		} else {
			Node.hide("spanTemplate_" + this.value);
			$S("platformContentTemplate_" + this.value,"");
		}
	});
}



function showStaticFileName(){
	if($("#spanStaticFileName").length) {
		if($NV("StaticFileNameFlag")=="Y"){
			Node.show("spanStaticFileName");
		} else {
			Node.hide("spanStaticFileName");
			$S('StaticFileName',"");
		}
	}
}

function browseTemplate(id, siteid, type, platformID){
	var value = $V("#"+id);
	var diag = new Dialog(Lang.get('Contentcore.Block.SelectTemplate'),
			CONTEXTPATH+"contentcore/templateSelectDialog.zhtml?SiteID="+siteid+"&Type="+type+"&Value="+value,700,300);
	diag.onOk = function(){
		var t = $DW.getTemplate();
		if(t){
 			$S(id,t);
			$D.close();
		}
	};
	diag.show();
}

function browseTemplateByPlatform(id, siteid, type, platformID){
	var value = $V("#"+id);
	var diag = new Dialog(Lang.get('Contentcore.Block.SelectTemplate'),
			CONTEXTPATH+"contentcore/templateSelectDialog.zhtml?SiteID="+siteid+"&Type="+type+"&Value="+value+"&PlatformID="+platformID,700,300);
	diag.onOk = function(){
		var t = $DW.getTemplate();
		if(t){
 			$S(id,t);
			$D.close();
		}
	};
	diag.show();
}

function selectSource(){
	var diag = new Dialog("sourceDialog");
	diag.width = 400;
	diag.height = 550;
	diag.title = Lang.get('Contentcore.SelectSource');
	diag.url = CONTEXTPATH+"wordmanage/sourceWordExtendDialog.zhtml";
	diag.show();
}

function selectTag(contentType){
	var diag = new Dialog("tagDialog");
	diag.width = 400;
	diag.height = 550;
	diag.title = Lang.get('Wordmanage.SelectTag');
	diag.url = CONTEXTPATH+"wordmanage/tagWordExtendDialog.zhtml?ContentType=" + contentType;
	diag.show();
}
$(function(){
	window.currentOrderType = $V('#SortOrderType');// 默认排序方式
});
function orderList(type){
	currentOrderType = type;
	$S('SortOrderType', type);
	var datalist=DataList("contentlist");
	datalist.sortable = type=='Default';
	datalist.setParam(Constant.PageIndex, 0);
	datalist.setParam("OrderType", type);
	datalist.loadData();
}

function search() {
	DataList.setParam("contentlist", Constant.PageIndex, 0);
	DataList.setParam("contentlist", "Type", $V("Type"));
	DataList.setParam("contentlist", "ContentID", "");
	DataList.setParam("contentlist", "Keyword", $V("Keyword").trim());
	DataList.loadData("contentlist");
	var sel = document.getElementById("sel");
	sel.checked = false;
}

/**
 * 内容快速编辑页面通用方法：相关内容
 */
function relative() {
	_relative($V("ContentID"), $V("CatalogID"), $V("RelativeContent"), function(response){
			showInQuickEditor($V("ContentID"));
		});
}

/**
 * 相关内容处理
 */
function _relative(contentid, catalogid, relaIDs, func) {
	var diag = new Dialog("diagRelative");
	diag.width = 650;
	diag.height = 450;
	diag.title = Lang.get('Contentcore.RelaContent');
	diag.showButtonRow = false;
	diag.url = CONTEXTPATH+"contentcore/contentRelativeDialog.zhtml?ContentID=" + contentid + "&CatalogID=" + catalogid + "&RelativeContent="+relaIDs;
	diag.show();
}

/**
 * 内容快速编辑页面通用方法：查看内容操作记录
 */
function contentLog(contentType, contentid){
	var diag = new Dialog("diagLogs");
	diag.width = 700;
	diag.height = 380;
	diag.title = Lang.get('Contentcore.ContentLog');
	diag.url = CONTEXTPATH+"contentcore/contentLog.zhtml?ContentType="+contentType+"&ContentID=" + contentid;
	diag.show();
}

/**
 * 内容编辑页面通用方法：内容批注
 */
function _note(contentType, contentid, catalogid){
	var diag = new Dialog({
		title:Lang.get('Contentcore.Endorse'),
		url:CONTEXTPATH + "contentcore/contentNote.zhtml?ContentType="+contentType+"&ContentID="+contentid+"&CatalogID="+catalogid,
		width:680,
		height:380
	});
	diag.show();
}

/**
 * 通用链接内容选择方法，处理链接内容时使用
 */
function selectContent(){
	var diag = new Dialog(Lang.get('Contentcore.Block.BrowseArticle'),
			CONTEXTPATH + "contentcore/contentSelector.zhtml?SelectType=radio&From=EditorWorkspace&LinkFlag=N",800,500);
	diag.onOk = function(){
			var arr = $DW.$N("SelectedID");
			for (var i = 0; arr && i < arr.length; i++) {
				if (arr[i].checked) {
					if(arr[i].getAttribute("value") == $V('#ContentID')){
						Dialog.alert(Lang.get('Contentcore.CannotLinkToSelf'));
						return;
					}
					if(arr[i].getAttribute("value") == $V('#CopyID')){
						Dialog.alert(Lang.get('Contentcore.CannotLinkToCopyContent'));
						return;
					}
 					var url = arr[i].getAttribute("link");
					$S("RedirectURL",url);
					var title = arr[i].getAttribute("title");
					var type = arr[i].getAttribute("contenttype");
					$S("CName",type+"："+title);
					break;
				}
			}
			diag.close();
	}
	diag.show();
}

/**
 * 通用链接栏目选择方法，处理链接内容时使用
 */
function selectCatalog(){
	var diag = new Dialog(Lang.get('Contentcore.SelectCatalog'),
			CONTEXTPATH+"contentcore/catalogSelector.zhtml?InputType=radio",400,500);
	diag.onOk = function(){
			var arr = $DW.Tree('tree1').getCheckedData();
			if(!arr||arr.length==0){
				Dialog.alert(Lang.get('Common.PleaseSelectRowFirst'));
				return;
			}
			for (var i = 0; arr && i < arr.length; i++) {
 					var url = arr[i].el.getAttribute("link");
					$S("RedirectURL",url);
					var name = arr[i].el.getAttribute("catalogName");
					$S("CName","栏目："+name);
					break;
			}
			diag.close();
	}
	diag.show();
}

/**
 * 通用链接地址选择方法，处理链接内容时使用
 */
function inputUrl(cName){
	var params="";
	var hiddenLink=$V("#RedirectURL");
	var name=$V("CName");
	if(cName&&(hiddenLink==name)){
		params="?outLink="+$V("#"+cName);
	}
	var diag = new Dialog(Lang.get('Contentcore.ExternalURL'),
			CONTEXTPATH+"contentcore/inputExternalLink.zhtml"+params,400,100);
	diag.onOk = function(){
		if($DW.Verify.hasError()){
			return;
		}
		var url = $DW.$V("#LinkURL");
		$S("RedirectURL",url);
		$S("CName",url);
		diag.close();
	}
	diag.show();
}

/**
 * 判断是否手动修改过发布平台属性
 * @param ele
 */
function hasChangePlatFormAttribute(ele){
	if(ele&&ele.val()=='false'){
		ele.val('true');
	}
	
}
/* updatecontentlist: 在其他页面发出  updatecontentlist 事件时，重载文章列表 */
function reloadDataList(evt){
	var selectedId=$NV("SelectedID");
	DataList.loadData('contentlist',function(){
		/*
		if(selectedId && selectedId[0]){
			var $checkbox=$('[name=SelectedID][value='+selectedId[0]+']');
			if($checkbox.length){
				$checkbox.closest('tr.datarow').click();
			}
		}
		*/
	});
}

AllDocumentsEvent.on('updatecontentlist', reloadDataList);
$(window).on('unload',function(){AllDocumentsEvent.un('updatecontentlist');});
/* end updatecontentlist */