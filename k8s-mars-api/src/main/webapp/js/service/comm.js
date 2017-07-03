'use strict';

angular.module('mainApp').service('comm',['$rootScope','$timeout','socketio','$location','$q',function($rootScope,$timeout,socketio,$location,$q){

	var addr = $location.protocol() +'://'+$location.host()+':'+'9090';
	var create = function(userName){
		if(userName) {
			var io = socketio.connect(addr+'?userName='+userName, {path:'/rest/socketio'});
		} else {
			var io = socketio.connect(addr, {path:'/rest/socketio'});
		}

		var listen = function(name,cb){
			io.on(name,function(d){
				cb.call(null,d);
			});
		};
		var disconnect = function(){
			io.disconnect();
		};
		return {
			listen: listen,
			disconnect: disconnect
		};

	};
	
	return {
		create: create
	};

}])