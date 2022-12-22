var mysql = require('mysql');

var pool;

exports.connect = function(){
	pool = mysql.createPool({
		connectionLimit: 100,
		host: 'localhost',
		user: 'root',
		password: 'bestfood',
		database: 'tvinfo'
	});
}

exports.get = function(){
	return pool;
}