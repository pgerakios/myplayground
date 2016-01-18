/* simple web server configuration 


*/
var http = require('http'), url = require('url'),
    path = require('path'), fs = require('fs')
    port = process.argv[2] || 8888;

/* Redis cache -- create a redis client

    npm install redis 
    npm install redis-server
      --> run redis-server
*/
var redis = require('redis'),
    client = redis.createClient();


client.on("error", function (err) {
    console.log("Error " + err);
});

http.createServer(function(request, response) {
  var uri = url.parse(request.url).pathname;
  var filename = path.join(process.cwd(), uri);
  function resp(headCode, headStruct, bodyString, fileString) {
      response.writeHead(headCode,  headStruct? headStruct : undefined);
      if(bodyString) 
        response.write(bodyString);
      if(fileString)
        response.write(fileString, 'binary');
      response.end();
  };
  function textType() {
    return {'Content-Type': 'text/plain'};
  }
  fs.exists(filename, function(exists) {
    if(!exists) {
      resp(404, textType(), 'File not found\n');
    } else {
      if (fs.statSync(filename).isDirectory()) 
        filename += '/index.html';  
      fs.readFile(filename, 'binary', function(err, file) {
        if(err)
          resp(500, textType(), err + '\n');
        else 
          resp(200, textType, null, file);
      });
    }
  });
}).listen(parseInt(port, 10));
console.log('Static file server running at\n  => http://localhost:' + port + '\nCTRL + C to shutdown');


/*
var config = {
  REDISURL: getEnv('REDISURL'),
  PORT: getEnv('PORT'),
  FOURSQUAREID: getEnv('FOURSQUAREID'),
  FOURSQUARESECRET: getEnv('FOURSQUARESECRET')
};
 
function getEnv(variable){
  if (process.env[variable] === undefined){
    throw new Error('You must create an environment variable for ' + variable);
  }
 
  return process.env[variable];
};
*/