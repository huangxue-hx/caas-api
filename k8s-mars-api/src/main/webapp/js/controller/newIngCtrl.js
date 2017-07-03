'use strict'

angular.module('mainApp')
.controller('NewIngCtrl',['$scope','routerStore','serviceList','namespaceStore','$filter',function($scope,routerStore,serviceList,namespaceStore,$filter){
	var vm = $scope;
	vm.ports = [];

	var init = function(){
		serviceList.getList(vm.currentNamespace).then(function(data){
			vm.serviceList = data;
		});

		vm.namespace = vm.currentNamespace;

		routerStore.getHost().then(function(data){
			vm.host = data;
		});
	};
	

	vm.saveIng = function(){
		if(vm.loading){
			return;
		}
		if((!vm.ingName)||(!vm.namespace)){

			return;
		}
		vm.addPort();
		if(vm.ports.length<=0){
			vm.errMsg= $filter('translate')('atLeastOnPort');
			return;
		}
		vm.loading = true;
		routerStore.add({
			ingName:vm.ingName,
			namespace:vm.namespace,
			ingLabel:vm.ingLabel,
			ingAnnotation:vm.ingAnnotation,
			rules: vm.ports,
			host: vm.ingName + '.' + vm.host

		}).then(function(){
			vm.loading = false;
			vm.closeThisDialog('true');
		},function(e){
			vm.loading = false;
			vm.errMsg = e.message;
			// vm.closeThisDialog(e);
		});
	};
	vm.addPort = function(){
		if(!!vm.path &&!!vm.service&&!!vm.port){
			vm.ports.push({
				path:vm.path,
				service:vm.service,
				port:vm.port+''
			});
			vm.path = null;
			vm.service = null;
			vm.port = null;
		}
	};
	
	vm.removePort = function(index){
		var t = [];
		for(var i = 0,l = vm.ports.length;i<l;i++){
			if(i != index){
				t.push(vm.ports[i]);
			}
		}
		vm.ports = t;
	};
	init();
}]);