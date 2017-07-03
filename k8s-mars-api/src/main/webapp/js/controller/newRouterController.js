'use strict';

angular.module('mainApp')
	.controller('NewRouterController',['$scope','ngDialog',function($scope,ngDialog){
		$scope.modifyRouter = function(){
			var d = ngDialog.open({
				template:'../view/modifyRouter.html',
				width:400
			});	
		}
	}]);