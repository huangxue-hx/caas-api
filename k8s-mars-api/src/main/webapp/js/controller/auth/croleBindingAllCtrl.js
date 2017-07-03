'use strict';

angular.module('mainApp').controller('CRoleBindingAllController',CRoleBindingAllController);
CRoleBindingAllController.$inject = ['$scope','binding','$location']
function CRoleBindingAllController($scope,binding,$location){
	var init = function(){
		binding.cRoleBindingList().then(function(data){
			$scope.cRoleBinding = data;
			console.log(data);
		})
	}
	init();

	$scope.$watch('checkAll',function(newVal,oldVal,scope){
		if(newVal != oldVal){
			if(newVal){
				$scope.cRoleBinding.forEach(function(item){
					item.selected = true;
				})
			}
			else{
				$scope.cRoleBinding.forEach(function(item){
					item.selected = false;
				})
			}
		}
	});

	$scope.delete = function(){
		var deleteCRList = [];
		$scope.cRoleBinding.forEach(function(item){
			if(item.selected){
				deleteCRList.push(item);
			}
		})
		deleteCRList.forEach(function(item,index){
			binding.crBindingDelete(item.name).then(function(data){
				if(index == deleteCRList.length-1){
					init();
				}
			})
		});
	}

	$scope.roleBindingDetail = function(name){
		$location.path('/auth/roleBinding/croleBindingDetail/'+name);
	}
}