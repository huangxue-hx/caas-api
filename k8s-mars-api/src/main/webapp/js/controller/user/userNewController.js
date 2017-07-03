'use strict'

angular.module('mainApp').controller('UserNewController',UserNewController);
UserNewController.$inject = ['$scope','$http','userName','$location','inform','pattern','$filter'];
function UserNewController($scope,$http,userName,$location,inform,pattern,$filter){

	var patterns = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,14}$/;
	var str = /(?=.*[\d]+)(?=.*[a-zA-Z]+)(?=.*[^a-zA-Z0-9]+).{7,14}/;

	$scope.back = function(){
		history.back(-1);
	}

	$scope.namePattern = pattern.commonName;

	$scope.emailPattern = pattern.mail;

	// $scope.pasPattern = pattern.pas;

	
	$scope.disable = function(){
		if(!$scope.userName || !$scope.Password || !$scope.realName)
			return true;
		else
			return false;
	}
	$scope.save = function(){
		userName.userList().then(function(data){
			var userGet = data;
			var sameName = false;
			var sameEmail = false;
			var regpas = $scope.Password;
			var flage = false;
			var fchar = false;
			userGet.forEach(function(item){
				if(item.username == $scope.userName || (!$scope.userName || $scope.userName == "")){
					sameName = true;
				}
				if(item.email == $scope.Email){
					sameEmail = true;
				}
			});
			if(sameEmail){
				var inf = {
					title: $filter('translate')('emailDuplicate'),
					text: $filter('translate')('EmailDuplicate'),
					type: "error"
				}
				inform.showInform(inf); //显示错误信息
			}
			if(sameName){
				var inf = {
					title: $filter('translate')('nameDuplicate'),
					text: $filter('translate')('userNameDuplicate'),
					type: "error"
				}
				inform.showInform(inf); //显示错误信息
			}
			flage = patterns.test(regpas)
			fchar = str.test(regpas);
			console.log(fchar);
			if(fchar == true && flage == false){
				var inf = {
					title: $filter('translate')('fillInPassword'),
					text: $filter('translate')('WrongPassword'),
					type: "error"
				}
				inform.showInform(inf); //显示错误信息
			}
			else if(flage == false){
				if(regpas.length<7){
					var inf = {
						title: $filter('translate')('fillPassword'),
						text: $filter('translate')('wrongPasswordLength'),
						type: "error"
					}
					inform.showInform(inf); //显示错误信息
				}else{
					var inf = {
						title: $filter('translate')('fillInPassword'),
						text: $filter('translate')('wrongPassword'),
						type: "error"
					}
					inform.showInform(inf); //显示错误信息
				}
			}
			else {
				var data = {
					userName: $scope.userName,
					Password: $scope.Password,
					email: $scope.Email,
					realName: $scope.realName,
					Comment: $scope.Comment
				};
				userName.addUser(data).then(function(res){
					if(res.success){
						$location.path('/user/userList') //tiaohui yuanlai yemian
					}else{
						var inf={
							title: 'Create Error',
							text:res.errMsg,
							type:'error'
						}
						inform.showInform(inf);
					}
				});
			}
		});
		
	}

}