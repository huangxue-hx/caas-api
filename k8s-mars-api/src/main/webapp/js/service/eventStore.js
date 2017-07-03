'use strict';

angular.module('mainApp').service('eventStore',['$http','$q','baseUrl',function($http,$q,baseUrl){

		var query = function(p){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/events',{params:p}).success(function(data, status, headers, config){
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
			'query': query
		};
}]);