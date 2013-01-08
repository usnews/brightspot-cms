package com.psddev.cms.tool.page;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWriter;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.DatabaseEnvironment;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectFieldComparator;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.State;
import com.psddev.dari.util.AggregateException;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.ImageMetadataMap;
import com.psddev.dari.util.IoUtils;
import com.psddev.dari.util.MultipartRequest;
import com.psddev.dari.util.MultipartRequestFilter;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.commons.fileupload.FileItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RoutingFilter.Path(application = "cms", value = "/content/uploadFiles")
@SuppressWarnings("serial")
public class UploadFiles extends PageServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFiles.class);

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        DatabaseEnvironment environment = Database.Static.getDefault().getEnvironment();
        PageWriter writer = page.getWriter();
        Exception postError = null;

        if (page.isFormPost()) {
            try {
                MultipartRequest request = MultipartRequestFilter.Static.getInstance(page.getRequest());

                if (request == null) {
                    throw new IllegalStateException("Not multipart!");
                }

                ObjectType selectedType = environment.getTypeById(page.param(UUID.class, "type"));

                ErrorUtils.errorIfNull(selectedType, "type");

                ObjectField previewField = selectedType.getField(selectedType.getPreviewField());

                if (previewField == null) {
                    throw new IllegalStateException("No preview field!");
                }

                FileItem[] files = request.getFileItems("file");
                StringBuilder js = new StringBuilder();

                if (files != null) {
                    for (FileItem file : files) {
                        StringBuilder path = new StringBuilder();
                        String random = UUID.randomUUID().toString().replace("-", "");

                        path.append(random.substring(0, 2));
                        path.append('/');
                        path.append(random.substring(2, 4));
                        path.append('/');
                        path.append(random.substring(4));
                        path.append('/');
                        path.append(StringUtils.toNormalized(file.getName()));

                        Map<String, List<String>> httpHeaders = new LinkedHashMap<String, List<String>>();

                        httpHeaders.put("Cache-Control", Collections.singletonList("public, max-age=31536000"));
                        httpHeaders.put("Content-Length", Collections.singletonList(String.valueOf(file.getSize())));
                        httpHeaders.put("Content-Type", Collections.singletonList(file.getContentType()));

                        StorageItem item = StorageItem.Static.create();
                        String contentType = file.getContentType();

                        item.setPath(path.toString());
                        item.setContentType(contentType);
                        item.getMetadata().put("http.headers", httpHeaders);
                        item.setData(file.getInputStream());

                        if (contentType != null && contentType.startsWith("image/")) {
                            InputStream fileInput = file.getInputStream();

                            try {
                                ImageMetadataMap metadata = new ImageMetadataMap(fileInput);
                                List<Throwable> errors = metadata.getErrors();

                                item.getMetadata().putAll(metadata);

                                if (!errors.isEmpty()) {
                                    LOGGER.info("Can't read image metadata!", new AggregateException(errors));
                                }

                            } finally {
                                IoUtils.closeQuietly(fileInput);
                            }
                        }

                        item.save();

                        State state = State.getInstance(selectedType.createObject(null));

                        state.put(previewField.getInternalName(), item);
                        page.publish(state);

                        js.append("$addButton.click();");
                        js.append("$input = $init.find(':input.objectId').eq(-1);");
                        js.append("$input.attr('data-label', '").append(StringUtils.escapeJavaScript(state.getLabel())).append("');");
                        js.append("$input.attr('data-preview', '").append(StringUtils.escapeJavaScript(item.getPublicUrl())).append("');");
                        js.append("$input.val('").append(StringUtils.escapeJavaScript(state.getId().toString())).append("');");
                        js.append("$input.change();");
                    }
                }

                writer.start("div", "id", page.createId()).end();

                writer.start("script", "type", "text/javascript");
                    writer.write("if (typeof jQuery !== 'undefined') (function($, win, undef) {");
                        writer.write("var $page = $('#" + page.getId() + "'),");
                        writer.write("$init = $page.popup('source').repeatable('closestInit'),");
                        writer.write("$addButton = $init.find('.addButton').eq(0),");
                        writer.write("$input;");
                        writer.write("if ($addButton.length > 0) {");
                            writer.write(js.toString());
                            writer.write("$page.popup('close');");
                        writer.write("} else {");
                            writer.write("win.location.reload();");
                        writer.write("}");
                    writer.write("})(jQuery, window);");
                writer.end();

                return;

            } catch (Exception error) {
                postError = error;
            }
        }

        Set<ObjectType> typesSet = new HashSet<ObjectType>();

        for (UUID typeId : page.params(UUID.class, "typeId")) {
            ObjectType type = environment.getTypeById(typeId);

            if (type != null) {
                for (ObjectType t : type.findConcreteTypes()) {
                    for (ObjectField field : t.getFields()) {
                        if (ObjectField.FILE_TYPE.equals(field.getInternalItemType())) {
                            typesSet.add(t);
                            break;
                        }
                    }
                }
            }
        }

        List<ObjectType> types = new ArrayList<ObjectType>(typesSet);
        Collections.sort(types, new ObjectFieldComparator("name", false));

        writer.start("h1").html("Upload Files").end();

        writer.start("form",
                "method", "post",
                "enctype", "multipart/form-data",
                "action", page.url(null));

            if (postError != null) {
                writer.start("div", "class", "message message-error");
                    writer.object(postError);
                writer.end();
            }

            writer.start("div", "class", "inputContainer");
                writer.start("div", "class", "inputContainer-label");
                    writer.start("label", "for", page.createId()).html("Type").end();
                writer.end();
                writer.start("div", "class", "inputContainer-smallInput");
                    writer.start("select", "id", page.getId(), "name", "type");
                        for (ObjectType type : types) {
                            writer.start("option", "value", type.getId());
                                writer.html(type.getDisplayName());
                            writer.end();
                        }
                    writer.end();
                writer.end();
            writer.end();

            writer.start("div", "class", "inputContainer");
                writer.start("div", "class", "inputContainer-label");
                    writer.start("label", "for", page.createId()).html("Files").end();
                writer.end();
                writer.start("div", "class", "inputContainer-smallInput");
                    writer.tag("input",
                            "id", page.getId(),
                            "type", "file",
                            "name", "file",
                            "multiple", "multiple");
                writer.end();
            writer.end();

            writer.start("div", "class", "buttons");
                writer.start("button", "name", "action-upload").html("Upload").end();
            writer.end();

        writer.end();
    }
}
