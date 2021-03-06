package seedu.taskManager.ui;

import java.util.logging.Logger;

import javax.swing.KeyStroke;

import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.taskManager.MainApp;
import seedu.taskManager.commons.core.ComponentManager;
import seedu.taskManager.commons.core.Config;
import seedu.taskManager.commons.core.LogsCenter;
import seedu.taskManager.commons.events.storage.DataSavingExceptionEvent;
import seedu.taskManager.commons.events.ui.ExitAppRequestEvent;
import seedu.taskManager.commons.events.ui.HideWindowEvent;
import seedu.taskManager.commons.events.ui.JumpToListRequestEvent;
import seedu.taskManager.commons.events.ui.ScrollToListRequestEvent;
import seedu.taskManager.commons.events.ui.ShowHelpRequestEvent;
import seedu.taskManager.commons.events.ui.ShowWindowEvent;
import seedu.taskManager.commons.util.StringUtil;
import seedu.taskManager.logic.Logic;
import seedu.taskManager.model.UserPrefs;


/**
 * The manager of the UI component.
 */
public class UiManager extends ComponentManager implements Ui {
    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/task_manager_pic.png";
    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private Logic logic;
    private Config config;
    private UserPrefs prefs;
    private MainWindow mainWindow;
    private boolean isShown = true;

    public UiManager(Logic logic, Config config, UserPrefs prefs) {
        super();
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");
        primaryStage.setTitle(config.getAppTitle());

        //Set the application icon.
        primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        try {
            mainWindow = new MainWindow(primaryStage, config, prefs, logic);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts();

            //@@author A0140072X
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            raise(new ExitAppRequestEvent());
                        }
                    });
                }
            });
            final Provider provider = Provider.getCurrentProvider(false);
            HotKeyListener listener = new HotKeyListener() {
                public void onHotKey(HotKey hotKey) {
                    if (isShown) {
                        raise(new HideWindowEvent());
                        isShown = false;
                    } else {
                        raise(new ShowWindowEvent());
                        isShown = true;
                    }
                }
            };
            provider.register(KeyStroke.getKeyStroke("control SPACE"), listener);

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }
    //@@author A0140072X
    @Override
    public void refresh() {

        try {

            mainWindow.fillInnerParts();

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }
    //@@author A0140072X
    @Override
    public void loadData(Logic logic) {
        try {
            mainWindow.loadLogic(logic);
            mainWindow.fillInnerParts();
        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }
    //@@author A0140072X
    @Override
    public void show() {
        mainWindow.show();
    }
    //@@author A0140072X
    @Override
    public void hide() {
        mainWindow.hide();
    }
    //@@author
    @Override
    public void stop() {
        prefs.updateLastUsedGuiSetting(mainWindow.getCurrentGuiSetting());
        mainWindow.hide();
    }

    private void showFileOperationAlertAndWait(String description, String details, Throwable cause) {
        final String content = details + ":\n" + cause.toString();
        showAlertDialogAndWait(AlertType.ERROR, "File Op Error", description, content);
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(mainWindow.getPrimaryStage(), type, title, headerText, contentText);
    }

    private static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
            String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.showAndWait();
    }

    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(Alert.AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }

    //==================== Event Handling Code ===============================================================


    @Subscribe
    private void handleDataSavingExceptionEvent(DataSavingExceptionEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        showFileOperationAlertAndWait("Could not save data", "Could not save data to file", event.exception);
    }

    @Subscribe
    private void handleShowHelpEvent(ShowHelpRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        mainWindow.handleHelp();
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        mainWindow.getTaskListPanel().scrollTo(event.targetIndex);
    }

    //@@author A0139509X
    @Subscribe
    private void handleScrollToListRequestEvent(ScrollToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        mainWindow.getTaskListPanel().scollToWithoutSelecting(event.targetIndex);
    }

}
