'use strict';

angular.module('mainApp').service('namespaceStore',['SpHttp','$q','baseUrl',function(SpHttp,$q,baseUrl){
	var getNamespace = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.ajax+'/avliablenamespace').success(function(res){
			deferred.resolve(res.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	};
	var getlist = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.ajax+'/avaliablenamespacelist').success(function(res){
			deferred.resolve(res.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	};
	var add = function(name){
		var deferred = $q.defer();
		SpHttp({
			method: 'post',
			url: baseUrl.ajax+'/namespace',
			data: {namespace:name}
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var deleteItem = function(name){
		var deferred = $q.defer();
			var opt = {
					method: 'DELETE',
					url: baseUrl.ajax+'/namespace',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:{
						namespace:name
					}
				};
			SpHttp(opt).success(function(res){
				deferred.resolve(res);
			}).error(function(err){
				deferred.reject(err);
			});
			return deferred.promise;
	};


	return{
		getNamespace: getNamespace,
		getlist: getlist,
		add: add,
		deleteItem: deleteItem
	}
}])