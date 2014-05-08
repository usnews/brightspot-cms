package com.psddev.installer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.exec.*;

import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class InstallController {

    private static final Logger LOG = LoggerFactory.getLogger(InstallController.class);

    public static final String CHECK_VALID = "OK";

    public static final String CHECK_INVALID = "BAD";


    @FXML
    Parent root;

    @FXML
    TabPane tabs;

    @FXML
    Label mysqlStatusLabel;

    @FXML
    Label mysqlStatusReport;

    @FXML
    Label mavenStatusLabel;

    @FXML
    Label mavenStatusReport;

    @FXML
    Label installDirectoryLabel;

    @FXML
    Tab projectTab;

    @FXML
    TextField groupId;

    @FXML
    TextField artifactId;

    @FXML
    CheckBox frontendCore;

    @FXML
    CheckBox frontendCommon;

    @FXML
    Button runButton;

    @FXML
    CheckBox runCargo;

    private String classPrefix;

    private String namespaceUri;

    private File installDirectory;

    @FXML
    protected void initialize() {

        //-- Check user's environment for Brightspot 3rd party requirements

        boolean hasMySql = hasMySQL();
        String mysqlStatusString = (hasMySql) ? CHECK_VALID : CHECK_INVALID;
        mysqlStatusLabel.setText(mysqlStatusString);

        boolean hasMaven = hasMaven();
        String mavenStatusString = (hasMaven) ? CHECK_VALID : CHECK_INVALID;
        mavenStatusLabel.setText(mavenStatusString);

        if (!hasMaven || !hasMySql) {
            projectTab.setDisable(true);
        }

        //-- Event handlers
        groupId.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> value, Boolean isOld, Boolean isNew) {
                if (!isNew) {
                    updateHiddenFields();
                }
            }
        });

        artifactId.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> value, Boolean isOld, Boolean isNew) {
                if (!isNew) {
                    updateHiddenFields();
                }
            }
        });
    }

    public boolean isValidToRun() {
        if (!StringUtils.isBlank(artifactId.getText()) &&
            !StringUtils.isBlank(groupId.getText()) &&
            !StringUtils.isBlank(classPrefix) &&
            !StringUtils.isBlank(namespaceUri) &&
            (installDirectory != null)) {

            return true;
        }

        return false;
    }

    private void updateHiddenFields() {

        //-- set namespaceUri field
        String groupIdText = groupId.getText();
        String artifactIdText = artifactId.getText();
        if (!StringUtils.isBlank(groupIdText) && !StringUtils.isBlank(artifactIdText)) {
            String[] tmp = StringUtils.split(groupIdText, ".");
            ArrayUtils.reverse(tmp);

            namespaceUri = artifactIdText + "." + StringUtils.join(tmp,".");

            if (isValidToRun()) {
                runButton.setDisable(false);
            }
        }

        //-- set classPrefix
        if (!StringUtils.isBlank(artifactIdText)) {
            classPrefix = StringUtils.capitalize(artifactIdText);

            if (isValidToRun()) {
                runButton.setDisable(false);
            }
        }

        //-- see if valid to run the install
        if (isValidToRun()) {
            runButton.setDisable(false);
        }
    }

    public void pickDirectory() {
        Stage stage = (Stage) root.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Installation Directory");
        directoryChooser.setInitialDirectory(SystemUtils.getUserHome());

        installDirectory = directoryChooser.showDialog(stage);

        if (installDirectory != null) {
            installDirectoryLabel.setText(installDirectory.getAbsolutePath());

            updateHiddenFields();
        }
    }

    public boolean hasMySQL() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/");
            mysqlStatusReport.setText("Connected to MySQL on Port 3306");

            return true;

        } catch (Exception e) {
            mysqlStatusReport.setText(e.getMessage());

        } finally {
            if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
        }

        return false;
    }

    public boolean hasMaven() {
        MavenCommandLineBuilder builder = new MavenCommandLineBuilder();
        InvocationRequest request = new DefaultInvocationRequest();

        String result = null;
        Properties p = null;
        try {
            p = CommandLineUtils.getSystemEnvVars();

            result  = builder.build(request).getExecutable();

            if (!StringUtils.isBlank(result)) {
                mavenStatusReport.setText("Found Maven at: " + result);
                return true;
            }
        } catch (Exception e) {
            result = e.getMessage();
        }

        mavenStatusReport.setText(result);
        return false;
    }

    public void setFrontendCommon() {
        if (frontendCommon.isSelected()) {
            frontendCore.setSelected(true);
        }
    }

    /*
     * Execute the Maven install for Brightspot.
     */
    public void runInstall() {
        SelectionModel selectionModel = tabs.getSelectionModel();

        // add new tab
        Tab installTab = new Tab();
        installTab.setClosable(true);
        installTab.setText("Installing...");

        tabs.getTabs().add(installTab);
        selectionModel.select(installTab);

        // create layout
        VBox box = new VBox();
        box.setPadding(new Insets(20, 20, 20, 20));

        ObservableList children = box.getChildren();

        HBox progressBox = new HBox();
        progressBox.setSpacing(10);
        progressBox.setPadding(new Insets(0, 0, 23, 0));

        final Label progressLabel = new Label();
        progressLabel.setText("Installing");

        ProgressBar progress = new ProgressBar();
        progress.setPrefWidth(200);
        progressBox.getChildren().addAll(progressLabel, progress);

        Label installLogLabel = new Label();
        installLogLabel.setText("Install Log");

        final TextArea installLog = new TextArea();
        installLog.setStyle("-fx-font-size: 9");
        installLog.setMinHeight(262);

        children.addAll(progressBox, installLogLabel, installLog);

        installTab.setContent(box);

        // start maven install
        Task task = new Task<Void>() {

            @Override public Void call() {

                double installCount = 1;
                double runCount = 0;

                if (frontendCore.isSelected()) {
                    installCount++;
                }

                if (frontendCommon.isSelected()) {
                    installCount++;
                }

                if (runCargo.isSelected()) {
                    installCount++;
                }

                Invoker invoker = new DefaultInvoker();
                InvocationOutputHandler handler = new MavenOutputHandler(installLog);
                invoker.setOutputHandler(handler);

                InvocationRequest request = new DefaultInvocationRequest();
                request.setBaseDirectory(installDirectory);
                request.setInteractive(false);
                request.setGoals(Collections.singletonList("archetype:generate"));

                // Brightspot CMS
                StringBuilder options = new StringBuilder();
                options.append(" -DarchetypeRepository=http://public.psddev.com/maven ");
                options.append(" -DarchetypeGroupId=com.psddev ");
                options.append(" -DarchetypeArtifactId=cms-app-archetype ");
                options.append(" -DarchetypeVersion=2.4-SNAPSHOT");
                options.append(" -DgroupId=");
                options.append(groupId.getText());
                options.append(" -DartifactId=");
                options.append(artifactId.getText());

                request.setMavenOpts(options.toString());

                runCount += .5;
                updateProgress(runCount, installCount);

                try {
                    InvocationResult result = invoker.execute(request);
                    runCount += .5;
                    updateProgress(runCount, installCount);
                } catch (MavenInvocationException e) { }

                // Frontend Core
                if (frontendCore.isSelected()) {
                    options.setLength(0);

                    options.append(" -DarchetypeGroupId=com.psddev ");
                    options.append(" -DarchetypeArtifactId=cms-content-common-archetype ");
                    options.append(" -DarchetypeVersion=1.0-SNAPSHOT ");
                    options.append(" -Dversion=1.0-SNAPSHOT ");
                    options.append(" -DgroupId=");
                    options.append(groupId.getText());
                    options.append(" -DartifactId=");
                    options.append(artifactId.getText());
                    options.append(" -DclassPrefix=");
                    options.append(classPrefix);
                    options.append(" -DnamespaceUri=");
                    options.append(namespaceUri);

                    request.setMavenOpts(options.toString());

                    runCount += .5;
                    updateProgress(runCount, installCount);

                    try {
                        InvocationResult result = invoker.execute(request);

                        runCount += .5;
                        updateProgress(runCount, installCount);
                    } catch (MavenInvocationException e) { }
                }

                // Frontend Common (Article, Author...)
                if (frontendCommon.isSelected()) {
                    options.setLength(0);

                    options.append(" -Ddari.version=2.4-SNAPSHOT ");
                    options.append(" -DarchetypeGroupId=com.psddev ");
                    options.append(" -DarchetypeArtifactId=cms-content-article-archetype ");
                    options.append(" -DarchetypeVersion=1.0-SNAPSHOT ");
                    options.append(" -Dversion=1.0-SNAPSHOT ");
                    options.append(" -DgroupId=");
                    options.append(groupId.getText());
                    options.append(" -DartifactId=");
                    options.append(artifactId.getText());

                    request.setMavenOpts(options.toString());

                    runCount += .5;
                    updateProgress(runCount, installCount);

                    try {
                        InvocationResult result = invoker.execute(request);

                        runCount += .5;
                        updateProgress(runCount, installCount);
                    } catch (MavenInvocationException e) { }
                }

                /*
                if (runCargo.isSelected()) {
                    runCount += .5;
                    updateProgress(runCount, installCount);

                    File projectDir = new File(installDirectory.getAbsolutePath() + "/" + artifactId.getText());

                    request.setBaseDirectory(projectDir);
                    request.setShowErrors(true);

                    String[] goals = {"package", "cargo:run"};
                    request.setGoals(Arrays.asList(goals));

                    options.setLength(0);

                    request.setMavenOpts(options.toString());

                    try {
                        InvocationResult result = invoker.execute(request);

                        runCount += .5;
                        updateProgress(runCount, installCount);
                    } catch (MavenInvocationException e) { }
                }
                */

                return null;
            }
        };

        progress.progressProperty().bind(task.progressProperty());

        new Thread(task).start();

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent workerStateEvent) {
                progressLabel.setText("Done");
            }
        });
    }
}