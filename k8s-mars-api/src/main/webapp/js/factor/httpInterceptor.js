'use strict';

angular.module('mainApp')
.factory("httpInterceptor",['$q','$rootScope','loadSvc',function($q,$rootScope,loadSvc){
	return{
		request:function(config){
			loadSvc.triggerReq(config.url);
			if(config.url == "/rest/harbor/repo" && config.method == "GET"){
				if(angular.element("#loadWait")[0])
					angular.element("#loadWait")[0].innerHTML = "<img src='../images/loading.gif' class='load-img'>";
			}

			if(angular.element(".nomore")[0]){
				var nomoreList = angular.element(".nomore");
				// console.log(nomoreList);
				for(var i=0;i<nomoreList.length;i++){
					nomoreList[i].innerHTML="";
				}
			}
			return config || $q.when(config);
		},
		requestError: function(rejection){

			return $q.reject(rejection);
		},
		response: function(response){
			loadSvc.triggerRes(response.config.url);

			if(response.config.url == "/rest/harbor/repo"){
				if(angular.element("#loadWait")[0])
					angular.element("#loadWait")[0].innerHTML = "";
			}
			
			if(angular.element(".nomore")[0]){
				var nomoreList = angular.element(".nomore");
				// console.log(nomoreList);
				for(var i=0;i<nomoreList.length;i++){
					// nomoreList[i].innerHTML="暂无数据";
				}
			}
			return response || $q.when(response);
		},
		responseError: function(rejection){
			return $q.reject(rejection);
		}
	}
}]);