'use strict'

angular.module('mainApp')
.directive('tanTabs',['baseUrl','$filter',function(baseUrl,$filter){
	return{
		restrict:'A',
		priorty:-1,
		templateUrl: baseUrl.static+'template/tanTabs.html',
		replace:true,

		link:function postLink(scope,iElement,iAttrs){
			scope.baseUrl = baseUrl.static;
        	if(!scope.now){
          		scope.now = 'basic';
        	}
			scope.switchTab = function(n){
				if(n != scope.now){
					scope.now = n;
					scope.$emit('tanTabChange',n);
				}
			};
			scope.switchTab('basicTanInformation');
		}
	}
}])