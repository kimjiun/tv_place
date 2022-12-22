var express = require('express');
var db = require('../db')
var router = express.Router();
var LOADING_SIZE = 20;
var R = require('r-script');
var fs = require('fs');

//recommend/list
router.get('/list', function(req, res, next) {
  var member_seq = req.query.member_seq;
  var user_latitude = req.query.user_latitude || DEFAULT_USER_LATITUDE;
  var user_longitude = req.query.user_longitude || DEFAULT_USER_LONGITUDE;
  var current_page = req.query.current_page || 0;
  var cftype = req.query.cf_type || 0;
  var recc_member_seq = member_seq;

  if(member_seq >= 30)
	 recc_member_seq = member_seq % 30 + 1;
	
  var rFilePath = "./rscript/" + cftype +"_cf.R";
  var out = R(rFilePath).data(Number(recc_member_seq)).callSync();

  var textfilePath = "./rscript/data/" + cftype + "/user" + recc_member_seq +".txt";

  fs.readFile(textfilePath, function(err, data) {
	    if(err) throw err;
	    var array = data.toString().split("\n");

	    for(i in array) {
	    	array[i] = Number(array[i]);
	    	if(array[i] == 0)
	    		array.splice(i, 1);
	    }
	 
	 	var Msg = cftype + " Recommend for ID : " + member_seq;
	    console.log(Msg);
	    console.log("Model Accuracy");
	    console.log("[  RMSE     MSE     MAE  ]");
	    console.log(out);
	    console.log("Recommend Items");
	    console.log(array);

	    var whereCondition = array.toString();


	    var start_page = current_page * LOADING_SIZE;

	    var sql = 
	    "select a.*, " +
	    "  (( 6371 * acos( cos( radians(?) ) * cos( radians( latitude ) ) * cos( radians( longitude ) - radians(?) )  " +
	    "  + sin( radians(?) ) * sin( radians( latitude ) ) ) ) * 1000) AS user_distance_meter, " +
	    "  if( exists(select * from tvinfo_keep where member_seq = ? and info_seq = a.seq), 'true', 'false') as is_keep, " +
	    "  (select filename from tvinfo_info_image where info_seq = a.seq) as image_filename " +
	    "from tvinfo_info as a " +
	    "where a.seq in (" + whereCondition +") " +  
	    "order by user_distance_meter "+
	    "limit ? , ? ; ";

		console.log("sql : " + sql);

		var params = [user_latitude, user_longitude, user_latitude, ,start_page, LOADING_SIZE];

		console.log("params : " + params);

		db.get().query(sql, params, function (err, rows) {
		    if (err) return res.sendStatus(400);		      
		    console.log("rows : " + JSON.stringify(rows));      
		    res.status(200).json(rows);
		});
  });
});

module.exports = router;