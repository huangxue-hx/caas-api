'use strict';

angular.module('mainApp').service('login',['$rootScope','$compile','$timeout','authStore','$state',function($rootScope,$compile,$timeout,authStore,$state){
	
	var tpl = document.querySelector('#loginTpl').innerHTML;
	var vm = $rootScope.$new();
	vm.isLogin = true;
	vm.bottom = parseInt(angular.element(window).height());




	// scope.noti = [];
	// scope.expand = false;
	var html  = $compile(tpl)(vm);
	// console.log(html);
	angular.element('body').append(html);
	var login = function(){
		vm.isLogin = false;
		$timeout(function(){
			vm.bottom = 0;
		});
	};
	vm.loginFn = function(){
		authStore.login({
			username: vm.username,
			password: vm.password
		}).then(function(d){
			vm.bottom = parseInt(angular.element(window).height());
			$timeout(function(){
				vm.isLogin = true;
			},1000);
			if(d.isSuperAdmin){
				$state.go('initial');
			}
			else{
				reload();
			}

		},function(e){
			shake();
		});
	};
	var shake = function(){
		vm.isWrong = true;
		$timeout(function(){
			vm.isWrong = false;
		},300);
	};
	var reload = function(){
		$state.reload();
		$rootScope.$broadcast('userLogin');
		vm.username = '';
		vm.password = '';

	};





	// scope._deleteCount = 0;

	
	// angular.element(html).on('click',function(e){
	// 	e.stopPropagation();
	// });
	// var show = function(){
	// 	scope.expand = true;
	// 	$timeout(function(){
	// 		angular.element('body').one('click',hide);
	// 	});

	// };
	// var hide = function(){
	// 	scope._deleteCount = 0;
	// 	scope.expand = false;
	// 	var temp = [];
	// 	var flag = false;
	// 	for(var i = 0,l = scope.noti.length;i<l;i++){
	// 		if(!scope.noti[i].isDelete){
	// 			temp.push(scope.noti[i]);
	// 		}
	// 		else{
	// 			flag = true;
	// 		}
	// 	}
	// 	if(flag){
	// 		scope.noti = temp;
	// 	}
	// };
	// var add = function(noti){
	// 	noti.time = new Date().getTime();
	// 	console.log(noti);
	// 	noti.animate = true;
	// 	scope.noti.unshift(noti);
	// 	$timeout(function(){
	// 		noti.animate = false;
	// 	});
	// };

	// scope.close = function(index){
	// 	scope.noti[index].isDelete = true;
	// 	scope._deleteCount ++;
	// };
	// scope.jump = function(item){
	// 	jump.go(item.target);
	// };


	return{
		login: login
		
	};

}])
