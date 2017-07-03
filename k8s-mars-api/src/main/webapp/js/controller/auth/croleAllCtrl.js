'use strict';

angular.module('mainApp').controller('CRoleAllController',CRoleAllController);
CRoleAllController.$inject = ['$scope','$location','authData']
function CRoleAllController($scope,$location,authData){
	
	var init = function(){
		authData.cRoleList().then(function(data){
			$scope.cRoleAll = data;
		})
		$scope.checkAllCrole = false;
	};

	init();

	$scope.$watch('checkAllCrole',function(newVal,oldVal,scope){
		if(newVal != oldVal){
			if(newVal){
				$scope.cRoleAll.forEach(function(item){
					item.selected = true;
				})
			}
			else{
				$scope.cRoleAll.forEach(function(item){
					item.selected = false;
				})
			}
		}
	});

	$scope.delete = function(name){
		var deleteList = [];
		$scope.cRoleAll.forEach(function(item){
			if(item.selected){
				deleteList.push(item);
			}
		});
		deleteList.forEach(function(item,index){
			authData.deleteCRole(item.name).then(function(data){
				if(index == deleteList.length-1){
					init();
				}
			})
		});
	}

	$scope.roleDetail = function(name,namespace){
		$location.path('/auth/role/croleDetail/'+name);
	}
}
