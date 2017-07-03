'use strict';

angular.module('mainApp')
.directive('spSwitch',['baseUrl',function(baseUrl){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/spSwitch.html',
      replace:true,
      scope:{
         spValue: '='
      },
      link:function postLink(scope,iElement,iAttrs){
        scope.baseUrl = baseUrl.static;
        scope.$watch('spValue',function(n,o){
          scope.val = !!scope.spValue;
        });
        if(!iAttrs.readonly){
          scope.toggle = function(){
            scope.val = scope.spValue = !scope.val;
          };
        }
       
        
      }
   }

}]);