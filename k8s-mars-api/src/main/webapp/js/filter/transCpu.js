'use strict';

angular.module('mainApp')
	.filter('cpuNumber',function(){
		return function(input){
			if(input)
				return parseInt(input)/1000;
			else
				return input;
		}
	})
	.filter('cpuString',function(){
		return function(input){
			if(input)
				return input*1000+'m';
		}
	})
	.filter('cpuFormat',function(){
		return function(input){
			if(input)
				return input+'m';
		}
	})
	.filter('newmemoryFormat',function(){
		return function(input){
			if(input){
				if(input.indexOf('i')>=0){
					return input;
				}
				else
					return input+'MiB';
			}
		}
	})
	.filter('memoryFormat',function(){
		return function(input){
			return input+'MiB';
		}
	})
	.filter('dynamicFilter',['$filter',function($filter){
	
		return function(value, filterName) {
			if(filterName){
				return $filter(filterName)(value);
			}
			else{
				return value;
			}
	    	
	  	};
	}])
	.filter('cpuRate',function(){
		return function(input){
			if(input)
				return input+'%';
		}
	})
	.filter('nsTemp',function(){
		return function(input){
			if(input =='admin'){
				return 'All Namespaces';
			}
			else{
				return input;
			}
		}
	}).filter('password',function(){
		return function(input){
			return '******';
		}
	}).filter('offset', function() { 
		return function(input, start) {
			start = parseInt(start, 10);
		return input.slice(start); 
	}
});