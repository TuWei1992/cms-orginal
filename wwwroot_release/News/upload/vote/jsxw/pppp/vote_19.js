(function(){
	//实现jquery的html方法。
	var html=function(){
		function __execScripts(__scripts){//按顺序执行脚本
			var scriptWapper,
				currentScript=__scripts.shift();
				
			if(currentScript==null){return;}
			if(currentScript.src){
				loadJs(currentScript.src,function(){__execScripts(__scripts);});
			}else{
				scriptWapper='try{'+currentScript.text+'}catch(ex){alert("调查中有js错误！");}';	
				
				if (window.execScript) {
					window.execScript(currentScript.text);
				} else {
					window['eval'](currentScript.text);
				}
				__execScripts(__scripts);			
			}
		}
		function loadJs(url, onsuccess) {
			var head = document.getElementsByTagName('head')[0] || document.documentElement,
				script = document.createElement('script'),
				done = false;
			
			script.src = url;
		
			script.onerror = script.onload = script.onreadystatechange = function() {
				if (!done && (!this.readyState || this.readyState === "loaded" || this.readyState === "complete")) {
					done = true;
					if (onsuccess) {
						onsuccess();
					}
					script.onerror = script.onload = script.onreadystatechange = null;
				}
			};
			head.appendChild(script);
		}
		
		return function(el,s){
			//getter
			if(s===undefined){
				return el.innerHTML;
			}
			
			//setter
			var match,attrs,srcMatch,__execScript,
				scripts=[],
				rscript = /(?:<script([^>]*)?>)((\n|\r|.)*?)(?:<\/script>)/ig,
				rsrc = /\ssrc=([\'\"])(.*?)\1/i;
			
			//提取脚本
			while (match = rscript.exec(s)) {
				attrs = match[1];
				srcMatch = attrs ? attrs.match(rsrc) : false;
				if (srcMatch && srcMatch[2]) {
					scripts.push({src:srcMatch[2]});				
				} else if (match[2] && match[2].length > 0) {
					scripts.push({text:match[2]});
				}
			}
			//el.innerHTML=s.replace(rscript,'');
			setHtml(el,s.replace(rscript,''));
			setTimeout(function(){__execScripts(scripts);},0);
		}
	}();
	
	//为兼容IE中innerHTML设置元素内容时报"未知的运行时错误"
	function setHtml(el,html){
		try{
			el.innerHTML=html;
		}catch(ex){
			while(el.lastChild){
				el.removeChild(el.lastChild);
			}
			var tempDiv=document.createElement('div'),
				frag=document.createDocumentFragment();
			tempDiv.innerHTML=html;
			
			while(tempDiv.firstChild){
				frag.appendChild(tempDiv.firstChild);
			}
			el.appendChild(frag);
			frag=tempDiv=null;
		}
	}
	
	function loadHTML(id, path){
		var xmlObj = null;
		try{
			xmlObj = new ActiveXObject('Msxml2.XMLHTTP');
		}catch(e){
			try{
				xmlObj = new ActiveXObject('Microsoft.XMLHTTP');
			}catch(e){
				xmlObj = new XMLHttpRequest();
			}
		}
		xmlObj.onreadystatechange = function(){
			if(xmlObj.readyState == 4){
				var elem=document.getElementById(id);
				if(elem) html(elem,xmlObj.responseText);
			}
		}
		xmlObj.open ('GET', path, true);
		xmlObj.send ('');
	}
	if(!document.body){
		if(document.addEventListener){
			document.addEventListener( 'DOMContentLoaded', loadfunc, false );
		}else{
			window.attachEvent('onload',loadfunc);
		}
	}else{
		loadfunc();
	}
	function loadfunc(){
		var siteRoot = 'http://news.2x.zving.com/',
			dynamicUrl='/zcms/vote/preview?ID=19',
			staticUrl='http://news.2x.zving.com/upload/vote/jsxw/pppp/vote_19.html',path=staticUrl;
		if(document.getElementById("votejs_19")){
			var prefix =document.getElementById("votejs_19").getAttribute("Prefix");
			if(prefix && prefix.indexOf("preview")>0){
				path = dynamicUrl
			}else{
				var htmlpoint =document.getElementById("votejs_19").getAttribute("htmlpoint");
				if(htmlpoint){
					path = path.replace(siteRoot,htmlpoint);					
				}
			}			
		}
		//专题编辑页面要避免使用缓存页面
		if(/special\/designer\.zhtml/.test(location.href)){
			path=path.replace(/^([^#?]+)(\?.*)?(#.*)?$/,function($0,$1,$2,$3){
				return $1+($2?$2+'&':'?')+(+new Date())+$3||'';
			});
		}
		
		loadHTML('votejs_19',path);
	}
	
})()