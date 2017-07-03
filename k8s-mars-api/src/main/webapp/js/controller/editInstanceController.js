'use strict'

angular.module('mainApp')
	.controller('EditInstanceController', ['$scope','serviceList',function($scope,serviceList){
		$scope.ifEdit = false;
		$scope.edit = function(){
			$scope.count = $scope.service.instance;
			if($scope.ifEdit)
				$scope.ifEdit = false;
			else
				$scope.ifEdit = true;
		};
		$scope.disable = function(){
			if((!isNaN($scope.count))&&($scope.count>0))
				return false;
			else
				return true;
		};
		$scope.saveCount = function(){
			if($scope.service.instance != $scope.count){
				$scope.service.instance = $scope.count;
				var sendData = {
					name: $scope.service.name ,
					namespace: $scope.namespace,
					scale: $scope.count
				}
				serviceList.instance(sendData).then(function(res){
					if(res.success){
						$scope.init();
					}
				});
			}
			if($scope.ifEdit)
				$scope.ifEdit = false;
			else
				$scope.ifEdit = true;
		}
	}])