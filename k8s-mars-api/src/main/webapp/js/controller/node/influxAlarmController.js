'use strict';

angular.module('mainApp')
.controller('InfluxAlarmController',['$scope','nodeMonitorStore',function($scope,monitorStore){
	var vm = $scope;
	vm.moduleList = ['kube-apiserver','kube-controller','kubelet','kube-proxy','kube-scheduler'];
	vm.metricList = ['process/cpu_usage_rate','process/memory_usage_rate'];
	
	vm.isAdd = false;
	var init = function(){
		monitorStore.listInfluxMonitor().then(function(data){
			vm.alarmList = data.thresholds;
		});
		vm.currentRate = 1;
		vm.currentModule = 'kube-apiserver';
		vm.measurement = 'process/cpu_usage_rate';
		vm.currentContact = '';
	};
	vm.deleteItem = function(item,index){
		monitorStore.deleteInfluxItem(item).then(function(){
			var tmp = [];
			for(var i = 0,l = vm.alarmList.length;i<l;i++){
				if(i != index){
					tmp.push(vm.alarmList[i]);
				}
			}
			vm.alarmList = tmp;
		});
	};
	vm.saveAlarm = function(){
		vm.closeThisDialog('true');
	};
	vm.add = function(){
		vm.isAdd = true;
	};
	vm.cancelAdd = function(){
		vm.isAdd = false;
	};
	vm.addItem = function(){
		monitorStore.addInfluxItem({
			processName: vm.currentModule,
			measurement: vm.currentMeasurement,
			threshold: vm.currentRate,
			alarmType: 'email',
			alarmContact: vm.currentContact
		}).then(function(){
			init();
			vm.cancelAdd();
		});
	};
	init();
}])