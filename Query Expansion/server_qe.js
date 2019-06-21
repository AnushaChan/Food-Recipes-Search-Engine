const express = require('express')
const app = express()
const bodyParser = require('body-parser')

var spawn = require("child_process").spawn;


// var pythonProcess = spawn('python',["./newconverter.py", 'indian']);


// pythonProcess.stdout.on('data', function(data){
//   console.log(data.toString()+">>")
// });


app.use(express.static('public'))
app.listen(3002, () => console.log('App listening on port 3002'))
app.get('/', (req, res) => res.redirect('/index.html'))

var router = express.Router();
router.get('/', function(req, res) {
    res.json({ message: 'welcome to our api' });
});

router.get('/qe', function(req, res) {
  console.log("GET request: /qe " + JSON.stringify(req.query));
  var q = req.query.q
  var outputStr = ""
  var pythonProcess = spawn('java',["-jar","IR.jar",q]);


        pythonProcess.stdout.on('data', function(data){
    //var strData =JSON.parse( data.toString())
    //res.send(strData);
    outputStr += data.toString()
    //console.log(outputStr+">>")
    //res.send({ message: 'welcome to our api' });
        });

  pythonProcess.stdout.on('close', function(data){
                  //var strData =JSON.parse( data.toString())
                        //res.send(strData);
      //output += data.toString() 
          //console.log(outputStr + "<<")
        console.log(outputStr.split("\n")[1])
      res.send(outputStr.split("\n")[1]);
        });
        //res.send("asdasd")

 })



app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use('/api', router);
