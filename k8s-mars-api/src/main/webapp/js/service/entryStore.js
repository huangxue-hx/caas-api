'use strict';

angular.module('mainApp').service('entryStore',['$http','$q','baseUrl',function($http,$q,baseUrl){

		var query = function(){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/router/entry').success(function(data, status, headers, config){
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