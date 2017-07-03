'use strict'

angular.module('mainApp')
.controller('NewTermCtrl',['$scope','routerStore','$stateParams','serviceList','pod','terminalStore','$window',function($scope,routerStore,$stateParams,serviceList,pod,terminalStore,$window){
	var vm = $scope;
	var depName = $stateParams.name;
	var ns = $stateParams.namespace;
	vm.sn = '';
	var getPodList = function(name,namespace){
		serviceList.service(name,namespace).then(function(data){
			vm.podList = data.podList;
			if(vm.podList.length){
				vm.pod = vm.podList[0].name;
				getContainerList(vm.podList[0].name,namespace);
			}
		});	
	};
	var getContainerList = function(name,namespace){
		pod.getPod(name,namespace).then(function(data){
			vm.containerList = data.containers;
			if(vm.containerList.length){
				vm.container = vm.containerList[0].name;
			}
		})
	};
	vm.getSn = function(c,p){
		terminalStore.getTerminal(p,c,ns).then(function(data){
			vm.sn = data;
		});
	};
	vm.$watch('container',function(n,o){
		if(n!=o){
			vm.sn = '';
		}
	});
	vm.$watch('pod',function(n,o){
		if(n!=o){
			vm.sn = '';
		}
	});
	vm.openTerminal = function(){
		$window.open('/terminal?Sn='+vm.sn,'newwindow', 'height=600, width=800, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, status=no');
	};
	getPodList(depName,ns);
	
}]);