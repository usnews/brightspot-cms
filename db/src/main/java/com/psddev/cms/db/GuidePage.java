package com.psddev.cms.db;


import com.psddev.dari.db.Record;
import com.psddev.dari.db.ReferentialText;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Production Guide class to hold information about Pages and Templates (objects with layouts)
 * 
 */
@Record.LabelFields({ "pageType/name" })
public class GuidePage extends Record {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GuidePage.class);

	@ToolUi.Note("Page type for Production Guide information. On Save, the Section list will be populated with all sections in the current layout")
	@Required
	@Indexed(unique = true)
	@DisplayName("Template or One-Off Page")
	Page pageType;

	@ToolUi.Note("Production Guide summary for this page type")
	@DisplayName("Summary")
	ReferentialText description;

	@ToolUi.Note("Sample (Published) Page as documentation example for this page/template")
	private Content samplePage;

	@ToolUi.Note("Production Guide field descriptions for this content type (Note that any fields within an embedded type should be defined separately in a Guide for the embedded type)")
	@Embedded
	private List<GuideSection> sectionDescriptions;

	public Page getPageType() {
		return pageType;
	}

	public void setPageType(Page pageType) {
		this.pageType = pageType;
	}

	public ReferentialText getDescription() {
		return description;
	}

	public void setDescription(ReferentialText description) {
		this.description = description;
	}

	public List<GuideSection> getSectionDescriptions() {
		return sectionDescriptions;
	}

	public void setSectionDescriptions(List<GuideSection> sectionDescriptions) {
		this.sectionDescriptions = sectionDescriptions;
	}

	public Content getSamplePage() {
		return samplePage;
	}

	public void setSamplePage(Content samplePage) {
		this.samplePage = samplePage;
	}

	public ReferentialText getSectionDescription(Section section) {
		if (section != null) {
			GuideSection gs = findOrCreateSectionGuide(section);
			if (gs != null) {
				return gs.getDescription();
			}
		}
		return null;
	}

	public ReferentialText getSectionTips(Section section) {
		if (section != null) {
			GuideSection gs = findOrCreateSectionGuide(section);
			if (gs != null) {
				return gs.getTips();
			}
		}
		return null;
	}

	public GuideSection findOrCreateSectionGuide(Section section) {
		if (sectionDescriptions == null) {
			sectionDescriptions = new ArrayList<GuideSection>();
		}
		if (!sectionDescriptions.isEmpty()) {
			for (GuideSection gs : sectionDescriptions) {
				if (gs.getSectionId() != null
						&& gs.getSectionId().equals(section.getId())) {
					return gs;
				}
			}
		}
		// else create it
		GuideSection gs = new GuideSection();
		if (section.getName() == null || section.getName().isEmpty()) {
			gs.setSectionName(section.getClass().getSimpleName());
		} else {
			gs.setSectionName(section.getName());
		}
		gs.setSectionId(section.getId());
		sectionDescriptions.add(gs);
		return gs;
	}

	public void generateSectionDescriptionList() {
		// Create an entry for each field
		Page type = getPageType();
		if (type != null) {
			Iterable<Section> sections = type.findSections();
			if (sections != null) {
				for (Section section : sections) {
					findOrCreateSectionGuide(section);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.psddev.dari.db.Record#beforeSave()
	 */
	public void beforeSave() {
		generateSectionDescriptionList();
	}

}