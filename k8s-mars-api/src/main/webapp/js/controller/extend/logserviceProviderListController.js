'use strict';

angular.module('mainApp').controller('LogProviderListController',LogProviderListController);
LogProviderListController.$inject = ['$scope','$stateParams','extendStore']
function LogProviderListController($scope,$stateParams,extendStore){
	var vm = $scope;
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){
		extendStore.logProviderList().then(function(data){
			vm.list = data;
		});

		//get project detail via tenantid and name
	
	}

	init();
}