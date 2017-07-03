'use strict';

angular.module('mainApp').controller('ProjectDetailController',ProjectDetailController);
ProjectDetailController.$inject = ['$scope','$stateParams','tenant','namespace','ngDialog','binding','$timeout','$rootScope','harborStore','inform','serviceList']
function ProjectDetailController($scope,$stateParams,tenant,namespace,ngDialog,binding,$timeout,$rootScope,harborStore,inform,serviceList){
	var tenantid = $stateParams.id;
	var name = $stateParams.name;
	
	var pre = true;
	if(!$stateParams.id && !$stateParams.name){
		tenantid = $rootScope.tenantid;
		name = $rootScope.currentNamespace;
		pre = false;
	}

	$rootScope.$on('namespaceCheckChange',function(event,data){
		if(!pre){
			tenantid = data.tenantid;
			name = data.namespace;
			init();
		}
	})

	$scope.back = function(){
		history.back(-1);
	}
	// $scope.spTabs = [
	// 	{serName:'projectInformation',serTemplate:'/project/projectInformation'},
	// 	// {serName:'Service',serTemplate:'/project/projectService'},
	// 	{serName:'Member',serTemplate:'/project/projectUser'},
	// 	{serName:'HarborMember',serTemplate:'/harbor/harborMember'}
	// ];

	$scope.addUser = function(){
		var d = ngDialog.open({
			template:'../../view/project/projectAddUser.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantname: $scope.tenant.name,
				tenantid: tenantid,
				projectname: name
			},
			appendClassName:'hc-dialog',
			controller:'ProjectAddUserController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initUser();
			}
		});
	}
	$scope.deleteUser = function(index){
		var sendData = {
			tenantname: $scope.tenant.name,
			namespace: name,
			tenantid: tenantid,
			role: $scope.userList[index].role,
			user: $scope.userList[index].name
		}
		binding.deleteBindUser(sendData).then(function(data){
			if(data.success){
				initUser();
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}

	$scope.addHarborUser = function(){
		var d = ngDialog.open({
			template:'../../view/harbor/harborAddUser.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantname: $scope.tenant.name,
				tenantid: tenantid,
				namespace: name
			},
			appendClassName:'hc-dialog',
			controller:'HarborAddUserController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initHarborUser();
			}
		});
	}
	$scope.deleteHarborUser = function(index){
		var sendData = {
			tenantname: $scope.tenant.name,
			tenantid: tenantid,
			namespace: name,
			user:{
				id: $scope.harborUserList[index].userId[0],
				name: $scope.harborUserList[index].user[0],
			},
			projects:[{
				projectId: $scope.harborUserList[index].harborid,
				role: $scope.harborUserList[index].role,
			}],
		}
		harborStore.deleteRole(sendData).then(function(data){
			if(data.success){
				initHarborUser();
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}
	

	var initUser = function(){
		namespace.getUser($scope.tenant.name,name).then(function(data){
			var tempUser = [{
				name: 'pm',
				member: [],
				fold: true
			},{
				name: 'dev',
				member: [],
				fold: true
			},{
				name: 'tester',
				member: [],
				fold: true
			}]
			data.forEach(function(item){
				if(item.role == 'dev'){
					tempUser[1].member.push(item.name);
				}
				else if(item.role == 'pm'){
					tempUser[0].member.push(item.name);
				}
				else if(item.role == 'tester'){
					tempUser[2].member.push(item.name);
				}
			});
			$scope.userList = tempUser;
		});	
	}
	$scope.expandMember = function(index){
		$scope.userList[index].fold = !$scope.userList[index].fold;
	}
	var initHarborUser = function(){
		harborStore.userList($scope.tenant.name,name).then(function(data){
			$scope.harborUserList = data;
		})
	}
	var initService = function(){
		serviceList.getList(name).then(function(data){
			$scope.serviceList = data;
		});
	}
	var init = function(){

		tenant.detail(tenantid).then(function(data){
			$scope.tenant = data[0];
			initUser();
			initHarborUser();
		});
		initService();
		//get project detail via tenantid and name
		namespace.detail(tenantid,name).then(function(data){
			$scope.project = data;
			$timeout(function() {
				$scope.podUsage = {
					width: 250
				}
				for(var q in $scope.project.quota){
					$scope[q+'Usage'] = {
						width: parseInt($scope.project.quota[q][1])/parseInt($scope.project.quota[q][0])*250
					}
				}
			}, 3);
		});
	}

	init();
}