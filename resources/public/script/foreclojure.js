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


function configureDataTables(){

    $('#problem-table').dataTable( {
        "iDisplayLength": 25,
        "aaSorting": [[ 3, "desc" ]],
        "aoColumns": [
            null,
            null,
            null,
            null,
            null
        ]
    } );

    $('#unapproved-problems').dataTable( {
        "iDisplayLength": 25,
        "aaSorting": [[ 2, "desc" ]],
        "aoColumns": [
            null,
            null,
            null
        ]
    } );

    $('#user-table').dataTable( {
        "iDisplayLength":25,
        "aaSorting": [[ 0, "asc" ]],
        "aoColumns": [
	    null,
            null,
            null
        ]
    } );
}

function configureCodeBox(){
    //For no javascript version we have the code-box text area
    //If we have javascript on then we remove it and replace it with
    //the proper div
    var oldBox = $('#code-box');
    var hiddenCodeInput = "<input type=\"hidden\" value=\"blank\" name=\"code\" id=\"code\">";
    oldBox.replaceWith("<div id=\"code-div\"> <pre id=\"editor\">" + oldBox.val() + "</pre></div>"+hiddenCodeInput);

    if ($("#run-button").length){
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
           time = 800,

           beforeSendCallback = function(data) {
             $("#message-text").text("Executing unit tests...");
             var anim = function() {
               if(cont) {
                 images.animate({
                   opacity: high ? 1.0 : 0.1,
                 }, time);
                 high = !high;
                 setTimeout(anim,time);
               }
             };
             anim();
           },
           successCallback = function(data) {
             var failingTest = data.failingTest;
             cont = false;

             images.stop(true);
             images.css({ opacity: 1.0, });
             images.each( function(index,element) {
               var color = "blue";
               if (index < failingTest) {
                 color = "green";
               } else if (index === failingTest) {
                 color = "red";
               }
               element.src = "/images/"+color+"light.png";
             });

             $("#message-text").html(data.message);
             $("#golfgraph").html(data.golfChart);
             $("#golfscore").html(data.golfScore);
             configureGolf();
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
