'use strict';

angular.module('mainApp')
	.controller('ServiceDetailController',['$scope','ngDialog','$stateParams','$http','serviceList','pod','multiLineCharts','$timeout','autoscaleStore','$interval','monitorStore','lineCharts','inform','$filter','pattern','storage',function($scope,ngDialog,$stateParams,$http,serviceList,pod,multiLineCharts,$timeout,autoscaleStore,$interval,monitorStore,lineCharts,inform,$filter,pattern,storage){
		var sendName = $stateParams.name;
		var sendNamespace = $stateParams.namespace;
		console.log(sendName);
		$scope.namespace = sendNamespace;
		$scope.color = ['#018588','#f5b297'];
		// $scope.time = $stateParams.time;
		$scope.spTabs = [
			{serName:'basicInformation',serTemplate:'/service/serviceInformation'},
			{serName:'logInformation',serTemplate:'/service/logInformation'},
			// {serName:'event',serTemplate:'event'},
			{serName:'monitor',serTemplate:'monitorChart'},
			{serName:'autoflex',serTemplate:'autoFlex'}
		];

		var outlist = [];
		$scope.outputlist = [
		{
			value:'0',
			name:'output'
		},{
			value:'1',
			name:'logout'
		}
	];
		outlist = $scope.outputlist;

		var initEvent = function(){
			serviceList.event(sendName,sendNamespace).then(function(data){
				$scope.events = data;
			});
		}
		var initPodList = function(){
			serviceList.podList(sendName,sendNamespace).then(function(data){
				$scope.podList = data;

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
		}

		//container block relative
		$scope.pathPattern = pattern.path;
		$scope.urlPattern = pattern.url;
		var conList;
		var change = {
			resource: false,
			command : false,
			args: false,
			env: false,
			port: false,
			storage: false,
			configmap: false
		};
		$scope.temp = {
			cpu: "",
			memory: ""
		};
		var initContainer = function(){
			// 取到volume列表,挂载卷
			storage.getList($scope.namespace).then(function(data){
				$scope.volume = data;
				$scope.volumeName = [];
				$scope.volume.forEach(function(item){
					$scope.volumeName.push(item.name);
				});
			});

			serviceList.conList(sendName,sendNamespace).then(function(data){
				$scope.containers = angular.copy(data);
				conList = angular.copy($scope.containers);
				change = {
					resource: false,
					command : false,
					args: false,
					env: false,
					port: false,
					storage: false,
					configmap: false
				};
			},function(err){
				
			});
		}
		initContainer();
		//编辑CPU
		$scope.editConCpu = function(index){
			$scope.temp.cpu = parseInt($scope.containers[index].resource.cpu);
			if(!!$scope.containers[index].ifCpuEdit)
				$scope.containers[index].ifCpuEdit = false;
			else
				$scope.containers[index].ifCpuEdit = true;
		}
		$scope.saveConCpu = function(index,temp){
			$scope.containers[index].resource.cpu = $filter('cpuFormat')(temp);
			// conList[index] = angular.copy($scope.containers[index]);
			change.resource = !angular.equals(conList[index].resource,$scope.containers[index].resource);
			
			if(!!$scope.containers[index].ifCpuEdit)
				$scope.containers[index].ifCpuEdit = false;
			else
				$scope.containers[index].ifCpuEdit = true;
		}

		//编辑内存
		$scope.editMemory = function(index){
			if($scope.containers[index].resource.memory.indexOf('G')>=0){
				$scope.temp.memory = parseInt($scope.containers[index].resource.memory)*1024;
			}
			else{
				$scope.temp.memory = parseInt($scope.containers[index].resource.memory);
			}
			if(!!$scope.containers[index].ifMemoryEdit)
				$scope.containers[index].ifMemoryEdit = false;
			else
				$scope.containers[index].ifMemoryEdit = true;
		} 
		$scope.saveMemory = function(index,temp){
			$scope.containers[index].resource.memory = temp;
			// conList[index] = angular.copy($scope.containers[index]);
			change.resource = !angular.equals(conList[index].resource,$scope.containers[index].resource);
			
			if($scope.containers[index].ifMemoryEdit)
				$scope.containers[index].ifMemoryEdit = false;
			else
				$scope.containers[index].ifMemoryEdit = true;
		}

		//edit config map
		$scope.changeConfigmap = function(conIndex,p){
			$scope.containers[conIndex].configmap.path = p;
			change.configmap = !angular.equals(conList[conIndex].configmap,$scope.containers[conIndex].configmap);
		}
		$scope.editConfigValue = function(conIndex){
			var d = ngDialog.open({
				template:'../view/service/changeConfigValue.html',
				width:600,
				closeByDocument: false,
				controller: EditConfigController,
				data:{
					name: $scope.containers[conIndex].configmap.name,
					namespace: $scope.namespace
				}
			});
		}

		//修改执行命令
		$scope.changeCommand = function(conIndex,index,c){
			$scope.containers[conIndex].command[index] = c;
			change.command = !angular.equals(conList[conIndex].command,$scope.containers[conIndex].command);
			// console.log($scope.tempPort);
		}
		$scope.deleteCommand = function(conIndex,index){
			$scope.containers[conIndex].command.splice(index,1);
			change.command = !angular.equals(conList[conIndex].command,$scope.containers[conIndex].command);
		}
		$scope.addCommand = function(conIndex){
			if(!$scope.containers[conIndex].command){
				$scope.containers[conIndex].command = [];
			}
			if(!!$scope.containers[conIndex].tempCommand){
				$scope.containers[conIndex].command.push($scope.containers[conIndex].tempCommand);
				change.command = !angular.equals(conList[conIndex].command,$scope.containers[conIndex].command);
				$scope.containers[conIndex].tempCommand = "";
			}
		}
		
		//修改执行参数
		$scope.changeArgs = function(conIndex,index,a){
			$scope.containers[conIndex].args[index] = a;
			change.args = !angular.equals(conList[conIndex].args,$scope.containers[conIndex].args);
			// console.log($scope.tempPort);
		}
		$scope.deleteArgs = function(conIndex,index){
			$scope.containers[conIndex].args.splice(index,1);
			change.args = !angular.equals(conList[conIndex].args,$scope.containers[conIndex].args);
		}
		$scope.addArgs = function(conIndex){
			if(!$scope.containers[conIndex].args){
				$scope.containers[conIndex].args = [];
			}
			if(!!$scope.containers[conIndex].tempArgs){
				$scope.containers[conIndex].args.push($scope.containers[conIndex].tempArgs);
				change.args = !angular.equals(conList[conIndex].args,$scope.containers[conIndex].args);
				$scope.containers[conIndex].tempArgs = "";
			}
		}

		//修改环境变量
		$scope.changeEnv = function(conIndex,index,e,type){
			if(type == 'name'){
				$scope.containers[conIndex].env[index].name = e;
			}
			else{
				$scope.containers[conIndex].env[index].value = e;
			}
			change.env = !angular.equals(conList[conIndex].env,$scope.containers[conIndex].env);
			// console.log($scope.tempPort);
		}
		$scope.deleteEnv = function(conIndex,index){
			$scope.containers[conIndex].env.splice(index,1);
			change.env = !angular.equals(conList[conIndex].env,$scope.containers[conIndex].env);
		}
		$scope.addEnv = function(conIndex){
			if(!$scope.containers[conIndex].env){
				$scope.containers[conIndex].env = [];
			}
			if(!!$scope.containers[conIndex].tempEnvName && !!$scope.containers[conIndex].tempEnvValue){
				var temp = {
					name: $scope.containers[conIndex].tempEnvName,
					value: $scope.containers[conIndex].tempEnvValue
				}
				$scope.containers[conIndex].env.push(temp);
				change.env = !angular.equals(conList[conIndex].env,$scope.containers[conIndex].env);
				$scope.containers[conIndex].tempEnvValue = "";
				$scope.containers[conIndex].tempEnvName = "";
			}
		}

		//编辑端口
		$scope.changePort = function(conIndex,index,p,type){
			if(type == 'containerPort'){
				$scope.containers[conIndex].ports[index].containerPort = p;
			}
			else{
				$scope.containers[conIndex].ports[index].protocol = p;
			}
			change.port = !angular.equals(conList[conIndex].ports,$scope.containers[conIndex].ports);
			// console.log($scope.tempPort);
		}
		$scope.deletePort = function(conIndex,index){
			$scope.containers[conIndex].ports.splice(index,1);
			change.port = !angular.equals(conList[conIndex].ports,$scope.containers[conIndex].ports);
		}
		$scope.addPort = function(conIndex){
			if(!$scope.containers[conIndex].ports){
				$scope.containers[conIndex].ports = [];
			}
			if(!!$scope.containers[conIndex].tempContainerPort && !!$scope.containers[conIndex].tempProtocol){
				var temp = {
					containerPort: $scope.containers[conIndex].tempContainerPort,
					protocol: $scope.containers[conIndex].tempProtocol
				}
				$scope.containers[conIndex].ports.push(temp);
				change.port = !angular.equals(conList[conIndex].ports,$scope.containers[conIndex].ports);
				$scope.containers[conIndex].tempContainerPort = "";
				$scope.containers[conIndex].tempProtocol = "";
			}
		}

		//修改volume
		//输入验证
		$scope.changeStorage = function(conIndex,index,s,type){
			if(type == 'name'){
				$scope.containers[conIndex].storage[index].name = s;
			}
			else{
				$scope.containers[conIndex].storage[index].mountPath = s;
			}
			change.storage = !angular.equals(conList[conIndex].storage,$scope.containers[conIndex].storage);
			// console.log($scope.tempPort);
		}
		$scope.deleteStorage = function(conIndex,index){
			$scope.containers[conIndex].storage.splice(index,1);
			change.storage = !angular.equals(conList[conIndex].storage,$scope.containers[conIndex].storage);
		}
		$scope.addStorage = function(conIndex){
			if(!$scope.containers[conIndex].storage){
				$scope.containers[conIndex].storage = [];
			}
			if(!!$scope.containers[conIndex].tempStorageName && !!$scope.containers[conIndex].tempStorageMountPath){
				var temp = {
					name: $scope.containers[conIndex].tempStorageName,
					mountPath: $scope.containers[conIndex].tempStorageMountPath,
					readOnly: false,
					type:'pv'
				}
				$scope.containers[conIndex].storage.push(temp);
				change.storage = !angular.equals(conList[conIndex].storage,$scope.containers[conIndex].storage);
				$scope.containers[conIndex].tempStorageName = "";
				$scope.containers[conIndex].tempStorageMountPath = "";
			}
		}

		$scope.expand = function(conIndex){
			if(!!$scope.containers[conIndex].expand){
				$scope.containers[conIndex].expand = false;
			}
			else{
				$scope.containers[conIndex].expand = true;
			}
		}
		$scope.disable = function(){
			if(change.resource || change.command ||
				change.args || change.env || change.port || change.storage || change.configmap)
				return false
			else
				return true
		}
		//保存容器信息，更新详情
		$scope.refresh = function(){
			var newContainers = new Array();
			var newItem = new Object();
			$scope.containers.forEach(function(item,index){
				console.log(item);
				if(typeof(item) != "undefined"){
					if(typeof(item.args) != "undefined" && item.args){
						newItem.args = item.args;
					}
					if(typeof(item.command) != "undefined" && item.command){
						newItem.command = item.command;
					}
					if(typeof(item.configmap) != "undefined" && item.configmap){
						newItem.configmap = item.configmap;
					}
					if(typeof(item.env) != "undefined" && item.env){
						newItem.env = item.env;
					}
					if(typeof(item.expand) != "undefined" && item.expand){
						newItem.expand = item.expand;
					}
					if(typeof(item.img) != "undefined" && item.img){
						newItem.img = item.img;
					}
					if(typeof(item.livenessProbe) != "undefined" && item.livenessProbe){
						newItem.livenessProbe = item.livenessProbe;
					}
					if(typeof(item.name) != "undefined" && item.name){
						newItem.name = item.name;
					}
					if(typeof(item.ports) != "undefined" && item.ports){
						newItem.ports = item.ports;
						console.log(newItem.ports);
					}
					if(typeof(item.readinessProbe) != "undefined" && item.readinessProbe){
						newItem.readinessProbe = item.readinessProbe;
					}
					if(typeof(item.resource) != "undefined" && item.resource){
						newItem.resource = item.resource;
					}
					if(typeof(item.storage) != "undefined" && item.storage){
						newItem.storage = item.storage;
					}
					newContainers.push(newItem);
				}
			});
			var sendData = {
				name: $scope.service.name,
				instance: $scope.service.instance,
				sessionAffinity : $scope.service.sessionAffinity,
				//containers: $scope.containers,
				containers: newContainers,
				namespace: $scope.service.namespace,
				annotation: $scope.service.annotation
			};
			// console.log(sendData);
			pod.editContainer(sendData).then(function(res){
				init();
				// if(res.success){
				// 	initContainer();
				// }
				// else{
				// 	initContainer();
				// }
			});		
		}

		// 五秒刷新功能
		var intervalLoad;
		var selectpod;
		var interRefresh = function(){
			serviceList.service(sendName,sendNamespace).then(function(data){
				
				if(data.status == 0 || data.status == 1){
					$scope.service = data;
					
					initEvent();
					initPodList();
					// initContainer();

					console.log($scope.service);
				}

				if(data.status==2||data.status==3){
					$scope.service = data;
					initEvent();
					initPodList();
					// initContainer();
					$timeout(function(){
						interRefresh(); 
					},5000);
				}
			});
			
		}

		$scope.$on('$destroy',function(){
			interRefresh = function(){
				
			}
		});
		
		var init = function(){
			serviceList.service(sendName,sendNamespace).then(function(data){
				$scope.service = data;
				initEvent();
				initPodList();
				initContainer();
				initPod();
			});
			interRefresh();
		}
		$scope.init = init;
		init();

		//返回上一页
		$scope.back = function(){
			// history.back(-1);
			if(!$scope.disable()){
				var d = ngDialog.open({
					template:'../template/deleteConfirm.html',
					width:300,
					closeByDocument: false,
					controller: ['$scope',function($scope){
						$scope.deleteText = $filter('translate')('giveUpConfig');
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
						history.back(-1);
					}
				});
			}
			else{
				history.back(-1);
			}
		}

		$scope.start = function(){
			var sendData = {
				name: sendName,
				namespace: sendNamespace
			};
			serviceList.start(sendData).then(function(data){
				init();
			});
		}

		$scope.stop = function(){
			var sendData = {
				name: sendName,
				namespace: sendNamespace
			};
			serviceList.stop(sendData).then(function(data){
				init();
			});
		}

		//打开控制台
		$scope.choose = function(){
			var d = ngDialog.open({
				template:'../view/choosePod.html',
				width:400,
				controller:'NewTermCtrl',
				data:{
					name:sendName,
					namespace:sendNamespace
				}
			});	
		}

		
		var initPod = function(){
			// 获取pod
			serviceList.podList(sendName,sendNamespace).then(function(data){
				// $scope.select.range='0';
				$scope.select.out = outlist[0].value;
				$scope.podList = data;
				$scope.select.podlists = data[0].name;
				$scope.logcontainers = data[0].containers;
				$scope.select.con = data[0].containers[0].name;
				$scope.select.cons = data[0].containers[0].name;
				$scope.conIndex = 0;
				
				var sendData = {
					container : $scope.select.cons,
					namespace : $scope.namespace,
					rangeType : $scope.select.range,
					scrollId : '',
					pod: $scope.select.podlists
				};
				pod.getLogOrigin(sendData).then(function(data){
					$scope.log = data;
				})

				
			});
		}
		initPod();
		$scope.rangeList = [
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
		},{
			value:'4',
			name:'thirtyDay'
		}
	];
	$scope.select = {
			out :0,
			con: '',
			range:'0',
			logdir:'',
			cons:'',
		};
	

		var cont;
		var scoid;

		$scope.$watch('select.out',function(newVal,oldVal){
			if(newVal != oldVal){
				if($scope.select.out == 0){
					var sendData = {
						container : $scope.select.cons,
						// logdir : newVal,
						namespace : $scope.namespace,
						rangeType : $scope.select.range,
						scrollId : '',
						pod: $scope.select.podlists
					};
					pod.getLogOrigin(sendData).then(function(data){
						$scope.log = data;
					})
				}
				if($scope.select.out == 1){
					var sendData = {
						container : $scope.select.con,
						namespace : $scope.namespace,
					};
					pod.getlogdir(sendData).then(function(data){
						$scope.logdirlist = data;
						$scope.select.logdir = data[0];
						// console.log(data);
						var sendDatas = {
							container : $scope.select.cons,
							logdir : $scope.select.logdir,
							namespace : $scope.namespace,
							rangeType : $scope.select.range,
							scrollId : '',
						}
						pod.getlog(sendDatas).then(function(data){
							$scope.log = data.log;
						})
					});
				}		
			}
		})
		$scope.$watch('select.podlists',function(newVal,oldVal){
			// console.log("3====9");
			if(newVal != oldVal){
				var namespace = '';

				$scope.podList.forEach(function(item){
					if(item.name == newVal)
						namespace = item.namespace;
				});
				pod.getPod(newVal,namespace).then(function(data){

					$scope.select.startTime = data.createTime;
					var now = Date.now();
					$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
					$scope.timeSpan = parseInt($scope.timeSpan/60000);
					if($scope.timeSpan < 30){
						$scope.select.time = '5';
					}
					
					$scope.logcontainers = data.containers;
					$scope.select.con = $scope.logcontainers[0].name;
					$scope.select.cons = $scope.logcontainers[0].name;

					if($scope.select.out == 0){
						var sendData = {
							container : $scope.select.cons,
							namespace : $scope.namespace,
							pod: $scope.select.podlists
						};
						pod.getLogOrigin(sendData).then(function(data){
							$scope.log = data;
							// console.log(data);
						})
					}
					if($scope.select.target == 'rxtx')
						$scope.chooseNetwork('rx','tx');
					else
						$scope.chooseType($scope.select.target);
				});
			};
		});
		$scope.$watch('select.con',function(newVal,oldVal){
			cont = newVal;
			if(newVal != oldVal){
				if($scope.select.out == 1){
				var sendData = {
					container : cont,
					namespace : $scope.namespace,
				};
				pod.getlogdir(sendData).then(function(data){
					$scope.logdirlist = data;
					$scope.select.logdir = data[0];
				});

				var sendData = {
					container : cont,
					logdir : $scope.select.logdir,
					namespace : $scope.namespace,
					rangeType : $scope.select.range,
					scrollId : '',
				};
				pod.getlog(sendData).then(function(data){
					$scope.log = data.log;
					scoid = data.ScrollId;
				})
				if($scope.log.length > 200){
					var sendData ={
						container : cont,
						logdir : $scope.select.logdir,
						namespace : $scope.namespace,
						rangeType : $scope.select.range,
						scrollId : scoid,
					};
					pod.getlog(sendData).then(function(data){
						// console.log(data);
						$scope.log = data.log;
						scoid = data.ScrollId
						if(!!$scope.log){
							fenyefunction($scope.log);
						}
					});
				}
			}
			if($scope.select.out == 0){
				var sendData = {
					container : cont,
					namespace : $scope.namespace,
					pod: $scope.select.podlists,
					rangeType : $scope.select.range,
				};
				pod.getLogOrigin(sendData).then(function(data){
					$scope.log = data;
				})
			}

				if($scope.select.target == 'rxtx')
					$scope.chooseNetwork('rx','tx');
				else
					$scope.chooseType($scope.select.target);
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
					// console.log(data.log);
					$scope.log = data.log;
					scoid = data.ScrollId;
				})
				if($scope.log.length > 200){
					var sendData ={
						container : cont,
						logdir : $scope.select.logdir,
						namespace : $scope.namespace,
						rangeType : $scope.select.range,
						scrollId : scoid,
					};
					pod.getlog(sendData).then(function(data){
						// console.log(data);
						$scope.log = data.log;
						scoid = data.ScrollId
						if(!!$scope.log){
							fenyefunction($scope.log);
						}
					});
				}
			}
		})


		// $scope.$watch('select.cons',function(newVal,oldVal,scope){
		// 	if(newVal != oldVal){
		// 		var sendData = {
		// 			container : newVal,
		// 			namespace : $scope.namespace,
		// 			pod: $scope.select.podlists,
		// 			rangeType : $scope.select.range,
		// 		};
		// 		pod.getLogOrigin(sendData).then(function(data){
		// 			$scope.log = data;
		// 		})
		// 	}
		// })
	

		$scope.$watch('select.range',function(newVal, oldVal, scope){
			
			if(newVal != oldVal){
				var now = Date.now();
				$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
				$scope.timeSpan = parseInt($scope.timeSpan/600000);
				if($scope.select.out == 1){
				var sendData = {
					container : cont,
					logdir : $scope.select.logdir,
					namespace : $scope.namespace,
					rangeType : newVal,
					scrollId : '',
				};
				pod.getlog(sendData).then(function(data){
					console.log(data);
					$scope.log = data.log;
					scoid = data.ScrollId;
					if(!!$scope.log){
						fenyefunction($scope.log);
					}
				});
				if($scope.log.length > 200){
					var sendData ={
						container : cont,
						logdir : $scope.select.logdir,
						namespace : $scope.namespace,
						rangeType : newVal,
						scrollId : scoid,
					};
					pod.getlog(sendData).then(function(data){
						// console.log(data);
						$scope.log = data.log;
						scoid = data.ScrollId
						if(!!$scope.log){
							fenyefunction($scope.log);
						}
					});
				}
			}
			if($scope.select.out == 0){
				var sendData = {
					container : cont,
					namespace : $scope.namespace,
					pod: $scope.select.podlists,
					rangeType : $scope.select.range,
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
		initPod();

		//监控部分
		$scope.select = {
			pod :'',
			container: '',
			time:'0',
			target:'cpu',
			startTime: ''
		};
		var interval;
		var empty = function(){
			$scope.nodata = true;
			angular.element('#graph')[0].innerHTML="";
		}

		// $scope.selectPod = function(p){
		// 	console.log(p);
		// 	if(p){
		// 		var namespace = '';
		// 		console.log($scope.service.podList);
		// 		$scope.service.podList.forEach(function(item){
		// 			if(item.name == p.name)
		// 				namespace = item.namespace;
		// 		});
		// 		pod.getPod(p.name,namespace).then(function(data){
		// 			$scope.select.startTime = data.createTime;
		// 			console.log(data);
		// 			var now = Date.now();
		// 			$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
		// 			$scope.timeSpan = parseInt($scope.timeSpan/60000);
		// 			if($scope.timeSpan < 30){
		// 				$scope.select.time = '5';
		// 			}
					
		// 			$scope.containers = data.containers;
		// 			$scope.select.container = $scope.containers[0].name;
		// 			$scope.selectC = $scope.containers[0].name;
		// 			if($scope.select.target == 'rxtx')
		// 				$scope.chooseNetwork('rx','tx');
		// 			else
		// 				$scope.chooseType($scope.select.target);
		// 		});
		// 	}
			
		// 	angular.element('#profile').removeClass('active');
	 //    }
  //   $scope.selectCon = function(c){
  //       $scope.selectC=c.name;
  //       if($scope.select.target == 'rxtx')
		// 	$scope.chooseNetwork('rx','tx');
		// else
		// 	$scope.chooseType($scope.select.target);
		// angular.element('#home2').removeClass('active');
		// angular.element('#podcon').removeClass('active');
		
  //   }


		$scope.$watch('select.pod',function(newVal,oldVal){
			if(newVal != oldVal){
				var namespace = '';
				// console.log($scope.service.podList);
				$scope.service.podList.forEach(function(item){
					if(item.name == newVal)
						namespace = item.namespace;
				});
				pod.getPod(newVal,namespace).then(function(data){
					$scope.select.startTime = data.createTime;
					// console.log("3===6");
					// console.log(data);
					var now = Date.now();
					$scope.timeSpan = now - (new Date($scope.select.startTime).getTime());
					$scope.timeSpan = parseInt($scope.timeSpan/60000);
					if($scope.timeSpan < 30){
						$scope.select.time = '5';
					}

					$scope.containersList = data.containers;
					$scope.select.container = $scope.containersList[0].name;
					if($scope.select.target == 'rxtx')
						$scope.chooseNetwork('rx','tx');
					else
						$scope.chooseType($scope.select.target);
				});
			};
		});
		$scope.$watch('select.container',function(newVal,oldVal){
			if(newVal != oldVal){
				if($scope.select.target == 'rxtx')
					$scope.chooseNetwork('rx','tx');
				else
					$scope.chooseType($scope.select.target);
			}
		});
		$scope.$watch('select.time',function(newVal,oldVal){
			if(newVal != oldVal){
				if($scope.select.target == 'rxtx')
					$scope.chooseNetwork('rx','tx');
				else
					$scope.chooseType($scope.select.target);
			}
		});

		$scope.chooseType = function(type){	
			$interval.cancel(interval);
			var unit;
			if(type == 'cpu'){
				unit = "%";
			}
			else if(type == 'memory'){
				unit = "MiB";
			}
			else 
				unit = "KB";
			// $scope.select.pod = 'influxdb-grafana-zny2q';
			// $scope.select.container = 'influxdb';
			$scope.select.target = type;
			monitorStore.getMonitor($scope.select.pod,$scope.select.container,$scope.select.time,$scope.select.target,$scope.select.startTime).then(function(data){
				if(!data.results[0].series){
					empty();
				}
				else{
					$scope.nodata = false;
					$scope.graph = data.results[0].series[0];
					// console.log($scope.graph);
							
					var chart = lineCharts.createChart('#graph',$scope.graph.name,unit);
					chart.batchAddData($scope.graph.values);
				}
			});

			interval = $interval(function(){
				monitorStore.getMonitor($scope.select.pod,$scope.select.container,$scope.select.time,$scope.select.target,$scope.select.startTime).then(function(data){
					// console.log(data);
					if(!data.results[0].series){
						empty();
					}
					else{
						$scope.nodata = false;
						$scope.graph = data.results[0].series[0];
						// console.log($scope.graph);
							
						var chart = lineCharts.createChart('#graph',$scope.graph.name,unit);
						chart.batchAddData($scope.graph.values);
					}
				});
			},60000);
		};

		$scope.chooseNetwork = function(rx,tx){
			$interval.cancel(interval);
			// $scope.select.pod = 'influxdb-grafana-zny2q';
			// $scope.select.container = 'influxdb';
			$scope.select.target = 'rxtx';

			$scope.graph={
				name: [],
				values: []
			};
			$scope.graph.name = ["up","down"];

			monitorStore.getNetwork($scope.select.pod,$scope.select.time,rx,$scope.select.startTime).then(function(data){
				if(!data.results[0].series){
					empty();
				}
				else{
					$scope.nodata = false;
					var network1 = data;
					monitorStore.getNetwork($scope.select.pod,$scope.select.time,tx,$scope.select.startTime).then(function(data){
						var network2 = data;
						$scope.graph.values = [network1.results[0].series[0].values,network2.results[0].series[0].values];
						var chart = multiLineCharts.createChart('#graph',$scope.graph.name,'KB/s');
						chart.batchAddData($scope.graph.values);
					});
				}
			});
			interval = $interval(function(){
				monitorStore.getNetwork($scope.select.pod,$scope.select.time,rx,$scope.select.startTime).then(function(data){
					if(!data.results[0].series){
						empty();
					}
					else{
						$scope.nodata = false;
						var network1 = data;
						monitorStore.getNetwork($scope.select.pod,$scope.select.time,tx,$scope.select.startTime).then(function(data){
							var network2 = data;
							$scope.graph.values = [network1.results[0].series[0].values,network2.results[0].series[0].values];
							var chart = multiLineCharts.createChart('#graph',$scope.graph.name,'KB/s');
							chart.batchAddData($scope.graph.values);
						});
					}
				});
			},60000);
		};
		$scope.$on('spTabChanged',function(event,data){
			if(data == 'monitor'){
				$scope.select.pod = $scope.service.podList[0].name;
				pod.getPod($scope.select.pod,$scope.namespace).then(function(data){
					$scope.containersList = data.containers;
					$scope.select.container = $scope.containersList[0].name;
					$scope.chooseType($scope.select.target,'%');
				});
			}
		});
		$scope.$on('$destroy',function(){
			$interval.cancel(interval);
		});

		//自动伸缩部分
		$scope.$watch('service.autoScaling',function(newVal,oldVal,scope){
			if(newVal != oldVal){
				if(newVal){
					$scope.spTabs = [
						{serName:'basicInformation',serTemplate:'/service/serviceInformation'},
						{serName:'logInformation',serTemplate:'/service/logInformation'},

						// {serName:'event',serTemplate:'event'},
						{serName:'monitor',serTemplate:'monitorChart'},
						{serName:'autoflex',serTemplate:'autoFlex'}
					];
				}
				else{
					$scope.spTabs.splice(3,1);
				}
			}
		});
		$scope.flexOn = function(){
			var d = ngDialog.open({
				template:'../view/chooseFlex.html',
				width:600,
				data:{
					name: $scope.service.name,
					namespace: sendNamespace
				},
				controller: 'ChooseFlexController'
			});
			d.closePromise.then(function(data){
				if(data.value == 'save'){
					serviceList.service(sendName,sendNamespace).then(function(data){
						$scope.service = data;
						// console.log($scope.service);
					});
				}
			});
		}
		$scope.deleteFlex = function(){
			var sendData = {
				deploymentName: $scope.service.name,
				namespace: sendNamespace
			};
			autoscaleStore.deleteAuto(sendData).then(function(res){
				if(res.success){
					// var inf = {
					// 	title: "关闭成功",
					// 	text: "自动伸缩关闭成功",
					// 	type: "message"
					// }
					// inform.showInform(inf);
					serviceList.service(sendName,sendNamespace).then(function(data){
						$scope.service = data;
						//相当于directive spTabs中的scope
						$scope.switchTab('basicInformation');
					});
				}
			});
		}

		$scope.ifCpuEdit = false;
		$scope.editCpu = function(){
			$scope.tempCpu = parseInt($scope.service.autoScaling.targetCpu);
			// console.log(typeof $scope.tempCpu);
			if($scope.ifCpuEdit)
				$scope.ifCpuEdit = false;
			else
				$scope.ifCpuEdit = true;
		}
		$scope.saveCpu = function(temp){
			var data={
				deploymentName: $scope.service.name,
				namespace: sendNamespace,
				max: $scope.service.autoScaling.instanceRange[1],
				min: $scope.service.autoScaling.instanceRange[0],
				cpu: temp
			};
			autoscaleStore.modifyScale(data).then(function(res){
				if(res.success){
					// var inf = {
					// 	title: "修改成功",
					// 	text: "自动伸缩#CPU占用率#修改成功",
					// 	type: "message"
					// }
					// inform.showInform(inf);

					serviceList.service(sendName,sendNamespace).then(function(data){
						$scope.service = data;
						// console.log($scope.service);
					});
				}
			});
			if($scope.ifCpuEdit)
				$scope.ifCpuEdit = false;
			else
				$scope.ifCpuEdit = true;
		}

		$scope.ifRangeEdit = false;
		$scope.editRange = function(){
			$scope.tempMin = parseInt($scope.service.autoScaling.instanceRange[0]);
			$scope.tempMax = parseInt($scope.service.autoScaling.instanceRange[1]);
			// console.log(typeof $scope.tempCpu);
			if($scope.ifRangeEdit)
				$scope.ifRangeEdit = false;
			else
				$scope.ifRangeEdit = true;
		};
		$scope.saveRange = function(min,max){
			var data={
				deploymentName: $scope.service.name,
				namespace: sendNamespace,
				max: max,
				min: min,
				cpu: $scope.service.autoScaling.targetCpu
			};
			autoscaleStore.modifyScale(data).then(function(res){
				if(res.success){
					// var inf = {
					// 	title: "修改成功",
					// 	text: "自动伸缩#当前数量范围#修改成功",
					// 	type: "message"
					// }
					// inform.showInform(inf);

					serviceList.service(sendName,sendNamespace).then(function(data){
						$scope.service = data;
						// console.log($scope.service);
					});
				}
			});
			if($scope.ifRangeEdit)
				$scope.ifRangeEdit = false;
			else
				$scope.ifRangeEdit = true;
		}

	}]);