'use strict';

angular.module('mainApp')
	.controller('ServiceAllController', ['$scope','$http','$rootScope','serviceList','$timeout','inform','suFormat','$filter', function($scope, $http, $rootScope,serviceList, $timeout,inform,suFormat,$filter){
		//设置一个本地变量,用来记录得到的list的原始值
		var serviceAll;
		// var tenantid = $rootScope.tenantid;
		var namespace;
		namespace = $rootScope.currentNamespace;

		$scope.query = {
			name:"",
			labels:[]
		};
		$scope.$on('namespaceCheckChange',function(event,data){
			namespace = data.namespace;
			init();
		})
		
		$scope.request = function(query){ 
			serviceList.getQuery(query,namespace).then(function(data){
				$scope.serviceList = angular.copy(data);
				serviceAll = angular.copy(data);
			});
		}
		//五秒刷新功能
		var request = false;
		var interRefresh = function(){
			request = true;
			$timeout(function() {
				var reload = false;
				if($scope.query.name||$scope.query.labels.length){
					serviceList.getQuery($scope.query,namespace).then(function(data){
						if(!angular.equals(data,serviceAll)){
							$scope.serviceList = angular.copy(data);
							serviceAll = angular.copy(data);
							console.log(data);
						}
						if($scope.serviceList){
							$scope.serviceList.forEach(function(item,index){
								if(item.status==2||item.status==3)
									reload = true;
							});
						}
						else{
							reload = true;
						}

						if(reload == true){
							interRefresh();	
						}
						else{
							request = false;
						}
					});
				}
				else{
					serviceList.getList(namespace).then(function(data){
						if(!angular.equals(data,serviceAll)){
							$scope.serviceList = angular.copy(data);
							serviceAll = angular.copy(data);
							console.log(data);
						}
						if($scope.serviceList){
							$scope.serviceList.forEach(function(item,index){
								if(item.status==2||item.status==3)
									reload = true;
							});
						}
						else{
							reload = true;
						}

						if(reload == true){
							interRefresh();	
						}
						else{
							request = false;
						}
					});	
				}	
			}, 5000);
		};
		$scope.$on('$destroy',function(){
			 interRefresh = function(){
			 	
			 } 
		});

		//搜索部分
		var labelMatch = true;
		var nameMatch = true;
		$scope.match = true;
		$scope.$watch('queryInput',function(newVal,oldVal){
			if(oldVal!=newVal){
				if(newVal){
					labelMatch = suFormat.matchLabel(newVal);
					nameMatch = suFormat.matchName(newVal);
					if(labelMatch||nameMatch){
						$scope.match = true;
					}
					else
						$scope.match = false;
				}
				else
					$scope.match = true;
			}
		})

		$scope.search = function(){
			if($scope.match){
				if($scope.queryInput){
					if($scope.queryInput.match(/=/)){
						if($scope.query.labels){
							$scope.query.labels = $scope.query.labels.concat($scope.queryInput.split(","))
						}
						else
							$scope.query.labels = $scope.queryInput.split(",");
						// console.log($scope.query);
					}
					else{
						$scope.query.name = $scope.queryInput;
					}
					$scope.request($scope.query);
				}
				$scope.queryInput="";
			}
		}
		
		$scope.question = false;
		
		var init = function(){
			if($scope.query.name || $scope.query.labels.length){
				$scope.request($scope.query);
			}
			else{
				$scope.checkboxAll = false;
				serviceList.getList(namespace).then(function(data){
					// console.log(data);
					$scope.serviceList = angular.copy(data);
					serviceAll = angular.copy(data);
					$scope.serviceList.forEach(function(item){
						item.checkbox = false;
					});
				});
			}
			if(!request)
				interRefresh();
		};
		init();
		// 刷新按钮,暂时取消
		// $scope.refresh = function(){
		// 	$scope.checkboxAll = false;
		// 	serviceList.getList().then(function(data){
		// 		$scope.serviceList = angular.copy(data);
		// 		serviceAll = angular.copy(data);
		// 		$scope.serviceList.forEach(function(item){
		// 			item.checkbox = false;
		// 		});
		// 	})
		// }

		//全选框功能
		$scope.$watch('checkboxAll',function(newVal, oldVal, scope){
			if(newVal != oldVal){
				if($scope.checkboxAll){
					$scope.serviceList.forEach(function(item){
						item.checkbox = true;
					});
				}
				else{
					$scope.serviceList.forEach(function(item){
						item.checkbox = false;
					})
				}
			};
		});	
		
		//服务停止
		$scope.stop = function(){
			var stopList = [];
			$scope.serviceList.forEach(function(item){
				if(item.checkbox){
					stopList.push(item);
				}
			});
			if(stopList.length == 0){
				var inf = {
					title: $filter('translate')('noServiceError'),
					text: $filter('translate')('selectServiceError'),
					type: "error"
				}
				inform.showInform(inf);
			}
			stopList.forEach(function(item,index){
				var sendData = {
					name: item.name,
					namespace: item.namespace
				};
				serviceList.stop(sendData).then(function(data){
					if(index==(stopList.length-1)){
						init();
					}
				});
			});
		}

		//服务开启
		$scope.start = function(){
			var startList = [];
			$scope.serviceList.forEach(function(item){
				if(item.checkbox){
					startList.push(item);
				}
			});
			if(startList.length==0){
				var inf = {
					title: $filter('translate')('noServiceError'),
					text: $filter('translate')('selectServiceError'),
					type: "error"
				}
				inform.showInform(inf);
			}
			startList.forEach(function(item,index){
				var sendData = {
					name: item.name,
					namespace: item.namespace
				};
				serviceList.start(sendData).then(function(data){
					if(index==(startList.length-1)){
						init();
					}
				});
			});
		}

		//删除服务
		$scope.delete = function(){
			var deleteList = [];
			$scope.serviceList.forEach(function(item){
				if(item.checkbox){
					deleteList.push(item);
				}
			});
			if(deleteList.length==0){
				var inf = {
					title: $filter('translate')('noServiceError'),
					text: $filter('translate')('selectServiceError'),
					type: "error"
				}
				inform.showInform(inf);
			}
			console.log(deleteList);
			deleteList.forEach(function(item,index){
				var sendData = {
					name : item.name,
					namespace : item.namespace
				};
				serviceList.deleteService(sendData).then(function(data){
					if(index==(deleteList.length-1)){
						init();
					}
				});
			});
		}

		//search
		$scope.deleteName = function(){
			$scope.query.name = "";
			$scope.request($scope.query);
			init();
		}
		$scope.deleteLabel = function(index){
			$scope.query.labels.splice(index,1);
			$scope.request($scope.query);
			init();
		}

	}]);