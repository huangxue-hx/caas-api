'use strict';

angular.module('mainApp').controller('ComponentDetailController',ComponentDetailController);
ComponentDetailController.$inject = ['$scope','$stateParams','nodeMonitorStore','ngDialog','$interval','lineCharts','multiLineCharts','nodeStore','$filter'];
function ComponentDetailController($scope,$stateParams,nodeMonitorStore,ngDialog,$interval,lineCharts,multiLineCharts,nodeStore,$filter){
	var vm = $scope;
	var unit = '%';
	var interval;
	vm.compName = $stateParams.name;
	vm.nodeName = $stateParams.node;
	vm.spTabs = [
			{serName:'monitor', serTemplate:'component/monitorChart'},
			{serName:'log', serTemplate:'component/log'}
		];
	vm.back = function(){
		history.back(-1);
	}
	var init = function(){
		nodeMonitorStore.getComponentStatus({name: vm.nodeName,processName: vm.compName}).then(function(data){
			vm.comp = data[0];
			loadGraph();
		});
		loadLog();
	};
	vm.selectNow = {
		processName: vm.compName, //Component
		range: '0',//timerange
		name: vm.nodeName, //nodename
		target: 'processcpu',  // processcpu , processmem,  disk
		type:'process'   // process  
	};
	vm.chooseType = function(type){
		vm.selectNow.target = type;
		loadGraph();

	};
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
	vm.$watch('selectNow.range',function(n,o){
		if(n!= o){
			loadGraph();
			loadLog();
		}
	});
	var loadGraph = function(){

		if(interval){
			$interval.cancel(interval);
		}
		if(vm.selectNow.range == '0'){
			interval = $interval(loadGraph,5000);
		}

		nodeMonitorStore.getMonitor(vm.selectNow).then(function(data){
			if(!data.results[0].series){
					console.log(data.results[0]);
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
	};
	var loadLog = function(){
		nodeStore.getLog({
			processName: $filter('harborName')(vm.selectNow.processName),
			rangeType: vm.selectNow.range,
			node: vm.selectNow.name
		}).then(function(data){
			vm.log = data;
		});
	};


	vm.$on('$destroy',function(){
		if(interval){
			$interval.cancel(interval);
		}
	});





	init();
} 	