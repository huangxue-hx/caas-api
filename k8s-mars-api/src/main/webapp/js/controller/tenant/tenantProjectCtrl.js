'use strict';

angular.module('mainApp').controller('TenantProjectController',TenantProjectController);
TenantProjectController.$inject = ['$scope','$stateParams','tenant','network','ngDialog','namespace','inform']
function TenantProjectController($scope,$stateParams,tenant,network,ngDialog,namespace,inform){
	var tenant;
	$scope.tenantid = $stateParams.id;
	tenant.detail($scope.tenantid).then(function(data){
		tenant = data[0];
		init();
	})

	var init = function(){
		console.log(tenant.name);
		namespace.list($scope.tenantid,tenant.name).then(function(data){
			$scope.projectList = data;
			// console.log(data);
		});
	}

	$scope.back = function(){
		history.back(-1);
	}
	$scope.deleteProject = function(p){
		var sendData = {
			tenantid: $scope.tenantid,
			name: p.name
		}
		namespace.deleteProject(sendData).then(function(data){
			if(data.success){
				init();
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

	$scope.newProject = function(){
		var d = ngDialog.open({
			template:'../../view/project/projectNew.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: tenant.name
			},
			controller: 'ProjectNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	$scope.changeProject = function(p){
		var d = ngDialog.open({
			template:'../../view/project/projectChange.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: tenant.name,
				project:p.name
			},
			controller: 'ProjectChangeController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	// init();
}
