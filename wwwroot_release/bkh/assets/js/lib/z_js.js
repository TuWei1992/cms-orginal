$(function(){
	var numberVal = '';
	var rate_elm = document.getElementById("rate"); 
	var point = document.getElementById("point_btn");
	var point2 = document.getElementById("point_btn2");
	var step_left = document.getElementById("rate_act_left");
	var step_right = document.getElementById("rate_act_right");
	var point_number = document.getElementById("point_number");
	var point2_number = document.getElementById("point2_number");
	var start_number = document.getElementById("start_number");
	var finish_number = document.getElementById("finish_number");
	var section_number= document.getElementById("section_number");
	
    $(".z_navbcon_xj").click(function(){
        if($(this).hasClass("on")){
            if($(this).find("i").hasClass("toggle")){
                $(this).find("i").removeClass("toggle");
            }else{
                $(this).find("i").addClass("toggle")
            }
        }else{
            $(this).addClass("on").siblings().removeClass("on");
        }
    })

    $(".z_navbcon_jf").click(function(){
        if($(this).hasClass("on")){
            if($(this).find("i").hasClass("toggle")){
                $(this).find("i").removeClass("toggle");
            }else{
                $(this).find("i").addClass("toggle")
            }
        }else{
            $(this).addClass("on").siblings().removeClass("on");
        }
    });
	function aa(){ 
		var fval = 2530;//旗子积分制
		var maxWidth = rate_elm.offsetWidth - point.offsetWidth;
		var fleft = Math.round((fval/3000)*maxWidth);
		$('.icon-flags').css("left",fleft+8);
	}
	point.onmousedown = function(e){
			var events = e || window.event;
		  	(events.preventDefault) ? events.preventDefault() : events.returnValue = false;
			var x = events.clientX;
			var thisLeft = this.offsetLeft;
			var numberLeft = point_number.offsetLeft;
			var number2Left = point2_number.offsetLeft;
			var numberWidth = point_number.offsetWidth;
			var maxWidth =  rate_elm.offsetWidth - point.offsetWidth+3;
			var stepMaxLeft = point2.offsetLeft;
			var maxnumberLeft = rate_elm.offsetWidth - point_number.offsetWidth+23;
			document.onmousemove = function(e){
				var events = e || window.event;
				var thisX = events.clientX;
				(events.preventDefault) ? events.preventDefault() : events.returnValue = false;
				pointX = Math.min(maxWidth,Math.max(-4,thisLeft+(thisX-x)),stepMaxLeft);
				point.style.left = pointX+'px';
				step_left.style.width=pointX+15+'px';
				numberX = Math.min(maxnumberLeft,Math.max(-23,numberLeft+(thisX-x)),stepMaxLeft-23);
				point_number.style.left = numberX+'px';
				numberVal = Math.round(Math.max(0,pointX/maxWidth)*3000);
				point_number.innerHTML=Math.round(Math.max(0,pointX/maxWidth)*3000)+'积分';
				if(numberX<=(numberWidth-23)){
					document.getElementById("start_number").style.visibility = "hidden";
				}else{
					document.getElementById("start_number").style.visibility = "visible";
				}
				console.log(Math.abs(pointX - point2.offsetLeft));
				if(Math.abs(pointX - point2.offsetLeft) <= 80){
					if(Math.abs(pointX - point2.offsetLeft)<=1){
						point_number.style.visibility= "visible";
						point2_number.style.visibility = "hidden";
						section_number.style.visibility = "hidden";
						point.style.zIndex = 2;
					}else{
						point.style.zIndex = 1;
						section_number.style.visibility = "visible";
						point_number.style.visibility = "hidden";
						point2_number.style.visibility = "hidden";
						section_number.style.left = numberX+"px";
						section_number.innerHTML = Math.round(Math.max(0,pointX/maxWidth)*3000)+'~'+point2_number.innerHTML;
					}
				}if(Math.abs(pointX - point2.offsetLeft) > 80){
					section_number.style.visibility = "hidden";
					point_number.style.visibility = "visible";
					point2_number.style.visibility = "visible";
				}
				
				if(maxWidth==pointX){
					point.style.zIndex = 2;
				}else{
					point.style.zIndex = 1;
				}
			}
			document.onmouseup=new Function('this.onmousemove=null');
		}
		//拖动条
		point2.onmousedown = function(e){
			var events = e || window.event;
		  	(events.preventDefault) ? events.preventDefault() : events.returnValue = false;
			var x = events.clientX;
			var thisLeft = this.offsetLeft;
			var numberLeft = point_number.offsetLeft;
			var number2Left = point2_number.offsetLeft;
			var number2Width = point2_number.offsetWidth;
			var maxWidth = rate_elm.offsetWidth - point2.offsetWidth+3;
			var stepMaxRight = point.offsetLeft;
			var thisMaxNunber = point_number.offsetLeft;
			var maxnumber2Left = rate_elm.offsetWidth - point2_number.offsetWidth+23;
			document.onmousemove = function(e){
				var events = e || window.event;
				var thisX = events.clientX;
				(events.preventDefault) ? events.preventDefault() : events.returnValue = false;
				point2X = Math.min(maxWidth,Math.max(-4,thisLeft+(thisX-x)));
				if(stepMaxRight>=point2X){
					point2X = stepMaxRight;
				}
				point2.style.left = point2X+'px';
				step_right.style.width=(maxWidth-point2X)+15+'px';
				number2X = Math.min(maxnumber2Left,Math.max(-23,number2Left+(thisX-x)));
				point2_number.style.left = number2X+'px';
				point2_number.innerHTML=Math.round(Math.max(0,point2X/maxWidth)*3000)+'积分';
				if(Math.abs(point2X - point.offsetLeft) <= 80){
					if(thisMaxNunber>=number2X){
						number2X = thisMaxNunber;
						point2_number.style.left = number2X+'px';
						point_number.style.visibility = "hidden";
						section_number.style.visibility = "hidden";
						point2_number.style.visibility = "visible";
						point2.style.zIndex = 2;
					}else{
						point2.style.zIndex = 1;
						point_number.style.visibility = "hidden";
						point2_number.style.visibility = "hidden";
						section_number.style.visibility = "visible";
						section_number.style.left = number2X+"px";
						section_number.innerHTML = numberVal+'~'+Math.round(Math.max(0,point2X/maxWidth)*3000)+'积分';	
					}
				}else{
					section_number.style.visibility = 'hidden';
					point_number.style.visibility = "visible";
					point2_number.style.visibility = "visible";
				}
				if(number2X>=(809-number2Width)){
					document.getElementById("finish_number").style.display = "none";
				}else{
					document.getElementById("finish_number").style.display = "block";
				}
			}
			document.onmouseup=new Function('this.onmousemove=null');
		}
		aa();
})