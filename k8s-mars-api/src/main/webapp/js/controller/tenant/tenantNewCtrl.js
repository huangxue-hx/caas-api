'use strict';

angular.module('mainApp').controller('TenantNewController',TenantNewController);
TenantNewController.$inject=['$scope','tenant','userName','$location','inform','pattern']
function TenantNewController($scope,tenant,userName,$location,inform,pattern){

	$scope.namePattern = pattern.commonName;

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

	$scope.back = function(){
		history.back(-1);
	}

	$scope.disable = function(){
		return !($scope.name && toUser($scope.userList).length>0)
	}

	$scope.save = function(){
		var sendData = {
			name: $scope.name,
			annotation: $scope.annotation,
			user: toUser($scope.userList),
			// user: toUser($scope.userList)
			userStr:JSON.stringify(toUser($scope.userList)).toString()
		}
		tenant.create(sendData).then(function(data){
			if(data.success){
				$location.path('/tenant/tenantNetwork/'+data.data[0].tenantid);
				// $location.path('/tenant/tenantNetwork/1112');
			}
			else{
				var inf={
					title: 'Create Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}

	initUser();
}