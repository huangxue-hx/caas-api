'use strict';

angular.module('mainApp').controller('CRoleBindingDetailController',CRoleBindingDetailController);
CRoleBindingDetailController.$inject=['$scope','binding','$stateParams','userName','authData','inform']
function CRoleBindingDetailController($scope,binding,$stateParams,userName,authData,inform){
	var name = $stateParams.name;

	$scope.back = function(){
		history.back(-1);
	}
	var drawUser = function(view,user){
		user.forEach(function(item){
			for(var i=0;i<view.length;i++){
				if(item == view[i].name){
					view[i].choose = true;
					break;
				}
			}
		});
		return view;
	}
	var toView = function(user){
		var view = [];
		user.forEach(function(item){
			var oneView = {
				name:item.username,
				choose: false
			}
			view.push(oneView);
		});
		return view;
	}
	var toUser = function(view){
		var user = [];
		view.forEach(function(item){
			if(item.choose){
				user.push(item.name);
			}
		})
		return user;
	}

	$scope.chooseUser = function(index){
		$scope.userList[index].choose = !$scope.userList[index].choose;
	}
	var initUser = function(){
		userName.userList().then(function(data){
			$scope.userList = data;
			$scope.userList = toView($scope.userList);
			$scope.userList = drawUser($scope.userList,$scope.roleBinding.user);
		});
	}
	var init = function(){
		binding.crBindingDetail(name).then(function(data){
			$scope.roleBinding = data;
			$scope.croleList =[];
			authData.cRoleList().then(function(data){
				data.forEach(function(item){
					$scope.croleList.push(item.name);
				});
			});
			initUser();
		})
	}
	init();

	$scope.refresh = function(){
		var user = toUser($scope.userList);
		var role = $scope.roleBinding.clusterRole;
		binding.crBindingPut($scope.roleBinding.name,user,role).then(function(data){
			if(data.success){
				var inf = {
					title: 'Success',
					text: 'Cluster RoleBinding Change Succeed!',
					type: 'noti'
				}
				inform.showInform(inf);
			}
		})
	}
}