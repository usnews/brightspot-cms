package com.psddev.cms.tool.file;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.FileContentType;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.State;
import com.psddev.dari.util.BrightcoveStorageItem;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StorageItem;

public class BrightcoveFileType implements FileContentType {

    @Override
    public double getPriority(StorageItem storageItem) {
        if (!(storageItem instanceof BrightcoveStorageItem)) {
            return DEFAULT_PRIORITY_LEVEL - 1;
        }
        return DEFAULT_PRIORITY_LEVEL + 1;
    }

    @Override
    public void writePreview(ToolPageContext page, State state, StorageItem fieldValue) throws IOException, ServletException {
        String playerKey = ((BrightcoveStorageItem) fieldValue).getPreviewPlayerKey();
        String playerId = ((BrightcoveStorageItem) fieldValue).getPreviewPlayerId();

        if (!ObjectUtils.isBlank(playerKey) && !ObjectUtils.isBlank(playerId)) {
            page.write(page.h("<!-- Start of Brightcove Player -->"));

            page.write(page.h("<!--\n" +
                    "By use of this code snippet, I agree to the Brightcove Publisher T and C\n" +
                    "found at https://accounts.brightcove.com/en/terms-and-conditions/.\n" +
                    "-->"));

            page.writeStart("script",
                    "language", "JavaScript",
                    "type", "text/javascript",
                    "src", "http://admin.brightcove.com/js/BrightcoveExperiences.js");
            page.writeEnd();

            page.writeStart("script", "type", "text/javascript");
                page.writeRaw("\n" +
                        "// Store reference to the player\n" +
                        "var player;\n" +
                        "// Store reference to the modules in the player\n" +
                        "var modVP;\n" +
                        "var modExp;\n" +
                        "var modCon;\n" +
                        "// This method is called when the player loads with the ID of the player\n" +
                        "// We can use that ID to get a reference to the player, and then the modules\n" +
                        "// The name of this method can vary but should match the value you specified\n" +
                        "// in the player publishing code for templateLoadHandler.\n" +
                        "var myTemplateLoaded = function(experienceID) {\n" +
                        "  // Get a reference to the player itself\n" +
                        "  player = brightcove.api.getExperience(experienceID);\n" +
                        "  // Get a reference to individual modules in the player\n" +
                        "  modVP = player.getModule(brightcove.api.modules.APIModules.VIDEO_PLAYER);\n" +
                        "  modExp = player.getModule(brightcove.api.modules.APIModules.EXPERIENCE);\n" +
                        "  modCon = player.getModule(brightcove.api.modules.APIModules.CONTENT);\n" +
                        "  if(modVP.loadVideoByID(" + ((BrightcoveStorageItem) fieldValue).getBrightcoveId() + ") === null) {\n" +
                        "    if(typeof(console) !== 'undefined') { console.log(\"Video with id= " + ((BrightcoveStorageItem) fieldValue).getBrightcoveId() + " could not be found\"); }\n" +
                        "  }\n" +
                        "};");
            page.writeEnd();

            page.writeStart("object", "id", "myExperience", "class", "BrightcoveExperience");
                page.writeTag("param", "name", "bgcolor", "value", "#FFFFFF");
                page.writeTag("param", "name", "width", "value", "480");
                page.writeTag("param", "name", "height", "value", "270");
                page.writeTag("param", "name", "playerId", "value", playerId);
                page.writeTag("param", "name", "playerKey", "value", playerKey);
                page.writeTag("param", "name", "isVid", "value", "true");
                page.writeTag("param", "name", "isUI", "value", "true");
                page.writeTag("param", "name", "dynamicStreaming", "value", "true");
                page.writeTag("param", "name", "includeAPI", "value", "true");
                page.writeTag("param", "name", "templateLoadHandler", "value", "myTemplateLoaded");
            page.writeEnd();

            page.write(page.h("<!--\n" +
                    " This script tag will cause the Brightcove Players defined above it to be created as soon\n" +
                    " as the line is read by the browser. If you wish to have the player instantiated only after\n" +
                    " the rest of the HTML is processed and the page load is complete, remove the line.\n" +
                    " -->"));

            page.writeTag("script", "type", "text/javascript");
                page.writeRaw("brightcove.createExperiences();");
            page.writeEnd();

            page.write(page.h("<!--End of Brightcove Player -->"));
        }
    }
}
