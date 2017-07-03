'use strict';

angular.module('mainApp').service('extendStore',['SpHttp','$q','baseUrl',function(SpHttp,$q,baseUrl){
	var monitorProviderList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.cloudPlatform+'/monitor/providerList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var imageProviderList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.cloudPlatform+'/image/providerList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var logProviderList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.cloudPlatform+'/es/providerList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var storageProviderList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.cloudPlatform+'/storage/providerList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var networkProviderList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.cloudPlatform+'/network/providerList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var lbProviderList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.cloudPlatform+'/haProxy/providerList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	return{
		monitorProviderList: monitorProviderList,
		imageProviderList: imageProviderList,
		logProviderList: logProviderList,
		storageProviderList: storageProviderList,
		networkProviderList: networkProviderList,
		lbProviderList: lbProviderList


	}
}])