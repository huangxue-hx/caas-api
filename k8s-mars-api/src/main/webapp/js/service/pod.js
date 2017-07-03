'use strict'

angular.module('mainApp').service('pod',['SpHttp','$q',function(SpHttp,$q){

	var getPod = function(name,namespace){
		var deferred = $q.defer();
		var url = '/rest/deployments/pod?name='+name+'&&namespace=' + namespace;
		SpHttp.get(url).success(function(data){
			if(data.success){
				deferred.resolve(data.data);
			}
			else
				deferred.reject(data.errMsg);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getLog = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/deployments/pod/container/log?container='+sendData.container+'&&pod='+sendData.pod+'&&namespace='+sendData.namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}
	var getLogOrigin = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/deployments/pod/app/log?container='+sendData.container+'&&pod='+sendData.pod+'&&namespace='+sendData.namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getlogdir = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/deployments/pod/container/logFileList?container='+sendData.container+'&&namespace='+sendData.namespace;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getlog = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/deployments/pod/container/log?container='+sendData.container+'&&namespace='+sendData.namespace+'&&rangeType='+sendData.rangeType+'&&logdir='+sendData.logdir+'&&scrollId='+sendData.scrollId;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var editContainer = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'PUT',
			url: 'rest/deployments',
			data: sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return{
		getPod : getPod,
		getLog : getLog,
		editContainer : editContainer,
		getlogdir : getlogdir,
		getlog : getlog,
		getLogOrigin: getLogOrigin
	}
}])