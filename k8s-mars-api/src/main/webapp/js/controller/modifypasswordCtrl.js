'use strict'

angular.module('mainApp')
.controller('ModifypasswordCtrl',['$scope','userStore','$filter','$rootScope',function($scope,userStore,$filter,$rootScope){
	var vm = $scope;


	vm.modify = function(){
		if(vm.password.length>0){
			userStore.changePwd({password:vm.password}).then(function(){
				$rootScope.$broadcast('userLogout');
				vm.closeThisDialog();
			},function(err){
				alert(err);
			});
		}
		else{
			alert('password required!');
		}	
	};
	

}]);