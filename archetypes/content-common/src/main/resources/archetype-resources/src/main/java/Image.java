package ${groupId};

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Renderer;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.LocalStorageItem;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

import java.io.File;

/**
 *  Simple Image Implementation
 */
@Recordable.PreviewField("file")
@Renderer.EmbedPath("/WEB-INF/renderer/layout/embed.jsp")
@Renderer.EmbedPreviewWidth(300)
@Renderer.Paths({
        @Renderer.Path(value = "/WEB-INF/renderer/enhancement/image.jsp") //context = "enhancement",
})
@ToolUi.Referenceable
public class Image extends Content {

    @Indexed
    @Required
    private String title;

    @ToolUi.Placeholder(dynamicText = "${content.title}", editable = true)
    private String altText;

    private String caption;

    @Required
    private StorageItem file;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public StorageItem getFile() {
        return file;
    }

    public void setFile(StorageItem file) {
        this.file = file;
    }

    /**
     *
     * @return Published <code>altText</code> field or if empty the <code>title</code> field.
     */
    public String getAltText() {
        if (StringUtils.isBlank(altText)) {
            return title;
        }
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    @Override
    protected void beforeDelete() {
        if (file instanceof LocalStorageItem) {
            deleteImageStorage((LocalStorageItem) file);
        }
    }

    //Delete the item off of storage when the image object is deleted
    private void deleteImageStorage(LocalStorageItem item) {
        new File(item.getRootPath() + "/" + item.getPath()).delete();
    }
}
