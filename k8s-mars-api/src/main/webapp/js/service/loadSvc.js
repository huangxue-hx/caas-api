'use strict';

angular.module('mainApp').service('loadSvc',[function(){
	var urlList = {};
	var triggerReq = function(url){
		for(var u in urlList){
			if(u == url){
				urlList[url][0].apply();
			}
		}
	}

	var triggerRes = function(url){
		for(var u in urlList){
			if(u == url){
				urlList[url][1].apply();
			}
		}
	}

	var reg = function(url,fn1,fn2){
		urlList[url] = [fn1,fn2];
	}

	return{
		reg: reg,
		triggerReq: triggerReq,
		triggerRes: triggerRes
	}
}]);