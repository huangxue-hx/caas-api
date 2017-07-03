angular.module('mainApp')
.config(['$translateProvider',function($translateProvider){
	// $translateProvider.useSanitizeValueStrategy('sanitize');

	$translateProvider.translations('ch',{
		// index page
		'Service': '服 务',
		'Storage': '存 储',
		'Image': '镜 像',
		'Router': '路 由',
		'service': '服务',
		'storage': '存储',
		'volumes':'存储',
		'imagetitle':'镜像',
		'image': '镜像',
		'router': '路由' ,
		'warning': '近期警告' ,
		'setting': '设置',
		'logout': '退出',
		'notification': '通知',
		'close': '关闭',
		'notNotification': '暂无通知',
		'confirm': '确定',
		'login': '登录',
		'chooseTrans':'switch to English',

		// serviceAll page
		'serviceList':'服务列表',
		'new':'创建',
		'operate':'操作',
		'start':'启 动',
		'stop':'停 止',
		'deleteText':'确定删除吗？',
		'delete':'删 除',
		'searchText':'请输入服务器名称或label进行搜索,不同label用，隔开',
		'name':'名称',
		'namespace':'namespace',
		'state':'状态',
		'label':'label',
		'serviceIP':'服务地址',
		'version':'版本',
		'instanceCount':'服务实例',
		'startTime':'创建时间',
		'stopped':'已停止',
		'started':'已启动',
		'stopping':'停止中',
		'starting':'启动中',
		'none':'无',
		'detail':'查看',

		// storageAll page
		'storageList':'存储列表',
		'deleteStorage':'确定删除服务吗？',
		'capacity':'大小',
		'usageState':'使用状态',
		'bound':'可用',
		'pending':'等待',
		'noBindService':'无绑定服务',

		//routerAll page
		'httpProxy':'HTTP代理',
		'entrance':'入口',
		'urlPattern':'入口',
		'port':'端口',
		'tcpProxy':'TCP/UDP代理',
		'targetPort':'目标端口',
		'proxy':'协议',
		'protocol':'协议',

		//autoFlex page
		'targetCpuUsage':'目标CPU占用率',
		'instanceRange':'当前数量范围',
		'nowInatanceNum':'当前实例数量',
		'nowCpuUsage':'当前CPU占用',
		'autoflexTime':'最近伸缩时间',
		'closeAutoflex':'关闭自动伸缩',
		'closeAutoflexText':'确定关闭自动伸缩？',
		'noAutoflexEvent':'暂时没有伸缩事件出现',

		//chooseFlex page
		'autoflexConfig':'自动伸缩配置',
		'minInstance':'当前数量范围下限',
		'maxInstance':'当前数量范围上限',

		//choosePod page
		'openConsole':'打开控制台',
		'choosePod':'选择pod',
		'chooseContainer':'选择容器',
		'getConsoleInformation':'获取控制台信息',

		//confirm page
		'no':'取消',
		'yes':'确定',

		//event page
		'noEvent':'暂时没有事件出现',

		//modifyContainer page
		'modifyContainer':'修改容器',
		'conatinerName':'容器名称',
		'fillInContainerName':'请填写容器名称',
		'wrongContainerName':'错误的容器名称',
		'chooseImage':'选择镜像',
		'fillInImage':'请补全镜像信息',
		'memory':'内存配额',
		'cpuValue':'CPU权重',
		'portConfig':'端口配置',
		'fillInPort':'请输入端口',
		'fillInProtocol':'请输入对应协议',
		'wrongPort':'错误的端口范围',
		'portDuplicatePort':'与当前容器中的其他端口重复',
		'portDuplicateContainer':'与其他容器中的端口重复',
		'command':'执行命令',
		'fillInCommand':'请输入命令',
		'args':'执行参数',
		'fillInArgs':'请输入参数',
		'env':'环境变量',
		'inputEnvKey':'环境变量的key',
		'inputEnvValue':'环境变量的value',
		'constantStorage':'持久化存储',
		'mount':'挂载',
		'inputMount':'请输入挂载点',
		'chooseVolume':'请选择存储卷',
		'readOnly':'只读',
		'wrongMount':'错误的挂载点格式,请以/xxx的格式输入',
		'gitStorage':'git存储',
		'inputGitUrl':'请输入git地址',
		'inputRevision':'请输入revision',
		'wrongGitUrl':'错误的git地址格式,请以url的格式输入',
		'deleteContainer':'删除容器',
		'deleteContainerText':'确定删除该容器？',
		'save':'保存修改',

		//newContainer page
		'newContainer':'添加容器',
		'saveContainer':'添加容器',

		//modifyRouter page
		'modifyRouter':'修改路由规则',
		'annotation':'备注',

		//monitorChart page
		'container':'容器',
		'time':'时间',
		'thirtyMinute':'30分钟以内',
		'sixHour':'6小时以内',
		'twentyFourHour':'24小时以内',
		'sevenDay':'7天以内',
		'thirtyDay':'30天以内',
		'setupToNow':'从创建开始至今',
		'createMonitor':'正在生成监控数据,请耐心等待...',

		//newIng page
		'newProxy':'新建代理',
		'addTCPRouter':'新建TCP路由',
		'addHTTPRouter':'新建HTTP路由',
		'fillInName':'请填写名称',
		'fillInNamespace':'请填写namespace',
		'hostName':'Host名称',
		'chooseService':'请选择服务',
		'targetService':'目标端口',
		'saveProxy':'新建代理',
		'fillInPath':'请输入路径',
		'portMap':'端口映射',
		'subDomain':'名称',
		'domain':'域名',

		//newLb page
		'fillInPicker':'请填写选择器',
		'portRange':'端口(20000-20670)',
		'protocolType':'协议类型',
		'selector':'服务选择',

		//podContainer page
		'refreshConfig':"更新配置",
		'runState':'运行状态',
		'restartTime':'重启次数',
		'readOnlyYes':'是',
		'readOnlyNo':'否',
		'log':'日志',
		'storagePath':'挂载路径',
		'wrongPath':'错误的挂载路径格式',
		'gitUrl':'git地址',

		//podInformation page
		'activeTime':'启动时间',
		'podActiveTime': '创建时间',
		'containerCount':'容器数量',
		'IP':'IP地址',
		'nodeIP':'节点地址',
		'creationTime':'创建时间',

		//podInstance page
		'internelIP':'内网地址',
		'podClusterIP':'pod集群地址',
		'internalDomain':'内网域名',
		'podIP':'pod地址',
		'running':'运行',
		'pause':'暂停',

		//serviceDetail page
		'autoflex':'自动伸缩',

		//serviceInformation page
		'refershTime':'更新时间',
		'internelPort':'内部端口',

		//serviceNew page
		'newService':'创建服务',
		'serviceName':'服务名称',
		'fillInServiceName':'请填写服务名称',
		'wrongServiceName':'错误的服务名称格式',
		'labelText':'请以a=b的形式填写,不同的label用,隔开',
		'wrongLabel':'错误的label格式',
		'session':'保持会话',
		'containerList':'容器列表',
		'config':'配置',
		'add':'创建',
		'fillInInternelIP':'请输入内部IP',
		'wrongIP':'错误的内部IP格式,IP上限为:255.255.255.255',
		'saveService':'创建服务',

		//warninglist page
		'related':'关联对象',

		//setting page
		'serviceAndPod':'服务/pod',
		'deleting':'删除中...',
		'creating':'新建中...',

		//storageDetail page
		'serviceHost':'服务器',
		'newStorage':'新建存储',
		'storageName':'存储名称',
		'saveStorage':'创建存储',
		'fillInStorageName':'请填写存储名称',
		'wrongStorageName':'错误的存储名称格式',

		//imageAll page 
		'downAndUpload':'上传与下载',
		'author':'作者',
		'loading':'载入中...',

		//upload page
		'downloadImage':'下载镜像',
		'downloadStep1':'在本地 docker 环境中输入以下命令，就可以pull一个镜像到本地了。',
		'downloadStep2':'注意：为了在本地方便使用，下载后您可以修改tag成短标签，比如',
		'pushImage':'发布镜像',
		'stepOne':'1. 在本地 docker 环境中输入以下命令进行登录。',
		'stepTwo':'2. 假如在本地已经有了想要push的image,比如这个image名为hello-world。如果在本地没有image,可以首先从Docker官方网站去pull一个下来。',
		'stepThree':'3. 然后，需要对这个image进行标记，在命令中输入：',
		'stepFour':'4. 最后在命令行输入如下命令就可以push这个image到你创建的镜像仓库中了。',

		'repository':'自定义仓库名',

		//editInstance page
		'inputInstance':'请填写实例',
		'instanceMustNumber':'实例数量必须为数字',
		'instanceMustLagerOne':'实例数量必须大于1',

		//podDetaileController psge
		'basicInformation':'基本信息',
		'podInformation':'基本信息',
		'monitor':'监控',
		'podInstance':'pod实例',
		'event':'事件',

		//confirm related
		'serviceError':'服务端异常',
		'serviceErrorText':'系统出了点小问题，请稍后重试！',
		'inputError':'输入错误',
		'inputCommand':'请输入执行命令',
		'inputArgs':'请输入执行参数',
		'inputEnv':'请补全环境变量',
		'inputStorage':'请补全挂载配置',
		'storageDuplicate':'重复的挂载路径',
		'atLeastOnPort':'请输入至少一条端口配置',
		'nameDuplicate':'重名错误',
		'conatinerNameDuplicate':'该容器与其他容器重名',
		'serviceNameDuplicate':'该服务与同一namespace下的其他服务重名',
		'noServiceError':'未选择服务',
		'selectServiceError':'请选择需要操作的服务',
		'giveUpService':'确定放弃新建服务？',

		'namespaceDuplicate':'与现有namespace重复！',
		'newFail':'新建失败！',
		'inputNamespace':'请输入namespace名称！',
		'deleteNamespaceService':'请先删除该namespace下的应用，再尝试删除！',
		'deleteFail':'删除失败！',

		'noStorageError':'未选择存储',
		'selectStorageError':'请选择需要操作的存储',
		'storageNameDuplicate':'该服务与同一namespace下的其他存储重名',

		'hostaddress':'入口域名：',
		'entranceip':'入口IP：',
	});

	$translateProvider.translations('en',{
		// index page
		'Service': 'Service',
		'Storage': 'Storage',
		'Image': 'Image',
		'Router': 'Router',
		'service': 'service',
		'storage': 'volume',
		'volumes': 'volume(s)',
		'imagetitle': 'IMAGE',
		'image': 'image',
		'router': 'router' ,
		'warning': 'warning' ,
		'setting': 'setting',
		'logout': 'logout',
		'notification': 'Notification',
		'close': 'close',
		'notNotification': 'No Notification',
		'confirm': 'confirm',
		'login': 'login',
		'chooseTrans':'切换为中文',

		// serviceAll page
		'serviceList':'SERVICE LIST',
		'new':'create',
		'operate':'operations',
		'start':'start',
		'stop':'stop',
		'deleteText':'Are You Sure to Delete?',
		'delete':'delete',
		'searchText':'search with service name or label',
		'name':'name',
		'namespace':'namespace',
		'state':'state',
		'label':'label',
		'serviceIP':'internal IP',
		'version':'version',
		'instanceCount':'service instance',
		'startTime':'active time',
		'stopped':'stopped',
		'started':'running',
		'stopping':'stopping',
		'starting':'starting',
		'none':'N/A',
		'detail':'about detail',

		// storageAll page
		'storageList':'VOLUME LIST',
		'deleteStorage':'Are You Sure to Delete?',
		'capacity':'capacity',
		'usageState':'usage state',
		'bound':'bound',
		'pending':'pending',
		'noBindService':'no service',

		//routerAll page
		'httpProxy':'HTTP Router(s)',
		'entrance':'entrance',
		'urlPattern':'URL pattern',
		'port':'port',
		'tcpProxy':'TCP/UDP Router(s)',
		'targetPort':'target port',
		'proxy':'proxy',
		'protocol':'protocol',

		//autoFlex page
		'targetCpuUsage':'target CPU usage',
		'instanceRange':'instance range',
		'nowInatanceNum':'instane',
		'nowCpuUsage':'current CPU usage',
		'autoflexTime':'last autoscaling time',
		'closeAutoflex':'disable autoscaling',
		'closeAutoflexText':'Sure To Close autoscaling？',
		'noAutoflexEvent':'no autoscaling event',

		//chooseFlex page
		'autoflexConfig':'autoscaling config',
		'minInstance':'min instance count',
		'maxInstance':'max instance count',

		//choosePod page
		'openConsole':'open console',
		'choosePod':'pod',
		'chooseContainer':'container',
		'getConsoleInformation':'get console detail',

		//confirm page
		'no':'cancel',
		'yes':'confirm',

		//event page
		'noEvent':'no event',

		//modifyContainer page
		'modifyContainer':'modify conatiner',
		'conatinerName':'name',
		'fillInContainerName':'please fill in container name',
		'wrongContainerName':'container name illegal',
		'chooseImage':'image',
		'fillInImage':'please fill in image',
		'memory':'memory quota',
		'cpuValue':'CPU weight',
		'portConfig':'port',
		'fillInPort':'please input port',
		'fillInProtocol':'please input protocol',
		'wrongPort':'invalid port range',
		'portDuplicatePort':'port same with port in this container',
		'portDuplicateContainer':'port same with port in other container',
		'command':'command',
		'fillInCommand':'please input command',
		'args':'args',
		'fillInArgs':'please input args',
		'env':'env',
		'inputEnvKey':'input key of environment variable',
		'inputEnvValue':'input val of environment variable',
		'constantStorage':'volume(s)',
		'mount':'mount',
		'inputMount':'please input mount',
		'chooseVolume':'please choose volume',
		'readOnly':'read only',
		'wrongMount':'wrong mount,please input as "/xxx"',
		'gitStorage':'git volume(s)',
		'inputGitUrl':'please input git url',
		'inputRevision':'please input revision',
		'wrongGitUrl':'wrong git url,please input as url format',
		'deleteContainer':'delete',
		'deleteContainerText':'Sure To Delete Container',
		'save':'save',

		//newContainer page
		'newContainer':'add container',
		'saveContainer':'save',

		//modifyRouter page
		'modifyRouter':'modify router',
		'annotation':'annotation',

		//monitorChart page
		'container':'container',
		'time':'time',
		'thirtyMinute':'last 30 minutes',
		'sixHour':'last 6 hours',
		'twentyFourHour':'last 24 hours',
		'sevenDay':'last 7 days',
		'thirtyDay':'last 30 days',
		'setupToNow':'since creation',
		'createMonitor':'creating monitor chart, please wait...',

		//newIng page
		'newProxy':'add proxy',
		'addTCPRouter':'create TCP Router',
		'addHTTPRouter':'create HTTP Router',
		'fillInName':'please fill in name',
		'fillInNamespace':'please fill in namespace',
		'hostName':'base domain',
		'chooseService':'service',
		'targetService':'target service',
		'saveProxy':'create',
		'fillInPath':'please fill in path',
		'portMap':'url mapping',
		'subDomain':'sub domain',
		'domain':'domain',

		//newLb page
		'fillInPicker':'please fill in picker',
		'portRange':'port range(20000-20670)',
		'protocolType':'protocol type',
		'selector':'service selector',

		//podContainer page
		'refreshConfig':"update",
		'runState':'state',
		'restartTime':'restart count',
		'readOnlyYes':'yes',
		'readOnlyNo':'no',
		'log':'log',
		'storagePath':'path',
		'wrongPath':'wrong path format',
		'gitUrl':'git url',

		//podInformation page
		'activeTime':'start time',
		'podActiveTime': 'create time',
		'containerCount':'container',
		'IP':'IP',
		'nodeIP':'node IP',
		'creationTime':'creation time',

		//podInstance page
		'internelIP':'internal IP',
		'podClusterIP':'pod cluster IP',
		'internalDomain':'internal domain',
		'podIP':'pod IP',
		'running':'running',
		'pause':'pause',

		//serviceDetail page
		'autoflex':'autoscaling',

		//serviceInformation page
		'refershTime':'last update',
		'internelPort':'port',

		//serviceNew page
		'newService':'add service',
		'serviceName':'name',
		'fillInServiceName':'please fill in service name',
		'wrongServiceName':'service name illegal',
		'labelText':'please input label as aaa=bb format',
		'wrongLabel':'label illegal',
		'session':'session',
		'containerList':'container(s)',
		'config':'config',
		'add':'add',
		'fillInInternelIP':'please fill in internal IP',
		'wrongIP':'IP illegal',
		'saveService':'save',

		//warninglist page
		'related':'related',

		//setting page
		'serviceAndPod':'service/pod',
		'deleting':'deleting...',
		'creating':'creating...',

		//storageDetail page
		'serviceHost':'service IP',
		'newStorage':'add volume',
		'storageName':'name',
		'saveStorage':'save',
		'fillInStorageName':'please fill in volume name',
		'wrongStorageName':'volume name illegal',

		//imageAll page 
		'downAndUpload':'upload & download',
		'author':'author',
		'loading':'loading...',

		//upload page
		'downloadImage':'download image',
		'downloadStep1':'In local docker environment input the following commands, then you can pull an image to your local repository.',
		'downloadStep2':'Notice：For your convenience, you can change the tag to short label after download,for example:',
		'pushImage':'release image',
		'stepOne':'1. In local environment input the following commands to login.',
		'stepTwo':'2. If you already had an image that want to pushed,for example its named hello-world.Otherwise you don\'t have an image,you can pull one on Docker official websit first.',
		'stepThree':'3. And then,we need to tag this image, input commands: ',
		'stepFour':'4. Finally input the following commands in console,and then you can push this image to your own image repository.',
		'repository':'self-define repository',

		//editInstance page
		'inputInstance':'please instance',
		'instanceMustNumber':'instance must be number',
		'instanceMustLagerOne':'instance must lager than 1',

		//podDetaileController psge
		'basicInformation':'serivce overview',
		'podInformation':'pod overview',
		'monitor':'monitor',
		'podInstance':'pod(s)',
		'event':'event',

		//confirm related
		'serviceError':'service exception',
		'serviceErrorText':'service exception，please try again later！',
		'inputError':'input erroe',
		'inputCommand':'please input command',
		'inputArgs':'please input args',
		'inputEnv':'please input environment variable',
		'inputStorage':'please input volume',
		'storageDuplicate':'duplicate volume path',
		'atLeastOnPort':'please input at least on port config',
		'nameDuplicate':'multiple name error',
		'conatinerNameDuplicate':'this container has same name with other container',
		'serviceNameDuplicate':'this service has same name with other service in this namespace',
		'noServiceError':'haven\'t choose service',
		'selectServiceError':'please choose service',
		'giveUpService':'Sure To Give Up Create Service?',

		'namespaceDuplicate':'multiple namespace',
		'newFail':'creat fail',
		'inputNamespace':'please input namespace',
		'deleteNamespaceService':'please delete service of this namespace and try again later',
		'deleteFail':'delete fail',

		'noStorageError':'haven\'t choose volume',
		'selectStorageError':'please choose volume',
		'storageNameDuplicate':'this volume has same name with other volume in this namespace',

		'hostaddress':'entrance domain：',
		'entranceip':'entrance IP：',
	});

	$translateProvider.preferredLanguage('en');
}])