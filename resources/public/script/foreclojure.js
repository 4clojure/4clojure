$(document).ready(function() {

  configureDataTables();
  CodeBox.initialize();
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
