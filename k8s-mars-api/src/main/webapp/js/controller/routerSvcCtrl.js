'use strict'

angular.module('mainApp')
.controller('RouterSvcController',['$scope','routerStore','ngDialog','baseUrl','entryStore',function($scope,routerStore,ngDialog,baseUrl,entryStore){
	var init = function(){
		if(!vm.currentNamespace){
			return;
		}
		routerStore.querySvc({namespace:vm.currentNamespace}).then(function(data){
			$scope.svc = data;
		});
		entryStore.query().then(function(data){
			vm.entry = data;
			vm.displayEntry = data ;
		});
	};
	var vm = $scope;
	vm.selectAll = false;
	vm.$watch('currentNamespace',function(n,o){
			if(n){
				init();
			}
		});
	var expandNow = null;
	vm.protocolList = ['TCP','UDP'];
	var ingDirtyCheck = function(ing){
		for(var i =0,l= ing.rules.length;i<l;i++){
			if(ing.rules[i].isDirty){
				ing.isDirty = true;
				break;
			}
			else{
				ing.isDirty = false;
			}
		}
	};
	vm.toggle = function(d){
		d.shrink = !d.shrink;
	};
	vm.toggle2 = function(d){
		if(d.expand){
			expandNow = null;
		}
		else{
			if(expandNow){
				expandNow.expand = false;
			}
			expandNow = d;
		}
		d.expand = !d.expand;
	};
	vm.newIng = function(){
		ngDialog.open({
			template: baseUrl.static + 'view/newIng.html',
			controller:'NewIngCtrl',
			width:600,
			closeByDocument:false
		}).closePromise.then(function(val){
			if(val.value == 'true'){
				init();
			}
		});

	};
	vm.newLb = function(){
		ngDialog.open({
			template: baseUrl.static + 'view/newLb.html',
			controller:'NewLbCtrl',
			width:650,
			closeByDocument:false
		}).closePromise.then(function(val){
			if(val.value == 'true'){
				init();
			}
		});
	};
	vm.editIng = function(item){
		item.editing = true;
		if(!item.origin){
			item.origin = {};
			item.origin.targetPort = item.targetPort +'';
			item.origin.protocol = item.protocol +'';
			item.origin.port = item.port +'';
		}
		
	};
	vm.confirmEdit = function(r,ing){
		r.port +='';
		r.targetPort +='';
		if(!(r.targetPort.length&&r.protocol.length&&r.port.length)){
			return;
		}
		r.loading = true;
		r.isEdit = false;
		routerStore.updateSvc(ing).then(function(data){
			r.loading = false;
			r.editing = false;
		},function(e){
			r.loading = false;
			alert('adding rule failed!');
		});
		
	


	};
	vm.deleteRule = function(r,ing,index){
		var t = [];
		for(var i = 0,l= ing.rules.length;i<l;i++){
			if(index != i){
				t.push(ing.rules[i]);
			}
		}
		ing.rules = t;
		r.loading = true;
		routerStore.updateSvc(ing).then(function(data){
			r.loading = false;
		},function(e){
			r.loading = false;
			alert('deleting rule failed!');
		});
	};

	vm.addRule = function(ing){
		for(var i = 0,l = ing.rules.length;i<l;i++){
			if(ing.rules[i].isEdit){
				return;
				break;
			}
		}
		var rule = {
			targetPort:'',
			protocol:'',
			port:'',
			isEdit:true
		};
		ing.rules.push(rule);
	};
	vm.confirmIng = function(ing){
		routerStore.update(ing);
	};
	vm.deleteSvc = function(){
		for(var i = 0,l = vm.svc.length;i<l;i++){
			if(vm.svc[i].selected){
				vm.deleteItem(vm.svc[i],i);
			}
		}
		vm.selectAll = false;
	};
	vm.deleteItem = function(ing,index){

		ing.loading = true;
		routerStore.deleteSvcItem(ing).then(function(d){
			var t = [];
			ing.loading = false;
			ing.selected = false;
			ing.deleted = true;
		},function(e){
			alert('deleting ingress failed!');

		});
	};
	vm.$watch('selectAll',function(n,o){
		var list = vm.svc;
		for(var i = 0,l = list.length;i<l;i++){
			if(!list[i].deleted){
				list[i].selected = true;
			}
		}
	});

	init();
}]);





