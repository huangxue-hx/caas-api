'use strict';
angular.module('mainApp')
	.directive('enterSearch',['$parse',function($parse){
		return{
			restrict: 'A',
			scope: {
				// 将scope的enterSearch于html的$scope的enterSearch绑定
				enterSearch: '&'
			},
			link: function(scope,element,attr){
				element.keypress(function(event){
					if(event.keyCode == 13){
						scope.$apply(function(){
							scope.enterSearch();
						});
					}
				})
			}
		}
	}]);