<!doctype html>
<head>
    <meta charset="utf-8">
    <title>通知</title>
<body style="color: rgb(102, 102, 102); font-family: Open Sans,Helvetica,Arial,sans-serif; font-size: 14px;">
    <div class="box-content" style="margin: 20px auto; width: 80%; min-width: 600px; max-width: 800px;">
        <div class="header-tip" style="text-align: right; color: rgb(170, 170, 170); padding-right: 25px; padding-bottom: 10px; font-size: 12px;">Confidential - Internal Use Only</div>
        <div class="info-top" style="background: <#if status=="SUCCESS">rgb(70, 190, 140);</#if><#if status=="FAILURE">rgb(250, 50, 4);</#if> padding: 15px 25px; color: rgb(255, 255, 255); line-height: 32px; overflow: hidden; border-top-left-radius: 10px; border-top-right-radius: 10px;">
            <img style="margin: 0px 10px 0px 0px; width: 32px; float: left;" src='cid:icon-info'>
            CICD通知
        </div>
    <div class="info-wrap" style="padding: 15px 15px 20px; border: 1px solid rgb(221, 221, 221); border-image: none; overflow: hidden; border-bottom-right-radius: 10px; border-bottom-left-radius: 10px;">
        <div class="tips" style="padding: 15px;">
              <p style="margin: 10px 0px;">Hi：</p>
              <p style="margin: 10px 0px;">&emsp;&emsp;以下是CICD的构建结果，敬请查看。</p>
              <p style="margin: 10px 0px;">&emsp;&emsp;Below is the build result for CICD, please check.</p>
        </div>
        <div class="time" style="padding: 0px 15px 15px; text-align: right; color: rgb(153, 153, 153);">${time?string('yyyy-MM-dd HH:mm:ss')}</div>
            <div class="title" style="color: #08c;height:10px;border-bottom: 1px solid #e0e0e0;margin-bottom:15px;">
                <span style="display:inline-block;background:#fff;padding:0 8px;margin-left:10px;">构建结果(Build Result)</span>
            </div>
        <div class="process" style="background: #f8f8f8;padding:10px 20px;">
                <ul class="detail" style="list-style:none;margin:0;padding: 8px 0px 8px 10px;border-bottom: 1px solid #e0e0e0;overflow: hidden;">
                    <img style="margin: 10px 10px 100px 0px; width: 32px; height: 33px; overflow:hidden; float: left;" src="cid:<#if status=='SUCCESS'>icon-status-success</#if><#if status=='FAILURE'>icon-status-fail</#if>">
                    <li class="w60" style="list-style:none;margin:0;width:45%;padding:5px 0;float:left;">
                        <label style="width:40%;float:left;color:#888;">租户名称<br>Tenant Name</label>
                        <span style="width:60%;float:left;line-height:36px;color:#333;">${tenantName}</span>
                    </li>
                    <li class="w40" style="list-style:none;padding:0;margin:0;width:45%;padding:5px 0;float:left;">
                        <label style="width:50%;float:left;color:#888;">项目名称<br>Project Name</label>
                        <span style="width:50%;float:left;line-height:36px;color:#333;">${projectName}</span>
                    </li>
                    <li class="w60" style="list-style:none;padding:0;margin:0;width:45%;padding:5px 0;float:left;">
                        <label style="width:40%;float:left;color:#888;">流水线名称<br>Pipeline Name</label>
                        <span style="width:60%;float:left;line-height:36px;color:#333;">${jobName}</span>
                    </li>
                    <li class="w40" style="list-style:none;padding:0;margin:0;width:45%;padding:5px 0;float:left;">
                        <label style="width:50%;float:left;color:#888;">步骤数<br>Number of Stages</label>
                        <span style="width:50%;float:left;line-height:36px;color:#333;">${stageBuildList?size}</span>
                    </li>
                    <li class="w60" style="list-style:none;padding:0;margin:0;width:45%;padding:5px 0;float:left;">
                        <label style="width:40%;float:left;color:#888;">启动时间<br>Start Time</label>
                        <span style="width:60%;float:left;line-height:36px;color:#333;">${startTime?string('yyyy-MM-dd HH:mm:ss')}</span>
                    </li>
                    <li class="w40" style="list-style:none;padding:0;margin:0;width:45%;padding:5px 0;float:left;">
                        <label style="width:50%;float:left;color:#888;">耗时<br>Duration</label>
                        <span style="width:50%;float:left;line-height:36px;color:#333;">${duration}</span>
                    </li>
                </ul>
            <div class="items-sub" style="padding-left:55px;">
            <#list stageBuildList as stageBuild>
                    <ul class="detail" style="list-style:none;padding:0;margin:0;padding: 8px 0px 8px 20px;border-bottom: 1px solid #e0e0e0;overflow: hidden;<#if !stageBuild_has_next>border: none;</#if>">
                        <img style="margin: 10px 10px 50px 0px; width: 32px; height: 33px; overflow:hidden; float: left;" src="cid:<#if stageBuild.status=='SUCCESS'>icon-status-success</#if><#if stageBuild.status=='FAILED'>icon-status-fail</#if><#if stageBuild.status=='NOTBUILT'>icon-status-unfinished</#if>">
                        <li style="list-style:none;padding:0;margin:0;width:80%;padding:5px 0;float:left;">
                            <label style="width:25%;float:left;color:#888;">步骤名称<br>Stage Name</label>
                            <span style="line-height:36px;color:#333;">${stageBuild.name}</span>
                        </li>
                        <li style="list-style:none;padding:0;margin:0;width:50%;padding:5px 0;float:left;">
                            <label style="width:40%;float:left;color:#888;">执行结果<br>Result</label>
                            <span style="line-height:36px;color:#333;">${stageBuild.status}</span>
                        </li>
                        <li style="list-style:none;padding:0;margin:0;width:40%;padding:5px 0;float:left;">
                            <label style="width:50%;float:left;color:#888;">耗时<br>Duration</label>
                            <span style="line-height:36px;color:#333;">${stageBuild.duration}</span>
                        </li>
                    </ul>
            </#list>
            </div>
        </div>

    </div>
    <br>
</div>
</body>
</html>