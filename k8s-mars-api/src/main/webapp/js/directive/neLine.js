'use strict';

angular.module('mainApp')
.directive('neLine',['baseUrl','ec','$filter',function(baseUrl,ec,$filter){
    return {
      restrict:'A',
      priority:-1,
      scope:{
         neName: '=',
         neData: '=',
         neTitle: '=',
         neMetric: '='
      },
      link:function postLink(scope,iElement,iAttrs){
        
        scope.$watch('neData',function(n,o){
            if(!!n){
                var date = [];
                var data = [];
                console.log(n)
                for(var i =0,l=n.length; i<l;i++){
                    if( (i == 0)&& (!parseInt(n[i][1]))){
                        continue;
                    }
                    date.push($filter('transTime')(n[i][0]));
                    data.push(parseFloat(n[i][1]).toFixed(2));
                }
                option.xAxis.data = date;
                option.series[0].data = data;
                myChart.setOption(option);
            }
        });
        var myChart = ec.init(iElement[0]);
        var option = {
            tooltip: {
                trigger: 'axis',
                position: function (pt) {
                    return [pt[0], '10%'];
                },
                formatter:'{b}<br /> {c}'+scope.neMetric||''
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: false,
                axisLabel:{
                  show:false
                },
                axisTick: {
                  show: false
                }
            },
            yAxis: {
                type: 'value',
                boundaryGap: [0, '100%'],
                 axisLabel:{
                  show:false,
                  textStyle:{

                    fontSize:8
                  }
                },
                 axisTick: {
                  show: false
                }
            },
            grid:[
                  {x: '-1%', y: '0%', width: '101%', height: '100%'}
            ],
            backgroundColor: 'rgb(244, 244, 244)',
            series: [
                {
                    name:scope.neTitle ||'',
                    type:'line',
                    smooth:true,
                    symbol: 'none',
                    sampling: 'average',
                    itemStyle: {
                        normal: {
                            color: 'rgb(23,130,184)'
                        }
                    },
                    areaStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                                offset: 0,
                                color: 'rgb(222,236,244)'
                            }, {
                                offset: 1,
                                color: 'rgb(222,236,244)'
                            }])
                        }
                    },
                    data: false
                }
            ]
        };
      }
   }

}]);
