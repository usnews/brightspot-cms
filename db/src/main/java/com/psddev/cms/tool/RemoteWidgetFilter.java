package com.psddev.cms.tool;

import com.psddev.cms.db.ToolUser;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RemoteWidgetFilter extends AbstractFilter {

    // --- AbstractFilter support ---

    // Creates an object that originates from the given {@code database}
    // based on the given {@code json} string.
    private Object createObject(Database database, String json) {
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonValues = (Map<String, Object>) ObjectUtils.fromJson(json);
        UUID id = ObjectUtils.to(UUID.class, jsonValues.remove("_id"));
        UUID typeId = ObjectUtils.to(UUID.class, jsonValues.remove("_typeId"));
        ObjectType type = database.getEnvironment().getTypeById(typeId);

        if (type == null) {
            throw new IllegalArgumentException(String.format(
                    "[%s] is not a valid type ID!", typeId));

        } else {
            Object object = type.createObject(id);

            State.getInstance(object).setValues(jsonValues);

            return object;
        }
    }

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String path = request.getServletPath();
        String embeddedPath = JspUtils.getEmbeddedServletPath(getServletContext(), path);
        Boolean isUpdating = null;

        if (embeddedPath.startsWith(JspWidget.REMOTE_DISPLAY_API)) {
            isUpdating = Boolean.FALSE;

        } else if (embeddedPath.startsWith(JspWidget.REMOTE_UPDATE_API)) {
            isUpdating = Boolean.TRUE;
        }

        if (isUpdating == null) {
            chain.doFilter(request, response);

        } else {
            ToolPageContext page = new ToolPageContext(getServletContext(), request, response);
            Database database = page.getTool().getState().getDatabase();

            try {
                ToolUser user = Query.findById(ToolUser.class, page.param(UUID.class, RemoteWidget.USER_ID_PARAMETER));

                if (user != null) {
                    AuthenticationFilter.Static.logIn(request, response, user);
                }

                JspWidget widget = (JspWidget) createObject(database, page.param(String.class, RemoteWidget.WIDGET_PARAMETER));
                Object object = createObject(database, page.param(String.class, RemoteWidget.OBJECT_PARAMETER));
                Writer writer = response.getWriter();

                response.setCharacterEncoding("UTF-8");

                if (isUpdating) {
                    widget.update(page, object);
                    response.setContentType("application/json");
                    writer.write(ObjectUtils.toJson(State.getInstance(object).getSimpleValues()));

                } else {
                    writer.write(widget.display(page, object));
                }

            } catch (IOException error) {
                throw error;
            } catch (ServletException error) {
                throw error;
            } catch (RuntimeException error) {
                throw error;
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        }
    }
}
