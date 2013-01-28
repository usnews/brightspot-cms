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

	@ToolUi.Note("Page type for Production Guide information. On Save, the Section list will be populated with all non-container sections in the current layout")
	@Required
	@Indexed(unique = true)
	@DisplayName("Template or One-Off Page")
	Page pageType;

	@ToolUi.Note("Production Guide summary for this page type")
	@DisplayName("Summary")
	ReferentialText description;

	@ToolUi.Note("Sample (Published) Page as documentation example for this page/template")
	private Content samplePage;

	@ToolUi.Note("Production Guide section descriptions for this page/template")
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
		if (section instanceof ContainerSection) {
			return null;
		}
		// Only allowed to create a production guide for a named section
		if (section.getName() == null || section.getName().isEmpty()) {
			return null;
		}
		if (sectionDescriptions == null) {
			sectionDescriptions = new ArrayList<GuideSection>();
		}
		if (!sectionDescriptions.isEmpty()) {
			for (GuideSection gs : sectionDescriptions) {
				if (gs.getSectionName() != null
						&& gs.getSectionName().equals(section.getName())) {
					return gs;
				}
			}
		}
		// else create it
		GuideSection gs = new GuideSection();
		gs.setSectionName(section.getName());
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