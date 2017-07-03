'use strict';
angular.module('mainApp').controller('StorageProviderListController',StorageProviderListController);
StorageProviderListController.$inject = ['$scope','$stateParams','extendStore']
function StorageProviderListController($scope,$stateParams,extendStore){
	var vm = $scope;
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){
		extendStore.storageProviderList().then(function(data){
			vm.list = data;
		});

		//get project detail via tenantid and name
	
	}

	init();
}