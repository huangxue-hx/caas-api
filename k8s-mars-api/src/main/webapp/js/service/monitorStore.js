'use strict';

angular.module('mainApp').service('monitorStore',['SpHttp','$q',function(SpHttp,$q){
	var getMonitor = function(pod,con,range,target,startTime){
		var deferred = $q.defer();
		var url = '/rest/monitor';
		// var url = '/rest/monitor?pod='+pod+
		// 	'&&rangeType='+range+'&&target='+target;

		SpHttp.get(url,{
			params:{
				pod: pod,
				container: con,
				rangeType: range,
				target: target,
				startTime: startTime
			}
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}
	var getNetwork = function(pod,range,target,startTime){
		var deferred = $q.defer();

		var url = '/rest/monitor';
		SpHttp.get(url,{
			params:{
				pod: pod,
				rangeType: range,
				target: target,
				startTime: startTime
			}
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	return {
		getMonitor : getMonitor,
		getNetwork : getNetwork
	}
}])