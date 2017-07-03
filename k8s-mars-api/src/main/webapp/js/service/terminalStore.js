'use strict';

angular.module('mainApp').service('terminalStore',['$http','$q','baseUrl',function($http,$q,baseUrl){

		var getTerminal = function(pod,container,namespace){
			var deferred = $q.defer();
			$http.post(baseUrl.ajax+'/terminal/getTerminal',{
				pod:pod,
				container:container,
				namespace:namespace

			}).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		

		return {
			'getTerminal':getTerminal
		};
}]);