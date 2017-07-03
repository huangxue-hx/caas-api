'use strict';

angular.module('mainApp').controller('RoleNewController',RoleNewController);
RoleNewController.$inject = ['$scope','namespace','resource','authData','$location'];
function RoleNewController($scope,namespace,resource,authData,$location){
	$scope.name="";
	$scope.resource = [];
	$scope.noneResource = [];
	$scope.type = 'role';
	$scope.addText = 'newRole'
	namespace.namespaceList().then(function(data){
		$scope.namespaceList = data;
		$scope.namespace = $scope.namespaceList[0];
	})
	$scope.back = function(){
		history.back(-1);
	}
	
	var toView = function(resource){
		var viewData = [];
		resource.forEach(function(item,index){
			var oneRes = {
				name: item.name,
				choose: false,
				hidden:true,
				operations:[]
			}
			item.operations.forEach(function(opeitem,index){
				var oneOp = {
					name:opeitem,
					choose:false
				}
				oneRes.operations.push(oneOp);
			});
			viewData.push(oneRes);
		});
		return viewData
	}

	var toResource = function(viewData){
		var resource = [];
		viewData.forEach(function(item){
			if(item.choose){
				var oneView = {
					name : item.name,
					operations: []
				}
				item.operations.forEach(function(opeitem){
					if(opeitem.choose){
						oneView.operations.push(opeitem.name);
					}
				})
				resource.push(oneView);
			}
		})
		return resource;
	}

	//resource format transition
	resource.resourceList().then(function(data){
		var resource = data;
		$scope.resource = toView(resource);
	})

	//none Url resource format transition
	resource.noneResourceList().then(function(data){
		var noneResource = data;
		$scope.noneResource = toView(noneResource);
	})

	// resource relative operation
	$scope.disableRes = function(index){
		if($scope.resource[index].choose){
			$scope.resource[index].choose = false;
			$scope.resource[index].operations.forEach(function(item){
				item.choose = false;
			});
		}
	}

	$scope.expand = function(index){
		$scope.resource[index].hidden = !$scope.resource[index].hidden;
	}

	$scope.chooseOpe = function(index){
		$scope.resource[index].choose = false;
		$scope.resource[index].operations.forEach(function(item){
			if(item.choose){
				$scope.resource[index].choose = true;
			}
		})
	}

	//noneResource relative operation
	$scope.disableNoneRes = function(index){
		if($scope.noneResource[index].choose){
			$scope.noneResource[index].choose = false;
			$scope.noneResource[index].operations.forEach(function(item){
				item.choose = false;
			});
		}
	}

	$scope.expandNone = function(index){
		$scope.noneResource[index].hidden = !$scope.noneResource[index].hidden;
	}

	$scope.chooseNoneOpe = function(index){
		$scope.noneResource[index].choose = false;
		$scope.noneResource[index].operations.forEach(function(item){
			if(item.choose){
				$scope.noneResource[index].choose = true;
			}
		})
	}

	$scope.save = function(){
		var resource = toResource($scope.resource);
		var noneResource = toResource($scope.noneResource);
		authData.addRole($scope.name,$scope.namespace,resource,noneResource).then(function(data){
			if(data.success){
				$location.path('/auth/role/roleAll');
			}
		})
	}

}