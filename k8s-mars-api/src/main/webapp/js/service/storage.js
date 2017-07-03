'use strict';

angular.module('mainApp').service('storage',['SpHttp','$q',function(SpHttp,$q){
	var getList = function(namespace){
		// console.log(namespace);
		var deferred = $q.defer();

		SpHttp.get('/rest/volume?namespace='+namespace).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var getStorage = function(name,namespace){
		var deferred = $q.defer();
		var url = "/rest/volume/detail?name="+name+"&&namespace="+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getQuery = function(query){
		var deferred = $q.defer();

		var url = '/rest/volume?name='+query;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteStorage = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'DELETE',
			url: 'rest/volume',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data: sendData
		}).success(function(res){
			deferred.resolve(res.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var newStorage = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url: 'rest/volume',
			data: sendData
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return {
		getList : getList,
		getStorage : getStorage,
		getQuery : getQuery,
		deleteStorage : deleteStorage,
		newStorage : newStorage
	}
}])