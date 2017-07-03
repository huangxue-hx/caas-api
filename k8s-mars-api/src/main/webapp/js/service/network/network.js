'use strict';

angular.module('mainApp').service('Network',['SpHttp','$q',function(SpHttp,$q){
	var list = function(tenantid){
		var deferred = $q.defer();
		if(!tenantid){
			var url = '/rest/network/list';
		}
		else{
			var url = '/rest/network/list?tenantid='+tenantid;
		}

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var detail = function(tenantid,networkid){
		var deferred = $q.defer();
		var url = '/rest/network/detail?tenantid='+tenantid+'&networkid='+networkid;

		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var availableList = function(tenantid,networkid){
		var deferred = $q.defer();
		var url = '/rest/network/detail?tenantid='+tenantid+'&networkid='+networkid;

		SpHttp.get(url).success(function(data){

			var tmp = [];
			for(var i = 0,l = data.data[0].subnets.length;i< l;i++){
				if(!data.data[0].subnets[i].binding){
					tmp.push(data.data[0].subnets[i]);
				}
			}
			data.data[0].subnets = tmp;
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}
	var create = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url:'/rest/network/create',
			data:sendData
		}).success(function(data){
			deferred.resolve(data);
		}).error(function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	}

	var deleteNetwork = function(sendData){
		var deferred = $q.defer();
		// var sendData = {
		// 	tenantid: tenantid,
		// 	networkid: networkid
		// }

		SpHttp({
			method: 'DELETE',
			url:'/rest/network/delete',
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
		list: list,
		detail: detail,
		create: create,
		deleteNetwork: deleteNetwork,
		availableList: availableList
	}
}])