/*
 * Copyright (C), 2015, 上海赛可电子商务有限公司
 * Author:   康明飞
 * Date:     2015-5-8
 * Description: 轻量级的PlaceHolder组件
 * evtType默认是focus，即鼠标点击到输入域时默认文本消失，keydown则模拟HTML5 placeholder属性在Firefox/Chrome里的特征，光标定位到输入域后键盘输入时默认文本才消失。
 * 此外，对于HTML5 placeholder属性，IE10+和Firefox/Chrome/Safari的表现形式也不一致，因此内部实现不采用原生placeholder属性
 */
(function($){    
    $.fn.placeholder = function(option, callback) {
        var settings = $.extend({
            color: '#cecece',
            evtType: 'focus'
        }, option)
     
        function bootstrap($that) {
            // some alias
            var word    = $that.attr("placeholder");
            // update 2015/5/8
            if(word=="") {
                return;
            }
            var color   = settings.color;
            var evtType = settings.evtType;
     
            // default
            var defColor = $that.css('color');
            var defVal   = $that.val();
     
            if (defVal == '' || defVal == word) {
                $that.css({color: color}).val(word);
            } else {
                $that.css({color: defColor});
            }
     
            function switchStatus(isDef) {
                if (isDef) {
                    $that.val('').css({color: defColor}) ;  
                } else {
                    $that.val(word).css({color: color});
                }
            }
            function asFocus() {
                $that.bind(evtType, function() {
                    var txt = $that.val();
                    if (txt == word) {
                        switchStatus(true);
                    }
                }).bind('blur', function() {
                    var txt = $that.val();
                    if (txt == '') {
                        switchStatus(false);
                    }
                })
            }
            function asKeydown() {
                $that.bind('focus', function() {
                    var elem = $that[0];
                    var val  = $that.val();
                    if (val == word) {
                        setTimeout(function() {
                            // 光标定位到首位
                            $that.setCursorPosition({index: 0});
                        }, 10)                 
                    }
                })
            }
     
            if (evtType == 'focus') {
                asFocus();
            } else if (evtType == 'keydown') {
                asKeydown();
            }
     
            // keydown事件里处理placeholder
            $that.keydown(function() {
                var val = $that.val();
                if (val == word) {
                    switchStatus(true);
                }
            }).keyup(function() {
                var val = $that.val();
                if (val == '') {
                    switchStatus(false);
                    $that.setCursorPosition({index: 0});
                }
            })
        }
     
        return this.each(function() {
            var $elem = $(this);
            bootstrap($elem)
            if ($.isFunction(callback)) callback($elem);
        })
    }
    $("input,textarea").placeholder();
})(jQuery);