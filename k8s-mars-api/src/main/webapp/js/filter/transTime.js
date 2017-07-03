'use strict';

angular.module('mainApp')
	.filter('transTime',['$filter',function($filter){
		return function(input){
			//input是传入的字符串
			if(input){
				var sp;
				if(sp = input.split(".")){
					// console.log(sp);
					input = sp[0];
				}
				// return input.replace(/[T Z]/gi," ");
				var date = new Date(Date.parse(input));
				// console.log(input);
				// console.log(date);
				date = $filter('date')(date,'yyyy-MM-dd HH:mm:ss');
				return date;
			}
			else
				return input;
		}
	}]);