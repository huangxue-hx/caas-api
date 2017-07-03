'use strict';

angular.module('mainApp').service('authData',['SpHttp','$q',function(SpHttp,$q){
	var roleList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/rolelist').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var cRoleList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/clusterroleList').success(function(data){
			// var temp = [];
			// data.data.forEach(function(item){
			// 	if(item.name != 'tm'){
			// 		temp.push(item);
			// 	}
			// });
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}
	var cRoleListforNew = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/clusterroleList').success(function(data){
			var temp = [];
			data.data.forEach(function(item){
				if(item.name != 'tm' && item.name!='admin'){
					temp.push(item);
				}
			});
			deferred.resolve(temp);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	} 

	var roleDetail = function(name,namespace){
		var deferred = $q.defer();
		var url = '/rest/roleDetail?name='+name+'&namespace='+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var cRoleDetail = function(name){
		var deferred = $q.defer();
		var url = '/rest/clusterroleDetail?name='+name;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var addCRole = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url:'/rest/clusterroles',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var addRole = function(name,namespace,resource,noneResource){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			namespace: namespace,
			resource:resource,
			noneResource: noneResource
		}
		SpHttp({
			method: 'POST',
			url: '/rest/roles',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var deleteRole = function(name,namespace){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			namespace: namespace
		}
		SpHttp({
			method: 'DELETE',
			url:'/rest/roles',
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

	var deleteCRole = function(name){
		var deferred = $q.defer();
		var sendData = {
			name: name
		}
		SpHttp({
			method: 'DELETE',
			url:'/rest/clusterroles',
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

	var putRole = function(name,namespace,resource,noneResource){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			namespace: namespace,
			resource: resource,
			noneResource: noneResource
		}
		SpHttp({
			method: 'PUT',
			url: '/rest/roles',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var putCRole = function(sendData){
		var deferred = $q.defer();
		
		SpHttp({
			method: 'PUT',
			url: '/rest/clusterroles',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var roleListNamespace = function(namespace){
		var deferred = $q.defer();
		var url = '/rest/roles/namespace?namespace='+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var harborRole = function(){
		var deferred = $q.defer();

		var url = '/rest/harborProject/role';
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	} 

	return {
		roleList : roleList,
		cRoleList : cRoleList,
		cRoleListforNew : cRoleListforNew,
		roleDetail : roleDetail,
		cRoleDetail : cRoleDetail,
		addCRole : addCRole,
		addRole : addRole,
		deleteRole : deleteRole,
		deleteCRole : deleteCRole,
		putRole : putRole,
		putCRole : putCRole,
		roleListNamespace : roleListNamespace,
		harborRole : harborRole
	}
}])