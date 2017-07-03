'use strict';

angular.module('mainApp').service('lineCharts',['ec','$filter',function(ec,$filter){


	var createChart = function(sel,name,unit){
		unit = unit||'';
		var o = {};
		o.name = name;
		o.addData = function(data){
			this.chart.addData([
		        [
		            0,        // 系列索引
		            data[0], // 新增数据
		            false,    // 新增数据是否从队列头部插入
		            false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
		            data[1]  // 坐标轴标签
		        ]
		    ]);
		};
		o.batchAddData = function(data){
			var d = [];
			var t = [];
			data.forEach(function(item){
				t.push($filter('transTime')(item[0]));
				if(unit == 'MiB')
					d.push($filter('monitorMemory')(item[1]));
				else if(unit == 'KB'||unit == 'KB/s')
					d.push($filter('monitorDisk')(item[1]));
				else
					d.push(item[1]);
			});
			// for(var i=0,l = data.length;i<l;i++){
			// 	d.push(data[i].value);
			// 	t.push(data[i].date);
			// }
			var opt = {

				tooltip : {
					trigger: 'axis',
					formatter: "{a}:{c}"+unit+"<br />time:{b}"
				},
				xAxis : [
					{
						type : 'category',
						boundaryGap : false,
						data : t
					}
				],
				yAxis : [
					{
						type : 'value',
						axisLabel : {
							formatter: '{value}'+unit
						}
					}
				],
				addDataAnimation:false,
				animation:false,
				color:['#93bedb'],
				series : [
				  {
				      name:this.name,
				      type:'line',
				      smooth:true,
				      itemStyle: {normal: {areaStyle: {type: 'default'}}},
				      data:d,
				  }
				]
	        };
			this.chart.clear();
			this.chart.setOption(opt);

		};
		o.clearData = function(){
			this.chart.clear();
			this.chart.setOption(option);
		};
		var option = {
	          	title : {
	            	text: o.name,
	            	x: 'center',
	                y: 'bottom',
	            	textStyle : {
	            		fontSize: 15,
    					color: '#728a9a',
    					fontWeight : 'normal',
    					fontFamily: 'Microsoft Yahei, Hiragino Sans GB, WenQuanYi Micro Hei, sans-serif'
	            	}
	         	 },
				tooltip : {
					trigger: 'axis',
					formatter: "{a}:{c}"+unit+"<br />time:{b}"
				},
				// legend: {
				//     data:['CPU']
				// },
				// toolbox: {
				//     show : true,
				//     feature : {
				//         mark : {show: true},
				//         dataView : {show: true, readOnly: false},
				//         magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
				//         restore : {show: true},
				//         saveAsImage : {show: true}
				//     }
				// },
				// calculable : true,
				xAxis : [
					{
						type : 'category',
						boundaryGap : false,
						data : ['','','','','','','','','','','','','','','','','','','','','','','','','','','','','','']
					}
				],
				yAxis : [
					{
						type : 'value',
						axisLabel : {
							formatter: '{value}'+unit
						}
					}
				],
				addDataAnimation:false,
				animation:false,
				series : [
						{
							type:'line',
							name:o.name,
							smooth:true,
							itemStyle: {normal: {areaStyle: {type: 'default'}}},
							data:[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
						}
				]
	      };
	      try{
	      	var myChart = echarts.init($(sel)[0]); 
			myChart.setOption(option);
			o.chart = myChart;
			return o;
		  } 	
	      catch(e){
	      	console.log(e);
	      }
	    
	};


	return {createChart: createChart};
}])
.service('multiLineCharts',['ec','$filter',function(ec,$filter){


	var createChart = function(sel,name,unit){
		unit = unit||'';
		var o = {};
		o.name = name;
		o.addData = function(data){
			this.chart.addData([
		        [
		            0,        // 系列索引
		            data[0][0], // 新增数据
		            false,    // 新增数据是否从队列头部插入
		            false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
		            data[0][1]  // 坐标轴标签
		        ],
		        [
		            1,        // 系列索引
		            data[1][0], // 新增数据
		            false,    // 新增数据是否从队列头部插入
		            false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
		            data[1][1]  // 坐标轴标签
		        ]
		    ]);
		};
		o.batchAddData = function(data){
			var d1 = [];
			var d2 = [];
			var t = [];
			data[0].forEach(function(item){
				t.push($filter('transTime')(item[0]));
				if(unit == 'MiB')
					d1.push($filter('monitorMemory')(item[1]));
				else if(unit == 'KB'||unit == 'KB/s')
					d1.push($filter('monitorDisk')(item[1]));
				else
					d1.push(item[1]);
			});
			data[1].forEach(function(item){
				if(unit == 'MiB')
					d2.push($filter('monitorMemory')(item[1]));
				else if(unit == 'KB'||unit == 'KB/s')
					d2.push($filter('monitorDisk')(item[1]));
				else
					d2.push(item[1]);
			});
			var opt = {
				// legend:{
				// 	show: true
				// },
				tooltip : {
					trigger: 'axis',
					formatter: "{a0}:{c0}"+unit+"<br/> {a1}:{c1}"+unit+"<br /> time:{b}"
				},
				legend : {
					data: this.name
				}, 
				xAxis : [
					{
						type : 'category',
						boundaryGap : false,
						data : t
					}
				],
				yAxis : [
					{
						type : 'value',
						axisLabel : {
							formatter: '{value}'+unit
						}
					}
				],
				addDataAnimation:false,
				animation:false,
				// color:['#5290bf','#8db9be'],
				series : [
				  {
				      name:this.name[0],
				      type:'line',
				      smooth:true,
				      itemStyle: {normal: {areaStyle: {type: 'default'}}},
				      data:d1
				  },
				  {
				  	  name:this.name[1],
				      type:'line',
				      smooth:true,
				      itemStyle: {normal: {areaStyle: {type: 'default'}}},
				      data:d2
				  }
				]
	        };
			this.chart.clear();
			this.chart.setOption(opt);

		};
		o.clearData = function(){
			this.chart.clear();
			this.chart.setOption(option);
		};
		var option = {
	          title : {
	                text: o.name[2],
	                x: 'center',
	                y: 'bottom',
	                textStyle : {
	            		fontSize: 15,
    					color: '#728a9a',
    					fontWeight : 'normal',
    					fontFamily: 'Microsoft Yahei, Hiragino Sans GB, WenQuanYi Micro Hei, sans-serif'
	            	}
	          },
				tooltip : {
					trigger: 'item',
					formatter: "{a0}:{c0}"+unit+"<br/> {a1}:{c1}"+unit+"<br /> time:{b}"
				},
				legend: {
			        data:[o.name[0],o.name[1]]
			    },
				// toolbox: {
				//     show : true,
				//     feature : {
				//         mark : {show: true},
				//         dataView : {show: true, readOnly: false},
				//         magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
				//         restore : {show: true},
				//         saveAsImage : {show: true}
				//     }
				// },
				// calculable : true,
				xAxis : [
					{
						type : 'category',
						boundaryGap : false,
						data : ['','','','','','','','','','','','','','','','','','','','','','','','','','','','','','']
					}
				],
				yAxis : [
					{
						type : 'value',
						axisLabel : {
							formatter: '{value}'+unit
						}
					}
				],
				addDataAnimation:false,
				animation:false,
				series : [
						{
							type:'line',
							name: o.name[0],
							smooth:true,
							itemStyle: {normal: {areaStyle: {type: 'default'}}},
							data:[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
						},
						{
							type:'line',
							name: o.name[1],
							smooth:true,
							itemStyle: {normal: {areaStyle: {type: 'default'}}},
							data:[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
						}
				]
	      };
	      try{
	      	var myChart = echarts.init($(sel)[0]); 
			myChart.setOption(option);
			o.chart = myChart;
			return o;
	      }
	      catch(e){
	      	console.log(e);
	      }
	    
	};


	return {createChart: createChart};
}])
;