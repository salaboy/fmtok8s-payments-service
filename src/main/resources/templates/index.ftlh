<!doctype html>
<html class="no-js" lang="">

<head>
    <meta charset="utf-8">
    <title></title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="manifest" href="site.webmanifest">
    <link rel="apple-touch-icon" href="icon.png">
    <!-- Place favicon.ico in the root directory -->
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/main.css">
    <link href="https://fonts.googleapis.com/css?family=Rubik:300,400&display=swap" rel="stylesheet">


    <meta name="theme-color" content="#E7E7EA">
</head>

<#--<body data-gr-c-s-loaded="true">-->
<#--<!--[if IE]>-->
<#--<p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="https://browsehappy.com/">upgrade-->
<#--    your browser</a> to improve your experience and security.</p>-->
<#--<![endif]&ndash;&gt;-->

<#--<!-- Add your site or application content here &ndash;&gt;-->
<#--<div class="block header">-->
<#--    <div class="header__image">-->
<#--        <img src="img/kubecon-logo.png" width="35">-->
<#--    </div>-->
<#--    <div class="header__title">-->
<#--        <h1>Payments v0.0.0 </h1>-->
<#--    </div>-->

<#--</div>-->
<#--<div class="bottom-blocks">-->
<#--    <div class="main-title">-->
<#--        <h2>Payments</h2>-->
<#--        <h3>Do not fresh, payment is being processed.</h3>-->
<#--        <p>Session ID: ${sessionId}</p>-->
<#--        <p>Reservation ID: ${reservationId}</p>-->
<#--        <p>Payment Processed??: ${status?string('yes', 'no')}</p>-->
<#--    </div>-->

<#--</div>-->



<#--<script src="js/vendor/modernizr-3.7.1.min.js"></script>-->
<#--<script src="https://code.jquery.com/jquery-3.4.1.min.js"-->
<#--        integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>-->
<#--<script>window.jQuery || document.write('<script src="js/vendor/jquery-3.4.1.min.js"><\/script>')</script>-->
<#--<script src="js/plugins.js"></script>-->
<#--<script type="text/javascript">-->
<#--    setTimeout(function() {-->
<#--        location.reload();-->
<#--    }, 1000);-->

<#--</script>-->


<#--</body>-->
<body class="bank">
<div class="logo">
    Your Bank
</div>
<div class="bank-body">
    <div id="status" class="lds-ellipsis"><div></div><div></div><div></div><div></div></div>
    <div id="statusText" class="copy">
        <h4>Processing your payment</h4>
        <i>Please wait a minute. Don't leave this page</i>
        <br/>
        Session ID: ${sessionId}
        <br/>
        Reservation ID: ${reservationId}

    </div>


</div>

<script src="js/vendor/modernizr-3.7.1.min.js"></script>
<script src="https://code.jquery.com/jquery-3.4.1.min.js"
        integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
<script>window.jQuery || document.write('<script src="js/vendor/jquery-3.4.1.min.js"><\/script>')</script>
<script src="js/plugins.js"></script>
<script type="text/javascript">
    setInterval(function() {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "/api/${reservationId}", true);

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                console.log(xhr.response);
                var ready = xhr.response;
                if(ready === "true"){
                    document.getElementById("status").innerHTML = "";
                    document.getElementById("status").className = "congrats";
                    var paymentApprovedh4 = document.createElement("h4");
                    paymentApprovedh4.innerHTML="Payment Approved";
                    var info = document.createElement("i");
                    info.innerHTML = "Thanks for your purchase";
                    document.getElementById("statusText").innerHTML = "";
                    document.getElementById("statusText").appendChild(paymentApprovedh4);
                    document.getElementById("statusText").appendChild(info);
                }
            }
        }
        xhr.send();
    }, 1000);

</script>
</body>

</html>