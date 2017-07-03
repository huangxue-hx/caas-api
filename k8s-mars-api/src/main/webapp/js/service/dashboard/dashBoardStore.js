'use strict';

angular.module('mainApp').service('dashboardStore',['SpHttp','$q','baseUrl',function(SpHttp,$q,baseUrl){
	
	var podInfo = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/podInfo').success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
		};
	var nodeCpu = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/nodemonitor',{
				params:{
					type: 'node',
					target: 'nodecpu',
					rangeType: '0'
				}
			}).success(function(data, status, headers, config){
				if(data.success){
					var res = [];
					for(var i = 0,l = data.data.results[0].series[0].values.length;i<l;i++){
						if(data.data.results[0].series[0].values[i][1] || ( (i!=0) &&(i!= (l-1)) )){

							data.data.results[0].series[0].values[i][1]*=100;
							res.push(data.data.results[0].series[0].values[i]);
						}
					}
					deferred.resolve(res);


				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
	};
	var nodeMem = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/nodemonitor',{
				params:{
					type: 'node',
					target: 'memory',
					rangeType: '0'
				}
			}).success(function(data, status, headers, config){
				if(data.success){
					var res = [];
					for(var i = 0,l = data.data.results[0].series[0].values.length;i<l;i++){
						if(data.data.results[0].series[0].values[i][1] || ( (i!=0) &&(i!= (l-1)) )){
							data.data.results[0].series[0].values[i][1] = parseFloat(data.data.results[0].series[0].values[i][1]/1024/1024).toFixed(2);
							res.push(data.data.results[0].series[0].values[i]);
						}
					}
					deferred.resolve(res);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
	};
	var nodeDisk = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/nodemonitor',{
				params:{
					type: 'node',
					target: 'disk',
					rangeType: '0'
				}
			}).success(function(data, status, headers, config){
				if(data.success){
					var res = [];
					for(var i = 0,l = data.data.results[0].series[0].values.length;i<l;i++){
						if(data.data.results[0].series[0].values[i][1] || ( (i!=0) &&(i!= (l-1)) )){
							data.data.results[0].series[0].values[i][1] = parseFloat(data.data.results[0].series[0].values[i][1]/1024/1024).toFixed(2);
							res.push(data.data.results[0].series[0].values[i]);
						}
					}
					deferred.resolve(res);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
	};
	var infraInfo = function(){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/infraInfo').success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
		
	};
	var tenantInfo = function(){
		var deferred = $q.defer();
		SpHttp.get(baseUrl.rest+'/tenant/list').success(function(data, status, headers, config){
			if(data.success){

				var res = {};
				res.tenants = data.data.length;
				res.harbor = [];
				res.namespace = [];
				for(var i = 0, l = data.data.length;i<l;i++){
					var h = {};
					var n = {};
					n.name = h.name = data.data[i].name;
					h.value = data.data[i].harborProjects.length;
					n.value = data.data[i].namespaces.length;
					res.harbor.push(h);
					res.namespace.push(n);
				}
				deferred.resolve(res);
			}
			else{
				deferred.reject(data.errMsg);
			}
		}).error(function(e,statuscode){
			deferred.reject(statuscode);
		});
		return deferred.promise;
	};
	var warningInfo = function(p){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/warningInfo',{params:p}).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
		
	};
	var eventInfo = function(p){
			var deferred = $q.defer();
			SpHttp.get(baseUrl.dashboard+'/eventInfo',{params:p}).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			}).error(function(e,statuscode){
				deferred.reject(statuscode);
			});
			return deferred.promise;
		
	};


	return {
		podInfo: podInfo,
		nodeCpu: nodeCpu,
		nodeMem: nodeMem,
		nodeDisk: nodeDisk,
		infraInfo:infraInfo,
		tenantInfo: tenantInfo,
		warningInfo: warningInfo,
		eventInfo : eventInfo
	}
}])