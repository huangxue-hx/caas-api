'use strict';
angular.module('mainApp')
	.directive('conEdit',['baseUrl','$timeout',function(baseUrl,$timeout){
		return{
			restrict: 'A',
			scope: {
				clChanged: '&',
				conEdit: '=',
				clPattern: '@',
				clWidth:'@',
				clType:'@',
				clList:'='

			},
      		templateUrl:baseUrl.static+'template/conEdit.html',
			link: function(scope,element,attr){
				scope.$$type = scope.clType||'text';
				if(scope.clWidth){
					element.find('.j-input').css({
						width:scope.clWidth+'px'
					});
				}
				var originValue = scope.conEdit;
				scope.isEdit = false;
				scope.edit = function(){
					scope.isEdit = true;
					$timeout(function(){
						element.find('.j-input').focus();
					},0);
				};
				if(scope.clPattern){
					var pt = new RegExp(scope.clPattern);

				}
				scope.confirm = function(){

					if(scope.conEdit == originValue){
						scope.isEdit = false;
					}
					else{
						if(scope.clPattern){
							if(pt.test(scope.conEdit)){
								scope.clChanged();
								scope.isEdit = false;
								originValue = scope.conEdit;

							}
							else{
								scope.error = true;
							}

						}
						else{
							scope.clChanged();
							scope.isEdit = false;
							originValue = scope.conEdit;

						}
					}

				};


			
			}
		}
	}]);