'use strict';

angular.module('mainApp').service('tenant',['SpHttp','$q',function(SpHttp,$q){
	var list = function(username){
		var deferred = $q.defer();
		if(!username){
			var url = '/rest/tenant/list';
		}
		else{
			var url = '/rest/tenant/list?username='+username;
		}
		SpHttp.get(url).success(function(data){
			 deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var detail = function(id){
		var deferred = $q.defer();
		var url = '/rest/tenant/detail?tenantid='+id;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var create = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url:'/rest/tenant/create',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var deleteTenant = function(id,name){
		var deferred = $q.defer();
		var sendData = {
			tenantid : id,
			tenantname : name
		}

		SpHttp({
			method: 'DELETE',
			url: '/rest/tenant/delete',
			headers:{
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var tenantUser = function(name){
		var deferred = $q.defer();
		var url = '/rest/tenant/userList?tenantname='+name;

		SpHttp.get(url).success(function(data){
			var tm = [];
			var rb = [];
			data.data.forEach(function(item,index){
				if(item.role == 'tm'){
					var duplicate = false;
					tm.forEach(function(tmitem){
						if(item.name == tmitem.name)
							duplicate = true;
					})
					if(!duplicate){
						var temp = {
							name: item.name,
							role: item.role,
							roleBindingName: item.roleBindingName,
							time: item.time
						}
						tm.push(temp);
					}
				}
				else{
					var temp = {
						name: item.name,
						role: item.role,
						namespace: item.namespace,
						roleBindingName: item.roleBindingName,
						time: item.time
					}
					rb.push(temp);
				}
			});
			var re = {
				tm: tm,
				rb: rb,
			}
			deferred.resolve(re);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var addTm = function(sendData){
		var deferred = $q.defer();
		var url = 'rest/rolebinding/tm';

		SpHttp({
			method:'POST',
			url:url,
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	var deleteTm = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'DELETE',
			url: '/rest/rolebinding/tm',
			headers:{
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	return{
		list : list,
		detail: detail,
		create: create,
		deleteTenant: deleteTenant,
		tenantUser: tenantUser,
		addTm: addTm,
		deleteTm: deleteTm
	}
}])