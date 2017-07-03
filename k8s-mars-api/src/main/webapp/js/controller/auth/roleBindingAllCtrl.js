'use strict';

angular.module('mainApp').controller('RoleBindingAllController',RoleBindingAllController);
RoleBindingAllController.$inject = ['$scope','binding','$location']
function RoleBindingAllController($scope,binding,$location){
	var init = function(){
		binding.roleBindingList().then(function(data){
			$scope.roleBinding = data;
		})
	}
	init();

	$scope.$watch('checkAll',function(newVal,oldVal,scope){
		if(newVal != oldVal){
			if(newVal){
				$scope.roleBinding.forEach(function(item){
					item.selected = true;
				});
			}
			else{
				$scope.roleBinding.forEach(function(item){
					item.selected = false;
				})
			}
		}
	});

	$scope.delete = function(){
		var deleteRList = [];
		$scope.roleBinding.forEach(function(item){
			if(item.selected){
				deleteRList.push(item);
			}
		});
		deleteRList.forEach(function(item,index){
			binding.rBindingDelete(item.name,item.namespace).then(function(data){
				if(index == deleteRList.length-1){
					init();
				}
			})
		});
	}

	$scope.roleBindingDetail = function(name,namespace){
		$location.path('/auth/roleBinding/roleBindingDetail/'+name+'/'+namespace);
	}
}