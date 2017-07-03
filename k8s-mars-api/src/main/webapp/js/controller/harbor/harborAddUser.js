'use strict';

angular.module('mainApp').controller('HarborAddUserController',HarborAddUserController)
HarborAddUserController.$inject = ['$scope','authData','harborStore','userName','ngDialog','inform'];
function HarborAddUserController($scope,authData,harborStore,userName,ngDialog,inform){
	var tenantname = $scope.ngDialogData.tenantname;
	var tenantid = $scope.ngDialogData.tenantid;
	var namespace = $scope.ngDialogData.namespace;

	var init = function(){
		authData.harborRole().then(function(data){
			$scope.roleList = data;
			$scope.role = $scope.roleList[0].name;
		});
		userName.userList().then(function(data){
			$scope.userList = data;
			$scope.user = $scope.userList[0];
			// $scope.userList = toView($scope.userList);
		});
		harborStore.list(tenantid).then(function(data){
			$scope.harborList = data;
			$scope.harbor = $scope.harborList[0];
		})
	}

	$scope.disable = function(){
		return !($scope.user&&$scope.harbor&&$scope.role);
	}

	$scope.save = function(){
		var sendData = {
			namespace: namespace,
			tenantid: tenantid,
			tenantname: tenantname,
			user:{
				id:$scope.user.user_id,
				name:$scope.user.username
			},
			projects:[{
				projectId: $scope.harbor.harborid,
				role: $scope.role
			}]
		}
		harborStore.addRole(sendData).then(function(data){
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

	init();
}