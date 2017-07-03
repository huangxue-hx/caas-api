'use strict';

angular.module('mainApp').controller('HarborDetailController',HarborDetailController);
HarborDetailController.$inject = ['$scope','$stateParams','harborStore','ngDialog','imgStore','inform','$timeout']
function HarborDetailController($scope,$stateParams,harborStore,ngDialog,imgStore,inform,$timeout){
	var tenantid = $stateParams.tenantid;
	var harborid = $stateParams.harborid;
	$scope.color = ['#298473','#bd2119','#527384','#dec57b','#d96c2b'];

	var expandNow = null;

	$scope.back = function(){
		history.back(-1);
	}

	$scope.refresh = function(){
		console.log("aaa");
		harborStore.refresh().then(function(){
			init();
		});
	}

	$scope.toggle = function(d){
		if(d.shrink){
			expandNow = null;
			d.shrink = false;
		}
		else{
			d.shrink = true;
			if(expandNow){
				expandNow.shrink = false;
			}
			expandNow = d;
		}
	};

	$scope.deleteRepo = function(image,event){
		// console.log(image);
		imgStore.deleteRepo(image.name).then(function(data){
			initRepo();
		})
	}
	$scope.deleteTag = function(image,tag,event){
		imgStore.deleteTag(image.name,tag.tag).then(function(data){
			initRepo();
		})
	}
	var initAnalyze = function(){
		harborStore.harborAnalyze($scope.harbor.name).then(function(data){
			$scope.total=data.image_num;
			$scope.analyze = [
				{
					name:'secure',
					value:data.clair_success
				},
				{
					name:'insecure',
					value:data.unsecurity_image_num
				},
				{
					name :'not support',
					value :data.clair_not_Support
				},
				{
					name:'abnormal',
					value:data.abnormal
				},{
					name:'mild',
					value:data.mild
				}
			]
		});
	}

	var initRepo = function(){
		harborStore.repoList($scope.harbor.harborid).then(function(data){
			$scope.repoList = data;
		})
	}

	var init = function(){
		//get project detail via tenantid and name
		harborStore.detail(tenantid,harborid).then(function(data){
			$scope.harbor = data;
			initAnalyze();
			initRepo();
		});
	}
	// $scope.pieStyle ={
	// 	background: '#fff',
	// 	color: ['#5eb1dc','#3297cc']
	// }

	init();
}