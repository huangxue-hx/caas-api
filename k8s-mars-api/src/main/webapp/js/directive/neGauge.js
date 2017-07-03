'use strict';

angular.module('mainApp')
.directive('neGauge',['baseUrl','ec',function(baseUrl,ec){
    return {
      restrict:'A',
      priority:-1,
      scope:{
         neName: '=',
         neValue: '=',
         neTotal: '='
      },
      link:function postLink(scope,iElement,iAttrs){
        var myChart = ec.init(iElement[0]);
        var option = {
            // tooltip : {
            //     formatter: "{a} <br/>{b} : {c}%"
            // },
            series: [
                {
                    type: 'gauge',
                    detail: {formatter:'{value}'},
                    data: [{value:scope.neValue||0, name: scope.neName||'dsdsd'}],
                    axisLine: {
                      lineStyle: {
                        color:[
                          [0.2,'#90b7ce'],
                          [0.8,'#2f8ec0'],
                          [1,'#126087']
                        ]
                      } 
                    },
                    pointer: {
                      width: 4
                    },
                    axisLabel: {
                      show:false
                    },
                    detail: {
                      textStyle: {
                        fontSize: 40
                      }
                    }
                    

                }
            ],
            backgroundColor: 'rgb(244, 244, 244)'
        };
        scope.$watch('neValue',function(n,o){
          if(n == o){

            return;
          }
          var rate = n/scope.neTotal*100;
          option.series[0].data[0].value = parseInt(rate);
          option.series[0].detail.formatter = n+'';
          myChart.setOption(option);
        });
         scope.$watch('neTotal',function(n,o){
          if(n == o){
            return;
          }
          var rate = scope.neValue/n*100;
          option.series[0].data[0].value = parseInt(rate);
          option.series[0].detail.formatter = scope.neValue+'';
          myChart.setOption(option);
        });
        

      }
   }

}]);
