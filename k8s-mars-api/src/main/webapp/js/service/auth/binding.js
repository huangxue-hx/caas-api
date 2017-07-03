'use strict';

angular.module('mainApp').service('binding',['SpHttp','$q',function(SpHttp,$q){
	var roleBindingList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/rolebindingList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var cRoleBindingList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/clusterrolebindingList').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var rBindingDetail = function(name,namespace){
		var deferred = $q.defer();
		var url = '/rest/rolebindingDetail?name='+name+'&namespace='+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var crBindingDetail = function(name){
		var deferred = $q.defer();
		var url = '/rest/clusterrolebindingDetail?name='+name;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var rBindingDelete = function(name,namespace){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			namespace: namespace
		}
		SpHttp({
			method: 'DELETE',
			url: '/rest/rolebindings',
			headers: {
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

	var crBindingDelete = function(name){
		var deferred = $q.defer();
		var sendData = {
			name: name
		}
		SpHttp({
			method: 'DELETE',
			url: '/rest/clusterrolebindings',
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

	var rBindingAdd = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'POST',
			url: '/rest/rolebindings',
			data: sendData,
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var crBindingAdd = function(name,user,role){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			user: user,
			clusterRole: role
		}
		SpHttp({
			method: 'POST',
			url: '/rest/clusterrolebindings',
			data: sendData,
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var rBindingPut = function(name,namespace,user,role){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			namespace: namespace,
			user: user,
			role: role
		}
		SpHttp({
			method: 'PUT',
			url: '/rest/rolebindings',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var crBindingPut = function(name,user,role){
		var deferred = $q.defer();
		var sendData = {
			name: name,
			user: user,
			clusterRole: role
		}
		SpHttp({
			method:'PUT',
			url:'/rest/clusterrolebindings',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var bindUser = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/rolebinding/user',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteBindUser = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'DELETE',
			url: '/rest/rolebinding/user',
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
		roleBindingList : roleBindingList,
		cRoleBindingList : cRoleBindingList,
		rBindingDetail : rBindingDetail,
		crBindingDetail : crBindingDetail,
		rBindingDelete : rBindingDelete,
		crBindingDelete : crBindingDelete,
		rBindingAdd : rBindingAdd,
		crBindingAdd : crBindingAdd,
		rBindingPut : rBindingPut,
		crBindingPut : crBindingPut ,
		bindUser : bindUser,
		deleteBindUser : deleteBindUser
	}

}])