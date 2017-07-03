'use strict';

angular.module('mainApp').controller('TenantDetailController',TenantDetailController);
TenantDetailController.$inject=['$scope','tenant','$stateParams','namespace','ngDialog','harborStore','binding','$rootScope','volume','network','inform','$filter','$timeout','serviceList']
function TenantDetailController($scope,tenant,$stateParams,namespace,ngDialog,harborStore,binding,$rootScope,volume,network,inform,$filter,$timeout,serviceList){
	$scope.tenantid = $stateParams.id;
	var pre = true;
	if(!$stateParams.id){
		$scope.tenantid = $rootScope.tenantid;
		pre = false;
	}

	$rootScope.$on('tenantCheckChange',function(event,data){
		if(!pre){
			$scope.tenantid = data.tenantid;
			// console.log(data);
			init();
		}
	})

	$scope.back = function(){
		history.back(-1);
	}

	//user for controller the title of html
	$scope.parent = true;
	$scope.spTabs = [
		{serName:'tenantInformation',serTemplate:'/tenant/tenantInformation'},
		{serName:'tenantResource',serTemplate:'/tenant/tenantResource'},
	];
	$scope.tenantTabs = [
		{serName:'tenantInformation',serTemplate:'/tenant/tenantInformation'},
		{serName:'tenantResource',serTemplate:'/tenant/tenantResource'},
	];

	//network related
	var initNetwork = function(){

		// get network list via tenantid
		network.list($scope.tenantid).then(function(data){
			$scope.networkList = data;
		});
	}
	$scope.deleteNetwork = function(n){
		var sendData = {
			networkid: n.networkid,
			tenantid: $scope.tenantid
		}
		//send delete request
		network.deleteNetwork(sendData).then(function(data){
			if(data.success){
				initNetwork();
			}
			else{
				var inf = {
					title: 'Delete Error',
					text: data.errMsg,
					type: 'error'
				}
				inform.showInform(inf);
			}
		})
	}
	$scope.newNetwork = function(){
		var d = ngDialog.open({
			template:'../../view/network/networkNew.html',
			width:700,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid
			},
			appendClassName:'hc-dialog',
			controller: 'NetworkNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initNetwork();
			}
		});	
	}

	// project related same with tenantProjectController,control the same html file
	$scope.circleStyle = {
		background: "#fff",
		color:""
	};
	var dataByService;
	var dataByMember;
	var initProject = function(cb){
		dataByService = [];
		dataByMember = [];
		namespace.list($scope.tenantid,$scope.tenant.name).then(function(data){
			$scope.projectList = data;
			if($scope.projectList.length>0){
				$scope.expProject($scope.projectList.length-1);
			}
			cb&&cb.apply();

			$scope.projectList.forEach(function(item){
				var temps = {
					name: $filter('harborName')(item.name),
					value: item.serviceNumber
				}
				dataByService.push(temps);
				var tempm = {
					name: $filter('harborName')(item.name),
					value: item.memberNumber
				}
				dataByMember.push(tempm);
			});
			$scope.circleData = {
				title:'service',
				data: dataByService
			}
		});
	}
	var initChart = function(){
		dataByService = [];
		dataByMember = [];
		namespace.list($scope.tenantid,$scope.tenant.name).then(function(data){
			data.forEach(function(item){
				var temps = {
					name: $filter('harborName')(item.name),
					value: item.serviceNumber
				}
				dataByService.push(temps);
				var tempm = {
					name: $filter('harborName')(item.name),
					value: item.memberNumber
				}
				dataByMember.push(tempm);
			});
			$scope.circleData = {
				title:'service',
				data: dataByService
			}
		});
	}
	var initProjectUser = function(index){
		namespace.getUser($scope.tenant.name,$scope.projectList[index].name).then(function(data){
			var tempUser = [{
				name: 'pm',
				member: [],
				fold: true
			},{
				name: 'dev',
				member: [],
				fold: true
			},{
				name: 'tester',
				member: [],
				fold: true
			}]
			data.forEach(function(item){
				if(item.role == 'dev'){
					tempUser[1].member.push(item.name);
				}
				else if(item.role == 'pm'){
					tempUser[0].member.push(item.name);
				}
				else if(item.role == 'tester'){
					tempUser[2].member.push(item.name);
				}
			});
			// console.log(tempUser);
			$scope.projectList[index].detail.userList = tempUser;
		});
	}
	var initHarborUser = function(index){
		harborStore.userList($scope.tenant.name,$scope.projectList[index].name).then(function(data){
			$scope.projectList[index].detail.harborUserList = data;
		});
	}
	var initProjectDetail = function(name,index){
		namespace.detail($scope.tenantid,name).then(function(data){
			$scope.projectList[index].detail = data;
			$timeout(function() {
				for(var q in $scope.projectList[index].detail.quota){
					$scope.projectList[index].detail[q+'Usage'] = {
						width: parseInt($scope.projectList[index].detail.quota[q][1])/parseInt($scope.projectList[index].detail.quota[q][0])*250
					}
				}
			}, 3);
			//initial service list
			serviceList.getList($scope.projectList[index].name).then(function(data){
				$scope.projectList[index].detail.serviceList = data;
			});
			//initial member
			initProjectUser(index);
			//initial harbor member
			initHarborUser(index);
		});
	}
	$scope.expandMember = function(pindex,index){
		$scope.projectList[pindex].detail.userList[index].fold = !$scope.projectList[pindex].detail.userList[index].fold;
	}
	$scope.addProUser = function(index){
		var d = ngDialog.open({
			template:'../../view/project/projectAddUser.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantname: $scope.tenant.name,
				tenantid: $scope.tenantid,
				projectname: $scope.projectList[index].name
			},
			appendClassName:'hc-dialog',
			controller:'ProjectAddUserController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initProjectUser(index);
				initChart();
			}
		});
	}
	$scope.deleteProUser = function(pindex,uindex,index){
		var sendData = {
			tenantname: $scope.tenant.name,
			namespace: $scope.projectList[pindex].name,
			tenantid: $scope.tenantid,
			role: $scope.projectList[pindex].detail.userList[uindex].name,
			user: $scope.projectList[pindex].detail.userList[uindex].member[index]
		}
		binding.deleteBindUser(sendData).then(function(data){
			if(data.success){
				initProjectUser(pindex);
				initChart();
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg.message,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}
	$scope.addHarborUser = function(index){
		var d = ngDialog.open({
			template:'../../view/harbor/harborAddUser.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantname: $scope.tenant.name,
				tenantid: $scope.tenantid,
				namespace: $scope.projectList[index].name
			},
			appendClassName:'hc-dialog',
			controller:'HarborAddUserController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initHarborUser(index);
			}
		});
	}
	$scope.deleteHarborUser = function(pindex,index){
		var sendData = {
			tenantname: $scope.tenant.name,
			tenantid: $scope.tenantid,
			namespace: $scope.projectList[pindex].name,
			user:{
				id: $scope.projectList[pindex].detail.harborUserList[index].userId[0],
				name: $scope.projectList[pindex].detail.harborUserList[index].user[0],
			},
			projects:[{
				projectId: $scope.projectList[pindex].detail.harborUserList[index].harborid,
				role: $scope.projectList[pindex].detail.harborUserList[index].role,
			}],
		}
		harborStore.deleteRole(sendData).then(function(data){
			if(data.success){
				initHarborUser(pindex);
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg.message,
					type:'error'
				}
				inform.showInform(inf);
			}
		})
	}
	$scope.chooseCircle = function(item){
		if(item == 'member'){
			$scope.circleData = {
				title:'member',
				data: dataByMember
			}
		}
		else{
			$scope.circleData = {
				title:'service',
				data: dataByService
			}
		}
	}

	$scope.mouse = false;
	$scope.circleMouseOut = function(data){
		$scope.mouse = false;
		// console.log($scope);
	}
	$scope.circleMouseOver = function(data){
		$scope.mouse = true;
		changeNowPro(data.name);
		// $scope.tempName = name;
	}
	$scope.deleteProject = function(p){
		var sendData = {
			tenantid: $scope.tenantid,
			name: p.name
		}
		namespace.deleteProject(sendData).then(function(data){
			if(data.success){
				initProject();
			}
			else{
				var inf = {
					title: 'Delete Error',
					text: data.errMsg,
					type: 'error'
				}
				inform.showInform(inf);
			}
		})
	}
	var changeNowPro = function(name){
		$scope.projectList.forEach(function(item){
			var tempName = $filter('harborName')(item.name);
			if(name == tempName){
				console.log(item);
				$scope.nowProject = item;
			}
		});
	}
	$scope.expProject = function(index){
		//no influence for last item,just for other item 
		if(index < $scope.projectList.length-1){
			//if it has expanded,fold all item
			if($scope.projectList[index].exp){
				$scope.projectList.forEach(function(item){
					item.exp = false;
				});
			}
			//if it fold,fold all and expand this
			else{
				$scope.projectList.forEach(function(item){
					item.exp = false;
				});
				$scope.projectList[index].exp = true;
			}
		}
		if(!$scope.projectList[index].detail){
			initProjectDetail($scope.projectList[index].name,index);
		}
	}

	$scope.newProject = function(){
		var d = ngDialog.open({
			template:'../../view/project/projectNew.html',
			width:800,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: $scope.tenant.name
			},
			appendClassName:'hc-dialog',
			controller: 'ProjectNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initProject();
			}
		});	
	}

	$scope.changeProject = function(p,index){
		var d = ngDialog.open({
			template:'../../view/project/projectChange.html',
			width:800,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: $scope.tenant.name,
				project:p.name
			},
			appendClassName:'hc-dialog',
			controller: 'ProjectChangeController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initProject(function(){
					$scope.expProject(index);
				});
			}
		});	
	}

	//harbor realted same with tenantHarborController,control the same html file
	var initHarbor = function(){
		harborStore.list($scope.tenantid).then(function(data){
			$scope.harborList = data;
		})
	}

	$scope.deleteHarbor = function(h){
		var sendData = {
			tenantid: $scope.tenantid,
			tenantname: $scope.tenant.name,
			// tenantid: '037b2163ae5b4287b773c70b53f7f758',
			projectid: h.harborid
		}
		harborStore.deleteHarbor(sendData).then(function(data){
			if(data.success){
				initHarbor();
			}
			else{
				var inf = {
					title: 'Delete Error',
					text: data.errMsg,
					type: 'error'
				}
				inform.showInform(inf);
			}
		})
	}

	$scope.newHarbor = function(){
		var d = ngDialog.open({
			template:'../../view/harbor/harborNew.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: $scope.tenant.name
			},
			appendClassName:'hc-dialog',
			controller: 'HarborNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initHarbor();
			}
		});	
	}

	// volume tab related
	var initVolume = function(){
		// get network list via tenantid
		volume.list($scope.tenantid).then(function(data){
			$scope.volumeList = data;
		});
	}

	$scope.deleteVolume = function(v){
		var sendData = {
			tenantid: $scope.tenantid,
			name: v.name
		}
		volume.deleteVolume(sendData).then(function(data){
			if(data.success){
				init();
			}
		})
	}

	$scope.newVolume = function(){
		var d = ngDialog.open({
			template:'../../view/volume/volumeNew.html',
			width:800,
			closeByDocument: false,
			data:{
				tenantid:$scope.tenantid,
				tenantname: $scope.tenant.name
			},
			appendClassName:'hc-dialog',
			controller: 'VolumeNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	// $scope.changeVolume = function(index){
	// 	var d = ngDialog.open({
	// 		template:'../../view/volume/volumeChange.html',
	// 		width:650,
	// 		closeByDocument: false,
	// 		data:{
	// 			tenantid:$scope.tenantid,
	// 			tenantname: $scope.tenant.name,
	// 			volume:$scope.volumeList[index].name
	// 		},
	// 		controller: 'VolumeChangeController'
	// 	});
	// 	d.closePromise.then(function(data){
	// 		if(data.value == 'done'){
	// 			init();
	// 		}
	// 	});	
	// }

	//member tab related
	var initUser = function(){
		tenant.tenantUser($scope.tenant.name).then(function(data){
			$scope.userList = data;
		})
	}
	$scope.addUser = function(){
		var d = ngDialog.open({
			template:'../../view/tenant/tenantAddUser.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantname: $scope.tenant.name,
				tenantid:$scope.tenantid,
			},
			appendClassName:'hc-dialog',
			controller:'TenantAddUserController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				initUser();
			}
		});
	}
	$scope.addTm = function(){
		var d = ngDialog.open({
			template:'../../view/tenant/tenantAddUser.html',
			width:650,
			closeByDocument: false,
			data:{
				tenantname: $scope.tenant.name,
				tenantid:$scope.tenantid,
				tm:true
			},
			appendClassName:'hc-dialog',
			controller:'TenantAddUserController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				tenant.detail($scope.tenantid).then(function(data){
					$scope.tenant = data[0];
					initUser();
				});
			}
		});
	}
	$scope.deleteUser = function(u){
		// console.log($scope.userList.rb[index]);
		var sendData= {
			tenantname: $scope.tenant.name,
			tenantid: $scope.tenantid,
			namespace: u.namespace,
			role: u.role,
			user: u.name
		}
		binding.deleteBindUser(sendData).then(function(data){
			if(data.success){
				initUser();
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});
	}
	$scope.deleteTm = function(username){
		var sendData= {
			tenantid: $scope.tenantid,
			user: username
		}
		tenant.deleteTm(sendData).then(function(data){
			if(data.success){
				tenant.detail($scope.tenantid).then(function(data){
					$scope.tenant = data[0];
					initUser();
				});
			}
			else{
				var inf={
					title: 'Delete Error',
					text:data.errMsg,
					type:'error'
				}
				inform.showInform(inf);
			}
		});
	}


	var init = function(){
		tenant.detail($scope.tenantid).then(function(data){
			$scope.tenant = data[0];
			initUser();
			initProject();
		});
		initNetwork();
		initHarbor();
		initVolume();
	}

	init(); 
}