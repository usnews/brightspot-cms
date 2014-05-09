package com.psddev.launcher;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

public class MavenOutputToTextArea implements InvocationOutputHandler {

    private final TextArea textArea;

    public MavenOutputToTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void consumeLine(final String line) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                textArea.appendText(line);
                textArea.appendText("\n");
            }
        });
    }
}
