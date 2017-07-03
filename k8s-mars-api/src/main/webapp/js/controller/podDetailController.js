'use strict'

angular.module('mainApp').controller('PodDetailController',PodDetailController);
PodDetailController.$inject = ['$scope','$stateParams','pod','$http','imgStore','inform','$filter','serviceList','storage','duplicate','pattern','suFormat']
	function PodDetailController($scope,$stateParams,pod,$http,imgStore,inform,$filter,serviceList,storage,duplicate,pattern,suFormat){
		
		var sendName = $stateParams.name;
		var service = $stateParams.service;
		var conList; //用来记录用户对pod中的容器列表的修改

		$scope.serviceName = $stateParams.service;
		// $scope.serviceTime = $stateParams.time;
		$scope.namespace = $stateParams.namespace;
		// console.log(sendName);

		$scope.pathPattern = pattern.path;
		$scope.urlPattern = pattern.url;

		$scope.spTabs = [
			{serName:'podInformation',serTemplate:'podInformation'},
			{serName:'logInformation',serTemplate:'podContainer'},
			// {serName:'event',serTemplate:'podEvent'},
		];

		$scope.select = {
			out :0,
			con: '',
			range:'0',
			logdir:'',
		};
		$scope.rangeList = [{
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
		},{
			value:'4',
			name:'thirtyDay'
		}];
		$scope.outputlist = [
			{
				value:'0',
				name:'output'
			},{
				value:'1',
				name:'logout'
			}
		];
	// $scope.select.range = $scope.rangeList[0];
		$scope.back = function(){
			history.back(-1);
		}

		var initProbe = function(){
			$scope.livenessProbe={
				key:'',
				value:'',
				exist:false
			}
			$scope.readinessProbe={
				key:'',
				value:'',
				exist:false
			}

			if($scope.container.livenessProbe&&$scope.container.livenessProbe.httpGet){
				$scope.livenessProbe.key = 'httpGet';
				$scope.livenessProbe.value = 'http://'+$scope.pod.ip+':'+$scope.container.livenessProbe.httpGet.port+'/'+$scope.container.livenessProbe.httpGet.path;
				$scope.livenessProbe.exist = true;
			}
			else if($scope.container.livenessProbe&&$scope.container.livenessProbe.tcpSocket){
				$scope.livenessProbe.key = 'tcpSocket';
				$scope.livenessProbe.value = $scope.pod.ip+':'+$scope.container.livenessProbe.tcpSocket.port;
				$scope.livenessProbe.exist = true;
			}
			else{
				$scope.livenessProbe.exist = false;
			}

			if($scope.container.readinessProbe&&$scope.container.readinessProbe.httpGet){
				$scope.readinessProbe.key = 'httpGet';
				$scope.readinessProbe.value = 'http://'+$scope.pod.ip+':'+$scope.container.readinessProbe.httpGet.port+'/'+$scope.container.readinessProbe.httpGet.path;
				$scope.readinessProbe.exist = true;
			}
			else if($scope.container.readinessProbe&&$scope.container.readinessProbe.tcpSocket){
				$scope.readinessProbe.key = 'tcpSocket';
				$scope.readinessProbe.value = $scope.pod.ip+':'+$scope.container.readinessProbe.tcpSocket.port;
				$scope.readinessProbe.exist = true;
			}
			else{
				$scope.readinessProbe.exist = false;
			}

			// console.log($scope.readinessProbe);
		}

		$scope.expand = function(conIndex){
			if(!!$scope.pod.containers[conIndex].expand){
				$scope.pod.containers[conIndex].expand = false;
			}
			else{
				$scope.pod.containers[conIndex].expand = true;
			}
		}

		//发送请求得到pod详情
		pod.getPod(sendName,$scope.namespace).then(function(data){
			console.log(data);
			console.log($scope.container);
			
			$scope.pod = data;
			$scope.select.con = data.containers[0].name;
			$scope.select.out = $scope.outputlist[0].value;
			$scope.container = angular.copy($scope.pod.containers[0]);
			conList = angular.copy($scope.pod.containers);
			$scope.conIndex = 0;
			// console.log(conList);
			initProbe();
		},function(err){
			if(suFormat.notFound(err)){
				$scope.back();
			}
		});

		//得到日志信息
		var cont;
		var scoid;
		$scope.$watch('select.out',function(newVal,oldVal,scope){
			if(newVal != oldVal){
				if(newVal == 0){
					var sendData = {
						container : $scope.select.con,
						pod: $scope.pod.name,
						namespace:$scope.namespace,
						rangeType:$scope.select.range
					};
					pod.getLogOrigin(sendData).then(function(data){
						$scope.log = data;
					})
				}
			
				if(newVal == 1){
					var sendData = {
							container : $scope.select.con,
							namespace : $scope.namespace,
						};
					pod.getlogdir(sendData).then(function(data){
						$scope.logdirlist = data;
						$scope.select.logdir = data[0];
						var sendDatas = {
							container : $scope.select.con,
							logdir : $scope.select.logdir,
							namespace : $scope.namespace,
							rangeType : $scope.select.range,
							scrollId : '',
						}
						pod.getlog(sendDatas).then(function(data){
							$scope.log = data.log;
							scoid = data.ScrollId;
						})
						if($scope.log.length > 200){
							var sendData ={
								container : $scope.select.con,
								logdir : $scope.select.logdir,
								namespace : $scope.namespace,
								rangeType : $scope.select.range,
								scrollId : scoid,
							};
							pod.getlog(sendData).then(function(data){
								$scope.log = data.log;
								scoid = data.ScrollId;
								if(!!$scope.log){
									fenyefunction($scope.log);
								}
							});
						}
					});
				}
			}
		})
		$scope.$watch('select.con',function(newVal, oldVal, scope){
			cont = newVal;
			if(newVal != oldVal){
				if($scope.select.out == 1){
					var sendData = {
							container : $scope.select.con,
							namespace : $scope.namespace,
						};
					pod.getlogdir(sendData).then(function(data){
						$scope.logdirlist = data;
						$scope.select.logdir = data[0];
						var sendDatas = {
							container : $scope.select.con,
							logdir : $scope.select.logdir,
							namespace : $scope.namespace,
							rangeType : $scope.select.range,
							scrollId : '',
						}
						pod.getlog(sendDatas).then(function(data){
							$scope.log = data.log;
							scoid = data.ScrollId;
						})
						if($scope.log.length > 200){
							var sendData ={
								container : $scope.select.con,
								logdir : $scope.select.logdir,
								namespace : $scope.namespace,
								rangeType : $scope.select.range,
								scrollId : scoid,
							};
							pod.getlog(sendData).then(function(data){
								$scope.log = data.log;
								scoid = data.ScrollId;
								if(!!$scope.log){
									fenyefunction($scope.log);
								}
							});
						}
					});
				}
				if($scope.select.out == 0){
					var sendData = {
						container : $scope.select.con,
						pod: $scope.pod.name,
						namespace:$scope.namespace,
						rangeType:$scope.select.range
					};
					pod.getLogOrigin(sendData).then(function(data){
						$scope.log = data;
					})
				}
			}
		});

		$scope.$watch('select.logdir',function(newVal,oldVal,scope){
			$scope.select.range = '0';
			if(newVal != oldVal){
				var sendData = {
					container : cont,
					logdir : newVal,
					namespace : $scope.namespace,
					rangeType : $scope.select.range,
					scrollId : '',
				};
				pod.getlog(sendData).then(function(data){
					$scope.log = data.log;
					scoid = data.ScrollId;
				});
				if($scope.log.length > 200){
					var sendData ={
						container : $scope.select.con,
						logdir : $scope.select.logdir,
						namespace : $scope.namespace,
						rangeType : $scope.select.range,
						scrollId : scoid,
					};
					pod.getlog(sendData).then(function(data){
						$scope.log = data.log;
						scoid = data.ScrollId;
						if(!!$scope.log){
							fenyefunction($scope.log);
						}
					});
				}
			}
		})
	

		$scope.$watch('select.range',function(newVal, oldVal, scope){
			if(newVal != oldVal){
				var now = Date.now();
				$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
				$scope.timeSpan = parseInt($scope.timeSpan/600000);
				if($scope.select.out == 1){
					var sendData = {
							container : $scope.select.con,
							namespace : $scope.namespace,
						};
					pod.getlogdir(sendData).then(function(data){
						$scope.logdirlist = data;
						$scope.select.logdir = data[0];
						var sendDatas = {
							container : $scope.select.con,
							logdir : $scope.select.logdir,
							namespace : $scope.namespace,
							rangeType : $scope.select.range,
							scrollId : '',
						}
						pod.getlog(sendDatas).then(function(data){
							$scope.log = data.log;
							scoid = data.ScrollId;
						})
						if($scope.log.length > 200){
							var sendData ={
								container : $scope.select.con,
								logdir : $scope.select.logdir,
								namespace : $scope.namespace,
								rangeType : $scope.select.range,
								scrollId : scoid,
							};
							pod.getlog(sendData).then(function(data){
								$scope.log = data.log;
								scoid = data.ScrollId;
								if(!!$scope.log){
									fenyefunction($scope.log);
								}
							});
						}
					});
				}
				if($scope.select.out == 0){
					var sendData = {
						container : $scope.select.con,
						pod: $scope.pod.name,
						namespace:$scope.namespace,
						rangeType:$scope.select.range
					};
					pod.getLogOrigin(sendData).then(function(data){
						$scope.log = data;
					})
				}
			}
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
	};