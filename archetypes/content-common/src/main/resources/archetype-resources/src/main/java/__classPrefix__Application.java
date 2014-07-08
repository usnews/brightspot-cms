package ${groupId};

import com.psddev.dari.db.Application;


public class ${classPrefix}Application extends Application {

    private boolean useNonMinifiedJs;

    private boolean useNonMinifiedCss;


    public boolean isUseNonMinifiedJs() {
        return useNonMinifiedJs;
    }

    public void setUseNonMinifiedJs(boolean useNonMinifiedJs) {
        this.useNonMinifiedJs = useNonMinifiedJs;
    }

    public boolean isUseNonMinifiedCss() {
        return useNonMinifiedCss; 
    }

    public void setUseNonMinifiedCss(boolean useNonMinifiedCss) {
        this.useNonMinifiedCss = useNonMinifiedCss;
    }

    /** @return Never null */
    public static ${classPrefix}Application getInstance() {
        return Application.Static.getInstance(${classPrefix}Application.class);
    }
}
