<!doctype html>
<head>
    <meta charset="utf-8">
    <title>通知</title>
    <style type="text/css">
        .time{
            padding-left:250px
        }
        .jobDetail{
            padding-left:50px
        }
        .stageDetail{
            padding-left:70px
        }
        .title{
            padding-left:30px
        }
        th{
            text-align:left
        }
    </style>
</head>
<body>
<div class="box-content">
    <div class="header-tip">Confidential - Internal Use Only</div>
    <div class="info-top">CICD通知(CICD Notification)</div>
    <div class="info-wrap">
        <div class="tips">
            <p>Hi：</p>
            <p>&emsp;&emsp;以下是CICD的构建结果，敬请查看。</p>
            <p>&emsp;&emsp;Below is the build result for CICD, please check.</p>
        </div>
        <div class="time">${time?string('yyyy-MM-dd HH:mm:ss')}</div>
        <br>
        <br>
        <div class="title"><span>构建结果(Build Result)</span></div>
        <div class="process">
            <br>
            <table class="jobDetail">
                <tr><th><label>流程名称(Pipeline Name)：</label></th><td><span>${jobName}</span></td>
                </tr>
                <tr><th><label>执行结果(Result)：</label></th><td><span>${status}</span></td>
                </tr>
                <tr><th><label>步骤数(Number of Stages)：</label></th><td><span>${stageBuildList?size}</span></td>
                </tr>
                <tr><th><label>启动时间(Start Time)：</label></th><td><span>${startTime?string('yyyy-MM-dd HH:mm:ss')}</span></td>
                </tr>
                <tr><th><label>耗时(Duration)：</label></th><td><span>${duration}</span></td>
                </tr>
            </table>
            <div class="items-sub">
            <#list stageBuildList as stageBuild>
                <br>
                <table class="stageDetail">
                    <tr><th><label>步骤名称(Stage Name)：</label></th><td><span>${stageBuild.name}</span></td>
                    </tr>
                    <tr><th><label>执行结果(Result)：</label></th><td><span>${stageBuild.status}</span></td>
                    </tr>
                    <tr><th><label>耗时(Duration)：</label></th><td><span>${stageBuild.duration}</span></td>
                    </tr>
                </table>
            </#list>
            </div>
        </div>

    </div>
    <br>
    <div class="footer-tip">
        &#xA9; 2017 HarmonyCloud Systems, Inc. and/or its affiliated entities
    </div>
</div>
</body>
</html>