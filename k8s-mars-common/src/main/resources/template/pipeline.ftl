import static java.util.UUID.randomUUID
def uuid
def label
def dateTime = new Date().format('yyyyMMddHHmmss')
<#assign buildInPod=false>
try{
httpRequest url:"${apiUrl!}/rest/openapi/cicd/preBuild?id=${job.id!}&amp;buildNum=${r'${currentBuild.number}'}&amp;dateTime=${r'${dateTime}'}",consoleLogResponseBody: true,timeout: ${timeout}
<#list stageList as stage>
<#if (stage.stageTemplateType == 0 ||((stage.stageTemplateType == 1 || stage.stageTemplateType == 6) && stage.environmentChange == true))>
<#if stage.stageOrder != 1>
    <#if buildInPod! == true>}</#if>
}
</#if>
<#assign buildInPod=true>
uuid = randomUUID() as String
label = uuid.take(8)
podTemplate(
    containers: [
        containerTemplate(
            alwaysPullImage: true,
            args: '',
            command: '',
            envVars: [
                containerEnvVar(key: 'DOCKER_DAEMON_ARGS', value: '--insecure-registry=${harborAddress!}'),containerEnvVar(key: 'GIT_SSL_NO_VERIFY', value: 'true')<#list stage.environmentVariables! as environmentVariable>,containerEnvVar(key: '${environmentVariable.key}', value: '${environmentVariable.value}')</#list>
            ],
            image: '${stage.buildEnvironment!}',
            name: 'jnlp',
            privileged: true,
            resourceLimitCpu: '',
            resourceLimitMemory: '',
            resourceRequestCpu: '',
            resourceRequestMemory: '',
            ttyEnabled: true,
            workingDir: '/home'
        )
    ],
    inheritFrom: '',
    label: "build-${r'${label}'}",
    name: "build-${r'${label}'}",
    nodeSelector: 'HarmonyCloud_Status=E',
    serviceAccount: '',
    volumes: [
    <#if imageBuildStages?size !=0>
        <#list imageBuildStages as tmpStage>
        configMapVolume(configMapName: '${tmpStage.dockerfileId}', mountPath: '${"/opt/dockerfile"+tmpStage.id}')<#if (tmpStage_has_next || stage.dependences!?size>0)>,</#if></#list>
    </#if>
    <#list stage.dependences! as dependence>
        persistentVolumeClaim(claimName: '${dependence.pvName!}', mountPath: '${dependence.mountPath!}', readOnly: false)<#if dependence_has_next>,</#if>
    </#list>
    <#if (imageBuildStages?size>0 || stage.dependences!?size>0)>,</#if>
        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
    ],
    imagePullSecrets: ['${secret}'],
    workingDir: '/home/build',
    workspaceVolume: emptyDirWorkspaceVolume(false)
) {

    node("build-${r'${label}'}"){
</#if>
        httpRequest url:"${apiUrl!}/rest/openapi/cicd/stageSync?id=${stage.id!}&amp;buildNum=${r'${currentBuild.number}'}&amp;dateTime=${r'${dateTime}'}",quiet: true,timeout: ${timeout}
    <#if (stageList?size>0 && stage.stageOrder == 1 && buildInPod! == false)>
        node('master'){
    </#if>
        stage('${stage.stageName}-${stage.id}'){
<#if stage.repositoryType! == "git">
            <![CDATA[checkout([$class: 'GitSCM', branches: [[name: '${stage.repositoryBranch!}']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '${stage.id}', url: '${stage.repositoryUrl}']]])]]>
<#elseif stage.repositoryType! == "svn">
            <![CDATA[checkout([$class: 'SubversionSCM',  locations: [[credentialsId: '${stage.id}', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: '${stage.repositoryUrl}']]])]]>
</#if>
<#if stage.stageTemplateType == 1>
            <#if stage.imageTagType == '0'>
            tag${stage.stageOrder!} = dateTime
            </#if>
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'harbor', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']])
            { sh 'docker login ${harborAddress!} --username=$USERNAME --password=$PASSWORD' }
            <#if stage.dockerfileType == 2>
            sh "cp -r ${'/opt/dockerfile'+stage.id} ./dockerfile@tmp${stage.id}"
            </#if>
            sh "docker build --no-cache <#if stage.dockerfileType == 1> -f ./${stage.dockerfilePath}</#if><#if stage.dockerfileType == 2> -f dockerfile@tmp${stage.id}/<#list dockerFileMap as key, value><#if key == stage.stageOrder>${value.name}</#if></#list></#if> -t ${harborAddress!}/${stage.harborProject!}/${stage.imageName!}:$tag${stage.stageOrder!} ."
            sh "docker push ${harborAddress!}/${stage.harborProject!}/${stage.imageName!}:$tag${stage.stageOrder!}"

</#if>
<#if stage.stageTemplateType == 2>
            httpRequest url:"${apiUrl!}/rest/openapi/cicdjobs/stages/${stage.id!}?buildNum=${r'${currentBuild.number}'}",consoleLogResponseBody: true, timeout: ${timeout}
</#if>
<#if (stage.stageTemplateType == 7 || stage.stageTemplateType == 8)>
            httpRequest url:"${apiUrl!}/rest/openapi/cicdjobs/stages/${stage.id!}?buildNum=${r'${currentBuild.number}'}",consoleLogResponseBody: true, timeout: ${timeout}
</#if>
<#if (stage.command!?size>0)>
    sh '''<#list stage.command! as command><![CDATA[${command}]]>
    </#list>'''
</#if>

        }
</#list>
<#if (stageList?size>0 && buildInPod! == true)>
    }
}
<#elseif (stageList?size>0 && buildInPod! == false )>
    }
</#if>
}finally{
    httpRequest url:"${apiUrl!}/rest/openapi/cicd/postBuild?id=${job.id!}&amp;buildNum=${r'${currentBuild.number}'}",quiet: true,timeout: ${timeout}
}