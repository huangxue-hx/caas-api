'use strict';

angular.module('mainApp', [
	'ui.router',
	// permission
	// 'permission',
	// 'permission.ui',
	'ngDialog',
	'angular-popups',
	'pascalprecht.translate'
	])
	.constant('baseUrl', {
	  'static': '',
	  'ajax': '/rest',
	  'dashboard': '/rest/dashboard',
	  'rest': '/rest',
	  'infrastructure': '/rest/infrastructure',
	  'cloudPlatform': '/rest/cloudPlatform'
	})
	.config(['$stateProvider','$urlRouterProvider','$httpProvider','ngDialogProvider', function($stateProvider,$urlRouterProvider,$httpProvider,ngDialogProvider){
		ngDialogProvider.setDefaults({
			closeByDocument: false
		});

		$httpProvider.interceptors.push('httpInterceptor');

		$urlRouterProvider.when("", "/dashboard")
			.when("/service","/service/serviceAll")
			.when("/storage","/storage/storageAll")
			.when("/image","/image/imageAll")
			.when("/router","/router/routerAll")
			.when("/auth","/auth/role")
			.when("/auth/role","/auth/role/roleAll")
			.when("/auth/roleBinding","/auth/roleBinding/roleBindingAll")
			.when("/user","/user/userList")
			.when("/tenant","/tenant/tenantList")
			.when("/node","/node/nodeList")
			// .when("/mytenant","/myTenant/mytenantList")
			.when("/myTenant","/myTenant/myTenantDetail")
			// .when("/service/serviceDetail/:name","/service/serviceDetail/:name/serviceInformation/:name") ;
			.when("/harbor","/harbor/harborList")
			.when("/volume","/volume/volumeList")
			.when("/network","/network/networkList")
			.when("/myProject","/myProject/myProjectDetail")
			.when("/component","/component/componentList")
			.when("/monitor","/monitor/monitorlist")
			.when("/imageprovider","/imageprovider/imageproviderlist")
			.when("/logprovider","/logprovider/logproviderlist")
			.when("/storageprovider","/storageprovider/storageproviderlist")
			.when("/networkprovider","/networkprovider/networkproviderlist")
			.when("/lbprovider","/lbprovider/lbproviderlist")
			.when("/audit","/audit/adminAudit")


		$stateProvider
			.state('service',{
				url: '/service',
				templateUrl: '../view/service.html',
				// controller: 'serviceController'
			})
			.state('serviceAll',{
				url: '/serviceAll',
				templateUrl: '../view/serviceAll.html',
				parent: 'service',
				controller: 'ServiceAllController'
			})
			.state('serviceDetail',{
				url: '/serviceDetail/:name/:namespace',
				templateUrl: '../view/service/serviceDetail.html',
				parent: 'service',
				controller: 'ServiceDetailController'
			})
			.state('podDetail',{
				url: '/podDetail/:name/:service/:namespace',
				templateUrl: '../view/podDetail.html',
				parent: 'service',
				controller: 'PodDetailController'
			})
			.state('serviceNew',{
				url: '/serviceNew',
				templateUrl: '../view/serviceNew.html',
				parent: 'service',
				controller: 'ServiceNewController'
			})
			.state('router',{
				url: '/router',
				templateUrl: '../view/router.html',
				// controller: 'serviceController'
			})
			.state('httpRouter',{
				url: '/routerhttp',
				templateUrl: '../view/routerHttp.html',
				controller: 'RouterController',
				parent: 'router'
			})
			.state('tcpRouter',{
				url: '/routertcp',
				templateUrl: '../view/routerTcp.html',
				controller: 'RouterSvcController',
				parent: 'router'
			})
			.state('newRouter',{
				url: '/newRouter',
				templateUrl: '../view/newRouter.html',
				parent: 'router',
				controller: 'NewRouterController'
			})
			.state('storage',{
				url: '/storage',
				templateUrl: '../view/storage/storage.html',
				// controller: 'serviceController'
			})
			.state('storageAll',{
				url: '/storageAll',
				templateUrl: '../view/storage/storageAll.html',
				parent: 'storage',
				controller: 'StorageAllController'
			})
			.state('storageNew',{
				url: '/storageNew',
				templateUrl: '../view/storage/storageNew.html',
				parent: 'storage',
				controller: 'StorageNewController'
			})
			.state('storageDetail',{
				url: '/storageDetail/:name/:namespace',
				templateUrl: '../view/storage/storageDetail.html',
				parent: 'storage',
				controller: 'StorageDetailController'
			})
			.state('image',{
				url: '/image',
				templateUrl: '../view/img/image.html',
			})
			.state('imageAll',{
				url: '/imageAll',
				templateUrl: '../view/img/imageAll.html',
				parent: 'image',
				controller: 'ImageAllController'
			})
			.state('imgDetail',{
				url:'/imgDetail/:tenantid/:harborid',
				templateUrl:'../view/harbor/harborDetail.html',
				// templateUrl:'../view/black.html',
				parent:'image',
				controller:'HarborDetailController'
			})

			//tenant tab begin 
			.state('tenant',{
				url:'/tenant',
				templateUrl:'../view/black.html',
			})
			.state('tenantList',{
				url:'/tenantList',
				templateUrl:'../view/tenant/tenantList.html',
				parent:'tenant',
				controller:'TenantListController'
			})
			.state('tenantDetail',{
				url:'/tenantDetail/:id',
				templateUrl:'../view/tenant/tenantDetail.html',
				parent:'tenant',
				controller:'TenantDetailController'
			})
			.state('tenantNew',{
				url:'/tenantNew',
				templateUrl:'../view/tenant/tenantNew.html',
				parent:'tenant',
				controller:'TenantNewController'
			})
			.state('tenantNetwork',{
				url:'/tenantNetwork/:id',
				templateUrl:'../view/tenant/tenantNetwork.html',
				parent:'tenant',
				controller:'TenantNetworkController'
			})
			.state('tenantProject',{
				url:'/tenantProject/:id',
				templateUrl:'../view/tenant/tenantProject.html',
				parent:'tenant',
				controller:'TenantProjectController'
			})
			.state('tenantProjectDetail',{
				url:'/tenantProjectDetail/:id/:name',
				templateUrl:'../view/project/projectDetail.html',
				parent:'tenant',
				controller:'ProjectDetailController'
			})
			.state('projectService',{
				url: '/serviceDetail/:name/:namespace',
				templateUrl: '../view/serviceDetail.html',
				parent: 'service',
				controller: 'ServiceDetailController'
			})
			.state('tenantHarbor',{
				url:'/tenantHarbor/:id',
				templateUrl:'../view/tenant/tenantHarbor.html',
				parent:'tenant',
				controller:'TenantHarborController'
			})
			.state('tenantHarborDetail',{
				url:'/tenantHarborDetail/:tenantid/:harborid',
				templateUrl:'../view/harbor/harborDetail.html',
				parent:'tenant',
				controller:'HarborDetailController'
			})
			.state('tenantNetworkDetail',{
				url:'/tenantNetworkDetail/:tenantid/:networkid',
				templateUrl:'../view/network/networkDetail.html',
				parent:'tenant',
				controller:'NetworkDetailController'
			})
			.state('tenantVolume',{
				url:'/tenantVolume/:id',
				templateUrl:'../view/tenant/tenantVolume.html',
				parent:'tenant',
				controller:'TenantVolumeController'
			})
			.state('tenantVolumeDetail',{
				url:'/tenantVolumeDetail/:id/:name',
				templateUrl:'../view/volume/volumeDetail.html',
				parent:'tenant',
				controller:'VolumeDetailController'
			})

			//mytanant tab
			// .state('myTenant',{
			// 	url:'/myTenant',
			// 	templateUrl:'../view/mytenant/mytenant.html',
			// })
			// .state('myTenant',{
			// 	url:'/myTenant',
			// 	templateUrl:'../view/mytenant/mytenantList.html',
			// 	parent:'myTenant',
			// 	controller:'MytenantController',
			// })
			// .state('Tenant',{
			// 	url:'/Tenant',
			// 	templateUrl:'../view/myTenant/mytenant.html',
			// })
			// .state('myTenant',{
			// 	url:'/myTenant',
			// 	templateUrl:'../view/myTenant/mytenantList.html',
			// 	parent:'Tenant',
			// 	controller:'MytenantController',
			// })
			.state('myTenant',{
				url:'/myTenant',
				templateUrl:'../view/black.html'
			})
			.state('myTenantDetail',{
				url:'/myTenantDetail',
				templateUrl:'../view/mytenant/tenantDetail.html',
				parent:'myTenant',
				controller:'TenantDetailController'
			})
			.state('mytenantProjectDetail',{
				url:'/tenantProjectDetail/:id/:name',
				templateUrl:'../view/project/projectDetail.html',
				parent:'myTenant',
				controller:'ProjectDetailController'
			})
			.state('mytenantHarborDetail',{
				url:'/tenantHarborDetail/:tenantid/:harborid',
				templateUrl:'../view/harbor/harborDetail.html',
				parent:'myTenant',
				controller:'HarborDetailController'
			})
			.state('mytenantVolumeDetail',{
				url:'/tenantVolumeDetail/:id/:name',
				templateUrl:'../view/volume/volumeDetail.html',
				parent:'myTenant',
				controller:'VolumeDetailController'
			})

			//my project related
			.state('myProject',{
				url:'/myProject',
				templateUrl:'../view/black.html'
			})
			.state('myProjectDetail',{
				url:'/myProjectDetail',
				templateUrl:'../view/project/projectDetail.html',
				parent:'myProject',
				controller:'ProjectDetailController'
			})

			//auth tab
			.state('auth',{
				url:'/auth',
				templateUrl:'../view/auth/auth.html',
			})
			.state('role',{
				url:'/role',
				templateUrl:'../view/auth/auth.html',
				parent:'auth'
			})
			.state('roleAll',{
				url:'/roleAll',
				templateUrl:'../view/auth/roleAll.html',
				parent:'role'
			})
			.state('roleDetail',{
				url:'/roleDetail/:name/:namespace',
				templateUrl:'../view/auth/roleDetail.html',
				controller:'RoleDetailController',
				parent:'role'
			})
			.state('croleDetail',{
				url:'/croleDetail/:name',
				templateUrl:'../view/auth/roleDetail.html',
				controller:'CRoleDetailController',
				parent:'role'
			})
			.state('roleNew',{
				url:'/roleNew',
				templateUrl:'../view/auth/roleNew.html',
				controller:'RoleNewController',
				parent:'role'
			})
			.state('croleNew',{
				url:'/croleNew',
				templateUrl:'../view/auth/roleNew.html',
				controller:'CRoleNewController',
				parent:'role'
			})
			.state('roleBinding',{
				url:'/roleBinding',
				templateUrl:'../view/auth/auth.html',
				parent:'auth'
			})
			.state('roleBindingAll',{
				url:'/roleBindingAll',
				templateUrl:'../view/auth/roleBindingAll.html',
				controller:'RoleBindingAllController',
				parent:'roleBinding'
			})
			.state('roleBindingDetail',{
				url:'/roleBindingDetail/:name/:namespace',
				templateUrl:'../view/auth/roleBindingDetail.html',
				controller:'RoleBindingDetailController',
				parent:'roleBinding'
			})
			.state('croleBindingDetail',{
				url:'/croleBindingDetail/:name',
				templateUrl:'../view/auth/roleBindingDetail.html',
				controller:'CRoleBindingDetailController',
				parent:'roleBinding'
			})
			.state('bindingNew',{
				url:'/bindingNew',
				templateUrl:'../view/auth/bindingNew.html',
				controller:'BindingNewController',
				parent:'roleBinding'
			})
			.state('cbindingNew',{
				url:'/cbindingNew',
				templateUrl:'../view/auth/bindingNew.html',
				controller:'CBindingNewController',
				parent:'roleBinding'
			})

			// user tab
			.state('user',{
				url:'/user',
				templateUrl:'../view/user/user.html',
			})
			.state('userList',{
				url:'/userList',
				templateUrl:'../view/user/userList.html',
				controller:'UserListController',
				parent:'user',
			})
			.state('userNew',{
				url:'/userNew',
				templateUrl:'../view/user/userNew.html',
				controller:'UserNewController',
				parent:'user',
			})
			.state('userUpdate',{
				url:'/userUpdate/:userName',
				templateUrl:'../view/user/userUpdate.html',
				controller:'UserUpdateController',
				parent:'user',
			})
			.state('userDetial',{
				url:'/userDetail/:username',
				templateUrl:'../view/user/userDetial.html',
				controller:'UserDetialCotroller',
				parent:'user',
			})
			.state('projectDetail',{
				url:'/tenantProjectDetail/:id/:name',
				templateUrl:'../view/project/projectDetail.html',
				parent:'user',
				controller:'ProjectDetailController'
			})

			// infra tab
			.state('node',{
				url:'/node',
				templateUrl:'../view/node/node.html',
			})
			.state('nodeList',{
				url:'/nodeList',
				templateUrl:'../view/node/nodelist.html',
				parent:'node',
				controller:'NodeListCtrl'
			})
			.state('nodeDetail',{
				url:'/nodeDetail/:nodeName',
				templateUrl:'../view/node/nodedetail.html',
				parent:'node',
				controller:'NodeDetailCtrl'
			})
			.state('podInformationDetail',{
				url: '/podDetail/:name/:service/:namespace',
				templateUrl: '../view/podDetail.html',
				parent: 'node',
				controller: 'PodDetailController'
			})

			.state('harbor',{
				url:'/harbor',
				templateUrl:'../view/black.html'
			})
			.state('harborList',{
				url:'/harborList',
				templateUrl:'../view/harbor/harborList.html',
				parent:'harbor',
				controller:'HarborAllController'
			})
			.state('HarborDetail',{
				url:'/tenantHarborDetail/:tenantid/:harborid',
				templateUrl:'../view/harbor/harborDetail.html',
				// templateUrl:'../view/black.html',
				parent:'harbor',
				controller:'HarborDetailController'
			})
			.state('repoDetail',{
				url:'/repoDetail/:harbor/:repo/:tag',
				templateUrl:'../view/harbor/repoDetail.html',
				parent:'harbor',
				controller:'RepoDetailController'
			})

			.state('volume',{
				url:'/volume',
				templateUrl:'../view/volume/volume.html'
			})
			.state('volumeList',{
				url:'/volumeList',
				templateUrl:'../view/volume/volumeList.html',
				parent:'volume',
				controller:'VolumeListController'	
			})
			.state('volumeDetail',{
				url:'/tenantVolumeDetail/:id/:name',
				templateUrl:'../view/volume/volumeDetail.html',
				parent:'volume',
				controller:'VolumeDetailController'
			})
			.state('network',{
				url:'/network',
				templateUrl:'../view/network/network.html'
			})
			.state('networkList',{
				url:'/networkList',
				templateUrl:'../view/network/networkList.html',
				parent:'network',
				controller:'NetworkListController'
			})
			.state('networkDetail',{
				url:'/tenantNetworkDetail/:tenantid/:networkid',
				templateUrl:'../view/network/networkDetail.html',
				parent:'network',
				controller:'NetworkDetailController'
			})

			//dashboard
			.state('dashboard',{
				url:'/dashboard',
				templateUrl:'../view/dashboard/dashBoard.html',
				controller:'DashboardController'

			})
			//dashboard
			.state('dashboardPm',{
				url:'/dashboardPm',
				templateUrl:'../view/dashboard/dashboardPm.html',
				controller:'DashboardPmController'

			})
			// component
			.state('component',{
				url:'/component',
				templateUrl:'../view/component/component.html'

			})
			.state('componentList',{
				url:'/componentList',
				parent:'component',
				templateUrl:'../view/component/componentList.html',
				controller:'ComponentListController'

			})
			.state('componentDetail',{
				url:'/componentDetail/:name/:node',
				parent:'component',
				templateUrl:'../view/component/componentDetail.html',
				controller:'ComponentDetailController'
			})
			// monitor
			.state('influxDB',{
				url:'/monitor',
				templateUrl:'../view/black.html'

			})
			.state('influxDBList',{
				url:'/monitorlist',
				parent:'influxDB',
				templateUrl:'../view/extend/monitorList.html',
				controller:'MonitorProviderListController'

			})	
			.state('harborCom',{
				url:'/imageprovider',
				templateUrl:'../view/black.html'

			})
			.state('harborComList',{
				url:'/imageproviderlist',
				parent:'harborCom',
				templateUrl:'../view/extend/imageList.html',
				controller:'ImageProviderListController'

			})	

			.state('elasticSearch',{
				url:'/logprovider',
				templateUrl:'../view/black.html'

			})
			.state('elasticSearchList',{
				url:'/logproviderlist',
				parent:'elasticSearch',
				templateUrl:'../view/extend/logList.html',
				controller:'LogProviderListController'

			})	

			.state('volumeCom',{
				url:'/storageprovider',
				templateUrl:'../view/black.html'

			})
			.state('volumeComList',{
				url:'/storageproviderlist',
				parent:'volumeCom',
				templateUrl:'../view/extend/storageList.html',
				controller:'StorageProviderListController'

			})	


			.state('networkCom',{
				url:'/networkprovider',
				templateUrl:'../view/black.html'

			})
			.state('networkComList',{
				url:'/networkproviderlist',
				parent:'networkCom',
				templateUrl:'../view/extend/networkList.html',
				controller:'NetworkProviderListController'

			})	


			.state('HAproxy',{
				url:'/lbprovider',
				templateUrl:'../view/black.html'

			})
			.state('HAproxyList',{
				url:'/lbproviderlist',
				parent:'HAproxy',
				templateUrl:'../view/extend/lbList.html',
				controller:'LbProviderListController'

			})

			.state('initial',{
				url:'/initial',
				templateUrl:'../view/initial/initial.html',
				controller:'InitialController'
			})

			.state('audit',{
				url:'/audit',
				templateUrl:'../view/black.html',
			})
			.state('adminAudit',{
				url:'/adminAudit',
				parent:'audit',
				templateUrl:'../view/audit/admin.html',
				controller:'AdminAuditController'
			})	

	}])
	.factory('noCacheInterceptor', function() {
	    return {
	      request: function(config) {
	        if (config.method === 'GET' && config.url.indexOf('tpl.html') === -1) {
	          var separator = config.url.indexOf('?') === -1 ? '?' : '&';
	          config.url = config.url + separator + 'noCache=' + new Date().getTime();
	        }
	        return config;
	      }
	    };
  	})
	 .config(['$httpProvider',function($httpProvider) {
    //jshint -W089
    $httpProvider.interceptors.push('noCacheInterceptor');

    $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded';
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
    // $httpProvider.defaults.headers.delete['Content-Type'] = 'application/x-www-form-urlencoded';
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    // Override $http service's default transformRequest
    $httpProvider.defaults.transformRequest = [function(data) {
      /**
       * The workhorse; converts an object to x-www-form-urlencoded serialization.
       * @param {Object} obj
       * @return {String}
       */
      var param = function(obj) {
        var query = '',
          name, value, fullSubName, subName, subValue, innerObj, i;

        for (name in obj) {
          value = obj[name];

          if (value instanceof Array) {
            for (i = 0; i < value.length; ++i) {
              subValue = value[i];
              fullSubName = name + '[' + i + ']';
              innerObj = {};
              innerObj[fullSubName] = subValue;
              query += param(innerObj) + '&';
            }
          } else if (value instanceof Object) {
        	  /* for (subName in value) {
              subValue = value[subName];
              fullSubName = name + '[' + subName + ']';
              innerObj = {};
              innerObj[fullSubName] = subValue;
              query += param(innerObj) + '&';
            }*/
        	  for (subName in value) {
                  subValue = value[subName];
                  var str = "labels+\[+[0-9]+\]$";
                  if(name.match("labels") && !name.match(str)){
                	  fullSubName = name + '[' + subName + ']';
                  }else{
                	  fullSubName = name + '.' + subName; 
                  }
                  innerObj = {};
                  innerObj[fullSubName] = subValue;
                  query += param(innerObj) + '&';
                }
          } else {
            //edit hw 2015 5-11
            // else if (value !== undefined && value !== null) {
            //jshint -W116
            query += encodeURIComponent(name) + '=' + encodeURIComponent((value == null ? '' : value)) + '&';
          }
        }

        return query.length ? query.substr(0, query.length - 1) : query;
      };

      return angular.isObject(data) && String(data) !== '[object File]' ? param(data) : data;
    }];
  }])

;