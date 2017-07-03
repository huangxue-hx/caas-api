'use strict'

angular.module('mainApp').controller('ImageAllController',ImageAllController);
ImageAllController.$inject = ['$scope','$http','inform','$filter','ngDialog','harborStore','$rootScope']
function ImageAllController($scope,$http,inform,$filter,ngDialog,harborStore,$rootScope){
	
	var harborAll;
	
	var tenantlist = [];
	
	var namespace= $rootScope.currentNamespace;

	$rootScope.$on('namespaceCheckChange',function(event,data){
		namespace = data.namespace;
		init();
	});

	//上传下载
	$scope.upload = function(){
		var d = ngDialog.open({
			template: '../view/img/upload.html',
			width: 600,
			scope:$scope
		})
	}

	var init = function(){
		harborStore.listViaUser(namespace).then(function(data){
			$scope.harborList = angular.copy(data);
			harborAll = angular.copy(data);
			$scope.harborList.forEach(function(item){
				item.checkbox = false;
			});
			// console.log(data);
		});
		
	}
	init();
	
};





