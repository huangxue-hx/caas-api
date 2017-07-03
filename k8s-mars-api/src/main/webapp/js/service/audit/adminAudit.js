'use strict';

angular.module('mainApp').service('adminAudit',['SpHttp','$q',function(SpHttp,$q){

	var operationList = function(){
		var deferred = $q.defer();
		
		SpHttp.get('/rest/userAudit/operations').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	var moduleList = function(){
		var deferred = $q.defer();
		
		SpHttp.get('/rest/userAudit/modules').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	var paramList = function(){
		var deferred = $q.defer();
		
		SpHttp.get('/rest/userAudit/params').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	var search = function(sendData){
		var deferred = $q.defer();
		
		SpHttp({
			method: 'POST',
			url: '/rest/userAudit/search',
			data: sendData
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	return {
		operationList: operationList,
		moduleList: moduleList,
		paramList: paramList,
		search: search
	}
}])