'use strict';

angular.module('mainApp').controller('UserDetialCotroller',UserDetialCotroller);
UserDetialCotroller.$inject = ['$scope','userName','$stateParams','ngDialog','$location','inform']
function UserDetialCotroller($scope,userName,$stateParams,ngDialog,$location){
	var name = $stateParams.username;
	$scope.name = $stateParams.username;
	var userdetial = [];
	var userdetials = null;

		userName.getUser(name).then(function(data){
		// console.log(data);
			userdetial = angular.copy(data);
			userdetial.forEach(function(item){
				if(item.username == name){
					userdetials = item;
				}
			});
			$scope.user = userdetials;
		});
	
		userName.rolebindingList(name).then(function(data){
			$scope.rolebindingList = data;
		});
		
		userName.roleGetList(name).then(function(data){
			console.log("3======6");
			console.log(data);
			$scope.rolegetList = data;
		})

	$scope.roleDetail = function(role,namespace){
		$location.path('/auth/role/croleDetail/'+role);
	}

	$scope.back = function(){
		history.back(-1);
	}
	$scope.choose = function(){
			var d = ngDialog.open({
				template:'../view/user/userUpdate.html',
				width:400,
				controller:'UserUpdateController',
				data:{
					name:name,
					// namespace:sendNamespace
				}
			});
		}
}