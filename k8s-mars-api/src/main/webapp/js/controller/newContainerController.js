'use strict';

angular.module('mainApp').controller('NewContainerController',NewContainerController);
NewContainerController.$inject = ['$scope','ngDialog','containerList','storage','imgStore','inform','$filter','duplicate','pattern','$rootScope','harborStore']
	function NewContainerController($scope,ngDialog,containerList,storage,imgStore,inform,$filter,duplicate,pattern,$rootScope,harborStore){
		$scope.namespace = $rootScope.currentNamespace;
		if($scope.ngDialogData){
			$scope.imageName = $scope.ngDialogData.img.source;
			$scope.imgTag = $scope.ngDialogData.img.tags;
			$scope.imageVersion = $scope.ngDialogData.tag;
		}

		// 取到volume列表,挂载卷
		storage.getList($scope.namespace).then(function(data){
			// console.log(data);
			$scope.volume = data;
		});


		$scope.containerPattern = pattern.containerName;
		$scope.pathPattern = pattern.path;
		$scope.urlPattern = pattern.url;

		$scope.resource = {
			memory: "",
			cpu: ""
		};
		$scope.command = [];
		$scope.args = [];
		$scope.env = [];
		$scope.ports = [];
		$scope.storage = [];
		$scope.configmap = {
			path: "",
			file:"",
			value:""
		};

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

		$scope.ListDuplicate = false;
		$scope.containerDup = false;
		$scope.$watch('oneports.port',function(newVal,oldVal){
			if(oldVal != newVal){
				$scope.ListDuplicate = duplicate.listAdd($scope.ports,'port',newVal);
				$scope.containerDup = duplicate.containerListAdd(containerList.containers,'ports','port',newVal);
			}
		});
		$scope.$watch('ports.length',function(newVal,oldVal){
			if(oldVal != newVal){
				$scope.ListDuplicate = duplicate.listAdd($scope.ports,'port',newVal);
			}
		});

		// 取到镜像信息
		if(!$scope.imgTag){
			$scope.imgTag = [];
		}
		harborStore.listViaUser($scope.namespace).then(function(data){
			$scope.harborList = data;
			$scope.harborid = $scope.harborList[0].harborid;
		});
		$scope.$watch('harborid',function(newVal,oldVal){
			if(newVal != oldVal){
				console.log(newVal);
				harborStore.repoList(newVal).then(function(data){
					$scope.img = data;
					$scope.imageName = $scope.img[0].source;
					$scope.imgTag = $scope.img[0].tags;
					$scope.imageVersion = $scope.img[0].tags[0].tag;
				});
			}
		});
		$scope.$watch('imageName',function(newVal,oldVal){
			if(newVal != oldVal){
				$scope.img.forEach(function(item){
					if(item.source == newVal){
						$scope.imgTag = item.tags;
						$scope.imageVersion = item.tags[0].tag;
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
					text: $filter('translate')('inputCommand'),
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
				$scope.env[$scope.env.length] =angular.copy(oneEnv);
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
				$scope.storage[$scope.storage.length] = angular.copy(onestorage);
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

		// log config relative
		$scope.log = {
			conf: false,
			path: ""
		}

		$scope.disable = function(){
			if($scope.containerName && !$scope.ListDuplicate && !$scope.containerDup &&($scope.ports.length!=0 || ($scope.oneports.port && $scope.oneports.protocol)) && ($scope.imageName)&& ($scope.imageVersion))
				return false;
			else 
				return true;
		}

		$scope.saveContainer = function(){
			$scope.resource.cpu = $filter('cpuFormat')($scope.resource.cpu);
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
			// if($scope.onegit.path&&$scope.onegit.gitUrl){
			// 	$scope.saveStorage($scope.onegit);
			// }

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
						port : $scope.liveConfHttp.port,
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

			$scope.container = {
				name : $scope.containerName,
				img : $scope.imageName,
				tag: $scope.imageVersion,
				resource: $scope.resource,
				livenessProbe: livenessProbe,
				readinessProbe: readinessProbe,
				log: log,
				command: $scope.command,
				args: $scope.args,
				storage:$scope.storage,
				env: $scope.env,
				ports: $scope.ports,
				configmap: $scope.configmap
			}
			// console.log($scope.container);
			var mutible = false;
			containerList.containers.forEach(function(item){
				if(item.name == $scope.container.name)
					mutible = true;
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
				containerList.containers[containerList.containers.length] = $scope.container;
				console.log(containerList.containers);

				ngDialog.close(this,'next');
			}
			// alert("添加成功！")
		}
	};