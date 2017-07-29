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
                    <defaultValue></defaultValue>
                </hudson.model.StringParameterDefinition>
                </#if></#list>
            </parameterDefinitions>
        </hudson.model.ParametersDefinitionProperty>
        <#if job.trigger! == true>
        <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
            <triggers>
                <#if job.pollScm! == true>
                <hudson.triggers.SCMTrigger>
                    <spec>${job.cronExpForPollScm}</spec>
                    <ignorePostCommitHooks>false</ignorePostCommitHooks>
                </hudson.triggers.SCMTrigger>
                </#if>
            </triggers>
        </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
        </#if>
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