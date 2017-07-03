'use strict'

angular.module('mainApp')
.service('SpHttp',['$http','inform','login','$filter',function($http,inform,login,$filter){
	var _sucHeap = {},
		_errHeap  = {},
		_nowId  = false;
	// function busy() {
 //      document.body.style.cssText = "cursor: progress !important";
 //    }
 //    function idle() {
    //   document.body.style.cssText = "";
    // }

	var sendReq = function(url,params,method){
		// busy();
		var rd = Math.random();
		rd = 'fn' + parseInt(rd*10000);
		_nowId = rd;
	  $http[method](url, params).success(function(result) {
        // idle();
        if(_sucHeap[rd]){
        	_sucHeap[rd](result);
        }
        if(!result.success){
   //      	var errorMessage;
   //      	if(angular.isObject(result.errMsg)){
   //      		errorMessage =  result.errMsg.address + ":" + result.errMsg.port + " " + result.errMsg.syscall + " " + result.errMsg.code;
   //      	}
   //      	else{
   //      		errorMessage = result.errMsg;
   //      	}
   //      	var inf = {
			// 	title: "操作失败",
			// 	text: errorMessage,
			// 	type: "error"
			// 	}
			// inform.showInform(inf);
        }
        // ErrorHandle.handle(result)
      }).error(function(reason, status) {
        // idle();
        if(_errHeap[rd]){
        	_errHeap[rd](reason,status);
        }
        if (status) {
          if (status === 401) {
          	login.login();
         //  	var target = url.split("?");
        	// target = target[0].split("/");
        	// target = target[target.length-1];
        	// if(target != 'listUnread'||target!='getCurrentUser'){
        	// 	window.location.href = '/login.html';
        	// }
          } 
          else {
            var inf = {
				title: $filter('translate')('serviceError'),
				text: $filter('translate')('serviceErrorText'),
				type: "error"
				}
			inform.showInform(inf);
          }
        }
      });
	};
	var SpHttp = function(opt){
		// busy();
		var rd = Math.random();
		rd = 'fn' + parseInt(rd*10000);
		_nowId = rd;
		$http(opt).success(function(result){
			// idle();
        	if(_sucHeap[rd]){
        		_sucHeap[rd](result);
        	}
        	if(!result.success){
	   //      	var errorMessage;
    //     		if(angular.isObject(result.errMsg)){
    //     			errorMessage =  result.errMsg.address + ":" + result.errMsg.port + " " + result.errMsg.syscall + " " + result.errMsg.code;
    //     		}
    //     		else{
    //     			errorMessage = result.errMsg;
    //     		}
    //     		var inf = {
				// 	title: "操作失败",
				// 	text: errorMessage,
				// 	type: "error"
				// 	}
				// inform.showInform(inf);
        	}
		}).error(function(reason,status){

			// idle();

	        if(_errHeap[rd]){
	        	_errHeap[rd](reason,status);
	        }
	        if (status) {
	          if (status === 401) {
	          	login.login();
	         //  	var target = url.split("?");
	        	// target = target[0].split("/");
	        	// target = target[target.length-1];
	        	// if(target != 'listUnread'||target!='getCurrentUser'){
	        	// 	window.location.href = '/login.html';
	        	// }
	          } 
	          else {
	            var inf = {
					title: $filter('translate')('serviceError'),
					text: $filter('translate')('serviceErrorText'),
					type: "error"
					}
				inform.showInform(inf);
	          }

	        }

		});
		return SpHttp;
	};
	var _get = function(url,opt){
		sendReq(url,opt,'get');
		return SpHttp;
	};
	var _post = function(url,opt){
		sendReq(url,opt,'post');
		return SpHttp;
	};
	var _delete = function(url,opt){
		sendReq(url,opt,'delete');
		return SpHttp;
	};
	var _put = function(url,opt){
		sendReq(url,opt,'put');
		return SpHttp;
	}
	var _success = function(fn){
		if(fn){
			_sucHeap[_nowId] = fn;
		}
		return SpHttp;
	};
	var _error = function(fn){
		if(fn){
			_errHeap[_nowId] = fn;
		}
		return SpHttp;
	};
	SpHttp.get = _get;
	SpHttp.post = _post;
	SpHttp.delete = _delete;
	SpHttp.put = _put;
	SpHttp.success = _success;
	SpHttp.error = _error;

	return SpHttp;

}])
