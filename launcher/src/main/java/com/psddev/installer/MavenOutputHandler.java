package com.psddev.installer;

import javafx.scene.control.TextArea;
import org.apache.maven.shared.invoker.PrintStreamHandler;

/**
 * Writes output of Maven Invoker to JavaFX {@code TextArea}.
 */
public class MavenOutputHandler extends PrintStreamHandler {
    private TextArea text;

    public MavenOutputHandler(TextArea text) {
        this.text = text;
    }

    @Override
    public void consumeLine(String line) {
        text.setText(text.getText() + line + "\n");
    }
}