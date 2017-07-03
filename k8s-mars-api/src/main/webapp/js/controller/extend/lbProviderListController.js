'use strict';

angular.module('mainApp').controller('LbProviderListController',LbProviderListController);
LbProviderListController.$inject = ['$scope','$stateParams','extendStore']
function LbProviderListController($scope,$stateParams,extendStore){
	var vm = $scope;
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){
		extendStore.lbProviderList().then(function(data){
			vm.list = data;
		});

		//get project detail via tenantid and name
	
	}

	init();
}