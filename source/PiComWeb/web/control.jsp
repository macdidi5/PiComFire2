<html>
    <head>
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
            var ref = firebase.database().ref();
            <%
                String t = "ref.child(\"control/GPIO_%02d\").on(\"value\", function(snapshot) {%n"
                        + "var image = document.getElementById('status_%<02d');%n"
                        + "if (snapshot.val().toString().match(\"true\")) {%n"
                        + "image.src = \"img/high.png\";%n" 
                        + "} else {%n"
                        + "image.src = \"img/low.png\";%n"
                        + "}%n"
                        + "});";
                
                for (int i = 0; i <= 16; i++) {
                    out.println(String.format(t, i));
                }
            %>
                
            function clickStatus(gpio) {
                var image = document.getElementById("status_" + gpio);
                var ref = firebase.database().ref('control/GPIO_' + gpio);
                
                if (image.src.toString().indexOf("high") === -1) {
                    ref.set(true);
                }
                else {
                    ref.set(false);
                }
            }
        </script>

        <div class="container">
            <div class="main">
                <jsp:include page="/WEB-INF/include/banner.jsp">
                    <jsp:param name="subTitle" value="Status & Control" />
                </jsp:include>

                <div class="content">
                    <div class="item">
                        <table border='1' bordercolor='#AAA' cellspacing='2' cellpadding='2'>
                            <tr>
                                <th>GPIO Pin</th>
                                <th>Status</th>
                                <th>GPIO Pin</th>
                                <th>Status</th>
                            </tr>
                        <%
                            String gpioName = "GPIO_%02d";
                            String gpioStatus = "<img id=\"status_%02d\" OnMouseOver=\"this.style.cursor='pointer';\" src=\"img/pending.png\" onclick=\"clickStatus('%<02d')\"/>";
                            int pinNum = 0;
                            
                            for (int i = 0; i < 9; i++) {
                                out.println("<tr>");
                                out.println("<td>" + String.format(gpioName, i) + "</td>");
                                out.println("<td>" + String.format(gpioStatus, i) + "</td>");
                                
                                if (i + 9 < 17) {
                                    out.println("<td>" + String.format(gpioName, i + 9) + "</td>");
                                    out.println("<td>" + String.format(gpioStatus, i + 9) + "</td>");
                                }
                                out.println("<tr>");
                            }
                        %>
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
