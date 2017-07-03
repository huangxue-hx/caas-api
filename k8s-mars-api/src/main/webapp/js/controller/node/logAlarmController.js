'use strict';

angular.module('mainApp')
.controller('LogAlarmController',['$scope','nodeMonitorStore',function($scope,monitorStore){
	var vm = $scope;
	var init = function(){
		vm.alarmList = {W: '警告和错误',E: '错误'};
		monitorStore.listLogMonitor().then(function(data){
			vm.logMonitor = data;
		});
		monitorStore.getEmailSetting().then(function(data){
			vm.emailSetting = data;

		});

	};

	vm.saveAlarm = function(){
		monitorStore.setEmailSetting(vm.emailSetting).then(function(data){
			vm.closeThisDialog('true');
		});
		for(var i = 0,l=vm.logMonitor.data.length;i<l;i++){
			vm.logMonitor.data[i].log_monitor = vm.logMonitor.data[i].log_monitor?1:0;
			vm.logMonitor.data[i].restart_monitor = vm.logMonitor.data[i].restart_monitor?1:0;

			monitorStore.setLogMonitor(vm.logMonitor.data[i]);
		}

	};
	init();
}])