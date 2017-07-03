'use strict';

angular.module('mainApp').controller('ImageProviderListController',VolumeDetailController);
VolumeDetailController.$inject = ['$scope','$stateParams','extendStore']
function VolumeDetailController($scope,$stateParams,extendStore){
	var vm = $scope;
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){
		extendStore.imageProviderList().then(function(data){
			vm.list = data;
		});

		//get project detail via tenantid and name
	
	}

	init();
}