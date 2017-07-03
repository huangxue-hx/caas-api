'use strict';

angular.module("mainApp")
.controller("StorageDetailController",['$scope','$stateParams','storage','inform',function($scope,$stateParams,storage,inform){
	var name = $stateParams.name;
	var namespace = $stateParams.namespace;
	$scope.namespace = namespace;

	storage.getStorage(name,namespace).then(function(data){
		$scope.storage = data;
	});

	$scope.back = function(){
		history.back(-1);
	}
}])