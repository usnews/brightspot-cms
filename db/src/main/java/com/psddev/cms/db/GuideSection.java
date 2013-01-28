package com.psddev.cms.db;

import java.util.UUID;

import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.db.Record;


/*
 * Production Guide class to hold information about layout sections
 * 
 */
@Record.LabelFields({ "sectionName" })
public class GuideSection extends Record {

	@Required
	@ToolUi.Note("Section name in template definition")
	@Guide.Description("The section name must match the name defined in the template exactly. This happens naturally as these guide sections get automatically created. However, if a section name is changed on the template definition at a later time, it needs to be changed in this production guide object as well. Sections without names and container-only sections cannot have production guide content.")
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

//	public UUID getSectionId() {
//		return sectionId;
//	}
//
//	public void setSectionId(UUID sectionId) {
//		this.sectionId = sectionId;
//	}

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
