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
com.psddev.dari.util.Settings,
com.psddev.dari.util.SparseSet,
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
java.util.Locale,
java.util.Map,
java.util.Set,
java.util.TreeMap,
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

String brightnessName = inputName + ".brightness";
String contrastName = inputName + ".contrast";
String flipHName = inputName + ".flipH";
String flipVName = inputName + ".flipV";
String grayscaleName = inputName + ".grayscale";
String invertName = inputName + ".invert";
String rotateName = inputName + ".rotate";
String sepiaName = inputName + ".sepia";

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

Map<String, Object> edits = (Map<String, Object>) fieldValueMetadata.get("cms.edits");

if (edits == null) {
    edits = new HashMap<String, Object>();
    fieldValueMetadata.put("cms.edits", edits);
}

double brightness = ObjectUtils.to(double.class, edits.get("brightness"));
double contrast = ObjectUtils.to(double.class, edits.get("contrast"));
boolean flipH = ObjectUtils.to(boolean.class, edits.get("flipH"));
boolean flipV = ObjectUtils.to(boolean.class, edits.get("flipV"));
boolean grayscale = ObjectUtils.to(boolean.class, edits.get("grayscale"));
boolean invert = ObjectUtils.to(boolean.class, edits.get("invert"));
int rotate = ObjectUtils.to(int.class, edits.get("rotate"));
boolean sepia = ObjectUtils.to(boolean.class, edits.get("sepia"));

Map<String, ImageCrop> crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() { }, fieldValueMetadata.get("cms.crops"));
if (crops == null) {
    // for backward compatibility
    crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() { }, state.getValue(cropsFieldName));
}
if (crops == null) {
    crops = new HashMap<String, ImageCrop>();
}

crops = new TreeMap<String, ImageCrop>(crops);

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

    brightness = wp.param(double.class, brightnessName);
    contrast = wp.param(double.class, contrastName);
    flipH = wp.param(boolean.class, flipHName);
    flipV = wp.param(boolean.class, flipVName);
    grayscale = wp.param(boolean.class, grayscaleName);
    invert = wp.param(boolean.class, invertName);
    rotate = wp.param(int.class, rotateName);
    sepia = wp.param(boolean.class, sepiaName);

    edits = new HashMap<String, Object>();

    if (brightness != 0.0) {
        edits.put("brightness", brightness);
    }
    if (contrast != 0.0) {
        edits.put("contrast", contrast);
    }
    if (flipH) {
        edits.put("flipH", flipH);
    }
    if (flipV) {
        edits.put("flipV", flipV);
    }
    if (invert) {
        edits.put("invert", invert);
    }
    if (rotate != 0) {
        edits.put("rotate", rotate);
    }
    if (grayscale) {
        edits.put("grayscale", grayscale);
    }
    if (sepia) {
        edits.put("sepia", sepia);
    }

    fieldValueMetadata.put("cms.edits", edits);

    if ("keep".equals(action)) {
        if (fieldValue != null) {
            newItem = fieldValue;
        } else {
            newItem = StorageItem.Static.createIn(wp.param(storageName));
            newItem.setPath(wp.param(pathName));
            newItem.setContentType(wp.param(contentTypeName));
        }

    } else if ("newUpload".equals(action) || "newUrl".equals(action)) {

        InputStream newItemData = null;

        if ("newUpload".equals(action)) {
            if (request instanceof MultipartRequest) {
                MultipartRequest mpRequest = (MultipartRequest) request;
                FileItem file = mpRequest.getFileItem(fileName);

                // Checks to make sure the file's content type is valid
                String groupsPattern = Settings.get(String.class, "cms/tool/fileContentTypeGroups");
                Set<String> contentTypeGroups = new SparseSet(ObjectUtils.isBlank(groupsPattern) ? "+/" : groupsPattern);
                if (!contentTypeGroups.contains(file.getContentType())) {
                    state.addError(field, String.format(
                            "Invalid content type [%s]. Must match the pattern [%s].",
                            file.getContentType(), contentTypeGroups));
                    return;
                }

                // Disallow HTML disguising as other content types per:
                // http://www.adambarth.com/papers/2009/barth-caballero-song.pdf
                if (!contentTypeGroups.contains("text/html")) {
                    InputStream input = file.getInputStream();

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
                            state.addError(field, String.format(
                                    "Can't upload [%s] file disguising as HTML!",
                                    file.getContentType()));
                            return;
                        }

                    } finally {
                        input.close();
                    }
                }

                if (file.getSize() > 0) {
                    String idString = UUID.randomUUID().toString().replace("-", "");
                    StringBuilder pathBuilder = new StringBuilder();
                    String label = state.getLabel();

                    if (ObjectUtils.isBlank(label)) {
                        label = file.getName();
                    }

                    label = StringUtils.toNormalized(label);

                    if (ObjectUtils.isBlank(label)) {
                        label = UUID.randomUUID().toString().replace("-", "");
                    }

                    pathBuilder.append(idString.substring(0, 2));
                    pathBuilder.append('/');
                    pathBuilder.append(idString.substring(2, 4));
                    pathBuilder.append('/');
                    pathBuilder.append(idString.substring(4));
                    pathBuilder.append('/');
                    pathBuilder.append(label);

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

    // Standard sizes.
    for (Iterator<Map.Entry<String, ImageCrop>> i = crops.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<String, ImageCrop> e = i.next();
        String cropId = e.getKey();
        double x = wp.doubleParam(cropsName + cropId + ".x");
        double y = wp.doubleParam(cropsName + cropId + ".y");
        double width = wp.doubleParam(cropsName + cropId + ".width");
        double height = wp.doubleParam(cropsName + cropId + ".height");
        String text = wp.param(cropsName + cropId + ".text");
        double textSize = wp.doubleParam(cropsName + cropId + ".textSize");
        double textX = wp.doubleParam(cropsName + cropId + ".textX");
        double textY = wp.doubleParam(cropsName + cropId + ".textY");
        double textWidth = wp.doubleParam(cropsName + cropId + ".textWidth");
        if (x != 0.0 || y != 0.0 || width != 0.0 || height != 0.0 || !ObjectUtils.isBlank(text)) {
            ImageCrop crop = e.getValue();
            crop.setX(x);
            crop.setY(y);
            crop.setWidth(width);
            crop.setHeight(height);
            crop.setText(text);
            crop.setTextSize(textSize);
            crop.setTextX(textX);
            crop.setTextY(textY);
            crop.setTextWidth(textWidth);
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

                    <div class="imageEditor-aside">
                        <div class="imageEditor-tools">
                            <h2>Tools</h2>
                            <ul>
                            </ul>
                        </div>

                        <div class="imageEditor-edit">
                            <h2>Filters</h2>
                            <table><tbody>
                                <tr>
                                    <th>Brightness</th>
                                    <td><input type="range" name="<%= brightnessName %>" value="<%= brightness %>" min="-1.0" max="1.0" step="0.01"></td>
                                </tr>
                                <tr>
                                    <th>Contrast</th>
                                    <td><input type="range" name="<%= contrastName %>" value="<%= contrast %>" min="-1.0" max="1.0" step="0.01"></td>
                                </tr>
                                <tr>
                                    <th>Flip H</th>
                                    <td><input type="checkbox" name="<%= flipHName %>" value="true"<%= flipH ? " checked" : "" %>></td>
                                </tr>
                                <tr>
                                    <th>Flip V</th>
                                    <td><input type="checkbox" name="<%= flipVName %>" value="true"<%= flipV ? " checked" : "" %>></td>
                                </tr>
                                <tr>
                                    <th>Invert</th>
                                    <td><input type="checkbox" name="<%= invertName %>" value="true"<%= invert ? " checked" : "" %>></td>
                                </tr>
                                <tr>
                                    <th>Grayscale</th>
                                    <td><input type="checkbox" name="<%= grayscaleName %>" value="true"<%= grayscale ? " checked" : "" %>></td>
                                </tr>
                                <tr>
                                    <th>Rotate</th>
                                    <td><input type="range" name="<%= rotateName %>" value="<%= rotate %>" min="-90" max="90" step="90"></td>
                                </tr>
                                <tr>
                                    <th>Sepia</th>
                                    <td><input type="checkbox" name="<%= sepiaName %>" value="true"<%= sepia ? " checked" : "" %>></td>
                                </tr>
                            </tbody></table>
                        </div>

                        <% if (!crops.isEmpty()) { %>
                            <div class="imageEditor-sizes">
                                <h2>Standard Sizes</h2>
                                <table><tbody>
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
                                            <td><input name="<%= wp.h(cropsName + cropId + ".text") %>" type="text" value="<%= wp.h(crop.getText()) %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".textSize") %>" type="text" value="<%= wp.h(crop.getTextSize()) %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".textX") %>" type="text" value="<%= crop.getTextX() %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".textY") %>" type="text" value="<%= crop.getTextY() %>"></td>
                                            <td><input name="<%= wp.h(cropsName + cropId + ".textWidth") %>" type="text" value="<%= crop.getTextWidth() %>"></td>
                                        </tr>
                                    <% } %>
                                </tbody></table>
                            </div>
                        <% } %>
                    </div>

                    <div class="imageEditor-image">
                        <img alt="" src="<%= wp.url("/misc/proxy.jsp", "url", fieldValue.getUrl()) %>">
                    </div>

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
