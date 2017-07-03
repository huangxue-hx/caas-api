'use strict';

angular.module('mainApp').controller('RepoDetailController',RepoDetailController)
RepoDetailController.$inject = ['$scope','harborStore','$stateParams']
function RepoDetailController($scope,harborStore,$stateParams){
	$scope.harborname = $stateParams.harbor;
	$scope.reponame = decodeURIComponent($stateParams.repo);
	$scope.tag = $stateParams.tag;

	var packageData = {};
	var informData = {};

	$scope.spTabs = [
		{serName:'package',serTemplate:'/harbor/packageDetailChart'},
		{serName:'vulnerability',serTemplate:'/harbor/informDetailChart'}
	];

	$scope.back = function(){
		history.back(-1);
	}

	var toChart = function(data){
		var chart = [];
		for(var d in data){
			var temp = {
				name: d,
				value: data[d]
			}
			chart.push(temp);
		}
		return chart;
	}

	var init = function(){
		harborStore.repoDetail($scope.harborname,$scope.reponame,$scope.tag).then(function(data){
			$scope.data = data;
			if(!!$scope.data.vulnerabilitiesByPackage.packages_summary){
				$scope.packageAna = 0;//has chart inform
				packageData.sum = $scope.data.vulnerabilitiesByPackage.packages_summary.sum;
				packageData.chart = $scope.data.vulnerabilitiesByPackage.packages_summary.level_summary;
				packageData.table = $scope.data.vulnerabilitiesByPackage.image_packages;

				$scope.package = {
				data: toChart(packageData.chart),
				state : packageData.chart,
				sum: packageData.sum,
				table: packageData.table
				}
			}
			else if(!!$scope.data.vulnerabilitiesByPackage.success){
				$scope.packageAna = 1;//success
				$scope.packageInf = $scope.data.vulnerabilitiesByPackage.success;
			}
			else if(!!$scope.data.vulnerabilitiesByPackage.notsupport){
				$scope.packageAna = 2;//notsupport
				$scope.packageInf = $scope.data.vulnerabilitiesByPackage.notsupport;
			}
			else{
				$scope.packageAna = 3;//abnormal
				$scope.packageInf = $scope.data.vulnerabilitiesByPackage.abnormal;
			}

			if(!!$scope.data.vulnerabilitySummary.vulnerability){
				$scope.vulAna = 0;
				informData.sum = $scope.data.vulnerabilitySummary.vulnerability['vulnerability-suminfo']['vulnerability-sum'];
				informData.chart = $scope.data.vulnerabilitySummary.vulnerability['vulnerability-suminfo'];
				informData.table = $scope.data.vulnerabilitySummary.vulnerability['vulnerability-list'];

				var tempState = {
					high_risk: informData.chart['high-level-sum'],
					medium_risk: informData.chart['medium-level-sum'],
					low_risk: informData.chart['low-level-sum'],
					negligible_risk: informData.chart['negligible-level'],
					unknown: informData.chart['unknown-level']
				}
				$scope.inform = {
					state: tempState,
					data: toChart(tempState),
					sum: informData.chart['vulnerability-sum'],
					repair: informData.chart['vulnerability-patches-sum'],
					table: informData.table
				};
			}
			else if(!!$scope.data.vulnerabilitySummary.success){
				$scope.vulAna = 1;
				$scope.vulInf = $scope.data.vulnerabilitySummary.success;
			}
			else if(!!$scope.data.vulnerabilitySummary.notsupport){
				$scope.vulAna = 2;
				$scope.vulInf = $scope.data.vulnerabilitySummary.notsupport;
			}
			else{
				$scope.vulAna = 3;
				$scope.vulInf = $scope.data.vulnerabilitySummary.abnormal;
			}

		});
	};
	$scope.pieStyle ={
		background: '#fff',
		color:['#bd2119','#d96c2b','#dec57b','#527384','#298473']
	}
	init();
}