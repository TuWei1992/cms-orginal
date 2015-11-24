
/*************************公用方法和属性****************************/
var JSONA = {
	toString: function(O) {
		var string = [];
		var isArray = function(a) {
			var string = [];
			for(var i=0; i< a.length; i++) string.push(JSONA.toString(a[i]));
			return string.join(',');
		};
		var isObject = function(obj) {
			var string = [];
			for (var p in obj){
				if(obj.hasOwnProperty(p) && p!='prototype'){
					string.push('"'+p+'":'+JSONA.toString(obj[p]));
				}
			};
			return string.join(',');
		};
		if (!O) return false;
		if (O instanceof Function) string.push(O);
		else if (O instanceof Array) string.push('['+isArray(O)+']');
		else if (typeof O == 'object') string.push('{'+isObject(O)+'}');
		//else if (typeof O == 'string') string.push('"'+O.replace(/(\/|\")/gm,'\\$1')+'"');
		else if (typeof O == 'string') string.push('"'+O+'"');
		else if (typeof O == 'number' && isFinite(O)) string.push(O);
		return string.join(',');
	},
	evaluate: function(str) {
		return (typeof str=="string")?eval('(' + str + ')'):str;
	}	
};

Observable={
	_events:{},
	addEvent:function(evtType,handler){
		if(!this._events[evtType]){
			this._events[evtType]=[];
		}
		this._events[evtType].push(handler);
	},
	fireEvent:function(evtType){
		var args =  Array.prototype.slice.call(arguments, 1);
		if(this._events[evtType] && this._events[evtType].length>0){
			for(var i=0;i<this._events[evtType].length;i++){
				this._events[evtType][i].apply(this||window,args);
			}
		}
	},
	removeEvent:function(evtType,handler){
		if(!handler){
			this._events[evtType]=[];
		}else{
			for(var i=0;i<this._events[evtType].length;i++){
				this._events[evtType][i]==handler;
				this._events[evtType].splice(i,1);
			}
		}
	}
};

function getEvtPos(evt) {
	var evt= evt || window.event;
	var pos = {};
	pos.x = evt.clientX+document.body.scrollLeft+document.documentElement.scrollLeft;
	pos.y = evt.clientY+document.body.scrollTop+document.documentElement.scrollTop;
	return pos;
};

function getElPos(el) {
	var doc = el.ownerDocument;
	if(el.parentNode===null||el.style.display=='none')
		return false;
	var parent = null;
	var pos = [];
	var box;
	if(el.getBoundingClientRect){//IE,FF3,己很精确，但还没有非常确定无误的定位
		box = el.getBoundingClientRect();
		var scrollTop = Math.max(doc.documentElement.scrollTop, doc.body.scrollTop);
		var scrollLeft = Math.max(doc.documentElement.scrollLeft, doc.body.scrollLeft);
		var X = box.left + scrollLeft - doc.documentElement.clientLeft;
		var Y = box.top + scrollTop - doc.documentElement.clientTop;
		if($.browser.msie){
			X--;
			Y--;
		}
		return {x:X, y:Y};
	}
	if (el.parentNode) {
		parent = el.parentNode;
	}else {
		parent = null;
	}
	while (parent && parent.tagName != 'BODY' && parent.tagName != 'HTML'){
		pos[0] -= parent.scrollLeft;
		pos[1] -= parent.scrollTop;
		if (parent.parentNode){
		  parent = parent.parentNode;
		}else{
		  parent = null;
		}
	}
	return {x:pos[0],y:pos[1]}
};

function getIframeWin(frameId){ 
	return innerDoc = $G(frameId).contentWindow;
}


