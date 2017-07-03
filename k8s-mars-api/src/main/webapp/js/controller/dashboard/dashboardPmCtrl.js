'use strict';

angular.module('mainApp').controller('DashboardPmController',DashboardPmController);
DashboardPmController.$inject = ['$scope','$stateParams','dashboardStore','$rootScope','namespace','tenant','harborStore']
function DashboardPmController($scope,$stateParams,dashboardStore,$rootScope,namespace,tenant,harborStore){
	var tenantid = $rootScope.tenantid;
	var name = $rootScope.currentNamespace;
	$scope.errColor = ['#d56983','#f0a5ba','#fab2c0','#edb3b1','#f3cad0'];
	$rootScope.$on('namespaceCheckChange',function(event,data){
		tenantid = data.tenantid;
		name = data.namespace;
		init();
	})
	var init = function(){
		if(!!tenantid && !!name){
			namespace.detail(tenantid,name).then(function(data){
				$scope.project = data;

				$scope.pod = $scope.project.quota.pod;
				$scope.cpu = [parseInt($scope.project.quota.cpu[0]),parseInt($scope.project.quota.cpu[1])];
				// $scope.cpu = $scope.project.quota.cpu;
				$scope.memory = [parseInt($scope.project.quota.memory[0]),parseInt($scope.project.quota.memory[1])];
			});

			tenant.detail(tenantid).then(function(data){
				$scope.tenant = data[0];
				namespace.getUser($scope.tenant.name,name).then(function(data){
					$scope.userList = data;
					$scope.userCount = $scope.userList.length;
				});
			});

			namespace.userNum(name).then(function(data){
				$scope.userDeployment = [];
				data.userArr.forEach(function(item){
					var temp = {
						name: item.name,
						value: item.num
					}
					$scope.userDeployment.push(temp);
				});
				$scope.totalDeploment = data.totalNum;
			});

			//get image count by user
			harborStore.statistcsByNamespace(name).then(function(data){
				$scope.statistics = data;
				$scope.insecurity = [];
				$scope.total = [];
				$scope.statistics.forEach(function(item){
					var tempa = {
						name: item.name,
						value: item.data.unsecurity_image_num||0
					}
					var tempb = {
						name: item.name,
						value: item.data.image_num||0
					}
					$scope.insecurity.push(tempa);
					$scope.total.push(tempb);
				});
			});

			//get all image count in this namespace
			harborStore.listViaUser(name).then(function(data){
				$scope.abnormal = 0;
				$scope.notSupport = 0;
				$scope.success = 0;
				$scope.insecurityCount = 0;
				$scope.totalImg = 0;
				data.forEach(function(item){
					harborStore.harborAnalyze(item.name).then(function(d){
						console.log(d);
						$scope.abnormal = $scope.abnormal + d.abnormal||0;
						$scope.notSupport = $scope.notSupport + d.clair_not_Support||0;
						$scope.success = $scope.success + d.clair_success||0;
						$scope.insecurityCount = $scope.insecurityCount + d.unsecurity_image_num||0;
						$scope.totalImg = $scope.totalImg + d.image_num||0;
					});
				});
				// console.log(data);
			});

			dashboardStore.warningInfo({namespace:name}).then(function(data){
				$scope.warningInfo = data;
			});

			dashboardStore.eventInfo({namespace:name}).then(function(data){
				$scope.eventInfo = data;
			});
		}
		// dashboardStore.podInfo().then(function(data){
		// 	vm.podInfo = data;
		// });
		// dashboardStore.nodeCpu().then(function(data){
		// 	vm.nodeCpu = data;
		// 	vm.nodeCpuNow = parseFloat(data[data.length-1][1]).toFixed(2);
		// });
		// dashboardStore.nodeMem().then(function(data){
		// 	vm.nodeMem = data;
		// 	var last = data[data.length-1][1];
		// 	vm.nodeMemNow = last;
		// });
		// dashboardStore.nodeDisk().then(function(data){
		// 	vm.nodeDisk = data;
		// 	var last = data[data.length-1][1];
		// 	vm.nodeDiskNow = last;
		// });
		// dashboardStore.infraInfo().then(function(data){
		// 	vm.infraInfo = data;
		// });
		// dashboardStore.tenantInfo().then(function(data){
		// 	vm.tenantInfo = data;
		// });
	};

	init();
}