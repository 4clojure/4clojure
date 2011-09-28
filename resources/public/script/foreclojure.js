$(document).ready(function() {

  configureDataTables();
  configureCodeBox();
  configureGolf();

  $("form#run-code button#approve-button").live("click", function(e) {
    e.preventDefault();
    if(confirm("Are you sure you want to mark this problem as approved?"))
      $(this).parents("form").attr("action", "/problem/approve").submit();
  });

  $("form#run-code button#reject-button").live("click", function(e) {
    e.preventDefault();
    if(confirm("Are you sure you want to reject this problem? It will be permanently deleted.")) {
      $(this).parents("form").attr("action", "/problem/reject").submit();
    }
  });

  $("form#run-code button#edit-button").live("click", function(e) {
    e.preventDefault();
    $(this).parents("form").attr("action", "/problem/edit").submit();
  });

});

var difficulty = new Array();
difficulty["Elementary"] = 0;
difficulty["Easy"]       = 1;
difficulty["Medium"]     = 2;
difficulty["Hard"]       = 3;
difficulty[""]           = 4;

jQuery.fn.dataTableExt.oSort['difficulty-asc'] = function(a, b) {
    return difficulty[a] - difficulty[b];
};

jQuery.fn.dataTableExt.oSort['difficulty-desc'] = function(a, b) {
    return difficulty[b] - difficulty[a];
};

function configureDataTables(){

    $('#problem-table').dataTable( {
        "iDisplayLength": 25,
        "aaSorting": [[5, "desc"], [1, "asc"], [4, "desc"]],
        "aoColumns": [
            {"sType": "string"},
            {"sType": "difficulty"},
            {"sType": "string"},
            {"sType": "string"},
            {"sType": "numeric"},
            {"sType": "string"}
        ]
    } );

    $('#unapproved-problems').dataTable( {
        "iDisplayLength": 25,
        "aaSorting": [[3, "desc"]],
        "aoColumns": [
            {"sType": "string"},
            {"sType": "string"},
            {"sType": "string"},
            {"sType": "string"}
        ]
    } );

    $('#user-table').dataTable( {
        "iDisplayLength":100,
        "aaSorting": [[0, "asc"]],
        "aoColumns": [
	    {"sType": "numeric"},
            {"sType": "string"},
            {"sType": "numeric"}
        ]
    } );
}

function setIconColor(element, color, timeOut) {
  timeOut = (typeof timeOut == "undefined") ? 0 : timeOut
  setTimeout (function() {
      element.src = "/images/"+color+"light.png";
  }, timeOut);
}

function changeToCodeView() {
  $('#code-div').show('fast');
  $('#golfgraph').hide('fast');
  $('#graph-link').html("View Chart");
}

function configureCodeBox(){
    //For no javascript version we have the code-box text area
    //If we have javascript on then we remove it and replace it with
    //the proper div
    var oldBox = $('#code-box');
    var disableJavaScriptCodeBox = $('#disable-javascript-codebox');
    if (disableJavaScriptCodeBox.length){
      return;
    }
    var hiddenCodeInput = "<input type=\"hidden\" value=\"blank\" name=\"code\" id=\"code\">";
    oldBox.replaceWith("<div id=\"code-div\"> <pre id=\"editor\">" + oldBox.val() + "</pre></div>"+hiddenCodeInput);

    if ($("#run-button").length || $("#submission-button").length){
       var editor = ace.edit("editor");
       editor.setTheme("ace/theme/textmate");

       var ClojureMode = require("ace/mode/clojure").Mode;
       var session = editor.getSession();

       var clickHandler = function() {
         var text = session.getValue(),
           id = $('#id').attr("value"),
           images = $(".testcases").find("img"),
           cont = true,
           high = false,
           animationTime = 800,
           waitTimePerItem = 500,
           waitTime = waitTimePerItem,

           beforeSendCallback = function(data) {
             var anim = function() {
               if(cont) {
                 images.animate({
                   opacity: high ? 1.0 : 0.1,
                 }, animationTime);
                 high = !high;
                 setTimeout(anim,animationTime);
               }
             };

             $("#message-text").text("Executing unit tests...");
             $("#error-message-text").text("");
             images.each( function(index, element) {
               setIconColor(element, "blue");
             });
             setTimeout(changeToCodeView,0);
             setTimeout(anim,0);
           },
           successCallback = function(data) {
             var failingTest = data.failingTest,
                 getColorFor = function(index) {
                     return index === failingTest ? "red" : "green";
                 },
                 testWasExecuted = function(index) {
                     return index <= failingTest;
                 },
                 setColor = function(index,element) {
                     var color = getColorFor(index);
                     waitTime = waitTimePerItem * (index+1);
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
                     cont = false;
                     images.stop(true);
                     images.css({ opacity: 1.0, });
                 };

             setTimeout(stopAnimation, waitTime);
             images.filter( testWasExecuted ).
                 each(setColor);
             setTimeout (setMessages, waitTime);
           };

         $.ajax({type: "POST",
           url: "/rest/problem/"+id,
           dataType: "json",
           data: { id: id, code: text, },
           timout: 20000, // default clojail timeout is 10000
           beforeSend: beforeSendCallback,
           success: successCallback,
           error: function(data, str, error) {
             $("#message-text").text("An Error occured: "+error);
           },
         });
         return false;
       };

       $("#run-button").click(clickHandler);

       session.setMode(new ClojureMode());
       session.setUseSoftTabs(true);
       session.setTabSize(2);

       document.getElementById('editor').style.fontSize='13px';
       $("#run-button").click(function(){
         var text = editor.getSession().getValue();
         $('#code').val(text);
       });

       $("#submission-button").click(function(){
         var text = editor.getSession().getValue();
         $('#code').val(text);
       });
    }
}

function configureGolf(){
  $('#graph-link').show();
  $('#golfgraph').hide();
  $('#graph-link').click(function() {
    $('#code-div').toggle('fast', function() {
      // Animation complete.
    });
    $('#golfgraph').toggle('fast', function() {
      // Animation complete.
    });
    var text = $('#graph-link').html();
    if (text && text == 'View Chart'){
       $('#graph-link').html("View Code");
    } else {
       $('#graph-link').html("View Chart");
    }
  });

}
