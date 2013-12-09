/*jslint white: true, onevar: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, regexp: true, newcap: true, immed: true, strict: true */
/*global window, document, alert */
/*global ActiveXObject, XMLHttpRequest*/
/*global setTimeout*/
/*global jwig*/
/*global Table*/
/*jslint plusplus: false */
"use strict";
var jwigboost = {
	/**
     * Naive decryption for email obfuscation: char-values to
     * chars, separated by '-' 
     * 
     * ex.: "102-111-111" -> "foo"
     */
    decryptMailto : function (encrypted) {
        var split, i, decrypted, value, character;
        split = encrypted.split("-");
        decrypted = "";
        for (i = 0; i < split.length; i++) {
            value = split[i];
            decrypted += String.fromCharCode(value);
        }
        document.location.href = "mailto:" + decrypted;
    },

    countChildElements : function (element) {
        var res = 0;
        if (element.hasChildNodes()) {
            for (var e = element.firstChild; e != null; e = e.nextSibling) {
                if (e.nodeType == 1) {
                    res++;
                }
            }
        }
        return res;
    },

    toggleDisplayNextSiblings : function (element) {
        for (var e = element.nextSibling; e != null; e = e.nextSibling) {
            if (e.nodeType == 1) {
                e.style.display = (e.style.display != "none") ? "none" : "";
            }
        }
    },

    addExpander : function (element) {
        var expander = document.createElement("a");
        expander.appendChild(document.createTextNode("..."));
        expander.style.textDecoration = "underline";
        expander.style.color = "#003D85";
        expander.style.cursor = "pointer";
        expander.onclick = function() {
            jwigboost.toggleDisplayNextSiblings(element);
            element.removeChild(expander);
        };
        if (element.hasChildNodes() && element.lastChild.nodeType == 3) {
            element.lastChild.textContent = element.lastChild.textContent + " ";
        } else {
            element.appendChild(document.createTextNode(" "));
        }
        element.appendChild(expander);
    },

    abbreviateList : function (list) {
        if (jwigboost.countChildElements(list) <= 1) {
            return;
        }
        var e = list.firstChild;
        while (e != null && e.nodeType != 1) {
            e = e.nextSibling;
        }
        jwigboost.toggleDisplayNextSiblings(e);
        jwigboost.addExpander(e);
    },

    abbreviateListsWithClassName : function (className) {
        $("." + className).each(function() {
            jwigboost.abbreviateList(this);
        });
    },
    
    browserSupportsInputPlaceholder : function () {
        return 'placeholder' in document.createElement('input');
    },

    _addPlaceholderText : function (textInput, str) {
        if (textInput.value == "") {
            $(textInput).addClass("placeholder");
            textInput.value = str;
        }
    },

    addPlaceholder : function (textInput, str) {
        jwigboost._addPlaceholderText(textInput, str);
        $(textInput).focus(function() {
            if ($(this).hasClass("placeholder")) {
                $(this).removeClass("placeholder");
                $(this).val("");
            }
        });
        $(textInput).blur(function() {
            jwigboost._addPlaceholderText(textInput, str);
        });
    },
    
    addFallbackPlaceholders : function () {
        var inputElements = document.getElementsByTagName("input");
        for (var i = 0; i < inputElements.length; i++) {
            var elem = inputElements[i];
            var placeholder = elem.getAttribute("placeholder");
            if (elem.type == "text" && placeholder != null) {
                jwigboost.addPlaceholder(elem, placeholder);
            }
        }
    },
    
    hideWithToggler : function (toHide, toggler) {
        $(toHide).hide();
        $(toggler).removeClass("collapsible").addClass("expandable");

        $(toggler).click(function() {
            if ($(toHide).is(":visible")) {
                $(toHide).hide();
                $(toggler).removeClass("collapsible").addClass("expandable");
            } else {
                $(toHide).show();
                $(toggler).removeClass("expandable").addClass("collapsible");
            }
        });
    },
    
    hideWithRadio : function (toHide, revealer) {
        if (!$(revealer).is(":checked"))
            $(toHide).hide();
        else
            $(toHide).show();

        var radios = $("input[type=radio][name="+revealer.attr("name")+"]").click(function() {
            if ($(revealer).val() != $(this).val())
                $(toHide).hide();
            else
                $(toHide).show();
        });
    },
    
    hideUnlessChecked : function (toHide, checkbox) {
        if (!checkbox.is(":checked"))
            $(toHide).hide();

        $(checkbox).click(function() {
            if ($(this).is(":checked"))
                $(toHide).show();
            else
                $(toHide).hide();
        });
    },
    
    BUSY_INDICATOR : null,
    
    showBusyIndicator : function () {
        var indicator = $("<div />").addClass("indicator");
        jwigboost.BUSY_INDICATOR = indicator;

        var width = 50;
        var height = 50;

        var innerHeight = $(window).height();
        var innerWidth  = $(window).width();
        var scrollY     = $(window).scrollTop();
        var scrollX     = $(window).scrollLeft();

        $(indicator).css("position", "absolute")
            .css("top", (innerHeight - height) / 2 + scrollY)
            .css("left", (innerWidth - width) / 2 + scrollX);

        $("body").append(indicator);

        return jwigboost.BUSY_INDICATOR;
    },
    
    hideBusyIndicator : function () {
        jwigboost.BUSY_INDICATOR.remove();
        jwigboost.BUSY_INDICATOR = null;
    },
    
    busyIndicatorIsVisible : function () {
        return jwigboost.BUSY_INDICATOR != null;
    },
    
    getFilename : function (fileInput) {
        var fragments = fileInput.value.split(/(\\|\/)/g);
        return fragments[fragments.length-1];
    },

    callLater : function (fn, delay) {
        window.setTimeout(fn, delay);
    },

    ensureInputTagDateAndDateTimeSupport: function(){
        function isDateSupported() { // consider http://modernizr.com/ for this job
            var i = document.createElement("input");
            i.setAttribute("type", "date");
            return i.type === "date";
        }

        if (!isDateSupported()) {
            $.datepicker.setDefaults({dateFormat: "yy-mm-dd"});
            $.timepicker.setDefaults({timeFormat: "HH:mm"});

            var dateInputs = $('input.jwb_date');
            dateInputs.datepicker();

            var dateTimeInputs = $('input.jwb_datetime');
            dateTimeInputs.val(function(i, val) { // hack to make datetimepicker separate the two datetime parts. Setting the 'separator to 'T' does not work (and this is prettier anyway.
                return val.replace('T', ' ');
            });
            dateTimeInputs.datetimepicker();
        }
    }
}