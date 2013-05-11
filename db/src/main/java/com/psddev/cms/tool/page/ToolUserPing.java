package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/toolUserPing")
@SuppressWarnings("serial")
public class ToolUserPing extends HttpServlet {

    @Override
    protected void service(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        @SuppressWarnings("resource")
        ToolPageContext page = new ToolPageContext(getServletContext(), request, response);
        Map<String, Object> jsonResponse = new HashMap<String, Object>();

        page.getResponse().setContentType("application/json");
        jsonResponse.put("status", page.getUser() != null ? "OK" : "ERROR");
        page.write(ObjectUtils.toJson(jsonResponse));
    }
}
