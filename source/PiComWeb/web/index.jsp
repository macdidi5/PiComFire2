<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="default.css" media="screen"/>
        <title>Welcome</title>
    </head>
    <body>  
        <div class="container">
            <div class="main">
                <jsp:include page="/WEB-INF/include/banner.jsp">
                    <jsp:param name="subTitle" value="Welcome" />
                </jsp:include>

                <div class="content">
                    <div class="item">
                        <h1>Welcome to PiCommander Web Client!</h1>
                        <a href='https://play.google.com/store/apps/details?id=net.macdidi5.picomfire&hl=zh-TW' target="_blank">
                            <img src="img/qrcode.png" width="500" height="500"/>
                        </a>
                    </div>
                </div>
                <jsp:include page="/WEB-INF/include/function.jsp" />
                <div class="clearer"><span></span></div>
            </div>
            <%@ include file="/WEB-INF/include/footer.jsp"%>
        </div>            
    </body>
</html>
