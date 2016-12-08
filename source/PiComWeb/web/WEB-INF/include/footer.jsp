<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.*" %>

<div class="footer">
    &copy; 2016  
    伺服器:
    <%= 
        InetAddress.getLocalHost().getHostAddress() + 
            ":" + request.getLocalPort() 
    %>
    <a href="http://tw.linkedin.com/in/macdidi5/zh-tw" target='_blank'>
        關於作者
    </a>
</div>