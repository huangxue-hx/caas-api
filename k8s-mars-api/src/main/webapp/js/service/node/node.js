'use strict';

angular.module('mainApp').service('nodeStore',['SpHttp','$q','baseUrl',function(SpHttp,$q,baseUrl){
	var nodeList = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.infrastructure+'/nodelist').success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var detail = function(nodeName){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.infrastructure+'/nodedetail',{params:{nodeName:nodeName}}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var podList = function(nodeName){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.infrastructure+'/podList',{params:{nodeName:nodeName}}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var eventList = function(nodeName){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.infrastructure+'/nodeevent',{params:{nodeName:nodeName}}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var getLog = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/monitor/log/search';

		SpHttp.get(url,
			{
				params:{
					processName: sendData.processName,
					rangeType: sendData.rangeType,
					node:sendData.node
				}
			}
			).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};

	var getLabel = function(name){
		var deferred = $q.defer();
		var url = '/rest/node/labels?name='+name;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var setLabel = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method:'PUT',
			url:'/rest/node/labels',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return{
		list : nodeList,
		detail: detail,
		podList: podList,
		eventList: eventList,
		getLog: getLog,
		getLabel: getLabel,
		setLabel: setLabel
	}
}])