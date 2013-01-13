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
	ObjectType type;
	
	@ToolUi.Note("Production Guide information about this content type")
	@ToolUi.Hidden
	// No plan for this yet - may not be needed
	ReferentialText description;
	
	@ToolUi.Note("Production Guide field descriptions for this content type")
	List<GuideField> fieldDescriptions;
	
	
	public ObjectType getType() {
		return type;
	}

	public void setType(ObjectType type) {
		this.type = type;
		// Automatically add a place holder for every known field of this type
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

	public void setFieldDescriptions(List<GuideField> fieldDescriptions) {
		this.fieldDescriptions = fieldDescriptions;
	}
	
	public ReferentialText getFieldDescription(String fieldName) {
		ReferentialText desc = null;
		if (fieldDescriptions != null) {
			for (GuideField gf : fieldDescriptions) {
				if (gf.getFieldName().equals(fieldName)) {
					return gf.getDescription();
				}
			}
		}
		return desc;
	}

	@Record.Embedded
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
					return guide.getFieldDescription(fieldName);
				}
			}
			return null;
		}
		
		public static boolean hasFieldGuideInfo (State state, String fieldName) {
			ObjectField field = state.getField(fieldName);
			if (field.isRequired()) return true;
			if (field.getMaximum() != null) return true;
			if (field.getMinimum() != null) return true;
			if (getFieldDescription(state, fieldName) != null) return true;
			
			return false;
		}
		
		public static GuideType getGuideType(ObjectType objectType) {
			return Query.from(GuideType.class).where("type = ?",objectType).first();
		}
		
	}

}