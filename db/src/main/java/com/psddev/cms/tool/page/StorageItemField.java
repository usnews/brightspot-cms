package com.psddev.cms.tool.page;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.State;
import com.psddev.dari.util.IoUtils;
import com.psddev.dari.util.MultipartRequest;
import com.psddev.dari.util.MultipartRequestFilter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.SparseSet;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "storageItemField")
public class StorageItemField extends PageServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageItemField.class);
    public static final String FILE_SELECTOR_ITEM_CLASS = "fileSelectorItem";
    public static final String FILE_SELECTOR_EXISTING_CLASS = "fileSelectorExisting";
    public static final String FILE_SELECTOR_NEW_URL_CLASS = "fileSelectorNewUrl";
    public static final String FILE_SELECTOR_NEW_UPLOAD_CLASS = "fileSelectorNewUpload";
    public static final String FILE_SELECTOR_DROPBOX_CLASS = "fileSelectorDropbox";

    public static void processField(ToolPageContext page) throws IOException, ServletException {

        if (page.isFormPost()) {
            doFormPost(page);
            //return;
        }

        HttpServletRequest request = page.getRequest();
        Object object = request.getAttribute("object");
        State state = State.getInstance(object);

        ObjectField field = (ObjectField) request.getAttribute("field");
        String fieldName = field.getInternalName();
        StorageItem fieldValue = (StorageItem) state.getValue(fieldName);

        page.writeStart("div", "class", "inputSmall");

            writeFileSelector(page);

            if (fieldValue != null) {
                FilePreview.writePreview(page);
            }

        page.writeEnd();
    }

    public static void doFormPost(ToolPageContext page) throws IOException, ServletException {
        HttpServletRequest request = page.getRequest();
        Object object = request.getAttribute("object");

        if (object == null) {
            return;
        }

        State state = State.getInstance(object);

        ObjectField field = (ObjectField) request.getAttribute("field");
        String fieldName = field.getInternalName();
        StorageItem fieldValue = (StorageItem) state.getValue(fieldName);

        String inputName = (String) page.getRequest().getAttribute("inputName");
        String actionName = inputName + ".action";
        String storageName = inputName + ".storage";
        String fileName = inputName + ".file";
        String urlName = inputName + ".url";
        String pathName = inputName + ".path";
        String dropboxName = inputName + ".dropbox";
        String contentTypeName = inputName + ".contentType";

        File file = null;
        FileItem fileItem = null;
        MultipartRequest mpRequest;

        try {
            String action = page.param(actionName);
            StorageItem newItem = null;

            if ("keep".equals(action)) {
                if (fieldValue != null) {
                    newItem = fieldValue;
                } else {
                    newItem = StorageItemField.createStorageItemFromPath(page.param(pathName), page.param(storageName));
                    newItem.setContentType(page.param(contentTypeName));
                }

            } else if ("newUpload".equals(action) ||
                    "dropbox".equals(action)) {
                file = File.createTempFile("cms.", ".tmp");

                if ("dropbox".equals(action)) {
                    Map<String, Object> fileData = (Map<String, Object>) ObjectUtils.fromJson(page.param(String.class, dropboxName));

                    if (fileData != null) {

                        //copy file to temp file on server
                        try (InputStream fileInput = new URL(ObjectUtils.to(String.class, fileData.get("link"))).openStream();
                             FileOutputStream fileOutput = new FileOutputStream(file)) {
                            IoUtils.copy(fileInput, fileOutput);
                        }

                        newItem = StorageItemField.createStorageItemFromDropboxData(page, field, file, fileData);
                    }

                } else if ((mpRequest = MultipartRequestFilter.Static.getInstance(request)) != null) {
                    fileItem = mpRequest.getFileItem(fileName);
                    if (fileItem != null) {
                        fileItem.write(file);
                        newItem = StorageItemField.createStorageItemFromFile(page, file, fileItem, field, state);
                    }
                }

            } else if ("newUrl".equals(action)) {
                newItem = StorageItem.Static.createUrl(page.param(String.class, urlName));
            }

            FilePreview.setMetadata(page, state, newItem, file);
            if (newItem != null &&
                    ("newUpload".equals(action) ||
                            "dropbox".equals(action))) {
                newItem.save();
            }

            state.putValue(fieldName, newItem);
        } catch (Exception e) {
            LOGGER.error("Unable to process uploaded file", e);
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }

    }

    public static void writeFileSelector(ToolPageContext page) throws IOException, ServletException {
        HttpServletRequest request = page.getRequest();
        Object object = request.getAttribute("object");
        State state = State.getInstance(object);

        ObjectField field = (ObjectField) request.getAttribute("field");
        String fieldName = field.getInternalName();
        StorageItem fieldValue = (StorageItem) state.getValue(fieldName);

        String inputName = ObjectUtils.firstNonBlank(page.param(String.class, "inputName"),  (String) request.getAttribute("inputName"));
        String actionName = inputName + ".action";
        String fileName = inputName + ".file";
        String urlName = inputName + ".url";
        String dropboxName = inputName + ".dropbox";
        String pluginIdentifier = "";
        String storageSetting = getStorageSetting(field);

        page.writeStart("div", "class", "fileSelector");

            page.writeStart("select",
                    "id", page.getId(),
                    "class", "toggleable",
                    "data-root", ".inputSmall",
                    "name", actionName);

                if (fieldValue != null) {
                    page.writeStart("option",
                            "data-hide", "." + FILE_SELECTOR_ITEM_CLASS,
                            "data-show", "." + FILE_SELECTOR_EXISTING_CLASS,
                            "value", "keep");
                        page.write("Keep Existing");
                    page.writeEnd();
                }

                page.writeStart("option",
                        "data-hide", "." + FILE_SELECTOR_ITEM_CLASS,
                        "value", "none");
                    page.write("None");
                page.writeEnd();
                page.writeStart("option",
                        "data-hide", "." + FILE_SELECTOR_ITEM_CLASS,
                        "data-show", "." + FILE_SELECTOR_NEW_UPLOAD_CLASS,
                        "value", "newUpload",
                        fieldValue == null && field.isRequired() ? "selected" : "", "");
                    page.write("New Upload");
                page.writeEnd();
                page.writeStart("option",
                        "data-hide", "." + FILE_SELECTOR_ITEM_CLASS,
                        "data-show", "." + FILE_SELECTOR_NEW_URL_CLASS,
                        "value", "newUrl");
                    page.write("New URL");
                page.writeEnd();

                if (!ObjectUtils.isBlank(page.getCmsTool().getDropboxApplicationKey())) {
                    page.writeStart("option",
                            "data-hide", "." + FILE_SELECTOR_ITEM_CLASS,
                            "data-show", "." + FILE_SELECTOR_DROPBOX_CLASS,
                            "value", "dropbox");
                    page.write("Dropbox");
                    page.writeEnd();
                }

            page.writeEnd();

            page.writeTag("input",
                    "class", FILE_SELECTOR_ITEM_CLASS + " " + FILE_SELECTOR_NEW_UPLOAD_CLASS + " " + pluginIdentifier,
                    "type", "file",
                    "name", page.h(fileName),
                    "data-field-name", fieldName,
                    "data-type-id", state.getTypeId(),
                    "data-input-name", inputName,
                    "data-storage", storageSetting,
                    "data-path-start", StorageItemField.createStorageItemPath(null, null));
            page.writeTag("input",
                    "class", FILE_SELECTOR_ITEM_CLASS + " " + FILE_SELECTOR_NEW_URL_CLASS,
                    "type", "text",
                    "name", page.h(urlName));

            if (!ObjectUtils.isBlank(page.getCmsTool().getDropboxApplicationKey())) {
                page.writeStart("span",
                        "class", FILE_SELECTOR_ITEM_CLASS + " " + FILE_SELECTOR_DROPBOX_CLASS,
                        "style", "display: inline-block; vertical-align: bottom");
                    page.writeTag("input",
                            "type", "dropbox-chooser",
                            "name", page.h(dropboxName),
                            "data-link-type", "direct",
                            "style", "visibility:hidden");
                page.writeEnd();
                page.writeStart("script", "type", "text/javascript");
                    page.writeRaw(
                            "$('.fileSelectorDropbox input').on('DbxChooserSuccess', function(event) {\n" +
                                    "   $(this).val(JSON.stringify(event.originalEvent.files[0]));\n" +
                                    "});");
                page.writeEnd();
            }

        page.writeEnd();
    }

    public static StorageItem createStorageItemFromFile(ToolPageContext page, File file, FileItem fileItem, ObjectField field, State state)throws IOException {

        if (!StorageItemField.checkFileContent(fileItem, page, state, field)) {
            return null;
        }

        String fileName = fileItem.getName();

        return createStorageItem(field, file, fileName, fileItem.getContentType(), fileItem.getSize());
    }

    public static StorageItem createStorageItemFromPath(String path, String storageSetting) {

        StorageItem storageItem = StorageItem.Static.createIn(storageSetting != null ? Settings.getOrDefault(String.class, storageSetting, null) : null);
        storageItem.setPath(path);
        storageItem.setContentType(storageItem.getContentType());

        //TODO: validate file content here?

        return storageItem;
    }

    public static StorageItem createStorageItemFromDropboxData(ToolPageContext page, ObjectField field, File file, Map<String, Object> fileData) throws IOException {
        String fileName = ObjectUtils.to(String.class, fileData.get("name"));
        return createStorageItem(field, file, fileName, ObjectUtils.getContentType(fileName), ObjectUtils.to(long.class, fileData.get("bytes")));
    }

    private static StorageItem createStorageItem(ObjectField field, File file, String fileName, String contentType, long fileSize) throws IOException {

        StorageItem item = StorageItem.Static.createIn(getStorageSetting(field));
        item.setPath(createStorageItemPath(fileName, null));
        item.setContentType(contentType);
        item.setData(new FileInputStream(file));

        Map<String, List<String>> httpHeaders = new LinkedHashMap<>();

        httpHeaders.put("Cache-Control", Collections.singletonList("public, max-age=31536000"));
        httpHeaders.put("Content-Length", Collections.singletonList(String.valueOf(fileSize)));
        httpHeaders.put("Content-Type", Collections.singletonList(contentType));

        item.getMetadata().put("http.headers", httpHeaders);
        item.getMetadata().put("originalFilename", fileName);

        return item;
    }

    public static boolean checkFileContent(FileItem fileItem, ToolPageContext page, State state, ObjectField field) throws IOException {

        String errorMessage = null;

        if (!isAcceptedContentType(fileItem)) {
            errorMessage = String.format(
                    "Invalid content type [%s]. Must match the pattern [%s].",
                    fileItem.getContentType(), getContentTypeGroups());

        } else if (isDisguisedHtml(fileItem)) {
            errorMessage = String.format(
                    "Can't upload [%s] file disguising as HTML!",
                    fileItem.getContentType());
        }

        if (state != null && field != null && !StringUtils.isBlank(errorMessage)) {
            state.addError(field, errorMessage);
        } else {
            page.getErrors().add(new IllegalArgumentException(errorMessage));
        }

        return StringUtils.isBlank(errorMessage);
    }

    private static boolean isAcceptedContentType(FileItem fileItem) throws IOException {
        String contentType = fileItem.getContentType();
        return getContentTypeGroups().contains(contentType);
    }

    private static boolean isDisguisedHtml(FileItem fileItem) throws IOException {
        Set<String> contentTypeGroups = getContentTypeGroups();

        // Disallow HTML disguising as other content types per:
        // http://www.adambarth.com/papers/2009/barth-caballero-song.pdf
        if (!contentTypeGroups.contains("text/html")) {
            InputStream input = fileItem.getInputStream();

            try {
                byte[] buffer = new byte[1024];
                String data = new String(buffer, 0, input.read(buffer)).toLowerCase(Locale.ENGLISH);
                String ptr = data.trim();

                if (ptr.startsWith("<!") ||
                        ptr.startsWith("<?") ||
                        data.startsWith("<html") ||
                        data.startsWith("<script") ||
                        data.startsWith("<title") ||
                        data.startsWith("<body") ||
                        data.startsWith("<head") ||
                        data.startsWith("<plaintext") ||
                        data.startsWith("<table") ||
                        data.startsWith("<img") ||
                        data.startsWith("<pre") ||
                        data.startsWith("text/html") ||
                        data.startsWith("<a") ||
                        ptr.startsWith("<frameset") ||
                        ptr.startsWith("<iframe") ||
                        ptr.startsWith("<link") ||
                        ptr.startsWith("<base") ||
                        ptr.startsWith("<style") ||
                        ptr.startsWith("<div") ||
                        ptr.startsWith("<p") ||
                        ptr.startsWith("<font") ||
                        ptr.startsWith("<applet") ||
                        ptr.startsWith("<meta") ||
                        ptr.startsWith("<center") ||
                        ptr.startsWith("<form") ||
                        ptr.startsWith("<isindex") ||
                        ptr.startsWith("<h1") ||
                        ptr.startsWith("<h2") ||
                        ptr.startsWith("<h3") ||
                        ptr.startsWith("<h4") ||
                        ptr.startsWith("<h5") ||
                        ptr.startsWith("<h6") ||
                        ptr.startsWith("<b") ||
                        ptr.startsWith("<br")) {
                    return true;
                }

            } finally {
                input.close();
            }
        }
        return false;
    }

    private static Set<String> getContentTypeGroups() {
        String groupsPattern = Settings.get(String.class, "cms/tool/fileContentTypeGroups");
        return new SparseSet(ObjectUtils.isBlank(groupsPattern) ? "+/" : groupsPattern);
    }

    public static String createStorageItemPath(String fileName, String label) {
        String idString = UUID.randomUUID().toString().replace("-", "");
        StringBuilder pathBuilder = new StringBuilder();
        String extension = "";

        if (!ObjectUtils.isBlank(fileName)) {
            int lastDotAt = fileName.indexOf('.');

            if (lastDotAt > -1) {
                extension = fileName.substring(lastDotAt);
                fileName = fileName.substring(0, lastDotAt);

            }
        }

        if (ObjectUtils.isBlank(label) ||
                ObjectUtils.to(UUID.class, label) != null) {
            label = fileName;
        }

        if (ObjectUtils.isBlank(label)) {
            label = UUID.randomUUID().toString().replace("-", "");
        }

        pathBuilder.append(idString.substring(0, 2));
        pathBuilder.append('/');
        pathBuilder.append(idString.substring(2, 4));
        pathBuilder.append('/');
        pathBuilder.append(idString.substring(4));
        pathBuilder.append('/');
        pathBuilder.append(StringUtils.toNormalized(label));
        pathBuilder.append(extension);

        return pathBuilder.toString();
    }

    public static String getStorageSetting(ObjectField field) {
        String storageName = Settings.get(String.class, StorageItem.DEFAULT_STORAGE_SETTING);
        String fieldStorageSetting = field.as(ToolUi.class).getStorageSetting();

        if (!StringUtils.isBlank(fieldStorageSetting)) {
            storageName = fieldStorageSetting;
        }

        return storageName;
    }

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        processField(page);
    }
}
