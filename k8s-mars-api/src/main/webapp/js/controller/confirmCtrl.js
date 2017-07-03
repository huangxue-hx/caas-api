'use strict'

angular.module('mainApp')
.controller('ConfirmCtrl',['$scope',function($scope){
	var vm = $scope;
	vm.message = vm.ngDialogData.content;
	console.log(vm);

}]);