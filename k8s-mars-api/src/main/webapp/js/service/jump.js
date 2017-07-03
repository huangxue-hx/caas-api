'use strict';

angular.module('mainApp').service('jump',['$rootScope','$timeout','$state',function($rootScope,$timeout,$state){
	
	var go = function(p){
		if(p.kind == 'Pod'){
			$state.go('podDetail',{name:p.name,namespace:p.namespace});
		}
		else if(p.kind =='Deployment'){
			$state.go('serviceDetail',{name:p.name,namespace:p.namespace});
		}
	};

	return{
		go : go
	};

}])