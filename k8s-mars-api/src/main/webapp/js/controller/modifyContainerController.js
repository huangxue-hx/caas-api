'use strict';

angular.module('mainApp').controller('ModifyContainerController',ModifyContainerController);
ModifyContainerController.$inject = ['$scope','ngDialog','$stateParams','containerList','storage','inform','$filter','duplicate','pattern','$rootScope','harborStore']
	function ModifyContainerController($scope,ngDialog,$stateParams,containerList,storage,inform,$filter,duplicate,pattern,$rootScope,harborStore){
		// 取到volume列表,挂载卷
		storage.getList($scope.namespace).then(function(data){
			$scope.volume = data;
		});

		$scope.containerPattern = pattern.containerName;
		$scope.pathPattern = pattern.path;
		$scope.urlPattern = pattern.url;

		//取到当前点击的container
		var container = angular.copy($scope.ngDialogData.container);
		$scope.container = container;

		var deleteIndex = $scope.ngDialogData.index;

		// 定义页面中获取到的container配置项
		var img = $scope.container.img;
		var version = $scope.container.tag;
		$scope.imgDetail = [img,version];

		$scope.resource = angular.copy($scope.container.resource);
		$scope.resource.cpu = parseInt($scope.resource.cpu);
		$scope.resource.memory = parseInt($scope.resource.memory);
		$scope.liveness = angular.copy($scope.container.livenessProbe);
		$scope.readiness = angular.copy($scope.container.readinessProbe);
		$scope.command = angular.copy($scope.container.command);
		$scope.args = angular.copy($scope.container.args);
		$scope.logText = $scope.container.log;
		$scope.env = angular.copy($scope.container.env);
		$scope.ports = angular.copy($scope.container.ports);
		$scope.storage = angular.copy($scope.container.storage);
		$scope.configmap = angular.copy($scope.container.configmap);
		
		$scope.oneports = {
			port: "",
			protocol: "TCP"
		};
		$scope.onecommand = "";
		$scope.oneargs = "";
		$scope.oneEnv = {
			key: "",
			value: ""
		};
		$scope.onestorage = {
			path : "",
			volume : "",
			readOnly: false,
			type: "pv"
		};
		$scope.onegit = {
			path : "",
			gitUrl : "",
			revision : "",
			readOnly: false,
			type: "gitRepo"
		};

		//probe relative config
		$scope.livenessProbe = {
			name: "httpGet",
			initialDelaySeconds: null,
			timeoutSeconds: null,
			periodSeconds: null,
			successThreshold: null,
			failureThreshold: null
		}
		$scope.liveConfHttp = {
			path: "",
			port: ""
		}
		$scope.liveConfTcp = {
			port: ""
		}

		$scope.readinessProbe = {
			name: "httpGet",
			initialDelaySeconds: null,
			timeoutSeconds: null,
			periodSeconds: null,
			successThreshold: null,
			failureThreshold: null
		}
		$scope.readConfHttp = {
			path: "",
			port: ""
		}
		$scope.readConfTcp = {
			port: ""
		}
		if($scope.liveness.httpGet){
			$scope.livenessProbe.name = "httpGet";
			$scope.livenessProbe.initialDelaySeconds = $scope.liveness.initialDelaySeconds;
			$scope.livenessProbe.timeoutSeconds = $scope.liveness.timeoutSeconds;
			$scope.livenessProbe.periodSeconds = $scope.liveness.periodSeconds;
			$scope.livenessProbe.successThreshold = $scope.liveness.successThreshold;
			$scope.livenessProbe.failureThreshold = $scope.liveness.failureThreshold;

			$scope.liveConfHttp.path = $scope.liveness.httpGet.path;
			$scope.liveConfHttp.port = $scope.liveness.httpGet.port;
		}
		else if($scope.liveness.tcpSocket){
			$scope.livenessProbe.name = "tcpSocket";
			$scope.livenessProbe.initialDelaySeconds = $scope.liveness.initialDelaySeconds;
			$scope.livenessProbe.timeoutSeconds = $scope.liveness.timeoutSeconds;
			$scope.livenessProbe.periodSeconds = $scope.liveness.periodSeconds;
			$scope.livenessProbe.successThreshold = $scope.liveness.successThreshold;
			$scope.livenessProbe.failureThreshold = $scope.liveness.failureThreshold;

			$scope.liveConfTcp.port = $scope.liveness.tcpSocket.port;
		}

		if($scope.readiness.httpGet){
			$scope.readinessProbe.name = "httpGet";
			$scope.readinessProbe.initialDelaySeconds = $scope.readiness.initialDelaySeconds;
			$scope.readinessProbe.timeoutSeconds = $scope.readiness.timeoutSeconds;
			$scope.readinessProbe.periodSeconds = $scope.readiness.periodSeconds;
			$scope.readinessProbe.successThreshold = $scope.readiness.successThreshold;
			$scope.readinessProbe.failureThreshold = $scope.readiness.failureThreshold;

			$scope.readConfHttp.path = $scope.readiness.httpGet.path;
			$scope.readConfHttp.port = $scope.readiness.httpGet.port;
		}
		else if($scope.readiness.tcpSocket){
			$scope.readinessProbe.name = "tcpSocket";
			$scope.readinessProbe.initialDelaySeconds = $scope.readiness.initialDelaySeconds;
			$scope.readinessProbe.timeoutSeconds = $scope.readiness.timeoutSeconds;
			$scope.readinessProbe.periodSeconds = $scope.readiness.periodSeconds;
			$scope.readinessProbe.successThreshold = $scope.readiness.successThreshold;
			$scope.readinessProbe.failureThreshold = $scope.readiness.failureThreshold;

			$scope.readConfTcp.port = $scope.readiness.tcpSocket.port;
		}

		// log config relative
		$scope.log = {
			path: ""
		}
		if($scope.logText){
			$scope.log.path = $scope.logText;
		}

		$scope.ListDuplicate = false;
		$scope.containerDup = false;
		$scope.$watch('oneports.port',function(newVal,oldVal){
			if(oldVal != newVal){
				$scope.ListDuplicate = duplicate.listAdd($scope.ports,'port',newVal);
				$scope.containerDup = duplicate.containerListModify(containerList.containers,'ports','port',deleteIndex,newVal);
			}
		});
		$scope.$watch('ports.length',function(newVal,oldVal){
			if(oldVal != newVal){
				$scope.ListDuplicate = duplicate.listAdd($scope.ports,'port',newVal);
			}
		});

		// 取到镜像信息img
		var count = 0;
		$scope.namespace = $rootScope.currentNamespace;
		$scope.imgTag = [];
		harborStore.listViaUser($scope.namespace).then(function(data){
			$scope.harborList = data;
			var harbor = img.split('/')[1];
			$scope.harborList.forEach(function(item){
				if(harbor == item.name){
					$scope.harborid = item.harborid;
				}
			});
		});
		$scope.$watch('harborid',function(newVal,oldVal){
			if(newVal != oldVal){
				count++;
				harborStore.repoList(newVal).then(function(data){
					$scope.img = data;
					if(count>1){
						$scope.imgDetail[0] = $scope.img[0].source;
					};
					$scope.img.forEach(function(item){
						if(item.source == $scope.imgDetail[0]){
							$scope.imgTag = item.tags;
							console.log($scope.imgTag);
						}
					});	
				});
			}
		});
		$scope.$watch('imgDetail[0]',function(newVal,oldVal){
			if(newVal != oldVal){
				$scope.img.forEach(function(item){
					if(item.source == newVal){
						$scope.imgTag = item.tags;
						console.log($scope.imgTag);
						if(count>1){
							$scope.imgDetail[1] = item.tags[0].tag
						}
					}
				});
			}
		});

		//expand related
		$scope.expand = false;
		$scope.expandAction = function(){
			$scope.expand = !$scope.expand;
		}

		$scope.saveCommand = function(onecommand){
			if(!onecommand){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('inputCommand'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else{
				// console.log($scope.command.length);
				$scope.command[$scope.command.length] = onecommand;
				$scope.onecommand = "";
			}
		}
		$scope.deleteCommand = function(index){
			$scope.command.splice(index,1);
		}

		$scope.saveArgs = function(oneargs){
			if(!oneargs){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('inputArgs'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else{
				$scope.args[$scope.args.length] = oneargs;
				$scope.oneargs= "";
			}
		}
		$scope.deleteArgs = function(index){
			$scope.args.splice(index,1);
		}

		$scope.saveEnv = function(oneEnv){
			if(!oneEnv||!oneEnv.key||!oneEnv.value){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('inputEnv'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else{
				$scope.env[$scope.env.length] = angular.copy(oneEnv);
				$scope.oneEnv = "";
			}
		}
		$scope.deleteEnv = function(index){
			// console.log(index);
			$scope.env.splice(index,1);
		}

		$scope.savePorts = function(oneports){
			if(!oneports||(!oneports.port)||(!oneports.protocol)){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('fillInPort'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else if($scope.ListDuplicate){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('portDuplicatePort'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else if($scope.containerDup){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('portDuplicateContainer'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else{
				oneports.expose = true;
				$scope.ports[$scope.ports.length] = angular.copy(oneports);
				$scope.oneports = {};
				$scope.oneports.protocol = "TCP";
			}
		}
		$scope.deletePorts = function(index){
			// console.log(index);
			$scope.ports.splice(index,1);
		}

		$scope.saveStorage = function(onestorage){
			var double = false;
			$scope.storage.forEach(function(item,index){
				if(item.path == onestorage.path)
					double = true;
			});
			if(!onestorage||(onestorage.type=='pv'&&(!onestorage.path||!onestorage.volume))){
				// var inf = {
				// 	title: $filter('translate')('inputError'),
				// 	text: $filter('translate')('fillInVolume'),
				// 	type: "error"
				// }
				// inform.showInform(inf);
			}
			else if(onestorage.type=='gitRepo'&&(!onestorage.path||!onestorage.gitUrl)){
				// var inf = {
				// 	title: $filter('translate')('inputError'),
				// 	text: $filter('translate')('fillInVolume'),
				// 	type: "error"
				// }
				// inform.showInform(inf);
			}
			else if(double){
				var inf = {
					title: $filter('translate')('inputError'),
					text: $filter('translate')('pathDuplicate'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else{
				$scope.storage[$scope.storage.length]=(angular.copy(onestorage));
				if(onestorage.type == 'pv'){
					$scope.onestorage = {
						path : "",
						volume : "",
						readOnly: false,
						type: "pv"
					};
				}
				else{
					$scope.onegit = {
					path : "",
					gitUrl : "",
					revision : "",
					readOnly: false,
					type: "gitRepo"
					};
				}
			}
		}
		$scope.deleteStorage = function(index){
			// console.log(index);
			$scope.storage.splice(index,1);
		}

		$scope.deleteContainer = function(){
			containerList.containers.splice(deleteIndex,1);
			console.log(containerList.containers);

			ngDialog.close(this);
			// alert("删除成功！")
		}

		$scope.disable = function(){
			if(($scope.container.name)&&($scope.resource.memory)&&($scope.resource.cpu) && !$scope.ListDuplicate && !$scope.containerDup&& ($scope.ports.length!=0 || ($scope.oneports.port && $scope.oneports.protocol)) 
				&& ($scope.imgDetail[0])
				&& ($scope.imgDetail[1]))
				return false;
			else 
				return true;
		}
		
		$scope.save = function(){
			// console.log($scope.container)
			// console.log(containerList.containers[deleteIndex]);
			var mutible = false;
			// console.log(mutible);
			containerList.containers.forEach(function(item,index){
				if(index != deleteIndex){
					if(item.name == $scope.container.name)
						mutible = true;
				}
			})
			if(mutible == true){
				var inf = {
					title: $filter('translate')('nameDuplicate'),
					text: $filter('translate')('conatinerNameDuplicate'),
					type: "error"
				}
				inform.showInform(inf);
			}
			else{
				if($scope.oneports.port && $scope.oneports.protocol){
				$scope.savePorts($scope.oneports);
				}
				if($scope.onecommand){
					$scope.saveCommand($scope.onecommand);
				}
				if($scope.oneargs){
					$scope.saveArgs($scope.oneargs);
				}
				if($scope.oneEnv.key && $scope.oneEnv.value){
					$scope.saveEnv($scope.oneEnv);
				}
				if($scope.onestorage.path&&$scope.onestorage.volume){
					$scope.saveStorage($scope.onestorage);
				}
				if($scope.onegit.path&&$scope.onegit.gitUrl){
					$scope.saveStorage($scope.onegit);
				}
				
				// var newStorage = [];
				// if(!$scope.checkbox){
				// 	$scope.storage.forEach(function(item,index){
				// 		if(item.type != 'pv'){
				// 			newStorage.push(angular.copy(item));
				// 		}
				// 	})
				// 	$scope.storage = angular.copy(newStorage);
				// }
				// newStorage = [];
				// if(!$scope.gitbox){
				// 	$scope.storage.forEach(function(item,index){
				// 		if(item.type != 'gitRepo')
				// 			newStorage.push(angular.copy(item));
				// 	});
				// 	$scope.storage = angular.copy(newStorage);
				// }

				var livenessProbe = {};
				if($scope.livenessProbe.name == 'httpGet' && $scope.liveConfHttp.port && $scope.liveConfHttp.path){
					livenessProbe = {
						httpGet : {
							path : $scope.liveConfHttp.path,
							port : $scope.liveConfHttp.port
						},
						initialDelaySeconds: $scope.livenessProbe.initialDelaySeconds,
						timeoutSeconds: $scope.livenessProbe.timeoutSeconds,
						periodSeconds: $scope.livenessProbe.periodSeconds,
						successThreshold: $scope.livenessProbe.successThreshold,
						failureThreshold: $scope.livenessProbe.failureThreshold
					}
				}
				else if($scope.livenessProbe.name == 'tcpSocket' && $scope.liveConfTcp.port){
					livenessProbe = {
						tcpSocket : {
							port : $scope.liveConfTcp.port
						},
						initialDelaySeconds: $scope.livenessProbe.initialDelaySeconds,
						timeoutSeconds: $scope.livenessProbe.timeoutSeconds,
						periodSeconds: $scope.livenessProbe.periodSeconds,
						successThreshold: $scope.livenessProbe.successThreshold,
						failureThreshold: $scope.livenessProbe.failureThreshold
					}
				}

				var readinessProbe = {};
				if($scope.readinessProbe.name == 'httpGet' && $scope.readConfHttp.port && $scope.readConfHttp.path){
					readinessProbe = {
						httpGet : {
							path : $scope.readConfHttp.path,
							port : $scope.readConfHttp.port
						},
						initialDelaySeconds: $scope.readinessProbe.initialDelaySeconds,
						timeoutSeconds: $scope.readinessProbe.timeoutSeconds,
						periodSeconds: $scope.readinessProbe.periodSeconds,
						successThreshold: $scope.readinessProbe.successThreshold,
						failureThreshold: $scope.readinessProbe.failureThreshold
					}
				}
				else if($scope.readinessProbe.name == 'tcpSocket' && $scope.readConfTcp.port){
					readinessProbe = {
						tcpSocket : {
							port : $scope.readConfTcp.port
						},
						initialDelaySeconds: $scope.readinessProbe.initialDelaySeconds,
						timeoutSeconds: $scope.readinessProbe.timeoutSeconds,
						periodSeconds: $scope.readinessProbe.periodSeconds,
						successThreshold: $scope.readinessProbe.successThreshold,
						failureThreshold: $scope.readinessProbe.failureThreshold
					}
				}
				

				var log="";
				if($scope.log.path){
					log = $scope.log.path;
				}

				//if config message not enough,set it to void
				if(!!$scope.configmap.path && !!$scope.configmap.file){

				}
				else{
					$scope.configmap.path="";
					$scope.configmap.file="";
					$scope.configmap.value="";
				}

				// console.log($scope.storage);
				$scope.container.command = angular.copy($scope.command);
				$scope.container.args = angular.copy($scope.args);
				$scope.container.resource = angular.copy($scope.resource);
				$scope.container.resource.cpu = $filter('cpuFormat')($scope.container.resource.cpu);
				$scope.container.img = $scope.imgDetail[0],
				$scope.container.tag = $scope.imgDetail[1];
				$scope.container.livenessProbe = angular.copy(livenessProbe);
				$scope.container.readinessProbe = angular.copy(readinessProbe);
				$scope.container.log = log;
				$scope.container.env = angular.copy($scope.env);
				$scope.container.ports = angular.copy($scope.ports);
				$scope.container.storage = angular.copy($scope.storage);
				$scope.container.configmap = angular.copy($scope.configmap);

				containerList.containers[deleteIndex] = angular.copy($scope.container);

				console.log(containerList.containers);

				ngDialog.close(this);
				
			}
		}
	};