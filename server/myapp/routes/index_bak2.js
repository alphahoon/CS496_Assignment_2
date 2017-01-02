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
  var db_albums = db.get('albums');
  var db_stats = db.get('statistics');
  
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
                // console.log("CREATED CONTACT: " + new_contact)
                var num = contact.contacts.length;
                res.json({result:'success', description:'new contact group created', contacts_num:num});
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
                // console.log("ADDED CONTACT: " + contact);
                db_contacts.find({user_id:contact.user_id}, {}, function(e, contacts) {
                  var contact = contacts[0];
                  var num = contact.contacts.length;
                  res.json({result:'success', description:'added contacts', contacts_num:num});
                  return;
                });
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

    case "CREATE_ALBUM":
      if (!json.user_id) {
        res.json({result:'failed', description:'user_id must be provided'});
        return;
      }
      if (!isValidId(json.user_id)) {
        res.json({result:'failed', description:'invalid user_id format'});
        return;
      }
      if (!json.album_name) {
        res.json({result:'failed', description:'album_name must be provided'});
        return;
      }
      if (!json.activity) {
        res.json({result:'failed', description:'activity must be provided'});
        return;
      }
      if (!json.activity.work    || (json.activity.work != '0'   && json.activity.work != '1')
        || !json.activity.study  || (json.activity.study != '0'  && json.activity.study != '1')
        || !json.activity.food   || (json.activity.food != '0'   && json.activity.food != '1')
        || !json.activity.cafe   || (json.activity.cafe != '0'   && json.activity.cafe != '1')
        || !json.activity.sports || (json.activity.sports != '0' && json.activity.sports != '1')
        || !json.activity.game   || (json.activity.game != '0'   && json.activity.game != '1')
        || !json.activity.travel || (json.activity.travel != '0' && json.activity.travel != '1')) {
        res.json({result:'failed', description:'activity list is incomplete'});
        return;
      }
      if (!json.friend_id_list) {
        res.json({result:'failed', description:'friend_id_list must be provided'});
        return;
      }
      if (!json.img_id_list) {
        res.json({result:'failed', description:'img_id_list must be provided'});
        return;
      }
      if (isEmptyObject(json.friend_id_list)) {
        res.json({result:'failed', description:'friend_id_list is empty'});
        return;
      }
      if (isEmptyObject(json.img_id_list)) {
        res.json({result:'failed', description:'img_id_list is empty'});
        return;
      }
      db_users.find({_id:json.user_id}, {}, function(e, users) {
        if (e) return next(e);
        if (isEmptyObject(users)) {
          res.json({result:'failed', description:'user not registered'});
          return;
        }
        var user = users[0];

        // CHECK friend ids
        db_contacts.find({user_id:user._id}, {}, function(e, contacts) {
          if (e) return next(e);
          if (isEmptyObject(contacts)) {
            res.json({result:'failed', description:'no contact group found'});
            return;
          }
          var contacts = contacts[0].contacts;

          var num_friends = json.friend_id_list.length;
          var num_friends_match = 0;
          for (var i in json.friend_id_list) {
            for (var j in contacts) {
              if (json.friend_id_list[i] == contacts[j].friend_id)
                num_friends_match++;
            }
          }

          var num_friends_unknown = num_friends - num_friends_match;
          if (num_friends_unknown != 0) {
            res.json({result:'failed', description:num_friends_unknown + ' friend_ids are unknown'});
            return;
          }

          // CHECK img ids
          db_images.find({user_id:user._id.toString()}, {}, function(e, images) {
            if (e) return next(e);
            if (isEmptyObject(images)) {
              res.json({result:'failed', description:'image not found'});
              return;
            }
            var num_imgs = json.img_id_list.length;
            var num_imgs_match = 0;
            for (var i in json.img_id_list) {
              for (var j in images) {
                if (json.img_id_list[i] == images[j]._id)
                  num_imgs_match++;
              }
            }

            var num_imgs_unknown = num_imgs - num_imgs_match;
            if (num_imgs_unknown != 0) {
              res.json({result:'failed', description:num_imgs_unknown + ' img_ids are unknown'});
              return;
            }

            // INSERT to albums
            db_albums.insert({user_id:user._id, album_name:json.album_name, activity:json.activity, friend_id_list:json.friend_id_list, img_id_list:json.img_id_list}, function (e, new_album) {
              if (e) return next(e);
              res.json({result:'success', description:'new album created', album_id: new_album._id});
              return;
            });
          });
        });
      });
      break;

    case "GET_ALBUM":
      break;

    case "UPDATE_ALBUM":
      break;

    case "GET_STATS":
      break;

    default:
      res.json({result:'failed', description:'unknown type'});
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
