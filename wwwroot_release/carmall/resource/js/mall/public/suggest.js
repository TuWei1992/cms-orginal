/**
 * Copyright (C), 2015, 上海赛可电子商务有限公司
 * Author:   康明飞
 * Date:     2015-5-4
 * Description: 下拉联想功能
 */
(function () {
    var Class = {
        create: function () {
            return function () {
                this.initialize.apply(this, arguments)
            }
        }
    };
    Object.extend = function (destination, source) {
        for (property in source) {
            destination[property] = source[property]
        }
        return destination
    };
    var Base = Class.create();
    Object.extend(Function.prototype, {
        bind: function () {
            var __m = this,
                object = arguments[0],
                args = new Array();
            for (var i = 1; i < arguments.length; i++) {
                args.push(arguments[i])
            }
            return function () {
                return __m.apply(object, args)
            }
        }
    });
    Object.extend(Base.prototype, {
        initialize: function () {},
        Browser: {
            IE: !! (window.attachEvent && navigator.userAgent.indexOf('Opera') === -1),
            Opera: navigator.userAgent.indexOf('Opera') > -1,
            WebKit: navigator.userAgent.indexOf('AppleWebKit/') > -1,
            Gecko: navigator.userAgent.indexOf('Gecko') > -1 && navigator.userAgent.indexOf('KHTML') === -1,
            MobileSafari: !! navigator.userAgent.match(/Apple.*Mobile.*Safari/)
        },
        $: function (o) {
            return typeof(o) == 'string' ? document.getElementById(o) : o
        },
        $Tag: function (o) {
            return typeof(o) == 'string' ? document.getElementsByTagName(o) : o
        },
        $C: function (o) {
            return document.createElement(o)
        },
        $E: function (e) {
            tempObj = e.target ? e.target : event.srcElement;
            return tempObj
        },
        $aE: function (elm, evType, fn, useCapture) {
            if (elm.addEventListener) {
                elm.addEventListener(evType, fn, useCapture);
                return true
            } else if (elm.attachEvent) {
                var r = elm.attachEvent('on' + evType, fn);
                return r
            } else {
                elm['on' + evType] = fn
            }
        },
        $dE: function (elm, evType, fn, useCapture) {
            if (elm.removeEventListener) {
                elm.removeEventListener(evType, fn, useCapture);
                return true
            } else if (elm.detachEvent) {
                var r = elm.detachEvent('on' + evType, fn);
                return r
            } else {
                elm['on' + evType] = null;
                return
            }
        },
        stopBubble: function (e) {
            if (!this.Browser.IE) {
                e.stopPropagation()
            } else {
                window.event.cancelBubble = true
            }
        },
        stopDefault: function (e) {
            if (!this.Browser.IE) {
                e.preventDefault()
            } else {
                window.event.returnValue = false
            }
        },
        isNullorEmpty: function (obj) {
            if (obj == null || obj == "" || obj == "undefined") {
                return true
            }
            return false
        },
        getXY: function (obj) {
            var curleft = 0;
            var curtop = 0;
            var border;
            if (obj.offsetParent) {
                do {
                    curleft += obj.offsetLeft;
                    curtop += obj.offsetTop;
                    if (this.getStyle(obj, 'position') == 'relative') {
                        if (border = this.getStyle(obj, 'border-top-width')) curtop += parseInt(border);
                        if (border = this.getStyle(obj, 'border-left-width')) curleft += parseInt(border);
                        break
                    }
                } while (obj = obj.offsetParent)
            } else if (obj.x) {
                curleft += obj.x;
                curtop += obj.y
            }
            return {
                'x': curleft,
                'y': curtop
            }
        },
        getStyle: function (obj, styleProp) {
            if (obj.currentStyle) return obj.currentStyle[styleProp];
            else if (window.getComputedStyle) return document.defaultView.getComputedStyle(obj, null).getPropertyValue(styleProp)
        }
    });
    var Gee = new Base();
    var MallSuggest = Class.create();
    Object.extend(MallSuggest.prototype, {
        initialize: function (obj, arg) {
            this.input = obj;
            this.dataurl = Gee.isNullorEmpty(arg.dataurl) ? "" : arg.dataurl;
            if (!Gee.isNullorEmpty(arg.dataurl)) this.dataurl = arg.dataurl;
            this.link = Gee.isNullorEmpty(arg.link) ? "" : arg.link;
            this.opacity = Gee.isNullorEmpty(arg.opacity) ? 1 : arg.opacity;
            this.className = Gee.isNullorEmpty(arg.className) ? "" : arg.className;
            this.max = Gee.isNullorEmpty(arg.max) ? 10 : arg.max;
            this.text = Gee.isNullorEmpty(arg.text) ? "请输入搜索内容" : arg.text;
            this.body = Gee.isNullorEmpty(arg.body) ? [0] : arg.body;
            this.target = Gee.isNullorEmpty(arg.target) ? "_blank" : arg.target;
            this.hotSearchList = Gee.isNullorEmpty(arg.hotSearchList) ? [] : arg.hotSearchList;
            this.callback = (arg.callback == null || arg.callback == "undefined") ? null : arg.callback;
            this.results = null;
            this._D = null;
            this._F = null;
            //request count
            this._requestCount=0;
            this._R = null;
            this._W = null;
            this._X = {};
            this._Y = {};
            this._hidden = false;
            this._iF = null;
            this._iN = null;
            this._iC = null;
            this._oForm = null;
            this._rInterval = -1;
            this.init()
        },
        init: function () {
            this._Y = {
                "key_" : this.hotSearchList
            };
            this.input = typeof(this.input) == "string" ? Gee.$(this.input) : this.input;
            if (this.input) {
                if (this._F == null) {
                    var FormNode = this.input.parentNode;
                    while (FormNode.nodeName.toLowerCase() == "form" && FormNode.nodeName.toLowerCase() == "body") {
                        FormNode = FormNode.parentNode
                    }
                    if (FormNode.nodeName.toLowerCase() == "form") {
                        this._oForm = {
                            action: FormNode.action,
                            target: FormNode.target,
                            method: FormNode.method
                        };
                        this._F = FormNode;
                    } else {
                        this._F = Gee.$C("form");
                        this._F.method = "get";
                        this.input.parentNode.insertBefore(this._F, this.input);
                        var _i = this.input;
                        this.input.parentNode.removeChild(this.input);
                        this._F.appendChild(_i)
                    }
                }
                this._F.input = this.input;
                this._F.onsubmit = function () {
                    if(this.input.value == this.input.getAttribute("placeholder")){
                        this.input.value="";
                    }
                    return true;
                };
                this._F.style.position = "relative";
                this._F.style.display = "block";
                this._F.target = this.target;
                this._F.style.zIndex = "19";
                this._F.style.width = "100%";
                this._F.style.height = "100%";
                this.input.setAttribute("autocomplete", "off");
                this.input.autoComplete = "off";
                this._iF = this._bd(this.inputFocus);
                this._iN = this._bd(this.Navigate);
                this._iC = this._bd(this.Confirm);
                Gee.$aE(this.input, "focus", this._iF);
                Gee.$aE(this.input, "blur", this._iF);
                Gee.$aE(this.input, "keyup", this._iN);
                Gee.$aE(this.input, "paste", this._iF);
                Gee.$aE(this.input, "keydown", this._iC);
                Gee.$aE(this.input, "mouseup", this._iN);
            }
        },
        dispose: function () {
            this._Y = {};
            this.input = typeof(this.input) == "string" ? Gee.$(this.input) : this.input;
            if (this.input) {
                if (this._oForm != null) {
                    this._F.action = this._oForm.action;
                    this._F.target = this._oForm.target;
                    this._F.method = this._oForm.method;
                }
                Gee.$dE(this.input, "focus", this._iF);
                Gee.$dE(this.input, "blur", this._iF);
                Gee.$dE(this.input, "keyup", this._iN);
                Gee.$dE(this.input, "paste", this._iF);
                Gee.$dE(this.input, "keydown", this._iC);
                Gee.$dE(this.input, "mouseup", this._iN)
            }
        },
        inputFocus: function (e) {
            var self=this;
            var _t = e.type;
            //不这样paste会有问题
            setTimeout(function(){
                if (self.input.value == self.text && _t.indexOf("focus") >= 0) {
                    self.input.value = "";
                    self._U = "";
                    self.Suggest()
                } else if (self.input.value == "" && _t.indexOf("blur") >= 0) {
                    self._U = "";
                    self.hiddenResults()
                } else if (_t.indexOf("blur") >= 0) {
                    self.hiddenResults()
                }else if (_t.indexOf("paste") >= 0) {
                    self.Suggest()
                }
            },0);
        },
        nGourl: false,
        Navigate: function (e) {
            switch (e.keyCode) {
            case 13:
            	this.nGourl = true;
                this.Submit()
                break;
            default:
                this.Suggest();
                break
            }
        },
        Confirm: function (e) {
            switch (e.keyCode) {
                case 38:
            		//可以防止鼠标晃动
                    Gee.stopDefault(e);
                    this.nGourl = false;
                    if (this.results != null && this.results.innerHTML != "") {
                        this.setLine(this.results.firstChild.rows[(!this._W || this._W.rowIndex == 1) ? this.results.firstChild.rows.length - 1 : this._W.rowIndex - 1])
                    }
                    break;
                case 40:
                    this.nGourl = false;
                    if (this.results != null && this.results.innerHTML != "") {
                        this.setLine(this.results.firstChild.rows[(!this._W || this._W.rowIndex == this.results.firstChild.rows.length - 1) ? 1 : this._W.rowIndex + 1])                      
                    }
                    break;
            }
        },
        _bd: function (_b, _c) {
            var _d = this;
            return function () {
                var _e = null;
                if (typeof _c != "undefined") {
                    for (var i = 0; i < arguments.length; i++) {
                        _c.push(arguments[i])
                    }
                    _e = _c
                } else {
                    _e = arguments
                }
                return _b.apply(_d, _e)
            }
        },
        _gt: function () {
            return (new Date()).getTime()
        },
        Suggest: function () {
            var _s = this.input.value;
            if (this._U != _s) {
                this._U = _s;
                if (_s != "") {
                    this._W && (this._W = null);
                    if (("key_" + _s) in this._Y) {
                        this.Tip();
                    } else {
                    	this.Tip();
                        this._io(_s, this._bd(this.Tip), this._bd(this.hiddenResults));
                    }
                } else {
                    if (this.results != null && this.results.innerHTML != "") {
                        this._W = null;
                    }
                    this.Tip();
                    //this.hiddenResults()
                }
            } else {
                if(this.input.value==""){
                    this.Tip();
                }
                this.setResults()
            }
        },
        setResults: function () {
            if (this.results != null && this.results.innerHTML != "") this.results.style.display = ""
        },
        hiddenResults: function () {
            if (this._hidden == false) {
                if (this.results != null) {
                    if(this.input.value==""){
                        this.results.innerHTML = ""
                    }
                    this.results.style.display = "none"
                }
            }
        },
        _io: function (s, _E, _F) {
            if (this._R == null) {
                this._R = Gee.$C("div");
                this._R.style.display = "none";
                document.body.insertBefore(this._R, document.body.lastChild)
            }
            this._requestCount++;
            var jsonpName = "jsonp" + this._gt();
            var _H = Gee.$C("script");
            _H._count = this._requestCount;
            _H.type = "text/javascript";
            _H.charset = "utf-8";
            _H.src = this.dataurl.replace("{#NAME}", jsonpName).replace("{#KEY}", s);
            _H._0j = this;
            if (_E) {
                _H._0k = _E
            }
            if (_F) {
                _H._0l = _F
            }
            _H._0m = s;
            _H._0n = jsonpName;
            window[_H._0n] = function(data) {
                var _I = data;
                if (typeof _I != "undefined") {
                    _H._0j._Y["key_" + _H._0m] = _I;
                    //确保最后一次的填充dom的操作不会被之前冲掉
                    if(_H._count == _H._0j._requestCount){
                        _H._0k(_I);
                    }
                }
                window[_H._0n] = null;
            };
            _H[document.all ? "onreadystatechange" : "onload"] = function () {
                if (document.all && this.readyState != "loaded" && this.readyState != "complete") {
                    return;
                }
                this._0j = null;
                this._0m = null;
                this._0n = null;
                this[document.all ? "onreadystatechange" : "onload"] = null;
                this.parentNode.removeChild(this)
            };
            this._R.request = _H;
            this._R.appendChild(_H)
        },
        Submit: function () {
            var _s = this.input.value;
            this._F.submit();
            this.hiddenResults();
        },
        setColor: function (o) {
            var _B = "";
            if (o._0f && o._0g) {
                //鼠标hover+键盘选中
                _B = "#f2f2f2"
            } else if (o._0f) {
                //键盘移动颜色
                _B = "#f2f2f2"
            } else if (o._0g) {
                //鼠标hover
                _B = "#f2f2f2"
            }
            if (o.style.backgroundColor != _B) {
                o.style.backgroundColor = _B
            }
        },
        setLine: function (o, e) {
            var _C = o.id.split(",");
            this._D = _C;
            var _D = _C[0];
            this._U = _D;
            this.input.value = _D;
            if (this._W != null) {
                this._W._0f = false;
                this.setColor(this._W)
            }
            o._0f = true;
            this.setColor(o);
            this._W = o;
            if (this.nGourl) this.Submit()
        },
        mouseoverLine: function (o) {
            o._0g = true;
            this.setColor(o)
        },
        mouseoutLine: function (o) {
            o._0g = false;
            this.setColor(o)
        },
        setLineMouse: function (o) {
            this.nGourl = true;
            this.setLine(o);
            if (this.callback != null) {
                this.callback({
                    code: this.input.value
                })
            }
        },
        hidepause: function () {
            this._hidden = true
        },
        hideresume: function () {
            this._hidden = false;
            this.hiddenResults()
        },
        setTip: function () {
            var _j = 0;
            var _k = 0;
            var _f = this.input;
            do {
                _j += _f.offsetTop || 0;
                _k += _f.offsetLeft || 0;
                _f = _f.offsetParent
            } while (_f);
            this.results.style.left = "0";
            this.results.style.top = "0";
            var _p = this.input.style.borderTopWidth;
            var _q = this.input.style.borderBottomWidth;
            var _r = this.input.clientHeight;
            _r += _p != "" ? _p.replace("px", "") * 1 : 1;
            _r += _q != "" ? _q.replace("px", "") * 1 : 1;
            if (this.results.style.marginTop != _r + "px") {
                this.results.style.marginTop = _r + "px"
            }
            var _s = this.input.clientWidth;
            this.results.style.width = _s + "px";
        },
        Tip: function () {
            var _s = this.input.value;
            this.results && (this.results.innerHTML = "");
            if (("key_" + _s) in this._Y && this._Y["key_" + _s] && this._Y["key_" + _s].length>0) {
                if (this.results == null) {
                    this.results = Gee.$C("div");
                    this.results.id = "kk-suggest-result";
                    this.results.style.zIndex = "19";
                    // this.results.style.width = this.width + "px";
                    // this.results.style.opacity = this.opacity;
                    // this.results.style.filter = "alpha(opacity:" + (this.opacity * 100) + ")";
                    this.results.style.position = "absolute";
                    this.results.style.display = "none";
                    this.results.style.background = "#000";
                    if (this.className == "") this.results.style.border = "1px solid #CCC";
                    else this.results.className = this.className;
                    this.input.parentNode.insertBefore(this.results, this.input);
                    this.results["suggest"] = this
                }
                this.setTip();
                var t = Gee.$C("table");
                t.border = "0";
                t.cellPadding = "0";
                t.cellSpacing = "0";
                t.style.lineHeight = "25px";
                t.style.border = "1px solid #FFF";
                t.style.background = "#FFF";
                t.style.fontSize = "12px";
                t.style.textAlign = "left";
                t.style.textIndent = "5px";
                t.style.color = "#666";
                t.style.width = "100%";
                t.style.position= "relative";
                var tH = Gee.$C("thead");
                var _th_tr = Gee.$C("tr");
                var _th_td = Gee.$C("td");
                _th_td.hidefocus = "true";
                // _th_td.style.fontSize="1.25em";
                    _th_td.style.padding = "1px";
                if(_s==""){
                    _th_td.innerHTML = "<b>热门搜索</b>";
                }else{                    
                    _th_td.innerHTML = "<b>搜索结果</b>";
                    _th_tr.style.display="none";
                }
                _th_tr.appendChild(_th_td);
                tH.appendChild(_th_tr)
                t.appendChild(tH);
                var tB = Gee.$C("tbody");
                this.results.body = tB;
                var _u = this._Y["key_" + _s];
                var _v = _u.length > this.max ? this.max : _u.length;
                for (var i = 0; i < _v; i++) {
                    var _x = _u[i];
                    var _t_tr = Gee.$C("tr");
                    _t_tr.id = _x.kw;
                    _t_tr.style.cursor = "pointer";
                    _t_tr._oj = this;
                    _t_tr.onmouseover = function () {
                        this._oj.mouseoverLine(this)
                    };
                    _t_tr.onmouseout = function () {
                        this._oj.mouseoutLine(this)
                    };
                    _t_tr.onmousedown = function () {
                        return this._oj.hidepause(this)
                    };
                    _t_tr.onclick = function () {
                        this._oj.setLineMouse(this);
                        this._oj.hideresume(this)
                    };
                    for (var j = 0; j < this.body.length; j++) {
                        var _t_td = Gee.$C("td");
                        _t_td.hidefocus = "true";
                        _t_td.style.padding = "1px";
                        var _key=_x[this.body[j]];
                        if(j==0&&_key.indexOf(_s)==0){
                            _key = _key.replace(_s, '<span style="color:#FFC107;">' + _s + '</span>');
                        }
                        _t_td.innerHTML = _key;
                        _t_tr.appendChild(_t_td)
                    }
                    tB.appendChild(_t_tr)
                }
                t.appendChild(tB);
                this.results.appendChild(t);
                this.setResults()
            } else {
                this.hiddenResults()
            }
        }
    });
    window.MallSuggest = MallSuggest
})();