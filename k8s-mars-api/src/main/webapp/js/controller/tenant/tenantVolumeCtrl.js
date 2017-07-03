'use strict';

angular.module('mainApp').controller('TenantVolumeController',TenantVolumeController);
TenantVolumeController.$inject = ['$scope','$stateParams','tenant','network','ngDialog','volume']
function TenantVolumeController($scope,$stateParams,tenant,network,ngDialog,volume){
	var tenant;
	$scope.tenantid = $stateParams.id;
	tenant.detail($scope.tenantid).then(function(data){
		tenant = data[0];
	})

	var init = function(){
		// get network list via tenantid
		volume.list($scope.tenantid).then(function(data){
			$scope.volumeList = data;
		});
	}

	$scope.back = function(){
		history.back(-1);
	}
	$scope.deleteVolume = function(v){
		var sendData = {
			tenantid: $scope.tenantid,
			name: v.name
		}
		volume.deleteVolume(sendData).then(function(data){
			if(data.success){
				init();
			}
		})
	}

	$scope.newVolume = function(){
		var d = ngDialog.open({
			template:'../../view/volume/volumeNew.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: tenant.name
			},
			controller: 'VolumeNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	$scope.changeVolume = function(index){
		var d = ngDialog.open({
			template:'../../view/volume/volumeChange.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: tenant.name,
				volume:$scope.volumeList[index].name
			},
			controller: 'VolumeChangeController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	init();
}
