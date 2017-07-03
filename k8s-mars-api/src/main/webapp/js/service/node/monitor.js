'use strict';

angular.module('mainApp').service('nodeMonitorStore',['SpHttp','$q',function(SpHttp,$q){
	var cache = {};
	var getMonitor = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/monitor/node';

		SpHttp.get(url,{
			params:{
				type: sendData.type,  //process node pod 
				rangeType: sendData.range, //time
				target: sendData.target,  // processcpu mem ...
				name: sendData.name,   //node
				startTime: sendData.startTime, 
				processName: sendData.processName // process name
			}
		}).success(function(data){
			var res = [];
			if(data.data.results[0].series){
				for(var i = 0,l = data.data.results[0].series[0].values.length;i<l;i++){
					var c = !!data.data.results[0].series[0].values[i][1];
					var v = (i!=0);
					var d = (i!= (l-1));
					if(data.data.results[0].series[0].values[i][1] ||( (i!=0) &&(i!= (l-1)) )){
						if(sendData.target == 'nodecpu'){
							data.data.results[0].series[0].values[i][1] *=100;
						}
						res.push(data.data.results[0].series[0].values[i]);
					}
				}
				data.data.results[0].series[0].values = res;
			}
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var getNetwork = function(sendData){
		var deferred = $q.defer();
		var url = '/rest/monitor';
		SpHttp.get(url,{
			params:{
				type: sendData.type,
				rangeType: sendData.range,
				target: sendData.target,
				name: sendData.name,
				startTime: sendData.startTime
			}
		}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	var listLogMonitor = function(){
		var deferred = $q.defer();

		var url = '/rest/monitor/log/listMonitor';
		SpHttp.get(url).success(function(data){
			var flag = false;
			var res = {};
			for(var i =0,l= data.data.length;i<l;i++){
				if(data.data[i].log_monitor || data.data[i].restart_monitor){
					flag = true;
				}
			}
			res.isMonit = flag;
			res.data = data.data
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	};
	var setLogMonitor = function(p){
		var deferred = $q.defer();
		SpHttp({
			method: 'POST',
			url: '/rest/monitor/log/setMonitor',
			data: p
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var listInfluxMonitor = function(){
		var deferred = $q.defer();

		var url = '/rest/monitor/threshold/list';
		SpHttp.get(url).success(function(data){
			var threshold = data.data.thresholds;
			for(var i = 0,l = threshold.length;i < l;i++){
				threshold[i].alarm_contact = threshold[i].alarm_contact.split(',');
			}
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	};
	var deleteInfluxItem = function(item){
		var deferred = $q.defer();

		SpHttp({
			method: 'DELETE',
			url:'/rest/monitor/threshold/delete',
			headers:{
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data: {id: item.id}
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;



	};
	var addInfluxItem = function(item){
		var deferred = $q.defer();
		SpHttp({
			method: 'POST',
			url: '/rest/monitor/threshold/create',
			data: item
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var getEmailSetting = function(){
		var deferred = $q.defer();

		var url = '/rest/monitor/log/listAlertEmail';
		SpHttp.get(url).success(function(data){
			
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	};
	var setEmailSetting = function(p){

		var deferred = $q.defer();
		SpHttp({
			method: 'POST',
			url: '/rest/monitor/log/setAlertEmail',
			data: p
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var getComponentStatus = function(p){

		var updateCache = function(def){
			cache.getComponentStatus.lastUpdateTime = new Date().getTime();
			var url = '/rest/monitor/process/status';
			SpHttp.get(url,{params:p}).success(function(data){
				var res = [];
				var d = data.data.results[0].series;
				for(var i = 0,l = d.length;i<l;i++){
					var tmp = {};
						tmp.name = d[i].tags.container_name;
						tmp.host = d[i].tags.host_id;
						tmp.time = d[i].values[0][0];
						tmp.status = d[i].values[0][1];
						res.push(tmp);
				}
				cache.getComponentStatus.data = res;
				def.resolve(res);
			}).error(function(err){
				def.reject(err);
			});	
		};


		var deferred = $q.defer();
		if(cache.getComponentStatus){
			var ts = new Date().getTime() - cache.getComponentStatus.lastUpdateTime;
			if(ts<0){
				deferred.resolve(cache.getComponentStatus.data);
			}
			else{
				updateCache(deferred);
			}
		}
		else{
			cache.getComponentStatus = {};
			updateCache(deferred);
		}

		
		return deferred.promise;
	};
	return {
		getMonitor : getMonitor,
		getNetwork : getNetwork,
		listLogMonitor: listLogMonitor,
		setLogMonitor: setLogMonitor,
		listInfluxMonitor: listInfluxMonitor,
		getEmailSetting: getEmailSetting,
		setEmailSetting: setEmailSetting,
		deleteInfluxItem: deleteInfluxItem,
		addInfluxItem: addInfluxItem,
		getComponentStatus: getComponentStatus
	}
}])