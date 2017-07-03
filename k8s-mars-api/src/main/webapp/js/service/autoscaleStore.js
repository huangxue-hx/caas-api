'use strict';

angular.module('mainApp')
.service('autoscaleStore',['SpHttp','$q',function(SpHttp,$q){
	var modifyScale = function(item){
		var deferred = $q.defer();
		SpHttp({
			method: 'PUT',
			url: 'rest/autoscale',
			data: item
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var start = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'post',
			url: '/rest/autoscale',
			data: sendData
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteAuto = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'DELETE',
			url: 'rest/autoscale',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
				},
			data: sendData
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return {
		modifyScale : modifyScale,
		start : start,
		deleteAuto : deleteAuto
	}
}])