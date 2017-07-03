'use strict';

angular.module('mainApp')
	.filter('harborName',function(){
		return function(input){
			if(input){
				var index = input.indexOf('-');
				return input.substring(index+1);
			}
		}
	})
	.filter('compStatus',function(){
		return function(input){
			if(input == "Y"){
				return "Running";
			}
			else{
				return "Down";
			}
		}
	});
