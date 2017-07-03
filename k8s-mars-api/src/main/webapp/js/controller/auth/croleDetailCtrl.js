'use strict';

angular.module('mainApp').controller('CRoleDetailController',CRoleDetailController);
CRoleDetailController.$inject = ['$scope','$location','authData','$stateParams','resource','inform']
function CRoleDetailController($scope,$location,authData,$stateParams,resource,inform){
	//返回上一页
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
	var toAll = function(resource){
		var viewData = [];
		resource.forEach(function(item,index){
			var oneRes = {
				name: item.name,
				choose: true,
				hidden:true,
				operations:[]
			}
			item.operations.forEach(function(opeitem,index){
				var oneOp = {
					name:opeitem,
					choose:true
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

	var chooseView = function(choose, all){
		console.log(choose);
		choose.forEach(function(item){
			for(var i=0;i<all.length;i++){
				if(item.name == all[i].name){
					all[i].choose = true;
					item.operations.forEach(function(opr){
						all[i].operations.forEach(function(alloper){
							if(opr == alloper.name){
								alloper.choose = true;
							}
						})
					})
					break;
				}
			}
		})
		return all;
	}

	// resource relative operation
	// $scope.disableRes = function(index){
	// 	// if($scope.resource[index].choose){
	// 	// 	$scope.resource[index].choose = false;
	// 	// 	$scope.resource[index].operations.forEach(function(item){
	// 	// 		item.choose = false;
	// 	// 	});
	// 	// }
	// 	$scope.resource[index].choose = !$scope.resource[index].choose;
	// 	$scope.resource[index].operations.forEach(function(item){
	// 		item.choose = $scope.resource[index].choose;
	// 	});
	// }

	$scope.expand = function(index){
		$scope.resource[index].hidden = !$scope.resource[index].hidden;
	}

	// $scope.chooseOpe = function(index){
	// 	$scope.resource[index].choose = false;
	// 	$scope.resource[index].operations.forEach(function(item){
	// 		if(item.choose){
	// 			$scope.resource[index].choose = true;
	// 		}
	// 	})
	// }

	//noneResource relative operation
	$scope.disableNoneRes = function(index){
		// if($scope.noneResource[index].choose){
		// 	$scope.noneResource[index].choose = false;
		// 	$scope.noneResource[index].operations.forEach(function(item){
		// 		item.choose = false;
		// 	});
		// }
		$scope.noneResource[index].choose = !$scope.noneResource[index].choose;
		$scope.noneResource[index].operations.forEach(function(item){
			item.choose = $scope.noneResource[index].choose;
		});
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

	var name = $stateParams.name;


	var initList = function(){
		//resource format transition
		resource.resourceList().then(function(data){
			var resource = data;
			if($scope.role.resource[0].name == "*"){
				$scope.resource = toAll(resource);
				console.log($scope.resource);
			}
			else{
				console.log($scope.role.resource);
				
				$scope.resource = toView(resource);
				console.log($scope.resource);
				$scope.resource = chooseView($scope.role.resource,$scope.resource);
			}	
		})

		//none Url resource format transition
		resource.noneResourceList().then(function(data){
			var noneResource = data;
			$scope.noneResource = toView(noneResource);
			$scope.noneResource = chooseView($scope.role.noneResource,$scope.noneResource);
		})
	}
	var init = function(){
		authData.cRoleDetail(name).then(function(data){
			$scope.role = data;
			initList();
		})

	}

	$scope.refresh = function(){
		var resource = toResource($scope.resource);
		var noneResource = toResource($scope.noneResource);
		var sendData = {
			name: $scope.role.name,
			index: $scope.role.index,
			resource: resource,
			noneResource: noneResource
		}
		authData.putCRole(sendData).then(function(data){
			if(data.success){
				var inf = {
					title: 'Success',
					text: 'Cluster RoleBinding Change Succeed!',
					type: 'noti'
				}
				inform.showInform(inf);
				init();
			}
		})
	}

	init();
}
