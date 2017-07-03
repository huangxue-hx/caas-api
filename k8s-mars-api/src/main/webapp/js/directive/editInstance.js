'use strict';

angular.module('mainApp')
	.directive('editInstance',function(){
		return {
			restrict: 'A',
			templateUrl: '../template/editInstance.html',
			scope: true,
			controller: 'EditInstanceController'
		}
	});