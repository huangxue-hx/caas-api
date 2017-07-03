'use strict';

angular.module('mainApp').controller('VolumeDetailController',VolumeDetailController);
VolumeDetailController.$inject = ['$scope','$stateParams','volume']
function VolumeDetailController($scope,$stateParams,volume){
	var tenantid = $stateParams.id;
	var name = $stateParams.name;
	// console.log(name);
	
	$scope.back = function(){
		history.back(-1);
	}

	var init = function(){

		//get project detail via tenantid and name
		volume.detail(tenantid,name).then(function(data){
			console.log(data);
			$scope.volume = data[0];
		})
	}

	init();
}