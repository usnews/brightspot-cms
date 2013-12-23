<%@ page import="com.psddev.dari.util.UploadProgressListener" %>
<%@ page import="com.psddev.dari.util.MultipartRequest" %>
<%@ page import="com.psddev.dari.util.JsonProcessor" %>
<%@ page import="com.psddev.dari.util.UploadInfo" %>
<%
//response.setContentType("text/html");
UploadInfo ui= new UploadInfo();
JsonProcessor jsonProcessor=new JsonProcessor();
if (session == null) {
    ui.setMessage("Sorry, session is null");
    out.println(jsonProcessor.generate(ui));
    return;
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

