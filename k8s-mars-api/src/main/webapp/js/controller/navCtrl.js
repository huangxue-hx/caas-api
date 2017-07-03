'use strict'

angular.module('mainApp')
.controller('NavCtrl',['$scope','comm','menu','$rootScope','inform','notification','eventStore','ngDialog','login','authStore','$state','$translate','namespace','$timeout',function($scope,comm,menu,$rootScope,inform,notification,eventStore,ngDialog,login,authStore,$state,$translate,namespace,$timeout){
	var vm = $scope;
	vm.warning = false;
	var communicator;
	var list = [];
	var id = '';
	var namelist = [];
	vm.currentTenant = {};
	vm.currentNamespace = '';
	vm.notiCount = 0;
	var notiDelay = null;

	vm.openNoti = function(){
		notification.show();
	};

	var init = function(){
		
		authStore.getUser().then(function(d){
			vm.userid = d.userId;
			vm.username = d.username;
			vm.tenantlist = d.tenants;
			console.log(d);
			if(vm.tenantlist.length>0){
				vm.currentTenant = vm.tenantlist[0];
			}
			communicator = comm.create(vm.username);
			communicator.listen('noti',function(d){
				var info = {
					title: d.target.name +' '+d.title,
					text: d.message,
					type: d.type=='Warning'?'error':'message',
					target: d.target
				};
				// inform.showInform(info);
				notification.add(info);
			});
		},function(e){
			if(e ==401){
				login.login();
			}
		});
	};
	notification.onAmountChange(function(amount,hasWarning){
		vm.notiCount = amount;
		vm.hasWarning = hasWarning;
		vm.showNumber = true;
		$timeout.cancel(notiDelay);
		notiDelay = $timeout(function(){
			vm.showNumber = false;
		},5000);
	});



	var loadNs = function(tid){
		namespace.getNsByUser(tid).then(function(data){
			if(!!data){
				vm.namespacelist = data[0].k8sNamespaces;
				vm.currentNamespace = data[0].k8sNamespaces[0];
				loadSecret(vm.currentNamespace);
			}
		});
	};
	var loadSecret = function(ns){
		authStore.checkSecret(ns);
	};

	vm.$watch('currentTenant',function(n,o){
		if(!!n.name){

			vm.namespacelist = vm.currentTenant.namespaces;
			$rootScope.tenantname = vm.currentTenant.name;
			console.log(vm.currentTenant.name);
			$rootScope.tenantid = vm.currentTenant.tenantid;

			$rootScope.userid = vm.userid;
			loadNs($rootScope.tenantid);
			$rootScope.$broadcast('tenantCheckChange',{namespace:vm.currentNamespace,tenantname:$rootScope.tenantname,tenantid:$rootScope.tenantid,userid:vm.userid});
		}
	});
	vm.$watch('currentNamespace',function(n,o){
		
		if(!!n){

			$rootScope.currentNamespace = n;
			$rootScope.tenantname = vm.currentTenant.name;
			$rootScope.tenantid = vm.currentTenant.tenantid;
			$rootScope.userid = vm.userid;
			loadSecret(n);
			$rootScope.$broadcast('namespaceCheckChange',{namespace:vm.currentNamespace,tenantname:$rootScope.tenantname,tenantid:$rootScope.tenantid,userid:vm.userid});
			eventStore.query({type:'Warning',namespace: vm.currentNamespace}).then(function(data){
				vm.warning = data.length;
			});

			
		}
	});

	vm.openWarning = function(){
		var d = ngDialog.open({
				template:'../view/warninglist.html',
				width:800,
				controller:'WarningCtrl'
			});	
	};
	vm.setting = function(){
		var d = ngDialog.open({
				template:'../view/setting.html',
				width:800,
				controller:'SettingCtrl'
			});	
	};
	vm.modifypassword = function(){
		var d = ngDialog.open({
				template:'../view/modifypassword.html',
				width:500,
				controller:'ModifypasswordCtrl'
			});	
	};
	vm.translate = function(){
		if($translate.use()=='ch'){
			$translate.use('en');
		}
		else
			$translate.use('ch');
	}
	vm.logout = function(){
		authStore.logout().then(function(d){
			login.login();
			$state.reload();
			communicator.disconnect();

		},function(e){
			alert(e);
		});

	};
	vm.checkTenant = function(item){
		vm.currentTenant = item;
	}
	vm.checkName = function(item){
		vm.currentNamespace = item;
	}
	vm.$on('userLogin',function(){
		init();
	});
	vm.$on('userLogout',function(){
		vm.logout();
	});
	init();
}]);