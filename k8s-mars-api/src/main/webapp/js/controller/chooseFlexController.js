'use strict';

angular.module('mainApp')
.controller('ChooseFlexController',['$scope','ngDialog','$http','inform','autoscaleStore',function($scope,ngDialog,$http,inform,autoscaleStore){
	var name = $scope.ngDialogData.name;
	var namespace = $scope.ngDialogData.namespace;
	$scope.putAutoscale = function(){
		var sendData = {
			deploymentName: name,
			namespace: namespace,
			max: $scope.max,
			min: $scope.min,
			cpu: $scope.cpu
		}
		autoscaleStore.start(sendData).then(function(res){
			if(res.success){
				ngDialog.close(this,'save');
			}
		});
	}
}])