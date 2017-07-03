'use strict';

angular.module('mainApp').controller('DashboardController',DashboardController);
DashboardController.$inject = ['$scope','$stateParams','dashboardStore']
function DashboardController($scope,$stateParams,dashboardStore){
	var vm = $scope;
	var init = function(){
		dashboardStore.podInfo().then(function(data){
			vm.podInfo = data;
		});
		dashboardStore.nodeCpu().then(function(data){
			vm.nodeCpu = data;
			vm.nodeCpuNow = parseFloat(data[data.length-1][1]).toFixed(2);
		});
		dashboardStore.nodeMem().then(function(data){
			vm.nodeMem = data;
			var last = data[data.length-1][1];
			vm.nodeMemNow = last;
		});
		dashboardStore.nodeDisk().then(function(data){
			vm.nodeDisk = data;
			var last = data[data.length-1][1];
			vm.nodeDiskNow = last;
		});
		dashboardStore.infraInfo().then(function(data){
			vm.infraInfo = data;
		});
		dashboardStore.tenantInfo().then(function(data){
			vm.tenantInfo = data;
		});
		dashboardStore.warningInfo().then(function(data){
			vm.warningInfo = data;
		});
		dashboardStore.eventInfo().then(function(data){
			vm.eventInfo = data;
		});
	};

	init();
}