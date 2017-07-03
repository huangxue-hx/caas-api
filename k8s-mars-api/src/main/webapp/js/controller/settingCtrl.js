'use strict'

angular.module('mainApp')
.controller('SettingCtrl',['$scope','userStore','$filter',function($scope,userStore,$filter){
	var vm = $scope;
	var init = function(){
		userStore.getlist().then(function(data){
			console.log(data);
			vm.userlist = data;
		});
	};
	vm.newNs = function(){
		vm.isAddNs = true;
	};
	vm.cancelNs = function(){
		vm.isAddNs = false;
		vm.newUsername = '';
		vm.newPassword = '';
	};
	vm.addNs = function(){
		if(vm.newUsername.length>0){
			var pat = new RegExp('[a-z0-9]([-a-z0-9]*[a-z0-9])?');
			if(!pat.test(vm.newUsername)){
				alert('invalid input');
				return;

			}
			for(var i = 0,l = vm.userlist.length;i<l;i++){
				if(vm.userlist[i].username == vm.newUsername){
					alert($filter('translate')('userDuplicate'));
					return;
					break;
				}
			}
			vm.adding = true;
			userStore.add(vm.newUsername,vm.newPassword).then(function(){
				vm.adding = false;
				vm.cancelNs();
				init();
			},function(){
				vm.adding = false;
				alert($filter('translate')('newUserFail'));
			});

		}
		else{
			alert($filter('translate')('inputNamespace'));
		}
	};
	vm.modifyPsd = function(user){
		userStore.changePwd(user);
	};
	vm.deleteUser = function(user){
		user.deleting = true;
		userStore.deleteUser(user).then(function(){
			init();
		},function(){
			user.deleting = false;
		});

	};
	init();
	

}]);