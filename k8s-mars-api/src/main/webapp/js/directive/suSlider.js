'use strict';

angular.module('mainApp')
.directive('suSlider',[function(){
	return {
		restrict: 'A',
		priority:-1,
		templateUrl: '../../template/suSlider.html',
		replece:true,
		link:function postLink(scope,iElement,iAttrs){
			// scope.suNow = 0;
			scope.suSlider.forEach(function(item,index){
				scope['slideView'+index] = {
					left: angular.element(window).width()
				}
			});
			var first = function(){
				scope.suNow = 0;
				scope['slideView0'] = {
					left: 0
				}
			}
			scope.next = function(){
				scope.suNow++;
			}
			scope.pre = function(){
				scope.suNow--;
			}
			first();
			scope.$watch('suNow',function(newVal,oldVal){
				if(newVal != oldVal){
					if(newVal > oldVal){
						scope['slideView'+oldVal] = {
							left: -angular.element(window).width()
						}
						scope['slideView'+newVal] = {
							left: 0
						}
					}
					else{
						scope['slideView'+oldVal] = {
							left: angular.element(window).width()
						}
						scope['slideView'+newVal] = {
							left: 0
						}
					}
				}
			});
		}
	}
}]);