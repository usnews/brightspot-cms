package com.psddev.installer;

import javafx.scene.control.TextArea;
import org.apache.commons.exec.LogOutputStream;

/**
 * Writes output of Apache Commons Exec to JavaFX {@code TextArea}.
 */
public class ExecLogOutputStream extends LogOutputStream {
    private TextArea text;

    public ExecLogOutputStream(TextArea text) {
        this.text = text;
    }

    @Override protected void processLine(String line, int level) {
        text.setText(text.getText() + line + "\n");
    }
}