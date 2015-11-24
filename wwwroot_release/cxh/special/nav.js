//zving.nav.js
(function(){
//一个表示关于每种菜单类型，样式中固定部分(主要是各级li，a，span的内外边距信息，在调整宽度时可能需要用于计算)的配置表（以菜单类型标识为key）
var navsRegistered={
	low_relief:{
		//isWidthFixed:true,
		ul_paddingLeft:20,
		lvl0_paddingLeft:5,
		lvl0_paddingRight:5
	},
	flat:{
		ul_paddingLeft:10,
		lvl0_paddingLeft:5,
		lvl0_paddingRight:5,
		widthDiff:10 //顶级菜单项和级联菜单项的宽度差（width属性数值的差）
	},
	highlight:{
		ul_paddingLeft:20,
		lvl0_paddingLeft:5,
		lvl0_paddingRight:5
	},
	gradual:{
		ul_paddingLeft:0,
		lvl0_paddingLeft:0,
		lvl0_paddingRight:0
	},
	simpleness:{}
};

var Nav=function(items,options){

	var defaultOptions={
		type:'highlight',
		//width:1000,//整个菜单的宽度（单位px）
		bgColor:'bg_blue',//基调色
		direction:'down',//子菜单的展开方式（向上、向下，向左，向右）
		itemWidthChangable:false,//是否可改变topListItemWidth
		topListItemWidth:100//顶级菜单项的宽度（单位px）
	};
	this.items=items||[];
	this.options={};
	if(typeof options==='object'){
		for(var key in options){
			if(options.hasOwnProperty(key)){
				this.options[key]=options[key];
			}
		}
	};

	for(var key in defaultOptions){
		if(defaultOptions.hasOwnProperty(key)&& !this.options[key]){
			this.options[key]=defaultOptions[key];	
		}
	}
	this.templateUrl='navs/'+this.options.type+'/index.html';
	this.setWidth(this.options.width);
};
//为每个子导航项生成html代码（包括其子元素）
Nav.genHtmlForItem=function(listItem){
		var html='';
		if(!listItem)return '';
		html+='<li class="sub"><a href="'+listItem.href+'" target="'+listItem.target+'" '+zving.Nav.returnFalse+'><span>'+listItem.name+'</span></a>';
		
		if(listItem.items && listItem.items.length){
			html+='<ul>';
			for(var i=0;i<listItem.items.length;i++){
				html+=zving.Nav.genHtmlForItem(listItem.items[i]);
			}
			html+='</ul>';
		}
		
		html+='</li>';
		return html;
};
Nav.prototype={
	constructor:Nav,
	

	
//编辑器中用下列数组结构存储导航的节点数据,因为这种数据结构有利于添加、删除、排序、修改操作。
//每次编辑操作后，把菜单的相关信息转换为对象数据并调用模板重新刷新菜单的显示。
/*
//表示菜单的数据对象
var nav={
	items:itemsTree,
	options:{
		width:600,//整个菜单的宽度
		bgColor:'bg_purple',//基调色
		direction:'down',//子菜单的展开方式（向上、向下，向左，向右）
		itemWidthChangable:false
		//目前highlight,gradual支持改变宽度
		topListItemWidth:100//顶级菜单项的宽度(只有在itemWidthChangable为true时，这个属性的值才会起作用)（单位px）
	}
};
//data为带层次关系的菜单项数组
var itemsTree=[
{
	index:0,//导航项在数组中的索引位置
	name:'0-0',//导航项显示的名称
	href:'#',//对应的链接
	parent:-1,//符导航项的索引
	target:'_blank',
	order:1//导航项的编号id，这个值在导航项被添加的时候，值为兄弟导航项的order值集合的最大值+1；添加后，可通过上移、下移操作调整导航项在所处子导航列表中的位置。
},
{
	index:1,
	name:'0-1',
	href:'#',
	parent:-1,
	target:'_blank',
	order:2
},
{
	index:2,
	name:'0-2',
	href:'#',
	parent:-1,
	target:'_blank',
	order:3
},
{
	index:3,
	name:'0-3',
	href:'#',
	parent:-1,
	target:'_blank',
	order:4
},
{
	index:4,
	name:'0-4',
	href:'#',
	parent:-1,
	target:'_blank',
	order:5
},
//
{
	index:5,
	name:'0-1-0',
	href:'#',
	parent:1,
	target:'_blank',
	order:1
},
{
	index:6,
	name:'0-1-1',
	href:'#',
	parent:1,
	target:'_blank',
	order:2
},
{
	index:7,
	name:'0-1-2',
	href:'#',
	parent:1,
	target:'_blank',
	order:3
},
{
	index:8,
	name:'0-1-3',
	href:'#',
	parent:1,
	target:'_blank',
	order:4
},

{
	index:9,
	name:'0-1-0-0',
	href:'#',
	parent:5,
	target:'_blank',
	order:1
},
{
	index:10,
	name:'0-1-0-1',
	href:'#',
	parent:5,
	target:'_blank',
	order:2
},
{
	index:11,
	name:'0-1-0-2',
	href:'#',
	parent:5,
	target:'_blank',
	order:3
}
];
*/
/*
//传递给模板的数据格式如下
var dataForTmplRender={
items:[
{
	name:'百度',
	target:'_blank',
	href:'http:www.baidu.com'
},
{
	name:'google',
	target:'_blank',
	href:'http:www.google.com',
	items:[
		{
			name:'1-0',
			target:'_blank',
			href:'#',
			items:[
				{
					name:'1-0-0',
					target:'_blank',
					href:'#',
					items:[
						{
							name:'1-0-0-0',
							target:'_blank',
							href:'#'
						},
						{
							name:'1-0-0-1',
							target:'_blank',
							href:'#'
						},
						{
							name:'1-0-0-2',
							target:'_blank',
							href:'#'
						}
					]
				},
				{
					name:'1-0-1',
					target:'_blank',
					href:'#',
					items:[
						{
							name:'1-1-0-0',
							target:'_blank',
							href:'#'
						},
						{
							name:'1-1-0-1',
							target:'_blank',
							href:'#'
						},
						{
							name:'1-1-0-2',
							target:'_blank',
							href:'#'
						}
					]
				}
			]
		},
		{
			name:'1-1',
			target:'_blank',
			href:'#'
		},
		{
			name:'1-2',
			target:'_blank',
			href:'#'
		}
	]
},
{
	name:'sina',
	target:'_blank',
	href:'http:www.sina.com',
	items:[
		{
			name:'新浪新闻',
			target:'_blank',
			href:'#'
		},
		{
			name:'新浪博客',
			target:'_blank',
			href:'#'
		}
	]
}
],
options:nav.options//对菜单样式对象的引用
};
*/	
	//数据转换方法（把菜单对应的节点树转换成模板接受的数据形式）
	dataAdapter:function(){
		var topItems=[];
		var items=this.items
		for(var i=0;i<items.length;i++){
			if(items[i]&&items[i].parent==-1){
				topItems.push(items[i]);
			}
		}
		//排序
		topItems.sort(function(itemA,itemB){
				return itemA.order-itemB.order;
		});
		
		var dataForTmplRender=[];
		for(var i=0;i<topItems.length;i++){
			dataForTmplRender.push(this.transformItem(topItems[i].index));
		}
		//console.log(JSON.stringify(dataForTmplRender,null,4));
		return dataForTmplRender;
	},
	//转换指定的树分支的数据格式
	transformItem:function(index){
		var item=this.items[index];
		var itemForTmplRender={
			name:item.name,
			href:item.href,
			target:item.target,
			order:item.order,
			items:[]
		};
		
		//找到所有子节点
		var items=this.items;
		for(var i=0;i<items.length;i++){
			if(items[i]&&items[i].parent==item.index){
				itemForTmplRender.items.push(this.transformItem(items[i].index));
			}
		}
		//排序
		itemForTmplRender.items.sort(function(itemA,itemB){
				return itemA.order-itemB.order;
		});
		
		return itemForTmplRender;
	},
	//生成html代码
	toHtml:function(){
		var navData={
			items:this.dataAdapter(),
			options:this.options
		};
		//console.log(JSON.stringify(navData,null,4));
		var html=simpleTmpl((window.Editor&&window.Editor.templateCache)?window.Editor.templateCache[this.templateUrl]:this.options.type,navData);		
		return html;
	},
	addItem:function(parentIndex,itemInfo){
		var index,space=this.space;
		if(space.length){
			index=space.pop();
		}else{
			index=this.items.length;
		}
		
		var order;
		
		var childItems=this.getChildNodes(parentIndex);
		childItems.sort(function(itemA,itemB){
				return itemA.order-itemB.order;
		});
		order=childItems.length?childItems[childItems.length-1].order+1:1;
		
		this.items[index]={
			index:index,
			name:itemInfo.name,
			href:itemInfo.href,
			target:itemInfo.target,
			parent:parentIndex,
			order:order
		};
		return index;
	
	},
	deleteItem:function(index){
		var childNodes=this.getChildNodes(index);
		
		for(var i=0;i<childNodes.length;i++){
			this.deleteItem(childNodes[i].index);
		}
				
		this.items[index]=undefined;
		this.space.push(index);
	},
	updateItem:function(index,itemInfo){
		var item=this.items[index];
		for(var key in itemInfo){
			if(itemInfo.hasOwnProperty(key)){
				item[key]=itemInfo[key];
			}
		}
	},
		
	space:[],//存储菜单的数组中的所有空闲位置。
	getChildNodes:function(index){
		var childItems=[];
		var items=this.items;
		for(var i=0;i<items.length;i++){
			if(items[i]&&items[i].parent==index){
				childItems.push(items[i]);
			}
		}
		
		return childItems;
	},
	setStyle:function(options){
		for(var key in options){
			if(options.hasOwnProperty(key)){
				this.options[key]=options[key];
			}
		}
		this.templateUrl='navs/'+this.options.type+'/index.html';
	},
	toNavDataTree:function(){
		
		return parseNav(this);
	},
	sort:function(index,newParentIndex,newOrder){//newOrder为对应项在新列表中的次序
		var items=this.items;
		
		var childItems=this.getChildNodes(newParentIndex);
		childItems.sort(function(itemA,itemB){
				return itemA.order-itemB.order;
		});
		
		if(items[index].parent==newParentIndex){
			
			console.log('childItems.indexOf(items[index]):'+childItems.indexOf(items[index]));
			console.log('childItems:'+JSON.stringify(childItems,null,4));
			childItems.splice(childItems.indexOf(items[index]),1);	
			
		}
		var orders=[];
		for(var i=0;i<childItems.length;i++){
			orders.push(childItems[i].order);
		}
		if(orders.length){
			orders.push(orders[orders.length-1]+1);
		}else{
			orders.push(1);
		}
		childItems.splice(newOrder-1,0,items[index]);
		
		
		for(var i=0;i<childItems.length;i++){
			childItems[i].order=orders[i];				
		}
		items[index].parent=newParentIndex;
	},
	isNoAddBtn:function(){
		return this.options.type==='simpleness';//简单菜单不支持子菜单，不提供添加子菜单项的功能。
	},
	setWidth:function(width){
		this.options.width=width;
		if(this.options.itemWidthChangable){
			this.options.topListItemWidth=getTopItemWidth(this.options.type,this.options.width,this.getChildNodes(-1).length);		
		}
		this.options.subListItemWidth=getSubItemWidth(this.options.type,this.options.topListItemWidth);
	}
};

Nav.navsRegistered=navsRegistered;

Nav.returnFalse='';
/*************/
// Simple JavaScript Templating
// John Resig - http://ejohn.org/ - MIT Licensed
  var cache = {};
 
  var simpleTmpl = function (str, data){
    // Figure out if we're getting a template, or if we need to
    // load the template - and be sure to cache the result.
	var fn;
	if(!/\W/.test(str)){
		fn = cache[str] = cache[str] || simpleTmpl(document.getElementById(str).innerHTML);
	}else{
		var fnStr="var p=[],print=function(){p.push.apply(p,arguments);};" +
		
        // Introduce the data as local variables using with(){}
        "with(obj){p.push('" +
		
        // Convert the template into pure JavaScript
        str
		.replace(/[\r\t\n]/g, " ")
		.split("<%").join("\t")
		.replace(/((^|%>)[^\t]*)'/g, "$1\r")
		.replace(/\t=(.*?)%>/g, "',$1,'")
		.split("\t").join("');")
		.split("%>").join("p.push('")
		.split("\r").join("\\'")
		+ "');}return p.join('');";
		//console.log(fnStr);
		fn=new Function("obj",fnStr);
	}
 
    // Provide some basic currying to the user
    return data ? fn( data ) : fn;
  };


window.zving=window.zving||{};
zving.Nav=Nav;




})();
//
function getTopItemWidth(type,width,number){
	if(number<1)return 200;
	var topItemWidth=100;
	switch(type){
		case 'highlight':
			topItemWidth=(width-zving.Nav.navsRegistered[type].ul_paddingLeft)/number-(zving.Nav.navsRegistered[type].lvl0_paddingLeft+zving.Nav.navsRegistered[type].lvl0_paddingRight);
			break;
		case 'low_relief':
			topItemWidth=(width-zving.Nav.navsRegistered[type].ul_paddingLeft)/number-(zving.Nav.navsRegistered[type].lvl0_paddingLeft+zving.Nav.navsRegistered[type].lvl0_paddingRight)
			break;
		case 'flat':
			topItemWidth=(width-zving.Nav.navsRegistered[type].ul_paddingLeft)/number-(zving.Nav.navsRegistered[type].lvl0_paddingLeft+zving.Nav.navsRegistered[type].lvl0_paddingRight)
			break;
		case 'gradual':
			topItemWidth=(width-zving.Nav.navsRegistered[type].ul_paddingLeft)/number-(zving.Nav.navsRegistered[type].lvl0_paddingLeft+zving.Nav.navsRegistered[type].lvl0_paddingRight)
			break;
			
		
		default:break;	
	}		
		
	return Math.floor(topItemWidth);
	
}
function getSubItemWidth(type,topListItemWidth){
	var subListItemWidth=0;
	switch(type){
		case 'highlight':break;
		case 'low_relief':break;
		case 'flat':
			subListItemWidth=topListItemWidth-(zving.Nav.navsRegistered[type].widthDiff);
			break;
		case 'gradual':break;			
		
		default:break;	
	}		
		
	return Math.floor(subListItemWidth);
	
}
//
function parseNode(nav,index,noAddBtn){
	var html;
	var item=nav[index];
	if(!item)return;
	
	var childItems=findChildItems(nav,index);
	
	if(!childItems.length){
		html='<li class="index'+item.index+'" index="'+item.index+'"><div class="itemWrapper"><div class="toolBar">'+(!noAddBtn ?'<input  class="btnAddSubItem" type="button" value='+Lang.get('Special.AddSubMenuItem')+' />':'')+'<input  class="btnDeleteItem" type="button" value='+Lang.get('Special.Delete')+' /><!--input  class="btnShowItemInfo" type="button" value="属性" /--></div><div class="itemInfo"><span class="item-status status1 noSubList"></span><span class="item-name" title="'+item.href+'">'+item.name+'</span></div></div></li>';
	}else{
		html='<li class="index'+item.index+'" index="'+item.index+'"><div class="itemWrapper"><div class="toolBar">'+(!noAddBtn ?'<input  class="btnAddSubItem" type="button" value='+Lang.get('Special.AddSubMenuItem')+' />':'')+'<input  class="btnDeleteItem" type="button" value='+Lang.get('Special.Delete')+' /><!--input  class="btnShowItemInfo" type="button" value="属性" /--></div><div class="itemInfo"><span class="item-status status1 hasSubList"></span><span class="item-name" title="'+item.href+'">'+item.name+'</span></div></div><ul class="connectedSortable">'
		for(var i=0;i<childItems.length;i++){
			html+=parseNode(nav,childItems[i].index,noAddBtn);		
		}
		html+='</ul></li>';		
	}

	return html;
}

function parseNav(nav){
	var items=nav.items;
	
	var topItems=findChildItems(items,-1);
	var html='<ul class="navInfo connectedSortable">';
	for(var i=0;i<topItems.length;i++){
		html+=parseNode(items,topItems[i].index,nav.isNoAddBtn());	
	}
	html+='</ul>';
	
	return html;
}

function findChildItems(nav,index){
	var childItems=[];
	for(var i=0;i<nav.length;i++){
		if(nav[i]&&nav[i].parent==index){
			childItems.push(nav[i]);
		}
	}
	childItems.sort(function(itemA,itemB){
			return itemA.order-itemB.order;
	});
	
	return childItems;
}


//