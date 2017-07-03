'use strict';
angular.module('mainApp')
	.directive('deleteConfirm',['ngDialog','$filter',function(ngDialog,$filter){
		return{
			restrict: 'A',
			scope: {
				deleteConfirm: '&',
				deleteText: '@'
			},
			link: function(scope,element,attr){
				element.click(function(event){
					var d = ngDialog.open({
						template:'../template/deleteConfirm.html',
						width:400,
						controller: ['$scope',function($scope){
							if(scope.deleteText){
								$scope.deleteText = scope.deleteText;
							}
							else{
								$scope.deleteText = $filter('translate')('deleteText');
							}
							$scope.yes = function(){
								ngDialog.close(this,'yes');
							};
							$scope.no = function(){
								ngDialog.close(this,'no');
							}
						}]
					});
					d.closePromise.then(function(data){
						if(data.value == 'yes'){
							scope.deleteConfirm();
						}
					});
				})
			}
		}
	}])