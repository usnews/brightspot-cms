package com.psddev.cms.db;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.psddev.cms.tool.CmsTool;
import com.psddev.dari.db.Application;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeReference;

/**
 * <p>Static utility class for building HTML 'img' tags and URLs edited by
 * an {@link ImageEditor}. This class is functionally equivalent to calling
 * the JSTL &lt;cms:img&gt; tag in your JSP code.  Example usage:</p>
 * <pre>
 * StorageItem myStorageItem;
 *
 * String imageTagHtml = new ImageTagBuilder(myStorageItem)
 *      .width(300)
 *      .height(200)
 *      .attributes(
 *          "class", "thumbnail",
 *          "alt", "My image")
 *      .toHtml();
 * </pre>
 * You can also grab just the image URL instead of the entire HTML output
 * by calling:
 * <pre>
 * String imageUrl = new ImageTagBuilder(myStorageItem)
 *      .width(300)
 *      .height(200)
 *      .toUrl()
 * </pre>
 */
public class ImageTagBuilder {
    private StorageItem item;
    @Deprecated
    private String field;
    private ImageEditor editor;

    private StandardImageSize standardImageSize;

    private Integer width;
    private Integer height;
    private CropOption cropOption;
    private ResizeOption resizeOption;

    private String tagName;
    private Set<String> srcAttributes = new LinkedHashSet<String>();
    private boolean hideDimensions;
    private boolean overlay;
    private boolean edits = true;

    private final Map<String, String> attributes = new LinkedHashMap<String, String>();

    // for backwards compatibility
    private State state;

    public ImageTagBuilder(StorageItem item) {
        this.item = item;
    }

    ImageTagBuilder() {
    }

    public StorageItem getItem() {
        return this.item;
    }

    void setItem(StorageItem item) {
        this.item = item;
    }

    /**
     * Sets the field that contains the image. If not set, the first
     * field with {@value ObjectField.FILE_TYPE} type is used.
     * @deprecated No replacement
     */
    @Deprecated
    void setField(String field) {
        this.field = field;
    }

    public ImageEditor getEditor() {
        return editor;
    }

    /**
     * Sets the name of the {@linkplain ImageEditor image editor}
     * to use.
     */
    public void setEditor(ImageEditor editor) {
        this.editor = editor;
    }

    public StandardImageSize getStandardImageSize() {
        return standardImageSize;
    }

    /**
     * Sets the internal name of the {@linkplain StandardImageSize
     * image size} to use.
     */
    public void setStandardImageSize(StandardImageSize standardImageSize) {
        this.standardImageSize = standardImageSize;
    }

    public Integer getWidth() {
        return width;
    }

    /**
     * Sets the width. Note that this will override the width provided
     * by the image size set with {@link #setSize(String)}.
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the height. Note that this will override the height provided
     * by the image size set with {@link #setSize(String)}.
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    public CropOption getCropOption() {
        return cropOption;
    }

    /**
     * Sets the crop option. Note that this will override the crop option
     * provided by the image size set with {@link #setSize(String)}.
     */
    public void setCropOption(CropOption cropOption) {
        this.cropOption = cropOption;
    }

    public ResizeOption getResizeOption() {
        return resizeOption;
    }

    /**
     * Sets the resize option. Note that this will override the resize option
     * provided by the image size set with {@link #setSize(String)}.
     */
    public void setResizeOption(ResizeOption resizeOption) {
        this.resizeOption = resizeOption;
    }

    public String getTagName() {
        return tagName;
    }

    /**
     * Sets the tag name to be rendered
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Set<String> getSrcAttributes() {
        return new LinkedHashSet<String>(srcAttributes);
    }

    /**
     * Overrides the default attribute (src) used to place the image URL. This
     * is usually used in the conjunction with lazy loading scripts that copy
     * the image URL from this attribute to the "src" attribute at some point
     * after the page has loaded.
     */
    public void setSrcAttributes(String... srcAttributes) {
        this.srcAttributes.clear();

        if (srcAttributes != null) {
            for (String srcAttribute : srcAttributes) {
                this.srcAttributes.add(srcAttribute);
            }
        }
    }

    public boolean isHideDimensions() {
        return hideDimensions;
    }

    /**
     * Sets whether the width and height attributes will be suppressed from the
     * final tag output.
     */
    public void setHideDimensions(boolean hideDimensions) {
        this.hideDimensions = hideDimensions;
    }

    public boolean isOverlay() {
        return overlay;
    }

    /**
     * Sets whether a text overlay will be rendered on top of the image.
     */
    public void setOverlay(boolean overlay) {
        this.overlay = overlay;
    }

    public boolean isEdits() {
        return edits;
    }

    /**
     * Sets whether image edits will be applied to the image.
     */
    public void setEdits(boolean edits) {
        this.edits = edits;
    }

    /**
     * Sets additional attributes to be added to the tag.
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

    void addAttribute(String key, Object value) {
        this.attributes.put(key, value != null ? value.toString() : null);
    }

    // --- builder pattern support ---

    public ImageTagBuilder editor(ImageEditor editor) {
        setEditor(editor);
        return this;
    }

    public ImageTagBuilder standardImageSize(StandardImageSize standardImageSize) {
        setStandardImageSize(standardImageSize);
        return this;
    }

    public ImageTagBuilder standardImageSize(String size) {
        for (StandardImageSize standardSize : StandardImageSize.findAll()) {
            if (standardSize.getInternalName().equals(size)) {
                setStandardImageSize(standardSize);
                break;
            }
        }
        return this;
    }

    public ImageTagBuilder width(Integer width) {
        setWidth(width);
        return this;
    }

    public ImageTagBuilder height(Integer height) {
        setHeight(height);
        return this;
    }

    public ImageTagBuilder cropOption(CropOption cropOption) {
        setCropOption(cropOption);
        return this;
    }

    public ImageTagBuilder cropOption(String cropOption) {
        setCropOption(CropOption.Static.fromImageEditorOption(cropOption));
        return this;
    }

    public ImageTagBuilder resizeOption(ResizeOption resizeOption) {
        setResizeOption(resizeOption);
        return this;
    }

    public ImageTagBuilder resizeOption(String resizeOption) {
        setResizeOption(ResizeOption.Static.fromImageEditorOption(resizeOption));
        return this;
    }

    public ImageTagBuilder tagName(String tagName) {
        setTagName(tagName);
        return this;
    }

    public ImageTagBuilder srcAttributes(String... srcAttributes) {
        setSrcAttributes(srcAttributes);
        return this;
    }

    public ImageTagBuilder hideDimensions() {
        setHideDimensions(true);
        return this;
    }

    public ImageTagBuilder overlay() {
        setOverlay(true);
        return this;
    }

    public ImageTagBuilder noEdits() {
        setEdits(false);
        return this;
    }

    public ImageTagBuilder attributes(Object... attributes) {
        this.attributes.clear();
        if (attributes != null) {
            for (int i=0; i<attributes.length; i+=2) {
                if (i+1 < attributes.length) {
                    Object key = attributes[i];
                    Object value = attributes[i+1];
                    if (key != null) {
                        this.attributes.put(key.toString(), value != null ? value.toString() : null);
                    }
                }
            }
        }
        return this;
    }

    /**
     * For backwards compatibility
     *
     * @deprecated
     */
    @Deprecated
    ImageTagBuilder setState(State state) {
        this.state = state;
        return this;
    }

    /**
     * For backwards compatibility
     *
     * @deprecated
     */
    @Deprecated
    ImageTagBuilder setRecordable(Recordable recordable) {
        setState(State.getInstance(recordable));
        return this;
    }

    /**
     *
     * @return the HTML for an img tag constructed by this Builder.
     */
    public String toHtml() {
        String html = convertAttributesToHtml(tagName, toAttributes());

        if (isOverlay()) {
            StorageItem item = null;
            Map<String, ImageCrop> crops = null;

            // legacy support
            if (this.state != null) {
                State objectState = this.state;
                String field = this.field;

                if (ObjectUtils.isBlank(field)) {
                    field = findStorageItemField(objectState);
                }

                item = findStorageItem(objectState, field);

                if (item != null) {
                    crops = findImageCrops(objectState, field);
                }

            } else {
                item = this.item;

                if (item != null) {
                    crops = findImageCrops(item);
                }
            }

            if (item != null && crops != null && standardImageSize != null) {
                ImageCrop crop = crops.get(standardImageSize.getId().toString());

                if (crop != null) {
                    List<ImageTextOverlay> textOverlays = crop.getTextOverlays();
                    boolean hasOverlays = false;

                    for (ImageTextOverlay textOverlay : textOverlays) {
                        if (!ObjectUtils.isBlank(textOverlay.getText())) {
                            hasOverlays = true;
                            break;
                        }
                    }

                    if (hasOverlays) {
                        StringBuilder overlay = new StringBuilder();
                        CmsTool cms = Application.Static.getInstance(CmsTool.class);
                        String defaultCss = cms.getDefaultTextOverlayCss();
                        String id = "i" + UUID.randomUUID().toString().replace("-", "");

                        overlay.append("<style type=\"text/css\">");

                        if (!ObjectUtils.isBlank(defaultCss)) {
                            overlay.append("#");
                            overlay.append(id);
                            overlay.append("{display:inline-block;overflow:hidden;position:relative;");
                            overlay.append(defaultCss);
                            overlay.append("}");
                        }

                        for (CmsTool.CssClassGroup group : cms.getTextCssClassGroups()) {
                            String groupName = group.getInternalName();
                            for (CmsTool.CssClass cssClass : group.getCssClasses()) {
                                overlay.append("#");
                                overlay.append(id);
                                overlay.append(" .cms-");
                                overlay.append(groupName);
                                overlay.append("-");
                                overlay.append(cssClass.getInternalName());
                                overlay.append("{");
                                overlay.append(cssClass.getCss());
                                overlay.append("}");
                            }
                        }

                        overlay.append("</style>");

                        overlay.append("<span id=\"");
                        overlay.append(id);
                        overlay.append("\">");
                        overlay.append(html);

                        for (ImageTextOverlay textOverlay : textOverlays) {
                            String text = textOverlay.getText();

                            overlay.append("<span style=\"left: ");
                            overlay.append(textOverlay.getX() * 100);
                            overlay.append("%; position: absolute; top: ");
                            overlay.append(textOverlay.getY() * 100);
                            overlay.append("%; font-size: ");
                            overlay.append(textOverlay.getSize() * standardImageSize.getHeight());
                            overlay.append("px; width: ");
                            overlay.append(textOverlay.getWidth() != 0.0 ? textOverlay.getWidth() * 100 : 100.0);
                            overlay.append("%;\">");
                            overlay.append(text);
                            overlay.append("</span>");
                        }

                        overlay.append("</span>");
                        html = overlay.toString();
                    }
                }
            }
        }

        return html;
    }

    /**
     *
     * @return the URL to the image as a String.
     */
    public String toUrl() {
        return toAttributes().get(
                srcAttributes.isEmpty() ?
                "src" : srcAttributes.iterator().next());
    }

    /** Returns all the attributes that will get placed on the img tag. */
    public Map<String, String> toAttributes() {
        // set all the attributes
        Map<String, String> attributes = new LinkedHashMap<String, String>();

        ImageEditor editor = this.editor;

        StandardImageSize standardImageSize = this.standardImageSize;

        Integer width = this.width;
        Integer height = this.height;
        CropOption cropOption = this.cropOption;
        ResizeOption resizeOption = this.resizeOption;

        Set<String> srcAttributes = this.srcAttributes;
        boolean hideDimensions = this.hideDimensions;

        StorageItem item = null;
        Integer originalWidth = null;
        Integer originalHeight = null;
        Map<String, ImageCrop> crops = null;

        if (this.state != null) { // backwards compatibility path
            State objectState = this.state;
            String field = this.field;

            if (ObjectUtils.isBlank(field)) {
                field = findStorageItemField(objectState);
            }

            item = findStorageItem(objectState, field);

            if (item != null) {
                originalWidth = findDimension(objectState, field, "width");
                originalHeight = findDimension(objectState, field, "height");
                crops = findImageCrops(objectState, field);
            }

        } else { // new code path
            item = this.item;

            if (item != null) {
                originalWidth = findDimension(item, "width");
                originalHeight = findDimension(item, "height");
                crops = findImageCrops(item);
            }
        }

        // null out all dimensions that are less than or equal to zero
        originalWidth = originalWidth != null && originalWidth <= 0 ? null : originalWidth;
        originalHeight = originalHeight != null && originalHeight <= 0 ? null : originalHeight;
        width = width != null && width <= 0 ? null : width;
        height = height != null && height <= 0 ? null : height;

        if (item != null) {
            Map<String, Object> options = new LinkedHashMap<String, Object>();

            Integer cropX = null, cropY = null, cropWidth = null, cropHeight = null;

            // set fields from this standard size if they haven't already been set
            if (standardImageSize != null) {
                // get the standard image dimensions
                if (width == null) {
                    width = standardImageSize.getWidth();
                    if (width <= 0) {
                        width = null;
                    }
                }
                if (height == null) {
                    height = standardImageSize.getHeight();
                    if (height <= 0) {
                        height = null;
                    }
                }

                // get the crop and resize options
                if (cropOption == null) {
                    cropOption = standardImageSize.getCropOption();
                }
                if (resizeOption == null) {
                    resizeOption = standardImageSize.getResizeOption();
                }

                // get the crop coordinates
                ImageCrop crop;
                if (crops != null && (crop = crops.get(standardImageSize.getId().toString())) != null &&
                        originalWidth != null && originalHeight != null) {

                    cropX = (int) (crop.getX() * originalWidth);
                    cropY = (int) (crop.getY() * originalHeight);
                    cropWidth = (int) (crop.getWidth() * originalWidth);
                    cropHeight = (int) (crop.getHeight() * originalHeight);
                }
            }

            // if the crop info is unavailable, assume that the image
            // dimensions are the crop dimensions in case the image editor
            // knows how to crop without the x & y coordinates
            if (cropWidth == null) {
                cropWidth = width;
            }
            if (cropHeight == null) {
                cropHeight = height;
            }

            // set the options
            if (cropOption != null) {
                options.put(ImageEditor.CROP_OPTION, cropOption.getImageEditorOption());
            }
            if (resizeOption != null) {
                options.put(ImageEditor.RESIZE_OPTION, resizeOption.getImageEditorOption());
            }

            if (isEdits()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> edits = (Map<String, Object>) item.getMetadata().get("cms.edits");

                if (edits != null) {
                    ImageEditor realEditor = editor;
                    if (realEditor == null) {
                        realEditor = ImageEditor.Static.getDefault();
                    }
                    for (Map.Entry<String, Object> entry : new TreeMap<String, Object>(edits).entrySet()) {
                        item = realEditor.edit(item, entry.getKey(), null, entry.getValue());
                    }
                }
            }

            // Requires at least the width and height to perform a crop
            if (cropWidth != null && cropHeight != null) {
                item = ImageEditor.Static.crop(editor, item, options, cropX, cropY, cropWidth, cropHeight);
            }

            // Requires only one of either the width or the height to perform a resize
            if (width != null || height != null) {
                item = ImageEditor.Static.resize(editor, item, options, width, height);
            }

            String url = item.getPublicUrl();
            if (url != null) {
                if (srcAttributes.isEmpty()) {
                    attributes.put("src", url);
                } else {
                    for (String srcAttribute : srcAttributes) {
                        attributes.put(srcAttribute, url);
                    }
                }
            }

            Integer newWidth = findDimension(item, "width");
            Integer newHeight = findDimension(item, "height");
            if (newWidth != null && !hideDimensions) {
                attributes.put("width", String.valueOf(newWidth));
            }
            if (newHeight != null && !hideDimensions) {
                attributes.put("height", String.valueOf(newHeight));
            }

            if (this.attributes != null) {
                attributes.putAll(this.attributes);
            }
        }

        if (standardImageSize != null) {
            attributes.put("data-size", standardImageSize.getInternalName());
        }

        return attributes;
    }

    private static String convertAttributesToHtml(String tagName, Map<String, String> attributes) {
        StringBuilder builder = new StringBuilder();
        if (!attributes.isEmpty()) {
            if (tagName == null) {
                tagName = "img";
            }
            builder.append("<");
            builder.append(tagName);
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                if (!(ObjectUtils.isBlank(key) || ObjectUtils.isBlank(value))) {
                    builder.append(" ");
                    builder.append(StringUtils.escapeHtml(key));
                    builder.append("=\"");
                    builder.append(StringUtils.escapeHtml(value));
                    builder.append("\"");
                }
            }
            builder.append(">");
            if (!"img".equalsIgnoreCase(tagName)) {
                builder.append("</");
                builder.append(tagName);
                builder.append(">");
            }
        }
        return builder.toString();
    }

    /**
     * Finds the dimension {@code name} ("width", or "height") for the given
     * StorageItem {@code item}.
     */
    private static Integer findDimension(StorageItem item, String name) {
        if (item == null) {
            return null;
        }
        Integer dimension = null;
        Map<String, Object> metadata = item.getMetadata();
        if (metadata != null) {
            dimension = ObjectUtils.to(Integer.class, metadata.get(name));
            if (dimension == null || dimension == 0) {
                dimension = null;
            }
        }
        return dimension;
    }

    /**
     * Finds the crop information for the StorageItem {@code item}.
     */
    private static Map<String, ImageCrop> findImageCrops(StorageItem item) {
        if (item == null) {
            return null;
        }
        Map<String, ImageCrop> crops = null;
        Map<String, Object> metadata = item.getMetadata();
        if (metadata != null) {
            crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() { }, metadata.get("cms.crops"));
        }
        if (crops == null) {
            crops = new HashMap<String, ImageCrop>();
        }
        return crops;
    }

    /**
     * @deprecated No replacement
     */
    @Deprecated
    private static String findStorageItemField(State state) {
        String field = null;
        ObjectType objectType = state.getType();
        if (objectType != null) {
            for (ObjectField objectField : objectType.getFields()) {
                if (ObjectField.FILE_TYPE.equals(objectField.getInternalType())) {
                    field = objectField.getInternalName();
                    break;
                }
            }
        }
        return field;
    }

    /**
     * @deprecated No replacement
     */
    @Deprecated
    private static StorageItem findStorageItem(State state, String field) {
        StorageItem item = null;
        if (field != null) {
            Object fieldValue = state.get(field);
            if (fieldValue instanceof StorageItem) {
                item = (StorageItem) fieldValue;
            }
        }
        return item;
    }

    /**
     * @deprecated Use {@link #findImageCrops(StorageItem)} instead
     */
    @Deprecated
    private static Map<String, ImageCrop> findImageCrops(State state, String field) {
        Map<String, ImageCrop> crops = null;
        Object fieldValue = state.get(field);
        if (fieldValue instanceof StorageItem) {
            crops = findImageCrops((StorageItem) fieldValue);
        }
        if (crops == null || crops.isEmpty()) {
            crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() { }, state.getValue(field + ".crops"));
        }
        if (crops == null) {
            crops = new HashMap<String, ImageCrop>();
        }
        return crops;
    }

    /**
     * Finds the dimension value with the given {@code field} and
     * {@code name} from the given {@code state}.
     * @deprecated Use {@link #findDimension(StorageItem, String)} instead.
     */
    @Deprecated
    private static Integer findDimension(State state, String field, String name) {
        Integer dimension = null;
        Object fieldValue = state.get(field);
        if (fieldValue instanceof StorageItem) {
            dimension = findDimension((StorageItem) fieldValue, name);
        }
        if (dimension == null || dimension == 0) {
            dimension = ObjectUtils.to(Integer.class, state.getValue(field + ".metadata/" + name));
            if (dimension == null || dimension == 0) {
                dimension = ObjectUtils.to(Integer.class, state.getValue(field + "." + name));
                if (dimension == null || dimension == 0) {
                    dimension = null;
                }
            }
        }
        return dimension;
    }
}
