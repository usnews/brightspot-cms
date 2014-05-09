package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.AuthenticationFilter;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "inlineEditorCookie")
public class InlineEditorCookie extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String userId = request.getParameter("userId");

        if (userId != null) {
            String signature = StringUtils.hex(StringUtils.hmacSha1(Settings.getSecret(), userId));

            if (signature.equals(request.getParameter("signature"))) {
                ToolUser user = Query.
                        from(ToolUser.class).
                        where("_id = ?", ObjectUtils.to(UUID.class, userId)).
                        first();

                if (user != null) {
                    AuthenticationFilter.Static.logIn(request, response, user);

                } else {
                    AuthenticationFilter.Static.logOut(request, response);
                }
            }
        }
    }
}
