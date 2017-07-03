'use strict';

angular.module('mainApp').controller('ComponentListController',ComponentListController);
ComponentListController.$inject = ['$scope','$stateParams','nodeMonitorStore','ngDialog'];
function ComponentListController($scope,$stateParams,nodeMonitorStore,ngDialog){
	var vm = $scope;
	var init = function(){
		nodeMonitorStore.getComponentStatus().then(function(data){
			console.log(data);
			vm.compList = data;
		});
		nodeMonitorStore.listLogMonitor().then(function(data){
			vm.isLogMonitor = data.isMonit;
		});
		nodeMonitorStore.listInfluxMonitor().then(function(data){
			vm.isInfluxMonitor = data.thresholds.length;
		});
	};
	vm.logMonitor = function(){
		var d = ngDialog.open({
					template:'../view/monitor/newMonitor.html',
					width:650,
					closeByDocument: false,
					controller: 'LogAlarmController'
				});
		d.closePromise.then(function(){
			init();
		});
	};
	vm.influxMonitor = function(){
		var d = ngDialog.open({
					template:'../view/monitor/newInfluxMonitor.html',
					width:800,
					closeByDocument: false,
					controller: 'InfluxAlarmController'
				});
		d.closePromise.then(function(){
			init();
		});
	};

	init();
} 	