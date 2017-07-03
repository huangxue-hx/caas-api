'use strict';

angular.module('mainApp').service('notification',['$rootScope','$timeout','$compile','jump',function($rootScope,$timeout,$compile,jump){
	
	var tpl = document.querySelector('#notificationTpl').innerHTML;
	var scope = $rootScope.$new();
	scope.noti = [];
	scope.expand = false;
	var html  = $compile(tpl)(scope);
	// console.log(html);
	angular.element('body').append(html);
	scope._deleteCount = 0;
	var listenList = [];

	
	angular.element('.notification-wrap').on('click',function(e){
		e.stopPropagation();
	});
	var show = function(){
		scope.expand = true;
		angular.element('body').one('click',hide);
	};
	var hide = function(){
		scope._deleteCount = 0;
		scope.expand = false;
		var temp = [];
		var flag = false;
		for(var i = 0,l = scope.noti.length;i<l;i++){
			if(!scope.noti[i].isDelete){
				temp.push(scope.noti[i]);
			}
			else{
				flag = true;
			}
		}
		if(flag){
			scope.noti = temp;
		}
		fireAmountChange(scope.noti);
	};
	var add = function(noti){
		noti.time = new Date().getTime();
		console.log(noti);
		noti.animate = true;
		scope.noti.unshift(noti);
		$timeout(function(){
			noti.animate = false;
		});
		fireAmountChange(scope.noti);
		
	};
	var fireAmountChange = function(list){
		console.log(list);
		var hasWarning = false;
		for(var i = 0,l = list.length; i<l;i++){
			if(list[i].type =='error'){
				hasWarning = true;
				break;
			}
		}
		listenList.forEach(function(fn){
			fn.call(null,scope.noti.length,hasWarning);
		});
	};
	var onAmountChange = function(fn){
		listenList.push(fn);
	};

	scope.close = function(index){
		if(!scope.noti[index].isDelete){
			scope.noti[index].isDelete = true;
			scope._deleteCount ++;	
		}
		
	};
	scope.jump = function(item){
		jump.go(item.target);
	};



	return{
		show: show,
		hide: hide,
		add: add,
		onAmountChange:onAmountChange
	};

}])