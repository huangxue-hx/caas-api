'use strict';

angular.module('mainApp')
.directive('projectTabs',['baseUrl','$filter',function(baseUrl,$filter){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/spTabs.html',
      replace:true,
      link:function postLink(scope,iElement,iAttrs){
        scope.baseUrl = baseUrl.static;
        if(!scope.now){
          scope.now = 'basic';
        }
        scope.switchTab = function(n){
          if(n !=scope.now){
            scope.now = n;
          }
        };
        scope.switchTab('projectInformation');
      }
   }
}])

.directive('harborTabs',['baseUrl','$filter',function(baseUrl,$filter){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/spTabs.html',
      replace:true,
      link:function postLink(scope,iElement,iAttrs){
        scope.baseUrl = baseUrl.static;
        if(!scope.now){
          scope.now = 'basic';
        }
        scope.switchTab = function(n){
          if(n !=scope.now){
            scope.now = n;
          }
        };
        scope.switchTab('harborInformation');
      }
   }
}])

.directive('tenantTabs',['baseUrl','$filter',function(baseUrl,$filter){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/spTabs.html',
      replace:true,
      link:function postLink(scope,iElement,iAttrs){
        scope.baseUrl = baseUrl.static;
        if(!scope.now){
          scope.now = 'basic';
        }
        scope.switchTab = function(n){
          if(n !=scope.now){
            scope.now = n;
          }
        };
        scope.switchTab('tenantInformation');
      }
   }
}])

.directive('mytenantTabs',['baseUrl','$filter',function(baseUrl,$filter){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/tenantTabs.html',
      replace:true,
      link:function postLink(scope,iElement,iAttrs){
        scope.baseUrl = baseUrl.static;
        if(!scope.now){
          scope.now = 'basic';
        }
        scope.switchTab = function(n){
          if(n !=scope.now){
            scope.now = n;
          }
        };
        scope.switchTab('tenantInformation');
      }
   }
}])