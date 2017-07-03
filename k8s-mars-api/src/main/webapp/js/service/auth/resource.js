'use strict';

angular.module('mainApp').service('resource',['SpHttp','$q',function(SpHttp,$q){
	var resourceList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/resources').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var noneResourceList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/noneResources').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return{
		resourceList : resourceList,
		noneResourceList : noneResourceList 
	}
}])