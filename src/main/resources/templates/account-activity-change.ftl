<#--    <link rel="stylesheet" href="/шаблон сайта/css/font-awesome.min.css">-->
<#--    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">-->
<head>
    <#include "header-footer.css">
    <#include "main-activity-change.css">
</head>
<body>
<div class="content">
    <div class="main">
        <div class="header">
            <table>
                <tbody>
                <tr>
                    <th style="width: 690px;height: 180px;">
                        <img src="${path}/${company}.png" style="height: 100px;border-radius: 7px;" alt="">
                    </th>
                </tr>
                </tbody>
            </table>
        </div>
        <hr>
        <div class="section">
            <div class="menu">
                <h2>Hello, ${userName}</h2>
                <span style="font-weight: 400; font-size: 18px;">A new activity was initiated on your account</span>
            </div>
            <p>Action: <strong>${action}</strong></p>
            <p>Status: <strong>${actionStatus}</strong></p>
            <p>Contact our support team if you have any questions or concerns. <br><a href="#">Ask us any question</a>
            </p>
            <p>Our best,<br> The Customer Engagement App team</p>
        </div>
        <div class="footer" style="display: flex;background-color: #1852b3;">
            <table>
                <tbody>
                <tr>
                    <th>
                        <img class="icons" src="${path}/phone.svg" style="fill:white; opacity: 0.8; height: 20px;" alt="">
                        <#--                <i class="fa fa-phone" style="color: #ffffffa8;"></i>-->
                    </th>
                    <th style="">
                        <a class="contact" href="tel: +7778 312 55 77" style="color: #ffffff;opacity: 0.8;font-weight: 500;">+7 778 312 55 77</a>
                    </th>
                    <th>
                        <img class="icons" src="${path}/mail.svg" style="fill:white; opacity: 0.8;height: 20px;" alt="">
                        <#--                <i class="fa fa-envelope-o" style="color: #ffffffa8;"></i>-->
                    </th>
                    <th style="">
                        <a class="contact" href="#" style="color: #ffffff;opacity: 0.8;font-weight: 500;">info@mdsp.kz</a>
                    </th>
                    <th style="width: 300px"></th>
                    <th>
                        <a class="icons" href="https://www.instagram.com/mds.program/" style="text-decoration: none;">
                            <img src="${path}/instagram.svg" style="fill: white; opacity: 0.8;height: 20px;"
                                 alt=""></a>
                    </th>
                    <th>
                        <a class="icons" href="https://www.youtube.com/channel/UConDWXefPqbxqbw3-KBCMNA" style="text-decoration: none;">
                            <img src="${path}/youtube.svg" style="fill: white;opacity: 0.8; height: 20px;" alt=""></a>
                    </th>
                </tr>
                </tbody>
            </table>
            <#--                <a href="https://www.instagram.com/mds.program/"><i class="fa fa-instagram" style="color: #ffffffa8;"></i></a>-->
            <#--                <a href="https://www.youtube.com/channel/UConDWXefPqbxqbw3-KBCMNA"><i class="fa fa-youtube-play" style="color: #ffffffa8;"></i></a>-->
        </div>
    </div>
</div>
</body>


