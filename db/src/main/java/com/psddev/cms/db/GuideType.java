package com.psddev.cms.db;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.db.State;
import com.psddev.dari.db.ValidationException;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PeriodicCache;
import com.psddev.dari.util.PullThroughCache;
import com.psddev.dari.util.PullThroughValue;
import com.psddev.dari.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Production Guide class to hold information about Content types and their associated fields
 * 
 * ObjectType and ObjectField don't currently lend themselves well to Modification classes, otherwise
 * this info would be stored as modifications on the those objects
 */
@Record.LabelFields("type/name")
public class GuideType extends Record {

	private static final Logger LOGGER = LoggerFactory.getLogger(GuideType.class);

	@ToolUi.Note("Content type for Production Guide information")
	@Required
	@Indexed
	ObjectType documentedType;
	
	@ToolUi.Note("Production Guide information about this content type")
	@ToolUi.Hidden
	// No plan for this yet - may not be needed
	ReferentialText description;
	
	@ToolUi.Note("Production Guide field descriptions for this content type (Note that any fields within an embedded type should be defined separately in a Guide for the embedded type)")
	private List<GuideField> fieldDescriptions;

	public ObjectType getDocumentedType() {
		return documentedType;
	}

	public void setDocumentedType(ObjectType documentedType) {
		this.documentedType = documentedType;
	}

    public ReferentialText getDescription() {
		return description;
	}

	public void setDescription(ReferentialText description) {
		this.description = description;
	}	

	public List<GuideField> getFieldDescriptions() {
		return fieldDescriptions;
	}
	
	/*
	 * Add a field description entry. This assumes that the caller already verified an 
	 * entry for this field doesn't already exist
	 */
	public void addFieldDescription(GuideField fieldDescription) {
		if (fieldDescriptions == null) {
			fieldDescriptions = new ArrayList<GuideField>();
		}
		fieldDescriptions.add(fieldDescription);
	}

	public void setFieldDescriptions(List<GuideField> fieldDescriptions) {
		this.fieldDescriptions = fieldDescriptions;
	}
	
	public ReferentialText getFieldDescription(String fieldName, boolean createIfMissing) {
		ReferentialText desc = null;
		if (fieldDescriptions != null) {
			for (GuideField gf : fieldDescriptions) {
				if (gf.getFieldName().equals(fieldName)) {
					return gf.getDescription();
				}
			}
		}
		if (createIfMissing == true) {
			GuideField gf = new GuideField();
			gf.fieldName = fieldName;
			// TODO - pull description from annotation?
			addFieldDescription(gf);
		}
		return desc;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.psddev.dari.db.Record#beforeSave()
	 */
	public void beforeSave() {
		// Create an entry for each field
		ObjectType type = getDocumentedType();
		if (type != null) {
			List<ObjectField> fields = type.getFields();
			for (ObjectField field : fields) {
				getFieldDescription(field.getInternalName(), true);
			}
		}
	}

	@Record.Embedded
	@Record.LabelFields({"fieldName", "description"})
	public static class GuideField extends Record {
		
		@Required
		@Indexed
		@ToolUi.Note("Internal fieldname in this object type")
		String fieldName;
		
		@ToolUi.Note("Production Guide information about this field")
		ReferentialText description;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public ReferentialText getDescription() {
			return description;
		}

		public void setDescription(ReferentialText description) {
			this.description = description;
		}
		
	}
	
	public static final class Static {
		
		public static ReferentialText getFieldDescription(State state, String fieldName) {
			if (state != null) {
				ObjectType typeDefinition = state.getType();
				GuideType guide = getGuideType(typeDefinition);
				if (guide != null) {
					return guide.getFieldDescription(fieldName, false);
				}
			}
			return null;
		}
		
		
		
		public static boolean hasFieldGuideInfo (State state, String fieldName) {
			ObjectField field = state.getField(fieldName);
			if (field.isRequired()) return true;
			if (field.getMaximum() != null) return true;
			if (field.getMinimum() != null) return true;
			ReferentialText desc = getFieldDescription(state, fieldName);
			if (desc != null && !desc.isEmpty()) return true;
			
			return false;
		}
		
		public static GuideType getGuideType(ObjectType objectType) {
			return Query.from(GuideType.class).where("documentedType = ?",objectType).first();
		}
		
	}

}