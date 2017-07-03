'use strict';

angular.module('mainApp').controller('HarborAllController',HarborAllController);
HarborAllController.$inject = ['$scope','$http','inform','$filter','ngDialog','harborStore']
function HarborAllController($scope,$http,inform,$filter,ngDialog,harborStore){
	var harborAll;
	
	var tenantlist = [];
	
	var init = function(){
		$scope.checkboxAll = false;
		harborStore.list().then(function(data){
			$scope.harborList = angular.copy(data);
			harborAll = angular.copy(data);
			
			$scope.harborList.forEach(function(item){
				item.checkbox = false;
			})
			
		});
		
	}

	
	init();

	$scope.checkboxAll = false;
	$scope.$watch('checkboxAll',function(newVal, oldVal, scope){
		if(newVal != oldVal){
			if($scope.checkboxAll){
				$scope.harborList.forEach(function(item){
					item.checkbox = true;
				});
			}
			else{
				$scope.harborList.forEach(function(item){
					item.checkbox = false;
				})
			}
		};
	});


	$scope.delete = function(data){
		var deleteList = [];
		var tenantid;
		$scope.harborList.forEach(function(item){
			if(item.checkbox){
				deleteList.push(item);
			}
		});
		if(deleteList.length == 0){
			var inf = {
				title: $filter('translate')('noHarborError'),
				text: $filter('translate')('selectHarborError'),
				type: "error"
			}
			inform.showInform(inf);
		}

		deleteList.forEach(function(item,index){
			$scope.harborList.forEach(function(num){
				if(item.name == num.name){
					tenantlist = num.tenant;
				}
			})
			var sendData = {
				// name: item.name,
				tenantid: tenantlist.tenantId,
				projectid: item.harborid,
				tenantname: tenantlist.name,
			}
			harborStore.deleteHarbor(sendData).then(function(data){
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
	$scope.newHarborList = function(){
		var d = ngDialog.open({
			template:'../../view/harbor/harborNew.html',
			width:650,
			closeByDocument: false,
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