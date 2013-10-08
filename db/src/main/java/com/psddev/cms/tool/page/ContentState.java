package com.psddev.cms.tool.page;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Preview;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.State;
import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/contentState")
public class ContentState extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Object object = page.findOrReserve();

        if (object == null) {
            return;
        }

        // Pretend to update the object.
        State state = State.getInstance(object);

        try {
            state.beginWrites();
            page.updateUsingParameters(object);
            page.updateUsingAllWidgets(object);

        } catch (IOException error) {
            throw error;

        } catch (ServletException error) {
            throw error;

        } catch (RuntimeException error) {
            throw error;

        } catch (Exception error) {
            ErrorUtils.rethrow(error);

        } finally {
            state.endWrites();
        }

        // Preview for looking glass.
        Preview preview = new Preview();
        ToolUser user = page.getUser();
        UUID currentPreviewId = user.getCurrentPreviewId();

        if (currentPreviewId == null) {
            currentPreviewId = preview.getId();

            user.setCurrentPreviewId(currentPreviewId);
            user.save();
        }

        Map<String, Object> values= state.getSimpleValues();

        preview.getState().setId(currentPreviewId);
        preview.setCreateDate(new Date());
        preview.setObjectType(state.getType());
        preview.setObjectId(state.getId());
        preview.setObjectValues(values);
        preview.setSite(page.getSite());
        preview.save();
        user.saveAction(page.getRequest(), object);

        // HTML display for the URL widget.
        Map<String, Object> jsonResponse = new CompactMap<String, Object>();
        @SuppressWarnings("unchecked")
        List<Directory.Path> automaticPaths = (List<Directory.Path>) state.getExtras().get("cms.automaticPaths");
        Directory.ObjectModification dirData = state.as(Directory.ObjectModification.class);
        boolean manual = Directory.PathsMode.MANUAL.equals(dirData.getPathsMode());

        if ((automaticPaths != null && !automaticPaths.isEmpty()) || manual) {
            StringWriter string = new StringWriter();
            @SuppressWarnings("resource")
            HtmlWriter html = new HtmlWriter(string);
            String automaticName = state.getId() + "/directory.automatic";

            html.writeStart("div", "class", "widget-urlsAutomatic");
                html.writeStart("p");
                    html.writeTag("input",
                            "type", "hidden",
                            "name", automaticName,
                            "value", true);

                    html.writeTag("input",
                            "type", "checkbox",
                            "id", page.createId(),
                            "name", automaticName,
                            "value", true,
                            "checked", manual ? null : "checked");

                    html.writeHtml(" ");

                    html.writeStart("label", "for", page.getId());
                        html.writeHtml("Generate Permalink?");
                    html.writeEnd();
                html.writeEnd();

                if (!manual) {
                    html.writeStart("ul");
                        for (Directory.Path p : automaticPaths) {
                            html.writeStart("li");
                                html.writeStart("a",
                                        "target", "_blank",
                                        "href", p.getPath());
                                    html.writeHtml(p.getPath());
                                html.writeEnd();

                                html.writeHtml(" (");
                                html.writeHtml(p.getType());
                                html.writeHtml(")");
                            html.writeEnd();
                        }
                    html.writeEnd();
                }
            html.writeEnd();

            jsonResponse.put("_urlWidgetHtml", string.toString());
        }

        HttpServletResponse response = page.getResponse();

        response.setContentType("application/json");
        page.write(ObjectUtils.toJson(jsonResponse));
    }
}
