'use strict';

angular.module('mainApp')
.controller('WarningCtrl',['$scope','eventStore','jump',function($scope,eventStore,jump){
	var vm = $scope;

	var init = function(){
		eventStore.query({type:'Warning',namespace:vm.currentNamespace}).then(function(data){
			vm.events = data;
		},function(e){
			vm.events = [];
		});
	};

	vm.events = [];
	vm.gotoDetail = function(e){
		jump.go(e.involvedObject);
		// vm.closeThisDialog();
	};
	init();
}])