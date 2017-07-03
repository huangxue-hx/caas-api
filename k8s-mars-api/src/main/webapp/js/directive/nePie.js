'use strict';

angular.module('mainApp')
.directive('nePie',['baseUrl','ec',function(baseUrl,ec){
    return {
      restrict:'A',
      priority:-1,
      scope:{
         neName: '=',
         neValue: '=',
         neBackground: '=',
         neColor: '=',
         neTitle: '='
      },
      link:function postLink(scope,iElement,iAttrs){
        var myChart = ec.init(iElement[0]);
        var tempColor = [];
        scope.$watch('neValue',function(n,o){
          if((n == o)&&(!n)){
            return ;
          }
          //if there is no data in array,don't show pie
          var nodata = true;
          n.forEach(function(item){
            if(item.value != 0){
              nodata = false;
            }
          });
          if(!nodata){
            //if there has data,cut the zero item
            var temp = [];
            tempColor = [];
            n.forEach(function(item,index){
              if(item.value >0){
                temp.push(item);
                if(!!scope.neColor){
                  tempColor.push(scope.neColor[index]);
                }
                else{
                  tempColor = false;
                }
              }
            });
            option.series[0].data = temp;
            option.title.subtext = '';
            if(tempColor){
              option.color = tempColor;
            }
            console.log(option);
            myChart.setOption(option);
          }
          else{
            option.series[0].data = [];
            option.title.subtext = 'No Data';
            myChart.setOption(option);
          }
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
                    radius : '70%',
                    center: ['50%', '50%'],
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
            backgroundColor: scope.neBackground||'rgb(244, 244, 244)',
            color:scope.neColor||['#5eb1dc','#3297cc','#2d89b9','#25739d','#1b5b7c']
        };

      }
   }

}]);
