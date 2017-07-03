'use strict';
angular.module('mainApp')
	.directive('neDropdownHint',['baseUrl','SpHttp','$compile','$timeout',function(baseUrl,SpHttp,$compile,$timeout){
		return{
			restrict: 'A',
			scope: {
				neDropdownHint: '=',
				ngModel: '='
			},
			link: function(scope,element,attr){
				var rd = "sn"+parseInt(Math.random()*10000);
				scope.index = -1;

				var html;
				scope.currentlist = [];
				scope.show = false;
				scope.width = element.width();
				scope.height = element.height();
				SpHttp.get(baseUrl.static+'template/neHint.html').success(function(data){
					html = $compile(data)(scope);
					html.attr({"id":rd});
					angular.element('body').append(html);
					html.attr({"id":rd});
					html.css({top:element.offset().top+ element.height(),left:element.offset().left});
				});
				var scrollHandler = function(e) {
				    html.css({top:element.offset().top + element.height(),left:element.offset().left});
				};
				document.addEventListener('scroll',scrollHandler, true);
				scope.$on("$destroy",function(){
					angular.element('#'+rd).remove();
					document.removeEventListener('scroll', scrollHandler, true);
				});
				element.on('focus',function(){
					scope.show = true;
					var val = element.val();
					if(val.length == 0){
						scope.currentlist = scope.neDropdownHint;
					}
					else{
						for(var i = 0,l = scope.neDropdownHint.length;i<l;i++){
							if(scope.neDropdownHint[i].indexOf(val)>-1){
								scope.currentlist.push(scope.neDropdownHint[i]);
							}
						}	
					}
				});
				element.on('blur',function(){
					$timeout(function(){
						scope.show = false;
						scope.currentlist = [];	
					},200);
					
				});
				element.on('keyup',function(){
					var val = element.val();
					var tmp = [];
					if(val.length == 0){
						scope.$apply(function(){
							scope.currentlist = scope.neDropdownHint;
						});
					}
					else{
						for(var i = 0,l = scope.neDropdownHint.length;i<l;i++){
							if(scope.neDropdownHint[i].indexOf(val)>-1){
								tmp.push(scope.neDropdownHint[i]);
							}
						}
						scope.$apply(function(){
							scope.currentlist = tmp;
						});	
					}
				});
				scope.select = function(val){
					scope.ngModel = val;
				};
				element.on('keydown',function(e){
					if((e.keyCode == 13)&&(scope.index > -1)){
						scope.$apply(function(){
							element.blur();
							element.val(scope.currentlist[scope.index]);
						});

					}
					else if(e.keyCode == 40){
						var l = scope.currentlist.length - 1;
						if(scope.index < l){
							scope.index ++;
						}
					}
					else if(e.keyCode == 38){
						if(scope.index > 0){
							scope.index --;
						}
					}
					else{
						scope.index = -1;
					}
				});
			}
		}
	}]);