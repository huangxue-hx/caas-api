'use strict'

angular.module('mainApp')
.controller('RouterController',['$scope','routerStore','ngDialog','baseUrl','entryStore','serviceList',function($scope,routerStore,ngDialog,baseUrl,entryStore,serviceList){
	var vm = $scope;
		vm.$watch('currentNamespace',function(n,o){
			if(n){
				init();
			}
		});
	vm.selectAll = false;

	var expandNow = null;
	var init = function(){
		if(!vm.currentNamespace){
			return;
		}
		routerStore.query({namespace:vm.currentNamespace}).then(function(data){
			console.log('ing');

			console.log(data);
			vm.ing = data;
		});	

		routerStore.getHost().then(function(data){
			vm.displayHost = data;
		});
		serviceList.getList(vm.currentNamespace).then(function(data){
			var tmp = [];
			for(var i = 0, l = data.length;i<l;i++){
				tmp.push(data[i].name);
			}
			vm.serviceList = tmp;
		});

	};
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
			width:650,
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
			width:600,
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
			item.origin.path = item.path +'';
			item.origin.service = item.service +'';
			item.origin.port = item.port +'';
		}
		
	};
	vm.confirmEdit = function(r,ing){
		r.port +='';
		if(!(r.path.length&&r.service.length&&r.port.length)){
			return;
		}
		r.loading = true;
		r.isEdit = false;
		routerStore.update(ing).then(function(data){
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
		routerStore.update(ing).then(function(data){
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
			path:'',
			service:'',
			port:'',
			isEdit:true
		};

		ing.rules.push(rule);
	};
	vm.confirmIng = function(ing){
		routerStore.update(ing);
	};
	vm.deleteIng = function(){
		for(var i = 0,l = vm.ing.length;i<l;i++){
			if(vm.ing[i].selected){
				vm.deleteItem(vm.ing[i],i);
			}
		}
		vm.selectAll = false;
	};
	vm.deleteItem = function(ing,index){
			ing.loading = true;
			routerStore.deleteItem(ing).then(function(d){
			var t = [];
			ing.loading = false;
			ing.selected = false;
			ing.deleted = true;
			},function(e){
				alert('deleting ingress failed!');

			});
	};
	vm.$watch('selectAll',function(n,o){
		var list = vm.ing;
		for(var i = 0,l = list.length;i<l;i++){
			if(!list[i].deleted){
				list[i].selected = true;
			}
		}
	});

	init();

}]);





