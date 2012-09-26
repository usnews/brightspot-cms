package com.psddev.cms.db;

import com.psddev.dari.util.ImageEditor;

public enum ResizeOption {

    IGNORE_ASPECT_RATIO("Ignore Aspect Ratio", ImageEditor.RESIZE_OPTION_IGNORE_ASPECT_RATIO),
    ONLY_SHRINK_LARGER("Only Shrink Larger", ImageEditor.RESIZE_OPTION_ONLY_SHRINK_LARGER),
    ONLY_ENLARGE_SMALLER("Only Enlarge Smaller", ImageEditor.RESIZE_OPTION_ONLY_ENLARGE_SMALLER),
    FILL_AREA("Fill Area", ImageEditor.RESIZE_OPTION_FILL_AREA);

    private final String displayName;
    private final String imageEditorOption;

    private ResizeOption(String displayName, String imageEditorOption) {
        this.displayName = displayName;
        this.imageEditorOption = imageEditorOption;
    }

    public String getImageEditorOption() {
        return imageEditorOption;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public final static class Static {

        private Static() {
        }

        public static ResizeOption fromImageEditorOption(String imageEditorOption) {
            for (ResizeOption option : ResizeOption.values()) {
                if (option.getImageEditorOption().equals(imageEditorOption)) {
                    return option;
                }
            }
            return null;
        }
    }
}
