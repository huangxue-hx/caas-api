'use strict';

angular.module('mainApp').controller('ServiceNewController',ServiceNewController);
ServiceNewController.$inject = ['$scope','ngDialog','containerList','serviceList','$location','inform','pattern','$filter','$rootScope','namespace'];
	function ServiceNewController($scope,ngDialog,containerList,serviceList,$location,inform,pattern,$filter,$rootScope,namespace){

	$scope.back = function(){
		if(containerList.containers.length!=0){
			var d = ngDialog.open({
				template:'../template/deleteConfirm.html',
				width:300,
				closeByDocument: false,
				controller: ['$scope',function($scope){
					$scope.deleteText = $filter('translate')('giveUpService');
					$scope.yes = function(){
						ngDialog.close(this,'yes');
					};
					$scope.no = function(){
						ngDialog.close(this,'no');
					}
				}]
			});
			d.closePromise.then(function(data){
				if(data.value == 'yes'){
					containerList.containers=[];
					history.back(-1);
				}
			});
		}
		else{
			history.back(-1);
		}
	}

	$scope.modifyContainer = function (container,$index){
		// console.log(name);
		var tem = document.querySelector('#modifyContainer').innerHTML;
		var d = ngDialog.open({
			template:tem,
			plain:true,
			width:800,
			closeByDocument: false,
			appendClassName:'hc-dialog',
			controller: 'ModifyContainerController',
			data:{
				index: $index,
				container:container
			}
		});
	}
	$scope.newContainer = function(){
		var tem = document.querySelector('#containerNew').innerHTML;
		// console.log(tem);
		var d = ngDialog.open({
			template:tem,
			plain: true,
			width:800,
			closeByDocument: false,
			appendClassName:'hc-dialog',
			controller: 'NewContainerController'
		});	
	}
	$scope.deleteContainer = function(index){
		containerList.containers.splice(index,1);
		$scope.containers = containerList.containers;
	}
	$scope.expContainer = function(index){
		//no influence for last item,just for other item 
		if(index < $scope.containers.length-1){
			//if it has expanded,fold all item
			if($scope.containers[index].exp){
				$scope.containers.forEach(function(item){
					item.exp = false;
				});
			}
			//if it fold,fold all and expand this
			else{
				$scope.containers.forEach(function(item){
					item.exp = false;
				});
				$scope.containers[index].exp = true;
			}
		}
	}

	$scope.namePattern = pattern.name;
	$scope.labelPattern = pattern.label;
	$scope.ipPattern = pattern.ip;

	$scope.containers = containerList.containers;
	$scope.serviceName = "";
	$scope.namespace = "";
	$scope.label = "";
	$scope.instanceCount;
	$scope.annotation = "";
	$scope.service = [];
	$scope.clusterIP = "";
	$scope.hostName = "";
	$scope.selector = {};
	$scope.tempselector = {
		key: "",
		value: ""
	};

	$scope.namespace = $rootScope.currentNamespace;
	$rootScope.$on('namespaceCheckChange',function(event,data){
		$scope.namespace = data.namespace;
	})

	//initial available selector
	var availableSelector;
	$scope.availableKey = [];
	$scope.availableValue = [];
	serviceList.getSelector().then(function(data){
		availableSelector = data;
		availableSelector.forEach(function(item){
			$scope.availableKey.push(item.name);
		});
	});
	$scope.$watch('tempselector.key',function(newval,oldval){
		if(newval != oldval && !!newval){
			$scope.availableValue = [];
			availableSelector.forEach(function(item){
				if(item.name == newval){
					$scope.availableValue = item.values;
				}
			});
		}
	});
	$scope.addSelector = function(){
		if(!!$scope.tempselector.key && !!$scope.tempselector.value){
			$scope.selector[$scope.tempselector.key] = $scope.tempselector.value;
			$scope.tempselector = {
				key: "",
				value: ""
			};
		}
	}
	$scope.deleteSelector = function(key){
		delete $scope.selector[key];
	}

	$scope.setup = function(){
		serviceList.getList($scope.namespace).then(function(data){
			var service = data;
			var sameName = false;
			if(!$scope.configIp){
				$scope.clusterIP = "";
			}
			if(service && service.length > 0){
				service.forEach(function(item){
					if(item.namespace == $scope.namespace){
						if(item.name == $scope.serviceName)
							sameName = true;
					}
				});
			}
			if(sameName){
				var inf = {
					title: $filter('translate')('nameDuplicate'),
					text: $filter('translate')('serviceNameDuplicate'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else {
				var sendData = {
					name : $scope.serviceName,
					namespace : $scope.namespace,
					labels : $scope.label,
					instance : $scope.instanceCount,
					selector : $scope.selector,
					sessionAffinity: $scope.session,
					containers: angular.copy($scope.containers),
					annotation: $scope.annotation,
					clusterIP: $scope.clusterIP,
					hostName: $scope.hostName
				};
				serviceList.newService(sendData).then(function(res){
					if(res.success){
						containerList.containers=[];
						$location.path('/service/serviceAll');
					}
				});
				// console.log(sendData);
			}

		});
	}

	$scope.disable = function(){
		if(!$scope.instanceCount || !$scope.serviceName || !$scope.namespace || ($scope.containers.length<1) ||($scope.configIp&& !$scope.clusterIP))
			return true;
		else 
			return false;
	}

	$scope.$on('$destroy',function(){
		containerList.containers=[];
	});
};