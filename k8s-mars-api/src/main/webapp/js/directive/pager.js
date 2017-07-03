 'use strict';
 angular.module('mainApp').
 directive('pager',['baseUrl','$timeout',function(baseUrl,$timeout){
  return {
    restrict:'A',
    priority:-1,
    scope: {
      pageCount: '=',
      currentPage: '=',
      search: '&'
    },

      // controller:function(){},
      link: function (scope,iElement,iAttrs){
        var refresh = false;
        console.log('enter directive');
        iElement.createPage({
          pageCount:scope.pageCount,
          current:scope.currentPage,
          backFn:function(p){
            scope.currentPage = p;
            $timeout(function(){
              scope.search();
            });
          }
        });

         scope.$watch('currentPage',function(newValue,oldValue){
          if(newValue == oldValue){
            return;
          }
          iElement.createPage({
            pageCount:scope.pageCount,
            current:scope.currentPage,
            backFn:function(p){
            }
          });
        });

        scope.$watch('pageCount',function(newValue,oldValue){
          if(newValue == oldValue){
            return;
          }
          iElement.createPage({
            pageCount:scope.pageCount,
            current:scope.currentPage,
            backFn:function(p){
            }
          });
        });

        scope.baseUrl = baseUrl.static;

      }
    }

  }]);