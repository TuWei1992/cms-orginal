// JavaScript Document

//搜索框提示
function soClick(obj){
	if(obj.value=="请输入检索关键字") obj.value =""; 
}
function soBlur(obj){
	if(obj.value=="") obj.value ="请输入检索关键字"; 
}

//加入收藏
function AddFav()
{
window.external.AddFavorite(location.href,document.title)
}
function addFavorite(url,title)
    {
		if(url=="http:///") return false;
		if ( document.all ){
			window.external.addFavorite(url,title); 
	    } else if ( window.sidebar ) {
			window.sidebar.addPanel(title,url, ""); 
		}
   }
   
//设为主页
function setHomepage(url)
{
	if(url=="http:///") return false;
	if (document.all){
		document.body.style.behavior='url(#default#homepage)';
		document.body.setHomePage(url);
	}else if (window.sidebar){
		if(window.netscape){
			try{
			netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
			alert("已成功设置为首页");
			}catch (e){
					alert( "该操作被浏览器拒绝，如果想启用该功能，请在地址栏内输入 about:config,然后将项 signed.applets.codebase_principal_support 值该为true" );
			}
		}
			var prefs = Components.classes['@mozilla.org/preferences-service;1'].getService(Components. interfaces.nsIPrefBranch);
			prefs.setCharPref('browser.startup.homepage',url);
	}
}
