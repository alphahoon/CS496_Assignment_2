var express = require('express');
var router = express.Router();
var app = express();

router.post('/', function(req, res, next) {
  var db = req.db;
  var json = req.body;
  var users = db.get('users');
  var contacts = db.get('contacts');
  
  if (isEmptyObject(json)) {
    res.json({result:'empty'});
  } else {
    if (json.type) {
      switch (json.type) {
        case "NEW_USER":
          console.log("new user");
          if (json.name == null || json.email == null)
            res.json({result:'failed', description:'name and email must be provided'});
          users.insert({name:json.name, email:json.email}, function(e, results) {
            if (e) return next(e);
            res.json({result:'success', _id:results._id, name:results.name, email:results.email});
          });
          break;
        case "GET_CONTACTS":
          console.log("get contacts");
          if (json._id == null)
            res.json({result:'failed', description:'_id must be provided'});
          users.find({_id:json._id}, {}, function(e, results) {
            if (e) return next(e);
            if (results == null)
              res.json({result:'failed', description:'user not registered'});
            //console.log(results[0]);
            //console.log(results[0]._id);
            contacts.find({user_id:results[0]._id}, {}, function(e, results) {
              //console.log(results);
              if (e) return next(e);
              if (results == null)
                res.json({result:'failed', description:'no contact group found'});
              res.json({result:'success', contacts:results[0].contacts});
            });
          });
          break;
        case "ADD_CONTACTS":
          if (json._id == null)
            res.json({result:'failed', description:'_id must be provided'});
          users.find({_id:json._id}, {}, function(e, results) {
            if (e) return next(e);
            if (results == null)
              res.json({result:'failed', description:'user not registered'});
            /*
            for (var contact in json.contacts) {
              if (contact.photo) {
                console.log(contact.photo);
                contact.photo.replace(/^data:image\/png;base64,/, "");
                var base64Data = contact.photo;
                var filename = require('crypto').createHash('md5').update(base64Data).digest('hex');
                require('fs').writeFile('/public/images/'+filename+'.png', base64Data, 'base64');
                contact.photo = 'http://52.78.200.87:3000/static/images/'+filename+'.png';
              }
            }
            */
            var user = results[0];
            /*
            for (var contact in json.contacts) {
              console.log(json.contacts[contact]);
            }
            */
            contacts.find({user_id:user._id}, {}, function(e, results) {
              if (results[0] == null) {
                contacts.insert({user_id:user._id, name:user.name, email:user.email, 
                  contacts: json.contacts}, function(e, results) {
                    if (e) return next(e);
                    res.json({result:'success', description:'new contact group created'});
                });
              } else {
                contacts.update({user_id:user._id},
                  {user_id:results[0].user_id,
                   name:results[0].name,
                   email:results[0].email,
                   contacts:results[0].contacts.concat(json.contacts)},
                  function(e, results) {
                  if (e) return next(e);
                  res.json({result:'success', description:'added contacts'});
                });
              }
            });
          });
          break;
        default:
          console.log("default");
          break;
      }
    }
    else {
      res.json({result:'type_unknown'});
    }
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

module.exports = router;
