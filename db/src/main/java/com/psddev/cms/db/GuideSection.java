package com.psddev.cms.db;


import com.psddev.dari.db.Record;
import com.psddev.dari.db.ReferentialText;



/**
 * Production Guide class to hold information about layout sections. Instances of this class are generally not stored
 * separately, but embedded in the GuidePage objects
 *
 */
@Record.LabelFields({ "sectionName" })
public class GuideSection extends Record {

    @Required
    @ToolUi.Note("Section name in template definition")
    private String sectionName;

    // Would rather use this for uniqueness, but at this time, the sectionId can change anytime
    // a template is saved.
    //@Required
    //private UUID sectionId;

    @ToolUi.Note("Production Guide description for this section")
    private ReferentialText description;

    @ToolUi.Note("Production Guide Tips for this section")
    private ReferentialText tips;

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

//    public UUID getSectionId() {
//        return sectionId;
//    }
//
//    public void setSectionId(UUID sectionId) {
//        this.sectionId = sectionId;
//    }

    public ReferentialText getDescription() {
        return description;
    }

    public void setDescription(ReferentialText description) {
        this.description = description;
    }

    public ReferentialText getTips() {
        return tips;
    }

    public void setTips(ReferentialText tips) {
        this.tips = tips;
    }

}
