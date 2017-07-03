'use strict';

angular.module('mainApp').service('initialStore',['SpHttp','$q',function(SpHttp,$q){
	var initCRole = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/clusterrole/initialization').success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var machineList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/clusterrolebinding/machineList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var addMachine = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/addMachine',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteMachine = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'DELETE',
			url:'/rest/deleteMachine',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(data){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var adminList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/clusterrolebinding/adminList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var addAdmin = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/addAdmin',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteAdmin = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'DELETE',
			url:'/rest/deleteAdmin',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var initNetwork = function(){
		var deferred = $q.defer();
		console.log('aaa');
		SpHttp.get('/rest/network/init').success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	return{
		initCRole: initCRole,
		machineList: machineList,
		addMachine: addMachine,
		deleteMachine: deleteMachine,
		adminList: adminList,
		addAdmin: addAdmin,
		deleteAdmin: deleteAdmin,
		initNetwork: initNetwork
	}
}]);