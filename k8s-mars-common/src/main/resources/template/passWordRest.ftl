<!doctype html>
<head>
    <meta charset="utf-8">
    <title>通知</title>
    <style type="text/css">
        .box-content {
            width: 80%;
            margin: 20px auto;
            max-width: 800px;
            min-width: 600px;
        }

        .box-content .header-tip {
            font-size: 12px;
            color: #aaa;
            text-align: right;
            padding-right: 25px;
            padding-bottom: 10px;
        }

        .box-content .footer-tip {
            font-size: 12px;
            color: #aaa;
            text-align: left;
            padding-left: 30px;
            padding-top: 10px;
        }

        .box-content .info-top {
            padding: 15px 25px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            background: -webkit-linear-gradient(30deg, #49a9e1, #43cdef);
            background: -o-linear-gradient(30deg, #49a9e1, #43cdef);
            background: -moz-linear-gradient(30deg, #49a9e1, #43cdef);
            background: linear-gradient(30deg, #49a9e1, #43cdef);
            color: #fff;
            overflow: hidden;
            line-height: 32px;
        }

        .box-content .info-top img {
            float: left;
            margin: 0 10px 0 0;
        }

        .box-content .info-wrap {
            background: #fff;
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
            border: 1px solid #ddd;
            overflow: hidden;
            padding: 15px 15px 20px;
        }

        .box-content .info-wrap .title {
            color: #08c;
            height: 10px;
            border-bottom: 1px solid #e0e0e0;
            position: relative;
            margin-bottom: 15px;
        }

        .box-content .info-wrap .title span {
            display: inline-block;
            background: #fff;
            padding: 0 8px;
            margin-left: 10px;
        }

        .box-content .icon-border {
            display: inline-block;
            margin-right: 5px;
            width: 1px;
            height: 12px;
            border-left: 4px solid #33c0c3;
        }

        .box-content .info-wrap .tips {
            padding: 15px;
        }

        .box-content .info-wrap .tips p {
            list-style: 160%;
            margin: 10px 0;
        }

        .box-content .info-wrap .time {
            text-align: right;
            color: #999;
            padding: 0 15px 15px;
            font-size: 14px;
        }

        .box-content .info-wrap .process {
            background: #f8f8f8;
            padding: 10px 20px;
        }

        .box-content .info-wrap .process ul,
        .box-content .info-wrap .process li {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .box-content .info-wrap .process .detail {
            padding: 8px 0px 8px 55px;
            border-bottom: 1px solid #e0e0e0;
            overflow: hidden;
            position: relative;
        }

        .box-content .info-wrap .process .items-sub {
            padding-left: 45px;
            border-bottom: 1px solid #e0e0e0;
        }

        .box-content .info-wrap .process .items-sub .detail:last-child {
            border: none
        }

        .box-content .info-wrap .process .detail li {
            width: 50%;
            padding: 5px 0;
            float: left;
            font-size: 14px;
        }

        .box-content .info-wrap .process .detail li.w100 {
            width: 100%
        }

        .box-content .info-wrap .process .detail li.w60 {
            width: 60%
        }

        .box-content .info-wrap .process .detail li.w40 {
            width: 40%
        }

        .box-content .info-wrap .process .detail li label {
            color: #666;
        }

        .box-content .info-wrap .process .detail .icon-status {
            display: block;
            width: 32px;
            height: 32px;
            overflow: hidden;
            background: url(cid:icon-status) no-repeat;
            position: absolute;
            top: 50%;
            left: 10px;
            margin-top: -16px;
        }

        .box-content .info-wrap .process .detail .icon-status.SUCCESS {
            background-position: 0 0px;
        }

        .box-content .info-wrap .process .detail .icon-status.FAILED {
            background-position: 0 -34px;
        }

        .box-content .info-wrap .process .detail .icon-status.FAILURE {
            background-position: 0 -34px;
        }

        .box-content .info-wrap .process .detail .icon-status.NOTBUILT {
            background-position: 0 -68px;
        }
    </style>
</head>
<body>
<div class="box-content">
    <div class="header-tip">Confidential - Internal Use Only</div>
    <div class="info-top"><img src='cid:icon-info.png'/>密码重置通知</div>
    <div class="info-wrap">
        <div class="tips">
            <p>尊敬的用户<b>${userName}</b>：</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您已经成功重置您的观云台账户密码，重置后密码为<b>${newPassWord}</b>。请您牢记您的密码并尽快删除该邮件，以防账户被他人盗用。</p>
        </div>
        <div class="time">${time?string('yyyy-MM-dd HH:mm:ss')}</div>


    </div>
</div>
</body>
</html>