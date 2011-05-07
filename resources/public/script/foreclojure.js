$(document).ready(function() {

  configureDataTables();
  configureCodeBox();
  configureGolf();

});

function configureDataTables(){

    $('#problem-table').dataTable( {
        "iDisplayLength": 25,
        "aaSorting": [[ 3, "desc" ]],
        "aoColumns": [
            null,
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
    oldBox.replaceWith("<div id=\"code-div\"> <pre id=\"editor\">" + oldBox.val() + "</pre></div>");
    var hiddenCodeInput = "<input type=\"hidden\" value=\"blank\" name=\"code\" id=\"code\">";
    $(hiddenCodeInput).insertBefore('#id');

    if ($("#run-button").length){
       var editor = ace.edit("editor");
       editor.setTheme("ace/theme/textmate");

       var ClojureMode = require("ace/mode/clojure").Mode;
       editor.getSession().setMode(new ClojureMode());
       document.getElementById('editor').style.fontSize='13px';
       $("#run-button").click(function(){
         var text = editor.getSession().getValue(); 
         $('#code').val(text);
       });
}
}

function configureGolf(){
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
    }else{
       $('#graph-link').html("View Chart");
    }
    

});

}
