'use strict';

angular.module('mainApp')
.directive('podTabs',['baseUrl','$filter',function(baseUrl,$filter){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/spTabs.html',
      replace:true,
      // scope:{
      //    spTabs: '=',
      //    appData: '='
      // },
      // controller:function(){},
      link:function postLink(scope,iElement,iAttrs){
        scope.baseUrl = baseUrl.static;
        if(!scope.now){
          scope.now = 'basic';
        }
        scope.switchTab = function(n){
          if(n !=scope.now){
            scope.now = n;
            scope.$emit('spTabChanged',n);
          }
        };
        scope.switchTab('podInformation');
      }
   }

}]);
