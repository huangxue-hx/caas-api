'use strict';

//no volume change for now

angular.module('mainApp').controller('VolumeChangeController',VolumeChangeController);
VolumeChangeController.$inject = ['$scope','network','ngDialog']
function VolumeChangeController($scope,network,ngDialog){
	var tenantid = $scope.ngDialogData.tenantid;
	var name = $scope.ngDialogData.project;
	$scope.tenantname = $scope.ngDialogData.tenantname;

	var init = function(){

		//get project detail via tenantid and name
		$scope.volume = {
			name: 'volume1',
			tenant: {
				name:'admin',
				tenantid:'222',
			},
			type:'nfs',
			capacity:1024,
			readOnly:true,
			multiple:false,
			config:{
				url: 'aaa',
				path: 'bbb',
			}
		}
	}

	$scope.save = function(){
		var sendData = {
			name: $scope.volume.name,
			tenantid: $scope.volume.tenant.tenantid,
			type:$scope.volume.type,
			capacity:$scope.volume.capacity,
			readOnly:$scope.volume.readOnly,
			multiple:$scope.volume.multiple,
			config:$scope.volume.config
		}
		console.log(sendData);

		ngDialog.close(this,'done');
	}

	init();
}