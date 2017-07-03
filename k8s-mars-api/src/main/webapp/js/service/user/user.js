'use strict'

angular.module('mainApp').service('userName',['SpHttp','$q',function(SpHttp,$q){
	var userList = function(){
		
		var deferred = $q.defer();
		var url = "/rest/userlist";
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
			
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	


	var getUser = function(userName){
		var deferred = $q.defer();
		var url = "/rest/userlist?username"+userName;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var getQuery = function(query){
		var deferred = $q.defer();

		var url = '/rest/userlist?username='+query;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var addUser = function(sendData){
		var deferred = $q.defer();
		SpHttp({
			method: 'POST',
			url: '/rest/adduser',
			data: sendData
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			console.log(err);
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var deleteUser = function(sendData){
		var deferred = $q.defer();

		SpHttp({
			method: 'POST',
			url: '/rest/deleteUser',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data: sendData
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var updateUser = function(item){
		var deferred = $q.defer();

		SpHttp({
			method: 'Post',
			url:'/rest/changePwd',
			data: {userName:item.userName,newPassword:item.newPassword,oldPassword:item.oldPassword},
		}).success(function(res){
			deferred.resolve(res);
		}).error(function(err){
			console.log(err);
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var rolebindingList = function(username){
		var deferred = $q.defer();
		var url = "/rest/rolebinding/user?user="+username;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	var roleGetList = function(username){
		var deferred = $q.defer();
		var url = "/rest/image/user/projectList?user="+username;
		SpHttp.get(url).success(function(data){
			deferred.resolve(data.data);
		}).error(function(err){
			deferred.reject(err);
		})
		return deferred.promise;
	}

	return{
		userList : userList,
		updateUser: updateUser,
		getUser : getUser,
		getQuery :getQuery,
		addUser : addUser,
		deleteUser : deleteUser,
		rolebindingList: rolebindingList,
		roleGetList: roleGetList
	}
}])