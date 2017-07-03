'use strict';

angular.module('mainApp')
.directive('spNumber',['baseUrl','$interval','$timeout',function(baseUrl,$interval,$timeout){
    return {
      restrict:'A',
      priority:-1,
      templateUrl:baseUrl.static+'template/spNumber.html',
      replace:true,
      scope:{
        spData:'=',
        spMax:'=',
        spMin:'=',
        spInterval:'=',
        spDirty:'='

      },
      // controller:function(){},
      link:function postLink(scope,iElement,iAttrs){
        var dirtyCheck = false;
        var _origin = null;
        var _min = null;
        scope.baseUrl = baseUrl.static;
        var filter = iAttrs.spFilter;
        var flag = false;
        if(!!filter){
          scope.filter = filter;
        }
        var interval = 1;
        if(!!scope.spInterval){
          interval = parseInt(scope.spInterval);
        }
        if(!scope.spData){
          scope.spData = interval;
        }
        scope.$watch('spMin',function(n,o){
          if(dirtyCheck){
            return;
          }
          if(!n){
            _min = interval;
          }
          else{
            _min = Math.floor(n/interval)*interval;
          }
        });


        scope.$watch('spData',function(n,o){
          if(!n){
            scope.spData = interval;
          }
          else{
            scope.spData = Math.ceil(scope.spData/interval)*interval;
          }
          if(!dirtyCheck){
            _origin = scope.spData;
            scope.spDirty = false;
          }
          else{
            if(_origin != scope.spData){
              scope.spDirty = true;
            }
            else{
              scope.spDirty = false;
            }
          }
        });
        scope.plus = function(){
          dirtyCheck = true;
          if(!scope.spMax||(scope.spData<scope.spMax)){
            scope.spData = parseInt(scope.spData);
            scope.spData += interval;
          }
          $timeout(function(){
            dirtyCheck = false;
          },0);
          
        };
        scope.minus = function(){
          dirtyCheck = true;
          if(scope.spData>_min){
            scope.spData = parseInt(scope.spData);
            scope.spData-= interval;
          }
          $timeout(function(){
            dirtyCheck = false;
          },0);

        };
        iElement.find('.plus').on('mousedown',function(){
          flag = true;
          var time = 100;
          $timeout(function(){

            var itv = function(){
              $timeout(function(){
                scope.plus();
                if(flag){
                  itv();
                }
              },time);
            };
            if(flag){
              itv();
            }

          },500);
          $timeout(function(){
            time = 50;
          },2000);
          
        });
        iElement.find('.minus').on('mousedown',function(){
          flag = true;
          var time = 100;
          $timeout(function(){
            var itv = function(){
              $timeout(function(){
                scope.minus();
                if(flag){
                  itv();
                }
              },time);
            };
            if(flag){
              itv();
            }
          },500);
          $timeout(function(){
            time = 50;
          },2000);
        });
         iElement.find('.minus,.plus').on('mouseup',function(){
            flag = false;
        });
      }
   }

}]);
