

function repaintForHeightAuto(pageJson){
var components={};
for(var key in pageJson.components){
	if(pageJson.components.hasOwnProperty(key)){
		components[key]={};
		components[key].width=pageJson.components[key].style.width;
		components[key].height=pageJson.components[key].style.height;
		components[key].top=pageJson.components[key].style.top;
		if(pageJson.components[key].componentType==='FullColumn'){
			components[key].top+=pageJson.options.marginTop+pageJson.options.paddingTop;
		}
		components[key].left=pageJson.components[key].style.left;
		components[key].heightAuto=pageJson.components[key].heightAuto||false;
	}
}
console.log(JSON.stringify(components,null,4));
var componentsTree=buildComponentsTree(pageJson);
console.log(componentsTree);
console.log(JSON.stringify(componentsTree,null,4));

/*树节点关系如下
{
    "cTempID1": [
        "content6",
        "container4"
    ],
    "cTempID2": [
        "container1"
    ],
    "cTempID3": [
        "container10"
    ],
    "cTempID4": [
        "container11"
    ],
    "cTempID5": [
        "cTempID1",
        "cTempID2"
    ],
    "cTempID6": [
        "cTempID3",
        "cTempID4"
    ],
    "cTempID7": [
        "cTempID5",
        "cTempID6"
    ]
}
*/
	//根据components提供的组件位置尺寸信息，完善组件树中各节点的属性
	//有下列属性
/*
//编辑时保存的区域信息（从components变量中提取）
	//区域左上和右下点的坐标
originalTop:
originalLeft:
originalHeight:
originalWidth:

//repaint处理过程中由于一些组件的自适应高度，用于记录当前组件的实际位置和尺寸信息

currentTop:
currentLeft:
currentHeight:
currentWidth:
*/
//用组件位置信息初始化组件树
var componentsTreeWithData={};
componentsTreeWithData.nodes={};

for(var key in componentsTree.nodes){
	if(!componentsTree.nodes.hasOwnProperty(key))continue;
	componentsTreeWithData.nodes[key]={
		childNodes:componentsTree.nodes[key],//子节点
		parentNode:componentsTree.nodes[key].parentNode,//父节点		
		isConstructedByStack:!!componentsTree.nodes[key].type,//是否是层叠构成的节点
		isLeafNode:componentsTree.nodes[key].isLeafNode, //是否为叶子节点
		id:key
	};
}

componentsTreeWithData.rootNodeID=componentsTree.rootNodeID;//树根节点
//console.log(JSON.stringify(componentsTreeWithData,null,4));

//初始化树节点对应的区域的位置和尺寸信息
nodeInit(componentsTreeWithData.rootNodeID);	
//console.log('after init:'+JSON.stringify(componentsTreeWithData,null,4));
//找到所有自适应的组件
var heightAutoComponents=[];
for(var id in components){
	if(!components.hasOwnProperty(id))continue;
	
	if(components[id].heightAuto){
		heightAutoComponents.push(id);
	}
}
//console.log('heightAutoComponents:'+heightAutoComponents);
//逐个处理自适应组件
for(var i=0;i<heightAutoComponents.length;i++){
	fitHeight(heightAutoComponents[i]);
}

//console.log('after fit:'+JSON.stringify(componentsTreeWithData,null,4));
//刷新页面中的组件
reflesh(componentsTreeWithData.rootNodeID);
//更新页面高度
document.getElementById('pageWrapper').style.height=componentsTreeWithData.nodes[componentsTreeWithData.rootNodeID].currentHeight+'px';

//刷新id为nodeID的节点在页面的显示（递归的实现），和nodeInit方法对应
function reflesh(id){
	var node=componentsTreeWithData.nodes[id];
	if(!node.isLeafNode){
		//如果非叶子节点，刷新所有子节点
		var childNodes=componentsTreeWithData.nodes[id].childNodes;	
		
		for(var i=0;i<childNodes.length;i++){
			reflesh(childNodes[i]);
		}
	}else{
	/*
		if(node.isConstructedByStack){
			var elIDs=node.childNodes;
			var el;
			for(var i=0;i<elIDs.length;i++){
				el=document.getElementById(elIDs[i]);
				el.style.top=(node.currentTop+node['child_'+elIDs[i]].topDifference)+'px';
				el.style.left=(node.currentLeft+node['child_'+elIDs[i]].leftDifference)+'px';
				//被包装的组件的高度为原来的高度加节点整体的高度变化量
				el.style.height=node['child_'+elIDs[i]].originalHeight+(node.currentHeight-node.originalHeight)+'px';
			}
		}else{
			var componentID=node.childNodes[0];
			var el=document.getElementById(componentID);
			el.style.top=node.currentTop+'px';
			el.style.left=node.currentLeft+'px';
			el.style.height=node.currentHeight+'px';
		}
		*/
		var elIDs=node.childNodes;
		var el;
		for(var i=0;i<elIDs.length;i++){
			var heightIncrement=node.currentHeight-node.originalHeight;
			
			el=document.getElementById(elIDs[i]);
			el.style.top=node.currentTop+'px';
			el.style.left=node.currentLeft+'px';
			
			if(heightIncrement<-1e-8 || heightIncrement>1e-8){
				el.style.height=node.currentHeight+'px';
				
				var contentWrapper=document.getElementById(elIDs[i]+'_contentWrapper');
				contentWrapper.style.height=parseInt(contentWrapper.style.height,10)+heightIncrement+'px';
			}
			
		}
	}
}

//以下为被调用的函数
function nodeInit(id){
	var node=componentsTreeWithData.nodes[id];
	if(node.isLeafNode){
		//叶子节点对应页面中的一个基础组件
		var rect=getRectOfComponent(node.childNodes[0]);
		node.originalTop=rect.top;
		node.originalLeft=rect.left;
		node.originalHeight=rect.height;
		node.originalWidth=rect.width;
	}else{
		//如果不是叶子节点，则先初始化子节点数据，然后通过子节点数据计算当前节点的位置和尺寸信息		
		var childNodes=getChildNodes(node);
				
		//初始化子节点数据
		for(var i=0;i<childNodes.length;i++){
			nodeInit(childNodes[i].id);
		}
		//计算当前节点位置和尺寸信息
		//父节点的left、top为所有子节点left、top的最小值，right、bottom为所有组件的right、bottom的最大值
		node.originalTop=childNodes[0].currentTop;
		node.originalLeft=childNodes[0].currentLeft;
		node.originalHeight=childNodes[0].currentHeight;
		node.originalWidth=childNodes[0].currentWidth;
		var oldTop,oldLeft;
		for(var i=1;i<childNodes.length;i++){
			//使node的区域刚好覆盖所有子节点的区域
			oldTop=node.originalTop;
			oldLeft=node.originalLeft;
			node.originalTop>childNodes[i].currentTop?node.originalTop=childNodes[i].currentTop:'';
			node.originalLeft>childNodes[i].currentLeft?node.originalLeft=childNodes[i].currentLeft:'';
			
			node.originalHeight=Math.max(oldTop+node.originalHeight,childNodes[i].currentTop+childNodes[i].currentHeight)-node.originalTop;
			node.originalWidth=Math.max(oldLeft+node.originalWidth,childNodes[i].currentLeft+childNodes[i].currentWidth)-node.originalLeft;
		}
		

	/*}else{
		
		//如果是叶子节点，根据节点类型（是层叠而成的还是独立的）分类处理
		var rect;//当前节点对应的矩形区域
		if(node.isConstructedByStack){
			//层叠成的组件由最多一个内容组件和n个边框组件构成；
			//统计最外层的那个组件
			rect=getRectOfComponent(node.childNodes[0]);
			var top=rect.top;
			var childNodes=node.childNodes;
			var indexOfOuter=0;//记录最外层的组件的索引
			for(var i=1;i<childNodes.length;i++){
				rect=getRectOfComponent(childNodes[i]);
				if(top>rect.top){
					top=rect.top;
					indexOfOuter=i;
				}
			}
			node.outerComponent=childNodes[indexOfOuter];
			//计算层叠组件相对于最外层的位置（组件左上顶点直接的相对位置）
			var rectOfOuterComponent,rectOfChildNode;
			rectOfOuterComponent=getRectOfComponent(node.outerComponent);
			
			for(var i=0;i<childNodes.length;i++){			
				var childComponentID='child_'+childNodes[i];
				rectOfChildNode=getRectOfComponent(childNodes[i]);
				node[childComponentID]={};
				node[childComponentID].topDifference=rectOfChildNode.top-rectOfOuterComponent.top;
				node[childComponentID].leftDifference=rectOfChildNode.left-rectOfOuterComponent.left;
				node[childComponentID].originalHeight=rectOfChildNode.height;
			}
			//组件的区域为最外层的那个组件区域
			//console.log('indexOfOuter:'+indexOfOuter);
			rect=rectOfOuterComponent;
		}else{
			rect=getRectOfComponent(node.childNodes[0]);			
		}
		
		//叶子节点对应页面中的一个基础组件
		//var rect=getRectOfComponent(node);
		node.originalTop=rect.top;
		node.originalLeft=rect.left;
		node.originalHeight=rect.height;
		node.originalWidth=rect.width;
		*/
	}
	//初始状态，current=original
	node.currentTop=node.originalTop;
	node.currentLeft=node.originalLeft;
	node.currentHeight=node.originalHeight;
	node.currentWidth=node.originalWidth;
}

function getRectOfComponent(id){
	//console.log(id);
	var rect={};
	rect.top=components[id].top;
	rect.left=components[id].left;
	rect.height=components[id].height;
	rect.width=components[id].width;
	return rect;
}

//根据专题页面中自适应高度组件的id，查找在组件树中对应的节点
function getTreeNode(componentID){
	for(var id in componentsTreeWithData.nodes){
		if(!componentsTreeWithData.nodes.hasOwnProperty(id))continue;
		
		if(componentsTreeWithData.nodes[id].childNodes.indexOf(componentID)!=-1){
			return componentsTreeWithData.nodes[id];
		}
	}
}


//使参数指定的组件高度自适应内容
function fitHeight(componentID){
	var node=getTreeNode(componentID);
	
	//console.log('height:'+$('#'+componentID).height());
	//var heightIncrement=$('#'+componentID).height()-getRectOfComponent(componentID).height;//$h是组件在页面中对应div的height设为auto后的实际高度
	var heightIncrement=document.getElementById(componentID+'_content4height').offsetHeight-document.getElementById(componentID+'_contentWrapper').offsetHeight;
	//console.log('heightIncrement:'+heightIncrement);
	changeHeightBy(node,heightIncrement);
}

//按指定的改变量改变组件的高度，同时更新组件中相关节点的位置使相对位置不变
function changeHeightBy(node,heightIncrement){
	//步骤：
	//改变同级组件的位置；
	//修改组件本身的高度；
	//计算父节点的高度改变量，递归地处理父节点
	var parentNode;
	while(true){
		if(!node||(heightIncrement<1e-8 && heightIncrement>-1e-8)) return;
		
		parentNode=getParentNode(node);
		if(parentNode&&parentNode.isConstructedByStack){
			//组件高度变化的可能原因只有两个：自身自适应高度导致的高度变化（此时其父节点被视为层叠出的节点），后代节点高度变化导致的高度变化。
			changeSiblingsHeight(node,heightIncrement);
			node.currentHeight+=heightIncrement;
			heightIncrement=heightIncrement;
		}else{
			moveSiblingsPositon(node,heightIncrement);
			//console.log('node:'+JSON.stringify(node));
			node.currentHeight+=heightIncrement;
			//console.log('after change:'+JSON.stringify(componentsTreeWithData.nodes,null,4))
			heightIncrement=getParentNodeHeightIncrement(node);
		}
		//console.log(heightIncrement);
		node=parentNode;		
	}
}
//按给定增量统一改变同级节点的高度
function changeSiblingsHeight(node,heightIncrement){
	var siblingNodes=getSiblingNodes(node);
	if(!siblingNodes||!siblingNodes.length)return;
	
	for(var i=0;i<siblingNodes.length;i++){
		changeHeightBySimply(siblingNodes[i],heightIncrement);
	}
}
//简单地改变节点的尺寸，紧紧所有后代节点都以相同的增量改变高度
function changeHeightBySimply(node,heightIncrement){
	if(node.isLeafNode){
		node.currentHeight+=heightIncrement;
	}else{
		var childNodes=getChildNodes(node);
		for(var i=0;i<childNodes.length;i++){
			childNodes[i].currentHeight+=heightIncrement;
		}
	}
}
//获得节点node的父节点，返回node类型
function getParentNode(node){
	var parentNodeID=node.parentNode;
	return parentNodeID?componentsTreeWithData.nodes[parentNodeID]:undefined;
}
//获得对应id的树节点
function getNode(nodeID){
	if(!nodeID)return undefined;
	return componentsTreeWithData.nodes[nodeID];
}
//移动完所有同级节点后(参数节点的位置和尺寸也已经调整)，计算父节点的高度变化
function getParentNodeHeightIncrement(node){
	var parentNode=getParentNode(node);
	if(!parentNode)return;
	var siblingIDs=parentNode.childNodes;//同胞节点nodeID,尺寸包括node本身
	//计算同级节点都调整位置后尺寸后，父节点的实际底边位置
	var bottom=0;//刚开始时父节点中没有子节点，逐渐添加节点
	var siblingNode;
	for(var i=0;i<siblingIDs.length;i++){
		siblingNode=getNode(siblingIDs[i]);
		//console.log('11siblingNode:'+JSON.stringify(siblingNode,null,4));
		if(bottom<siblingNode.currentTop+siblingNode.currentHeight){
			//console.log('siblingNode.currentTop+siblingNode.currentHeight'+siblingNode.currentTop+','+siblingNode.currentHeight);
			bottom=siblingNode.currentTop+siblingNode.currentHeight;	
			//console.log('bottom:'+bottom);
			
		}
	}
	//console.log('bottom:'+bottom);
	//整个容器的左上顶点的位置不变，变动的只有高度
	return bottom-parentNode.currentTop-parentNode.currentHeight;
}
//获得所以子节点
function getChildNodes(node){
	var childNodeIDs=node.childNodes,
		childNodes=[];
	for(var i=0;i<childNodeIDs.length;i++){
		childNodes.push(componentsTreeWithData.nodes[childNodeIDs[i]]);
	}
	return childNodes;
}
function getSiblingNodes(node){
	var parentNode=getParentNode(node);
	if(!parentNode)return [];//如果当前节点为根节点，直接返回（因为没有同胞节点），递归处理结束
	
	var siblingIDs=parentNode.childNodes; //包括node自己的id
	//console.log('siblingIDs:'+siblingIDs);
	var index=siblingIDs.indexOf(node.id);
	var siblingNodes=[];
	for(var i=0;i<siblingIDs.length;i++){
		if(i!=index){
			siblingNodes.push(componentsTreeWithData.nodes[siblingIDs[i]]);
		}		
	}
	return siblingNodes;
}
//处理所有同级节点的移动
function moveSiblingsPositon(node,heightIncrement){
	var siblingNodes=getSiblingNodes(node);
	if(!siblingNodes||!siblingNodes.length)return;
	
	siblingNodes.sort(function(nodeA,nodeB){
		return nodeB.currentTop-nodeA.currentTop||nodeB.currentLeft-nodeA.currentLeft ;
	});
	
	moveSomeSiblingsPositon(node,heightIncrement,siblingNodes);
}

//移动同级节点中受影响的节点，以保持相对位置关系不变（不同列的节点不受影响）
function moveSomeSiblingsPositon(node,heightIncrement,siblingsUndoneSorted){
	if(!siblingsUndoneSorted.length)return;

	var siblingsHandOn=getSiblingsOnHand(node,siblingsUndoneSorted);//接下来要处理的同级节点（哪些在node下方，紧挨则node，且彼此没有x域上的焦点的节点集合）
	console.log('siblingsHandOn:'+JSON.stringify(siblingsHandOn,null,4));
	for(var i=0;i<siblingsHandOn.length;i++){
		moveSomeSiblingsPositon(siblingsHandOn[i],heightIncrement,siblingsUndoneSorted);//siblingsUndoneSorted在moveSomeSiblingsPositon中会不断更新
		//节点整体移动
		changeTopBy(siblingsHandOn[i],heightIncrement);
	}
}

//改变节点对应区域在页面的位置（也就是改变top值），heightIncrement为变化量（可为负值）；
//节点内部的节点都做相应的修改；
//node为节点类型
function changeTopBy(node,heightIncrement){
	
	if(!node.isLeafNode){
		//调整节点本身的位置
		node.currentTop+=heightIncrement;
		//调整内部节点的位置
		var childNodeIDs=node.childNodes;
		for(var i=0;i<childNodeIDs.length;i++){
			changeTopBy(getNode(childNodeIDs[i]),heightIncrement);
		}
	}else{
		//调整节点本身的位置
		node.currentTop+=heightIncrement;
	}
	
	

}


//获得要处理的和node在空间上存在直接相邻关系的同胞节点
//参数说明：node,siblingsUndone都为树节点，且siblingsUndone已按下列注释的排序方式(根据top left值增序排序)排序好
	/*
	siblingsUndone.sort(function(nodeA,nodeB){
		return nodeB.currentTop-nodeA.currentTop||nodeB.currentLeft-nodeA.currentLeft ;
	});
	*/
function getSiblingsOnHand(node,siblingsUndoneSorted){

	//1.判断是否处于node的下方且和node在水平域上有交集，如果没有，不考虑
	//2.如果1返回true，进一步判断该节点是否和siblingsOnHand中某个节点有1所描述的关系
	var siblingsOnHand=[];
	for(var i=0;i<siblingsUndoneSorted.length;i++){
		if(underAndHasMixInX(node,siblingsUndoneSorted[i])){
			for(var j=0;j<siblingsOnHand.length;j++){
				if(underAndHasMixInX(siblingsOnHand[j],siblingsUndoneSorted[i])){
					break;
				}
			}
			if(j==siblingsOnHand.length){
				siblingsOnHand.push(siblingsUndoneSorted[i]);
			}
		}
	}
	//更新siblingsUndone，使不包含siblingsOnHand中的节点
	var index;
	for(var i=0;i<siblingsOnHand.length;i++){
		index=siblingsUndoneSorted.indexOf(siblingsOnHand[i]);
		if(index!=-1){
			siblingsUndoneSorted.splice(index,1);
		}
	}
	
	return siblingsOnHand;
}

//判断是否处于node的下方且和node在水平域上有交集
//参数说明：
//nodeRef:被参照节点
//nodeTarget:目标节点
function underAndHasMixInX(nodeRef,nodeTarget){	
	return nodeRef.currentTop<=nodeTarget.currentTop && 
	((nodeRef.currentLeft+nodeRef.currentWidth-1>=nodeTarget.currentLeft && nodeRef.currentLeft+nodeRef.currentWidth-1<=nodeTarget.currentLeft+nodeTarget.currentWidth-1)||
	(nodeRef.currentLeft>=nodeTarget.currentLeft && nodeRef.currentLeft<=nodeTarget.currentLeft+nodeTarget.currentWidth-1)||
	(nodeTarget.currentLeft+nodeTarget.currentWidth-1>=nodeRef.currentLeft && nodeTarget.currentLeft+nodeTarget.currentWidth-1<=nodeRef.currentLeft+nodeTarget.currentWidth-1)||
	(nodeTarget.currentLeft>=nodeRef.currentLeft && nodeTarget.currentLeft<=nodeRef.currentLeft+nodeRef.currentWidth-1));
}

//根据布局字符串构造节点树
function parseComponentsTree(structureDis){
	//输入合法性检查
	
	//以下是对合法输入的处理
	var componentsTree={};
	componentsTree.nodes={};
	var i=0;
	do{
		structureDis=structureDis.replace(/<b>([^<]+)<\/b>/gi,function($0,$1){
		
			var str=$1.replace(/^\s+|,+\s*$/g,'');//去掉首尾的空字符，并去掉最后面多余的逗号
			var componentId='cTempID'+(++i);
			componentsTree.nodes[componentId]=str.replace(/(?:^\s*\{?\s*)|(?:\s*\}?\s*$)/g,'').split(/\s*,\s*/g);
			if(/^\s*\{.+\}\s*$/.test(str)){
				componentsTree.nodes[componentId].type=1;//如果组件id包括在一对大括号之间，说明是由括号中id对应的组件层叠而成的组件
			}else{
				componentsTree.nodes[componentId].type=0; //独立的组件
			}
					
			return componentId+','; //用逗号间隔同级别的组件id
		});
	}while(/^<b>.+<\/b>$/i.test(structureDis));
	
	//建立树节点之间的父子关系
	for(var id in componentsTree.nodes){
		if(!componentsTree.nodes.hasOwnProperty(id)) continue;
		
		for(var otherId in componentsTree.nodes){
			if(otherId!==id && componentsTree.nodes[otherId].indexOf(id)!=-1){
				componentsTree.nodes[id].parentNode=otherId;
				break;
			}
		}
	}
	
	//断言：没有parentNode的节点为根节点
	//查找树的根节点(id)
	for(var id in componentsTree.nodes){
		if(!componentsTree.nodes.hasOwnProperty(id)) continue;
		
		if(!componentsTree.nodes[id].parentNode){
			componentsTree.rootNodeID=id;
			break;
		}		
	}
	//标记叶子节点
	for(var id in componentsTree.nodes){
		if(!componentsTree.nodes.hasOwnProperty(id)) continue;
		
		componentsTree.nodes[id].isLeafNode=true;
		var childNodes=componentsTree.nodes[id];
		for(var otherId in componentsTree.nodes){
			if(otherId!==id && childNodes.indexOf(otherId)!=-1){
				componentsTree.nodes[id].isLeafNode=false;
				break;
			}
		}
	}	
	
	
	return componentsTree;
}

//根据pageJson构造组件节点树
function buildComponentsTree(pageJson){
	var componentsTree={};
	componentsTree.nodes={};
	
	//构建树节点
	var components=pageJson.components,
		groups=pageJson.groups;

	var n=0;
	for(var key in components){		
			componentsTree.nodes['_'+key]=[key];
	}
	var blocks,nodeID;
	for(var key in groups){
		if(groups.hasOwnProperty(key)){
			blocks=groups[key].blocks;
			nodeID='_'+key;
			componentsTree.nodes[nodeID]=[];
			componentsTree.nodes[nodeID].type=0;
			for(var i=0;i<blocks.length;i++){
				
				//为了处理边框内部的内容组件的自适应高度，需要确定节点的类型标识组件是否为多个层叠组件构成的组件），为简单起见，认为每个自适应高度组件都和它
				//同级的组件为层叠关系，设置其父节点类型为层叠类型。因此，在编辑过程中需要把自适应高度的组件和希望与其保持层叠关系的组件放在一个组中，
				//如果没有要与之保持层叠关系的组件，该组件也要独立编为一组，以免影响其他组件。				
				componentsTree.nodes[nodeID].push('_'+blocks[i].id);
				
				if(blocks[i].type!=='Group'&&components[blocks[i].id].heightAuto){
					componentsTree.nodes[nodeID].type=1;					
				}
				
			}
			
		}
	}
	
	
	//建立树节点之间的父子关系
	for(var id in componentsTree.nodes){
		if(!componentsTree.nodes.hasOwnProperty(id)) continue;
		
		for(var otherId in componentsTree.nodes){
			if(otherId!==id && componentsTree.nodes[otherId].indexOf(id)!=-1){
				componentsTree.nodes[id].parentNode=otherId;
				break;
			}
		}
	}
	
	//添加根节点
	var level1Node=[];
	
	//没有parentNode的节点为一级节点
	for(var id in componentsTree.nodes){
		if(!componentsTree.nodes.hasOwnProperty(id)) continue;
		
		if(!componentsTree.nodes[id].parentNode){
			level1Node.push(id);
			componentsTree.nodes[id].parentNode='root';
		}		
	}
	
	componentsTree.nodes['root']=level1Node;
	componentsTree.rootNodeID='root';
	//标记叶子节点
	for(var id in componentsTree.nodes){
		if(!componentsTree.nodes.hasOwnProperty(id)) continue;
		
		componentsTree.nodes[id].isLeafNode=true;
		var childNodes=componentsTree.nodes[id];
		for(var otherId in componentsTree.nodes){
			if(otherId!==id && childNodes.indexOf(otherId)!=-1){
				componentsTree.nodes[id].isLeafNode=false;
				break;
			}
		}
	}	
	
	
	return componentsTree;
}
}



/*参数格式说明：
/*存储页面信息的json对象
{
    "options": {
        "pageCssText": "width: 960px; margin-top: 20px; margin-right: auto; margin-bottom: 0px; margin-left: auto; border-top: 0px solid #000000; border-bottom: 0px solid #000000; border-left: 0px solid #000000; border-right: 0px solid #000000; height: auto; min-height: 100%; line-height: normal; font-size: 12px; font-family: tahoma; color: #000; text-align: left; font-weight: normal; font-style: normal; padding-top: 10px; padding-right: 10px; padding-bottom: 10px; padding-left: 10px; ",
        "bodyCssText": "background-image: none; background-repeat: repeat; background-position: left top; background-color: transparent; ",
        "width": 960
    },
    "identity": 29,
    "zIndexCounter": 115,
    "components": {
        "nav13": {
            "id": "nav13",
            "componentType": "Nav",
            "style": {
                "width": 960,
                "height": 45,
                "left": 0,
                "top": 0,
                "paddingTop": 0,
                "paddingRight": 0,
                "paddingBottom": 0,
                "paddingLeft": 0,
                "borderTopStyle": "solid",
                "borderRightStyle": "solid",
                "borderBottomStyle": "solid",
                "borderLeftStyle": "solid",
                "borderTopWidth": 0,
                "borderRightWidth": 0,
                "borderBottomWidth": 0,
                "borderLeftWidth": 0,
                "borderTopColor": "transparent",
                "borderRightColor": "transparent",
                "borderBottomColor": "transparent",
                "borderLeftColor": "transparent",
                "zIndex": 112,
                "opacity": 100,
                "backgroundColor": "#fff",
                "backgroundImage": "none",
                "backgroundPositionX": "0",
                "backgroundPositionY": "0",
                "backgroundAttachment": "scroll",
                "backgroundRepeat": "repeat"
            },
            "nav": {
                "items": [
                    {
                        "index": 0,
                        "name": "1111",
                        "href": "",
                        "target": "_blank",
                        "parent": -1,
                        "order": 1
                    },
                    {
                        "index": 1,
                        "name": "222222",
                        "href": "",
                        "target": "_blank",
                        "parent": -1,
                        "order": 2
                    },
                    {
                        "index": 2,
                        "name": "33",
                        "href": "",
                        "target": "_blank",
                        "parent": -1,
                        "order": 3
                    },
                    {
                        "index": 3,
                        "name": "44-11111",
                        "href": "",
                        "target": "_blank",
                        "parent": -1,
                        "order": 4
                    },
                    {
                        "index": 4,
                        "name": "11-111",
                        "href": "",
                        "target": "_blank",
                        "parent": 0,
                        "order": 1
                    },
                    {
                        "index": 5,
                        "name": "11-2222222",
                        "href": "",
                        "target": "_blank",
                        "parent": 0,
                        "order": 2
                    },
                    {
                        "index": 6,
                        "name": "11-3333",
                        "href": "",
                        "target": "_blank",
                        "parent": 0,
                        "order": 3
                    },
                    {
                        "index": 7,
                        "name": "22-11",
                        "href": "",
                        "target": "_blank",
                        "parent": 1,
                        "order": 1
                    },
                    {
                        "index": 8,
                        "name": "22-2222222222",
                        "href": "",
                        "target": "_blank",
                        "parent": 1,
                        "order": 2
                    },
                    {
                        "index": 9,
                        "name": "22-333333333",
                        "href": "",
                        "target": "_blank",
                        "parent": 1,
                        "order": 3
                    },
                    {
                        "index": 10,
                        "name": "22-4444444",
                        "href": "",
                        "target": "_blank",
                        "parent": 1,
                        "order": 4
                    },
                    {
                        "index": 11,
                        "name": "33-111111",
                        "href": "",
                        "target": "_blank",
                        "parent": 2,
                        "order": 1
                    },
                    {
                        "index": 12,
                        "name": "33-222222222",
                        "href": "",
                        "target": "_blank",
                        "parent": 2,
                        "order": 2
                    },
                    {
                        "index": 13,
                        "name": "33-3333333333",
                        "href": "",
                        "target": "_blank",
                        "parent": 2,
                        "order": 3
                    },
                    {
                        "index": 14,
                        "name": "44-1111111111",
                        "href": "",
                        "target": "_blank",
                        "parent": 3,
                        "order": 1
                    },
                    {
                        "index": 15,
                        "name": "44-222222222",
                        "href": "",
                        "target": "_blank",
                        "parent": 3,
                        "order": 2
                    },
                    {
                        "index": 16,
                        "name": "44-3333333333",
                        "href": "",
                        "target": "_blank",
                        "parent": 3,
                        "order": 3
                    }
                ],
                "options": {
                    "id": "nav13",
                    "type": "hs3",
                    "bgColor": "bg_red",
                    "direction": "down",
                    "itemWidthChangable": false,
                    "topListItemWidth": 100,
                    "width": 960
                }
            }
        },
        "border14": {
            "id": "border14",
            "componentType": "Border",
            "style": {
                "width": 421,
                "height": 245,
                "left": 0,
                "top": 44,
                "paddingTop": 0,
                "paddingRight": 0,
                "paddingBottom": 0,
                "paddingLeft": 0,
                "borderTopStyle": "solid",
                "borderRightStyle": "solid",
                "borderBottomStyle": "solid",
                "borderLeftStyle": "solid",
                "borderTopWidth": 0,
                "borderRightWidth": 0,
                "borderBottomWidth": 0,
                "borderLeftWidth": 0,
                "borderTopColor": "transparent",
                "borderRightColor": "transparent",
                "borderBottomColor": "transparent",
                "borderLeftColor": "transparent",
                "zIndex": 107,
                "opacity": 100,
                "backgroundColor": "#fff",
                "backgroundImage": "none",
                "backgroundPositionX": "0",
                "backgroundPositionY": "0",
                "backgroundAttachment": "scroll",
                "backgroundRepeat": "repeat"
            },
            "showMore": false,
            "moreUrl": "",
            "moduleUrl": "containers/blue_triangle_title_bg__radius_border/index.html",
            "title": "1111111"
        },
        "border15": {
            "id": "border15",
            "componentType": "Border",
            "style": {
                "width": 536,
                "height": 474,
                "left": 424,
                "top": 41,
                "paddingTop": 0,
                "paddingRight": 0,
                "paddingBottom": 0,
                "paddingLeft": 0,
                "borderTopStyle": "solid",
                "borderRightStyle": "solid",
                "borderBottomStyle": "solid",
                "borderLeftStyle": "solid",
                "borderTopWidth": 0,
                "borderRightWidth": 0,
                "borderBottomWidth": 0,
                "borderLeftWidth": 0,
                "borderTopColor": "transparent",
                "borderRightColor": "transparent",
                "borderBottomColor": "transparent",
                "borderLeftColor": "transparent",
                "zIndex": 108,
                "opacity": 100,
                "backgroundColor": "#fff",
                "backgroundImage": "none",
                "backgroundPositionX": "0",
                "backgroundPositionY": "0",
                "backgroundAttachment": "scroll",
                "backgroundRepeat": "repeat"
            },
            "showMore": true,
            "moreUrl": "",
            "moduleUrl": "containers/red_title_bg/index.html",
            "title": "33333333333"
        },
        "border16": {
            "id": "border16",
            "componentType": "Border",
            "style": {
                "width": 420,
                "height": 222,
                "left": 0,
                "top": 293,
                "paddingTop": 0,
                "paddingRight": 0,
                "paddingBottom": 0,
                "paddingLeft": 0,
                "borderTopStyle": "solid",
                "borderRightStyle": "solid",
                "borderBottomStyle": "solid",
                "borderLeftStyle": "solid",
                "borderTopWidth": 0,
                "borderRightWidth": 0,
                "borderBottomWidth": 0,
                "borderLeftWidth": 0,
                "borderTopColor": "transparent",
                "borderRightColor": "transparent",
                "borderBottomColor": "transparent",
                "borderLeftColor": "transparent",
                "zIndex": 109,
                "opacity": 100,
                "backgroundColor": "#fff",
                "backgroundImage": "none",
                "backgroundPositionX": "0",
                "backgroundPositionY": "0",
                "backgroundAttachment": "scroll",
                "backgroundRepeat": "repeat"
            },
            "showMore": true,
            "moreUrl": "",
            "moduleUrl": "containers/red_title_bg2/index.html",
            "title": "22222"
        },
        "content17": {
            "id": "content17",
            "componentType": "Content",
            "style": {
                "lineHeight": "inherit",
                "fontSize": "inherit",
                "fontFamily": "宋体",
                "color": "#000",
                "fontStyle": "normal",
                "fontWeight": "",
                "textAlign": "left",
                "width": 396,
                "height": 207,
                "left": 13.5,
                "top": 74,
                "paddingTop": 0,
                "paddingRight": 0,
                "paddingBottom": 0,
                "paddingLeft": 0,
                "borderTopStyle": "solid",
                "borderRightStyle": "solid",
                "borderBottomStyle": "solid",
                "borderLeftStyle": "solid",
                "borderTopWidth": 0,
                "borderRightWidth": 0,
                "borderBottomWidth": 0,
                "borderLeftWidth": 0,
                "borderTopColor": "transparent",
                "borderRightColor": "transparent",
                "borderBottomColor": "transparent",
                "borderLeftColor": "transparent",
                "zIndex": 110,
                "opacity": 100,
                "backgroundColor": "#fff",
                "backgroundImage": "none",
                "backgroundPositionX": "0",
                "backgroundPositionY": "0",
                "backgroundAttachment": "scroll",
                "backgroundRepeat": "repeat"
            },
            "dataID": 111,
            "type": "HtmlContentBlock",
            "className": "HtmlContentBlock"
        },
        "content18": {
            "id": "content18",
            "componentType": "Content",
            "style": {
                "lineHeight": "inherit",
                "fontSize": "inherit",
                "fontFamily": "宋体",
                "color": "#000",
                "fontStyle": "normal",
                "fontWeight": "",
                "textAlign": "left",
                "width": 406.96897374701666,
                "height": 185,
                "left": 8.01909307875895,
                "top": 321,
                "paddingTop": 0,
                "paddingRight": 0,
                "paddingBottom": 0,
                "paddingLeft": 0,
                "borderTopStyle": "solid",
                "borderRightStyle": "solid",
                "borderBottomStyle": "solid",
                "borderLeftStyle": "solid",
                "borderTopWidth": 0,
                "borderRightWidth": 0,
                "borderBottomWidth": 0,
                "borderLeftWidth": 0,
                "borderTopColor": "transparent",
                "borderRightColor": "transparent",
                "borderBottomColor": "transparent",
                "borderLeftColor": "transparent",
                "zIndex": 111,
                "opacity": 100,
                "backgroundColor": "#fff",
                "backgroundImage": "none",
                "backgroundPositionX": "0",
                "backgroundPositionY": "0",
                "backgroundAttachment": "scroll",
                "backgroundRepeat": "repeat"
            },
            "dataID": 112,
            "type": "HtmlContentBlock",
            "className": "HtmlContentBlock"
        }
    },
    "groups": {
        "group28": {
            "id": "group28",
            "blocks": [
                {
                    "id": "content17",
                    "type": "Component"
                },
                {
                    "id": "border14",
                    "type": "Component"
                }
            ]
        },
        "group27": {
            "id": "group27",
            "blocks": [
                {
                    "id": "content18",
                    "type": "Component"
                },
                {
                    "id": "border16",
                    "type": "Component"
                }
            ]
        },
        "group29": {
            "id": "group29",
            "blocks": [
                {
                    "id": "group27",
                    "type": "Group"
                },
                {
                    "id": "group28",
                    "type": "Group"
                },
                {
                    "id": "nav13",
                    "type": "Component"
                },
                {
                    "id": "border15",
                    "type": "Component"
                }
            ]
        }
    }
}


页面发布时提供的组件位置信息，供repaint时使用

var components={
        "container1": {            
                "width": 312,
                "height": 341,
                "left": 361,
                "top": 17               
        },
        "container4": {           
                "width": 337,
                "height": 252,
                "left": 15,
                "top": 14                          
        },
        "content6": {           
                "width": 258,
                "height": 211,
                "left": 51,
                "top": 45,
				"heightAuto":true
        },
        "container10": {           
                "width": 271,
                "height": 202,
                "left": 15,
                "top": 379
        },
        "container11": {           
                "width": 501,
                "height": 216,
                "left": 301,
                "top": 379  
        }
};
*/
