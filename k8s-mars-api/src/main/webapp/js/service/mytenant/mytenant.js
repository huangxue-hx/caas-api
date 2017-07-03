'use strict';

angular.module('mainApp').service('myTenant',['SpHttp','$q',function(SpHttp,$q){
	var list = function(tenantid){
		var deferred = $q.defer();
		if(!tenantid){
			var url = '/rest/tenant/projectList';
		}
		else{
			var url = '/rest/tenant/projectList?tenantid=037b2163ae5b4287b773c70b53f7f758';
		}

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getNamespaceList = function(tenantid){
		var deferred = $q.defer();
		if(!tenantid){
			var url = '/rest/namespace';
		}
		else{
			var url = '/rest/namespace?tenantid='+tenantid;
		}

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return {
		list : list,
		getNamespaceList : getNamespaceList
	}
}])