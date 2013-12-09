var SubmitHandlers = {
    submitted: false,

    submitForm: function(form, submitValue) {
        // Set the submit value
        var submitInput = form.elements["jwig_submit"];
        submitInput.value = submitValue;

        var prototype;
        if (Object.getPrototypeOf) {
            prototype = Object.getPrototypeOf(form);
        } else if (form.__proto__) {
            prototype = form.__proto__;
        } else {
            // Not supported in browser!
            // Examples: IE < 9
            alert("CourseAdmin does not support you browser. Please update to a newer version.");
            return ;
        }

        SubmitHandlers.submitted = true;
        prototype.submit.call(form);
    },

    remapEnter: function(formId, submitValue) {
        var ENTER_KEY = 13;

        var form = $("#" + formId)[0];
        $(document).keypress(function handleEnter(event) {
            if (event.which == ENTER_KEY) {
                // Check that the active element of the page is inside
                // the form, and is an input field.
                // NOTE: Uses document.activeElement which is introduced
                //       as a standard in HTML5, but works in almost
                //       every browser.
                // If active element is undefined, we just submit the form.
                if (document.activeElement) {
                    var activeElement = document.activeElement;
                    if (!activeElement.form || activeElement.form !== form) {
                        // We are not focued in the form.
                        return ;
                    }

                    // Perform default browser action in some cases.
                    var defaultAction = false;
                    var defaultEnterActionTypes = [ "submit", "textarea" ];
                    for (var i = 0; i < defaultEnterActionTypes.length; i++) {
                        if (activeElement.type == defaultEnterActionTypes[i])
                            defaultAction = true;
                    }
                    if (defaultAction)
                        return ;
                }
                event.preventDefault();

                SubmitHandlers.submitForm(form, submitValue);
            }
        });
    },

    confirmSubmit: function(formId, confirms) {
        var forms = $("#" + formId);
        
        forms.each(function() {
            var form = this;

            // Assign the handler as the first submit handler
            var oldSubmitHandler = form.onsubmit;
            form.onsubmit = undefined;
            $(form).submit(function(event) {
                var submitValue = $(this).find("input[name='jwig_submit']").val();
                var confirmed = true;
                
                for (var i = 0; i < confirms.length; i++) {
                    var con = confirms[i];

                    var regexp = new RegExp(con.match, "i");
                    if (regexp.test(submitValue) && !confirm(con.msg)) {
                        // Prevent other handlers from being executed.
                        // This is needed in case of JWIG validation.
                        event.stopImmediatePropagation();
                        
                        SubmitHandlers.submitted = false;
                        return false;
                    }
                }

                return true;
            });

            if (oldSubmitHandler != undefined) {
                $(form).submit(oldSubmitHandler);
            }
        });
    },

    confirmLeaveAndDiscard: function(msg, formId) {
        var changed = false;

        // The container where input change should be detected
        var form = $("#" + formId);
        var container = ($(form).size() != 0) ? form : $("body");

        // Monitor TinyMCE
        var tinyMCEChanged = function() {
            var dirty = false;
            var editors = tinyMCE.editors;
            for (var i = 0; i < editors.length; i++) {
                // Ensure we are inside the container
                if ($.contains(container[0], editors[i].getElement()))
                    dirty = dirty || editors[i].isDirty();
            }
            return dirty;
        };

        // Set changed to true, if tiny is removed when it has been modified.
        tinyMCE.onRemoveEditor.add(function(sender,editor) {
            if (editor.isDirty())
                changed = true;
        });

        var monitoredInputs = [ ];
        var monitoredEditors = [ ];

        // Monitor conventional fields
        var setChanged = function() { changed = true; };
        var setupChangeListeners = function() {
            // If we just reloaded the page nothing should be changed.
            changed = false;

            // Set up listener on all content that can be changed.
            // input and textarea fields.
            var changeableTags = [ "input", "select", "textarea" ];
            for (var i = 0; i < changeableTags.length; i++) {
                var tag = changeableTags[i];

                // Remove listeners already set up.
                for (var j = 0; j < monitoredInputs.length; j++) {
                    $(monitoredInputs[j]).unbind("changed", setChanged);
                }

                monitoredInputs = [ ];

                // Watch conventional tags
                $(container).find(tag).each(function () {
                    if ($(this).hasClass("filterField") || $(this).hasClass("nonVolatile"))
                        return ;
                    monitoredInputs.push(this);
                    $(this).change(setChanged);
                });
            }
        };
        setupChangeListeners();

        // Monitor new elements appearing if DynamicContent is used.
        dynamicContent.registerUpdatedContentCallback(setupChangeListeners);
        dynamicContent.changeContentGuard = function(source) {
            if ($(source).hasClass("saving"))
                changed = false;

            if ($(source).hasClass("safe"))
                return true;

            if (changed || tinyMCEChanged()) {
                if (confirm(msg) == true) {
                    changed = false;
                    return true;
                }

                if ($(source).hasClass("nonVolatile")) {
                    // Reset the input to default value.
                    SubmitHandlers.resetInputToDefault(source);
                }

                if (jwigboost.busyIndicatorIsVisible()) {
                    jwigboost.hideBusyIndicator();
                }

                return false;
            }
            return true;
        };

        // If something is submitted the user should not be prompted.
        var setSubmitHandler = function (form) {
            $(form).submit(function() {
                SubmitHandlers.submitted = true;
            });
        };
        if (container == form) {
            setSubmitHandler(form);
        } else {
            $(container).find("form").each(function() {
                setSubmitHandler(this);
            });
        }

        $(window).bind("beforeunload", function(event) {
            if ((tinyMCEChanged() || changed) && !SubmitHandlers.submitted) {
                return msg;
            }
        });
    },

    resetInputToDefault: function(input) {
        if (input.type == "checkbox" || input.type == "radio") {
            var form = input.form;
            if (form) {
                var elements = form.elements;
                for (var i = 0; i < elements.length; i++) {
                    var elm = elements[i];
                    if (elm.name && elm.name == input.name) {
                        elm.checked = elm.defaultChecked;
                    }
                }
            } else {
                input.checked = input.defaultChecked;
            }
        } else {
            input.value = input.defaultValue;
        }
    }
};
