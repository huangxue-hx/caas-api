'use strict'

angular.module('mainApp').controller('StorageNewController',StorageNewController);
StorageNewController.$inject = ['$scope','$http','storage','$location','inform','namespaceStore','pattern','$filter','$rootScope'];
function StorageNewController($scope,$http,storage,$location,inform,namespaceStore,pattern,$filter,$rootScope){

	$scope.back = function(){
		history.back(-1);
	}

	$scope.namePattern = pattern.name;

	$scope.namespace = $rootScope.currentNamespace;
	$rootScope.$on('namespaceCheckChange',function(event,data){
		$scope.namespace = data.namespace;
	})

	// namespaceStore.getNamespace().then(function(data){
	// 	$scope.namespaceList = data;
	// 	$scope.namespace = $scope.namespaceList[0];
	// });

	$scope.disable = function(){
		if(!$scope.storageName || !$scope.namespace || 
			!$scope.capacity || ($scope.capacity <1))
			return true;
		else
			return false;
	}
	$scope.save = function(){
		storage.getList($scope.namespace).then(function(data){
			var storageList = data;
			var sameName = false;
			storageList.forEach(function(item){
				if(item.namespace == $scope.namespace){
					if(item.name == $scope.storageName)
						sameName = true;
				}
			});
			if(sameName){
				var inf = {
					title: $filter('translate')('nameDuplicate'),
					text: $filter('translate')('storageNameDuplicate'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else {
				if(!$scope.readonly){
					$scope.readonly = false;
				}
				var data = {
					name: $scope.storageName,
					capacity: $scope.capacity,
					namespace: $scope.namespace,
					tenantid: $scope.tenantid,
					readonly: $scope.readonly,
					bindOne: false
				};
				// console.log(data);
				storage.newStorage(data).then(function(res){
					if(res.success){
						$location.path('/storage/storageAll')
					}
				});
			}
		});
		
	}

}