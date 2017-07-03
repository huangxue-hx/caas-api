'use strict';

angular.module('mainApp').controller('BindingNewController',BindingNewController);
BindingNewController.$inject=['$scope','binding','authData','userName','namespace','$location']
function BindingNewController($scope,binding,authData,userName,namespace,$location){
	$scope.back = function(){
		history.back(-1);
	}
	$scope.type = 'roleBinding';
	$scope.addText = 'newRoleBinding';


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
	$scope.$watch('namespace',function(newVal,oldVal){
		authData.roleListNamespace(newVal).then(function(data){
			$scope.roleList = data;
			$scope.role = $scope.roleList[0];
		})
	})
	$scope.chooseUser = function(index){
		$scope.userList[index].choose = !$scope.userList[index].choose;
	}
	var initUser = function(){
		userName.userList().then(function(data){
			$scope.userList = data;
			$scope.userList = toView($scope.userList);
		});
	}
	var init = function(){
		$scope.croleList=[];
		authData.cRoleList().then(function(data){
			data.forEach(function(item){
				$scope.croleList.push(item.name);
			});
			$scope.clusterRole = $scope.croleList[0];
		});
		//initiall namespace list and namespace
		namespace.namespaceList().then(function(data){
			$scope.namespaceList = data;
			$scope.namespace = $scope.namespaceList[0];
		})
		initUser();
	}
	init();

	$scope.save = function(){
		var user = toUser($scope.userList);
		var sendData = {
			name: $scope.name,
			namespace: $scope.namespace,
			user: user,
			role: role
		};
		binding.rBindingAdd(sendData).then(function(data){
			if(data.success){
				$location.path('/auth/roleBinding/roleBindingAll');
			}
		})
	}
}