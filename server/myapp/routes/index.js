var express = require('express');
var path = require('path');
var fs = require('fs');

var router = express.Router();
var app = express();

app.use('/static', express.static(path.join(__dirname, 'public')));

router.post('/', function(req, res, next) {
  var db = req.db;
  var json = req.body;

  var db_users = db.get('users');
  var db_contacts = db.get('contacts');
  var db_images = db.get('images');
  
  var message = '';
  console.log(json);
  
  if (isEmptyObject(json)) {
    res.json({result:'failed', description:'json body is empty'});
    return;
  }
  if (!json.type) {
    res.json({result:'failed', description:'you must specify type'});
    return;
  }
  switch (json.type) {
    case "NEW_USER":
      if (!json.name || !json.email) {
        res.json({result:'failed', description:'name and email must be provided'});
        return;
      }
      db_users.insert({name:json.name, email:json.email}, function(e, new_user) {
        if (e) return next(e);
        res.json({result:'success', user_id:new_user._id, name:new_user.name, email:new_user.email});
        return;
      });
      break;

    case "GET_CONTACTS":
      if (!json.user_id) {
        res.json({result:'failed', description:'user_id must be provided'});
        return;
      }
      if (!isValidId(json.user_id)) {
        res.json({result:'failed', description:'invalid user_id format'});
        return;
      }
      db_users.find({_id:json.user_id}, {}, function(e, users) {
        if (e) return next(e);
        if (isEmptyObject(users)) {
          res.json({result:'failed', description:'user not registered'});
          return;
        }
        db_contacts.find({user_id:users[0]._id}, {}, function(e, contacts) {
          if (e) return next(e);
          if (isEmptyObject(contacts)) {
            res.json({result:'failed', description:'no contact group found'});
            return;
          }
          var contact = contacts[0];
          if (isEmptyObject(contact.contacts)) {
            res.json({result:'failed', description:'contacts field is empty'});
            return;
          }
          res.json({result:'success', contacts:contact.contacts});
          return;
        });
      });
      break;

    case "ADD_CONTACTS":
      if (!json.user_id) {
        res.json({result:'failed', description:'user_id must be provided'});
        return;
      }
      if (!isValidId(json.user_id)) {
        res.json({result:'failed', description:'invalid user_id format'});
        return;
      }
      if (!json.contacts) {
        res.json({result:'failed', description:'no contacts array found in json'});
        return;
      }
      db_users.find({_id:json.user_id}, {}, function(e, users) {
        if (e) return next(e);
        if (isEmptyObject(users)) {
          res.json({result:'failed', description:'user not registered'});
          return;
        }
        var user = users[0];
        db_contacts.find({user_id:user._id}, {}, function(e, contacts) {
          var contact = contacts[0];
          if (isEmptyObject(contact)) {
            db_contacts.insert({user_id:user._id, name:user.name, email:user.email, contacts: json.contacts}, function(e, new_contact) {
                if (e) return next(e);
                res.json({result:'success', description:'new contact group created'});
                return;
            });
          } else {
            db_contacts.update({user_id:user._id},
              {user_id:contact.user_id,
               name:contact.name,
               email:contact.email,
               contacts:contact.contacts.concat(json.contacts)},
              function(e, updated_contact) {
                if (e) return next(e);
                res.json({result:'success', description:'added contacts'});
                return;
            });
          }
        });
      });
      break;

    case "UPLOAD_IMG":
      if (!json.user_id) {
        res.json({result:'failed', description:'user_id must be provided'});
        return;
      }
      if (!isValidId(json.user_id)) {
        res.json({result:'failed', description:'invalid user_id format'});
        return;
      }
      db_users.find({_id:json.user_id}, {}, function(e, users) {
        if (e) return next(e);
        if (isEmptyObject(users)) {
          res.json({result:'failed', description:'user not registered'});
          return;
        }
        var user = users[0];
        if (!json.img) {
          res.json({result:'failed', description:'image not included'});
          return;
        }
        var base64Data = json.img;
        var imageBuffer = decodeBase64Image(base64Data);
        var filetype = getImageContainer(base64Data);
        var filename = 'image_' + Date.now() + '.' + filetype;
        console.log(filetype, filename);
        fs.writeFile(__dirname + '/../public/images/' + filename, imageBuffer.data, function(e, file){
          if (e) return next(e);
          var url = 'http://52.78.200.87:3000/static/images/' + filename;
          db_images.insert({user_id:json.user_id, img_url:url}, function(e, image) {
            res.json({result:'success', img_id:image._id});
            return;
          });
        });            
      });
      break;

    case "GET_IMG":
      if (!json.user_id || !json.img_id) {
        res.json({result:'failed', description:'user_id and img_id must be provided'});
        return;
      }
      if (!isValidId(json.user_id)) {
        res.json({result:'failed', description:'invalid user_id format'});
        return;
      }
      if (!isValidId(json.img_id)) {
        res.json({result:'failed', description:'invalid img_id format'});
        return;
      }
      db_users.find({_id:json.user_id}, {}, function(e, users) {
        if (e) return next(e);
        if (isEmptyObject(users)) {
          res.json({result:'failed', description:'user not registered'});
          return;
        }
        var user = users[0];
        db_images.find({user_id:json.user_id, _id:json.img_id}, {}, function(e, images) {
          if (e) return next(e);
          if (isEmptyObject(images)) {
            res.json({result:'failed', description:'image not found'});
            return;
          }
          var image = images[0];
          res.json({result:'success', img_url:image.img_url});
          return;
        });
      }); 
      break;

    default:
      res.json({result:'failed', description:'unknown type'});
      return;
      break;
  }
});

function isEmptyObject(obj) {
  for (var key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      return false;
    }
  }
  return true;
}

function isValidId(obj) {
  if (isEmptyObject(obj))
    return false;
  var id = obj;
  if (typeof id == "string") {
    var length = id.replace(/[\0-\x7f]|([0-\u07ff]|(.))/g,"$&$1$2").length;
    if (length == 24)
      return true;
  }
  return false;
}

function decodeBase64Image(dataString) {
  var matches = dataString.match(/^data:([A-Za-z-+\/]+);base64,(.+)$/),response = {};

  if (matches.length !== 3) {
    return new Error('Invalid input string');
  }

  response.type = matches[1];
  response.data = new Buffer(matches[2], 'base64');

  return response;
}

function getImageContainer(dataString) {
  var header = dataString.substring(0,30);
  var end = header.indexOf(";base64,");
  var start = "data:image/".length;
  return header.substring(start, end);
}

module.exports = router;
