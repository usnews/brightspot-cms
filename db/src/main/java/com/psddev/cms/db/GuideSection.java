package com.psddev.cms.db;

import java.util.UUID;

import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.db.Record;


/*
 * Production Guide class to hold information about layout sections
 * 
 */
@Record.LabelFields({ "sectionName", "sectionId" })
public class GuideSection extends Record {

	@Required
	private String sectionName;

	@Required
	private UUID sectionId;

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

	public UUID getSectionId() {
		return sectionId;
	}

	public void setSectionId(UUID sectionId) {
		this.sectionId = sectionId;
	}

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
