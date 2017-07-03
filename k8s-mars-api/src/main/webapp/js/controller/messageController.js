'use strict';

// 首页index页面消息栏的控制器
angular.module('mainApp')
.controller('MessageController',['$scope','$rootScope','$state','menu','$location','$translate',function($scope,$rootScope,$state,menu,$location,$translate){
	
	$scope.$state = $state;
	
	$scope.icon = "left";
	$scope.openMenu = function(){
		if($scope.foldMenu){
			$scope.foldMenu = false;
			$scope.icon = "left";
		}
		else{
			$scope.foldMenu = true;
			$scope.icon = "right";
		}

	}
	$scope.authDirect = "right";
	$scope.authorization = false;

	$scope.clusterComiconDirect = "right";
	$scope.clusterCom = false;

	$scope.expandComiconDirect = "right";
	$scope.expandCom = false;

	$scope.expand = function(indexa,indexb){
		if($scope.menuList[indexa].subMenu[indexb].direct == 'right')
			$scope.menuList[indexa].subMenu[indexb].direct = 'down';
		else
			$scope.menuList[indexa].subMenu[indexb].direct = 'right'
	}



	//ask for authorization menu
	$rootScope.namespace = 'default';
	var initMenu = function(){
		menu.menuList($rootScope.currentNamespace).then(function(data){
			$scope.menuList = data;
			//location url based on menu
			var nowurl = $location.url();
			var router = nowurl.split('/')[1];
			var exist = false;
			var first = 'dashboard';
			var firstExist = false;
			// var init = false;
			if($scope.menuList && $scope.menuList.length>0){
				$scope.menuList.forEach(function(itemone){
					itemone.subMenu.forEach(function(itemtow){
						if(itemtow.url == router){
							exist = true;
						}
						else if(itemtow.url == first){
							firstExist = true;
						}
						// else if(itemtow.url == 'initial'){
						// 	init = true;
						// }
					});
				});
				if(!exist && firstExist){
					$location.url('/dashboard');
				}
				else if(!exist && !firstExist){
					$location.url('/dashboardPm');
				}
			}

			// $scope.menuList.forEach(function(itema){
			// 	itema.subMenu.forEach(function(itemb){
			// 		itemb.direct = 'right';
			// 	})
			// })
		});
	}
	$scope.$on('namespaceCheckChange',initMenu);

	

	$scope.$on('userLogin',function(){
		initMenu();
	});
	
	initMenu();

	//logout相关处理
	$scope.logout = function(){
		window.location.href = '../login.html';
	}
	$scope.translate = function(){
		if($translate.use()=='ch'){
			$translate.use('en');
		}
		else
			$translate.use('ch');
	}



}]);