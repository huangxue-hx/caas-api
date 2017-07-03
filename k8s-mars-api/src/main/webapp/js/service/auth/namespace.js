'use strict';

angular.module('mainApp').service('namespace',['SpHttp','$q',function(SpHttp,$q){
	var namespaceList = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/namespaces').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise; 
	}

	var list = function(tenantid,tenantname){
		var deferred = $q.defer();
		if(!tenantid){
			var url = '/rest/namespace';
		}
		else{
			var url = '/rest/namespace?tenantid='+tenantid+'&tenantname='+tenantname;
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
		var url = '/rest/namespace/detail?tenantid='+tenantid+'&name='+name;
		// var url = '/rest/tenant/projectDetail?tenantid=037b2163ae5b4287b773c70b53f7f758&projectid='+projectid;

		SpHttp.get(url).success(function(data){
			if(!!data.data.quota){
				if(data.data.quota.memory[0].indexOf('G')>=0){
					data.data.quota.memory[0] = parseInt(data.data.quota.memory[0])*1024 + 'Mi';
				}
				if(data.data.quota.memory[1].indexOf('G')>=0){
					data.data.quota.memory[1] = parseInt(data.data.quota.memory[1])*1024 + 'Mi';
				}
				if(data.data.quota.cpu[0].indexOf('m')<0){
					data.data.quota.cpu[0] = parseInt(data.data.quota.cpu[0])*1000 + 'm';
				}
				if(data.data.quota.cpu[1].indexOf('m')<0){
					data.data.quota.cpu[1] = parseInt(data.data.quota.cpu[1])*1000 + 'm';
				}
				data.data.quota.memory[0] = parseInt(data.data.quota.memory[0]);
				data.data.quota.memory[1] = parseInt(data.data.quota.memory[1]);
				data.data.quota.cpu[0] = parseInt(data.data.quota.cpu[0]);
				data.data.quota.cpu[1] = parseInt(data.data.quota.cpu[1]);
			}

			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var create = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/namespace',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var update = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/namespace/update',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteProject = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'DELETE',
			url:'/rest/namespace',
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

	var getUser = function(tenant,namespace){
		var deferred = $q.defer();
		var url = '/rest/tenant/namespace/userList?tenantname='+tenant+'&namespace='+namespace;

		// var url1 = '/rest/tenant/namespace/userList?tenantname=tekk1&namespace=test';

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}
	var getNsByUser = function(tid){
		var deferred = $q.defer();
		var url = '/rest/currentNamespaces';
		SpHttp.get(url,{params:{tenantid: tid}}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	};
	// get deployment number of user by namespace
	var userNum = function(namespace){
		var deferred = $q.defer();
		var url = '/rest/deployments/namespace/userNum?namespace='+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	return {
		namespaceList : namespaceList,
		list : list,
		detail : detail,
		create : create,
		update : update,
		deleteProject : deleteProject,
		getUser : getUser,
		getNsByUser: getNsByUser,
		userNum: userNum
	}
}])