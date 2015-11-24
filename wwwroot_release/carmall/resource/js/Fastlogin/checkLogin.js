/**
 * 验证登录
 * 
 * path-用户账户体系域名,checktype-验证类型：B-校验是否绑定手机，L-校验是否登录，R-用户注册
 * 
*/
function checkLogin(path, checktype)
{
	if(path == null || path == '' || path.indexOf('.com') == -1)
	{
		alert("请传入正确的域名参数");
		return;
	}
	if(checktype == null || checktype == '')
		checktype = "login";
	else if(checktype == 'B')
		checktype = "bind";
	else if(checktype == 'L')
		checktype = "login";
	else if(checktype == 'R')
		checktype = "register";
	else
		checktype = "login";
	
	var dialogTitle = checktype == 'bind' ? '请填写手机号' : checktype == 'login' ? '快速注册' : checktype == 'register' ? '快速注册' : '';
	var dialogHeight = checktype !== 'bind' ? 475 : 215;
	var dialogWidth = 480;
	
	document.domain = path.substring(path.indexOf('.') + 1, path.indexOf('.com') + 4);
	
	var getUrl = path + "/simple/checkUser.htm?simple_checktype=" + checktype + "&d=" + new Date().getMilliseconds() + "&callback=?";
	
	$.ajax({
	   type:'post',
	   url: getUrl,
	   dataType: "jsonp",
	   jsonp: "callback",
	   success:function(data){
		   if(data)
		   {
			   if(data["simple_status"] == 10){
				   var obj = {"userid" : data["simple_userid"]};
				   backCallFun(obj);
			   } else if(data["simple_status"] == 100 || data["simple_status"] == -2) {
				   //验证类型是绑定手机，而用户是未登录状态时弹出登录页面，否则弹出绑定手机页面
				   if(data["simple_status"] == -2)
				   {
					   checktype = "login_bind";
					   dialogTitle = "快速注册";
					   dialogHeight = 475;
				   }
				   ECar.dialog.open({
						title: dialogTitle,
						width: dialogWidth,
						height:dialogHeight,
						content: '<iframe  id="inneriframe" name="inneriframe" class="dialog-iframe" frameborder="no" border="0" scrolling="no" src="' + path +'/simple/toSimple.htm?simple_pagetype=' + checktype + '"></iframe>'
					});
			   } else if(data["simple_status"] == -1) {
				   alert("服务器繁忙，请稍后再试");
			   } else if(data["simple_status"] == -3) {
				   alert("请确认验证类型");
			   }
		   } else {
			   alert("服务器繁忙，请稍后再试");
		   }
	   },
	   error:function(){					   
	    alert("error");
	   }
	});
}
