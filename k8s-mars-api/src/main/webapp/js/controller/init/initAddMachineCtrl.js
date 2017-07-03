'use strict';

angular.module('mainApp').controller('InitAddMachineController',InitAddMachineController);
InitAddMachineController.$inject = ['$scope','initialStore','inform','pattern','ngDialog']
function InitAddMachineController($scope,initialStore,inform,pattern,ngDialog){

	var patterns = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,14}$/;
	$scope.namePattern = pattern.commonName;
	$scope.emailPattern = pattern.mail;

	// $scope.pasPattern = pattern.pas;

	
	$scope.disable = function(){
		if(!$scope.userName || !$scope.Password || !$scope.realName)
			return true;
		else
			return false;
	}
	$scope.save = function(){
		var data = {
			userName: $scope.userName,
			Password: $scope.Password,
			email: $scope.Email,
			realName: $scope.realName,
			Comment: $scope.Comment
		};
		initialStore.addMachine(data).then(function(data){
			if(data.success){
				ngDialog.close(this,'done');
			}
			else{
				console.log("aaa");
				var inf = {
					title: 'Create Error',
					text: data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});
		
	}
}