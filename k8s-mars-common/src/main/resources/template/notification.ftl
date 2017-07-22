<!doctype html>
<head>
    <meta charset="utf-8">
    <title>通知</title>
    <style type="text/css">
        body{
            color: #666;
            font-size: 14px;
            font-family: "Open Sans",Helvetica,Arial,sans-serif;
        }
        dl,dt,dd,ul,li{
            margin: 0;
            padding:0;
            list-style: none;
        }
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
<#if status! == "FAILURE">
            background: -webkit-linear-gradient(30deg, #fa3204 , #ff6d01);
            background: -o-linear-gradient(30deg, #fa3204 , #ff6d01);
            background: -moz-linear-gradient(30deg, #fa3204 , #ff6d01);
            background: linear-gradient(30deg, #fa3204 , #ff6d01);
</#if>
<#if status! == "SUCCESS">
            background: -webkit-linear-gradient(30deg, #15fa4e, #9eff51);
            background: -o-linear-gradient(30deg, #15fa4e , #9eff51);
            background: -moz-linear-gradient(30deg, #15fa4e , #9eff51);
            background: linear-gradient(30deg, #15fa4e , #9eff51);
</#if>
            color: #fff;
            overflow: hidden;
            line-height: 32px;
        }
        .box-content .info-top img{
            float: left;
            margin: 0 10px 0 0;
            width: 32px;
        }
        .box-content .info-wrap{
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
            border:1px solid #ddd;
            overflow: hidden;
            padding: 15px 15px 20px;
        }
        .box-content .info-wrap .title{
            font-size: 14px;
            color: #333;
            padding-bottom: 5px;
        }
        .box-content .icon-border{
            display: inline-block;
            margin-right: 5px;
            width:1px;
            height:12px;
            border-left: 4px solid #33c0c3;
        }
        .box-content .info-wrap .limit{
            background: #f8f8f8;
            padding:15px 20px;
        }
        .box-content .info-wrap .limit dl{
            padding: 6px;
        }
        .box-content .info-wrap .limit dl dt{
            display: inline-block;
            width: 90px;
            text-align: right;
            color: #888;
        }
        .box-content .info-wrap .limit dl dd{
            display: inline-block;
            color: #333;
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
        }
        .box-content .info-wrap table.list{
            width: 100%;
            border-collapse: collapse;
            border-top:1px solid #eee;
        }
        .box-content .info-wrap table.list td{
            padding:6px;
            line-height: 150%;
            border-bottom: 1px solid #eee;
        }
        .box-content .info-wrap table.list td:first-child{
            padding-left: 15px;
        }
        .box-content .info-wrap table.list td:last-child{
            padding-right: 15px;
        }
        .box-content .info-wrap table.list td p{
            margin: 0;
            padding: 2px 0;
        }
        .box-content .info-wrap table.list thead tr{
            background: #fafafa;
            color: #333;
        }
        .box-record{
            border-bottom: 1px solid #d6e1ee;
            display: flex;
            flex-wrap: wrap;
            padding: 10px 50px 10px 20px;
            box-sizing: border-box;
            position: relative;
        }
    </style>
</head>
<body>
<div class="box-content">
    <div class="header-tip">Confidential - Internal Use Only</div>
    <div class="info-top"><img src="cid:icon-alarm" />CICD通知</div>
    <div class="info-wrap">
        <div class="tips">
            <p>尊敬的用户：</p>
            <p>以下是CICD的构建结果，点击<a href="${url}">此处</a>查看详细信息。</p>
        </div>
        <div class="time">${time!?datetime}</div>
        <div class="limit">
            <div class="title"><span class="icon-border"></span>构建结果</div>
            <dl>
                <dt>流程名称：</dt>
                <dd>
                    <span>${jobName!}</span>
                </dd>
            </dl>
            <dl>
                <dt>状态：</dt>
                <dd>${status!}</dd>
            </dl>
            <dl>
                <dt>构建时间：</dt>
                <dd>${startTime!?datetime}</dd>
            </dl>
            <dl>
                <dt>用时：</dt>
                <dd>${duration!}</dd>
            </dl>
        </div>
        <br>
        <table class="list">
            <div class="title"><span class="icon-border"></span>步骤信息</div>
            <thead>
            <tr>

            </tr>
            </thead>
            <tbody>
            <tr>

            </tr>
            <tr>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="footer-tip">
        © 2017 HarmonyCloud Systems, Inc. and/or its affiliated entities
    </div>
</div>
</body>
</html>