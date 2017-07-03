'use strict';
angular.module('mainApp').controller('MonitorProviderListController',MonitorProviderListController);
MonitorProviderListController.$inject = ['$scope','$stateParams','extendStore']
function MonitorProviderListController($scope,$stateParams,extendStore){
	var vm = $scope;
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){
		extendStore.monitorProviderList().then(function(data){
			vm.list = data;
		});

		//get project detail via tenantid and name
	
	}

	init();
}