'use strict';
angular.module('mainApp')
	.directive('neShowmore',[function(){
		return{
			restrict: 'A',
			link: function(scope,element,attr){
				var timeout;
				var init = function(){
					element.width(attr.neWidth);
					element.height(attr.neShrinkHeight);
					element.css({"overflow":"hidden"});	
				};
				var expand = function(){
					element.animate({
						"height":attr.neExpandHeight + "px",
					},300);
				};
				var shrink = function(){
					element.animate({
						"height":attr.neShrinkHeight + "px",
					},300);
				};
				element.find('input').on('focus',function(){
					clearTimeout(timeout);
					expand();
				});
				element.find('input').on('blur',function(){
					timeout = setTimeout(function(){
						shrink();
					},100);
				});






				init();
			}
		}
	}]);