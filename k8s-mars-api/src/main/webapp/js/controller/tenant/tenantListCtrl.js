'use strict';

angular.module('mainApp').controller('TenantListController',TenantListController);
TenantListController.$inject=['$scope','tenant','inform']
function TenantListController($scope,tenant,inform){

	var init = function(){
		tenant.list().then(function(data){
			$scope.tenantList = data;
		})

	}

	$scope.$watch('checkboxAll',function(newVal,oldVal){
		if(newVal != oldVal){
			if(newVal){
				$scope.tenantList.forEach(function(item){
					item.checkbox = true;
				})
			}
			else{
				$scope.tenantList.forEach(function(item){
					item.checkbox = false;
				})
			}
		}
	});

	$scope.delete = function(){
		var deleteList = [];
		$scope.tenantList.forEach(function(item){
			if(item.checkbox){
				deleteList.push(item);
			}
		});
		deleteList.forEach(function(item,index){
			tenant.deleteTenant(item.tenantid,item.name).then(function(data){
				if(!data.success){
					var inf = {
						title: 'Detele Error',
						text: data.errMsg,
						type: 'error'
					}
					inform.showInform(inf);
				}
				if(index == deleteList.length-1)
					init();
			})
		});
	}

	init();

}