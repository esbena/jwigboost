var Popup = function(title, url, settings) {
    var self = this;

    // Initialize title and URL
    this.title = title;
    this.url   = url;

    // Initialize default settings
    this.settings = {
        dimensions  : Popup.fixedSize(800, 600),
        closebutton : true,

        margin      : 20
    };

    // Copy settings from user input
    for (var setting in settings) {
        this.settings[setting] = settings[setting];
    }

    // Create popup HTML
    var overlay = $("<div />").addClass("overlay");
    this.popup = $("<div />").addClass("popup");
    $(window).resize(function() {
        self.resize();
    });

    var topbar = $("<div />").addClass("topbar").appendTo(this.popup);
    var titlediv = $("<div />").addClass("title").text(title).appendTo(topbar);

    if (this.settings.closebutton) {
        var closebutton = $("<div />").addClass("closebutton").text("[X]").appendTo(topbar);
        closebutton.click(function() {
            self.close();
        });
    }

    this.content = $("<div />").addClass("content").appendTo(this.popup);
    this.container = $("<div />").append(overlay).append(this.popup).hide();
    this.iframe = null;
};

Popup.fixedSize = function (width, height) {
    return function() {
        return { 'width'  : width,
                 'height' : height }
    };
};

Popup.fullscreen = function() {
    var margin = 20;

    return { 'width'  : $(window).width() - 2 * margin,
             'height' : $(window).height() - 2 * margin };
};

Popup.currentOpen = null;
Popup.closeCurrent = function() {
    if (Popup.currentOpen != null)
        Popup.currentOpen.close();
};

Popup.prototype.resize = function() {
    var dims = this.settings.dimensions();
    this.popup.css("height", dims.height +"px")
        .css("width", dims.width +"px")
        .css("margin-left", (- (dims.width / 2)) +"px")
        .css("top", ($(window).scrollTop() + this.settings.margin) +"px");

    if (this.iframe)
        this.iframe.css("height", (dims.height - 45) + "px")
}

Popup.prototype.open = function() {
    var self = this;
    Popup.currentOpen = this;

    var loader = $('<div style="width:100%; text-align:center; margin-top: 100px;">' +
                   '<img src="'+ BASEURL + 'ActivityIndicator.gif" alt="Loading..."/></div>')
        .appendTo(this.content);
    
    // Add the popup to body
    this.container.appendTo($("body"));
    this.resize();
    
    this.iframe = $("<iframe />").attr("src", this.url).hide();
    self.content.append(this.iframe);
    this.iframe.load(function() {
        self.resize();
        loader.hide();
        $(this).show();
    });
    
    this.container.show("slow");

    return false;
};

Popup.prototype.close = function() {
    this.container.hide("slow", function() {
        $(this).detach();
    });
    return false;
};
