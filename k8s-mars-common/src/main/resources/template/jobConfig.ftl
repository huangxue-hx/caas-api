<flow-definition plugin="workflow-job@2.11">
    <actions/>
    <description/>
    <keepDependencies>false</keepDependencies>
    <properties>
        <hudson.model.ParametersDefinitionProperty>
            <parameterDefinitions>
                <hudson.model.StringParameterDefinition>
                    <name>tag</name>
                    <description/>
                    <defaultValue></defaultValue>
                </hudson.model.StringParameterDefinition>
                <#list stageList as stage><#if stage.stageTemplateType == 1>
                <hudson.model.StringParameterDefinition>
                    <name>tag${stage.stageOrder}</name>
                    <description/>
                    <defaultValue><#if stage.imageTagType == '1'>${stage.imageBaseTag}<#elseif stage.imageTagType == '2'>${stage.imageTag}</#if></defaultValue>
                </hudson.model.StringParameterDefinition>
                </#if></#list>
                <#if parameterList??>
                    <#list parameterList as parameter>
                        <#if parameter.type == 1>
                            <hudson.model.StringParameterDefinition>
                                <name>${parameter.name}</name>
                                <description><![CDATA[${parameter.description!}]]></description>
                                <defaultValue><![CDATA[${parameter.value}]]></defaultValue>
                            </hudson.model.StringParameterDefinition>
                        <#elseif parameter.type == 2>
                            <hudson.model.ChoiceParameterDefinition>
                                <name>${parameter.name}</name>
                                <description><![CDATA[${parameter.description!}]]></description>
                                <choices class="java.util.Arrays$ArrayList">
                                    <a class="string-array">
                                        <#list parameter.value?split("\n") as value>
                                        <string><![CDATA[${value}]]></string>
                                        </#list>
                                    </a>
                                </choices>
                            </hudson.model.ChoiceParameterDefinition>
                        </#if>
                    </#list>
                </#if>
            </parameterDefinitions>
        </hudson.model.ParametersDefinitionProperty>
        <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
            <triggers>
            <#if (trigger?? && trigger.valid == true)>
                <#if trigger.type == 1>
                    <hudson.triggers.TimerTrigger>
                        <spec>${trigger.cronExp}</spec>
                    </hudson.triggers.TimerTrigger>
                </#if>
                <#if trigger.type == 2>
                <hudson.triggers.SCMTrigger>
                    <spec>${trigger.cronExp}</spec>
                    <ignorePostCommitHooks>false</ignorePostCommitHooks>
                </hudson.triggers.SCMTrigger>
                </#if>
            </#if>
                    <jenkins.triggers.ReverseBuildTrigger>
                        <spec/>
                    <#if (trigger?? && trigger.valid == true && trigger.type == 4)>
                        <upstreamProjects>${triggerJobName!}</upstreamProjects>
                    </#if>
                        <threshold>
                            <name>SUCCESS</name>
                            <ordinal>0</ordinal>
                            <color>BLUE</color>
                            <completeBuild>true</completeBuild>
                        </threshold>
                    </jenkins.triggers.ReverseBuildTrigger>
            </triggers>
        </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>

    </properties>
    <definition class="org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition" plugin="workflow-cps@2.31">
        <script>
${script}
        </script>
        <sandbox>true</sandbox>
    </definition>
    <triggers/>
    <disabled>false</disabled>
</flow-definition>