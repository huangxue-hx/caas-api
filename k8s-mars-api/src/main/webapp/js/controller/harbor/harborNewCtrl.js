'use strict';

angular.module('mainApp').controller('HarborNewController',HarborNewController);
HarborNewController.$inject = ['$scope','tenant','ngDialog','harborStore','inform','pattern']
function HarborNewController($scope,tenant,ngDialog,harborStore,inform,pattern){

	$scope.namePattern = pattern.commonName;

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

	$scope.disable = function(){
		return !$scope.name;
	}
	
	$scope.save = function(){
		var harborName;
		var tenantname;
		if($scope.pre == 'tenant'){
			harborName = $scope.tenantname+'-'+$scope.name;
			// harborName = 'tekk222233_'+$scope.name;
		}
		else{
			$scope.tenantList.forEach(function(item){
				if(item.tenantid == $scope.tenantid){
					tenantname = item.name;
				}
			});
			harborName = tenantname+'-'+$scope.name;
		}
		var sendData = {
			name: harborName,
			tenantid: $scope.tenantid,
			// tenantid:'037b2163ae5b4287b773c70b53f7f758'
		}
		harborStore.create(sendData).then(function(data){
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
		});
	}

	init();
	
}