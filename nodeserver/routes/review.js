var express = require('express');
var db = require('../db')
var router = express.Router();
var LOADING_SIZE = 20;

//review/insert
router.post('/insert', function(req, res, next) {
  if (!req.body.review_member_seq) {
    return res.sendStatus(400);
  }
   console.log("insert hello");

  var member_seq = req.body.review_member_seq;
  var item_seq = req.body.review_item_seq;
  var rate = req.body.rate;
  var review = req.body.review;
 
  var sql_insert = 
    "insert into tvinfo_rate (member_seq, item_seq, rate, review) " +
    "values(?, ?, ?, ?); ";

  console.log(sql_insert);

  var params = [member_seq, item_seq, rate, review];
  console.log(params);

  db.get().query(sql_insert, params, function (err, result) {
  		if (err) return res.sendStatus(400);
        res.sendStatus(200); 
  });
});

//review/list
router.get('/list', function(req, res, next) {
  var item_seq = req.query.item_seq;
  var current_page = req.query.current_page || 0;

  if (!item_seq) {
    return res.sendStatus(400);
  }

  var start_page = current_page * LOADING_SIZE;

  console.log(item_seq + ", " + current_page + ", " + start_page);

  var sql = 
    "select a.seq as review_seq, a.member_seq as review_member_seq, a.reg_date as review_date, " +
    " rate, review, a.item_seq as review_item_seq, " +
    "  (select member_icon_filename from tvinfo_member where seq = a.member_seq) as member_icon_filename," +
    "  (select name from tvinfo_member where seq = a.member_seq) as name " +
    "from tvinfo_rate as a " +
    "where a.item_seq = ? " + 
    "order by a.reg_date desc " +
    "limit ? , ? ; ";
  console.log("sql : " + sql);

  var params = [item_seq, start_page, LOADING_SIZE];

  db.get().query(sql, params, function (err, rows) {
      if (err) {
      	console.log(err.message);
      	return res.sendStatus(400);
      }

      console.log("rows : " + JSON.stringify(rows));      
      res.status(200).json(rows);
  });
});

//review/mylist
router.get('/mylist', function(req, res, next) {
  var member_seq = req.query.member_seq;
  var current_page = req.query.current_page || 0;

  if (!member_seq) {
    return res.sendStatus(400);
  }

  var start_page = current_page * LOADING_SIZE;

  console.log(member_seq + ", " + current_page + ", " + start_page);

  var sql = 
    "select a.seq as review_seq, a.member_seq as review_member_seq, a.reg_date as review_date, " +
    " rate, review, a.item_seq as review_item_seq, " +
    "  (select filename from tvinfo_info_image where seq = a.item_seq) as image_filename," +
    "  (select name from tvinfo_info where seq = a.item_seq) as name " +
    "from tvinfo_rate as a " +
    "where a.member_seq = ? " + 
    "order by a.reg_date desc " +
    "limit ? , ? ; ";
  console.log("sql : " + sql);

  var params = [member_seq, start_page, LOADING_SIZE];

  db.get().query(sql, params, function (err, rows) {
      if (err) {
      	console.log(err.message);
      	return res.sendStatus(400);
      }

      console.log("rows : " + JSON.stringify(rows));      
      res.status(200).json(rows);
  });
});

module.exports = router ;