'use strict';

angular.module('mainApp').controller('NetworkDetailController',NetworkDetailController);
NetworkDetailController.$inject = ['$scope','$stateParams','network','ngDialog','inform']
function NetworkDetailController($scope,$stateParams,network,ngDialog,inform){
	var tenantid = $stateParams.tenantid;
	var networkid = $stateParams.networkid;

	$scope.back = function(){
		history.back(-1);
	}

	$scope.addSubnet = function(){
		var d = ngDialog.open({
			template:'../../view/network/addSubnet.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid: tenantid,
				networkid: networkid
			},
			controller:'AddSubnetController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});
	}

	$scope.deleteSubnet = function(subnetid){
		console.log(subnetid);
		var sendData = {
			networkid: networkid,
			subnetid: subnetid
		}
		network.deleteSubnet(sendData).then(function(data){
			if(data.success){
				init();
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}

	var init = function(){

		//get project detail via tenantid and name
		network.detail(tenantid,networkid).then(function(data){
			$scope.network = data[0];
		});
	}

	init();
}