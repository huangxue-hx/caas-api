'use strict';

angular.module('mainApp').controller('VolumeNewController',VolumeNewController);
VolumeNewController.$inject = ['$scope','tenant','ngDialog','network','volume','inform','pattern']
function VolumeNewController($scope,tenant,ngDialog,network,volume,inform,pattern){

	$scope.namePattern = pattern.commonName;

	$scope.readOnly=false;
	$scope.multiply=false;
	var init = function(){
		if($scope.ngDialogData){
			console.log($scope.ngDialogData);
			$scope.pre = 'tenant';
			$scope.tenantid = $scope.ngDialogData.tenantid;
			$scope.tenantname = $scope.ngDialogData.tenantname;
		}
		else{
			$scope.pre = 'new';
			tenant.list().then(function(data){
				$scope.tenantList = data;
				$scope.tenantid = $scope.tenantList[0].tenantid;
			})
		}
		volume.provider().then(function(data){
			$scope.providerList = data;
			console.log($scope.providerList);
			$scope.provider = $scope.providerList[0];
		});

	}

	$scope.$watch('readOnly',function(newVal,oldVal){
		if(newVal){
			$scope.multiple = true;
		}
	})

	$scope.disable = function(){
		return !$scope.name;
	}

	$scope.save = function(){
		console.log( $scope.providerList[$scope.provider]);
		var sendData = {
			name: $scope.name,
			tenantid: $scope.tenantid,
			providerName: $scope.provider.name,
			type: $scope.provider.type,
			capacity:$scope.capacity,
			readOnly:!!$scope.readOnly,
			multiple:!!$scope.multiple,
		}
		volume.create(sendData).then(function(data){
			if(data.success){
				ngDialog.close(this,'done');
			}
			else{
				var inf={
					title: 'Create Error',
					text:data.errMsg.message,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}

	init();
	
}