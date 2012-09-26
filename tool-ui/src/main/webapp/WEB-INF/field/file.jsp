<%@ page import="

com.psddev.cms.db.ImageCrop,
com.psddev.cms.db.StandardImageSize,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,
com.psddev.dari.util.AggregateException,
com.psddev.dari.util.BrightcoveStorageItem,
com.psddev.dari.util.MultipartRequest,
com.psddev.dari.util.ImageMetadataMap,
com.psddev.dari.util.IoUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StorageItem,
com.psddev.dari.util.StorageItem.Static,
com.psddev.dari.util.StringUtils,
com.psddev.dari.util.TypeReference,

java.io.File,
java.io.InputStream,
java.io.IOException,
java.util.AbstractMap,
java.util.Collections,
java.util.HashMap,
java.util.Iterator,
java.util.LinkedHashMap,
java.util.List,
java.util.Map,
java.util.UUID,

org.apache.commons.fileupload.FileItem,

org.slf4j.Logger,
org.slf4j.LoggerFactory
" %><%!

private static final Logger LOGGER = LoggerFactory.getLogger(ToolPageContext.class);
%><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));
UUID id = state.getId();

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
StorageItem fieldValue = (StorageItem) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");
String actionName = inputName + ".action";
String storageName = inputName + ".storage";
String pathName = inputName + ".path";
String contentTypeName = inputName + ".contentType";
String fileName = inputName + ".file";
String urlName = inputName + ".url";
String cropsName = inputName + ".crops.";

String metadataFieldName = fieldName + ".metadata";
String widthFieldName = fieldName + ".width";
String heightFieldName = fieldName + ".height";
String cropsFieldName = fieldName + ".crops";

Map<String, Object> fieldValueMetadata = null;
if (fieldValue != null) {
    fieldValueMetadata = fieldValue.getMetadata();
}

if (fieldValueMetadata == null) {
    fieldValueMetadata = new LinkedHashMap<String, Object>();
}

Map<String, ImageCrop> crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() { }, fieldValueMetadata.get("cms.crops"));
if (crops == null) {
    // for backward compatibility
    crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() { }, state.getValue(cropsFieldName));
}
if (crops == null) {
    crops = new HashMap<String, ImageCrop>();
}

Map<String, StandardImageSize> sizes = new HashMap<String, StandardImageSize>();
for (StandardImageSize size : StandardImageSize.findAll()) {
    String sizeId = size.getId().toString();
    sizes.put(sizeId, size);
    if (crops.get(sizeId) == null) {
        crops.put(sizeId, new ImageCrop());
    }
}

if ((Boolean) request.getAttribute("isFormPost")) {
    String action = wp.param(actionName);
    StorageItem newItem = null;

    if ("keep".equals(action)) {
        newItem = StorageItem.Static.createIn(wp.param(storageName));
        newItem.setPath(wp.param(pathName));
        newItem.setContentType(wp.param(contentTypeName));

    } else if ("newUpload".equals(action) || "newUrl".equals(action)) {

        InputStream newItemData = null;

        if ("newUpload".equals(action)) {
            if (request instanceof MultipartRequest) {
                MultipartRequest mpRequest = (MultipartRequest) request;
                FileItem file = mpRequest.getFileItem(fileName);

                if (file.getSize() > 0) {
                    String idString = UUID.randomUUID().toString().replace("-", "");
                    StringBuilder pathBuilder = new StringBuilder();
                    pathBuilder.append(idString.substring(0, 2));
                    pathBuilder.append('/');
                    pathBuilder.append(idString.substring(2, 4));
                    pathBuilder.append('/');
                    pathBuilder.append(idString.substring(4));
                    pathBuilder.append('/');
                    pathBuilder.append(file.getName());

                    newItem = StorageItem.Static.create();
                    newItem.setPath(pathBuilder.toString());
                    newItem.setContentType(file.getContentType());

                    Map<String, List<String>> httpHeaders = new LinkedHashMap<String, List<String>>();
                    httpHeaders.put("Cache-Control", Collections.singletonList("public, max-age=31536000"));
                    httpHeaders.put("Content-Length", Collections.singletonList(String.valueOf(file.getSize())));
                    httpHeaders.put("Content-Type", Collections.singletonList(file.getContentType()));
                    fieldValueMetadata.put("http.headers", httpHeaders);

                    newItem.setData(file.getInputStream());

                    newItemData = file.getInputStream();
                }
            }

        } else if ("newUrl".equals(action)) {
            newItem = StorageItem.Static.createUrl(wp.param(urlName));

            newItemData = newItem.getData();
        }

        // Automatic image metadata extraction.
        if (newItem != null) {
            String contentType = newItem.getContentType();

            if (contentType != null && contentType.startsWith("image/")) {
                try {
                    ImageMetadataMap metadata = new ImageMetadataMap(newItemData);
                    fieldValueMetadata.putAll(metadata);

                    List<Throwable> errors = metadata.getErrors();
                    if (!errors.isEmpty()) {
                        LOGGER.info("Can't read image metadata!", new AggregateException(errors));
                    }

                } finally {
                    IoUtils.closeQuietly(newItemData);
                }
            }
        }
    }

    // Crops.
    for (Iterator<Map.Entry<String, ImageCrop>> i = crops.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<String, ImageCrop> e = i.next();
        String cropId = e.getKey();
        double x = wp.doubleParam(cropsName + cropId + ".x");
        double y = wp.doubleParam(cropsName + cropId + ".y");
        double width = wp.doubleParam(cropsName + cropId + ".width");
        double height = wp.doubleParam(cropsName + cropId + ".height");
        if (x != 0.0 || y != 0.0 || width != 0.0 || height != 0.0) {
            ImageCrop crop = e.getValue();
            crop.setX(x);
            crop.setY(y);
            crop.setWidth(width);
            crop.setHeight(height);
        } else {
            i.remove();
        }
    }
    fieldValueMetadata.put("cms.crops", crops);
    // Removes legacy cropping information
    if (state.getValue(cropsFieldName) != null) {
        state.remove(cropsFieldName);
    }

    // Transfers legacy metadata over to it's new location within the StorageItem object
    Map<String, Object> legacyMetadata = ObjectUtils.to(new TypeReference<Map<String, Object>>() { }, state.getValue(metadataFieldName));
    if (legacyMetadata != null && !legacyMetadata.isEmpty()) {
        for (Map.Entry<String, Object> entry : legacyMetadata.entrySet()) {
            if (!fieldValueMetadata.containsKey(entry.getKey())) {
                fieldValueMetadata.put(entry.getKey(), entry.getValue());
            }
        }
        state.remove(metadataFieldName);
    }

    if (newItem != null) {
        newItem.setMetadata(fieldValueMetadata);
    }

    if (newItem != null && "newUpload".equals(action)) {
        newItem.save();
    }

    state.putValue(fieldName, newItem);
    return;
}

// --- Presentation ---

String newUploadClass = wp.createId();
String newUrlClass = wp.createId();
String existingClass = wp.createId();

%><div class="smallInput">
    <div class="fileSelector">
        <select class="toggleable" id="<%= wp.getId() %>" name="<%= wp.h(actionName) %>">
            <% if (fieldValue != null) { %>
                <option data-hide=".<%= newUploadClass %>, .<%= newUrlClass %>" data-show=".<%= existingClass %>" value="keep">Keep Existing</option>
            <% } %>
            <option data-hide=".<%= newUploadClass %>, .<%= newUrlClass %>, .<%= existingClass %>" value="none">None</option>
            <option data-hide=".<%= newUrlClass %>, .<%= existingClass %>" data-show=".<%= newUploadClass %>" value="newUpload">New Upload</option>
            <option data-hide=".<%= newUploadClass %>, .<%= existingClass %>" data-show=".<%= newUrlClass %>" value="newUrl">New URL</option>
        </select>
        <input class="<%= newUploadClass %>" type="file" name="<%= wp.h(fileName) %>">
        <input class="<%= newUrlClass %>" type="text" name="<%= wp.h(urlName) %>">
    </div>

    <%
    if (fieldValue != null) {
        String contentType = fieldValue.getContentType();
        %>
        <div class="<%= existingClass %> filePreview">
            <input name="<%= wp.h(storageName) %>" type="hidden" value="<%= wp.h(fieldValue.getStorage()) %>">
            <input name="<%= wp.h(pathName) %>" type="hidden" value="<%= wp.h(fieldValue.getPath()) %>">
            <input name="<%= wp.h(contentTypeName) %>" type="hidden" value="<%= wp.h(contentType) %>">

            <% if (contentType != null && contentType.startsWith("image/")) { %>
                <div class="imageEditor">
                    <ul class="toolbar piped">
                        <li><a class="icon-table" href="<%= wp.url("/content/imageMetadata.jsp", "id", id, "field", fieldName) %>" target="contentImageMetadata">Metadata</a></li>
                        <% if (!crops.isEmpty()) { %>
                            <li>Crops:
                                <table class="crops"><tbody>
                                    <%
                                    for (Map.Entry<String, ImageCrop> e : crops.entrySet()) {
                                        String cropId = e.getKey();
                                        ImageCrop crop = e.getValue();
                                        StandardImageSize size = sizes.get(cropId);
                                        if (size == null && ObjectUtils.to(UUID.class, cropId) != null) {
                                            continue;
                                        }
                                        %>
                                        <% if (size != null) { %>
                                            <tr data-size-width="<%= size.getWidth() %>" data-size-height="<%= size.getHeight() %>">
                                                <th><%= wp.h(size.getDisplayName()) %></th>
                                        <% } else { %>
                                            <tr>
                                                <th><%= wp.h(cropId) %></th>
                                        <% } %>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".x") %>" type="text" value="<%= crop.getX() %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".y") %>" type="text" value="<%= crop.getY() %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".width") %>" type="text" value="<%= crop.getWidth() %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".height") %>" type="text" value="<%= crop.getHeight() %>"></td>
                                        </tr>
                                    <% } %>
                                </tbody></table>
                            </li>
                        <% } %>
                    </ul>
                    <img src="<%= wp.h(fieldValue.getUrl()) %>">
                </div>
            <% } else if(fieldValue instanceof BrightcoveStorageItem) { %>

                <% String playerKey = ((BrightcoveStorageItem) fieldValue).getPreviewPlayerKey(); %>
                <% String playerId = ((BrightcoveStorageItem) fieldValue).getPreviewPlayerId(); %>

                <% if(!ObjectUtils.isBlank(playerKey) && !ObjectUtils.isBlank(playerId)) { %>
                    <!-- Start of Brightcove Player -->

                    <!--
                    By use of this code snippet, I agree to the Brightcove Publisher T and C
                    found at https://accounts.brightcove.com/en/terms-and-conditions/.
                    -->
                    <script language="JavaScript" type="text/javascript" src="http://admin.brightcove.com/js/BrightcoveExperiences.js"></script>

                    <script type="text/javascript">


                        // Store reference to the player
                        var player;

                        // Store reference to the modules in the player
                        var modVP;
                        var modExp;
                        var modCon;

                        // This method is called when the player loads with the ID of the player
                        // We can use that ID to get a reference to the player, and then the modules
                        // The name of this method can vary but should match the value you specified
                        // in the player publishing code for “templateLoadHandler”.

                        var myTemplateLoaded = function(experienceID) {
                            // Get a reference to the player itself
                            player = brightcove.api.getExperience(experienceID);

                          // Get a reference to individual modules in the player
                          modVP = player.getModule(brightcove.api.modules.APIModules.VIDEO_PLAYER);
                          modExp = player.getModule(brightcove.api.modules.APIModules.EXPERIENCE);
                          modCon = player.getModule(brightcove.api.modules.APIModules.CONTENT);

                          if(modVP.loadVideoByID(<%=((BrightcoveStorageItem)fieldValue).getBrightcoveId()%>) === null) {
                              if(typeof(console) !== 'undefined') { console.log("Video with id=<%=((BrightcoveStorageItem)fieldValue).getBrightcoveId()%> could not be found"); }
                          }
                        };
                    </script>

                    <object id="myExperience" class="BrightcoveExperience">
                      <param name="bgcolor" value="#FFFFFF" />
                      <param name="width" value="480" />
                      <param name="height" value="270" />
                      <param name="playerID" value="<%=playerId%>" />
                      <param name="playerKey" value="<%=playerKey%>" />
                      <param name="isVid" value="true" />
                      <param name="isUI" value="true" />
                      <param name="dynamicStreaming" value="true" />
                      <param name="includeAPI" value="true" />
                      <param name="templateLoadHandler" value="myTemplateLoaded" />
                    </object>

                    <!--
                    This script tag will cause the Brightcove Players defined above it to be created as soon
                    as the line is read by the browser. If you wish to have the player instantiated only after
                    the rest of the HTML is processed and the page load is complete, remove the line.
                    -->
                    <script type="text/javascript">brightcove.createExperiences();</script>

                    <!-- End of Brightcove Player -->
                <% } else { %>
                    <p>No Brightcove player is configured for previewing videos.</p>
                <% } %>

            <% } else { %>
                <a href="<%= wp.h(fieldValue.getUrl()) %>" target="_blank"><%= wp.h(contentType) %>: <%= wp.h(fieldValue.getPath()) %></a>
            <% } %>
        </div>
    <% } %>
</div>
