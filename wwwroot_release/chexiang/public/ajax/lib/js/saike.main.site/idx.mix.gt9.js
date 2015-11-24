/*
 * Selecter v3.1.10 - 2014-09-14
 * A jQuery plugin for replacing default select elements. Part of the Formstone Library.
 * http://formstone.it/selecter/
 *
 * Copyright 2014 Ben Plum; MIT Licensed
 */

;(function ($, window) {
	"use strict";

	var guid = 0,
		userAgent = (window.navigator.userAgent||window.navigator.vendor||window.opera),
		isFirefox = /Firefox/i.test(userAgent),
		isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(userAgent),
		isFirefoxMobile = (isFirefox && isMobile),
		$body = null;

	/**
	 * @options
	 * @param callback [function] <$.noop> "Select item callback"
	 * @param cover [boolean] <false> "Cover handle with option set"
	 * @param customClass [string] <''> "Class applied to instance"
	 * @param label [string] <''> "Label displayed before selection"
	 * @param external [boolean] <false> "Open options as links in new window"
	 * @param links [boolean] <false> "Open options as links in same window"
	 * @param mobile [boolean] <false> "Force desktop interaction on mobile"
	 * @param trim [int] <0> "Trim options to specified length; 0 to disable”
	 */
	var options = {
		callback: $.noop,
		cover: false,
		customClass: "",
		label: "",
		external: false,
		links: false,
		mobile: false,
		trim: 0
	};

	var pub = {

		/**
		 * @method
		 * @name defaults
		 * @description Sets default plugin options
		 * @param opts [object] <{}> "Options object"
		 * @example $.selecter("defaults", opts);
		 */
		defaults: function(opts) {
			options = $.extend(options, opts || {});
			return $(this);
		},

		/**
		 * @method
		 * @name disable
		 * @description Disables target instance or option
		 * @param option [string] <null> "Target option value"
		 * @example $(".target").selecter("disable", "1");
		 */
		disable: function(option) {
			return $(this).each(function(i, input) {
				var data = $(input).parent(".selecter").data("selecter");

				if (data) {
					if (typeof option !== "undefined") {
						var index = data.$items.index( data.$items.filter("[data-value=" + option + "]") );

						data.$items.eq(index).addClass("disabled");
						data.$options.eq(index).prop("disabled", true);
					} else {
						if (data.$selecter.hasClass("open")) {
							data.$selecter.find(".selecter-selected").trigger("click.selecter");
						}

						data.$selecter.addClass("disabled");
						data.$select.prop("disabled", true);
					}
				}
			});
		},

		/**
		 * @method
		 * @name destroy
		 * @description Removes instance of plugin
		 * @example $(".target").selecter("destroy");
		 */
		destroy: function() {
			return $(this).each(function(i, input) {
				var data = $(input).parent(".selecter").data("selecter");

				if (data) {
					if (data.$selecter.hasClass("open")) {
						data.$selecter.find(".selecter-selected").trigger("click.selecter");
					}

					// Scroller support
					if ($.fn.scroller !== undefined) {
						data.$selecter.find(".selecter-options").scroller("destroy");
					}

					data.$select[0].tabIndex = data.tabIndex;

					data.$select.find(".selecter-placeholder").remove();
					data.$selected.remove();
					data.$itemsWrapper.remove();

					data.$selecter.off(".selecter");

					data.$select.off(".selecter")
								.removeClass("selecter-element")
								.show()
								.unwrap();
				}
			});
		},

		/**
		 * @method
		 * @name enable
		 * @description Enables target instance or option
		 * @param option [string] <null> "Target option value"
		 * @example $(".target").selecter("enable", "1");
		 */
		enable: function(option) {
			return $(this).each(function(i, input) {
				var data = $(input).parent(".selecter").data("selecter");

				if (data) {
					if (typeof option !== "undefined") {
						var index = data.$items.index( data.$items.filter("[data-value=" + option + "]") );
						data.$items.eq(index).removeClass("disabled");
						data.$options.eq(index).prop("disabled", false);
					} else {
						data.$selecter.removeClass("disabled");
						data.$select.prop("disabled", false);
					}
				}
			});
		},


		/**
		* @method private
		* @name refresh
		* @description DEPRECATED - Updates instance base on target options
		* @example $(".target").selecter("refresh");
		*/
		refresh: function() {
			return pub.update.apply($(this));
		},

		/**
		* @method
		* @name update
		* @description Updates instance base on target options
		* @example $(".target").selecter("update");
		*/
		update: function() {
			return $(this).each(function(i, input) {
				var data = $(input).parent(".selecter").data("selecter");

				if (data) {
					var index = data.index;

					data.$allOptions = data.$select.find("option, optgroup");
					data.$options = data.$allOptions.filter("option");
					data.index = -1;

					index = data.$options.index(data.$options.filter(":selected"));

					_buildOptions(data);

					if (!data.multiple) {
						_update(index, data);
					}
				}
			});
		}
	};

	/**
	 * @method private
	 * @name _init
	 * @description Initializes plugin
	 * @param opts [object] "Initialization options"
	 */
	function _init(opts) {

		var $items, sltPlaceholder, sltSelected;

		// Local options
		opts = $.extend({}, options, opts || {});

		// Check for Body
		if ($body === null) {

			$body = $("body");

		}

		// Apply to each element
		$items = $(this);

		for (var i = 0, count = $items.length; i < count; i++) {

			_build($items.eq(i), opts);

		}

		return $items;

	}

	/**
	 * @method private
	 * @name _build
	 * @description Builds each instance
	 * @param $select [jQuery object] "Target jQuery object"
	 * @param opts [object] <{}> "Options object"
	 */
	function _build($select, opts) {

		if (!$select.hasClass("selecter-element")) {

			// EXTEND OPTIONS
			opts = $.extend({}, opts, $select.data("selecter-options"));

			opts.multiple = $select.prop("multiple");

			opts.disabled = $select.is(":disabled");

			if (opts.external) {

				opts.links = true;

			}

			// Test for selected option in case we need to override the custom label
			var $originalOption = $select.find(":selected");

			if (!opts.multiple && opts.label !== "") {

				var _option = $('<option/>', {

					'value': '',

					'class': 'selecter-placeholder',

					'selected': true

				});

				_option.html(opts.label);

				$select.prepend(_option);

			} else {

				opts.label = "";

			}

			// Build options array
			var $allOptions = $select.find("option, optgroup"),
				$options = $allOptions.filter("option");

			// update original in case we needed a custom label placeholder
			$originalOption = $options.filter(":selected");

			var originalIndex = ($originalOption.length > 0) ? $options.index($originalOption) : 0,
				originalLabel = (opts.label !== "") ? opts.label : $originalOption.text(),
				wrapperTag = "div";
				//wrapperTag = (opts.links) ? "nav" : "div"; // nav's usage still up for debate...

			// Swap tab index, no more interacting with the actual select!
			opts.tabIndex = $select[0].tabIndex;
			$select[0].tabIndex = -1;

			// Build HTML
			var inner = "",
				wrapper = "";

			// Build wrapper
			wrapper += '<' + wrapperTag + ' class="selecter ' + opts.customClass;
			// Special case classes
			if (isMobile) {
				wrapper += ' mobile';
			} else if (opts.cover) {
				wrapper += ' cover';
			}
			if (opts.multiple) {
				wrapper += ' multiple';
			} else {
				wrapper += ' closed';
			}
			if (opts.disabled) {
				wrapper += ' disabled';
			}
			wrapper += '" tabindex="' + opts.tabIndex + '">';
			wrapper += '</' + wrapperTag + '>';

			// Build inner
			if (!opts.multiple) {
				inner += '<span class="selecter-selected">';
				// inner += $('<span></span>').text( _trim((($originalOption.text() !== "") ? $originalOption.text() : opts.label), opts.trim) ).html();
				inner += $('<span></span>').text( _trim(originalLabel, opts.trim) ).html();
				inner += '</span>';
			}
			inner += '<div class="selecter-options">';
			inner += '</div>';

			// Modify DOM
			$select.addClass("selecter-element")
				   .wrap(wrapper)
				   .after(inner);

			// Store plugin data
			var $selecter = $select.parent(".selecter"),
				data = $.extend({
					$select: $select,
					$allOptions: $allOptions,
					$options: $options,
					$selecter: $selecter,
					$selected: $selecter.find(".selecter-selected"),
					$itemsWrapper: $selecter.find(".selecter-options"),
					index: -1,
					guid: guid++
				}, opts);

			_buildOptions(data);

			if (!data.multiple) {

				_update(originalIndex-1, data); // Modifyied by Tony Stark

			}

			// Scroller support
			if ($.fn.scroller !== undefined) {
				data.$itemsWrapper.scroller();
			}

			// Bind click events
			data.$selecter.on("touchstart.selecter", ".selecter-selected", data, _onTouchStart)
						  .on("click.selecter", ".selecter-selected", data, _onClick)
						  .on("click.selecter", ".selecter-item", data, _onSelect)
						  .on("close.selecter", data, _onClose)
						  .data("selecter", data);

			// Bind Blur/focus events
			//if ((!data.links && !isMobile) || isMobile) {
				data.$select.on("change.selecter", data, _onChange);

				if (!isMobile) {
					data.$selecter.on("focus.selecter", data, _onFocus)
								  .on("blur.selecter", data, _onBlur);

					// handle clicks to associated labels - not on mobile
					data.$select.on("focus.selecter", data, function(e) {
						e.data.$selecter.trigger("focus");
					});
				}

			//} else {
				// Disable browser focus/blur for jump links
				//data.$select.hide();
			//}
		}
	}

	/**
	 * @method private
	 * @name _buildOptions
	 * @description Builds instance's option set
	 * @param data [object] "Instance data"
	 */
	function _buildOptions(data) {
		var html = '',
			itemTag = (data.links) ? "a" : "span",
			j = 0;

		for (var i = 0, count = data.$allOptions.length; i < count; i++) {
			var $op = data.$allOptions.eq(i);

			// Option group
			if ($op[0].tagName === "OPTGROUP") {
				html += '<span class="selecter-group';
				// Disabled groups
				if ($op.is(":disabled")) {
					html += ' disabled';
				}
				html += '">' + $op.attr("label") + '</span>';
			} else {
				var opVal = $op.val();

				if (!$op.attr("value")) {
					$op.attr("value", opVal);
				}

				html += '<' + itemTag + ' class="selecter-item';
				if ($op.hasClass('selecter-placeholder')) {
					html += ' placeholder';
				}
				// Default selected value - now handles multi's thanks to @kuilkoff
				if ($op.is(':selected')) {
					html += ' selected';
				}
				// Disabled options
				if ($op.is(":disabled")) {
					html += ' disabled';
				}
				html += '" ';
				if (data.links) {
					html += 'href="' + opVal + '"';
				} else {
					html += 'data-value="' + opVal + '"';
				}
				html += '>' + $("<span></span>").text( _trim($op.text(), data.trim) ).html() + '</' + itemTag + '>';
				j++;
			}
		}

		data.$itemsWrapper.html(html);
		data.$items = data.$selecter.find(".selecter-item");
	}

	/**
	 * @method private
	 * @name _onTouchStart
	 * @description Handles touchstart to selected item
	 * @param e [object] "Event data"
	 */
	function _onTouchStart(e) {
		e.stopPropagation();

		var data = e.data,
			oe = e.originalEvent;

		_clearTimer(data.timer);

		data.touchStartX = oe.touches[0].clientX;
		data.touchStartY = oe.touches[0].clientY;

		data.$selecter.on("touchmove.selecter", ".selecter-selected", data, _onTouchMove)
					  .on("touchend.selecter", ".selecter-selected", data, _onTouchEnd);
	}

	/**
	 * @method private
	 * @name _onTouchMove
	 * @description Handles touchmove to selected item
	 * @param e [object] "Event data"
	 */
	function _onTouchMove(e) {
		var data = e.data,
			oe = e.originalEvent;

		if (Math.abs(oe.touches[0].clientX - data.touchStartX) > 10 || Math.abs(oe.touches[0].clientY - data.touchStartY) > 10) {
			data.$selecter.off("touchmove.selecter touchend.selecter");
		}
	}

	/**
	 * @method private
	 * @name _onTouchEnd
	 * @description Handles touchend to selected item
	 * @param e [object] "Event data"
	 */
	function _onTouchEnd(e) {
		var data = e.data;

		data.$selecter.off("touchmove.selecter touchend.selecter click.selecter");

		// prevent ghosty clicks
		data.timer = _startTimer(data.timer, 1000, function() {
			data.$selecter.on("click.selecter", ".selecter-selected", data, _onClick)
						  .on("click.selecter", ".selecter-item", data, _onSelect);
		});

		_onClick(e);
	}

	/**
	 * @method private
	 * @name _onClick
	 * @description Handles click to selected item
	 * @param e [object] "Event data"
	 */
	function _onClick(e) {
		e.preventDefault();
		e.stopPropagation();

		var data = e.data;

		if (!data.$select.is(":disabled")) {
			$(".selecter").not(data.$selecter).trigger("close.selecter", [data]);

			// Handle mobile, but not Firefox, unless desktop forced
			if (!data.mobile && isMobile && !isFirefoxMobile) {
				var el = data.$select[0];
				if (window.document.createEvent) { // All
					var evt = window.document.createEvent("MouseEvents");
					evt.initMouseEvent("mousedown", false, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
					el.dispatchEvent(evt);
				} else if (el.fireEvent) { // IE
					el.fireEvent("onmousedown");
				}
			} else {
				// Delegate intent
				if (data.$selecter.hasClass("closed")) {
					_onOpen(e);
				} else if (data.$selecter.hasClass("open")) {
					_onClose(e);
				}
			}
		}
	}

	/**
	 * @method private
	 * @name _onOpen
	 * @description Opens option set
	 * @param e [object] "Event data"
	 */
	function _onOpen(e) {
		e.preventDefault();
		e.stopPropagation();

		var data = e.data;

		// Make sure it's not alerady open
		if (!data.$selecter.hasClass("open")) {
			var offset = data.$selecter.offset(),
				bodyHeight = $body.outerHeight(),
				optionsHeight = data.$itemsWrapper.outerHeight(true),
				selectedOffset = (data.index >= 0) ? data.$items.eq(data.index).position() : { left: 0, top: 0 };

			// Calculate bottom of document
			if (offset.top + optionsHeight > bodyHeight) {
				data.$selecter.addClass("bottom");
			}

			data.$itemsWrapper.show();

			// Bind Events
			data.$selecter.removeClass("closed")
						  .addClass("open");
			$body.on("click.selecter-" + data.guid, ":not(.selecter-options)", data, _onCloseHelper);

			_scrollOptions(data);
		}
	}

	/**
	 * @method private
	 * @name _onCloseHelper
	 * @description Determines if event target is outside instance before closing
	 * @param e [object] "Event data"
	 */
	function _onCloseHelper(e) {
		e.preventDefault();
		e.stopPropagation();

		if ($(e.currentTarget).parents(".selecter").length === 0) {
			_onClose(e);
		}
	}

	/**
	 * @method private
	 * @name _onClose
	 * @description Closes option set
	 * @param e [object] "Event data"
	 */
	function _onClose(e) {
		e.preventDefault();
		e.stopPropagation();

		var data = e.data;

		// Make sure it's actually open
		if (data.$selecter.hasClass("open")) {
			data.$itemsWrapper.hide();
			data.$selecter.removeClass("open bottom")
						  .addClass("closed");

			$body.off(".selecter-" + data.guid);
		}
	}

	/**
	 * @method private
	 * @name _onSelect
	 * @description Handles option select
	 * @param e [object] "Event data"
	 */
	function _onSelect(e) {
		e.preventDefault();
		e.stopPropagation();

		var $target = $(this),
			data = e.data;

		if (!data.$select.is(":disabled")) {
			if (data.$itemsWrapper.is(":visible")) {
				// Update
				var index = data.$items.index($target);

				if (index !== data.index) {
					_update(index, data);
					_handleChange(data);
				}
			}

			if (!data.multiple) {
				// Clean up
				_onClose(e);
			}
		}
	}

	/**
	 * @method private
	 * @name _onChange
	 * @description Handles external changes
	 * @param e [object] "Event data"
	 */
	function _onChange(e, internal) {
		var $target = $(this),
			data = e.data;

		if (!internal && !data.multiple) {
			var index = data.$options.index(data.$options.filter("[value='" + _escape($target.val()) + "']"));

			_update(index, data);
			_handleChange(data);
		}
	}

	/**
	 * @method private
	 * @name _onFocus
	 * @description Handles instance focus
	 * @param e [object] "Event data"
	 */
	function _onFocus(e) {
		e.preventDefault();
		e.stopPropagation();

		var data = e.data;

		if (!data.$select.is(":disabled") && !data.multiple) {
			data.$selecter.addClass("focus")
						  .on("keydown.selecter-" + data.guid, data, _onKeypress);

			$(".selecter").not(data.$selecter)
						  .trigger("close.selecter", [ data ]);
		}
	}

	/**
	 * @method private
	 * @name _onBlur
	 * @description Handles instance focus
	 * @param e [object] "Event data"
	 */
	function _onBlur(e, internal, two) {
		e.preventDefault();
		e.stopPropagation();

		var data = e.data;

		data.$selecter.removeClass("focus")
					  .off("keydown.selecter-" + data.guid);

		$(".selecter").not(data.$selecter)
					  .trigger("close.selecter", [ data ]);
	}

	/**
	 * @method private
	 * @name _onKeypress
	 * @description Handles instance keypress, once focused
	 * @param e [object] "Event data"
	 */
	function _onKeypress(e) {
		var data = e.data;

		if (e.keyCode === 13) {
			if (data.$selecter.hasClass("open")) {
				_onClose(e);
				_update(data.index, data);
			}
			_handleChange(data);
		} else if (e.keyCode !== 9 && (!e.metaKey && !e.altKey && !e.ctrlKey && !e.shiftKey)) {
			// Ignore modifiers & tabs
			e.preventDefault();
			e.stopPropagation();

			var total = data.$items.length - 1,
				index = (data.index < 0) ? 0 : data.index;

			// Firefox left/right support thanks to Kylemade
			if ($.inArray(e.keyCode, (isFirefox) ? [38, 40, 37, 39] : [38, 40]) > -1) {
				// Increment / decrement using the arrow keys
				index = index + ((e.keyCode === 38 || (isFirefox && e.keyCode === 37)) ? -1 : 1);

				if (index < 0) {
					index = 0;
				}
				if (index > total) {
					index = total;
				}
			} else {
				var input = String.fromCharCode(e.keyCode).toUpperCase(),
					letter,
					i;

				// Search for input from original index
				for (i = data.index + 1; i <= total; i++) {
					letter = data.$options.eq(i).text().charAt(0).toUpperCase();
					if (letter === input) {
						index = i;
						break;
					}
				}

				// If not, start from the beginning
				if (index < 0 || index === data.index) {
					for (i = 0; i <= total; i++) {
						letter = data.$options.eq(i).text().charAt(0).toUpperCase();
						if (letter === input) {
							index = i;
							break;
						}
					}
				}
			}

			// Update
			if (index >= 0) {
				_update(index, data);
				_scrollOptions(data);
			}
		}
	}

	/**
	 * @method private
	 * @name _update
	 * @description Updates instance based on new target index
	 * @param index [int] "Selected option index"
	 * @param data [object] "instance data"
	 */
	function _update(index, data) {
		var $item = data.$items.eq(index),
			isSelected = $item.hasClass("selected"),
			isDisabled = $item.hasClass("disabled");

		// Check for disabled options
		if (!isDisabled) {
			if (data.multiple) {
				if (isSelected) {
					data.$options.eq(index).prop("selected", null);
					$item.removeClass("selected");
				} else {
					data.$options.eq(index).prop("selected", true);
					$item.addClass("selected");
				}
			} else if (index > -1 && index < data.$items.length) {
				var newLabel = $item.html(),
					newValue = $item.data("value");

				data.$selected.html(newLabel)
							  .removeClass('placeholder');

				data.$items.filter(".selected")
						   .removeClass("selected");

				data.$select[0].selectedIndex = index;

				$item.addClass("selected");
				data.index = index;
			} else if (data.label !== "") {
				data.$selected.html(data.label);
			}
		}
	}

	/**
	 * @method private
	 * @name _scrollOptions
	 * @description Scrolls options wrapper to specific option
	 * @param data [object] "Instance data"
	 */
	function _scrollOptions(data) {
		var $selected = data.$items.eq(data.index),
			selectedOffset = (data.index >= 0 && !$selected.hasClass("placeholder")) ? $selected.position() : { left: 0, top: 0 };

		if ($.fn.scroller !== undefined) {
			data.$itemsWrapper.scroller("scroll", (data.$itemsWrapper.find(".scroller-content").scrollTop() + selectedOffset.top), 0)
							  .scroller("reset");
		} else {
			data.$itemsWrapper.scrollTop( data.$itemsWrapper.scrollTop() + selectedOffset.top );
		}
	}

	/**
	 * @method private
	 * @name _handleChange
	 * @description Handles change events
	 * @param data [object] "Instance data"
	 */
	function _handleChange(data) {
		if (data.links) {
			_launch(data);
		} else {
			data.callback.call(data.$selecter, data.$select.val(), data.index);
			data.$select.trigger("change", [ true ]);
		}
	}

	/**
	 * @method private
	 * @name _launch
	 * @description Launches link
	 * @param data [object] "Instance data"
	 */
	function _launch(data) {
		//var url = (isMobile) ? data.$select.val() : data.$options.filter(":selected").attr("href");
		var url = data.$select.val();

		if (data.external) {
			// Open link in a new tab/window
			window.open(url);
		} else {
			// Open link in same tab/window
			window.location.href = url;
		}
	}

	/**
	 * @method private
	 * @name _trim
	 * @description Trims text, if specified length is greater then 0
	 * @param length [int] "Length to trim at"
	 * @param text [string] "Text to trim"
	 * @return [string] "Trimmed string"
	 */
	function _trim(text, length) {
		if (length === 0) {
			return text;
		} else {
			if (text.length > length) {
				return text.substring(0, length) + "...";
			} else {
				return text;
			}
		}
	}

	/**
	 * @method private
	 * @name _escape
	 * @description Escapes text
	 * @param text [string] "Text to escape"
	 */
	function _escape(text) {
		return (typeof text === "string") ? text.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1') : text;
	}

	/**
	 * @method private
	 * @name _startTimer
	 * @description Starts an internal timer
	 * @param timer [int] "Timer ID"
	 * @param time [int] "Time until execution"
	 * @param callback [int] "Function to execute"
	 * @param interval [boolean] "Flag for recurring interval"
	 */
	function _startTimer(timer, time, func, interval) {
		_clearTimer(timer, interval);
		if (interval === true) {
			return setInterval(func, time);
		} else {
			return setTimeout(func, time);
		}
	}

	/**
	 * @method private
	 * @name _clearTimer
	 * @description Clears an internal timer
	 * @param timer [int] "Timer ID"
	 */
	function _clearTimer(timer) {
		if (timer !== null) {
			clearInterval(timer);
			timer = null;
		}
	}

	$.fn.selecter = function(method) {
		if (pub[method]) {
			return pub[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return _init.apply(this, arguments);
		}
		return this;
	};

	$.selecter = function(method) {
		if (method === "defaults") {
			pub.defaults.apply(this, Array.prototype.slice.call(arguments, 1));
		}
	};
})(jQuery, window);


/*
 * The MIT License
 *
 * Copyright (c) 2012 James Allardice
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Defines the global Placeholders object along with various utility methods
(function (global) {

    "use strict";

    // Cross-browser DOM event binding
    function addEventListener(elem, event, fn) {
        if (elem.addEventListener) {
            return elem.addEventListener(event, fn, false);
        }
        if (elem.attachEvent) {
            return elem.attachEvent("on" + event, fn);
        }
    }

    // Check whether an item is in an array (we don't use Array.prototype.indexOf so we don't clobber any existing polyfills - this is a really simple alternative)
    function inArray(arr, item) {
        var i, len;
        for (i = 0, len = arr.length; i < len; i++) {
            if (arr[i] === item) {
                return true;
            }
        }
        return false;
    }

    // Move the caret to the index position specified. Assumes that the element has focus
    function moveCaret(elem, index) {
        var range;
        if (elem.createTextRange) {
            range = elem.createTextRange();
            range.move("character", index);
            range.select();
        } else if (elem.selectionStart) {
            elem.focus();
            elem.setSelectionRange(index, index);
        }
    }

    // Attempt to change the type property of an input element
    function changeType(elem, type) {
        try {
            elem.type = type;
            return true;
        } catch (e) {
            // You can't change input type in IE8 and below
            return false;
        }
    }

    // Expose public methods
    global.Placeholders = {
        Utils: {
            addEventListener: addEventListener,
            inArray: inArray,
            moveCaret: moveCaret,
            changeType: changeType
        }
    };

}(this));

(function (global) {

    "use strict";

    var validTypes = [
            "text",
            "search",
            "url",
            "tel",
            "email",
            "password",
            "number",
            "textarea"
        ],

        // The list of keycodes that are not allowed when the polyfill is configured to hide-on-input
        badKeys = [

            // The following keys all cause the caret to jump to the end of the input value
            27, // Escape
            33, // Page up
            34, // Page down
            35, // End
            36, // Home

            // Arrow keys allow you to move the caret manually, which should be prevented when the placeholder is visible
            37, // Left
            38, // Up
            39, // Right
            40, // Down

            // The following keys allow you to modify the placeholder text by removing characters, which should be prevented when the placeholder is visible
            8, // Backspace
            46 // Delete
        ],

        // Styling variables
        placeholderStyleColor = "#ccc",
        placeholderClassName = "placeholdersjs",
        classNameRegExp = new RegExp("(?:^|\\s)" + placeholderClassName + "(?!\\S)"),

        // These will hold references to all elements that can be affected. NodeList objects are live, so we only need to get those references once
        inputs, textareas,

        // The various data-* attributes used by the polyfill
        ATTR_CURRENT_VAL = "data-placeholder-value",
        ATTR_ACTIVE = "data-placeholder-active",
        ATTR_INPUT_TYPE = "data-placeholder-type",
        ATTR_FORM_HANDLED = "data-placeholder-submit",
        ATTR_EVENTS_BOUND = "data-placeholder-bound",
        ATTR_OPTION_FOCUS = "data-placeholder-focus",
        ATTR_OPTION_LIVE = "data-placeholder-live",
        ATTR_MAXLENGTH = "data-placeholder-maxlength",

        // Various other variables used throughout the rest of the script
        test = document.createElement("input"),
        head = document.getElementsByTagName("head")[0],
        root = document.documentElement,
        Placeholders = global.Placeholders,
        Utils = Placeholders.Utils,
        hideOnInput, liveUpdates, keydownVal, styleElem, styleRules, placeholder, timer, form, elem, len, i;

    // No-op (used in place of public methods when native support is detected)
    function noop() {}

    // Avoid IE9 activeElement of death when an iframe is used.
    // More info:
    // http://bugs.jquery.com/ticket/13393
    // https://github.com/jquery/jquery/commit/85fc5878b3c6af73f42d61eedf73013e7faae408
    function safeActiveElement() {
        try {
            return document.activeElement;
        } catch (err) {}
    }

    // Hide the placeholder value on a single element. Returns true if the placeholder was hidden and false if it was not (because it wasn't visible in the first place)
    function hidePlaceholder(elem, keydownValue) {
        var type,
            maxLength,
            valueChanged = (!!keydownValue && elem.value !== keydownValue),
            isPlaceholderValue = (elem.value === elem.getAttribute(ATTR_CURRENT_VAL));

        if ((valueChanged || isPlaceholderValue) && elem.getAttribute(ATTR_ACTIVE) === "true") {
            elem.removeAttribute(ATTR_ACTIVE);
            elem.value = elem.value.replace(elem.getAttribute(ATTR_CURRENT_VAL), "");
            elem.className = elem.className.replace(classNameRegExp, "");

            // Restore the maxlength value
            maxLength = elem.getAttribute(ATTR_MAXLENGTH);
            if (parseInt(maxLength, 10) >= 0) { // Old FF returns -1 if attribute not set (see GH-56)
                elem.setAttribute("maxLength", maxLength);
                elem.removeAttribute(ATTR_MAXLENGTH);
            }

            // If the polyfill has changed the type of the element we need to change it back
            type = elem.getAttribute(ATTR_INPUT_TYPE);
            if (type) {
                elem.type = type;
            }
            return true;
        }
        return false;
    }

    // Show the placeholder value on a single element. Returns true if the placeholder was shown and false if it was not (because it was already visible)
    function showPlaceholder(elem) {
        var type,
            maxLength,
            val = elem.getAttribute(ATTR_CURRENT_VAL);
        if (elem.value === "" && val) {
            elem.setAttribute(ATTR_ACTIVE, "true");
            elem.value = val;
            elem.className += " " + placeholderClassName;

            // Store and remove the maxlength value
            maxLength = elem.getAttribute(ATTR_MAXLENGTH);
            if (!maxLength) {
                elem.setAttribute(ATTR_MAXLENGTH, elem.maxLength);
                elem.removeAttribute("maxLength");
            }

            // If the type of element needs to change, change it (e.g. password inputs)
            type = elem.getAttribute(ATTR_INPUT_TYPE);
            if (type) {
                elem.type = "text";
            } else if (elem.type === "password") {
                if (Utils.changeType(elem, "text")) {
                    elem.setAttribute(ATTR_INPUT_TYPE, "password");
                }
            }
            return true;
        }
        return false;
    }

    function handleElem(node, callback) {

        var handleInputsLength, handleTextareasLength, handleInputs, handleTextareas, elem, len, i;

        // Check if the passed in node is an input/textarea (in which case it can't have any affected descendants)
        if (node && node.getAttribute(ATTR_CURRENT_VAL)) {
            callback(node);
        } else {

            // If an element was passed in, get all affected descendants. Otherwise, get all affected elements in document
            handleInputs = node ? node.getElementsByTagName("input") : inputs;
            handleTextareas = node ? node.getElementsByTagName("textarea") : textareas;

            handleInputsLength = handleInputs ? handleInputs.length : 0;
            handleTextareasLength = handleTextareas ? handleTextareas.length : 0;

            // Run the callback for each element
            for (i = 0, len = handleInputsLength + handleTextareasLength; i < len; i++) {
                elem = i < handleInputsLength ? handleInputs[i] : handleTextareas[i - handleInputsLength];
                callback(elem);
            }
        }
    }

    // Return all affected elements to their normal state (remove placeholder value if present)
    function disablePlaceholders(node) {
        handleElem(node, hidePlaceholder);
    }

    // Show the placeholder value on all appropriate elements
    function enablePlaceholders(node) {
        handleElem(node, showPlaceholder);
    }

    // Returns a function that is used as a focus event handler
    function makeFocusHandler(elem) {
        return function () {

            // Only hide the placeholder value if the (default) hide-on-focus behaviour is enabled
            if (hideOnInput && elem.value === elem.getAttribute(ATTR_CURRENT_VAL) && elem.getAttribute(ATTR_ACTIVE) === "true") {

                // Move the caret to the start of the input (this mimics the behaviour of all browsers that do not hide the placeholder on focus)
                Utils.moveCaret(elem, 0);

            } else {

                // Remove the placeholder
                hidePlaceholder(elem);
            }
        };
    }

    // Returns a function that is used as a blur event handler
    function makeBlurHandler(elem) {
        return function () {
            showPlaceholder(elem);
        };
    }

    // Functions that are used as a event handlers when the hide-on-input behaviour has been activated - very basic implementation of the "input" event
    function makeKeydownHandler(elem) {
        return function (e) {
            keydownVal = elem.value;

            //Prevent the use of the arrow keys (try to keep the cursor before the placeholder)
            if (elem.getAttribute(ATTR_ACTIVE) === "true") {
                if (keydownVal === elem.getAttribute(ATTR_CURRENT_VAL) && Utils.inArray(badKeys, e.keyCode)) {
                    if (e.preventDefault) {
                        e.preventDefault();
                    }
                    return false;
                }
            }
        };
    }
    function makeKeyupHandler(elem) {
        return function () {
            hidePlaceholder(elem, keydownVal);

            // If the element is now empty we need to show the placeholder
            if (elem.value === "") {
                elem.blur();
                Utils.moveCaret(elem, 0);
            }
        };
    }
    function makeClickHandler(elem) {
        return function () {
            if (elem === safeActiveElement() && elem.value === elem.getAttribute(ATTR_CURRENT_VAL) && elem.getAttribute(ATTR_ACTIVE) === "true") {
                Utils.moveCaret(elem, 0);
            }
        };
    }

    // Returns a function that is used as a submit event handler on form elements that have children affected by this polyfill
    function makeSubmitHandler(form) {
        return function () {

            // Turn off placeholders on all appropriate descendant elements
            disablePlaceholders(form);
        };
    }

    // Bind event handlers to an element that we need to affect with the polyfill
    function newElement(elem) {

        // If the element is part of a form, make sure the placeholder string is not submitted as a value
        if (elem.form) {
            form = elem.form;

            // If the type of the property is a string then we have a "form" attribute and need to get the real form
            if (typeof form === "string") {
                form = document.getElementById(form);
            }

            // Set a flag on the form so we know it's been handled (forms can contain multiple inputs)
            if (!form.getAttribute(ATTR_FORM_HANDLED)) {
                //Utils.addEventListener(form, "submit", makeSubmitHandler(form));
                form.setAttribute(ATTR_FORM_HANDLED, "true");
            }
        }

        // Bind event handlers to the element so we can hide/show the placeholder as appropriate
        Utils.addEventListener(elem, "focus", makeFocusHandler(elem));
        Utils.addEventListener(elem, "blur", makeBlurHandler(elem));

        // If the placeholder should hide on input rather than on focus we need additional event handlers
        if (hideOnInput) {
            Utils.addEventListener(elem, "keydown", makeKeydownHandler(elem));
            Utils.addEventListener(elem, "keyup", makeKeyupHandler(elem));
            Utils.addEventListener(elem, "click", makeClickHandler(elem));
        }

        // Remember that we've bound event handlers to this element
        elem.setAttribute(ATTR_EVENTS_BOUND, "true");
        elem.setAttribute(ATTR_CURRENT_VAL, placeholder);

        // If the element doesn't have a value and is not focussed, set it to the placeholder string
        if (hideOnInput || elem !== safeActiveElement()) {
            showPlaceholder(elem);
        }
    }

    Placeholders.nativeSupport = test.placeholder !== void 0;

    if (!Placeholders.nativeSupport) {

        // Get references to all the input and textarea elements currently in the DOM (live NodeList objects to we only need to do this once)
        inputs = document.getElementsByTagName("input");
        textareas = document.getElementsByTagName("textarea");

        // Get any settings declared as data-* attributes on the root element (currently the only options are whether to hide the placeholder on focus or input and whether to auto-update)
        hideOnInput = root.getAttribute(ATTR_OPTION_FOCUS) === "false";
        liveUpdates = root.getAttribute(ATTR_OPTION_LIVE) !== "false";

        // Create style element for placeholder styles (instead of directly setting style properties on elements - allows for better flexibility alongside user-defined styles)
        styleElem = document.createElement("style");
        styleElem.type = "text/css";

        // Create style rules as text node
        styleRules = document.createTextNode("." + placeholderClassName + " { color:" + placeholderStyleColor + "; }");

        // Append style rules to newly created stylesheet
        if (styleElem.styleSheet) {
            styleElem.styleSheet.cssText = styleRules.nodeValue;
        } else {
            styleElem.appendChild(styleRules);
        }

        // Prepend new style element to the head (before any existing stylesheets, so user-defined rules take precedence)
        head.insertBefore(styleElem, head.firstChild);

        // Set up the placeholders
        for (i = 0, len = inputs.length + textareas.length; i < len; i++) {
            elem = i < inputs.length ? inputs[i] : textareas[i - inputs.length];

            // Get the value of the placeholder attribute, if any. IE10 emulating IE7 fails with getAttribute, hence the use of the attributes node
            placeholder = elem.attributes.placeholder;
            if (placeholder) {

                // IE returns an empty object instead of undefined if the attribute is not present
                placeholder = placeholder.nodeValue;

                // Only apply the polyfill if this element is of a type that supports placeholders, and has a placeholder attribute with a non-empty value
                if (placeholder && Utils.inArray(validTypes, elem.type)) {
                    newElement(elem);
                }
            }
        }

        // If enabled, the polyfill will repeatedly check for changed/added elements and apply to those as well
        timer = setInterval(function () {
            for (i = 0, len = inputs.length + textareas.length; i < len; i++) {
                elem = i < inputs.length ? inputs[i] : textareas[i - inputs.length];

                // Only apply the polyfill if this element is of a type that supports placeholders, and has a placeholder attribute with a non-empty value
                placeholder = elem.attributes.placeholder;
                if (placeholder) {
                    placeholder = placeholder.nodeValue;
                    if (placeholder && Utils.inArray(validTypes, elem.type)) {

                        // If the element hasn't had event handlers bound to it then add them
                        if (!elem.getAttribute(ATTR_EVENTS_BOUND)) {
                            newElement(elem);
                        }

                        // If the placeholder value has changed or not been initialised yet we need to update the display
                        if (placeholder !== elem.getAttribute(ATTR_CURRENT_VAL) || (elem.type === "password" && !elem.getAttribute(ATTR_INPUT_TYPE))) {

                            // Attempt to change the type of password inputs (fails in IE < 9)
                            if (elem.type === "password" && !elem.getAttribute(ATTR_INPUT_TYPE) && Utils.changeType(elem, "text")) {
                                elem.setAttribute(ATTR_INPUT_TYPE, "password");
                            }

                            // If the placeholder value has changed and the placeholder is currently on display we need to change it
                            if (elem.value === elem.getAttribute(ATTR_CURRENT_VAL)) {
                                elem.value = placeholder;
                            }

                            // Keep a reference to the current placeholder value in case it changes via another script
                            elem.setAttribute(ATTR_CURRENT_VAL, placeholder);
                        }
                    }
                } else if (elem.getAttribute(ATTR_ACTIVE)) {
                    hidePlaceholder(elem);
                    elem.removeAttribute(ATTR_CURRENT_VAL);
                }
            }

            // If live updates are not enabled cancel the timer
            if (!liveUpdates) {
                clearInterval(timer);
            }
        }, 100);
    }

    Utils.addEventListener(global, "beforeunload", function () {
        Placeholders.disable();
    });

    // Expose public methods
    Placeholders.disable = Placeholders.nativeSupport ? noop : disablePlaceholders;
    Placeholders.enable = Placeholders.nativeSupport ? noop : enablePlaceholders;

}(this));

(function ($) {

    "use strict";

    var originalValFn = $.fn.val,
        originalPropFn = $.fn.prop;

    if (!Placeholders.nativeSupport) {

        $.fn.val = function (val) {
            var originalValue = originalValFn.apply(this, arguments),
                placeholder = this.eq(0).data("placeholder-value");
            if (val === undefined && this.eq(0).data("placeholder-active") && originalValue === placeholder) {
                return "";
            }
            return originalValue;
        };

        $.fn.prop = function (name, val) {
            if (val === undefined && this.eq(0).data("placeholder-active") && name === "value") {
                return "";
            }
            return originalPropFn.apply(this, arguments);
        };
    }

}(jQuery));
/*!
 * jQuery Validation Plugin v1.13.0
 *
 * http://jqueryvalidation.org/
 *
 * Copyright (c) 2014 Jörn Zaefferer
 * Released under the MIT license
 */
(function( factory ) {
	if ( typeof define === "function" && define.amd ) {
		define( ["jquery"], factory );
	} else {
		factory( jQuery );
	}
}(function( $ ) {

$.extend($.fn, {
	// http://jqueryvalidation.org/validate/
	validate: function( options ) {

		// if nothing is selected, return nothing; can't chain anyway
		if ( !this.length ) {
			if ( options && options.debug && window.console ) {
				console.warn( "Nothing selected, can't validate, returning nothing." );
			}
			return;
		}

		// check if a validator for this form was already created
		var validator = $.data( this[ 0 ], "validator" );
		if ( validator ) {
			return validator;
		}

		// Add novalidate tag if HTML5.
		this.attr( "novalidate", "novalidate" );

		validator = new $.validator( options, this[ 0 ] );
		$.data( this[ 0 ], "validator", validator );

		if ( validator.settings.onsubmit ) {

			this.validateDelegate( ":submit", "click", function( event ) {
				if ( validator.settings.submitHandler ) {
					validator.submitButton = event.target;
				}
				// allow suppressing validation by adding a cancel class to the submit button
				if ( $( event.target ).hasClass( "cancel" ) ) {
					validator.cancelSubmit = true;
				}

				// allow suppressing validation by adding the html5 formnovalidate attribute to the submit button
				if ( $( event.target ).attr( "formnovalidate" ) !== undefined ) {
					validator.cancelSubmit = true;
				}
			});

			// validate the form on submit
			this.submit( function( event ) {
				if ( validator.settings.debug ) {
					// prevent form submit to be able to see console output
					event.preventDefault();
				}
				function handle() {
					var hidden;
					if ( validator.settings.submitHandler ) {
						if ( validator.submitButton ) {
							// insert a hidden input as a replacement for the missing submit button
							hidden = $( "<input type='hidden'/>" )
								.attr( "name", validator.submitButton.name )
								.val( $( validator.submitButton ).val() )
								.appendTo( validator.currentForm );
						}
						validator.settings.submitHandler.call( validator, validator.currentForm, event );
						if ( validator.submitButton ) {
							// and clean up afterwards; thanks to no-block-scope, hidden can be referenced
							hidden.remove();
						}
						return false;
					}
					return true;
				}

				// prevent submit for invalid forms or custom submit handlers
				if ( validator.cancelSubmit ) {
					validator.cancelSubmit = false;
					return handle();
				}
				if ( validator.form() ) {
					if ( validator.pendingRequest ) {
						validator.formSubmitted = true;
						return false;
					}
					return handle();
				} else {
					validator.focusInvalid();
					return false;
				}
			});
		}

		return validator;
	},
	// http://jqueryvalidation.org/valid/
	valid: function() {
		var valid, validator;

		if ( $( this[ 0 ] ).is( "form" ) ) {
			valid = this.validate().form();
		} else {
			valid = true;
			validator = $( this[ 0 ].form ).validate();
			this.each( function() {
				valid = validator.element( this ) && valid;
			});
		}
		return valid;
	},
	// attributes: space separated list of attributes to retrieve and remove
	removeAttrs: function( attributes ) {
		var result = {},
			$element = this;
		$.each( attributes.split( /\s/ ), function( index, value ) {
			result[ value ] = $element.attr( value );
			$element.removeAttr( value );
		});
		return result;
	},
	// http://jqueryvalidation.org/rules/
	rules: function( command, argument ) {
		var element = this[ 0 ],
			settings, staticRules, existingRules, data, param, filtered;

		if ( command ) {
			settings = $.data( element.form, "validator" ).settings;
			staticRules = settings.rules;
			existingRules = $.validator.staticRules( element );
			switch ( command ) {
			case "add":
				$.extend( existingRules, $.validator.normalizeRule( argument ) );
				// remove messages from rules, but allow them to be set separately
				delete existingRules.messages;
				staticRules[ element.name ] = existingRules;
				if ( argument.messages ) {
					settings.messages[ element.name ] = $.extend( settings.messages[ element.name ], argument.messages );
				}
				break;
			case "remove":
				if ( !argument ) {
					delete staticRules[ element.name ];
					return existingRules;
				}
				filtered = {};
				$.each( argument.split( /\s/ ), function( index, method ) {
					filtered[ method ] = existingRules[ method ];
					delete existingRules[ method ];
					if ( method === "required" ) {
						$( element ).removeAttr( "aria-required" );
					}
				});
				return filtered;
			}
		}

		data = $.validator.normalizeRules(
		$.extend(
			{},
			$.validator.classRules( element ),
			$.validator.attributeRules( element ),
			$.validator.dataRules( element ),
			$.validator.staticRules( element )
		), element );

		// make sure required is at front
		if ( data.required ) {
			param = data.required;
			delete data.required;
			data = $.extend( { required: param }, data );
			$( element ).attr( "aria-required", "true" );
		}

		// make sure remote is at back
		if ( data.remote ) {
			param = data.remote;
			delete data.remote;
			data = $.extend( data, { remote: param });
		}

		return data;
	}
});

// Custom selectors
$.extend( $.expr[ ":" ], {
	// http://jqueryvalidation.org/blank-selector/
	blank: function( a ) {
		return !$.trim( "" + $( a ).val() );
	},
	// http://jqueryvalidation.org/filled-selector/
	filled: function( a ) {
		return !!$.trim( "" + $( a ).val() );
	},
	// http://jqueryvalidation.org/unchecked-selector/
	unchecked: function( a ) {
		return !$( a ).prop( "checked" );
	}
});

// constructor for validator
$.validator = function( options, form ) {
	this.settings = $.extend( true, {}, $.validator.defaults, options );
	this.currentForm = form;
	this.init();
};

// http://jqueryvalidation.org/jQuery.validator.format/
$.validator.format = function( source, params ) {
	if ( arguments.length === 1 ) {
		return function() {
			var args = $.makeArray( arguments );
			args.unshift( source );
			return $.validator.format.apply( this, args );
		};
	}
	if ( arguments.length > 2 && params.constructor !== Array  ) {
		params = $.makeArray( arguments ).slice( 1 );
	}
	if ( params.constructor !== Array ) {
		params = [ params ];
	}
	$.each( params, function( i, n ) {
		source = source.replace( new RegExp( "\\{" + i + "\\}", "g" ), function() {
			return n;
		});
	});
	return source;
};

$.extend( $.validator, {

	defaults: {
		messages: {},
		groups: {},
		rules: {},
		errorClass: "error",
		validClass: "valid",
		errorElement: "label",
		focusInvalid: true,
		errorContainer: $( [] ),
		errorLabelContainer: $( [] ),
		onsubmit: true,
		ignore: ":hidden",
		ignoreTitle: false,
		onfocusin: function( element ) {
			this.lastActive = element;

			// hide error label and remove error class on focus if enabled
			if ( this.settings.focusCleanup && !this.blockFocusCleanup ) {
				if ( this.settings.unhighlight ) {
					this.settings.unhighlight.call( this, element, this.settings.errorClass, this.settings.validClass );
				}
				this.hideThese( this.errorsFor( element ) );
			}
		},
		onfocusout: function( element ) {
			if ( !this.checkable( element ) && ( element.name in this.submitted || !this.optional( element ) ) ) {
				this.element( element );
			}
		},
		onkeyup: function( element, event ) {
			if ( event.which === 9 && this.elementValue( element ) === "" ) {
				return;
			} else if ( element.name in this.submitted || element === this.lastElement ) {
				this.element( element );
			}
		},
		onclick: function( element ) {
			// click on selects, radiobuttons and checkboxes
			if ( element.name in this.submitted ) {
				this.element( element );

			// or option elements, check parent select in that case
			} else if ( element.parentNode.name in this.submitted ) {
				this.element( element.parentNode );
			}
		},
		highlight: function( element, errorClass, validClass ) {
			if ( element.type === "radio" ) {
				this.findByName( element.name ).addClass( errorClass ).removeClass( validClass );
			} else {
				$( element ).addClass( errorClass ).removeClass( validClass );
			}
		},
		unhighlight: function( element, errorClass, validClass ) {
			if ( element.type === "radio" ) {
				this.findByName( element.name ).removeClass( errorClass ).addClass( validClass );
			} else {
				$( element ).removeClass( errorClass ).addClass( validClass );
			}
		}
	},

	// http://jqueryvalidation.org/jQuery.validator.setDefaults/
	setDefaults: function( settings ) {
		$.extend( $.validator.defaults, settings );
	},

	messages: {
		required: "This field is required.",
		remote: "Please fix this field.",
		email: "Please enter a valid email address.",
		url: "Please enter a valid URL.",
		date: "Please enter a valid date.",
		dateISO: "Please enter a valid date ( ISO ).",
		number: "Please enter a valid number.",
		digits: "Please enter only digits.",
		creditcard: "Please enter a valid credit card number.",
		equalTo: "Please enter the same value again.",
		maxlength: $.validator.format( "Please enter no more than {0} characters." ),
		minlength: $.validator.format( "Please enter at least {0} characters." ),
		rangelength: $.validator.format( "Please enter a value between {0} and {1} characters long." ),
		range: $.validator.format( "Please enter a value between {0} and {1}." ),
		max: $.validator.format( "Please enter a value less than or equal to {0}." ),
		min: $.validator.format( "Please enter a value greater than or equal to {0}." )
	},

	autoCreateRanges: false,

	prototype: {

		init: function() {
			this.labelContainer = $( this.settings.errorLabelContainer );
			this.errorContext = this.labelContainer.length && this.labelContainer || $( this.currentForm );
			this.containers = $( this.settings.errorContainer ).add( this.settings.errorLabelContainer );
			this.submitted = {};
			this.valueCache = {};
			this.pendingRequest = 0;
			this.pending = {};
			this.invalid = {};
			this.reset();

			var groups = ( this.groups = {} ),
				rules;
			$.each( this.settings.groups, function( key, value ) {
				if ( typeof value === "string" ) {
					value = value.split( /\s/ );
				}
				$.each( value, function( index, name ) {
					groups[ name ] = key;
				});
			});
			rules = this.settings.rules;
			$.each( rules, function( key, value ) {
				rules[ key ] = $.validator.normalizeRule( value );
			});

			function delegate( event ) {
				var validator = $.data( this[ 0 ].form, "validator" ),
					eventType = "on" + event.type.replace( /^validate/, "" ),
					settings = validator.settings;
				if ( settings[ eventType ] && !this.is( settings.ignore ) ) {
					settings[ eventType ].call( validator, this[ 0 ], event );
				}
			}
			$( this.currentForm )
				.validateDelegate( ":text, [type='password'], [type='file'], select, textarea, " +
					"[type='number'], [type='search'] ,[type='tel'], [type='url'], " +
					"[type='email'], [type='datetime'], [type='date'], [type='month'], " +
					"[type='week'], [type='time'], [type='datetime-local'], " +
					"[type='range'], [type='color'], [type='radio'], [type='checkbox']",
					"focusin focusout keyup", delegate)
				// Support: Chrome, oldIE
				// "select" is provided as event.target when clicking a option
				.validateDelegate("select, option, [type='radio'], [type='checkbox']", "click", delegate);

			if ( this.settings.invalidHandler ) {
				$( this.currentForm ).bind( "invalid-form.validate", this.settings.invalidHandler );
			}

			// Add aria-required to any Static/Data/Class required fields before first validation
			// Screen readers require this attribute to be present before the initial submission http://www.w3.org/TR/WCAG-TECHS/ARIA2.html
			$( this.currentForm ).find( "[required], [data-rule-required], .required" ).attr( "aria-required", "true" );
		},

		// http://jqueryvalidation.org/Validator.form/
		form: function() {
			this.checkForm();
			$.extend( this.submitted, this.errorMap );
			this.invalid = $.extend({}, this.errorMap );
			if ( !this.valid() ) {
				$( this.currentForm ).triggerHandler( "invalid-form", [ this ]);
			}
			this.showErrors();
			return this.valid();
		},

		checkForm: function() {
			this.prepareForm();
			for ( var i = 0, elements = ( this.currentElements = this.elements() ); elements[ i ]; i++ ) {
				this.check( elements[ i ] );
			}
			return this.valid();
		},

		// http://jqueryvalidation.org/Validator.element/
		element: function( element ) {
			var cleanElement = this.clean( element ),
				checkElement = this.validationTargetFor( cleanElement ),
				result = true;

			this.lastElement = checkElement;

			if ( checkElement === undefined ) {
				delete this.invalid[ cleanElement.name ];
			} else {
				this.prepareElement( checkElement );
				this.currentElements = $( checkElement );

				result = this.check( checkElement ) !== false;
				if ( result ) {
					delete this.invalid[ checkElement.name ];
				} else {
					this.invalid[ checkElement.name ] = true;
				}
			}
			// Add aria-invalid status for screen readers
			$( element ).attr( "aria-invalid", !result );

			if ( !this.numberOfInvalids() ) {
				// Hide error containers on last error
				this.toHide = this.toHide.add( this.containers );
			}
			this.showErrors();
			return result;
		},

		// http://jqueryvalidation.org/Validator.showErrors/
		showErrors: function( errors ) {
			if ( errors ) {
				// add items to error list and map
				$.extend( this.errorMap, errors );
				this.errorList = [];
				for ( var name in errors ) {
					this.errorList.push({
						message: errors[ name ],
						element: this.findByName( name )[ 0 ]
					});
				}
				// remove items from success list
				this.successList = $.grep( this.successList, function( element ) {
					return !( element.name in errors );
				});
			}
			if ( this.settings.showErrors ) {
				this.settings.showErrors.call( this, this.errorMap, this.errorList );
			} else {
				this.defaultShowErrors();
			}
		},

		// http://jqueryvalidation.org/Validator.resetForm/
		resetForm: function() {
			if ( $.fn.resetForm ) {
				$( this.currentForm ).resetForm();
			}
			this.submitted = {};
			this.lastElement = null;
			this.prepareForm();
			this.hideErrors();
			this.elements()
					.removeClass( this.settings.errorClass )
					.removeData( "previousValue" )
					.removeAttr( "aria-invalid" );
		},

		numberOfInvalids: function() {
			return this.objectLength( this.invalid );
		},

		objectLength: function( obj ) {
			/* jshint unused: false */
			var count = 0,
				i;
			for ( i in obj ) {
				count++;
			}
			return count;
		},

		hideErrors: function() {
			this.hideThese( this.toHide );
		},

		hideThese: function( errors ) {
			errors.not( this.containers ).text( "" );
			this.addWrapper( errors ).hide();
		},

		valid: function() {
			return this.size() === 0;
		},

		size: function() {
			return this.errorList.length;
		},

		focusInvalid: function() {
			if ( this.settings.focusInvalid ) {
				try {
					$( this.findLastActive() || this.errorList.length && this.errorList[ 0 ].element || [])
					.filter( ":visible" )
					.focus()
					// manually trigger focusin event; without it, focusin handler isn't called, findLastActive won't have anything to find
					.trigger( "focusin" );
				} catch ( e ) {
					// ignore IE throwing errors when focusing hidden elements
				}
			}
		},

		findLastActive: function() {
			var lastActive = this.lastActive;
			return lastActive && $.grep( this.errorList, function( n ) {
				return n.element.name === lastActive.name;
			}).length === 1 && lastActive;
		},

		elements: function() {
			var validator = this,
				rulesCache = {};

			// select all valid inputs inside the form (no submit or reset buttons)
			return $( this.currentForm )
			.find( "input, select, textarea" )
			.not( ":submit, :reset, :image, [disabled]" )
			.not( this.settings.ignore )
			.filter( function() {
				if ( !this.name && validator.settings.debug && window.console ) {
					console.error( "%o has no name assigned", this );
				}

				// select only the first element for each name, and only those with rules specified
				if ( this.name in rulesCache || !validator.objectLength( $( this ).rules() ) ) {
					return false;
				}

				rulesCache[ this.name ] = true;
				return true;
			});
		},

		clean: function( selector ) {
			return $( selector )[ 0 ];
		},

		errors: function() {
			var errorClass = this.settings.errorClass.split( " " ).join( "." );
			return $( this.settings.errorElement + "." + errorClass, this.errorContext );
		},

		reset: function() {
			this.successList = [];
			this.errorList = [];
			this.errorMap = {};
			this.toShow = $( [] );
			this.toHide = $( [] );
			this.currentElements = $( [] );
		},

		prepareForm: function() {
			this.reset();
			this.toHide = this.errors().add( this.containers );
		},

		prepareElement: function( element ) {
			this.reset();
			this.toHide = this.errorsFor( element );
		},

		elementValue: function( element ) {
			var val,
				$element = $( element ),
				type = element.type;

			if ( type === "radio" || type === "checkbox" ) {
				return $( "input[name='" + element.name + "']:checked" ).val();
			} else if ( type === "number" && typeof element.validity !== "undefined" ) {
				return element.validity.badInput ? false : $element.val();
			}

			val = $element.val();
			if ( typeof val === "string" ) {
				return val.replace(/\r/g, "" );
			}
			return val;
		},

		check: function( element ) {
			element = this.validationTargetFor( this.clean( element ) );

			var rules = $( element ).rules(),
				rulesCount = $.map( rules, function( n, i ) {
					return i;
				}).length,
				dependencyMismatch = false,
				val = this.elementValue( element ),
				result, method, rule;

			for ( method in rules ) {
				rule = { method: method, parameters: rules[ method ] };
				try {

					result = $.validator.methods[ method ].call( this, val, element, rule.parameters );

					// if a method indicates that the field is optional and therefore valid,
					// don't mark it as valid when there are no other rules
					if ( result === "dependency-mismatch" && rulesCount === 1 ) {
						dependencyMismatch = true;
						continue;
					}
					dependencyMismatch = false;

					if ( result === "pending" ) {
						this.toHide = this.toHide.not( this.errorsFor( element ) );
						return;
					}

					if ( !result ) {
						this.formatAndAdd( element, rule );
						return false;
					}
				} catch ( e ) {
					if ( this.settings.debug && window.console ) {
						console.log( "Exception occurred when checking element " + element.id + ", check the '" + rule.method + "' method.", e );
					}
					throw e;
				}
			}
			if ( dependencyMismatch ) {
				return;
			}
			if ( this.objectLength( rules ) ) {
				this.successList.push( element );
			}
			return true;
		},

		// return the custom message for the given element and validation method
		// specified in the element's HTML5 data attribute
		// return the generic message if present and no method specific message is present
		customDataMessage: function( element, method ) {
			return $( element ).data( "msg" + method.charAt( 0 ).toUpperCase() +
				method.substring( 1 ).toLowerCase() ) || $( element ).data( "msg" );
		},

		// return the custom message for the given element name and validation method
		customMessage: function( name, method ) {
			var m = this.settings.messages[ name ];
			return m && ( m.constructor === String ? m : m[ method ]);
		},

		// return the first defined argument, allowing empty strings
		findDefined: function() {
			for ( var i = 0; i < arguments.length; i++) {
				if ( arguments[ i ] !== undefined ) {
					return arguments[ i ];
				}
			}
			return undefined;
		},

		defaultMessage: function( element, method ) {
			return this.findDefined(
				this.customMessage( element.name, method ),
				this.customDataMessage( element, method ),
				// title is never undefined, so handle empty string as undefined
				!this.settings.ignoreTitle && element.title || undefined,
				$.validator.messages[ method ],
				"<strong>Warning: No message defined for " + element.name + "</strong>"
			);
		},

		formatAndAdd: function( element, rule ) {
			var message = this.defaultMessage( element, rule.method ),
				theregex = /\$?\{(\d+)\}/g;
			if ( typeof message === "function" ) {
				message = message.call( this, rule.parameters, element );
			} else if ( theregex.test( message ) ) {
				message = $.validator.format( message.replace( theregex, "{$1}" ), rule.parameters );
			}
			this.errorList.push({
				message: message,
				element: element,
				method: rule.method
			});

			this.errorMap[ element.name ] = message;
			this.submitted[ element.name ] = message;
		},

		addWrapper: function( toToggle ) {
			if ( this.settings.wrapper ) {
				toToggle = toToggle.add( toToggle.parent( this.settings.wrapper ) );
			}
			return toToggle;
		},

		defaultShowErrors: function() {
			var i, elements, error;
			for ( i = 0; this.errorList[ i ]; i++ ) {
				error = this.errorList[ i ];
				if ( this.settings.highlight ) {
					this.settings.highlight.call( this, error.element, this.settings.errorClass, this.settings.validClass );
				}
				this.showLabel( error.element, error.message );
			}
			if ( this.errorList.length ) {
				this.toShow = this.toShow.add( this.containers );
			}
			if ( this.settings.success ) {
				for ( i = 0; this.successList[ i ]; i++ ) {
					this.showLabel( this.successList[ i ] );
				}
			}
			if ( this.settings.unhighlight ) {
				for ( i = 0, elements = this.validElements(); elements[ i ]; i++ ) {
					this.settings.unhighlight.call( this, elements[ i ], this.settings.errorClass, this.settings.validClass );
				}
			}
			this.toHide = this.toHide.not( this.toShow );
			this.hideErrors();
			this.addWrapper( this.toShow ).show();
		},

		validElements: function() {
			return this.currentElements.not( this.invalidElements() );
		},

		invalidElements: function() {
			return $( this.errorList ).map(function() {
				return this.element;
			});
		},

		showLabel: function( element, message ) {
			var place, group, errorID,
				error = this.errorsFor( element ),
				elementID = this.idOrName( element ),
				describedBy = $( element ).attr( "aria-describedby" );
			if ( error.length ) {
				// refresh error/success class
				error.removeClass( this.settings.validClass ).addClass( this.settings.errorClass );
				// replace message on existing label
				error.html( message );
			} else {
				// create error element
				error = $( "<" + this.settings.errorElement + ">" )
					.attr( "id", elementID + "-error" )
					.addClass( this.settings.errorClass )
					.html( message || "" );

				// Maintain reference to the element to be placed into the DOM
				place = error;
				if ( this.settings.wrapper ) {
					// make sure the element is visible, even in IE
					// actually showing the wrapped element is handled elsewhere
					place = error.hide().show().wrap( "<" + this.settings.wrapper + "/>" ).parent();
				}
				if ( this.labelContainer.length ) {
					this.labelContainer.append( place );
				} else if ( this.settings.errorPlacement ) {
					this.settings.errorPlacement( place, $( element ) );
				} else {
					place.insertAfter( element );
				}

				// Link error back to the element
				if ( error.is( "label" ) ) {
					// If the error is a label, then associate using 'for'
					error.attr( "for", elementID );
				} else if ( error.parents( "label[for='" + elementID + "']" ).length === 0 ) {
					// If the element is not a child of an associated label, then it's necessary
					// to explicitly apply aria-describedby

					errorID = error.attr( "id" );
					// Respect existing non-error aria-describedby
					if ( !describedBy ) {
						describedBy = errorID;
					} else if ( !describedBy.match( new RegExp( "\b" + errorID + "\b" ) ) ) {
						// Add to end of list if not already present
						describedBy += " " + errorID;
					}
					$( element ).attr( "aria-describedby", describedBy );

					// If this element is grouped, then assign to all elements in the same group
					group = this.groups[ element.name ];
					if ( group ) {
						$.each( this.groups, function( name, testgroup ) {
							if ( testgroup === group ) {
								$( "[name='" + name + "']", this.currentForm )
									.attr( "aria-describedby", error.attr( "id" ) );
							}
						});
					}
				}
			}
			if ( !message && this.settings.success ) {
				error.text( "" );
				if ( typeof this.settings.success === "string" ) {
					error.addClass( this.settings.success );
				} else {
					this.settings.success( error, element );
				}
			}
			this.toShow = this.toShow.add( error );
		},

		errorsFor: function( element ) {
			var name = this.idOrName( element ),
				describer = $( element ).attr( "aria-describedby" ),
				selector = "label[for='" + name + "'], label[for='" + name + "'] *";
			// aria-describedby should directly reference the error element
			if ( describer ) {
				selector = selector + ", #" + describer.replace( /\s+/g, ", #" );
			}
			return this
				.errors()
				.filter( selector );
		},

		idOrName: function( element ) {
			return this.groups[ element.name ] || ( this.checkable( element ) ? element.name : element.id || element.name );
		},

		validationTargetFor: function( element ) {
			// if radio/checkbox, validate first element in group instead
			if ( this.checkable( element ) ) {
				element = this.findByName( element.name ).not( this.settings.ignore )[ 0 ];
			}
			return element;
		},

		checkable: function( element ) {
			return ( /radio|checkbox/i ).test( element.type );
		},

		findByName: function( name ) {
			return $( this.currentForm ).find( "[name='" + name + "']" );
		},

		getLength: function( value, element ) {
			switch ( element.nodeName.toLowerCase() ) {
			case "select":
				return $( "option:selected", element ).length;
			case "input":
				if ( this.checkable( element ) ) {
					return this.findByName( element.name ).filter( ":checked" ).length;
				}
			}
			return value.length;
		},

		depend: function( param, element ) {
			return this.dependTypes[typeof param] ? this.dependTypes[typeof param]( param, element ) : true;
		},

		dependTypes: {
			"boolean": function( param ) {
				return param;
			},
			"string": function( param, element ) {
				return !!$( param, element.form ).length;
			},
			"function": function( param, element ) {
				return param( element );
			}
		},

		optional: function( element ) {
			var val = this.elementValue( element );
			return !$.validator.methods.required.call( this, val, element ) && "dependency-mismatch";
		},

		startRequest: function( element ) {
			if ( !this.pending[ element.name ] ) {
				this.pendingRequest++;
				this.pending[ element.name ] = true;
			}
		},

		stopRequest: function( element, valid ) {
			this.pendingRequest--;
			// sometimes synchronization fails, make sure pendingRequest is never < 0
			if ( this.pendingRequest < 0 ) {
				this.pendingRequest = 0;
			}
			delete this.pending[ element.name ];
			if ( valid && this.pendingRequest === 0 && this.formSubmitted && this.form() ) {
				$( this.currentForm ).submit();
				this.formSubmitted = false;
			} else if (!valid && this.pendingRequest === 0 && this.formSubmitted ) {
				$( this.currentForm ).triggerHandler( "invalid-form", [ this ]);
				this.formSubmitted = false;
			}
		},

		previousValue: function( element ) {
			return $.data( element, "previousValue" ) || $.data( element, "previousValue", {
				old: null,
				valid: true,
				message: this.defaultMessage( element, "remote" )
			});
		}

	},

	classRuleSettings: {
		required: { required: true },
		email: { email: true },
		url: { url: true },
		date: { date: true },
		dateISO: { dateISO: true },
		number: { number: true },
		digits: { digits: true },
		creditcard: { creditcard: true }
	},

	addClassRules: function( className, rules ) {
		if ( className.constructor === String ) {
			this.classRuleSettings[ className ] = rules;
		} else {
			$.extend( this.classRuleSettings, className );
		}
	},

	classRules: function( element ) {
		var rules = {},
			classes = $( element ).attr( "class" );

		if ( classes ) {
			$.each( classes.split( " " ), function() {
				if ( this in $.validator.classRuleSettings ) {
					$.extend( rules, $.validator.classRuleSettings[ this ]);
				}
			});
		}
		return rules;
	},

	attributeRules: function( element ) {
		var rules = {},
			$element = $( element ),
			type = element.getAttribute( "type" ),
			method, value;

		for ( method in $.validator.methods ) {

			// support for <input required> in both html5 and older browsers
			if ( method === "required" ) {
				value = element.getAttribute( method );
				// Some browsers return an empty string for the required attribute
				// and non-HTML5 browsers might have required="" markup
				if ( value === "" ) {
					value = true;
				}
				// force non-HTML5 browsers to return bool
				value = !!value;
			} else {
				value = $element.attr( method );
			}

			// convert the value to a number for number inputs, and for text for backwards compability
			// allows type="date" and others to be compared as strings
			if ( /min|max/.test( method ) && ( type === null || /number|range|text/.test( type ) ) ) {
				value = Number( value );
			}

			if ( value || value === 0 ) {
				rules[ method ] = value;
			} else if ( type === method && type !== "range" ) {
				// exception: the jquery validate 'range' method
				// does not test for the html5 'range' type
				rules[ method ] = true;
			}
		}

		// maxlength may be returned as -1, 2147483647 ( IE ) and 524288 ( safari ) for text inputs
		if ( rules.maxlength && /-1|2147483647|524288/.test( rules.maxlength ) ) {
			delete rules.maxlength;
		}

		return rules;
	},

	dataRules: function( element ) {
		var method, value,
			rules = {}, $element = $( element );
		for ( method in $.validator.methods ) {
			value = $element.data( "rule" + method.charAt( 0 ).toUpperCase() + method.substring( 1 ).toLowerCase() );
			if ( value !== undefined ) {
				rules[ method ] = value;
			}
		}
		return rules;
	},

	staticRules: function( element ) {
		var rules = {},
			validator = $.data( element.form, "validator" );

		if ( validator.settings.rules ) {
			rules = $.validator.normalizeRule( validator.settings.rules[ element.name ] ) || {};
		}
		return rules;
	},

	normalizeRules: function( rules, element ) {
		// handle dependency check
		$.each( rules, function( prop, val ) {
			// ignore rule when param is explicitly false, eg. required:false
			if ( val === false ) {
				delete rules[ prop ];
				return;
			}
			if ( val.param || val.depends ) {
				var keepRule = true;
				switch ( typeof val.depends ) {
				case "string":
					keepRule = !!$( val.depends, element.form ).length;
					break;
				case "function":
					keepRule = val.depends.call( element, element );
					break;
				}
				if ( keepRule ) {
					rules[ prop ] = val.param !== undefined ? val.param : true;
				} else {
					delete rules[ prop ];
				}
			}
		});

		// evaluate parameters
		$.each( rules, function( rule, parameter ) {
			rules[ rule ] = $.isFunction( parameter ) ? parameter( element ) : parameter;
		});

		// clean number parameters
		$.each([ "minlength", "maxlength" ], function() {
			if ( rules[ this ] ) {
				rules[ this ] = Number( rules[ this ] );
			}
		});
		$.each([ "rangelength", "range" ], function() {
			var parts;
			if ( rules[ this ] ) {
				if ( $.isArray( rules[ this ] ) ) {
					rules[ this ] = [ Number( rules[ this ][ 0 ]), Number( rules[ this ][ 1 ] ) ];
				} else if ( typeof rules[ this ] === "string" ) {
					parts = rules[ this ].replace(/[\[\]]/g, "" ).split( /[\s,]+/ );
					rules[ this ] = [ Number( parts[ 0 ]), Number( parts[ 1 ] ) ];
				}
			}
		});

		if ( $.validator.autoCreateRanges ) {
			// auto-create ranges
			if ( rules.min && rules.max ) {
				rules.range = [ rules.min, rules.max ];
				delete rules.min;
				delete rules.max;
			}
			if ( rules.minlength && rules.maxlength ) {
				rules.rangelength = [ rules.minlength, rules.maxlength ];
				delete rules.minlength;
				delete rules.maxlength;
			}
		}

		return rules;
	},

	// Converts a simple string to a {string: true} rule, e.g., "required" to {required:true}
	normalizeRule: function( data ) {
		if ( typeof data === "string" ) {
			var transformed = {};
			$.each( data.split( /\s/ ), function() {
				transformed[ this ] = true;
			});
			data = transformed;
		}
		return data;
	},

	// http://jqueryvalidation.org/jQuery.validator.addMethod/
	addMethod: function( name, method, message ) {
		$.validator.methods[ name ] = method;
		$.validator.messages[ name ] = message !== undefined ? message : $.validator.messages[ name ];
		if ( method.length < 3 ) {
			$.validator.addClassRules( name, $.validator.normalizeRule( name ) );
		}
	},

	methods: {

		// http://jqueryvalidation.org/required-method/
		required: function( value, element, param ) {
			// check if dependency is met
			if ( !this.depend( param, element ) ) {
				return "dependency-mismatch";
			}
			if ( element.nodeName.toLowerCase() === "select" ) {
				// could be an array for select-multiple or a string, both are fine this way
				var val = $( element ).val();
				return val && val.length > 0;
			}
			if ( this.checkable( element ) ) {
				return this.getLength( value, element ) > 0;
			}
			return $.trim( value ).length > 0;
		},

		// http://jqueryvalidation.org/email-method/
		email: function( value, element ) {
			// From http://www.whatwg.org/specs/web-apps/current-work/multipage/states-of-the-type-attribute.html#e-mail-state-%28type=email%29
			// Retrieved 2014-01-14
			// If you have a problem with this implementation, report a bug against the above spec
			// Or use custom methods to implement your own email validation
			return this.optional( element ) || /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/.test( value );
		},

		// http://jqueryvalidation.org/url-method/
		url: function( value, element ) {
			// contributed by Scott Gonzalez: http://projects.scottsplayground.com/iri/
			return this.optional( element ) || /^(https?|s?ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test( value );
		},

		// http://jqueryvalidation.org/date-method/
		date: function( value, element ) {
			return this.optional( element ) || !/Invalid|NaN/.test( new Date( value ).toString() );
		},

		// http://jqueryvalidation.org/dateISO-method/
		dateISO: function( value, element ) {
			return this.optional( element ) || /^\d{4}[\/\-](0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])$/.test( value );
		},

		// http://jqueryvalidation.org/number-method/
		number: function( value, element ) {
			return this.optional( element ) || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test( value );
		},

		// http://jqueryvalidation.org/digits-method/
		digits: function( value, element ) {
			return this.optional( element ) || /^\d+$/.test( value );
		},

		// http://jqueryvalidation.org/creditcard-method/
		// based on http://en.wikipedia.org/wiki/Luhn/
		creditcard: function( value, element ) {
			if ( this.optional( element ) ) {
				return "dependency-mismatch";
			}
			// accept only spaces, digits and dashes
			if ( /[^0-9 \-]+/.test( value ) ) {
				return false;
			}
			var nCheck = 0,
				nDigit = 0,
				bEven = false,
				n, cDigit;

			value = value.replace( /\D/g, "" );

			// Basing min and max length on
			// http://developer.ean.com/general_info/Valid_Credit_Card_Types
			if ( value.length < 13 || value.length > 19 ) {
				return false;
			}

			for ( n = value.length - 1; n >= 0; n--) {
				cDigit = value.charAt( n );
				nDigit = parseInt( cDigit, 10 );
				if ( bEven ) {
					if ( ( nDigit *= 2 ) > 9 ) {
						nDigit -= 9;
					}
				}
				nCheck += nDigit;
				bEven = !bEven;
			}

			return ( nCheck % 10 ) === 0;
		},

		// http://jqueryvalidation.org/minlength-method/
		minlength: function( value, element, param ) {
			var length = $.isArray( value ) ? value.length : this.getLength( $.trim( value ), element );
			return this.optional( element ) || length >= param;
		},

		// http://jqueryvalidation.org/maxlength-method/
		maxlength: function( value, element, param ) {
			var length = $.isArray( value ) ? value.length : this.getLength( $.trim( value ), element );
			return this.optional( element ) || length <= param;
		},

		// http://jqueryvalidation.org/rangelength-method/
		rangelength: function( value, element, param ) {
			var length = $.isArray( value ) ? value.length : this.getLength( $.trim( value ), element );
			return this.optional( element ) || ( length >= param[ 0 ] && length <= param[ 1 ] );
		},

		// http://jqueryvalidation.org/min-method/
		min: function( value, element, param ) {
			return this.optional( element ) || value >= param;
		},

		// http://jqueryvalidation.org/max-method/
		max: function( value, element, param ) {
			return this.optional( element ) || value <= param;
		},

		// http://jqueryvalidation.org/range-method/
		range: function( value, element, param ) {
			return this.optional( element ) || ( value >= param[ 0 ] && value <= param[ 1 ] );
		},

		// http://jqueryvalidation.org/equalTo-method/
		equalTo: function( value, element, param ) {
			// bind to the blur event of the target in order to revalidate whenever the target field is updated
			// TODO find a way to bind the event just once, avoiding the unbind-rebind overhead
			var target = $( param );
			if ( this.settings.onfocusout ) {
				target.unbind( ".validate-equalTo" ).bind( "blur.validate-equalTo", function() {
					$( element ).valid();
				});
			}
			return value === target.val();
		},

		// http://jqueryvalidation.org/remote-method/
		remote: function( value, element, param ) {
			if ( this.optional( element ) ) {
				return "dependency-mismatch";
			}

			var previous = this.previousValue( element ),
				validator, data;

			if (!this.settings.messages[ element.name ] ) {
				this.settings.messages[ element.name ] = {};
			}
			previous.originalMessage = this.settings.messages[ element.name ].remote;
			this.settings.messages[ element.name ].remote = previous.message;

			param = typeof param === "string" && { url: param } || param;

			if ( previous.old === value ) {
				return previous.valid;
			}

			previous.old = value;
			validator = this;
			this.startRequest( element );
			data = {};
			data[ element.name ] = value;
			$.ajax( $.extend( true, {
				url: param,
				mode: "abort",
				port: "validate" + element.name,
				dataType: "json",
				data: data,
				context: validator.currentForm,
				success: function( response ) {
					var valid = response === true || response === "true",
						errors, message, submitted;

					validator.settings.messages[ element.name ].remote = previous.originalMessage;
					if ( valid ) {
						submitted = validator.formSubmitted;
						validator.prepareElement( element );
						validator.formSubmitted = submitted;
						validator.successList.push( element );
						delete validator.invalid[ element.name ];
						validator.showErrors();
					} else {
						errors = {};
						message = response || validator.defaultMessage( element, "remote" );
						errors[ element.name ] = previous.message = $.isFunction( message ) ? message( value ) : message;
						validator.invalid[ element.name ] = true;
						validator.showErrors( errors );
					}
					previous.valid = valid;
					validator.stopRequest( element, valid );
				}
			}, param ) );
			return "pending";
		}

	}

});

$.format = function deprecated() {
	throw "$.format has been deprecated. Please use $.validator.format instead.";
};

// ajax mode: abort
// usage: $.ajax({ mode: "abort"[, port: "uniqueport"]});
// if mode:"abort" is used, the previous request on that port (port can be undefined) is aborted via XMLHttpRequest.abort()

var pendingRequests = {},
	ajax;
// Use a prefilter if available (1.5+)
if ( $.ajaxPrefilter ) {
	$.ajaxPrefilter(function( settings, _, xhr ) {
		var port = settings.port;
		if ( settings.mode === "abort" ) {
			if ( pendingRequests[port] ) {
				pendingRequests[port].abort();
			}
			pendingRequests[port] = xhr;
		}
	});
} else {
	// Proxy ajax
	ajax = $.ajax;
	$.ajax = function( settings ) {
		var mode = ( "mode" in settings ? settings : $.ajaxSettings ).mode,
			port = ( "port" in settings ? settings : $.ajaxSettings ).port;
		if ( mode === "abort" ) {
			if ( pendingRequests[port] ) {
				pendingRequests[port].abort();
			}
			pendingRequests[port] = ajax.apply(this, arguments);
			return pendingRequests[port];
		}
		return ajax.apply(this, arguments);
	};
}

// provides delegate(type: String, delegate: Selector, handler: Callback) plugin for easier event delegation
// handler is only called when $(event.target).is(delegate), in the scope of the jquery-object for event.target

$.extend($.fn, {
	validateDelegate: function( delegate, type, handler ) {
		return this.bind(type, function( event ) {
			var target = $(event.target);
			if ( target.is(delegate) ) {
				return handler.apply(target, arguments);
			}
		});
	}
});

}));
(function($) {

    $.fn.slider = function(options) {

            var opts = $.extend({}, $.fn.slider.defaults, options);
            var showSize = opts.showSize;
            var pageNum = opts.curPage;
            return this.each(function() {
                var scrollTimer;
                var $slider = $(this);
                var $sliderView = $slider.find(".arrow-slider");
                var $sliderItem = $slider.find("li");
                var $sliderUl = $slider.find("ul:first");
                var len = $sliderUl.find("li").size();
                var mo = len % opts.offsetSize;
                var num = opts.offsetSize - mo;
                //不足1页时自动补足
                if (mo != 0 || len < opts.showSize) {
                    $sliderUl.find("li:lt(" + num + ")").clone().appendTo($sliderUl);
                }

                if (opts.offsetSize > len) {
                    $('<div class="slider-error-tips">请查看相关API</div>').appendTo($slider);
                }
                var totalSize = $sliderUl.find("li").size();
                if (totalSize <= 1) {
                    opts.auto = false;
                }
                var width = ($sliderItem.width() + opts.itemPadding) * showSize - opts.itemPadding;

                $sliderUl.find("li").css({
                    "padding-right": opts.itemPadding
                }).each(function(index, ele) {
                    $(ele).attr("data-item", "item" + index);
                });
                $sliderUl.find("li:first").addClass("cur");
                $sliderUl.width(($sliderItem.width() + opts.itemPadding) * totalSize).height($sliderItem.height());
                $sliderView.height($sliderItem.height()).width(width);
                $slider.width(width);


                //显示类型
                $slider.addClass("slider-" + opts.type);

                //总数及页数
                var $currentPage;

                if (opts.offsetSize == opts.showSize && opts.showSize == 1 && opts.showType) {

                    $('<div class="slider-total"><span class="slider-current-page">' + pageNum + '</span>/<span class="slider-items">' + len + '</span></div>').appendTo($slider);
                    if (opts.showAmount) {
                        $(".slider-total").css({
                            "display": "display"
                        });
                    } else {
                        $(".slider-total").css({
                            "display": "none"
                        });
                    }

                    //图片说明性文案
                    var showAlt = function() {
                        $.each($sliderUl.find("li"), function(key, slide) {
                            var caption = $(slide).find('img').attr('alt');
                            if (caption && opts.alt) {
                                caption = $('<div class="slider-tips">' + caption + '</div>');
                                caption.appendTo($(slide));
                            }
                        });
                    }();

                    var markers = function() {
                        var marker = [];
                        marker.push('<ul class="slider-markers">');
                        for (var i = 0; i < len; i++) {
                            marker.push('<li>' + (i + 1) + '</li>');
                        }
                        marker.push('</ul>');
                        $slider.append(marker.join(''));
                        $($slider.find(".slider-markers li").eq(opts.curPage - 1)).addClass("active-marker");

                        $slider.find(".slider-markers").delegate("li", "mouseover", function() {
                            clearInterval(scrollTimer);
                        })
                        $slider.find(".slider-markers").delegate("li", "click", function() {
                            var index = $(this).index(); // 顺序排位置
                            if (!$sliderUl.is(":animated") && index != "undefind") {
                                var ind = $sliderUl.find("li[data-item$=" + index + "]").index(); // 目标元素当前dom中的位置
                                var curInd = parseInt($sliderUl.find("li").eq(0).data("item").match(/\d+/)[0]); // 当前显示的dom
                                if (index === curInd) {
                                    return;
                                }
                                if (index > curInd) {
                                    $sliderUl.stop().animate({
                                        left: '-=' + $sliderItem.outerWidth(true) * ind
                                    }, opts.speed, function() {
                                        $sliderUl.css({
                                            "left": 0
                                        }).find("li").slice(0, ind).appendTo($sliderUl);
                                        $sliderUl.find("li").removeClass("cur");
                                        $sliderUl.find("li:first").addClass("cur");
                                    });
                                } else {
                                    var $toBeMoved = $sliderUl.find("li").slice(ind, len),
                                        dif = curInd - index;
                                    $toBeMoved.prependTo($sliderUl);
                                    $sliderUl.css({
                                        "left": "-=" + $sliderItem.outerWidth(true) * $toBeMoved.length
                                    });
                                    $sliderUl.stop().animate({
                                        "left": 0
                                    }, opts.speed, function() {
                                        $sliderUl.find("li").removeClass("cur");
                                        $sliderUl.find("li:first").addClass("cur");
                                    });
                                }
                            }
                            $(this).siblings().removeClass("active-marker").end().addClass("active-marker");
                            $slider.find(".slider-current-page").text(index + 1);
                            pageNum = parseInt($slider.find(".slider-current-page").text());
                        })

                        $slider.find(".slider-markers").delegate("li", "mouseout", function() {
                            if (!opts.auto) {
                                clearInterval(scrollTimer);
                            } else {
                                scrollTimer = setInterval(function() {
                                    sliderBox($sliderView);
                                }, opts.interval);
                            }
                        });
                    }();
                }

                function showArrow() {
                    var showArrowMarkup = '<div class="arrow-slider-left" id="J_imgLeft">left</div>' + '<div class="arrow-slider-right" id="J_imgRight">right</div>';
                    $(showArrowMarkup).appendTo($slider);
                    var $prevBtn = $slider.find(".arrow-slider-left");
                    var $nextBtn = $slider.find(".arrow-slider-right");

                    var arrowTop = parseInt(($sliderItem.height() - $prevBtn.height()) / 2);
                    $prevBtn.css({
                        "top": arrowTop,
                        "left": $prevBtn.width() / 3
                    });
                    $nextBtn.css({
                        "top": arrowTop,
                        "right": $prevBtn.width() / 3
                    });

                    //绑定mouseover
                    $prevBtn.bind("mouseover", function() {
                        $(this).addClass("arrow-slider-left-hover");
                        clearInterval(scrollTimer);
                    }).bind("mouseout", function() {
                        $(this).removeClass("arrow-slider-left-hover");
                        if (!opts.auto) {
                            clearInterval(scrollTimer);
                        } else {
                            scrollTimer = setInterval(function() {
                                sliderBox($sliderView);
                            }, opts.interval);
                        }

                    });

                    //绑定mouseover
                    $nextBtn.bind("mouseover", function() {
                        $(this).addClass("arrow-slider-right-hover");
                        clearInterval(scrollTimer);
                    }).bind("mouseout", function() {
                        $(this).removeClass("arrow-slider-right-hover");
                        if (!opts.auto) {
                            clearInterval(scrollTimer);
                        } else {
                            scrollTimer = setInterval(function() {
                                sliderBox($sliderView);
                            }, opts.interval);
                        }
                    });

                    //向左 按钮 向右移动
                    $prevBtn.click(function() {
                        if (!$sliderUl.is(":animated")) {
                            $sliderUl.find("li").slice(len - opts.offsetSize, len).insertBefore($sliderUl.find("li:first"));
                            $sliderUl.css({
                                left: '-=' + $sliderItem.outerWidth(true) * opts.offsetSize
                            });
                            $sliderUl.animate({
                                left: '+=' + $sliderItem.outerWidth(true) * opts.offsetSize
                            }, opts.speed, function() {
                                $sliderUl.css({
                                    "left": 0
                                });
                            });
                            if ($slider.find(".slider-current-page").size() > 0) {
                                $currentPage = $slider.find(".slider-current-page");
                                if (parseInt($currentPage.text()) == 1) {
                                    pageNum = Math.ceil(totalSize / showSize);
                                    $currentPage.text(pageNum--);
                                } else {
                                    $currentPage.text(parseInt($currentPage.text()) - 1);
                                }

                                pageNum = $currentPage.text();
                                //当前显示数字
                                var markersItem = $slider.find("ul:last").find("li");
                                markersItem.removeClass("active-marker");
                                $(markersItem.eq($currentPage.text() - 1)).addClass("active-marker");
                            }

                        }
                    });

                    //往右 按钮 向左移动
                    $nextBtn.click(function() {
                        if (!$sliderUl.is(":animated")) {
                            $sliderUl.animate({
                                left: '-=' + $sliderItem.outerWidth(true) * opts.offsetSize
                            }, opts.speed, function() {
                                $sliderUl.css({
                                    "left": 0
                                }).find("li:lt(" + opts.offsetSize + ")").appendTo($sliderUl);
                            });

                            if ($slider.find(".slider-current-page").size() > 0) {
                                $currentPage = $slider.find(".slider-current-page");
                                if (parseInt($currentPage.text()) == Math.ceil(totalSize / showSize)) {
                                    pageNum = 0;
                                    $currentPage.text(++pageNum);
                                } else {
                                    $currentPage.text(1 + parseInt($currentPage.text()));
                                }
                                pageNum = $currentPage.text();
                                //当前显示数字
                                var markersItem = $slider.find("ul:last").find("li");
                                markersItem.removeClass("active-marker");
                                $(markersItem.eq($currentPage.text() - 1)).addClass("active-marker");
                            }
                        }
                    });
                }

                //默认显示第几页
                $sliderUl.animate({
                    left: '-=' + $sliderItem.outerWidth(true) * opts.offsetSize * (opts.curPage - 1)
                }, 0, function() {
                    $sliderUl.css({
                        "left": 0
                    }).find("li:lt(" + opts.offsetSize * (opts.curPage - 1) + ")").appendTo($sliderUl);
                });

                //轮播
                $sliderView.hover(function() {
                    clearInterval(scrollTimer);
                }, function() {
                    if (!opts.auto) {
                        clearInterval(scrollTimer);
                    } else {
                        scrollTimer = setInterval(function() {
                            sliderBox($sliderView);
                        }, opts.interval);
                    }
                });

                //是否显示箭头
                if (opts.showArrow) {
                    showArrow();
                    //只有一页时取消绑定事件
                    if (len == opts.offsetSize || len == opts.showSize) {
                        opts.auto = false;
                        $slider.find(".arrow-slider-left").unbind("click").unbind("mouseover");
                        $slider.find(".arrow-slider-right").unbind("click").unbind("mouseover");
                    }
                } else {
                    if (opts.noArrow) return;
                    // 悬停显示
                    $slider.hover(function() {
                        if ($(".arrow-slider-left").length == 0) {
                            showArrow();
                        }
                    }, function() {
                        $(".arrow-slider-left").remove();
                        $(".arrow-slider-right").remove();
                    });
                }



                //是否自动
                if (opts.auto) {
                    $sliderView.trigger("mouseleave");
                }

                //私有函数
                function sliderBox(obj) {
                    var $self = obj.find("ul:first");

                    if (opts.direction == "left") {
                        $self.animate({
                            left: '-=' + $sliderItem.outerWidth(true) * opts.offsetSize
                        }, opts.speed, function() {
                            $self.css({
                                "left": 0
                            }).find("li:lt(" + opts.offsetSize + ")").appendTo($self);
                        });
                        if (pageNum == Math.ceil(totalSize / showSize)) {
                            pageNum = 0;
                        }
                        if ($slider.find(".slider-current-page").size() > 0) {
                            $slider.find(".slider-current-page").text(++pageNum);
                        }
                    } else {
                        $sliderUl.find("li").slice(len - opts.offsetSize, len).insertBefore($sliderUl.find("li:first"));
                        $sliderUl.css({
                            left: '-=' + $sliderItem.outerWidth(true) * opts.offsetSize
                        });
                        $sliderUl.animate({
                            left: '+=' + $sliderItem.outerWidth(true) * opts.offsetSize
                        }, opts.speed, function() {
                            $sliderUl.css({
                                "left": 0
                            });
                        });

                        if ($slider.find(".slider-current-page").size() > 0) {
                            $currentPage = $slider.find(".slider-current-page");
                            if ($currentPage.text() == 1) {
                                pageNum = Math.ceil(totalSize / showSize);
                            }
                            $currentPage.text(pageNum--);
                        }
                    }
                    var markersItem = $slider.find("ul:last").find("li");
                    markersItem.removeClass("active-marker");
                    $(markersItem.eq(pageNum - 1)).addClass("active-marker");
                }
            });
        }

    // 暴露插件的默认配置
    $.fn.slider.defaults = {
        offsetSize: 1,
        showSize: 1,
        itemPadding: 0,
        auto: true,
        curPage: 1,
        showArrow: true,
        noArrow: false, // added by cd, plz contact fangbin
        speed: 600,
        interval: 5000,
        direction: "left",
        alt: false,
        type: "rectangle", // dot/figure
        showAmount: false,
        showType: true
    };

})(jQuery);
/*
 * Lazy Load - jQuery plugin for lazy loading images
 *
 * Copyright (c) 2007-2013 Mika Tuupola
 *
 * Licensed under the MIT license:
 *   http://www.opensource.org/licenses/mit-license.php
 *
 * Project home:
 *   http://www.appelsiini.net/projects/lazyload
 *
 * Version:  1.9.3
 *
 */

(function($, window, document, undefined) {
    var $window = $(window);

    $.fn.lazyload = function(options) {
        var elements = this;
        var $container;
        var settings = {
            threshold       : 0,
            failure_limit   : 0,
            event           : "scroll",
            effect          : "show",
            container       : window,
            data_attribute  : "original",
            skip_invisible  : true,
            appear          : null,
            load            : null,
            placeholder     : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAANSURBVBhXYzh8+PB/AAffA0nNPuCLAAAAAElFTkSuQmCC"
        };

        function update() {
            var counter = 0;

            elements.each(function() {
                var $this = $(this);
                if (settings.skip_invisible && !$this.is(":visible")) {
                    return;
                }
                if ($.abovethetop(this, settings) ||
                    $.leftofbegin(this, settings)) {
                        /* Nothing. */
                } else if (!$.belowthefold(this, settings) &&
                    !$.rightoffold(this, settings)) {
                        $this.trigger("appear");
                        /* if we found an image we'll load, reset the counter */
                        counter = 0;
                } else {
                    if (++counter > settings.failure_limit) {
                        return false;
                    }
                }
            });

        }

        if(options) {
            /* Maintain BC for a couple of versions. */
            if (undefined !== options.failurelimit) {
                options.failure_limit = options.failurelimit;
                delete options.failurelimit;
            }
            if (undefined !== options.effectspeed) {
                options.effect_speed = options.effectspeed;
                delete options.effectspeed;
            }

            $.extend(settings, options);
        }

        /* Cache container as jQuery as object. */
        $container = (settings.container === undefined ||
                      settings.container === window) ? $window : $(settings.container);

        /* Fire one scroll event per scroll. Not one scroll event per image. */
        if (0 === settings.event.indexOf("scroll")) {
            $container.bind(settings.event, function() {
                return update();
            });
        }

        this.each(function() {
            var self = this;
            var $self = $(self);

            self.loaded = false;

            /* If no src attribute given use data:uri. */
            if ($self.attr("src") === undefined || $self.attr("src") === false) {
                if ($self.is("img")) {
                    $self.attr("src", settings.placeholder);
                }
            }

            /* When appear is triggered load original image. */
            $self.one("appear", function() {
                if (!this.loaded) {
                    if (settings.appear) {
                        var elements_left = elements.length;
                        settings.appear.call(self, elements_left, settings);
                    }
                    $("<img />")
                        .bind("load", function() {

                            var original = $self.attr("data-" + settings.data_attribute);
                            $self.hide();
                            if ($self.is("img")) {
                                $self.attr("src", original);
                            } else {
                                $self.css("background-image", "url('" + original + "')");
                            }
                            $self[settings.effect](settings.effect_speed);

                            self.loaded = true;

                            /* Remove image from array so it is not looped next time. */
                            var temp = $.grep(elements, function(element) {
                                return !element.loaded;
                            });
                            elements = $(temp);

                            if (settings.load) {
                                var elements_left = elements.length;
                                settings.load.call(self, elements_left, settings);
                            }
                        })
                        .attr("src", $self.attr("data-" + settings.data_attribute));
                }
            });

            /* When wanted event is triggered load original image */
            /* by triggering appear.                              */
            if (0 !== settings.event.indexOf("scroll")) {
                $self.bind(settings.event, function() {
                    if (!self.loaded) {
                        $self.trigger("appear");
                    }
                });
            }
        });

        /* Check if something appears when window is resized. */
        $window.bind("resize", function() {
            update();
        });

        /* With IOS5 force loading images when navigating with back button. */
        /* Non optimal workaround. */
        if ((/(?:iphone|ipod|ipad).*os 5/gi).test(navigator.appVersion)) {
            $window.bind("pageshow", function(event) {
                if (event.originalEvent && event.originalEvent.persisted) {
                    elements.each(function() {
                        $(this).trigger("appear");
                    });
                }
            });
        }

        /* Force initial check if images should appear. */
        $(document).ready(function() {
            update();
        });

        return this;
    };

    /* Convenience methods in jQuery namespace.           */
    /* Use as  $.belowthefold(element, {threshold : 100, container : window}) */

    $.belowthefold = function(element, settings) {
        var fold;

        if (settings.container === undefined || settings.container === window) {
            fold = (window.innerHeight ? window.innerHeight : $window.height()) + $window.scrollTop();
        } else {
            fold = $(settings.container).offset().top + $(settings.container).height();
        }

        return fold <= $(element).offset().top - settings.threshold;
    };

    $.rightoffold = function(element, settings) {
        var fold;

        if (settings.container === undefined || settings.container === window) {
            fold = $window.width() + $window.scrollLeft();
        } else {
            fold = $(settings.container).offset().left + $(settings.container).width();
        }

        return fold <= $(element).offset().left - settings.threshold;
    };

    $.abovethetop = function(element, settings) {
        var fold;

        if (settings.container === undefined || settings.container === window) {
            fold = $window.scrollTop();
        } else {
            fold = $(settings.container).offset().top;
        }

        return fold >= $(element).offset().top + settings.threshold  + $(element).height();
    };

    $.leftofbegin = function(element, settings) {
        var fold;

        if (settings.container === undefined || settings.container === window) {
            fold = $window.scrollLeft();
        } else {
            fold = $(settings.container).offset().left;
        }

        return fold >= $(element).offset().left + settings.threshold + $(element).width();
    };

    $.inviewport = function(element, settings) {
         return !$.rightoffold(element, settings) && !$.leftofbegin(element, settings) &&
                !$.belowthefold(element, settings) && !$.abovethetop(element, settings);
     };

    /* Custom selectors for your convenience.   */
    /* Use as $("img:below-the-fold").something() or */
    /* $("img").filter(":below-the-fold").something() which is faster */

    $.extend($.expr[":"], {
        "below-the-fold" : function(a) { return $.belowthefold(a, {threshold : 0}); },
        "above-the-top"  : function(a) { return !$.belowthefold(a, {threshold : 0}); },
        "right-of-screen": function(a) { return $.rightoffold(a, {threshold : 0}); },
        "left-of-screen" : function(a) { return !$.rightoffold(a, {threshold : 0}); },
        "in-viewport"    : function(a) { return $.inviewport(a, {threshold : 0}); },
        /* Maintain BC for couple of versions. */
        "above-the-fold" : function(a) { return !$.belowthefold(a, {threshold : 0}); },
        "right-of-fold"  : function(a) { return $.rightoffold(a, {threshold : 0}); },
        "left-of-fold"   : function(a) { return !$.rightoffold(a, {threshold : 0}); }
    });

})(jQuery, window, document);

/*

	 _____                              ____                                   __
	/\___ \                            /\  _`\                    __          /\ \__
	\/__/\ \     __     __  __     __  \ \,\L\_\     ___   _ __  /\_\   _____ \ \ ,_\
	   _\ \ \  /'__`\  /\ \/\ \  /'__`\ \/_\__ \    /'___\/\`'__\\/\ \ /\ '__`\\ \ \/
	  /\ \_\ \/\ \L\.\_\ \ \_/ |/\ \L\.\_ /\ \L\ \ /\ \__/\ \ \/  \ \ \\ \ \L\ \\ \ \_
	  \ \____/\ \__/.\_\\ \___/ \ \__/.\_\\ `\____\\ \____\\ \_\   \ \_\\ \ ,__/ \ \__\
	   \/___/  \/__/\/_/ \/__/   \/__/\/_/ \/_____/ \/____/ \/_/    \/_/ \ \ \/   \/__/
	                                                                      \ \_\
	                                                                       \/_/

 	Statement: ...//TODO: Write statement.

 	Describe: Script for index page.

 	Further Changes, Comments: ...//TODO: Give a further changes and comments link.

 	Javascript Design Pattern (Code Management):    ...//TODO: Cehck design pattern.

 	    Modules Patterns, Object literal notation

 	Docs: ...//TODO: Give a link about project documents.

 	Original Author: Shen Weizhong ( Tony Stark )

		Cell Phone: (+86) 15921299022

		Github：//github.com/Tony-Stark/

		Trello: //trello.com/shenweizhong/

		个人全球统一标识（ Gravatar ）：//en.gravatar.com/swzcowboy/

		个人主页（ Personal Homepage ）：//iTonyYo.github.io/

		Linkedin：//cn.linkedin.com/in/itonyyo/

		Twitter: @iTonyYo, //twitter.com/iTonyYo/

		Instagram：//instagram.com/itonyyo/

		Facebook：//www.facebook.com/shenweizhong/

		Google+: //plus.google.com/114960355664861539339/

		Instagram: //instagram.com/itonyyo/

		QQ：563214029, //user.qzone.qq.com/563214029/

		Sina Weibo: //weibo.com/itonyyo/

		WhatsApp：15921299022

		微信（ WeChat ）：iTonyYo

		Facebook Messenger：shenweizhong

		Skype：live:swzyocowboy

		Line：shenweizhong

		Email: swzyocowboy@icloud.com, swzyocowboy@hotmail.com, iTonyYo@gmail.com, itonyyo@vip.qq.com, shenweizhong@facebook.com

 	Thanks: ...//TODO: If there are some contributors, just mark them.

 	Version: 0.1.0-alpha

	Creation Date: 2014.10.13 10:17 AM ( Tony ).

	Last Update: 2014.10.16 15:38 PM ( Tony ).    ...//TODO: Update the 'Last Update'.

 	Music ( Custom ): Fireproof.mp3

 	License: ...//TODO: Give a license.

 	Copyright: ...//TODO: Give a copyright.

*/

(function ($, window, ECar) {

	var fns;

	fns = {

		/*
				 (__)
				 (oo)
		  /-------\/
		 / |     ||----> Options.
		*  ||----||
		  ___/  ___/
		*/
		config: {},

		/*
				 (__)
				 (oo)
		  /-------\/
		 / |     ||----> Script for initialization.
		*  ||----||
		  ___/  ___/
		*/
		init: function () {

			var _body;

			_body = $('body');

			this._effect.caps();

			this._effect.mouseOffset();

			this._set.docScrollBar();

			this._set.topAd();

			this._set.mixture();

			this._set.slider();

			this._set.tab();

			this._set.caption();

			this._set.imgOffset();

			this._set.imgLazyLoad();

			this._set.monitor(_body);

			this._set.im.init();

			this._set.slt();

			this._set.sltOptSrl();

			this._set._form.init();

		},

		/*
				 (__)
				 (oo)
		  /-------\/
		 / |     ||----> Utils.
		*  ||----||
		  ___/  ___/
		*/
		helpers: {

			pdControl: function (e) {

				e.stopPropagation();

				e.preventDefault();

			},

			clickOrTouch: function (e) {

				return Modernizr.touch ? 'touchstart' : 'click';

			},

			getQueryString: function (url) {

				return url.substring(url.indexOf("?")+1);

			},

			Arg: function (name, url) {

				var parameters, pos, _key, _val;

				parameters = this.getQueryString(url).split("&");

				for(var i = 0; i < parameters.length; i++)  {

					pos = parameters[i].indexOf('=');

					if (pos == -1) {

						continue;

					}

					_key = parameters[i].substring(0, pos);

					_val = parameters[i].substring(pos + 1);

					if(_key == name) {

						return unescape(_val.replace(/\+/g, " "));

					}

				}

			}

		},

		_effect: {

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for defining caption effect.
			*  ||----||
			  ___/  ___/
			*/
			caps: function () {

				$.fn.showCap = function(heightEnd, hightBegin) {

					var $picWindow;

					$picWindow = this;

					$picWindow.on("mouseover", function() {

						var cap;

						cap = $(this).find(".gallery-banner");

						cap.stop().animate({

							height: heightEnd

						}, 100);

					}).on("mouseleave", function() {

						var cap;

						cap = $(this).find(".gallery-banner");

						cap.stop().animate({

							height: hightBegin

						}, 100);

					});

				};

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for defining mouse offset effect.
			*  ||----||
			  ___/  ___/
			*/
			mouseOffset: function () {

				$.fn.imgOffset = function(options) {

					var $picWindows = this,

						sensitiveness = options.sensitiveness,

						picSelector = options.picSelector,

						unnatual = options.unnatual||false;

					$picWindows.each(function(idx, ele) {

						var $picWindow = $(ele),

							$pic = $picWindow.find(picSelector),

							windowWidth = $picWindow.innerWidth(),

							picWidth = $pic.innerWidth(),

							ratio = (sensitiveness * (picWidth - windowWidth)) / windowWidth,

							moveRange = {

								leftMost: windowWidth - picWidth,

								rightMost: 0

							},

							startPosition = {

								pageX: null,

								pageY: null,

								leftDst: null

							};

						$picWindow.on("mouseenter", function(e) {

							startPosition.pageX = e.pageX;

							startPosition.pageY = e.pageY;

							startPosition.leftDst = parseFloat($pic.css("left"));

						});

						$picWindow.on("mousemove", function(e) {

							var currentPosition = {

									pageX: e.pageX,

									pageY: e.pageY

								},

								deltaX = currentPosition.pageX - startPosition.pageX;

							if (unnatual) {

								deltaX *= -1;

							}

							// 目标位置
							var destPos = startPosition.leftDst + deltaX * ratio;

							// update startPosition
							startPosition.pageX = currentPosition.pageX;
							startPosition.pageY = currentPosition.pageY;

							// 再移动就超出了
							if (outOfRange(moveRange, destPos, deltaX, unnatual)) {

								return;

							}

							// update only when the $pic moves
							$pic.css("left", startPosition.leftDst=destPos);

						});

						function outOfRange(moveRange, destPos, deltaX/*, unnatual*/) {

							return (deltaX > 0 && destPos > moveRange["rightMost"])/*往右移动*/ ||
								(deltaX < 0 && destPos < moveRange["leftMost"]);/*往左移动*/

						}

						//TODO[]: When 'mouseleave', reset image to initial position.
						$picWindow.on("mouseleave", $.noop);

					});

				};

			}

		},

		_set: {

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Redefinition for the scroll bar of document.
			*  ||----||
			  ___/  ___/
			*/
			docScrollBar: function () {

				var _root;

				_root = $(':root');

				_root.niceScroll({

					cursorcolor: "#999",

			        cursoropacitymin: 0,

			        cursoropacitymax: 1,

			        cursorwidth: 5,

			        cursorborder: "0 solid #fff",

			        cursorborderradius: 0,

			        zindex: 9999,

			        scrollspeed: 60,

			        mousescrollstep: 40,

			        touchbehavior: false,

			        hwacceleration: true,

			        boxzoom: false,

			        dblclickzoom: false,

			        gesturezoom: false,

			        grabcursorenabled: false,

			        autohidemode: true,

			        background: "",

			        iframeautoresize: true,

			        cursorminheight: 20,

			        preservenativescrolling: true,

			        railoffset: { top: 0, left: -5 },

			        bouncescroll: true,

			        spacebarenabled: true,

			        railpadding: { top: 0, right: 0, left: 0, bottom: 0 },

			        disableoutline: true,

			        horizrailenabled: false,

			        railalign: 'right',

			        railvalign: 'bottom',

			        enabletranslate3d: true,

			        enablemousewheel: true,

			        enablekeyboard: true,

			        smoothscroll: true,

			        sensitiverail: true,

			        enablemouselockapi: true,

			        cursorfixedheight: false,

			        hidecursordelay: 400,

			        directionlockdeadzone: 6,

			        nativeparentscrolling: true,

			        enablescrollonselection: true,

			        rtlmode: false,

			        cursordragontouch: true,

			        oneaxismousemode: "auto",

			        scriptpath: ""

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for Top Sliding Ad.
			*  ||----||
			  ___/  ___/
			*/
			topAd: function () {

				var $adBlock, $adBanner, $closeBtn, $doubleEightAd, $holidayAdbanner,
					adBlockExists, _slideUp, hideAd;

				$adBlock = $('#topbanner');

				$adBanner = $('#adbanner');

				$closeBtn = $('.bannerclose');

				$doubleEightAd = $('#doubleEightAd');

				$holidayAdbanner = $("#holidayAdbanner");

				adBlockExists = $adBanner.length > 0 ? true : false;

				$doubleEightAd.load("http://www.chexiang.com/common/holidayad.htm", function() {

					if ($adBanner.length === 0 || ($adBanner.length > 0 && $adBlock.data("slideup") === true)) {

						//$doubleEightAd.show();

						$doubleEightAd.find("img").on("load", function() {

							$doubleEightAd.slideDown(300);

						});

					} else {

						$doubleEightAd.hide();

					}

				});

				_slideUp = function () {

					$adBlock.animate({

						height: 0

					}, {

						duration: 1000,

						done: function() {

							$adBlock.hide();

							$adBlock.data("slideup", true);

							if ($holidayAdbanner.length > 0) {

								$doubleEightAd.slideDown(500);

							}

						}

					});

				};

				hideAd = function () {

					setTimeout(function() {

						if ($adBlock.is(":visible") && !$adBlock.is(":animated")) {

							_slideUp();

						}

						$closeBtn.off();

					}, 3000);

				};

				if (adBlockExists) {

					$adBlock.slideDown({

						duration: 1000,

						always: hideAd()

					});

					// close button
					$closeBtn.one('click', function() {

						_slideUp();

					});

				} else {

					if ($holidayAdbanner.length > 0) {

						$doubleEightAd.show();

					}

				}

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start mixture functions.
			*  ||----||
			  ___/  ___/
			*/
			mixture: function () {

				var f4ImgLinker;

				f4ImgLinker = $('.col-grid-cars').find('img');

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||---->
				*  ||----||
				  ___/  ___/
				*/
				$.ajaxSetup({

					cache: false

				});

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||----> Smooth scrolling to internal links.
				*  ||----||
				  ___/  ___/
				*/
				$('a[href^="#"]').on('click',function (e) {

					var _hash;

					_hash = this.hash;

					if (!_hash == '') {

						e.preventDefault();

						var _target;

						_target = $(_hash);

						$('html, body').stop().animate({

							'scrollTop': _target.offset().top

						}, 1000, 'swing', function () {

							window.location.hash = target;

						});

					}

				});

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||---->
				*  ||----||
				  ___/  ___/
				*/
				f4ImgLinker.on(fns.helpers.clickOrTouch(), function () {

					var that;

					that = $(this);

					that.next('a').get(0).click();

				});

				/*
						 (__)
						 (oo)
				  /-------\/
				 / |     ||----> 首页会员积分权益兑换
				*  ||----||
				  ___/  ___/
				*/
				$("#PakageList a.btn").on('click', function (e) {

					e.preventDefault();

					$.ajax({

						url: $(this).data("href"),

						cache: false,

						dataType: "jsonp",

						jsonp: "onJSONPLoad",

						jsonpCallback: "onJSONPLoad"

					});

				});

				window.onJSONPLoad = function (data) {

					if(data.errorType=='nologin'){

						var backUrl = window.location.href;

						window.location.href = mainBase + "/account/login.htm?backUrl=" + encodeURIComponent(backUrl);

					} else if(data.errorType=='false'){

						ECar.dialog.alert(data.info);

					} else {

						ECar.dialog.confirm({message: '<i class="icon icon-alert-l"></i>只能兑换一次！确认兑换吗?', callback: function() {

			                location.href = memberBase + "/member/benefits/preorderSubmit.htm?typePackage=" + data.packageType;

			            }});

					}

				};

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start slides effect.
			*  ||----||
			  ___/  ___/
			*/
			slider: function () {

				var mainPromo, thirdFloorPromo;

				mainPromo = $("#J_arrowSlider");

				thirdFloorPromo = $("#banner3F");

				mainPromo.slider({

					type:"rectangle"

				});

				thirdFloorPromo.slider({

					type: "rectangle",

					showArrow: false,

					noArrow: true

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start tab effect.
			*  ||----||
			  ___/  ___/
			*/
			tab: function () {

				var ajaxCache;

				ajaxCache = {};

				ECar.tab({

					selector: ".model-contents-tabs",

					event: "click",

					delay: 200,

					callback: function(id) {

						// var $models = $("#"+id)
						if(id in ajaxCache) {

							return;

						}

						ajaxCache[id] = true;

						$("#"+id).load(base + '/indexHotCarByType.htm', {enumIndexHotCarType:id}, function() {

							var $contentBlocks = $("#"+id),

								$models = $("#"+id).children(".model"),

								blockHeight = 217;

							// blockHeight = $models.eq(0).outerHeight()+parseFloat($models.eq(0).css("margin-bottom"))+parseFloat($models.eq(0).css("margin-top"));
							if($models.length<=4) {

								$contentBlocks.height(blockHeight);

							} else {

								$contentBlocks.height(blockHeight*2);

								$contentBlocks.css("overflow", "hidden");

							}

						});

						//$(".model-contents-models").load(base + '/indexHotCarByType.htm',{enumIndexHotCarType:id}); // plz return HTML fragments back

					}

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start caption effect.
			*  ||----||
			  ___/  ___/
			*/
			caption: function () {

				var elesWithCap;

				elesWithCap = $('.show-cap');

				elesWithCap.showCap(40, 0);

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for mouse offset effect.
			*  ||----||
			  ___/  ___/
			*/
			imgOffset: function () {

				var gallery, dealer;

				gallery = $(".gallery-window");

				dealer = $(".dealerlist-pic-window");

				gallery.imgOffset({

					sensitiveness: 3,

					unnatual:true,

					picSelector: ".gallery-img"

				});

				dealer.imgOffset({

					sensitiveness: 3,

					unnatual:true,

					picSelector: ".dealerlist-pic"

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start image lazy load effect.
			*  ||----||
			  ___/  ___/
			*/
			imgLazyLoad: function () {

				var imgs;

				imgs = $('.lazy');

				imgs.lazyload({

					effect: "fadeIn",

					threshold: 200

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start '秒针' monitor.
			*  ||----||
			  ___/  ___/
			*/
			monitor: function (container) {

				var _this, monitorHref;

				_this = this;

				monitorHref = container.find("a");

				monitorHref.each(function() {

					var href, $img, src, patt, patt_08, patt_09, patt_10, pIndex, pCode, dmpIndex, dmpCode;

					href = $(this).attr('href');

					$img = $(this).children("img");

					src = $img.attr("src");

					patt = /miaozhen/;

					patt_08 = /1011308/;

					patt_09 = /1011309/;

					patt_10 = /1011310/;

					pIndex;

					pCode;

					dmpIndex;

					dmpCode;

					if (patt.test(href)) {

						if (patt_08.test(href)) {

							var srcPre, img;

							pCode = _this.helpers.Arg('p', href);

							dmpIndex = href.lastIndexOf("DMP-");

							dmpCode = href.substr(dmpIndex + 4, 3);

							//发送监控请求，本地图片正常加载
							srcPre = "http://g.cn.miaozhen.com/x.gif?k=1011308&p=" + pCode + "&rt=2&ns=[M_ADIP]&ni=[M_IESID]&na=[M_MAC]&v=[M_LOC]&o=http://sgm.dmp.miaozhen.com/x.gif?k=DMP-39&p=DMP-" + dmpCode + "&rt=2&o=";

							img = new Image();

							img.src = srcPre;

						}

						if (patt_09.test(href)) {

							var srcPre, img;

							pCode = _this.helpers.Arg('p', href);

							//发送监控请求，本地图片正常加载
							srcPre = "http://g.cn.miaozhen.com/x.gif?k=1011309&p=" + pCode + "&rt=2&ns=[M_ADIP]&ni=[M_IESID]&na=[M_MAC]&v=[M_LOC]&o=";

							img = new Image();

							img.src = srcPre;

						}

						if (patt_10.test(href)) {

							var srcPre, img;

							pCode = _this.helpers.Arg('p', href);

							//发送监控请求，本地图片正常加载
							srcPre = "http://g.cn.miaozhen.com/x.gif?k=1011310&p=" + pCode + "&rt=2&ns=[M_ADIP]&ni=[M_IESID]&na=[M_MAC]&v=[M_LOC]&o=";

							img = new Image();

							img.src = srcPre;

						}

					}

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start IM functions.
			*  ||----||
			  ___/  ___/
			*/
			im: {

				config: {

					scrollTop: '30px',

					duration: 1000,

					easing: 'swing',

					csim: $('.mainsite-im'),

					csimUrl: 'http://kf1.chexiang.com/new/client.php?unique_id=&unique_name=&arg=admin&style=2&l=zh-cn&lytype=0&charset=gbk&referer=http%3A%2F%2Fwww.chexiang.com%2F&isvip=bcf14bbb85a346c2fb52e8cea8822cce&identifier=&keyword=&tfrom=1&tpl=crystal_blue',

					eleToTop: $('.backtoTop')

				},

				init: function () {

					this.jumpForwardToCS();

					this.scrollToTop();

					this.showQRCode();

				},

				jumpForwardToCS: function () {

					var _this;

					_this = this;

					this.config.csim.on('click', function () {

						window.open(_this.config.csimUrl.replace(/.chexiang./g, "."+imBase+"."), "车享客服", "height=573, width=803, top=80, left=300,toolbar=no, menubar=no, scrollbars=no, resizable=yes, location=n o, status=no");

					});

				},

				scrollToTop: function () {

					var _config;

					_config = this.config;

					this.config.eleToTop.on('click', function (e) {

						var win, doc;

						win = $(window);

						doc = $('html, body');

						if (win.scrollTop() <= 0) {

							e.preventDefault();

							e.stopPropagation();

						} else {

							doc.stop().animate({

								'scrollTop': _config.scrollTop

							}, _config.duration, _config.easing);

						}

					});

				},

				showQRCode: function () {

					var btn, _body;

					btn = $('.btnShowQRCode');

					_body = $('body');

					btn.on('click', function () {

						var that, code, _siblings, _siblingsCode;

						that = $(this);

						code = that.children('div');

						_siblings = that.siblings('.btnShowQRCode');

						_siblingsCode = _siblings.children('div');

						_siblingsCode.hide();

						_siblings.removeClass('highlight');

						that.addClass('highlight');

						code.show();

					});

					_body.on('click.chexiang.im', function (e) {

						if (!$(e.target).hasClass('btnShowQRCode')) {

							btn.removeClass('highlight');

							btn.children('div').hide();

						}

					});

				}

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Redefinition for the select element.
			*  ||----||
			  ___/  ___/
			*/
			slt: function () {

				var _slt;

				_slt = $('select');

				_slt.selecter({

					label: '车辆所在地',

					cover: false,

					callback: function (val, idx) {}

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Redefinition for the scroll bar of select options panel.
			*  ||----||
			  ___/  ___/
			*/
			sltOptSrl: function () {

				var sltOpts;

				sltOpts = $('.selecter-options');

				sltOpts.niceScroll({

					cursorcolor: "#ccc",

			        cursoropacitymin: 0,

			        cursoropacitymax: 1,

			        cursorwidth: 5,

			        cursorborder: "0 solid #fff",

			        cursorborderradius: 0,

			        zindex: 9999,

			        scrollspeed: 60,

			        mousescrollstep: 40,

			        touchbehavior: false,

			        hwacceleration: true,

			        boxzoom: false,

			        dblclickzoom: false,

			        gesturezoom: false,

			        grabcursorenabled: false,

			        autohidemode: true,

			        background: "",

			        iframeautoresize: true,

			        cursorminheight: 20,

			        preservenativescrolling: true,

			        railoffset: { top: -5, left: -5 },

			        bouncescroll: true,

			        spacebarenabled: true,

			        railpadding: { top: 0, right: 0, left: 0, bottom: 0 },

			        disableoutline: true,

			        horizrailenabled: false,

			        railalign: 'right',

			        railvalign: 'bottom',

			        enabletranslate3d: true,

			        enablemousewheel: true,

			        enablekeyboard: true,

			        smoothscroll: true,

			        sensitiverail: true,

			        enablemouselockapi: true,

			        cursorfixedheight: false,

			        hidecursordelay: 400,

			        directionlockdeadzone: 6,

			        nativeparentscrolling: true,

			        enablescrollonselection: true,

			        rtlmode: false,

			        cursordragontouch: true,

			        oneaxismousemode: "auto",

			        scriptpath: ""

				});

			},

			/*
					 (__)
					 (oo)
			  /-------\/
			 / |     ||----> Script for start form validation functions.
			*  ||----||
			  ___/  ___/
			*/
			_form: {

				init: function () {

					this.setFocusAssist();

					this.setMessage();

					this.setCustomMethod();

					this.setDefault();

					this._vali();

				},

				setFocusAssist: function () {

					var field;

					field = $('.f4-field');

					field.on('click.chexiang.form.field', function (e) {

						if (e.target.nodeName == 'DIV') {

							$(this).children('input').focus();

						}

					});

				},

				setMessage: function () {

					$.extend($.validator.messages, {

						required: '必须填写',

						remote: '请修正此栏位',

						email: '请输入有效的电子邮件',

						url: '请输入有效的网址',

						date: '请输入有效的日期',

						dateISO: '请输入有效的日期 (YYYY-MM-DD)',

						number: '请输入正确的数字',

						digits: '只可输入数字',

						creditcard: '请输入有效的信用卡号码',

						equalTo: '你的输入不相同',

						extension: '请输入有效的后缀',

						maxlength: $.validator.format('最多 {0} 个字'),

						minlength: $.validator.format('最少 {0} 个字'),

						rangelength: $.validator.format('请输入长度为 {0} 至 {1} 之間的字串'),

						range: $.validator.format('请输入 {0} 至 {1} 之间的数值'),

						max: $.validator.format('请输入不大于 {0} 的数值'),

						min: $.validator.format('请输入不小于 {0} 的数值')

					});

				},

				setCustomMethod: function () {

					$.validator.addMethod('nowhitespace', function(value, element) {

						return this.optional(element) || /^\S+$/i.test(value);

					}, '不许存在空格。');

					$.validator.addMethod('phone', function(value, element) {

						return this.optional(element) || /^0?(13[0-9]|15[012356789]|18[0-9]|14[57])[0-9]{8}$/i.test(value);

					}, '请输入正确的手机号码。');

					$.validator.addMethod('notEqual', function(value, element, param) {

						return this.optional(element) || value !== $(param).val();

					}, '不可填写与左边相同的内容。');

				},

				setDefault: function () {

					$.validator.setDefaults({

						debug: true,

						onfocusin: false,

						onfocusout: false,

						onkeyup: false,

						focusInvalid: true,

						focusCleanup: false,

						success: function(error, element) {

							$(element).closest('.f4-field').removeClass('_error');

						},

						errorPlacement: $.noop

					});

				},

				_vali: function () {

					var frmRegisterValior;

					frmRegisterValior = $('#frm-sell').validate({

						rules: {

							sltLocation: {

								required: true

							},

							iptModel: {

								required: true

							},

							iptUser: {

								required: true

							},

							iptPhone: {

								required: true,

								phone: true,

								digits: true

							}

						},

						showErrors: function (errorMap, errorList) {

							var fisrtErrorMessage, firstErrorElement, errerArea;

							errerArea = $('.error-area');

							if (frmRegisterValior.numberOfInvalids() > 0) {

								fisrtErrorMessage = errorList[0].message;

								firstErrorElement = $(errorList[0].element);

								firstErrorElement.closest('.f4-field').addClass('_error');

								(fisrtErrorMessage === $.validator.messages.required) ? errerArea.html($.trim(firstErrorElement.data('nick')) + fisrtErrorMessage) : errerArea.html(fisrtErrorMessage);

								errerArea.removeClass('hide');

								this.defaultShowErrors();

							} else {

								errerArea.addClass('hide');

							}

						},

						submitHandler: function(form, event) {

							var btnSubmit;

							event.preventDefault();

							btnSubmit = $(form).find('input[type=submit]');

							if ($('html').hasClass('ie7') || $('html').hasClass('ie8')) {

								$(form).valid();

								if (frmRegisterValior.numberOfInvalids() === 0) {

									btnSubmit.prop('disabled', true);

									form.submit();

								} else {

									frmRegisterValior.focusInvalid();

									return false;

								}

							} else {

								btnSubmit.prop('disabled', true);

								form.submit();

							}

						}

					});

				}

			}

		}

	};

	$(fns.init());

} (jQuery, window, window.ECar));