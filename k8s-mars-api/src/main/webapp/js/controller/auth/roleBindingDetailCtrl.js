'use strict';

angular.module('mainApp').controller('RoleBindingDetailController',RoleBindingDetailController);
RoleBindingDetailController.$inject=['$scope','binding','$stateParams','userName','authData','inform']
function RoleBindingDetailController($scope,binding,$stateParams,userName,authData,inform){
	var name = $stateParams.name;
	var namespace = $stateParams.namespace;

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
		binding.rBindingDetail(name,namespace).then(function(data){
			$scope.roleBinding = data;
			authData.roleListNamespace($scope.roleBinding.namespace).then(function(data){
				$scope.roleList = data;
			});
			initUser();
		})
	}
	init();

	$scope.refresh = function(){
		var user = toUser($scope.userList);
		var role = $scope.roleBinding.role;
		binding.rBindingPut($scope.roleBinding.name,$scope.roleBinding.namespace,user,role).then(function(data){
			if(data.success){
				var inf = {
					title: 'Success',
					text: 'RoleBinding Change Succeed!',
					type: 'noti'
				}
				inform.showInform(inf);
			}
		})
	}
}