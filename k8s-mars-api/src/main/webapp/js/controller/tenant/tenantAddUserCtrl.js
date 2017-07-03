'use strict';

angular.module('mainApp').controller('TenantAddUserController',TenantAddUserController);
TenantAddUserController.$inject = ['$scope','binding','ngDialog','userName','authData','namespace','tenant','inform']
function TenantAddUserController($scope,binding,ngDialog,userName,authData,namespace,tenant,inform){
	var tenantname = $scope.ngDialogData.tenantname;
	var tenantid = $scope.ngDialogData.tenantid;
	$scope.tm = !(!$scope.ngDialogData.tm);

	var initUser = function(){
		userName.userList().then(function(data){
			$scope.userList = data;
			$scope.user = $scope.userList[0].username;
			// $scope.userList = toView($scope.userList);
		});
	}

	var initRole = function(){
		authData.cRoleListforNew().then(function(data){
			$scope.roleList = data;
			$scope.role = $scope.roleList[0].name;
		})
	}

	var initProject = function(){
		namespace.list(tenantid).then(function(data){
			$scope.projectList = data;
			$scope.project = $scope.projectList[0].name;
		})
	}

	initUser();
	initRole();
	initProject();

	$scope.disable = function(){
		if($scope.tm){
			return !$scope.user;
		}
		else{
			return !($scope.project&&$scope.role&&$scope.user);
		}
	}
	$scope.save = function(){
		if($scope.tm){
			var sendData = {
				tenantid: tenantid,
				user: $scope.user
			}
			tenant.addTm(sendData).then(function(data){
				if(data.success){
					ngDialog.close(this,'done');
				}
				else{
					var inf={
						title: 'Create Error',
						text:data.errMsg.message,
						type:'error'
					}
					inform.showInform(inf);
					console.log(inf);
				}
			});
		}
		else{
			var sendData = {
				tenantname: tenantname,
				tenantid:tenantid,
				namespace: $scope.project,
				role: $scope.role,
				user: $scope.user
			};
			binding.bindUser(sendData).then(function(data){
				if(data.success){
					ngDialog.close(this,'done');
				}
				else{
					var inf={
						title: 'Create Error',
						text:data.errMsg.message,
						type:'error'
					}
					inform.showInform(inf);
				}
			});
		}
	}

}