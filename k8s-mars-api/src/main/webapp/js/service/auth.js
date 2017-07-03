'use strict';

angular.module('mainApp').service('authStore',['$http','$q','baseUrl',function($http,$q,baseUrl){

		var getUser = function(p){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/currentuser').success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
		};
		var login = function(p){
			
			var deferred = $q.defer();
			$http.post(baseUrl.ajax+'/login',p).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,h){
				deferred.reject(e);
			});
			return deferred.promise;
		};
		var logout = function(){
			var deferred = $q.defer();
			$http.post(baseUrl.ajax+'/logout').success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var checkSecret = function(ns){
			var deferred = $q.defer();
			$http.get(baseUrl.rest+'/secret/checked',{params:{
				namespace: ns
			}}).success(function(data, status, headers, config){
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
			'getUser': getUser,
			'login': login,
			'logout': logout,
			'checkSecret': checkSecret
		};
}]);