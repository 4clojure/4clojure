var CodeBox = {

  disableJavascript:  null,
  element:            null,
  submitButtons:      null,
  editor:             null,
  editorSession:      null,
  editorElement:      null,
  cont:               true,
  high:               false,
  animationTime:      800,
  waitTimePerItem:    500,
  images:             null,

  initialize: function() {
    this.disableJavascript = $('#disable-javascript-codebox').length > 0;
    this.element = $("#code-box");
    this.submitButtons = $("#run-button, #submission-button");

    if(!this.disableJavascript && this.submitButtons.length > 0) {
      this.setupEditor();
    }

    this.submitButtons.live("click", $.proxy(this.run, this));
  },

  setupEditor: function() {
    this.element.after("<div id=\"code-div\"> <pre id=\"editor\">" +
        this.element.val() + "</pre></div>");

    this.element.hide();
    this.editorElement = $("#editor");

    this.editor = ace.edit("editor");
    this.editor.setTheme("ace/theme/textmate");

    var ClojureMode = require("ace/mode/clojure").Mode;
    this.editorSession = this.editor.getSession();
    this.editorSession.setMode(new ClojureMode());
    this.editorSession.setUseSoftTabs(true);
    this.editorSession.setTabSize(2);
    this.editorElement.css("font-size", "13px");
  },

  getCode: function() {
    if(this.disableJavascript)
      return $("#code-box").val();
    else
      return this.editorSession.getValue();
  },

  run: function(e) {
    e.preventDefault();

    var text = this.getCode(),
      id = $('#id').attr("value");

    this.images = $(".testcases").find("img"),

    $.ajax({
      type: "POST",
      url: "/rest/problem/"+id,
      dataType: "json",
      data: { id: id, code: text, },
      timeout: 20000, // default clojail timeout is 10000
      beforeSend: $.proxy(this.beforeSendCallback, this),
      success: $.proxy(this.successCallback, this),
      error: function(data, str, error) {
        $("#message-text").text("An Error occured: "+error);
      }});
  },

  beforeSendCallback: function() {
    var anim = function() {
      if(this.cont) {
        this.images.animate({
          opacity: this.high ? 1.0 : 0.1,
        }, this.animationTime);
        this.high = !this.high;
        setTimeout(anim,this.animationTime);
      }
    };

    $("#message-text").text("Executing unit tests...");
    $("#error-message-text").text("");
    this.images.each( function(index, element) {
      setIconColor(element, "blue");
    });
    setTimeout(changeToCodeView,0);
    setTimeout(anim,0);
  },

  successCallback: function(data) {
    var waitTime = this.waitTimePerItem;

    var failingTest = data.failingTest,
      getColorFor = function(index) {
        return index === failingTest ? "red" : "green";
      },
      testWasExecuted = function(index) {
        return index <= failingTest;
      },
      setColor = function(index,element) {
        var color = getColorFor(index);
        waitTime = CodeBox.waitTimePerItem * (index+1);
        setIconColor(element, color, waitTime);
      },
      setMessages = function() {
        $("#message-text").html(data.message);
        $("#error-message-text").html(data.error);
        $("#golfgraph").html(data.golfChart);
        $("#golfscore").html(data.golfScore);
        configureGolf();
      },
      stopAnimation = function() {
        this.cont = false;
        this.images.stop(true);
        this.images.css({ opacity: 1.0, });
      };

    setTimeout($.proxy(stopAnimation, this), waitTime);
    this.images.filter( testWasExecuted ).each(setColor);
    setTimeout(setMessages, waitTime);
  },
}
