'use strict';

angular.module('mainApp').service('menu',['SpHttp','$q',function(SpHttp,$q){
	var menuList = function(namespace){
		var deferred = $q.defer();
		// var url = '/rest/getMenu?namespace='+namespace;
		var url = '/rest/getMenu';

		SpHttp.get(url,{params:{
			namespace: namespace
		}}).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	}

	return{
		menuList : menuList
	}
}])