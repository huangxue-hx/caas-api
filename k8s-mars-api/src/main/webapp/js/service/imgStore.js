'use strict';

angular.module('mainApp').service('imgStore',['SpHttp','$q','baseUrl',function(SpHttp,$q,baseUrl){

		var query = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.ajax+'/image/repo').success(function(data, status, headers, config){
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
					url: baseUrl.ajax+'/image/repo',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:{
						repo:repoName
					}
				};
			SpHttp(opt).success(function(data, status, headers, config){
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
					url: baseUrl.ajax+'/image/repo',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:{
						repo:repoName,
						tag:tag

					}
				};
			SpHttp(opt).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.msg);
				}
			});
			return deferred.promise;
		};
		var getImgHost = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.ajax+'/imghost').success(function(data, status, headers, config){
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
			'deleteTag':deleteTag,
			'getImgHost':getImgHost
		};
}]);