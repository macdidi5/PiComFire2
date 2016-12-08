<html>
    <head>        
        <script>var CANV_GAUGE_FONTS_PATH = 'fonts'</script>
	<script src="gauge.js"></script>        

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="default.css" media="screen"/>
        <title>Status</title>

        <script src="https://www.gstatic.com/firebasejs/3.1.0/firebase.js"></script>
        <script src="https://www.gstatic.com/firebasejs/3.1.0/firebase-app.js"></script>
        <script src="https://www.gstatic.com/firebasejs/3.1.0/firebase-database.js"></script>
        
        <script>
          var config = {
            apiKey: "AIzaSyCzA7lBdf3gKhNsjYwFeJDxO0WlvGNr_fM",
            authDomain: "picom.firebaseapp.com",
            databaseURL: "https://picom.firebaseio.com",
            storageBucket: "project-3995662155652230549.appspot.com",
          };
          
          firebase.initializeApp(config);
        </script>
    </head>
    <body>
        <script>
            var tempValue = 0;            
            firebase.database().ref('monitor/m001').on('value', function(snapshot) {
                tempValue = snapshot.val();
                document.getElementById("helloTemp").innerHTML = snapshot.val().toString();
            });
            
            var humValue = 0;            
            firebase.database().ref('monitor/m002').on('value', function(snapshot) {
                humValue = snapshot.val();
                document.getElementById("helloHum").innerHTML = snapshot.val().toString();
            });            
        </script>

        <div class="container">
            <div class="main">
                <jsp:include page="/WEB-INF/include/banner.jsp">
                    <jsp:param name="subTitle" value="Monitor" />
                </jsp:include>

                <div class="content">
                    <div class="item">
                        <table border='0' cellspacing='6' cellpadding='6'>
                            <tr>
                                <td>
                                    <canvas id="temp_gauge"
                                            data-title="Temperature"
                                            width="240" 
                                            height="240"
                                            data-type="canv-gauge"
                                            data-circles-outervisible="true"
                                            data-circles-middlevisible="true"
                                            data-circles-innervisible="true"
                                            data-highlights="10 20 #F9FBE7, 20 30 #F0F4C3, 30 40 #E6EE9C, 40 50 #DCE775, 50 60 #D4E157, 60 70 #CDDC39, 70 80 #C0CA33, 80 90 #AFB42B, 90 100 #9E9D24"
                                            data-onready="setInterval( function() { 
                                                Gauge.Collection.get('temp_gauge').setValue(tempValue);
                                            }, 500);">
                                    </canvas>                                    
                                </td>
                                <td>
                                    <canvas id="hum_gauge"
                                            data-title="Humidity"
                                            width="240" 
                                            height="240"
                                            data-type="canv-gauge"
                                            data-circles-outervisible="true"
                                            data-circles-middlevisible="true"
                                            data-circles-innervisible="true"
                                            data-highlights="10 20 #F9FBE7, 20 30 #F0F4C3, 30 40 #E6EE9C, 40 50 #DCE775, 50 60 #D4E157, 60 70 #CDDC39, 70 80 #C0CA33, 80 90 #AFB42B, 90 100 #9E9D24"
                                            data-onready="setInterval( function() { 
                                                Gauge.Collection.get('hum_gauge').setValue(humValue);
                                            }, 500);">
                                    </canvas>                                    
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                <jsp:include page="/WEB-INF/include/function.jsp" />
                <div class="clearer"><span></span></div>
            </div>
            <%@ include file="/WEB-INF/include/footer.jsp"%>
        </div>            
    </body>
</html>
