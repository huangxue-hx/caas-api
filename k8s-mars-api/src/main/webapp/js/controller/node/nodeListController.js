'use strict';

angular.module('mainApp')
.controller('NodeListCtrl',['$scope','nodeStore','nodeMonitorStore','ngDialog',function($scope,nodeStore,monitorStore,ngDialog){
	var vm = $scope;
	var init = function(){
		nodeStore.list().then(function(data){
			vm.list = data;
		});
		monitorStore.listLogMonitor().then(function(data){
			vm.isLogMonitor = data.isMonit;
		});
		monitorStore.listInfluxMonitor().then(function(data){
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
}])