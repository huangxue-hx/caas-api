'use strict';

angular.module('mainApp').controller('ProjectNewController',ProjectNewController);
ProjectNewController.$inject = ['$scope','tenant','ngDialog','network','namespace','inform','pattern']
function ProjectNewController($scope,tenant,ngDialog,network,namespace,inform,pattern){

	$scope.namePattern = pattern.commonName;

	$scope.tenantid = false;
	var init = function(){
		if($scope.ngDialogData){
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
	}

	$scope.$watch('tenantid',function(newVal,oldVal){
		if(!!newVal){
			network.list(newVal).then(function(data){
				$scope.networkList = data;
				$scope.network = data[0];
			})
		}
	})
	$scope.$watch('network.networkid',function(newVal,oldVal){
		if(newVal != oldVal){
			network.availableList($scope.tenantid,newVal).then(function(data){
				$scope.subnetList = data[0].subnets;
				$scope.subnet = $scope.subnetList[0];
			});
		}	
	})
	
	$scope.disable = function(){
		return !($scope.name&&$scope.network&&$scope.subnet);
	}
	
	$scope.save = function(){
		if($scope.pre == 'new'){
			$scope.tenantList.forEach(function(item,index){
				if(item.tenantid == $scope.tenantid){
					$scope.tenantname = item.name;
				}
			});
		}
		var sendData = {
			name: $scope.tenantname+'-'+$scope.name,
			tenantid: $scope.tenantid,
			network: {
				name : $scope.network.name,
				networkid : $scope.network.networkid,
				subnet : {
					subnetid : $scope.subnet.subnetid,
					subnetname : $scope.subnet.name 
				}
			},
			annotation: $scope.annotation,
			quota:{
				pod: $scope.pod,
				service: $scope.service,
				rc: 100,
				resourceQuota: 100,
				secret: 100,
				pvc: $scope.pvc,
				cpu: $scope.cpu+'m',
				memory: $scope.memory+'Mi'
			}
		}
		// console.log(sendData);
		namespace.create(sendData).then(function(data){
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

	init();
	
}