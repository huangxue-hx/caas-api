'use strict';

angular.module('mainApp').controller('RoleAllController',RoleAllController);
RoleAllController.$inject = ['$scope','$location','authData']
function RoleAllController($scope,$location,authData){
	
	var init = function(){
		authData.roleList().then(function(data){
			$scope.roleAll = data;
		})
		$scope.checkAllRole = false;
	};

	init();

	$scope.$watch('checkAllRole',function(newVal,oldVal,scope){
		if(newVal != oldVal){
			if(newVal){
				$scope.roleAll.forEach(function(item){
					item.selected = true;
				});
			}
			else{
				$scope.roleAll.forEach(function(item){
					item.selected = false;
				})
			}
		}
	})

	$scope.delete = function(name){
		var deleteList = [];
		$scope.roleAll.forEach(function(item){
			if(item.selected){
				deleteList.push(item);
			}
		});
		deleteList.forEach(function(item,index){
			authData.deleteRole(item.name,item.namespace).then(function(data){
				if(index == deleteList.length-1){
					init();
				}
			})
		});
	}

	$scope.roleDetail = function(name,namespace){
		$location.path('/auth/role/roleDetail/'+name+'/'+namespace);
	}
}
