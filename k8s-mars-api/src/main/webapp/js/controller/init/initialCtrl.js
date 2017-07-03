'use strict';

angular.module('mainApp').controller('InitialController',InitialController);
InitialController.$inject = ['$scope','$timeout','authData','inform','initialStore','ngDialog','$state','$rootScope']
function InitialController($scope,$timeout,authData,inform,initialStore,ngDialog,$state,$rootScope){

	var init = function(){
		$scope.suNow = 0;
		$scope.suSlider = [
			{serTemplate:'initial/role'},
			{serTemplate:'initial/count'},
			{serTemplate:'initial/admin'}
			// {serTemplate:'initial/volumeProvider'}
		];
		cRoleInit();
		machineListInit();
		adminListInit();
	}

	//cluster role related
	var cRoleInit = function(){
		authData.cRoleList().then(function(data){
			$scope.cRoleAll = data;
			// console.log($scope.cRoleAll);
		});
	}
	$scope.deleteCRole = function(role){
		authData.deleteCRole(role.name).then(function(data){
			if(!data.success){
				var inf = {
					title: 'Delete Error',
					text: data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
			else{
				cRoleInit();
			}
		});
	}
	$scope.initRole = function(){
		initialStore.initCRole().then(function(data){
			if(data.success){
				$scope.cRoleAll = data.data;
			}
		});
	}

	//machine account related
	var machineListInit = function(){
		initialStore.machineList().then(function(data){
			$scope.machineList = data;
		})
	}
	$scope.addMachine = function(){
		var d = ngDialog.open({
			template: '../../view/initial/addMachine.html',
			width: 700,
			closeByDocument: false,
			controller: 'InitAddMachineController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				machineListInit();
			}
		});	
	}
	$scope.deleteMachine = function(m){
		var sendData = {
			userName: m.userName
		}
		initialStore.deleteMachine(sendData).then(function(data){
			if(data.success){
				machineListInit();
			}
			else{
				var inf = {
					title: 'Delete Error',
					text: data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});
	}

	//admin list related
	var adminListInit = function(){
		initialStore.adminList().then(function(data){
			$scope.adminList = data;
		})
	}
	$scope.addAdmin = function(){
		var d = ngDialog.open({
			template: '../../view/initial/addMachine.html',
			width: 700,
			closeByDocument: false,
			controller: 'InitAddAdminController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				adminListInit();
			}
		});	
	}
	$scope.deleteAdmin = function(a){
		var sendData = {
			userName: a.userName
		}
		initialStore.deleteAdmin(sendData).then(function(data){
			if(data.success){
				adminListInit();
			}
			else{
				var inf = {
					title: 'Delete Error',
					text: data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});
	}


	$scope.done = function(){
		initialStore.initNetwork().then(function(data){
			if(data.success){
				$state.go('dashboard');
				$rootScope.$broadcast('userLogin');
			}
			else{
				var inf = {
					title: 'Network Initial Error',
					text: data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});

		// $state.go('dashboard');
		// $rootScope.$broadcast('userLogin');
	}
	init();
}