'use strict';

angular.module('mainApp').controller('TenantNetworkController',TenantNetworkController);
TenantNetworkController.$inject=['$scope','$stateParams','tenant','ngDialog','network','inform']
function TenantNetworkController($scope,$stateParams,tenant,ngDialog,network,inform){
	
	var tenant;
	$scope.tenantid = $stateParams.id;
	tenant.detail($scope.tenantid).then(function(data){
		tenant = data[0];
	})
	var initNetwork = function(){

		// get network list via tenantid
		network.list($scope.tenantid).then(function(data){
			$scope.networkList = data;
		});
	}
	$scope.deleteNetwork = function(n){
		var sendData = {
			networkid: n.networkid,
			tenantid: tenant.tenantid
		}
		//send delete request
		network.deleteNetwork(sendData).then(function(data){
			if(data.success){
				initNetwork();
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
	$scope.newNetwork = function(){
		var d = ngDialog.open({
			template:'../../view/network/networkNew.html',
			width:700,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid
			},
			controller: 'NetworkNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initNetwork();
			}
		});	
	}

	initNetwork();
}