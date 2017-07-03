'use strict';

angular.module('mainApp')
.directive('nePreload',['$timeout',function($timeout){
    return {
      restrict:'A',
      priority:-1,
      link:function postLink(scope,iElement,iAttrs){
        $timeout(function(){
          // iElement.find(':before,:after').fadeOut();
          iElement.removeClass('ne-preload');
        },1000);   
      }
   }

}]);
