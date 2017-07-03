'use strict';

angular.module('mainApp').service('volume',['SpHttp','$q',function(SpHttp,$q){
	var list = function(tenantid){
		var deferred = $q.defer();
		if(!tenantid){
			var url = '/rest/volumeBytenantid';
		}
		else{
			var url = '/rest/volumeBytenantid?tenantid='+tenantid;
			// var url = '/rest/tenant/projectList?tenantid=037b2163ae5b4287b773c70b53f7f758';
		}

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var detail = function(tenantid,name){
		var deferred = $q.defer();
		var url = '/rest/volumeBytenantid/detail?tenantid='+tenantid+'&name='+name;
		// var url = '/rest/tenant/projectDetail?tenantid=037b2163ae5b4287b773c70b53f7f758&projectid='+projectid;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	var create = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/volumeBytenantid',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteVolume = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'DELETE',
			url:'/rest/volumeBytenantid/delete',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var provider = function(){
		var deferred = $q.defer();
		SpHttp.get('/rest/volumeprovider/list').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return {
		list : list,
		detail: detail,
		create: create,
		deleteVolume: deleteVolume,
		provider: provider
	}
}])