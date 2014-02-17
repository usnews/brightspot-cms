<%@ page import="com.psddev.dari.util.JsonProcessor" %>
<%@ page import="com.psddev.dari.db.UploadProgress" %>
<%@ page import="com.psddev.dari.util.UploadProgressListener" %>
<%
JsonProcessor jsonProcessor=new JsonProcessor();
String uploadProgressKey = UploadProgressListener.Static.getUploadProgressUniqueKey(request);
//If action parameter is passed as delete..delete upload progress data..else retrieve and return it
if (request.getParameter("action") != null  && request.getParameter("action").equals("delete") ) {
   if (uploadProgressKey != null) 
   UploadProgress.Static.delete(uploadProgressKey);
   return;
}
UploadProgress uploadProgress=null;
if (uploadProgressKey != null ) {
uploadProgress=UploadProgress.Static.find(uploadProgressKey);
}
if (uploadProgress == null ) uploadProgress= new UploadProgress();
out.println(jsonProcessor.generate(uploadProgress));
%>

