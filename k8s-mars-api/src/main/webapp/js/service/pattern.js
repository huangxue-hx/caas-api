'use strict'

// 用来保存正则表达式的服务
angular.module('mainApp')
.service('pattern',[function(){
	var name = '[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*';
	var containerName = '[a-z0-9]([-a-z0-9]*[a-z0-9])?';
	var label = '[a-zA-Z0-9-]+[=][a-zA-Z0-9-]+([,][a-zA-Z0-9-]+[=][a-zA-Z0-9-]+)*';
	var ip = '^([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$';
	var path = '([/][0-9a-zA-Z.]*)+';
	var url = '(((^https?:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)$';
	var mail = '^([a-z0-9_-])+@([a-z0-9_-])+(.[a-z]{2,5})+';
	// var mail = '\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,5}';

	//tenant related pattern
	var commonName = '[a-z]+';
	
	return {
		name : name,
		containerName : containerName,
		label : label,
		ip : ip,
		path : path,
		url : url,
		commonName : commonName,
		mail : mail
	}
}])