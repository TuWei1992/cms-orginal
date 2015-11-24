 $(function () {

    	 //验证码刷新
    	 window.chageCodeImage = function (id){
    			document.getElementById(id).src = base+"/bygj/validateCode.htm?d=" + new Date().getTime();
    	 };
    	 var sourceUrl = location.search;
         function submitSuggest(){
              //form 异步提交
            if (sourceUrl.length > 0 && sourceUrl.indexOf("?sourceUrl=") != -1) {   //包含 “?”
                var resultsUrl = sourceUrl.split('?sourceUrl=',2);
                //alert(resultsUrl[1]);
                var data = {
                    suggestContent:$("#suggestContent").val(),
                    contactMobile:$("#contactMobile").val(),
                    checkCode:$("#checkCode").val(),
                    sourceUrl:resultsUrl[1]
                 }
              }else{
                var data = {
                    suggestContent:$("#suggestContent").val(),
                    contactMobile:$("#contactMobile").val(),
                    checkCode:$("#checkCode").val()
                 }
              }

             $.post(base +　"/feedback/suggestAdd.htm", data, function(result){
                 var res = Number(result);
                 if (res ===0) {
                     ECar.dialog.alert('验证码校验失败');
                      $('#checkCode').addClass('border-red');
                      chageCodeImage('NewVerifyNode');
                 }else if(res ===1) {
                     ECar.dialog.showLoading({
                        title: '消息提示',
                        loadingText: '保存成功，您即将回到首页...',
                        width: 350
                      });
                      $('#lx_forms').find('input[class^="lx-cont"]').val('');
                      $('#lx_forms').find('textarea').val('');
                      $('.isay-nums').find('b').html(500);
                      chageCodeImage('NewVerifyNode');
                      setTimeout(function(){
                          window.location.href = base;
                      },3000);
                      $("[placeholder]").trigger("blur");
                 }else if (res ===-1) {
                     ECar.dialog.alert('保存失败');
                     chageCodeImage('NewVerifyNode');
                 }
             });
        }

	       $("textarea[maxlength]").keyup(function(){
	    	   var area=$(this);
	    	   var max=parseInt(area.attr("maxlength"),10); //获取maxlength的值
	    	   if(max>0){
		    	   if(area.val().length>max){ //textarea的文本长度大于maxlength
		    		   area.val(area.val().substr(0,max)); //截断textarea的文本重新赋值
		    	   }
	    	   }
	        });


        //textarea 计数器
        var isayTxt = $('#suggestContent');
        isayTxt.bind('keyup input focus paste',function () {
            var isayTxtLen= isayTxt.val().replace(/\s+/g,"").length;   //全局查找多余的空白转为null
            if (isayTxtLen <= 500 && isayTxtLen > 0) {
                isayTxt.removeClass('border-red');
                $('.lx-msg-error').hide();
                $('.isay-nums').find('b').html(500 - isayTxtLen);
            }else if(isayTxtLen ==0){
            	$('.isay-nums').find('b').html(500);
            }else if(isayTxtLen > 500 ){
            	$('.isay-nums').find('b').html(0);
            }
        });

       //验证码是否显示错误提示信息
        var  lxContNode = $('#checkCode');
        lxContNode.bind('keyup input focus paste',function () {
            var lxContNodeValll= $(this).val().replace(/\s+/g,"").length;   //检测及时的输入框信息
            if (lxContNodeValll > 0) {
                lxContNode.removeClass('border-red');
               $('.lx-msg-error').hide();
            };
        });

        //输入框显示placeholder效果
        ECar.placeholder($('.lx-cont-tel'),{nullClass : "clr-gray9"});
        ECar.placeholder($('.lx-cont-node'),{nullClass : "clr-gray9"});
        ECar.placeholder($('.isay-txt'),{nullClass : "clr-gray9"});

        //表单提交验证



        $("#lxSubmitBtn").click(function(){
            var lxContNode = $('#checkCode'),
            lxContNodeVal = $('#checkCode').val();

            //textarea验证
            if (isayTxt.val().length <=0) {
                isayTxt.addClass('border-red');
                $('.lx-msg-error').show();
                $('.lx-msg-error span').text('请填写您的意见或建议！');
                return false;
            };

            //验证码=======验证

            if(lxContNodeVal.length <= 0){
                lxContNode.addClass('border-red');
                $('.lx-msg-error').show();
                $('.lx-msg-error span').text('请输入验证码');
                return false;
            }else if(lxContNodeVal.length > 0 && lxContNodeVal.length <4  || lxContNodeVal.length > 4 ){
                lxContNode.addClass('border-red');
                $('.lx-msg-error').show();
                $('.lx-msg-error span').text('您输入的验证码错误，请重新输入验证码');
                return false;
            }


            /*else if (!/^[A-Za-z]+$/.test(lxContNodeVal)) {
                lxContNode.addClass('border-red');
                $('.lx-msg-error').show();
                $('.lx-msg-error span').text('您输入的验证码错误，请重新获取验证码');
                return false;
            };*/

            submitSuggest();

           });







    });