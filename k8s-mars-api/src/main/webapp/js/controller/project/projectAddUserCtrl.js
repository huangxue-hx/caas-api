'use strict';

angular.module('mainApp').controller('ProjectAddUserController',ProjectAddUserController);
ProjectAddUserController.$inject = ['$scope','binding','ngDialog','userName','authData','inform']
function ProjectAddUserController($scope,binding,ngDialog,userName,authData,inform){
	var tenantname = $scope.ngDialogData.tenantname;
	var tenantid = $scope.ngDialogData.tenantid;
	var projectname = $scope.ngDialogData.projectname;

	// var toView = function(user){
	// 	var view = [];
	// 	user.forEach(function(item){
	// 		var oneView = {
	// 			name:item.username,
	// 			choose: false
	// 		}
	// 		view.push(oneView);
	// 	});
	// 	return view;
	// }
	// var toUser = function(view){
	// 	var user = [];
	// 	view.forEach(function(item){
	// 		if(item.choose){
	// 			user.push(item.name);
	// 		}
	// 	})
	// 	return user;
	// }
	// $scope.chooseUser = function(index){
	// 	$scope.userList[index].choose = !$scope.userList[index].choose;
	// }
	var initUser = function(){
		userName.userList().then(function(data){
			$scope.userList = data;
			$scope.user = $scope.userList[0].username;
			// $scopWEe.userList = toView($scope.userList);
		});
	}

	var initRole = function(){
		authData.cRoleListforNew().then(function(data){
			$scope.roleList = data;
			$scope.role = $scope.roleList[0].name;
		})
	}

	initUser();
	initRole();

	$scope.disable = function(){
		return !($scope.role&&$scope.user);
	}

	$scope.save = function(){
		var sendData = {
			tenantname: tenantname,
			tenantid: tenantid,
			namespace: projectname,
			role: $scope.role,
			user: $scope.user
		}
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
		})
	}

}