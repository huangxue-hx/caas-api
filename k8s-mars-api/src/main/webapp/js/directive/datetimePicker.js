 'use strict';
 angular.module('mainApp').
 directive('datePicker',['baseUrl',function(baseUrl){
    return {
      restrict:'A',
      priority:-1,

      // controller:function(){},
      link:function postLink(scope,iElement,iAttrs){
        console.log('enter directive');
        iElement.datetimepicker();
        scope.baseUrl = baseUrl.static;

      }
   }

}]);