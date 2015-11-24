$().ready(function() {
	var index = new CarServiceIndex;
});

var Class = {
    create: function () {
        return function () {
            this.initialize.apply(this, arguments);
        };
    }
};

var controller = "/carservice";
/** 车服务url */
var ajaxUrl = {
	getProvince : mallBase + controller + "/provinceList.htm",
	getCity : mallBase + controller + "/cityListByProv.htm",
	queryPeccancy : mallBase + controller + "/queryPeccancy.htm",
	getProvinceShort : mallBase + controller + "/provinceListShort.htm",
	validatePeccancy : mallBase + controller + "/validatePeccancy.htm"
};

var CarServiceIndex = Class.create();
/** 车服务首页 */
CarServiceIndex.prototype = {
	initialize : function() {
		var that = this;
		var index = 1,hoving = false;
      	$(".engineNo").hide();
		$("#fadongjihao").hide();
		$(".warn").hide();
      	$(".frameNo").show();
		$("#chejiahao").show();
		
		/**
		
		// 页面banner事件
		$('.img-tool > span').on('click', function(e) {
			var target = $(e.currentTarget);
			$(".slide").fadeOut();
			$(".slide[data-index=" + target.data("index") + "]").fadeIn();
			$(".img-tool > span").removeClass("cur-img");
			target.addClass("cur-img");
			index = target.data("index");
		});
		// 自动轮播
		var inter = setInterval(function() {
            if( !hoving ) {
                index++;
                if (index > $(".slide").length) {
                    index = 1;
                }
                $(".img-tool>span[data-index=" + index + "]").trigger("click");
            }
		}, 4000);

        $("#fullscreen").on("mouseover" , function () {
            hoving = true;
        })
        $("#fullscreen").on("mouseout" , function () {
            hoving = false;
        })*/
		// $("form").delegate("input,select", "blur", function() {
		// that.addBlurEvent($(this));
		// });
		// 获取省市信息

		$("#illegalSearchBtn").on("click", function() {
			// $("form input").trigger("blur");
			// $("form select").trigger("blur");
			if (that.validate()) {
				that.searchIllegal(ajaxUrl.validatePeccancy);
			} 
          return false;
		});
		$("#carNo").on("blur", function() {
			$("#carNo").val($(this).val().toUpperCase());
		});
		$("#province").on("change", function() {
			$(".warn").empty();
			var provCode = $("#province").val();
			var option = "<option value=\"\">请选择市</option>";
			if ("" == provCode || null == provCode) {
				$("#city").empty().append(option);
				return;
			}
			// $("input[name='provinceName']").val($("#province").find('option:selected').text());
			var dataParam = "provCode=" + $("#province").val();
			that.getCity(ajaxUrl.getCity, function(msg) {
				that.genErrorHtml(msg);
			}, dataParam, null, null);
		});
		$("#city").on("change", function() {
			if (null == $("#city").val() || "" == $("#city").val()) {
				return;
			}
			$(".warn").empty();
			$("#frameNo").val("");
			$("#engineNo").val("");
			// $("input[name='cityName']").val($("#city").find('option:selected').text());
			var dataParam = "provCode=" + $("#province").val() + "&cityCode=" + $("#city").val();
			that.queryPeccancy(ajaxUrl.queryPeccancy, dataParam, function(msg) {
				that.genErrorHtml(msg);
			}, null);
		});
		// 提交表单，如果出现错误，回显错误消息
		var errMsg = $("#errMsg").val();
		if (errMsg != "" && errMsg != null) {
			that.genErrorHtml(errMsg);
			$("#errMsg").val("");
		} else {
			$(".warn").empty();
			$(".warn").hide();
		}
		// 提交表单，如果错误返回，有省市信息则回显省市
		var provinceValue = $("#province").attr("attr-value");
		var cityValue = $("#city").attr("attr-value");
		if (null != provinceValue && "" != provinceValue) {
			that.getProvince(ajaxUrl.getProvince, function(msg) {
				that.genErrorHtml(msg);
			}, provinceValue, function() {
				if (null != cityValue && "" != cityValue) {
					var dataParam = "provCode=" + provinceValue;
					var param = dataParam + "&cityCode=" + cityValue;
					that.getCity(ajaxUrl.getCity, function(msg) {
						that.genErrorHtml(msg);
					}, dataParam, cityValue, function() {
						that.queryPeccancy(ajaxUrl.queryPeccancy, param, function(msg) {
							that.genErrorHtml(msg);
						}, null);
					});
				} else {
					if (null != provinceValue && "" != provinceValue) {
						var dataParam = "provCode=" + provinceValue;
						that.getCity(ajaxUrl.getCity, function(msg) {
							that.genErrorHtml(msg);
						}, dataParam, null, null);
					}
				}
			},function(){
				var dataParam = "provCode=" + "SH";
				var param = dataParam + "&cityCode=" + "SH";
				that.getCity(ajaxUrl.getCity, function(msg) {
					that.genErrorHtml(msg);
				}, dataParam, "SH", function() {
					that.queryPeccancy(ajaxUrl.queryPeccancy, param, function(msg) {
						that.genErrorHtml(msg);
					}, null);
				});
				
			});
		} else {
			that.getProvince(ajaxUrl.getProvince, function(msg) {
				that.genErrorHtml(msg);
			}, null, null,null);
		}
		// ie placeholder 失效
		if ($.browser.msie) {
			$(":input[placeholder]").each(function() {
				$(this).placeholder();
			});
		}
		if ($.browser.msie && ($.browser.version == "7.0")) {
			$(":input[placeholder]").each(function() {
				$(this).placeholder();
			});
		}
	},
	searchIllegal : function(baseUrl) {
		$("#illegalSearchBtn").attr("disabled","disabled");
		// var dataParam = $("#illegalForm").serializeObject();
		// dataParam = JSON.stringify(dataParam);
		var dataParam = $("#province").val() + "|" + $("#city").val() + "|" + $("#carBelong").val() + "|"
				+ $("#carNo").val() + "|" + $("#engineNo").val() + "|" + $("#frameNo").val();
		$("#illegalData").val(dataParam);
		$("#illegalForm").attr("action", baseUrl);
		$("#illegalForm").submit();
		$("#illegalSearchBtn").removeAttr("disabled");
		//releasePage();
	},
	validate : function() {
		var provCode = $("#province").val();
		if (provCode == null || provCode == "") {
			this.genErrorHtml("请选择省份");
			return false;
		}
		var cityCode = $("#city").val();
		if (cityCode == null || cityCode == "") {
			this.genErrorHtml("请选择城市");
			return false;
		}
		var carBelong = $("#carBelong");
		var carBelongVal = $("#carBelong").val();
		var carBelongRequired = carBelong.attr("data-required");
		if ("required" == carBelongRequired) {
			if ("" == carBelongVal || null == carBelongVal) {
				this.genErrorHtml("请选择车牌归属地");
				return false;
			} else {
			}

		}
		var carNo = $("#carNo");
		var carNoVal = $("#carNo").val();
		var carNoRequired = carNo.attr("data-required");
		if ("required" == carNoRequired) {
			if ("" == carNoVal || null == carNoVal) {
				this.genErrorHtml("请输入车牌号码");
				return false;
			} else {
				if (!this.isNumAndLetter(carNoVal)) {
					this.genErrorHtml("请输入正确的车牌号码");
					return false;
				} else {
					if (carNoVal.length != 6) {
						this.genErrorHtml("请输入正确的车牌号码");
						return false;
					}
				}
			}

		}
		if (!this.isNumAndLetter(carNoVal)) {
			this.genErrorHtml("请输入正确的车牌号码");
			return false;
		}
		var engineNo = $("#engineNo");
		var engineNoVal = $("#engineNo").val();
		var engineNoRequired = engineNo.attr("data-required");
		if ("required" == engineNoRequired) {
			if ("" == engineNoVal || null == engineNoVal) {
				this.genErrorHtml("请输入发动机号");
				return false;
			} else {
				var num = engineNo.attr("num");
				var length = engineNoVal.length;
				if (length != num && num != 0) {
					this.genErrorHtml("输入的发动机号必须为后" + num + "位");
					return false;
				} else if (num == 0) {
					// 需要输入全部的发动机号，不能超过20位
					if (length > 20) {
						this.genErrorHtml("请输入正确的发动机号");
						return false;
					} else {

					}
				}

			}

		}
		if (!this.isEngineeNo(engineNoVal)) {
			this.genErrorHtml("请输入正确的发动机号");
			return false;
		}
		var frameNo = $("#frameNo");
		var frameNoVal = $("#frameNo").val();
		var frameNoRequired = frameNo.attr("data-required");
		if ("required" == frameNoRequired) {
			if ("" == frameNoVal || null == frameNoVal) {
				this.genErrorHtml("请输入车架号");
				return false;
			} else {
				var num = frameNo.attr("num");
				var length = frameNoVal.length;
				if (length != num && num != 0) {
					this.genErrorHtml("输入的车架号必须为后" + num + "位");
					return false;
				} else if (num == 0) {
					// 需要输入全部的车架号,不能超过17位
					if (length > 17) {
						this.genErrorHtml("请输入正确的车架号");
						return false;
					} else {

					}

				}
			}
		}
		if (!this.isNumAndLetter(frameNoVal)) {
			this.genErrorHtml("请输入正确的车架号");
			return false;
		}
		return true;
	},
	isNumAndLetter : function(str) {
		var reg = /^[0-9a-zA-Z]*$/g;
		return reg.test(str);
	},
	isEngineeNo : function(str) {
		var reg = /^[0-9a-zA-Z]*-*[0-9a-zA-Z]*$/g;
		return reg.test(str);
	},
	// 验证字符串是否含有中文
	isChineseChar : function(str) {
		var reg = /w?[\u4E00-\u9FA5]+w?/;
		var flag = reg.test(str);
		return flag;
	},
	// 生成错误信息页面片段
	genErrorHtml : function(msg) {
		$(".warn").show();
		$(".warn").empty();
		var divHtml = "<font style=\"color:red;width:100%; height:20px; line-height:20px; text-align:center; display:block;\">"
				+ msg + "</font>";
		$(".warn").append(divHtml);
	},
	// 获取所有省的信息
	getProvince : function(baseUrl, callback, provinceValue, loadCity,loadSHCity) {
		$.ajax({
			type : "POST",
			url : baseUrl,
			dataType : "json",
			success : function(response) {
				var option = "<option value=\"\">请选择省</option>";
				var flag =true;//true代表省下架了 false代表正常
				if (response.status == "01") {
					var list = response.result;
					if (list != null) {
						for (index in list) {
							if (provinceValue == list[index].provinceCode) {
								flag =false;
								break;
							}
						}
						if(provinceValue==null || provinceValue==""){
							flag = false;
						}
						if(flag){
							//省下架后默认到上海市
							for (index in list) {
								if (list[index].provinceCode == "SH") {
									option += "<option value=\"" + list[index].provinceCode + "\" selected=\"selected\">"
											+ list[index].provinceName + "</option>";
								} else {
									option += "<option value=\"" + list[index].provinceCode + "\">"
											+ list[index].provinceName + "</option>";
								}

							}
							
						}else{
							for (index in list) {
								if (provinceValue == list[index].provinceCode) {
									option += "<option value=\"" + list[index].provinceCode + "\" selected=\"selected\">"
											+ list[index].provinceName + "</option>";
								} else {
									option += "<option value=\"" + list[index].provinceCode + "\">"
											+ list[index].provinceName + "</option>";
								}

							}
						}
						
					}

				} else {
					callback(response.error[0].message);
				}
				if (option != "") {
					$("#province").empty().append(option);
				}
				$("#city").empty().append("<option value=\"\">请选择市</option>");
				if(flag){
					if(loadSHCity!=null){
						loadSHCity();
					}
				}else{
					if (loadCity != null) {
						loadCity();
					}
				}
				

			}
		});
	},
	// 根据省获取市
	getCity : function(baseUrl, callback, dataParam, cityValue, searchPenccanyRule) {
		$.ajax({
			type : "POST",
			url : baseUrl,
			data : dataParam,
			dataType : "json",
			success : function(response) {
				var option = "<option value=\"\">请选择市</option>";
				if (response.status == "01") {
					var list = response.result;
					if (list != null) {
						for (index in list) {
							if (cityValue == list[index].cityCode) {
								option += "<option value=\"" + list[index].cityCode + "\" selected=\"selected\">"
										+ list[index].cityName + "</option>";
							} else {
								option += "<option value=\"" + list[index].cityCode + "\">" + list[index].cityName
										+ "</option>";
							}

						}
					}

				} else {
					callback(response.error[0].message);
				}
				if (option != "") {
					$("#city").empty().append(option);
					if (searchPenccanyRule != null) {
						searchPenccanyRule();
					}
				}

			}
		});
	},
	// 根据省市信息查询规则
	queryPeccancy : function(baseUrl, dataParam, callback) {
		$.ajax({
			type : "POST",
			url : baseUrl,
			data : dataParam,
			dataType : "json",
			success : function(response) {
				if (response.status == "01") {
					var entity = response.result;
					if (entity != null) {
						if (entity.isEngine == '0') {
							// 不需要发动机号
							$("#engineNo").removeAttr("data-required");
							$("#engineNo").val("");
							$(".engineNo").hide();
							$("#fadongjihao").hide();
						} else if (entity.isEngine == '1') {
							$(".engineNo").show();
							$("#fadongjihao").show();
							// 需要发动机号
							$("#engineNo").attr("data-required", "required");
							// 需要几位发动机号
							$("#engineNo").attr("num", entity.engineNo);
							if (entity.engineNo != 0) {
								$("#engineNo").attr("placeholder", "请输入后" + entity.engineNo + "位发动机号");
							} else {
								$("#engineNo").attr("placeholder", "请输入全部发动机号");
							}
							if ($.browser.msie) {
								$(".engineNo>.test").remove();
								$("#engineNo").placeholder();
							}

						} else {
							$(".engineNo").hide();
							$("#fadongjihao").hide();
							$("#engineNo").val("");
						}
						if (entity.isFrame == '0') {
							// 不需要车架号
							$("#frameNo").removeAttr("data-required");
							$(".frameNo").hide();
							$("#chejiahao").hide();
							$("#frameNo").val("");
						} else if (entity.isFrame == '1') {
							// 需要车架号
							$(".frameNo").show();
							$("#chejiahao").show();
							$("#frameNo").attr("data-required", "required");
							// 需要几位车架号
							$("#frameNo").attr("num", entity.frameNo);
							if (entity.frameNo != 0) {
								var str = "请输入后" + entity.frameNo + "位车架号";
								$("#frameNo").attr("placeholder", str);

							} else {
								var str = "请输入全部车架号";
								$("#frameNo").attr("placeholder", str);
							}
							if ($.browser.msie) {
								$(".frameNo>.test").remove();
								$("#frameNo").placeholder();
							}
						} else {
							$(".frameNo").hide();
							$("#frameNo").val("");
							$("#chejiahao").hide();
						}
					}
				} else {
					callback(response.error[0].message);
				}

			}
		});
	},
	// 为页面表单项新增blur事件
	addBlurEvent : function(target) {
		var required = target.attr("data-required");
		var value = target.val();
		var hidden = target.type;
		if (hidden == "hidden") {
			return;
		}
		if (null == required && "" == required) {
			target.attr("style", "");
		} else if ("required" == required) {
			if (null == value || "" == value) {
				target.attr("style", "background-color:red");
				this.genErrorHtml("请输入必填项");
			} else {
				target.attr("style", "");
				$(".warn").empty();
				$(".warn").hide();
			}
		}
	}

};