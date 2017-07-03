'use strict';

angular.module('mainApp')
.directive('suLoad',['loadSvc',function(loadSvc){
	return{
		restrict: 'A',
		priority: 2000,
		link: function(scope,element,attrs){
			var flag = false;
			element.on('click',function(e){
				if(flag){
          			return false;
				}
        	});
        	loadSvc.reg(attrs.suLoad,function(){
        		flag = true;
        		element.attr('disabled','true');
        		element.addClass('loadButton');
        	},function(){
        		flag= false;
        		element.removeAttr('disabled');
        		element.removeClass('loadButton');
        	});
		}
	}
}])