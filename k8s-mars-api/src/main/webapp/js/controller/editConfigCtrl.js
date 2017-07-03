'use strict';

angular.module('mainApp').controller('EditConfigController',EditConfigController);
EditConfigController.$inject = ['$scope','serviceList','ngDialog'];
function EditConfigController($scope,serviceList,ngDialog){
	var name = $scope.ngDialogData.name;
	var namespace = $scope.ngDialogData.namespace;

	serviceList.configMap(name,namespace).then(function(data){
		$scope.file = data.file;
		$scope.data = data.data;
	});

	$scope.change = function(){
		var sendData = {
			file : $scope.file,
			data : $scope.data,
			name : name,
			namespace : namespace
		}
		serviceList.configMapEdit(sendData).then(function(data){
			if(data.success){
				ngDialog.close(this);
			}
		});
	}
}