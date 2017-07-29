import static java.util.UUID.randomUUID
import  org.csanchez.jenkins.plugins.kubernetes.pipeline.PodTemplateAction
def uuid
def label

@NonCPS
def clearTemplateNames() {
    def r = currentBuild.rawBuild
    def action = r.getAction(PodTemplateAction.class);
    if(action) {
        action.names.clear()
    }
}

try{
<#list stageList as stage>
<#if stage.stageTemplateType == 0>
<#if stage.stageOrder != 1>
    }
}
</#if>
uuid = randomUUID() as String
label = uuid.take(8)
podTemplate(
    containers: [
        containerTemplate(
            alwaysPullImage: false,
            args: '',
            command: '',
            envVars: [
                containerEnvVar(key: 'DOCKER_DAEMON_ARGS', value: '--insecure-registry=${harborHost!}')<#list stage.environmentVariables as environmentVariable>,containerEnvVar(key: '${environmentVariable.key}', value: '${environmentVariable.value}')</#list>
            ],
            image: '${harborHost!}/${stage.buildEnvironment!}',
            name: 'jnlp',
            privileged: true,
            resourceLimitCpu: '',
            resourceLimitMemory: '',
            resourceRequestCpu: '',
            resourceRequestMemory: '',
            ttyEnabled: true,
            workingDir: '/home/build'
        )
    ],
    inheritFrom: '',
    label: "build-${r'${label}'}",
    name: "build-${r'${label}'}",
    nodeSelector: '',
    serviceAccount: '',
    volumes: [
        nfsVolume(mountPath: '/root/.m2', readOnly: false, serverAddress: '10.10.101.147', serverPath: '/nfs/m2-pv'),
        nfsVolume(mountPath: '/root/script', readOnly: false, serverAddress: '10.10.101.147', serverPath: '/nfs/buildscript'),
        nfsVolume(mountPath: '/var/lib/docker', readOnly: false, serverAddress: '10.10.101.147', serverPath: '/nfs/docker')
    ],
    workingDir: '/home/build',
    workspaceVolume: emptyDirWorkspaceVolume(false)
) {

    node("build-${r'${label}'}"){
</#if>
<#if stage.stageTemplateType == 1>
    <#if stage.dockerfileType == 2>
        <#list dockerFileMap as key, value>
            <#if key == stage.stageOrder>
        sh 'echo -e "${value.content}">${value.name}'
            </#if>
        </#list>
    </#if>
</#if>
        stage('${stage.stageName}'){
<#if stage.repositoryType! == "git">
            git url:'${stage.repositoryUrl}',credentialsId:'${stage.id}'<#if stage.repositoryBranch??>, branch:'${stage.repositoryBranch!}'</#if>
<#elseif stage.repositoryType! == "svn">
            checkout([$class: 'SubversionSCM',  locations: [[credentialsId: '${stage.id}', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: '${stage.repositoryUrl}']]]) }
</#if>
<#if stage.stageTemplateType == 1>
            withDockerRegistry([credentialsId: 'harbor', url: 'http://${harborHost!}']) {
                sh 'docker build <#if stage.dockerfileType == 1> -f ./${stage.dockerFilePath}</#if><#if stage.dockerfileType == 2> -f <#list dockerFileMap as key, value><#if key == stage.stageOrder>${value.name}</#if></#list></#if> -t ${harborHost!}/${stage.harborProject!}/${stage.imageName!}:$tag${stage.stageOrder!} .'
                sh 'docker push ${harborHost!}/${stage.harborProject!}/${stage.imageName!}:$tag${stage.stageOrder!}'
            }
            //withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'harbor', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']])
            //{ sh 'docker login ${harborHost!} --username=$USERNAME --password=$PASSWORD' }
            //docker.build('${harborHost!}/${stage.harborProject!}/${stage.imageName!}:$tag${stage.stageOrder!}<#if stage.dockerfileType == 1> -f ./${stage.dockerFilePath}</#if><#if stage.dockerfileType == 2> -f <#list dockerFileMap as key, value><#if key == stage.stageOrder>${value.name}</#if></#list></#if>').push()
</#if>
<#if stage.stageTemplateType == 2>
            echo '开始升级'
            httpRequest "${apiUrl!}/rest/openapi/cicd/deploy?stageId=${stage.id!}&amp;buildNum=${r'${currentBuild.number}'}"
</#if>
<#list stage.command as command>
            sh '${command}'
</#list>
        }
        httpRequest "${apiUrl!}/rest/openapi/cicd/stageSync?id=${job.id!}&amp;buildNum=${r'${currentBuild.number}'}&amp;stageOrder=${stage.stageOrder}"
</#list>
<#if (stageList?size>0) >
    }
}
</#if>
}finally{
    clearTemplateNames()
    httpRequest "${apiUrl!}/rest/openapi/cicd/postBuild?id=${job.id!}&amp;buildNum=${r'${currentBuild.number}'}"
}