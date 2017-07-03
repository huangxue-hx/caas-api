'use strict';

angular.module('mainApp')
	.filter('monitorMemory',function(){
		return function(input){
			if(input){
				return (parseInt(input)/1024/1024).toFixed(2);
			}
			else
				return input
		}
	})
	.filter('monitorDisk',function(){
		return function(input){
			if(input){
				return (parseInt(input)/1024).toFixed(2);
			}
			else
				return input
		}
	});