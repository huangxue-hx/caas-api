'use strict';

angular.module('mainApp').controller('NetworkListController',NetworkListController);
NetworkListController.$inject = ['$scope','$http','inform','$filter','ngDialog','network']
function NetworkListController($scope,$http,inform,$filter,ngDialog,network){
	var networklist = [];
	var tenantlist = [];

	var init = function(){
		$scope.checkboxAll = false;
		network.list().then(function(data){
			$scope.networkList = angular.copy(data);
			
			networklist = data.tenant;
			
			// $scope.networkList.forEach(function(item){
			// 	item.checkbox = false;
			// })
			
		});
	}

	$scope.delete = function(data){
		var deleteList = [];
		var tenantid;
		var networkid;
		$scope.networkList.forEach(function(item){
			if(item.checkbox){
				deleteList.push(item);
				console.log(deleteList);
			}
		});
		if(deleteList.length == 0){
			var inf = {
				title: $filter('translate')('noNetworkError'),
				text: $filter('translate')('selectNetworkError'),
				type: "error"
			}
			inform.showInform(inf);
		}

		deleteList.forEach(function(item,index){
			$scope.networkList.forEach(function(num){
				if(item.name == num.name){
					tenantlist = num.tenant;
				}
			})
			var sendData = {
				
				networkid: item.networkid,
				tenantid: tenantlist.tenantid,
			}
			network.deleteNetwork(sendData).then(function(data){
				if(!data.success){
					var inf = {
						title: 'Detele Error',
						text: data.errMsg,
						type: 'error'
					}
					inform.showInform(inf);
				}
				if(index == (deleteList.length-1)){
					init();
				}
			})
		});
	}

	$scope.newNetWork = function(){
		var d = ngDialog.open({
			template:'../../view/network/networkNew.html',
			width:700,
			closeByDocument: false,
			// data:{
			// 	tenantid:networklist.tenantid
			// },
			controller: 'NetworkNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}
	init();
}