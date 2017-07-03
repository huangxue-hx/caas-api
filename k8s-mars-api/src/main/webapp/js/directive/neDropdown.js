'use strict';

angular.module('mainApp')
.directive('neDropdown',['baseUrl','$filter',function(baseUrl,$filter){
    return {
      restrict:'A',
      priority:-1,
      templateUrl :baseUrl.static+'template/neDropdown.html',
      scope:{
        neDropdown: '=',
        neList: '=',
        neStyle: '=',
        neName: '=',
        neDisp: '@',
        neFilter: '@'
      },

      link:function postLink(scope,iElement,iAttrs){

          var vm = scope;
          vm.disp = '';
          vm.list = [];
          var style = vm.neStyle||{};
          var list = iElement.find('.droplist');
          vm.expand = function(){
            var length = vm.neList.length - 1;
            var height = parseInt(style.height||50)*length;
            list.css({"height":height+"px"});
          };
          vm.shrink = function(){
            list.css({"height":"0px"});
          };
          vm.select = function(index){
            vm.neDropdown = vm.neList[index];
            vm.shrink();
          };
          vm.$watch('neList',function(n,o){
            vm.list = [];
            if(n&&(n.length > 0)){

              iElement.css({"visibility":"visible"});
              for(var i=0,l=n.length;i<l;i++){
                vm.list.push((vm.neDisp&&vm.neDisp.length)?n[i][vm.neDisp]:n[i]);
              }
              console.log(n);
              console.log(vm.list);
            }
            else{
              iElement.css({"visibility":"hidden"});
            }
          });
          vm.$watch('neDropdown',function(n,o){
            if(n){
              if(vm.neDisp&& vm.neDisp.length>0){
                vm.disp = n[vm.neDisp];
              }
              else{
                vm.disp = n;
              }
            }
            else{
              iElement.css({"visibility":"hidden"});
            }
          });


      }
   }

}]);
