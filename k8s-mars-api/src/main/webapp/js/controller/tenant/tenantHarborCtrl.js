'use strict';

angular.module('mainApp').controller('TenantHarborController',TenantHarborController);
TenantHarborController.$inject = ['$scope','$stateParams','tenant','ngDialog','harborStore','inform']
function TenantHarborController($scope,$stateParams,tenant,ngDialog,harborStore,inform){
	var tenant;
	$scope.tenantid = $stateParams.id;
	tenant.detail($scope.tenantid).then(function(data){
		tenant = data[0];
	})

	var init = function(){
		harborStore.list($scope.tenantid).then(function(data){
			$scope.harborList = data;
		})
	}

	$scope.back = function(){
		history.back(-1);
	}
	$scope.deleteHarbor = function(h){
		var sendData = {
			tenantid: $scope.tenantid,
			tenantname: tenant.name,
			// tenantid: '037b2163ae5b4287b773c70b53f7f758',
			projectid: h.harborid
		}
		harborStore.deleteHarbor(sendData).then(function(data){
			if(data.success){
				init();
			}
			else{
				var inf = {
					title: 'Detele Error',
					text: data.errMsg,
					type: 'error'
				}
				inform.showInform(inf);
			}
		})
	}

	$scope.newHarbor = function(){
		var d = ngDialog.open({
			template:'../../view/harbor/harborNew.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: tenant.name
			},
			controller: 'HarborNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	init();
}
