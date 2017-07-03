'use strict'

angular.module('mainApp')
.controller('NewLbCtrl',['$scope','routerStore','namespaceStore','$timeout','$filter','serviceList',function($scope,routerStore,namespaceStore,$timeout,$filter,serviceList){
	var vm = $scope;
	



	var init = function(){
		vm.rules = [];
		vm.protocolList = ['TCP','UDP'];
		vm.namespace = vm.currentNamespace;
		vm.protocol = vm.protocolList[0];
		serviceList.getList(vm.namespace).then(function(data){
			vm.appList = data;
			if(data.length>0){
				vm.appNow = data[0].name;
			}
			else{
				vm.appNow = null;
			}
		});
	};
	var showMsg = function(msg){
		vm.errMsg = msg;
		$timeout(function(){
			vm.errMsg = '';
		},5000);

	};

	vm.saveIng = function(){
		if(vm.loading){
			return;
		}
		if((!vm.name)||(!vm.namespace)||(!vm.appNow)){

			return;
		}
		vm.addPort();
		for(var i = 0,l = vm.appList.length;i<l;i++){
			if(vm.appList[i].name == vm.appNow){
				if(vm.appList[i].selector.matchLabels){
					
					vm.selector = vm.appList[i].selector.matchLabels;	
				}
				else{
					vm.selector = {app:vm.appNow};
				}
				break;
			}
			
		}
		if(vm.rules.length<=0){
			showMsg($filter('translate')('atLeastOnPort'));
			return;
		}
		vm.loading = true;
		routerStore.addSvc(vm).then(function(){
			vm.loading = false;
			vm.closeThisDialog('true');
		},function(e){
			vm.loading = false;
			vm.errMsg = e.message;
			// vm.closeThisDialog(e);
		});
	};
	vm.addPort = function(){
		if(!!vm.targetPort && !!vm.port){
			if(vm.port>=20000&&vm.port<=40000){
				vm.portErr = false;
				vm.rules.push({
					targetPort:vm.targetPort + '',
					protocol:vm.protocol + '',
					port:vm.port + ''
				});
				vm.targetPort = null;
				vm.protocol = vm.protocolList[0];
				vm.port = null;	
			}
			else{
				vm.portErr = true;
			}
		}
	};
	
	vm.removePort = function(index){
		var t = [];
		for(var i = 0,l = vm.rules.length;i<l;i++){
			if(i != index){
				t.push(vm.rules[i]);
			}
		}
		vm.rules = t;
	};
	init();
}]);