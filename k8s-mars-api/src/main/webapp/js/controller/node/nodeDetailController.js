'use strict';

angular.module('mainApp')

.controller('NodeDetailCtrl',['$scope','nodeStore','$stateParams','nodeMonitorStore','$interval','lineCharts','multiLineCharts','$filter','inform',function($scope,nodeStore,$stateParams,nodeMonitorStore,$interval,lineCharts,multiLineCharts,$filter,inform){

	var vm = $scope;
	var nodeName = $stateParams.nodeName;
	var time;
	var loglist;
	var comlist = [];

	var typlist = [
					{name:'node'},
					{name:'process'}
				];

	vm.spTabs = [
			{serName:'nodeInfo',serTemplate:'node/nodeInformation'},
			// {serName:'podList',serTemplate:'node/podInstance'},
			// {serName:'event',serTemplate:'node/event'},
			{serName:'monitor',serTemplate:'node/monitorChart'},
			{serName:'log',serTemplate:'node/log'}
		];

		vm.rangeList = [
		{
			value:'0',
			name:'thirtyMinute'
		},{
			value:'1',
			name:'sixHour'
		},{
			value:'2',
			name:'twentyFourHour'
		},{
			value:'3',
			name:'sevenDay'
		}
		// ,{
		// 	value:'4',
		// 	name:'thirtyDay'
		// }
	];
	$scope.color = ['#018588','#f5b297'];

	$scope.select = {
			type :'',
			range:'0',
			target:'',
			name : '',
			startTime: '',
			processName:'',
			com:''
		};

	vm.back = function(){
		history.back(-1);
	}
	// var res = [];
	vm.NodeName = nodeName;
	var init = function(){

		nodeMonitorStore.getComponentStatus().then(function(data){

			var res = [];
			for(var i = 0,l = data.length; i<l; i++){
				if(data[i].host == nodeName){
					data[i].name =  $filter('harborName')(data[i].name);
					res.push(data[i]);
				}
			}
			console.log(data);
			$scope.comList = res;
			console.log(res);
			vm.select.com = res[0].name;
			console.log(vm.select.com);
		});
		// vm.select.com = res[0].name;
		nodeStore.detail(nodeName).then(function(data){
			vm.node = data;
		});

		nodeStore.podList(nodeName).then(function(data){
			vm.podList = data;

			//get pie data
			var running = 0;
			var pause = 0;
			if($scope.podList){
				$scope.podList.forEach(function(item){
					if(item.status == 'Running'){
						running++;
					}
					else{
						pause++;
					}
				})
			}
			$scope.pieData = [{
				name:'Running',
				value: running
			},{
				name: 'Pause',
				value: pause
			}];
		});

		nodeStore.eventList(nodeName).then(function(data){
			vm.eventList = data;
		});

		nodeStore.list().then(function(data){
			vm.nodeList = data;
			$scope.select.node = $scope.nodeList[0].ip;
		});

		nodeStore.podList(nodeName).then(function(data){
			vm.podList = data;
		});

		labelInit();
	};

	//label related
	vm.tempLabel = {
		key: "",
		value: ""
	}
	var initialLabel;
	var labelInit = function(){
		nodeStore.getLabel(nodeName).then(function(data){
			vm.labels = angular.copy(data);
			initialLabel = angular.copy(data);
			vm.labelChange = false;
		});
	}
	vm.addLabel = function(){
		if(!!vm.tempLabel.key && !!vm.tempLabel.value){
			vm.labels[vm.tempLabel.key] = vm.tempLabel.value;
		}
		vm.tempLabel = {
			key: "",
			value: ""
		}
		vm.labelChange = !angular.equals(initialLabel,vm.labels);
		console.log(vm.labelChange);
	}
	vm.deleteLabel = function(key){
		delete vm.labels[key];
		vm.labelChange = !angular.equals(initialLabel,vm.labels);
	}
	vm.saveLabels = function(){
		var sendData = {
			name: nodeName,
			labels: vm.labels
		}
		nodeStore.setLabel(sendData).then(function(data){
			if(data.success){
				var inf = {
					title: $filter('translate')('Success'),
					text: $filter('translate')('Label Edit Success'),
					type: "noti"
				}
				inform.showInform(inf);
			}
			else{
				var inf = {
					title: $filter('translate')('Error'),
					text: data.errMsg,
					type: "error"
				}
				inform.showInform(inf);
			}
			labelInit();
		})
	}

		//监控部分
		
		var interval;
		var empty = function(){
			$scope.nodata = true;
			angular.element('#graph')[0].innerHTML="";
		}

		vm.typList = typlist;
		$scope.$watch('select.type',function(n,o){
			$scope.select.range = '0';
			if(n != o){
				$scope.select.type = n;
				if(n == 'process'){
					$scope.select.target = 'processcpu';
					vm.select.com = vm.comList[0].name;
				}else{
					$scope.select.target = 'nodecpu';
				}
				if(vm.nodeList){
					$scope.nodeList.forEach(function(item){
						
						if(item.name == nodeName){
							
							$scope.select.name = nodeName;
							$scope.select.startTime = item.time;
							$scope.select.processName = $scope.comList[0].name;
							
							console.log($scope.comList[0].name);
							vm.typList = typlist;
							var sendData = {
											processName : $scope.select.processName,
											rangeType : $scope.select.range,
											node : nodeName
										};
							nodeStore.getLog(sendData).then(function(data){
								loglist = data;
								fenyefunction(loglist);
							})
						}

						var now = Date.now();
						$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
						$scope.timeSpan = parseInt($scope.timeSpan/60000);
						$scope.chooseType($scope.select.target);
					})
				}
			}
		})


		//得到日志信息
		$scope.$watch('select.com',function(newVal, oldVal, scope){
			if(newVal != oldVal){
				$scope.select.processName = newVal;
				
				$scope.chooseType($scope.select.target);
				var sendData = {
					processName : $scope.select.processName,
					rangeType : vm.select.range,
					node : nodeName
				};
				nodeStore.getLog(sendData).then(function(data){
					loglist = data;
					
					if(!!loglist){
						fenyefunction(loglist);
						
					}

				});
			}
		});
		$scope.$watch('select.range',function(newVal, oldVal, scope){
			if(newVal != oldVal){
				var now = Date.now();
				$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
				$scope.timeSpan = parseInt($scope.timeSpan/600000);
				if($scope.timeSpan < 30){
					$scope.select.range = '5';
				}
						
				$scope.chooseType($scope.select.target);
				var sendData = {
					processName : $scope.select.processName,
					rangeType : $scope.select.range,
					node : nodeName
				};
				nodeStore.getLog(sendData).then(function(data){
					
					loglist= data;
					if(!!loglist){
						fenyefunction(loglist);
					}
				});
			}
		});

	$scope.myNumber = 0;

		//选择类型
		$scope.chooseType = function(type){	
			$interval.cancel(interval);
			var unit;
			if(type == 'nodecpu' || type == 'processcpu' || type == 'processmem'){
				unit = "%";
			}
			else if(type == 'memory'){
				unit = "MiB";
			}
			else {
				unit = "KB";
			}
			
			$scope.select.target = type;
			
			nodeMonitorStore.getMonitor($scope.select).then(function(data){
				if(!data.results[0].series){
						var chart = lineCharts.createChart('#graph',$scope.graph.name,unit);
						chart.clearData();
						empty();

					}
					else{
						var datalist = data.results[0].series[0];
						datalist.values.forEach(function(item,index){
						
							if(item == null){
								item = 0;
							}
						})
						for(var i=0;i<datalist.values.length;i++){
							if(datalist.values[i][1]==null){
								datalist.values[i][1] = 0;
							}
						}
						$scope.nodata = false;
						$scope.graph = datalist;
								
						var chart = lineCharts.createChart('#graph',$scope.graph.name,unit);
						chart.batchAddData($scope.graph.values);
					}
			});

			interval = $interval(function(){
				nodeMonitorStore.getMonitor($scope.select).then(function(data){
					var datalist;
						if(!data.results[0].series){
							empty();
						}
						else{
							datalist = data.results[0].series[0];
							for(var i=0;i<datalist.values.length;i++){
								if(datalist.values[i][1]==null){
									datalist.values[i][1] = 0;
								}
							}
							$scope.nodata = false;
							$scope.graph = datalist;
			
							var chart = lineCharts.createChart('#graph',$scope.graph.name,unit);
							chart.batchAddData($scope.graph.values);
						}
				});
			},5000);
		};

		
		$scope.$on('$destroy',function(){
			$interval.cancel(interval);
		});


var fenyefunction = function(data){
	if(!!data){
	
    var d2 = $filter('limitTo')(data, 15);
    $scope.itemsPerPage = 15;
    $scope.currentPage = 0;
    
    $scope.log = d2;
         
    $scope.pageCount = function () {
        	
        return Math.ceil(data.length / $scope.itemsPerPage) - 1;
    };   

        $scope.nextPage = function () {
            if ($scope.currentPage < $scope.pageCount() && $scope.currentPage>=0) {
       
                $scope.currentPage++;
 
                var d2 = $filter('limitTo')(data, ($scope.currentPage+1)*15);
                $scope.log = d2;
                
            }
        };

        $scope.nextPageDisabled = function () {
            return $scope.currentPage === $scope.pageCount() ? "disabled" : "";
        }
    $scope.logmore = function(page){
        	if(page == $scope.pageCount()-1){
        		
        		return false;
        	}else{
        		return true;
        	}
        }
    }
}
	
init();	

		
}])
