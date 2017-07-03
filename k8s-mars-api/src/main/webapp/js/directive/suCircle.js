'use strict';

angular.module('mainApp')
.directive('suCircle',['baseUrl','ec','$rootScope','$timeout',function(baseUrl,ec,$rootScope,$timeout){
    return {
      restrict:'A',
      priority:-1,
      scope:{
         neName: '=',
         neValue: '=',
         neStyle: '=',
         neTitle: '=',
         neMouseout: '&',
         neMouseover:'&'
      },
      link:function postLink(scope,iElement,iAttrs){
        var myChart = ec.init(iElement[0]);
        myChart.on("mouseover",function(data){
            $timeout(function(){
                scope.neMouseover(data);
            });
        });
        myChart.on("mouseout",function(data){
            $timeout(function(){
                scope.neMouseout(data);
            });
        });
        var tempColor = [];
        scope.$watch('neValue',function(n,o){
          if((n == o)&&(!n)){
            return ;
          }
          //cut zero data item
          var temp = [];
          tempColor = [];
          n.forEach(function(item,index){
            if(item.value >0){
              temp.push(item);
              if(!!scope.neStyle.color){
                tempColor.push(scope.neStyle.color[index]);
              }
              else{
                tempColor = false;
              }
            }
          });
          option.series[0].data = temp;
          if(tempColor&&tempColor.length){
            option.color = tempColor;
          }
          myChart.setOption(option);
        });
        var option = {
            title : {
                text: scope.neTitle||'',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)"
            },
            series : [
                {
                    name: scope.neName||'',
                    type: 'pie',
                    radius: ['50%', '70%'],
            		avoidLabelOverlap: false,
                    data:[
                    ],
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ],
            backgroundColor: scope.neStyle.background||'rgb(244, 244, 244)',
            color:scope.neStyle.color||['#5eb1dc','#3297cc','#2d89b9','#25739d','#1b5b7c']
        };

      }
   }
}]);
