'use strict';

angular.module('mainApp').controller('ProjectChangeController',ProjectChangeController);
ProjectChangeController.$inject = ['$scope','network','ngDialog','namespace','inform']
function ProjectChangeController($scope,network,ngDialog,namespace,inform){
	var tenantid = $scope.ngDialogData.tenantid;
	var name = $scope.ngDialogData.project;
	$scope.tenantname = $scope.ngDialogData.tenantname;

	var init = function(){

		//get project detail via tenantid and name
		namespace.detail(tenantid,name).then(function(data){
			$scope.project = data;
		});
	}

	$scope.save = function(){
		$scope.project.quota.cpu[0] = $scope.project.quota.cpu[0] +'m';
		$scope.project.quota.memory[0] = $scope.project.quota.memory[0] +'Mi';
		var sendData = {
			name: $scope.project.name,
			quota: {
				pod: $scope.project.quota.pod[0],
				service: $scope.project.quota.service[0],
				rc: 100,
				resourceQuota: 100,
				secret: 100,
				pvc: $scope.project.quota.pvc[0],
				cpu: $scope.project.quota.cpu[0],
				memory: $scope.project.quota.memory[0],
			},
			tenantid: tenantid,
			// networkid: $scope.project.network.networkid,
			// subnetid: $scope.project.network.subnet.subnetid,
			network: $scope.project.network,
			annotation: $scope.project.annotation
		}
		namespace.update(sendData).then(function(data){
			if(data.success){
				ngDialog.close(this,'done');
			}
			else{
				var inf={
					title: 'Update Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});
	}

	init();
}