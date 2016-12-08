<html>
    <head>        
        <script src="RGraph.common.core.js"></script>
        <script src="RGraph.line.js"></script>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="default.css" media="screen"/>
        <title>Device</title>
        
        <script type="text/javascript">
        <%
            for (int i = 0; i < 7; i++) {
                out.println(String.format("var value%02d = 0;", i));
            }
            
            out.println("window.onload = function () {");
            out.println("var points = 20;");
            out.println("var max = 100;");
            
            for (int i = 0; i < 7; i++) {
                out.println(String.format("var data%02d = new Array(points);", i));
                out.println(String.format("var RG%02d = RGraph;", i));
                out.println(String.format("var obj%02d = new RGraph.Line({", i));
                out.println(String.format("id: 'cvs%02d',", i));
                out.println(String.format("data: data%02d,", i));
                out.println("options: {");
                out.println("gutterLeft: 35,");
                out.println("ymax: max,");
                out.println("tickmarks: 'circle',");
                out.println("linewidth: 1,");
                out.println("shadow: null,");
                out.println("backgroundGridVlines: true,");
                out.println("backgroundGridBorder: true,");
                out.println("backgroundGridColor: '#eee',");
                out.println("color: 'black',");
                out.println("numxticks: 5,");
                out.println("axisColor: '#666',");
                out.println("textColor: '#666',");
                out.println("textSize: 10,");
                out.println("colors: ['red'],");
                out.println("noxaxis: true,");
                out.println("textAccessible: true");
                out.println("}}).draw();");
                out.println(String.format("function draw%02d() {", i));
                out.println(String.format("RG%02d.clear(obj%02d.canvas);", i, i));
                out.println(String.format("obj%02d.original_data[0].shift();", i));
                out.println(String.format("obj%02d.original_data[0].push(value%02d);", i, i));
                out.println(String.format("obj%02d.draw();", i));
                out.println(String.format("setTimeout(draw%02d, 1500);", i));
                out.println("}");
                out.println(String.format("setTimeout(draw%02d, 1500);", i));
            }
            
            out.println("};");
        %>
        </script>
        
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
            <%
                for (int i = 0; i < 7; i++) {
                    out.println(String.format("firebase.database().ref('device/D%02d/name').once('value').then(function(snapshot) {", i));
                    out.println("var dname = snapshot.val();");
                    out.println(String.format("document.getElementById(\"dname%02d\").textContent=dname;", i));
                    out.println("});");
                }
            %>
                
            <%
                for (int i = 0; i < 7; i++) {
                    out.println(String.format("firebase.database().ref('device/D%02d/value').on('value', function(snapshot) {", i));
                    out.println(String.format("value%02d = snapshot.val();", i));
                    out.println(String.format("document.getElementById(\"dvalue%02d\").textContent=value%02d;", i, i));
                    out.println("});");
                }
            %>    
        </script>        
        <div class="container">
            <div class="main">
                <jsp:include page="/WEB-INF/include/banner.jsp">
                    <jsp:param name="subTitle" value="Monitor" />
                </jsp:include>

                <div class="content">
                    <div class="item">
                        <table>
                            <%
                                for (int i = 0; i < 7; i++) {
                                    out.println(String.format("<tr bgcolor=\"#D8D8D8\"><td><h3><span id=\"dname%02d\">---</span></h3></td><td align=\"center\"><h3><span id=\"dvalue%02d\">---</div></h3></td></tr>", i, i));
                                    out.println(String.format("<tr bgcolor=\"#FAFAFA\"><td colspan=\"2\"><canvas id=\"cvs%02d\" width=\"560\" height=\"150\">", i));
                                    out.println("[No canvas support]</canvas></td></tr>");
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
