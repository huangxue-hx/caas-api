'use strict';

angular.module('mainApp').service('harborStore',['SpHttp','$q',function(SpHttp,$q){
	var list = function(tenantid){
		var deferred = $q.defer();
		if(!tenantid){
			var url = '/rest/tenant/projectList?tenantid=';
		}
		else{
			var url = '/rest/tenant/projectList?tenantid='+tenantid;
			// var url = '/rest/tenant/projectList?tenantid=037b2163ae5b4287b773c70b53f7f758';
		}

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var detail = function(tenantid,projectid){
		var deferred = $q.defer();
		var url = '/rest/tenant/projectDetail?tenantid='+tenantid+'&projectid='+projectid;
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
			url:'/rest/tenant/createProject',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteHarbor = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'DELETE',
			url:'/rest/tenant/deleteProject',
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

	var userList = function(tenantname,namespace){
		var deferred = $q.defer();
		var url = '/rest/harborProject/namespace/user?tenantname='+tenantname+'&namespace='+namespace;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	var addRole = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'POST',
			url:'/rest/harborProject/role',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteRole = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'DELETE',
			url:'/rest/harborProject/role',
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

	var harborAnalyze = function(name){
		var deferred = $q.defer();

		var url = '/rest/harborProject/security/clairStatistcsOfProject?name='+name;
		// var url = '/rest/harborProject/security/clairStatistcsOfProject?name=library';
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var repoList = function(id){
		var deferred = $q.defer();

		var url = '/rest/image/repo?pid='+id;
		// var url = '/rest/image/repo?pid=2';
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var repoDetail = function(harbor,repo,tag){
		var deferred = $q.defer();

		var url = '/rest/image/repo/tag?projectName='+harbor+'&repoName='+repo+'&tag='+tag;
		// var url = '/rest/image/repo/tag?projectName=default&repoName=ubuntu&tag=latest';
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var listViaUser = function(namespace){
		var deferred = $q.defer();

		var url = '/rest/harborProject/user/image?namespace='+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var statistcsByNamespace = function(namespace){
		var deferred = $q.defer();

		var url = '/rest/harborProject/clairStatistcsByNamespace?namespace='+namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var refresh = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/harborProject/refresh').success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return {
		list : list,
		detail: detail,
		create: create,
		deleteHarbor: deleteHarbor,
		addRole: addRole,
		deleteRole: deleteRole,
		userList: userList,
		//addRole: addRole,
		//deleteRole: deleteRole,
		harborAnalyze: harborAnalyze,
		repoList: repoList,
		repoDetail: repoDetail,
		listViaUser: listViaUser,
		statistcsByNamespace: statistcsByNamespace,
		refresh: refresh
	}
}])