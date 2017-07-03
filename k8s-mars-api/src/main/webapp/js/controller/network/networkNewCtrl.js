'use strict';

angular.module('mainApp').controller('NetworkNewController',NetworkNewController);
NetworkNewController.$inject = ['$scope','tenant','ngDialog','network','pattern','inform']
function NetworkNewController($scope,tenant,ngDialog,network,pattern,inform){

	$scope.namePattern = pattern.commonName;

	var init = function(){
		if($scope.ngDialogData){
			$scope.pre = 'tenant';
			var tenantid = $scope.ngDialogData.tenantid;
			tenant.detail(tenantid).then(function(data){
				$scope.tenant = data[0];
			    console.log($scope.tenant);
			})
		}
		else{
			$scope.pre = 'new';
			tenant.list().then(function(data){
				$scope.tenantList = data;
				$scope.tenant = $scope.tenantList[0];
			})
		}
	}

	// $scope.onesub = {
	// 	subname: "",
	// 	cidr: "",
	// 	gateway:""

	// }
	$scope.onesub = {subname:""};
	$scope.subnets = [];
	$scope.saveSub = function(){
		if(!!$scope.onesub.subname){
			$scope.subnets.push(angular.copy($scope.onesub));
			// $scope.subnets[0].subname = $scope.onesub[0].subname;
			$scope.onesub = {
				subname: "",
				// cidr: "",
				// gateway:""

			}
		}
	}
	$scope.deleteSub = function(index){
		$scope.subnets.splice(index,1);
	}
	$scope.disable = function(){
		// return !($scope.name&&(($scope.onesub.name&&$scope.onesub.cidr&&$scope.onesub.gateway)||$scope.subnets.length>0))
		return !($scope.name && ($scope.onesub.subname || $scope.subnets.length>0));
	}
	// $scope.save = function(){
	// 	if($scope.onesub.name && $scope.onesub.cidr && $scope.onesub.gateway)
	// 		$scope.saveSub();
	// 	if($scope.name){
	// 		var sendData = {
	// 			name: $scope.name,
	// 			tenant:{
	// 				name: $scope.tenant.name,
	// 				tenantid: $scope.tenant.tenantid
	// 			},
	// 			subnets: $scope.subnets
	// 		}
	// 		network.create(sendData).then(function(data){
	// 			if(data.success){
	// 				ngDialog.close(this,'done');
	// 			}
	// 			else{
	// 				var inf={
	// 					title: 'Create Error',
	// 					text:data.errMsg,
	// 					type:'error'
	// 				}
	// 				inform.showInform(inf);
	// 			}
	// 		})
	// 	}
	// 	// console.log(sendData);
	// }
	$scope.save = function(){
		if($scope.onesub.subname)
			$scope.saveSub();
		if($scope.name){
			var sendData = {
				networkname: $scope.name,
				tenant:{
					tenantname: $scope.tenant.name,
					tenantid: $scope.tenant.tenantid
				},
				subnets: $scope.subnets
			}
			network.create(sendData).then(function(data){
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
		// console.log(sendData);
	}

	init();
	
}