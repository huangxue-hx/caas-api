'use strict';

angular.module('mainApp').controller('VolumeListController',VolumeListController);
VolumeListController.$inject = ['$scope','$http','$filter','inform','ngDialog','volume']
function VolumeListController($scope,$http,$filter,inform,ngDialog,volume){
	
	var volumelist = [];
	var init = function(){
		volume.list().then(function(data){
			
			$scope.volumeList = data;
			volumelist = data;
			$scope.volumeList.forEach(function(item){
				item.checkbox = false;
			})
			
		})
	}

	$scope.checkboxAll = false;
	$scope.$watch('checkboxAll',function(newVal, oldVal, scope){
		if(newVal != oldVal){
			if($scope.checkboxAll){
				$scope.volumeList.forEach(function(item){
					item.checkbox = true;
				});
			}
			else{
				$scope.volumeList.forEach(function(item){
					item.checkbox = false;
				})
			}
		};
	});

	$scope.delete = function(data){
		var deleteList = [];
		var tenantid;
		$scope.volumeList.forEach(function(item){
			if(item.checkbox){
				deleteList.push(item);
				
			}
		});
		if(deleteList.length == 0){
			var inf = {
				title: $filter('translate')('noVolumeError'),
				text: $filter('translate')('selectVolumeError'),
				type: "error"
			}
			inform.showInform(inf);
		}

		deleteList.forEach(function(item,index){
			if($scope.volumeList.tenantid){
				var sendData = {
					
					tenantid: $scope.volumeList.tenantid,
					name: item.name
				}
			}else{
				var sendData = {
					tenantid: '',
					name: item.name
				}
			}
			volume.deleteVolume(sendData).then(function(data){
				if(!data.success){
					var inf = {
						title: 'Detele Error',
						text: data.errMsg,
						type: 'error'
					}
					inform.showInform(inf);
				}
				if(index == (deleteList.length-1)){
					init();
				}
			})
		});
	}

	$scope.volumeNew = function(){
		var d = ngDialog.open({
			template:'../../view/volume/volumeNew.html',
			width:650,
			closeByDocument: false,
			controller: 'VolumeNewController'
		});
		d.closePromise.then(function(data){
			if(data.value == 'done'){
				init();
			}
		});	
	}

	init();
}