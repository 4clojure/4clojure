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
        "aaSorting": [[ 0, "asc" ]],
        "aoColumns": [
	    null,
            null,
            null
        ]
    } );
}

function configureCodeBox(){
    if ($("#run-button").length){
       var editor = ace.edit("editor");
       editor.setTheme("ace/theme/textmate");

       var ClojureMode = require("ace/mode/clojure").Mode;
       editor.getSession().setMode(new ClojureMode());
       document.getElementById('editor').style.fontSize='14px';
       $("#run-button").click(function(){
         var text = editor.getSession().getValue(); 
         $('#code').val(text);
       });
}
    
   

}
