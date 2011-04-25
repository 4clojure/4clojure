$(document).ready(function() {

  configureDataTables();
  configureCodeBox();

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
        "aaSorting": [[ 1, "desc" ]],
        "aoColumns": [
            null,
            null
        ]
    } );
}

function configureCodeBox(){
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/twilight");

    var JavaScriptMode = require("ace/mode/javascript").Mode;
    editor.getSession().setMode(new JavaScriptMode());

    $("#run-button").click(function(){
      var text = editor.getSession().getValue(); 
      $('#code').val(text);
    });

}
