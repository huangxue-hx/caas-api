'use strict'

angular.module('mainApp').controller('UserUpdateController',UserUpdateController);
UserUpdateController.$inject = ['$scope','$http','userName','$location','pattern','$filter','ngDialog','$stateParams','inform'];
function UserUpdateController($scope,$http,userName,$location,pattern,$filter,ngDialog,$stateParams,inform){
	$scope.UserName = $scope.ngDialogData.name;
	var name = $scope.ngDialogData.name;
	var patterns = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,14}$/;
	var flage = false;

	 $scope.commit = function(){
	 	var regpas = $scope.newPassword;
	 	flage = patterns.test(regpas)
	 	console.log(regpas);
			if(flage == false){
				if(regpas.length<7){
					var inf = {
						title: $filter('translate')('fillPassword'),
						text: $filter('translate')('wrongPasswordLength'),
						type: "error"
					}
					inform.showInform(inf); //显示错误信息
				}else{
					var inf = {
					title: $filter('translate')('fillInPassword'),
					text: $filter('translate')('wrongPassword'),
					type: "error"
				}
				inform.showInform(inf); //显示错误信息
				}
			}else{
				var data = {
					userName:name,
					// oldPassword:$rootScope.userpasword,
					oldPassword:$scope.oldPassword,
					newPassword:$scope.newPassword,
				}
			}
			userName.updateUser(data).then(function(res){
				if(res.success){
					$location.path('user/userDetial');
					$scope.closeThisDialog();
				}else{
					var inf={
						title: 'Update Error',
						text:res.success,
						type:'error'
					}
					inform.showInform(inf);
				}
			});
		
	}
	$scope.back = function(){
		history.back(-1);
	}
	
	
}