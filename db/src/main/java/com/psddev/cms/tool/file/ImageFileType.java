package com.psddev.cms.tool.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.psddev.cms.db.ImageCrop;
import com.psddev.cms.db.ImageTag;
import com.psddev.cms.db.ResizeOption;
import com.psddev.cms.db.StandardImageSize;
import com.psddev.cms.tool.FileContentType;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ColorDistribution;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.DimsImageEditor;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.JavaImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeReference;

public class ImageFileType implements FileContentType {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageFileType.class);

    @Override
    public boolean isSupported(StorageItem storageItem) {
        String contentType = storageItem.getContentType();
        return !StringUtils.isBlank(contentType) && contentType.startsWith("image/");
    }

    @Override
    public boolean isPreferred(StorageItem storageItem) {
        return false;
    }

    @Override
    public void writePreview(ToolPageContext page, State state, StorageItem fieldValue) throws IOException, ServletException {
        writeImageEditor(page, state, fieldValue);
    }

    /**
     * Writes the image editor for display in the CMS
     * @param page
     * @throws IOException
     * @throws ServletException
     */
    public void writeImageEditor(ToolPageContext page, State state, StorageItem fieldValue) throws IOException, ServletException {

        Class hotspotClass = ObjectUtils.getClassByName(ImageTag.HOTSPOT_CLASS);
        boolean projectUsingBrightSpotImage = hotspotClass != null && !ObjectUtils.isBlank(ClassFinder.Static.findClasses(hotspotClass));

        if (projectUsingBrightSpotImage) {
            page.include("set/hotSpot.jsp");
        }

        page.writeStart("div", "class", "imageEditor");
            writeImageEditorAside(page, fieldValue, state);
            writeImageEditorImage(page, fieldValue);
        page.writeEnd();

        if (projectUsingBrightSpotImage) {
            page.include("set/hotSpot.jsp");
        }
    }

    /**
     * Wrapper for writing image editor tools via:
     * {@link #writeImageEditorTools(ToolPageContext, StorageItem, State)}
     * {@link #writeImageEditorEdit(ToolPageContext, Map)}
     * {@link #writeImageEditorSizes(ToolPageContext, State, Map)}
     * @param page
     * @param fieldValue
     * @param state
     * @throws IOException
     */
    private void writeImageEditorAside(ToolPageContext page, StorageItem fieldValue, State state) throws IOException {

        Map<String, Object> fieldValueMetadata = null;
        if (fieldValue != null) {
            fieldValueMetadata = fieldValue.getMetadata();
        }

        if (fieldValueMetadata == null) {
            fieldValueMetadata = new LinkedHashMap<String, Object>();
        }

        page.writeStart("div", "class", "imageEditor-aside");
            writeImageEditorTools(page, fieldValue, state);
            writeImageEditorEdit(page, fieldValueMetadata);
            writeImageEditorSizes(page, state, fieldValueMetadata);
        page.writeEnd();
    }

    /**
     * Writes some tools for the Image Editor, including:
     * View Original link, View Resized Link and (Optional) {@link com.psddev.dari.db.ColorDistribution.Data}
     * @param page
     * @param fieldValue
     * @param state
     * @throws IOException
     */
    private void writeImageEditorTools(ToolPageContext page, StorageItem fieldValue, State state) throws IOException {

        page.writeStart("div", "class", "imageEditor-tools");
            page.writeStart("h2");
                page.write("Tools");
            page.writeEnd();

            page.writeStart("ul");

                if (state.as(ColorDistribution.Data.class).getDistribution() != null) {
                    page.writeStart("li");
                        page.writeStart("a",
                                "class", "icon icon-tint",
                                "href", page.h(page.cmsUrl("/contentColors", "id", state.getId())),
                                "target", "contentColors");
                            page.write("Colors");
                        page.writeEnd();
                    page.writeEnd();
                }

                page.writeStart("li");
                    page.writeStart("a",
                            "class", "action-preview",
                            "href", page.h(fieldValue.getPublicUrl()),
                            "target", "_blank");
                        page.write("View Original");
                    page.writeEnd();
                page.writeEnd();

                page.writeStart("li");
                    page.writeStart("a",
                            "class", "icon icon-crop",
                            "href", page.h(page.url("/contentImages", "data", ObjectUtils.toJson(fieldValue))),
                            "target", "contentImages");
                        page.write("View Resized");
                    page.writeEnd();
                page.writeEnd();

            page.writeEnd();

        page.writeEnd();
    }

    /**
     * Writes the inputs for image editing:
     * blurs and all {@link com.psddev.cms.tool.file.ImageFileType.ImageAdjustment} types
     * @param page
     * @param fieldValueMetadata
     * @throws IOException
     */
    private void writeImageEditorEdit(ToolPageContext page, Map<String, Object> fieldValueMetadata) throws IOException {
        HttpServletRequest request = page.getRequest();
        String inputName = ObjectUtils.firstNonBlank(page.param(String.class, "inputName"),  (String) request.getAttribute("inputName"));
        boolean useJavaImageEditor = ImageEditor.Static.getDefault() != null && (ImageEditor.Static.getDefault() instanceof JavaImageEditor);
        String blurName = inputName + ".blur";

        Map<String, Object> edits = (Map<String, Object>) fieldValueMetadata.get("cms.edits");

        if (edits == null) {
            edits = new HashMap<String, Object>();
            fieldValueMetadata.put("cms.edits", edits);
        }

        List<String> blurs = new ArrayList<String>();
        if (!ObjectUtils.isBlank(edits.get("blur"))) {
            Object blur = edits.get("blur");
            if (blur instanceof String && ObjectUtils.to(String.class, blur).matches("(\\d+x){3}\\d+")) {
                blurs.add(ObjectUtils.to(String.class, blur));
            } else if (blur instanceof List) {
                for (Object blurItem : (List) blur) {
                    String blurValue = ObjectUtils.to(String.class, blurItem);
                    if (blurValue.matches("(\\d+x){3}\\d+")) {
                        blurs.add(blurValue);
                    }
                }
            }
        }

        page.writeStart("div", "class", "imageEditor-edit");

            page.writeStart("h2");
                page.write("Adjustments");
            page.writeEnd();

            page.writeStart("table");
                page.writeStart("tbody");

                    if (useJavaImageEditor) {
                        page.writeStart("tr");
                            page.writeStart("th");
                                page.write("Blur");
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeStart("a",
                                        "class", "imageEditor-addBlurOverlay");
                                    page.write("Add Blur");
                                page.writeEnd();
                                page.writeTag("br");

                                if (!ObjectUtils.isBlank(blurs)) {
                                    for (String blur : blurs) {
                                        page.writeTag("input",
                                                "type", "hidden",
                                                "name", page.h(blurName),
                                                "value", blur);
                                    }
                                }
                            page.writeEnd();
                        page.writeEnd();
                    }

                    for (ImageAdjustment adj : ImageAdjustment.values()) {
                        page.writeStart("tr");
                            page.writeStart("th");
                                page.writeHtml(page.h(StringUtils.toPascalCase(adj.title)));
                            page.writeEnd();
                            page.writeStart("td");
                            if (!adj.javaImageEditorOnly || useJavaImageEditor) {
                                boolean isCheckbox = adj.inputType.equals("checkbox");
                                boolean isChecked = isCheckbox && ObjectUtils.equals(edits.get(adj.title), true);
                                page.writeTag("input",
                                        "type", adj.inputType,
                                        "name", inputName + "." + adj.title,
                                        adj.inputType.equals("range") ? "min" : "", adj.inputType.equals("range") ? adj.min : "",
                                        adj.inputType.equals("range") ? "max" : "", adj.inputType.equals("range") ? adj.max : "",
                                        adj.inputType.equals("range") ? "step" : "", adj.inputType.equals("range") ? adj.step : "",
                                        "value", ObjectUtils.to(adj.valueType, isCheckbox ? true : edits.get(adj.title)),
                                        isChecked ? "checked" : "",
                                        isChecked ? "true" : "");
                            }

                            page.writeEnd();
                        page.writeEnd();
                    }

                page.writeEnd();
            page.writeEnd();

        page.writeEnd();
    }

    /**
     * Writes the tools to allow editors to select from {@link StandardImageSize}s
     * and edit their crops
     * @param page
     * @param state
     * @param fieldValueMetadata
     * @throws IOException
     */
    private void writeImageEditorSizes(ToolPageContext page, State state, Map<String, Object> fieldValueMetadata) throws IOException {

        String inputName = ObjectUtils.firstNonBlank(page.param(String.class, "inputName"), (String) page.getRequest().getAttribute("inputName"));
        String cropsFieldName = inputName + ".crops";

        Map<String, ImageCrop> crops = ObjectUtils.to(new TypeReference<Map<String, ImageCrop>>() {
        }, fieldValueMetadata.get("cms.crops"));
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

        ImageEditor defaultImageEditor = ImageEditor.Static.getDefault();
        boolean centerCrop = !(defaultImageEditor instanceof DimsImageEditor) || ((DimsImageEditor) defaultImageEditor).isUseLegacyThumbnail();

        if (crops.isEmpty()) {
            return;
        }

        page.writeStart("div", "class", "imageEditor-sizes");
            page.writeStart("h2");
                page.write("Standard Sizes");
            page.writeEnd();

            page.writeStart("table", "data-crop-center", centerCrop);
                page.writeStart("tbody");
                    for (Map.Entry<String, ImageCrop> e : crops.entrySet()) {
                        String cropId = e.getKey();
                        ImageCrop crop = e.getValue();
                        StandardImageSize size = sizes.get(cropId);
                        if (size == null && ObjectUtils.to(UUID.class, cropId) != null) {
                            continue;
                        }
                        if (size != null) {
                            page.writeStart("tr",
                                    "data-size-name", size.getInternalName(),
                                    "data-size-independent", size.isIndependent(),
                                    "data-size-width", size.getWidth(),
                                    "data-size-height", size.getHeight());
                                page.writeStart("th");
                                    page.write(page.h(size.getDisplayName()));
                                page.writeEnd();
                        } else {
                            page.writeStart("tr");
                                page.writeStart("th");
                                    page.write(page.h(cropId));
                                page.writeEnd();
                        }
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".x"),
                                        "type", "text",
                                        "value", crop.getX());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".y"),
                                        "type", "text",
                                        "value", crop.getY());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".width"),
                                        "type", "text",
                                        "value", crop.getWidth());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".height"),
                                        "type", "text",
                                        "value", crop.getHeight());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".texts"),
                                        "type", "text",
                                        "value", crop.getTexts());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".textSizes"),
                                        "type", "text",
                                        "value", crop.getTextSizes());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".textXs"),
                                        "type", "text",
                                        "value", crop.getTextXs());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".textYs"),
                                        "type", "text",
                                        "value", crop.getTextYs());
                            page.writeEnd();
                            page.writeStart("td");
                                page.writeTag("input",
                                        "name", page.h(cropsFieldName + "." + cropId + ".textWidths"),
                                        "type", "text",
                                        "value", crop.getTextWidths());
                            page.writeEnd();
                        page.writeEnd();
                    }
                page.writeEnd();
            page.writeEnd();
        page.writeEnd();
    }

    /**
     * Writes the image preview
     * @param page
     * @param fieldValue
     * @throws IOException
     */
    private void writeImageEditorImage(ToolPageContext page, StorageItem fieldValue) throws IOException {

        String fieldValueUrl;
        String resizeScale = "";

        if (ImageEditor.Static.getDefault() != null) {
           ImageTag.Builder imageTagBuilder = new ImageTag.Builder(fieldValue)
                   .setWidth(1000)
                   .setResizeOption(ResizeOption.ONLY_SHRINK_LARGER)
                   .setEdits(false);
            Number originalWidth = null;
            if (!ObjectUtils.isBlank(CollectionUtils.getByPath(imageTagBuilder.getItem().getMetadata(), "image/originalWidth"))) {
                originalWidth = (Number) CollectionUtils.getByPath(imageTagBuilder.getItem().getMetadata(), "image/originalWidth");
            } else if (!ObjectUtils.isBlank(CollectionUtils.getByPath(imageTagBuilder.getItem().getMetadata(), "dims/originalWidth"))) {
                originalWidth = (Number) CollectionUtils.getByPath(imageTagBuilder.getItem().getMetadata(), "dims/originalWidth");
            } else if (!ObjectUtils.isBlank(CollectionUtils.getByPath(imageTagBuilder.getItem().getMetadata(), "width"))) {
                originalWidth = (Number) CollectionUtils.getByPath(imageTagBuilder.getItem().getMetadata(), "width");
            }
            if (originalWidth != null) {
                if (originalWidth.intValue() > 1000) {
                    resizeScale = String.format("%.2f", (double) 1000 / originalWidth.intValue());
                }
            }
            fieldValueUrl = imageTagBuilder.toUrl();
        } else {
            fieldValueUrl = fieldValue.getPublicUrl();
        }

        page.writeStart("div", "class", "imageEditor-image");
            page.writeTag("img",
                    "alt", "",
                    "data-scale", resizeScale,
                    "src", page.url("/misc/proxy.jsp",
                            "url", fieldValueUrl,
                            "hash", StringUtils.hex(StringUtils.hmacSha1(Settings.getSecret(), fieldValueUrl))));
        page.writeEnd();
    }

    private enum ImageAdjustment {

        BRIGHTNESS("brightness", -1.0, 1.0, 0.01, double.class),
        CONTRAST("contrast", -1.0, 1.0, 0.01, double.class),
        FLIP_H("flipH"),
        FLIP_V("flipV"),
        INVERT("invert"),
        GRAYSCALE("grayscale"),
        ROTATE("rotate", -90.0, 90.0, 90.0, int.class),
        SEPIA("sepia"),
        SHARPEN("sharpen", true),
        BLUR("blur");

        private String title;
        private String inputType;
        private Class valueType;
        private boolean javaImageEditorOnly = false;
        private double min;
        private double max;
        private double step;

        ImageAdjustment(String title) {
            this.title = title;
            this.inputType = "checkbox";
            this.valueType = boolean.class;
            this.javaImageEditorOnly = false;
        }

        ImageAdjustment(String title, boolean javaImageEditorOnly) {
            this.title = title;
            this.inputType = "checkbox";
            this.valueType = boolean.class;
            this.javaImageEditorOnly = javaImageEditorOnly;
        }

        ImageAdjustment(String title, double min, double max, double step, Class valueType) {
            this.title = title;
            this.min = min;
            this.max = max;
            this.step = step;
            this.inputType = "range";
            this.valueType = valueType;
        }
    }
}
