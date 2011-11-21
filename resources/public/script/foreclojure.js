var updateProblemCountDelay = 4000; // milliseconds

$(document).ready(function() {

  configureDataTables();
  configureCodeBox();
  configureGolf();

  if($("#totalcount").length > 0)
    configureCounter();

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

  $("button.user-follow-button").live("click", function(e) {
      e.preventDefault();
      var $form = $(this).parents("form");
      var $button = $(this);
      $.ajax({type: "POST",
              url: "/rest" + $form.attr("action"),
              dataType: "json",
              success: function(data) {
                if (data) {
                  $button.text(data["next-label"]);
                  $form.attr("action", data["next-action"]);
                }
              },
             });
      return false;
  });

  $("#all-users-link").html("[show <a href=\"/users/all\">all</a>]");

  $("#user-table,#server-user-table").addClass("js-enabled");

  $("#user-table input.following, #server-user-table input.following").live("click", function(e) {
    e.preventDefault();
    var $checkbox = $(this)
    var $form = $checkbox.parents("form")
    $.ajax({type: "POST",
            url: "/rest" + $form.attr("action"),
            dataType: "json",
            success: function(data) {
              if (data) {
                $checkbox.attr("checked", data["following"]);
                $form.attr("action", data["next-action"]);
              }
             },
           });
    return false;
  });

});

var difficulty = {
    "Elementary": 0,
    "Easy": 1,
    "Medium": 2,
    "Hard": 3,
    "": 4
};

// dataTable will call this function in preparation for sorting a column.
// We're responsible for giving it the "real" data to sort on, for all the
// rows at once
jQuery.fn.dataTableExt.afnSortData['difficulty'] = function(oSettings, iColumn)
{
    var aData = [];
    // fnGetTrNodes returns a context we can use in jquery to iterate over
    // only the <td> elements for this column. General approach taken from
    // http://datatables.net/plug-ins/sorting#functions_data_source
    $('td:eq('+iColumn+')', oSettings.oApi._fnGetTrNodes(oSettings)).each(function () {
	aData.push(difficulty[$(this).text()]);
    });
    return aData;
}

// See comments for above function to make sense of this mess
jQuery.fn.dataTableExt.afnSortData['user-name'] = function(oSettings, iColumn)
{
    var aData = [];
    $('td:eq('+iColumn+') a.user-profile-link', oSettings.oApi._fnGetTrNodes(oSettings)).each(function () {
	aData.push($(this).text());
    });
    return aData;
}

// See comments for above function to make sense of this mess
jQuery.fn.dataTableExt.afnSortData['following'] = function(oSettings, iColumn)
{
    var aData = [];
    $('td:eq('+iColumn+') span.following', oSettings.oApi._fnGetTrNodes(oSettings)).each(function () {
        var followingText = $(this).text();
        if (!followingText || followingText == "") { followingText = "no" }
	aData.push(followingText);
    });
    return aData;
}


function configureDataTables(){

    $('#problem-table').dataTable( {
        "iDisplayLength": 100,
        "aaSorting": [[5, "desc"], [1, "asc"], [4, "desc"]],
        "aoColumns": [
            {"sType": "string"},
            {"sSortDataType": "difficulty", "sType": "numeric"},
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
            {"sSortDataType": "user-name"},
            {"sType": "numeric"},
            {"sSortDataType": "following"}
        ]
    } );

    
    $('#server-user-table').dataTable( {
        "aoColumns": [
            null,
            null,
            null,
            {"bSortable": false}
        ],
        "iDisplayLength":100,
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "/datatable/users"
    } );
}

function setIconColor(element, color, timeOut) {
  timeOut = (typeof timeOut == "undefined") ? 0 : timeOut
  setTimeout (function() {
      element.src = element.src.replace(new RegExp("(.*/images/).*(light.png)"), "$1" + color + "$2");
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
       editor.setShowPrintMargin(false);

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

function configureCounter() {
  $("#totalcounter").html("");
  $("#totalcounter").flipCounter({
    number:parseInt($('#counter-value').val()),
    numFractionalDigits:0,
    digitClass:"counter-digit",
    digitHeight:40,
    digitWidth:22.5,
    imagePath:"/images/flipCounter-medium.png",
    easing: jQuery.easing.easeOutCubic
  });
  setTimeout("updateProblemCount()", updateProblemCountDelay);
}

function updateProblemCount() {
  var updateDuration = updateProblemCountDelay > 2500 ? 2500 : (updateProblemCountDelay - 500);

  if($("#totalcount").length > 0) {
    $.get("/problems/solved", function(data) {
      if($("#totalcounter").flipCounter("getNumber") != data) {
        $("#totalcounter").flipCounter("stopAnimation")
        $("#totalcounter").flipCounter("startAnimation", {
            number: parseInt(data),
            duration: updateDuration,
            easing: jQuery.easing.easeOutCubic
          });
      }
    });
    setTimeout("updateProblemCount()", updateProblemCountDelay);
  }
}
