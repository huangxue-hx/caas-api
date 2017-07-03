'use strict';
angular.module('mainApp')
	.directive('floatNumber',['baseUrl',function(baseUrl){
		return{
			restrict: 'A',
			templateUrl :baseUrl.static+'view/floatNumber.html',
			scope: {
				floatNumber: '='
			},
			link: function(scope,element,attr){
				scope.list = [];
				for(var i = 0;i<101;i++){
					scope.list.push(i);
				}
				scope.list.push('100+');
				scope.now = 0;
				scope.$watch('floatNumber',function(n){
					if(n){
						if(n<= 100){
							scope.now = n;
						}
						else{
							scope.now = 101;
						}

					}
				});
			}
		}
	}]);