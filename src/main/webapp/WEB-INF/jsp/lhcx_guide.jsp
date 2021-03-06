 <%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
    <style>
        *{
            font-family: Aharoni;
            font-size: 0.9rem;
        }
        .header{
            text-align: center;
        }
        .header img{
            width: 2rem;
            height: 2rem;
            display: block;
            margin-left: calc(50% - 1rem);
            margin-bottom: .5rem;
        }
        .header span{
            color: #fe9905;
        }
        .main p{
            margin-top: 2rem;
        }
        .main .title{
            font-size: 1.2rem;
            font-weight: bold;
            border-left: 5px solid #fe9905;
            padding-left: 1.5rem;
        }
        .main ul,
        .main ul li{
            list-style: none;
            margin: 0;
            padding: 0.5rem 0;
        }
        .main ul{
            margin-top: .5rem;
        }
        .main ul li img{
            margin-top: 0.5rem;
            text-align: center;
            display: block;
            width: 14rem;
            height: 24rem;
            margin-left: calc(50% - 7rem);
        }
        .footer{
            margin-top: 2rem;
        }
        .footer span{
            display: block;
            text-align: center;
            font-size: .8rem;
        }
        .footer .footerTitle{
            color: #fe9905;
        }
    </style>

<div class="header">
    <img src="/resource/images/lhcx_role_logo.png" alt="">
    <span>来回拼车使用指南</span>
</div>
<div class="main">
    <p>&#x3000;&#x3000;通过来回出行APP，乘客可以直接发布出行信息等待车主接单，而车主只需通过注册审核，接下来等待听单即可。那么如何玩转来回出行APP呢？以下是详细操作流程：</p>
    <div class="driver">
        <span class="title">如果您是车主：</span>
        <ul>
            <li>
                <span>&#x3000;&#x3000;第一步，点击“开始听单”，等待乘客发单并且需要在车主注册成功的条件下。</span>
                <img src="/resource/images/driver_start.jpg" alt="">
            </li>
            <li>
                <span>&#x3000;&#x3000;第二步，当有乘客在附近时自动推送订单，点击“立即抢单”，同时乘客端会显示你的订单已被抢。</span>
                <img src="/resource/images/driver_hear.jpg" alt="">
            </li>
            <li>
                <span>&#x3000;&#x3000;第三步，联系乘客，询问乘客所在位置,到达乘客位置，点击“发车”去向乘客要到达的目的地。</span>
                <img src="/resource/images/driver_setout.jpg" alt="">
            </li>
            <li>
                <span>&#x3000;&#x3000;第四步，行驶到达乘客的目的地后，点击“已到达”。</span>
                <img src="/resource/images/driver_arrive.jpg" alt="">
            </li>
            <li>
                <span>&#x3000;&#x3000;第五步，乘客选择自己的支付方式，支付后车主会有一条推送的消息，显示乘客支付成功。</span>
                <img src="/resource/images/driver_complete.jpg" alt="">
            </li>
        </ul>
    </div>
    <div class="panssenger">
        <span class="title">如果您是乘客：</span>
        <ul>
            <li>
                <span>&#x3000;&#x3000;第一步，选择您想要出发路线。</span>
                <img src="/resource/images/passenger_setout.png" alt="">

            </li>
            <li>
                <span>&#x3000;&#x3000;第二步，点击“预约转车”后等待车主抢单。</span>
                <img src="/resource/images/passenger_release.png" alt="">
                <img src="/resource/images/passenger_waiting.png" alt="">
            </li>
            <li>
                <span>&#x3000;&#x3000;第三步，车主抢单之后，告诉车主所在位置，等待车主到来。</span>
                <img src="/resource/images/passenger_orders.png" alt="">
            </li>
            <li>
                <span>&#x3000;&#x3000;第四步，上车后，等到达目的地选择任意一种方式去支付</span>
                <img src="/resource/images/passenger_aboard.png" alt="">
            </li>
        </ul>
    </div>
</div>
<div class="footer">
    <span class="footerTitle">河南来回网络科技有限公司</span>
    <span class="copy">Copyright &copy; 2016-2017 Laihuipinche.</span>
    <span class="rights">All Rights Reserved.</span>
</div>
