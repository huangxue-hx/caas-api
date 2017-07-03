'use strict'

angular.module('mainApp')
.service('suFormat',function(){
	var matchName = function(str){
		var pattern = /[a-zA-Z0-9]*/;
		if(pattern.exec(str)){
			if(pattern.exec(str)[0] == str){
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}

	var matchLabel = function(str){
		var pattern =  /[a-zA-Z0-9-]+[=][a-zA-Z0-9-]+([,][a-zA-Z0-9-]+[=][a-zA-Z0-9-]+)*/;
		if(pattern.exec(str)){
			if(pattern.exec(str)[0] == str){
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	
	var matchIp = function(str){
		var pattern1 = /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;
		// var pattern = /[012][0-9]{0,2}[.][012][0-9]{0,2}[.][012][0-9]{0,2}[.][012][0-9]{0,2}/;
		// console.log("aaa");
		// console.log(pattern.exec(str));
		if(pattern1.exec(str)){
			// console.log(pattern1.exec(str));
			if(pattern1.exec(str)[0] == str){
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}

	// 用于新建容器中的挂载点输入验证
	var matchVolumepath = function(str){
		var pattern = /([/][0-9a-zA-Z.]+)+/;
		if(pattern.exec(str)){
			if(pattern.exec(str)[0] == str)
				return true;
			else
				return false;
		}
		else
			return false;
	}

	// 用于新建容器中的port输入验证
	var matchPort = function(str){
		var num = parseInt(str);
		if(num>=10 && num<=65535)
			return true;
		else
			return false;
	}

	//用于匹配报错信息中的not found
	var notFound = function(str){
		var pattern = /not found/gi;
		if(str.match(pattern)){
			return true;
		}
		else
			return false;
	}

	//url format
	var matchUrl = function(str){
		var pattern = /(((^https?:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)$/g;
		if(pattern.exec(str)){
			if(pattern.exec(str)[0] == str)
				return true;
			else
				return false;
		}
		else
			return false;
	}

	return{
		matchName : matchName,
		matchLabel : matchLabel,
		matchIp : matchIp,
		matchVolumepath : matchVolumepath,
		matchPort : matchPort,
		notFound : notFound,
		matchUrl : matchUrl
	}
})