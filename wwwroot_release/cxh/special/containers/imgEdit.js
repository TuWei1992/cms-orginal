imgEditState = {
	allowBigZoom:[1,100],
	zoomGap:1,
	src: '',
	apply: {
		top: 0,
		left: 0,
		width: 0,
		height: 0,
		iscut:true
	},
	scale: {
		x: 1,
		y: 1
	},
	angle: {
		imganglevalue: "0",
		isflipx: false,
		isflipy: false,
		clicknum:0
	},
	waterMake: {
		imgSrc: '',
		imgs:[]
	},
	text: {
		text: '',
		height: '26',
		fontSize: '14',
		left: 20,
		top: 20,
		color: '#336699',
		fontFamily: '宋体',
		fontWeight: '',
		textdecoration: 'none',
		fontstyle: 'normal',
		opicty: '1',
		fliterCon:'none',
		textshadowCon:'none'
	},
	smallImgPosition: {
		top: 0,
		left: 0,
		width: 0,
		height: 0
	}
};
var imgW;
var imgH;
var fontText = {
	height: '',
	font: '',
	size: '',
	fcolor: '',
	transparency: '',
	fbold: '',
	Weight: '',
	Italic: '',
	underline: '',
	text: '',
	top: '',
	left: '',
	fliterCon:'none',
	textshadowCon:'none'
};
var _move = false;

function docClick() {
	var clickImg = 0;
	$(document).click(function(e) {
		var evt = e ? e : (window.event ? window.event : null);
		var node = evt.srcElement ? evt.srcElement : evt.target;
		if ($(node).attr("id") == "photo") {
			if (clickImg == 0) {
				_move = false;
				var left = e.pageX - parseInt($("#photo").offset().left);
				var top = e.pageY - parseInt($("#photo").offset().top);
				fontText.top = top;
				fontText.left = left;
				addText();
				clickImg = 1;
				move();
			} else if (clickImg == 1) {
				if ( !! $(".TextBox").length) {
					if ($(".TextBox").text().trim() == "") {
						$("#imgBox div").remove(".TextBox");
						$("#photo").css("cursor", "text");
						docClick();
						return;
					}
				}
				$(".TextBox").attr("contentEditable", "false");
				$(".TextBox").css("cursor", "move");
				$(".TextBox").blur();
			}
		} else {
			if ($(node).attr("class") != "TextBox") {
				if ( !! $(".TextBox").length) {
					if ($(".TextBox").text().trim() == "") {
						$("#imgBox div").remove(".TextBox");
						$("#photo").css("cursor", "text");
						docClick();
						return;
					}
				}
				$(".TextBox").attr("contentEditable", "false");
				$(".TextBox").css("cursor", "move");
				$(".TextBox").blur();
			}
		}
	});
}

function moveWaterImg() {
	var _x, _y;
	var moveW = false;
	$(".warterImg").click(function() {
		moveW = false;
		$(this).addClass("waterImgCur").siblings("img").removeClass("waterImgCur");
		$S("waterimage_opicty",$(this).css("opacity"));
	}).mousedown(function(e) {
		moveW = true;
		curimgW = $(this).width();
		curimgH = $(this).height();
		waterImgSelf=$(this);
		_x = e.pageX - parseInt($(this).css("left"));
		_y = e.pageY - parseInt($(this).css("top"));
		e.preventDefault();
	});
	$(document).mousemove(function(e) {
		if (moveW) {
			e.preventDefault();
			var x = e.pageX - _x;
			var y = e.pageY - _y;
			var leftwidth = $("#photo").width() - curimgW;
			var topwidth = $("#photo").height() - curimgH;
			if (x < 0) {
				x = 0;
			} else if (x > leftwidth) {
				x = leftwidth;
			}
			if (y < 0) {
				y = 0;
			} else if (y > topwidth) {
				y = topwidth;
			}
			$(waterImgSelf).css({
				top: y,
				left: x
			});
		}
	}).mouseup(function() {
		moveW = false;
	});
}
function ImgAreaSelect() {
	this.maxW = 0;
	this.maxH = 0;
	this.imgW = 0;
	this.imgH = 0;
	this.selectW = 0;
	this.selectH = 0;
	this.handles = true;
	this.x1 = 0;
	this.y1 = 0;
	this.x2;
	this.y2;
	this.aspectRatio = "";
}
ImgArea = new ImgAreaSelect(); //裁剪页签页使用
sImgArea = new ImgAreaSelect(); //缩率图设置页签页使用
function ImgSelect(type) {
	var currentimgW = $("#photo").width();
	var currentimgH = $("#photo").height();
	if(!type){ //不传类别即默认为裁剪功能
		$("#Imgwidth").val("");
		$("#Imgheight").val("");
		ImgArea.imgW = currentimgW;
		ImgArea.imgH = currentimgH;
		ImgArea.maxW = currentimgW;
		ImgArea.maxH = currentimgH;
		ImgArea.selectW = currentimgW;
		ImgArea.selectH = currentimgH;
		setaspectRatio();
	}else if(type=="getSimg"){ //类型为"getSimg"时为缩率图设置功能
		//$("#sImgScale").val("");
		sImgArea.imgW = currentimgW;
		sImgArea.imgH = currentimgH;
		sImgArea.maxW = currentimgW;
		sImgArea.maxH = currentimgH;
		sImgArea.selectW = currentimgW;
		sImgArea.selectH = currentimgH;
		setaspectRatio("getSimg");
	}
}
function setaspectRatio(type) { //比例值
	if(!type){
		var ischeck = $("#ischeck").attr("checked");
		var aspectRatio = $("#proportion input:checked").val();
		if (ischeck == "" || ischeck == null) {
			ImgArea.aspectRatio = "";
		}
		if (ischeck == "checked" && aspectRatio) {
			ImgArea.aspectRatio = aspectRatio;
		}
		setSelect();
	}else if(type=="getSimg"){
		var aspectRatio=$("#sImgScale").val();
		sImgArea.aspectRatio = aspectRatio;
		setSelect("getSimg");
	}	
}
function setSelect(type) {
	if(!type){
		return $('#photo').imgAreaSelect({
			x1: 0,
			y1: 0,
			x2: 0,
			y2: 0,
			maxWidth: ImgArea.maxW,
			maxHeight: ImgArea.maxH,
			handles: ImgArea.handles,
			aspectRatio: ImgArea.aspectRatio,
			instance: ImgArea.handles,
			onSelectStart:function(img, selection){	},
			onSelectChange: setoptions
		});
	}else if(type=="getSimg"){
		return $('#photo').imgAreaSelect({
			x1: 0,
			y1: 0,
			x2: 0,
			y2: 0,
			maxWidth: sImgArea.maxW,
			maxHeight: sImgArea.maxH,
			handles: sImgArea.handles,
			aspectRatio: sImgArea.aspectRatio,
			instance: sImgArea.handles,
			onSelectStart:function(img, selection){	
				if(Verify.hasError(null, "#sImgScaleWrap", false)){  //缩率图生成比例未填写或比例填写不正确时，给出提示并返回。
					cancelSmallImg();
					return;
				}
			},
			onSelectChange: setSimg
		});
	}	
}
function setoptions( img, selection) {	
	$("div.imgareaselect-outer").css({ "background": "transparent"});
	ImgArea.selectW = selection.width;
	ImgArea.selectH = selection.height;
	ImgArea.x1 = selection.x1;
	ImgArea.y1 = selection.y1;
	ImgArea.x2 = selection.x2;
	ImgArea.y2 = selection.y2;
	$("#Imgwidth").val(ImgArea.selectW);
	$("#Imgheight").val(ImgArea.selectH);
	imgEditState.apply.left = ImgArea.x1;
	imgEditState.apply.top = ImgArea.y1;
	imgEditState.apply.width = ImgArea.selectW;
	imgEditState.apply.height = ImgArea.selectH;	
	
}

function setSimg( img, selection) {
	$("div.imgareaselect-outer").css({ "background": "transparent" });
	sImgArea.selectW = selection.width;
	sImgArea.selectH = selection.height;
	sImgArea.x1 = selection.x1;
	sImgArea.y1 = selection.y1;
	sImgArea.x2 = selection.x2;
	sImgArea.y2 = selection.y2;
	imgEditState.smallImgPosition.left = sImgArea.x1;
	imgEditState.smallImgPosition.top = sImgArea.y1;
	imgEditState.smallImgPosition.width = sImgArea.selectW;
	imgEditState.smallImgPosition.height = sImgArea.selectH;	
	
}
function setSelectblock() {
	ImgArea.selectW = $("#Imgwidth").val();
	ImgArea.selectH = $("#Imgheight").val();
	var ias = $('#photo').data('imgAreaSelect');
	if (ias.getSelection().width > 0 && ias.getSelection().height > 0) {
		var w = parseInt(ImgArea.x1) + parseInt(ImgArea.selectW);
		var h = parseInt(ImgArea.y1) + parseInt(ImgArea.selectH);
		ImgArea.x2 = w;
		ImgArea.y2 = h;
		if (w > ImgArea.maxW) {
			var x_width = ImgArea.maxW - ImgArea.x1;
			var x_sideWidth;
			if (ImgArea.selectW > x_width) {
				x_sideWidth = w - ImgArea.maxW;
				ImgArea.x2 = ImgArea.maxW;
				if (x_sideWidth > ImgArea.x1) {
					ImgArea.x1 = 0;
				} else {
					ImgArea.x1 = ImgArea.x1 - x_sideWidth;
				}
			}
		} else if (h > ImgArea.maxH) {
			var y_width = ImgArea.maxH - ImgArea.y1;
			var y_sideWidth;
			if (ImgArea.selectH > y_width) {
				y_sideWidth = h - ImgArea.maxH;
				ImgArea.y2 = ImgArea.maxH;
				if (y_sideWidth > ImgArea.y1) {
					ImgArea.y1 = 0;
				} else {
					ImgArea.y1 = ImgArea.y1 - y_sideWidth;
				}
			}
		}
		imgEditState.apply.left = ImgArea.x1;
		imgEditState.apply.top = ImgArea.y1;
		imgEditState.apply.width = ImgArea.selectW;
		imgEditState.apply.height = ImgArea.selectH;
		ias.setSelection(ImgArea.x1, ImgArea.y1, ImgArea.x2, ImgArea.y2);
		ias.update();
	} else {
		if ($("#Imgwidth").val() == "" || $("#Imgwidth").val() == null) { return; }
		if ($("#Imgheight").val() == "" || $("#Imgheight").val() == null) { return; }
		$("div.imgareaselect-outer").css({ "background": "transparent"});
		$("#photo").imgAreaSelect({
			x1: 0,
			y1: 0,
			x2: $("#Imgwidth").val(),
			y2: $("#Imgheight").val()
		});
	}
}
function addWaterImg(ele){
	var newImgW = $("#photo").width();
	var wartersrc = $(ele).attr("src");
	$(ele).addClass("current");
	$("#imgBox").css({ "position": "relative", "width": newImgW, "margin": "0 auto" });
	$("#imgBox").append("<img  class='warterImg' src='" + wartersrc + "' />");
	moveWaterImg();
}
function setWaterImgOpicty() {
	var opictyvalue= $V("waterimage_opicty");
	if(!!$(".waterImgCur").length){
		$(".waterImgCur").css({"opacity": opictyvalue,"filter":"alpha(opacity=" + opictyvalue*100 + ")"});
	}
}
$(function() {
	$("#menus  li").click(function() {
		var num = $(this).prevAll().length;
		if (num == 0 || num == 2 || num == 3 || num==4) {
			if ($("#photo").attr("style") && $("#photo").attr("style").length > 35) {
				Dialog.confirm("是否保存旋转操作", saveRotate, function() { cancelRotate(); });
			}
			$("#photo").attr("style", "");
		}
		if (num != 0 && num > 0) {
			var ias = $('#photo').data('imgAreaSelect');
			if (ias.getSelection().width > 0 && imgEditState.apply.iscut) {
				Dialog.confirm(tipCutting, applyCut, function() {
					cancel();
					if(num == 4){
						ImgSelect("getSimg");  //如果当前页签已经切换到缩率图设置页面，那么取消裁剪后要重新调用缩率图的方法
					}
				});
				ias.setOptions({ "movable": false });
			}else{
				if(num == 4){
					ImgSelect("getSimg");  //如果当前页签已经切换到缩率图设置页面，那么取消裁剪后要重新调用缩率图的方法
				}
			}
			$(".imgareaselect-selection").parent().hide();
			$(".imgareaselect-outer").hide();
			ias.setOptions({ "disable": true });
			ias.update();
			if (num == 1) {
				if($.browser.safari && (typeof setTextLayTimer != "undefined")){
					clearInterval(setTextLayTimer);
				}
				waterMarkOk = false;
				if (!appTextOk && ( !! $(".TextBox").length)) {
					Dialog.confirm(tipPressText, applyText, function() {
						cancelText();
					});
				}
				if ( !! $("#imgBox > .TextBox").length) {
					$("#imgBox div").remove(".TextBox");
				}
				appTextOk = false;
				if (!waterMarkOk && ( !! $(".warterImg").length)) {
					Dialog.confirm(tipPressImage, applyWaterMark, function() {
						cancelWaterMark();
					});
				}
				if ( !! $("#imgBox > .warterImg").length) {
					$("#imgBox img").remove(".warterImg");
					$(".waterImgWrap > img.current").removeClass("current");
				}
				$("#photo").css("cursor", "default");
				$(document).off("click"); //注销事件
				if (ias.getSelection().width > 0 && !imgEditState.apply.iscut) {
					Dialog.confirm("是否生成缩率图", saveSmallImg, function() { cancelSmallImg(); });
					ias.setOptions({ "movable": false });
				}
			}
			if (num == 2) {
				if($.browser.safari && (typeof setTextLayTimer != "undefined")){
					clearInterval(setTextLayTimer);
				}
				waterMarkOk = false;
				if (!appTextOk && ( !! $(".TextBox").length)) {
					Dialog.confirm(tipPressText, applyText, function() { cancelText(); });
				}
				if ( !! $("#imgBox > .TextBox").length) {
					$("#imgBox div").remove(".TextBox");
				}
				$("#photo").css("cursor", "default");
				$(document).off("click"); //注销事件
				if (ias.getSelection().width > 0 && !imgEditState.apply.iscut) {
					Dialog.confirm("是否生成缩率图", saveSmallImg, function() { cancelSmallImg(); });
					ias.setOptions({ "movable": false });
				}
			}
			if (num == 3) {
				if ($G("#color").watch) {
					$G("#color").watch("value", function(attr, oldVal, newVal) {
						setTimeout(setOptions, 100);
						return newVal;
					});
					$G("#linecolor").watch("value", function(attr, oldVale, newVale) {
						setTimeout(setOptions, 100);
						return newVale;
					});
				} else if ($.browser.version == "9.0") {
					$("#color").on("textinput", function() {
						setOptions();
					});
					$("#linecolor").on("textinput", function() {
						setOptions();
					});
				}else if($.browser.safari){
					setTextLayTimer=setInterval(setOptions,200);
				} else {
					$("#color").on("propertychange", function() {
						setOptions();
					});
					$("#linecolor").on("propertychange", function() {
						setOptions();
					});
				}
				appTextOk = false;
				if (!waterMarkOk && ( !! $(".warterImg").length)) {
					Dialog.confirm(tipPressImage, applyWaterMark, function() { cancelWaterMark(); });
				}
				if ( !! $("#imgBox > .warterImg").length) {
					$("#imgBox img").remove(".warterImg");
					$(".waterImgWrap > img.current").removeClass("current");
				}
				if ( !! $("#imgBox > .TextBox").length) {
					$("#photo").css("cursor", "default");
				} else {
					$("#photo").css("cursor", "text");
					docClick();
				}

				if (ias.getSelection().width > 0 && !imgEditState.apply.iscut) {
					Dialog.confirm("是否生成缩率图", saveSmallImg, function() { cancelSmallImg(); });
					ias.setOptions({ "movable": false });
				}
			}
			if(num == 4){
				if($.browser.safari && (typeof setTextLayTimer != "undefined")){
					clearInterval(setTextLayTimer);
				}
				waterMarkOk = false;
				if (!appTextOk && ( !! $(".TextBox").length)) {
					Dialog.confirm(tipPressText, applyText, function() { cancelText(); });
				}
				if ( !! $("#imgBox > .TextBox").length) {
					$("#imgBox div").remove(".TextBox");
				}
				appTextOk = false;
				if (!waterMarkOk && ( !! $(".warterImg").length)) {
					Dialog.confirm(tipPressImage, applyWaterMark, function() {
						cancelWaterMark();
					});
				}
				if ( !! $("#imgBox > .warterImg").length) {
					$("#imgBox img").remove(".warterImg");
					$(".waterImgWrap > img.current").removeClass("current");
				}
				
				$("#photo").css("cursor", "default");
				$("div.imgareaselect-outer").css({ "background": "transparent"});
				var ias = $('#photo').data('imgAreaSelect');
				ias.setOptions({
					movable: true,
					resizable: true,
					handles: true
				});
				ias.setOptions({ "enable": true });
				ias.update();
				imgEditState.apply.iscut = false;	
			}
			
		} else {
			if($.browser.safari && (typeof setTextLayTimer != "undefined")){
					clearInterval(setTextLayTimer);
				}
			if (!waterMarkOk && ( !! $(".warterImg").length)) {
				Dialog.confirm(tipPressImage, applyWaterMark, function() {
					cancelWaterMark();
				});
			}
			if (!appTextOk && ( !! $(".TextBox").length)) {
				Dialog.confirm(tipPressText, applyText, function() {
					cancelText();
				});
			}
			//ss.value = 50;
			ss.value = 100;
			$("#photo").attr("style","");
			imgW=$("#photo").width();
			setScale();
			$(".z-slider-thumb").css("left", ss.value + "px");
			if ( !! $("#imgBox > .TextBox").length) {
				$("#imgBox div").remove(".TextBox");
			}
			if ( !! $("#imgBox > .warterImg").length) {
				$("#imgBox img").remove(".warterImg");
				$(".waterImgWrap > img.current").removeClass("current");
			}
			
			$("#photo").css("cursor", "default");
			$("div.imgareaselect-outer").css({ "background": "transparent"});
			$(".imgareaselect-selection").parent().hide();
			$(".imgareaselect-outer").hide();
			var ias = $('#photo').data('imgAreaSelect');
			if (ias.getSelection().width > 0 && !imgEditState.apply.iscut) {
				Dialog.confirm("是否生成缩率图", saveSmallImg, function() {
					cancelSmallImg();
					ImgSelect();
				});
				ias.setOptions({
					"movable": false
				});
			}else{
				ImgSelect(); //切换到裁剪页签页面，缩率图选区宽度不存在时要重现调用裁剪方法	
			}
			ias.setOptions({
				movable: true,
				resizable: true,
				handles: true
			});
			ias.setOptions({ "enable": true });
			ias.update();
			imgEditState.apply.iscut = true;	
		}
	});

	$("#isLockWH").click(function() {
		var isLockWH = $("#isLockWH").attr("checked");
		var lock = true;
		if (isLockWH == "" || isLockWH == null) {
			lock = true;
		}else{
			lock =false;
		}
		var ias = $('#photo').data('imgAreaSelect')
		ias.setOptions({resizable:lock})

	});

	$("#proportion input,#ischeck").click(function() {
		var ischeck = $("#ischeck").attr("checked");
		var aspectRatio = $("#proportion input:checked").val();
		if (ischeck == "" || ischeck == null) {
			ImgArea.aspectRatio = "";
		}
		if (ischeck == "checked" && aspectRatio) {
			ImgArea.aspectRatio = aspectRatio;
		}
		$('#photo').imgAreaSelect({
			aspectRatio: ImgArea.aspectRatio
		});
	});
	$("#Imgwidth,#Imgheight").keyup(function() {
		if ($("#ischeck").attr("checked")) {
			var bi = $NV("bi") + "";
			var arr = bi.split(":");
			if (this.id == "Imgwidth") {
				$S("#Imgheight", parseInt(this.value * arr[1] / arr[0]))
			} else {
				$S("#Imgwidth", parseInt(this.value * arr[0] / arr[1]))
			}
		}
		setSelectblock();
	});
	$("#bold,#Italic,#underline,#Outline").change(function() {
		setOptions();
	});
	
	$("#larger").click(function() {
		if (ss.value >= imgEditState.allowBigZoom[1]) {
			return;
		}
		ss.value = Math.round(ss.value + imgEditState.zoomGap);
		var nowVlaue = ss.value;
		setScale();
		$(".z-slider-thumb").css("left", ss.value + "px")
	});
	$("#narrow").click(function() {
		if (ss.value<=imgEditState.allowBigZoom[0]) {
			return;
		}
		ss.value = Math.round(ss.value - imgEditState.zoomGap);
		var nowVlaue = ss.value;
		setScale();
		$(".z-slider-thumb").css("left", ss.value + "px")
	});
	$("#oldimgWid").click(function() {
		ss.value = 100;
		$("#photo").attr("style",""); //实际大小时去除图片样式定义的宽高
		
		setScale();
		$(".z-slider-thumb").css("left", ss.value + "px")
	});
	
	$("#sImgScale").keyup(function() {
		var aspectRatio=$(this).val();
		sImgArea.aspectRatio = aspectRatio;
		$('#photo').imgAreaSelect({
			aspectRatio: sImgArea.aspectRatio
		});
	});
});
$(window).load(function() {
	if ($("#photo")) {
		imgW = $("#photo").width();
		imgH = $("#photo").height();
		imgEditState.src = $("#photo").attr("src");
		imgEditState.apply.width = imgW;
		imgEditState.apply.height = imgH;
		$("#sliderCon label#scaleImg").text(imgW + " * " + imgH);
		$("#sliderCon label#scale").text("100%")
	}
	ImgSelect();
	$("div.imgareaselect-outer").css({ "background": "transparent"});
	ss = new Slider().render(getDom("sliderContent"));
	ss.value = 100;
	ss.on('slide', function() {
		setScale()
	});
	ss.on('slideclick', function() {
		setScale()
	});
	$(".z-slider-thumb").css("left", "100px");
	var param = {
		right: $G("rotRight"),
		left: $G("rotLeft"),
		flipv: $G("rotFlipv"),
		fliph: $G("rotFliph"),
		img: $G("photo"),
		aglvalue: 0,
		rotation: 0,
		fligx: "",
		fligy: "",
		scalex: 1,
		scaley: 1,
		rot: 0
	};
	var fun = {
		right: function() {
			++imgEditState.angle.clicknum;
			param.rot=imgEditState.angle.imganglevalue==0?0:param.rot;
			param.aglvalue=imgEditState.angle.imganglevalue==0?0:param.aglvalue;
			param.scaley=imgEditState.angle.isflipy == false?1:-1;
			param.scalex=imgEditState.angle.isflipx == false?1:-1;
			if(imgEditState.angle.clicknum ==1){
				param.fligx=imgEditState.angle.isflipx == false?"":"FlipH";
				param.fligy=imgEditState.angle.isflipy == false?"":"Flipv";
			}
			if ((param.fligy == "" && param.fligx != "") || (param.fligx == "" && param.fligy != "")) {
				param.fligy = param.fligy == "" ? "Flipv" : "";
				param.fligx = param.fligx == "" ? "FlipH" : ""
			}
			param.rot += 1;
			if (param.rot === 4) {
				param.rot = 0
			}
			param.aglvalue = 90 * param.rot;
			imgEditState.angle.imganglevalue = "+" + param.aglvalue;
			param.rotation = param.rot;
			imgEditState.angle.imganglevalue=param.rot;
			return false
		},
		left: function() {
			++imgEditState.angle.clicknum;
			param.rot=imgEditState.angle.imganglevalue==0?0:param.rot;
			param.aglvalue=imgEditState.angle.imganglevalue==0?0:param.aglvalue;
			param.scaley=imgEditState.angle.isflipy == false?1:-1;
			param.scalex=imgEditState.angle.isflipx == false?1:-1;
			if(imgEditState.angle.clicknum ==1){
				param.fligx=imgEditState.angle.isflipx == false?"":"FlipH";
				param.fligy=imgEditState.angle.isflipy == false?"":"Flipv";
			}
			if ((param.fligy == "" && param.fligx != "") || (param.fligx == "" && param.fligy != "")) {
				param.fligy = param.fligy == "" ? "Flipv" : "";
				param.fligx = param.fligx == "" ? "FlipH" : ""
			}
			param.rot -= 1;
			if (param.rot === -1) {
				param.rot = 3
			}
			param.aglvalue = 90 * param.rot;
			if (param.aglvalue != 0) {
				imgEditState.angle.imganglevalue = "-" + (360 - param.aglvalue)
			} else {
				imgEditState.angle.imganglevalue = "-" + 0
			}
			param.rotation = param.rot;
			imgEditState.angle.imganglevalue=param.rot;
			return false
		},
		flipv: function() {
			++imgEditState.angle.clicknum;
			param.rot=imgEditState.angle.imganglevalue==0?0:param.rot;
			param.aglvalue=imgEditState.angle.imganglevalue==0?0:param.aglvalue;
			param.scaley=imgEditState.angle.isflipy == false?1:-1;
			param.scalex=imgEditState.angle.isflipx == false?1:-1;
			if(imgEditState.angle.clicknum ==1){
				param.fligx=imgEditState.angle.isflipx == false?"":"FlipH";
				param.fligy=imgEditState.angle.isflipy == false?"":"Flipv";
			}
			param.fligy = param.fligy == "" ? "Flipv" : "";
			if (param.rot == 1 || param.rot == 3) {
				param.scalex = param.scalex == 1 ? -1 : 1;
			} else {
				param.scaley = param.scaley == 1 ? -1 : 1;
			}
			imgEditState.angle.isflipx = param.scalex == -1 ? true : false;
			imgEditState.angle.isflipy = param.scaley == -1 ? true : false;
			return false
		},
		fliph: function() {
			++imgEditState.angle.clicknum;
			param.rot=imgEditState.angle.imganglevalue==0?0:param.rot;
			param.aglvalue=imgEditState.angle.imganglevalue==0?0:param.aglvalue;
			param.scaley=imgEditState.angle.isflipy == false?1:-1;
			param.scalex=imgEditState.angle.isflipx == false?1:-1;
			if(imgEditState.angle.clicknum ==1){
				param.fligx=imgEditState.angle.isflipx == false?"":"FlipH";
				param.fligy=imgEditState.angle.isflipy == false?"":"Flipv";
			}
			param.fligx = param.fligx == "" ? "FlipH" : "";
			if (param.rot == 1 || param.rot == 3) {
				param.scaley = param.scaley == 1 ? -1 : 1;
			} else {
				param.scalex = param.scalex == 1 ? -1 : 1;
			}
			imgEditState.angle.isflipx = param.scalex == -1 ? true : false;
			imgEditState.angle.isflipy = param.scaley == -1 ? true : false;
			return false
		},
		updateImgStyle: function() {
			curstyle = "-moz-transform:rotate(" + param.aglvalue + "deg) scaleX(" + param.scalex + ") scaleY(" + param.scaley + "); -webkit-transform:rotate(" + param.aglvalue + "deg) scaleX(" + param.scalex + ") scaleY(" + param.scaley + "); transform:rotate(" + param.aglvalue + "deg) scaleX(" + param.scalex + ") scaleY(" + param.scaley + "); filter:progid:DXImageTransform.Microsoft.BasicImage(rotation=" + param.rotation + ") " + param.fligx + " " + param.fligy + ";"
			$G("photo").style.cssText = curstyle;

		}
	};
	param.right&&(param.right.onclick = function() {
		fun.right();
		fun.updateImgStyle();
	});
	param.left&&(param.left.onclick = function() {
		fun.left();
		fun.updateImgStyle();
	});
	param.fliph&&(param.fliph.onclick = function() {
		fun.fliph();
		fun.updateImgStyle();
	});
	param.flipv&&(param.flipv.onclick = function() {
		fun.flipv();
		fun.updateImgStyle();
	})
});

function setScale() {
	var v = (Math.round(ss.value)) / 100;
	$('#photo').attr("style","width:"+Math.round(imgW * v )+ "px");
	$("#sliderCon label#scale").text(Math.round(v * 100) + "%");
	var imgWid = $('#photo').width();
	var imgHid = $('#photo').height();
	$("#sliderCon label#scaleImg").text(imgWid + " * " + imgHid);
	
	var ias = $('#photo').data('imgAreaSelect');
	ias.setOptions({
		imageHeight: $('#photo').height(),
		imageWidth: $('#photo').width(),
		maxHeight: $('#photo').height(),
		maxWidth: $('#photo').width()
	});
	ias.update();
	ImgArea.maxW = $('#photo').width();
	ImgArea.maxH = $('#photo').height();
	imgEditState.scale.x = v;
	imgEditState.scale.y = v;
}
function cancel() {
	ImgSelect();
	deselect();
}
function cancelSmallImg(){
	ImgSelect("getSimg");
	deselect();
}
function deselect(){
	$("div.imgareaselect-outer").css({ "background": "transparent"});
	$($("div.imgareaselect-outer")[0]).prev().hide();
}
function cancelWaterMark() {
	if ( !! $("#imgBox > .warterImg").length) {
		$("#imgBox img").remove(".warterImg")
	}
	if ($(".waterImgWrap > img.current").length > 0) {
		$(".waterImgWrap > img.current").removeClass("current")
	}
}
function cancelText() {
	var curnum=$("#menus  a.current").parent().prevAll().length;
	if ( !! $("#imgBox > .TextBox").length) {
			$("#imgBox div").remove(".TextBox")
	}
	if(curnum == 3){
		$("#photo").css("cursor", "text");
		docClick();
	}
}
function cancelRotate() {
	$("#photo").attr("style", "")
	imgEditState.angle.imganglevalue = 0;
	imgEditState.angle.isflipx = false;
	imgEditState.angle.isflipy = false;
	imgEditState.angle.clicknum = 0;
}