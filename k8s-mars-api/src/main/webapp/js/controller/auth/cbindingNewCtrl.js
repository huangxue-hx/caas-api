'use strict';

angular.module('mainApp').controller('CBindingNewController',CBindingNewController);
CBindingNewController.$inject=['$scope','binding','authData','userName','namespace','$location']
function CBindingNewController($scope,binding,authData,userName,namespace,$location){
	$scope.back = function(){
		history.back(-1);
	}
	$scope.type = 'croleBinding';
	$scope.addText = 'newCRoleBinding';

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
		initUser();
	}
	init();

	$scope.save = function(){
		var user = toUser($scope.userList);
		binding.crBindingAdd($scope.name,user,$scope.clusterRole).then(function(data){
			if(data.success){
				$location.path('/auth/roleBinding/roleBindingAll');
			}
		})
	}
}