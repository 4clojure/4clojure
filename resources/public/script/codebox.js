var CodeBox = {

  disableJavascript:  null,
  element:            null,
  submitButtons:      null,
  editor:             null,
  allEditors:         [],
  high:               false,
  animationTime:      800,
  waitTimePerItem:    500,
  images:             null,


  initialize: function() {
    this.disableJavascript = $('#disable-javascript-codebox').length > 0
                                || $.browser.mobile;
    this.element = $("#code-box");
    this.submitButtons = $("#run-button, #submission-button");
    var isSettingsPage = $('#settings #code-box').length > 0;

    if(!this.disableJavascript && this.submitButtons.length > 0 || isSettingsPage) {
      this.setupEditor();
    }

    $("#run-button").live("click", $.proxy(this.run, this));
    $("#submission-button").live("click", $.proxy(this.submitProblem, this));
  },

  setupEditor: function() {
    this.editor = CodeMirror.fromTextArea(this.element[0],
                                         {mode: 'clojure',
                                          lineNumbers: true,
                                          theme: this.theme});
    $(this.editor.getWrapperElement()).addClass('codebox');
    $('#theme').live('change', function() {
      var theme = $(this).val();
      CodeBox.editor.setOption('theme', theme);
    });
    $('#theme').val(this.theme);
  },

  getCode: function() {
    if(this.disableJavascript)
      return $("#code-box").val();
    else
      return this.editor.getValue();
  },

  toggle: function() {
    if(this.disableJavascript)
      $("#code-box").toggle('fast');
    else
      $(".codebox").toggle('fast');
  },

  submitProblem: function(e) {
    e.preventDefault();
    $("#code-box").val(this.getCode());
    $("#code-box").closest("form").submit();
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
      if(this.images.filter('.animated').length > 0) {
        this.images.filter('.animated').animate({
          opacity: this.high ? 1.0 : 0.1,
        }, this.animationTime);
        this.high = !this.high;
        setTimeout($.proxy(anim, this),this.animationTime);
      } else {
        this.high = false;
      }
    };

    $("#message-text").text("Executing unit tests...");
    $("#error-message-text").text("");
    this.images.each( function(index, element) {
      setIconColor(element, "blue");
    });
    setTimeout(changeToCodeView,0);
    this.images.addClass("animated");
    setTimeout($.proxy(anim, this),0);
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
        setIconColor(element, color, waitTime, true);
        if(color == "red") { //failing test
          setTimeout("CodeBox.stopAnimation()", waitTime);
        }
      },
      setMessages = function() {
        $("#message-text").html(data.message);
        $("#error-message-text").html(data.error);
        $("#golfgraph").html(data.golfChart);
        $("#golfscore").html(data.golfScore);
        configureGolf();
      };

    this.images.filter( testWasExecuted ).each(setColor);
    setTimeout(setMessages, waitTime);
  },

  stopAnimation: function() {
    this.images.stop(true).removeClass("animated").css({ opacity: 1.0, });
  },
}
