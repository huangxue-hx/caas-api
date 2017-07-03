'use strict';

angular.module('mainApp').controller('AdminAuditController',AdminAuditController);
AdminAuditController.$inject = ['$scope','adminAudit','userName']
function AdminAuditController($scope,adminAudit,userName){

	var vm = $scope;

	vm.selected = {
		user:'',
		operation:'',
		module:'',
		paramType:'',
		param:'',
		beginTime:'',
		endTime:''
	};

	vm.myPage = {
		currentPage: 1,
		pageSize: 15,
		pageCount: 0
	}
	
	var initUser = function(){
		userName.userList().then(function(data){
			data = data || [];
			$scope.userList = [];
			for(let item of data){
				$scope.userList.push(item.username);
			}
		});
	};

	var initOperation = function(){
		adminAudit.operationList().then(function(data){
			$scope.operationList = data
		});
	};

	var initModule = function(){
		adminAudit.moduleList().then(function(data){
			$scope.moduleList = data;
		});
	};

	var initParamType = function(){
		adminAudit.paramList().then(function(data){
			$scope.paramTypeList = [];
			for(let item in data){
				$scope.paramTypeList.push(item);
			}
			$scope.paramList = data;
		});
	};

	var initAuditList = function(pageNum){
		pageNum = pageNum || 1;
		var startTime = '';
		var endTime = '';
		if(vm.selected.beginTime){
			startTime = vm.selected.beginTime.replace(' ', 'T')+'+08:00';
		}
		if(vm.selected.endTime){
			endTime = vm.selected.endTime.replace(' ', 'T')+'+08:00';
		}
		var sendData = {
			user: vm.selected.user,
			operation: vm.selected.operation,
			module: vm.selected.module,
			startTime: startTime,
			endTime: endTime,
			pageNum: pageNum,
			size: vm.myPage.pageSize,
			params: {
				name: vm.selected.paramType,
				value: vm.selected.param
			}
		};
		adminAudit.search(sendData).then(function(data){
			data = data || {};
			data.log = data.log || [];
			$scope.auditList = angular.copy(data.log);
			vm.myPage.pageCount = Math.ceil(data.total / vm.myPage.pageSize);
		});
	};

	var init = function(){
		initUser();
		initOperation();
		initModule();
		initParamType();
		initAuditList();
	}

	init();

	
	$scope.search = function(){
		console.log('param' + vm.selected.param);
		console.log('beginTime' + vm.selected.beginTime);
		console.log('endTime' + vm.selected.endTime);
		initAuditList(vm.myPage.currentPage);
	};

}