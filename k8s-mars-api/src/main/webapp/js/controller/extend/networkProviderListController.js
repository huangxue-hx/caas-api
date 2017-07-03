'use strict';
angular.module('mainApp').controller('NetworkProviderListController',NetworkProviderListController	);
NetworkProviderListController.$inject = ['$scope','$stateParams','extendStore']
function NetworkProviderListController($scope,$stateParams,extendStore){
	var vm = $scope;
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){
		extendStore.networkProviderList().then(function(data){
			vm.list = data;
		});

		//get project detail via tenantid and name
	
	}

	init();
}