'use strict';

angular.module('mainApp').service('routerStore',['$http','$q','baseUrl',function($http,$q,baseUrl){

		var query = function(p){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/router/ing',{
				params:p
			}).success(function(data, status, headers, config){
				if(data.success){
					for(var i=0,l=data.data.length;i<l;i++){
						if(data.data[i].labels){
							var temp = [];
							for(var name in data.data[i].labels){
								temp.push({
									name: name,
									value: data.data[i].labels[name]
								});
							}
							data.data[i].labels = temp;
						}
					}
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var querySvc = function(p){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/router/svc',{
				params:p
			}).success(function(data, status, headers, config){
				if(data.success){
					for(var i=0,l=data.data.length;i<l;i++){
						if(data.data[i].labels){
							var temp = [];
							for(var name in data.data[i].labels){
								temp.push({
									name: name,
									value: data.data[i].labels[name]
								});
							}
							data.data[i].labels = temp;
						}
						if(data.data[i].selector){
							var s = [];
							for(var name in data.data[i].selector){
								s.push({
									name: name,
									value: data.data[i].selector[name]
								});
							}
							data.data[i].selector = s;
						}
					}
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var add = function(p){
			var labels = {};
			if(p.ingLabel&&p.ingLabel.length>0){
				var vec = p.ingLabel.split(',');
				for(var i = 0,l = vec.length;i<l;i++){
					var temp = vec[i].split('=');
					labels[temp[0]] = temp[1];
				}
			}

			var deferred = $q.defer();
			$http.post(baseUrl.ajax+'/router/ing',{
				name:p.ingName,
				namespace:p.namespace,
				labels:labels,
				annotaion:p.ingAnnotation,
				rules:p.rules,
				host: p.host

			}).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var addSvc = function(p){
			var labels = {};
			if(p.labels&&p.labels.length>0){
				var vec = p.labels.split(',');
				for(var i = 0,l = vec.length;i<l;i++){
					var temp = vec[i].split('=');
					labels[temp[0]] = temp[1];
				}
			}
			
			var deferred = $q.defer();
			$http.post(baseUrl.ajax+'/router/svc',{
				name:p.name,
				namespace:p.namespace,
				labels:labels,
				annotaion:p.annotation,
				rules:p.rules,
				selector:p.selector,
				app: p.appNow

			}).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var update = function(p){
			var deferred = $q.defer();
			$http.put(baseUrl.ajax+'/router/ing',p).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var updateSvc = function(p){
			var deferred = $q.defer();
	
			
			$http.put(baseUrl.ajax+'/router/svc',p).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var deleteItem = function(p){
			var opt = {
					method: 'DELETE',
					url: baseUrl.ajax+'/router/ing',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:p
				};
			var deferred = $q.defer();
			$http(opt).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var deleteSvcItem = function(p){
			var opt = {
					method: 'DELETE',
					url: baseUrl.ajax+'/router/svc',
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded'
					},
					data:p
				};
			var deferred = $q.defer();
			$http(opt).success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		var getHost = function(){
			var deferred = $q.defer();
			$http.get(baseUrl.ajax+'/router/host').success(function(data, status, headers, config){
				if(data.success){
					deferred.resolve(data.data);
				}
				else{
					deferred.reject(data.errMsg);
				}
			});
			return deferred.promise;
		};
		return {
			'query': query,
			'add': add,
			'addSvc': addSvc,
			'update': update,
			'deleteItem': deleteItem,
			'querySvc': querySvc,
			'updateSvc': updateSvc,
			'deleteSvcItem': deleteSvcItem,
			'getHost': getHost
		};
}]);