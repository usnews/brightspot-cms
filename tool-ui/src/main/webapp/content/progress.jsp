<%@ page import="java.util.List" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%@ page import="com.psddev.dari.util.UploadProgressListener" %>
<%@ page import="com.psddev.dari.util.MultipartRequestFilter" %>
<%@ page import="com.psddev.dari.util.MultipartRequest" %>
<%@ page import="com.psddev.dari.util.JsonProcessor" %>
<%@ page import="com.psddev.dari.util.UploadInfo" %>
<%
//response.setContentType("text/html");
UploadInfo ui= new UploadInfo();
JsonProcessor jsonProcessor=new JsonProcessor();
//PrintWriter out = response.getWriter();
//HttpSession session = request.getSession(true);
if (session == null) {
    ui.setMessage("Sorry, session is null");
    out.println(jsonProcessor.generate(ui));
    return;
    //out.println("Sorry, session is null"); // just to be safe
}

UploadProgressListener listener= MultipartRequest.Static.getListenerFromSession(request);
if (listener == null) {
    ui.setMessage("Progress listener is null");
    out.println(jsonProcessor.generate(ui));
    return;
}
UploadInfo ui2= new UploadInfo(listener);
out.println(jsonProcessor.generate(ui2));
%>

