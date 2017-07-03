'use strict';

angular.module('mainApp')
.controller('StorageAllController',['$scope','storage','$http','$timeout','inform','$filter','$rootScope',function($scope,storage,$http,$timeout,inform,$filter,$rootScope){
	var storageAll;

	$scope.namespace = $rootScope.currentNamespace;

	//五秒刷新功能
	var request = false;
	var storageRefresh = function(){
		var reload = false;
		request= true;
		$timeout(function() {
			storage.getList($scope.namespace).then(function(data){
				if(!angular.equals(data,storageAll)){
					$scope.storageList = angular.copy(data);
					storageAll = angular.copy(data);
					// console.log(data);
				}

				if($scope.storageList){
					$scope.storageList.forEach(function(item){
						if(item.status=='Pending')
							reload = true;
					})
				}
				else{
					reload = true;
				}
				// console.log(reload);
				if(reload == true){
					storageRefresh();
				}
				else{
					request = false;
				}
			});
		}, 5000);
	};

	$scope.$on('$destroy',function(){
		storageRefresh = function(){
			return false;	
		}
	});
	$scope.$on('namespaceCheckChange',function(event,data){
		$scope.namespace = data.namespace;
		init();
	})

	var init = function(){
		$scope.checkboxAll = false;
		storage.getList($scope.namespace).then(function(data){
			$scope.storageList = angular.copy(data);
			storageAll = angular.copy(data);
			$scope.storageList.forEach(function(item){
				item.checkbox = false;
			})
			// console.log($scope.storageList);
		});
		if(!request)
			storageRefresh();
	}
	init();

	$scope.checkboxAll = false;
	$scope.$watch('checkboxAll',function(newVal, oldVal, scope){
		if(newVal != oldVal){
			if($scope.checkboxAll){
				$scope.storageList.forEach(function(item){
					item.checkbox = true;
				});
			}
			else{
				$scope.storageList.forEach(function(item){
					item.checkbox = false;
				})
			}
		};
	});	

	$scope.delete = function(){
		var deleteList = [];
		$scope.storageList.forEach(function(item){
			if(item.checkbox){
				deleteList.push(item);
			}
		});
		if(deleteList.length == 0){
			var inf = {
				title: $filter('translate')('noStorageError'),
				text: $filter('translate')('selectStorageError'),
				type: "error"
			}
			inform.showInform(inf);
		}
		deleteList.forEach(function(item,index){
			var sendData = {
				name: item.name,
				namespace: item.namespace
			}
			storage.deleteStorage(sendData).then(function(data){
				if(index == (deleteList.length-1)){
					init();
				}
			})
		});
	}

	$scope.queryName = "";
	$scope.search = function(){
		if($scope.query){
			$scope.queryName = $scope.query;
			storage.getQuery($scope.queryName).then(function(data){
				$scope.storageList = angular.copy(data);
				storageAll = angular.copy(data);
			});
			storageRefresh();
		};
		$scope.query = "";
	}
	$scope.deleteName = function(){
		$scope.queryName = "";
		init();
	}

	$scope.question = false;
}])