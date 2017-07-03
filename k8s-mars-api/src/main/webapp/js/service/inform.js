'use strict';

angular.module('mainApp').service('inform',['$rootScope','$timeout','$compile',function($rootScope,$timeout,$compile){
	
	var tpl = document.querySelector('#sliderTpl').innerHTML;
	var notis = {};
	angular.element('body').append('<div class="sp-sliders-wrap"></div>');
	var slider = angular.element('.sp-sliders-wrap');



	//info:{title:'title',isShow:false,isHide:false,type:'noti/alert'}
	var showInform = function(info){ 


		var serial = parseInt(Math.random()*10000);
		serial = 'noti' + serial;

		var scope = $rootScope.$new();
		scope.serial = serial;
		notis[serial] = info;
		for(var i in info){
			scope[i] = info[i];
		}
		scope.time = new Date().getTime();
		var html  = $compile(tpl)(scope);
		slider.append(html);
		$timeout(function(){
			scope.isShow = true;
		},0);
		$timeout(function(){
			if(notis[serial]){
				$rootScope.$broadcast('noti.add',notis[serial]);
				scope.confirm(serial);
			}
		},30000);

		scope.confirm = function(serial){
			notis[serial] = null;
			scope.isShow  = false;
			$timeout(function(){
				scope.isHide = true;
			},300);
			$timeout(function(){
				angular.element('#'+serial).remove();
				scope.$destroy();
			},600);
		};
	};

	return{
		showInform : showInform
	};

}])