'use strict';

angular.module('mainApp').service('serviceList',['SpHttp','$q',function(SpHttp,$q){
	var getList = function(namespace){
		var deferred = $q.defer();
		
		SpHttp.get('/rest/deployments?namespace='+namespace).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var service = function(name,namespace){
		var url = '/rest/deployments/detail?name='+name+'&&namespace='+namespace;
		var deferred = $q.defer();

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getQuery = function(query,namespace){
		var deferred = $q.defer();
		var url;

		if(query.name && query.labels.length)
			url = 'rest/deployments?namespace='+namespace+'&name='+query.name+'&&labels='+query.labels.join(",");
		else if(query.name && !query.labels.length){
			url = 'rest/deployments?namespace='+namespace+'&name='+query.name;
		}
		else if(!query.name && query.labels.length){
			url = 'rest/deployments?namespace='+namespace+'&labels='+query.labels.join(",");
		}
		else{
			url = 'rest/deployments?namespace='+namespace;
		}
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var newService = function(sendData){
		var deferred = $q.defer();
		
		SpHttp({
			method: 'POST',
			url: '/rest/deployments',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var stop = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url: '/rest/deployments/stop',
			data: sendData
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var start = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url: '/rest/deployments/start',
			data: sendData
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteService = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'delete',
			url: '/rest/deployments',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data:sendData
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var instance = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url: '/rest/deployments/scale',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var event = function(service,namespace){
		var deferred = $q.defer();
		var url = '/rest/deployments/events?name='+service+'&&namespace='+namespace;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var podList = function(service,namespace){
		var deferred = $q.defer();
		var url = '/rest/deployments/podlist?name='+service+'&&namespace='+namespace;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var conList = function(service,namespace){
		var deferred = $q.defer();
		var url = '/rest/deployments/containers?name='+service+'&&namespace='+namespace;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var configMap = function(name,namespace){
		var deferred = $q.defer();
		var url = '/rest/configmap?name='+name+'&namespace='+namespace;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var configMapEdit = function(sendData){
		var deferred = $q.defer();
		var sendData = sendData;

		SpHttp({
			method: 'PUT',
			url: '/rest/configmap',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getSelector = function(){
		var deferred = $q.defer();

		SpHttp.get('/rest/node/availablelabels').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return {
		getList : getList,
		service : service,
		getQuery : getQuery,
		newService : newService,
		stop : stop,
		start : start,
		deleteService : deleteService,
		instance : instance,
		event : event,
		podList : podList,
		conList : conList,
		configMap : configMap,
		configMapEdit : configMapEdit,
		getSelector : getSelector
	}
}])