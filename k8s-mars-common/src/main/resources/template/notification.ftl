<!doctype html>
<head>
    <meta charset="utf-8">
    <title>通知</title>
    <style type="text/css">
        .box-content{
            width: 80%;
            margin: 20px auto;
            max-width: 800px;
            min-width: 600px;
        }
        .box-content .header-tip{
            font-size: 12px;
            color: #aaa;
            text-align: right;
            padding-right: 25px;
            padding-bottom: 10px;
        }
        .box-content .footer-tip{
            font-size: 12px;
            color: #aaa;
            text-align: left;
            padding-left: 30px;
            padding-top: 10px;
        }
        .box-content .info-top{
            padding: 15px 25px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            background: -webkit-linear-gradient(30deg, #49a9e1 , #43cdef);
            background: -o-linear-gradient(30deg, #49a9e1 , #43cdef);
            background: -moz-linear-gradient(30deg, #49a9e1 , #43cdef);
            background: linear-gradient(30deg, #49a9e1 , #43cdef);
            color: #fff;
            overflow: hidden;
            line-height: 32px;
        }
        .box-content .info-top img{
            float: left;
            margin: 0 10px 0 0;
        }
        .box-content .info-wrap{
            background:#fff;
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
            border:1px solid #ddd;
            overflow: hidden;
            padding: 15px 15px 20px;
        }
        .box-content .info-wrap .title{
            color: #08c;
            height:10px;
            border-bottom: 1px solid #e0e0e0;
            position:relative;
            margin-bottom:15px;
        }
        .box-content .info-wrap .title span{
            display:inline-block;
            background:#fff;
            padding:0 8px;
            margin-left:10px;
        }
        .box-content .icon-border{
            display: inline-block;
            margin-right: 5px;
            width:1px;
            height:12px;
            border-left: 4px solid #33c0c3;
        }
        .box-content .info-wrap .tips{
            padding:15px;
        }
        .box-content .info-wrap .tips p{
            list-style: 160%;
            margin: 10px 0;
        }
        .box-content .info-wrap .time{
            text-align: right;
            color: #999;
            padding: 0 15px 15px;
            font-size:14px;
        }
        .box-content .info-wrap .process{
            background: #f8f8f8;
            padding:10px 20px;
        }
        .box-content .info-wrap .process ul,
        .box-content .info-wrap .process li{
            list-style:none;
            padding:0;
            margin:0;
        }
        .box-content .info-wrap .process .detail{
            padding:8px 0px 8px 55px;
            border-bottom:1px solid #e0e0e0;
            overflow:hidden;
            position:relative;
        }
        .box-content .info-wrap .process .items-sub{
            padding-left:45px;
            border-bottom:1px solid #e0e0e0;
        }
        .box-content .info-wrap .process .items-sub .detail:last-child{
            border: none
        }
        .box-content .info-wrap .process .detail li{
            width:50%;
            padding:5px 0;
            float:left;
            font-size:14px;
        }
        .box-content .info-wrap .process .detail li.w100{
            width:100%
        }
        .box-content .info-wrap .process .detail li.w60{
            width:60%
        }
        .box-content .info-wrap .process .detail li.w40{
            width:40%
        }
        .box-content .info-wrap .process .detail li label{
            color:#666;
        }
        .box-content .info-wrap .process .detail .icon-status{
            display:block;
            width:32px;
            height:32px;
            overflow:hidden;
            background:url(cid:icon-status) no-repeat;
            position:absolute;
            top:50%;
            left:10px;
            margin-top:-16px;
        }
        .box-content .info-wrap .process .detail .icon-status.SUCCESS{
            background-position: 0 0px;
        }
        .box-content .info-wrap .process .detail .icon-status.FAILED{
            background-position: 0 -34px;
        }
        .box-content .info-wrap .process .detail .icon-status.FAILURE{
            background-position: 0 -34px;
        }
        .box-content .info-wrap .process .detail .icon-status.NOTBUILT{
            background-position: 0 -68px;
        }
    </style>
</head>
<body>
    <div class="box-content">
        <div class="header-tip">Confidential - Internal Use Only</div>
        <div class="info-top"><img src='cid:icon-info'/>CICD通知</div>
        <div class="info-wrap">
            <div class="tips">
                <p>尊敬的用户：</p>
                <p>&emsp;&emsp;以下是CICD的构建结果，点击<a href="${url}">此处</a>查看详细信息。</p>
            </div>
            <div class="time">${time?string('yyyy-MM-dd HH:mm:ss')}</div>

            <div class="title"><span>构建结果</span></div>
            <div class="process">
                <ul class="detail">
                    <i class="icon-status ${status}"></i>
                    <li class="w60">
                        <label>流程名称：</label>
                        <span>${jobName}</span>
                    </li>
                    <li class="w40">
                        <label>步骤数：</label>
                        <span>${stageBuildList?size}</span>
                    </li>
                    <li class="w60">
                        <label>启动时间：</label>
                        <span>${startTime?string('yyyy-MM-dd HH:mm:ss')}</span>
                    </li>
                    <li class="w40">
                        <label>耗&emsp;时：</label>
                        <span>${duration}</span>
                    </li>
                </ul>
                <div class="items-sub">
                    <#list stageBuildList as stageBuild>
                    <ul class="detail">
                        <i class="icon-status ${stageBuild.status}"></i>
                        <li class="w100">
                            <label>步骤名称：</label>
                            <span>${stageBuild.name}</span>
                        </li>
                        <li>
                            <label>启动时间：</label>
                            <span>${stageBuild.startTime?string('yyyy-MM-dd HH:mm:ss')}</span>
                        </li>
                        <li>
                            <label>耗时：</label>
                            <span>${stageBuild.duration}</span>
                        </li>
                    </ul>
                    </#list>
                </div>
            </div>

        </div>
        <div class="footer-tip">
            &#xA9; 2017 HarmonyCloud Systems, Inc. and/or its affiliated entities
        </div>
    </div>
    </body>
    </html>