'use strict';

angular.module('mainApp').service('ingStore',['$http','$q','baseUrl',function($http,$q,baseUrl){

		var query = function(){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/harbor/repo').success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.msg);
				}
			});
			return deferred.promise;
		};
		var deleteRepo = function(repoName){
			var deferred = $q.defer();
			var opt = {
					method: 'DELETE',
					url: baseUrl.ajax+'/harbor/repo',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:{
						repo:repoName
					}
				};
			$http(opt).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.msg);
				}
			});
			return deferred.promise;
		};
		var deleteTag = function(repoName,tag){
			var deferred = $q.defer();
			var opt = {
					method: 'DELETE',
					url: baseUrl.ajax+'/rest/harbor/repo',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:{
						repo:repoName,
						tag:tag

					}
				};
			$http(opt).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.msg);
				}
			});
			return deferred.promise;
		};
		return {
			'query':query,
			'deleteRepo':deleteRepo,
			'deleteTag':deleteTag
		};
}]);