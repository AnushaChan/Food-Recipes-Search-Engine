var solrIP = "localhost:8983"
var clusterIP = "localhost:3000"
var googleIP = "localhost:3001"
var queryExpIP = "localhost:3002"



function searchWeb(searchTerm) {
  var linkBing = "https://www.bing.com/search?q=" + searchTerm

  document.getElementById('iframe_bing').setAttribute("src", linkBing)
}

var globV //delete this
var globV2
var tempGoogle

function searchOurIndex(searchTerm) {
  console.log("searching our index")
  var querryString = "http://" + solrIP + "/solr/nutch/select?q=content%3A" + searchTerm 
  $.ajax({
    url: querryString,
    success: function( response ) {
        //console.log( response ); // server response
        globV = response // delete this line

        displayResults(response.response.docs)
    }
  });
}

function searchGoogle(searchTerm){
  console.log("searching google")
  var querryString = "http://"+googleIP+"/api/google?q=" + searchTerm 
  $.ajax({
    url: querryString,
    success: function( response ) {
        //console.log( response ); // server response
        //globV = response // delete this line
        response = JSON.parse(response)
        tempGoogle = response
        displayResults(response)
    }
  });
}

function resultClicked(url){
  console.log("clicked")
  var win = window.open(url, '_blank');
  win.focus();
}

function clearResults(){
  $("#results").html("");
}

function displayResults(results){
  globV2 = results
	resultHTML = ""


  results.map(function(result){
    resultHTML += '<div class="result" >\
              <h3 class="title" onclick="resultClicked(\'' + result.url[0] + '\');">' + result.title + '</h2>\
              <a href="'+result.url[0]+'">'+result.url[0].substring(0, 80)+'</a>\
              <p class="content">' + result.content[0].substring(0, 300) + '...</p></div>';
  })

	$("#results").html(resultHTML);
}



var globV3

function searchCluster(searchTerm){
  console.log("searching cluster")
  var querryString = "http://"+ clusterIP +"/api/cluster?q=" + searchTerm 
  $.ajax({
    url: querryString,
    success: function( response ) {
        console.log( response ); // server response
        //globV = response // delete this line
        globV3 = response

        var tempRes = []


        response.map(function(result){
          var tempResObj = {}
          tempResObj["title"] = result.title
        tempResObj["url"] = [result.url]
        tempResObj["content"] = [result.content]

        tempRes.push(tempResObj)

        })
        
        //response = JSON.parse(response)
        //tempGoogle = response
        displayResults(tempRes)
    }
  });
}

function searchQueryExp(searchTerm){
  console.log("searching query exp")
  var querryString = "http://"+ queryExpIP +"/api/qe?q=" + searchTerm 
  $.ajax({
    url: querryString,
    success: function( response ) {
        console.log( response ); // server response
        //globV = response // delete this line
        globV3 = response

        //var tempRes = []
        $("#search-text").val(response)

        searchOurIndex(response)
        // response.map(function(result){
        //   var tempResObj = {}
        //   tempResObj["title"] = result.title
        // tempResObj["url"] = [result.url]
        // tempResObj["content"] = [result.content]

        // tempRes.push(tempResObj)

        // })
        
        //response = JSON.parse(response)
        //tempGoogle = response
        //displayResults(tempRes)
    }
  });
}


var searchTerm;
var searchRadioBut = 0;

$("document").ready(function() {
  $("#searchForm").submit(function() {
  	searchTerm = $("#search-text").val()
    searchWeb(searchTerm)
    //searchOurIndex(searchTerm)
    //searchGoogle(searchTerm)


     switch (searchRadioBut) {
      case 0:
        searchOurIndex(searchTerm)
        console.log($(this)[0].id)
        searchRadioBut = 0
        $('.iframe-container').css("display", "none");
        break;
      case 1:
        searchGoogle(searchTerm)
        console.log($(this)[0].id)
        searchRadioBut = 1
        $('.iframe-container').css("display", "none");
        break;
      case 2:
        $('.iframe-container').css("display", "block");
        console.log($(this)[0].id)
        searchOurIndex(searchTerm)
        searchRadioBut = 2
        break;
      case 3:
        $('.iframe-container').css("display", "none");
        console.log($(this)[0].id)
        searchCluster(searchTerm)
        searchRadioBut = 3
        break;
      case 4:
        $('.iframe-container').css("display", "none");
        console.log($(this)[0].id)
        searchQueryExp(searchTerm)
        searchRadioBut = 4
        break;
    }


    return false
  });


  $('input[type=radio][name=group1]').on('change', function() {
    switch ($(this)[0].id) {
      case 'radioOurSearch':
        searchOurIndex(searchTerm)
        console.log($(this)[0].id)
        searchRadioBut = 0
        $('.iframe-container').css("display", "none");
        break;
      case 'radioGoogle':
        searchGoogle(searchTerm)
        console.log($(this)[0].id)
        searchRadioBut = 1
        $('.iframe-container').css("display", "none");
        break;
      case 'radioBing':
        $('.iframe-container').css("display", "block");
        searchOurIndex(searchTerm)
        console.log($(this)[0].id)
        searchRadioBut = 2
        break;
      case 'radioCluster':
        $('.iframe-container').css("display", "none");
        console.log($(this)[0].id)
        searchCluster(searchTerm)
        searchRadioBut = 3
        break;
      case 'QueryExp':
        $('.iframe-container').css("display", "none");
        console.log($(this)[0].id)
        searchQueryExp(searchTerm)
        searchRadioBut = 4
        break;
    }
  });


})





