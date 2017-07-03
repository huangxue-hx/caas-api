'use strict';

angular.module('mainApp').controller('AddSubnetController',AddSubnetController);
AddSubnetController.$inject = ['$scope','$stateParams','network','ngDialog','inform']
function AddSubnetController($scope,$stateParams,network,ngDialog,inform){
	var tenantid = $scope.ngDialogData.tenantid;
	var networkid = $scope.ngDialogData.networkid;

	$scope.save = function(){
		var sendData = {
			subnetname : $scope.name,
			networkid : networkid,
			tenantid : tenantid,
			// cidr : $scope.cidr,
			// gateway : $scope.gateway
		}
		// console.log(sendData);
		network.addSubnet(sendData).then(function(data){
			if(data.success){
				ngDialog.close(this,'done');
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
}