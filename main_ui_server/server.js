"use strict";
const express = require('express')
const app = express()
const bodyParser = require('body-parser')

const google = require('google')

var spawn = require("child_process").spawn;

var pythonProcess = spawn('python',["./newconverter.py", "indian"]);



pythonProcess.stdout.on('data', function(data){
    console.log(data.toString())
});

pythonProcess.stdout.on('end', function(data){
  //console.log('Sum of numbers=');
  //console.log(data)
});



app.use(express.static('public'))
app.listen(3001, () => console.log('App listening on port 3001'))
app.get('/', (req, res) => res.redirect('/index.html'))

var router = express.Router();
router.get('/', function(req, res) {
    res.json({ message: 'welcome to our api' });   
});

router.get('/google', function(req, res) {
  console.log("GET request: /google " + JSON.stringify(req.query));
  var q = req.query.q


  google.resultsPerPage = 20

  google(q, function (err, resp){
    if (err) 
      console.error(err)
    var retObj = []
    for (var i = 0; i < resp.links.length; ++i) {
      var tempObj = {}
      var link = resp.links[i]

      tempObj["title"] = link.title
      tempObj["url"] = [link.href]
      tempObj["content"] = [link.description]
      // console.log(link.title + ' - ' + link.href)
      // console.log(link.description + "\n")
      if (link.title && link.href && link.description && retObj.length <= 10)
        retObj.push(tempObj)
    }
    res.json(JSON.stringify(retObj))
  })

   
});


// router.get('/bing', function(req, res) {
//   console.log("GET request: /bing " + JSON.stringify(req.query));
//   var q = req.query.q

//   sec.bing(q).then(function(result){
//     console.log(result);
// });
//   // google.resultsPerPage = 20

//   // google(q, function (err, resp){
//   //   if (err) 
//   //     console.error(err)
//   //   var retObj = []
//   //   for (var i = 0; i < resp.links.length; ++i) {
//   //     var tempObj = {}
//   //     var link = resp.links[i]

//   //     tempObj["title"] = link.title
//   //     tempObj["url"] = [link.href]
//   //     tempObj["content"] = [link.description]
//   //     // console.log(link.title + ' - ' + link.href)
//   //     // console.log(link.description + "\n")
//   //     if (link.title && link.href && link.description && retObj.length <= 10)
//   //       retObj.push(tempObj)
//   //   }
//   //   res.json(JSON.stringify(retObj))
//   // })

   
// });




app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use('/api', router);



