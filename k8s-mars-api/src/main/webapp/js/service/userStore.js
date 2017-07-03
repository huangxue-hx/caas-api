'use strict';

angular.module('mainApp').service('userStore',['SpHttp','$q','baseUrl',function(SpHttp,$q,baseUrl){

	var getlist = function(){
		var deferred = $q.defer();

		SpHttp.get(baseUrl.ajax+'/userlist').success(function(res){
			var t = [];
			for(var i=0,l= res.data.length; i<l; i++){
				t.push({username:res.data[i],password:''});
			}
			deferred.resolve(t);
		}).error(function(err){
			deferred.reject(err);
		});
		return deferred.promise;
	};
	var add = function(name,password){
		var deferred = $q.defer();
		SpHttp({
			method: 'post',
			url: baseUrl.ajax+'/adduser',
			data: {username:name,password:password}
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var changePwd = function(p){
		var deferred = $q.defer();
		SpHttp({
			method: 'post',
			url: baseUrl.ajax+'/changePwd',
			data: {password:p.password,username:p.username}
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};
	var deleteUser = function(p){
		var deferred = $q.defer();
		SpHttp({
			method: 'post',
			url: baseUrl.ajax+'/deleteUser',
			data: {username:p.username}
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	};



	return{
		getlist: getlist,
		add: add,
		changePwd: changePwd,
		deleteUser: deleteUser
	}
}])