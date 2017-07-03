'use strict';

angular.module('mainApp')
.controller('UserListController',['$scope','userName','$http','inform','$rootScope','$filter','ngDialog',function($scope,userName,$http,inform,$rootScope,$filter,ngDialog){
	var userAll;
	var rolebindingAll;

	var init = function(){
		$scope.checkboxAll = false;
		userName.userList().then(function(data){
			$scope.usersList = angular.copy(data);
			userAll = angular.copy(data);
			$rootScope.userpasword = data.password;
			$scope.usersList.forEach(function(item){
				item.checkbox = false;
			})
		});
	}

	// $scope.userUpdate = function(index){
	// 	var d = ngDialog.open({
	// 		template:'../../view/user/userUpdate.html',
	// 		width:650,
	// 		closeByDocument: false,
	// 		data:{
	// 			username:name,
	// 			userindex:$scope.usersList[index].name
	// 		},
	// 		controller: 'UserUpdateController'
	// 	});
	// 	d.closePromise.then(function(data){
	// 		if(data.value == 'done'){
	// 			init();
	// 		}
	// 	});	
	// }

	init();
	
	$scope.checkboxAll = false;
	$scope.$watch('checkboxAll',function(newVal, oldVal, scope){
		if(newVal != oldVal){
			if($scope.checkboxAll){
				$scope.usersList.forEach(function(item){
					item.checkbox = true;
				});
			}
			else{
				$scope.usersList.forEach(function(item){
					item.checkbox = false;
				})
			}
		};
	});	

	$scope.delete = function(username){
		var deleteList = [];
		$scope.usersList.forEach(function(item){ //用scope来调用页面的元素
			if(item.checkbox){
				deleteList.push(item);
			}
		});
		if(deleteList.length == 0){
			var inf = {
				title: $filter('translate')('noUserError'),
				text: $filter('translate')('selectUserError'),
				type: "error"
			}
			inform.showInform(inf);
		}
		deleteList.forEach(function(item,index){
			var sendData = {   //根据参数来删除
				userName: item.username,
			}	
			userName.deleteUser(sendData).then(function(data){  //调用service所写的function
				if(!data.success){
					var inf = {
						title: 'Detele Error',
						text: data.errMsg,
						type: 'error'
					}
					inform.showInform(inf);
				}
				if(index == (deleteList.length-1)){
					init();
				}
			})
		});
	}
}])