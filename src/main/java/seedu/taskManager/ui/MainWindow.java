package seedu.taskManager.ui;

import java.io.File;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import seedu.taskManager.commons.core.Config;
import seedu.taskManager.commons.core.GuiSettings;
import seedu.taskManager.commons.core.LogsCenter;
import seedu.taskManager.commons.events.ui.ExitAppRequestEvent;
import seedu.taskManager.commons.events.ui.LoadRequestEvent;
import seedu.taskManager.commons.events.ui.NewResultAvailableEvent;
import seedu.taskManager.commons.events.ui.SaveRequestEvent;
import seedu.taskManager.commons.util.FxViewUtil;
import seedu.taskManager.logic.Logic;
import seedu.taskManager.logic.commands.CommandResult;
import seedu.taskManager.logic.commands.exceptions.CommandException;
import seedu.taskManager.model.UserPrefs;



/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Region> {

    private static final String ICON = "/images/task_manager_pic.png";
    private static final String FXML = "MainWindow.fxml";
    private static final int MIN_HEIGHT = 600;
    private static final int MIN_WIDTH = 450;
    private static final String TOGGLE = "toggle";

    private Stage primaryStage;
    private Logic logic;
    private CommandBox commandBox;
    private final Logger logger = LogsCenter.getLogger(MainWindow.class);

    // Independent Ui parts residing in this Ui container
    private TaskListPanel taskListPanel;
    private Config config;

    @FXML
    private AnchorPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem loadMenuItem;

    @FXML
    private AnchorPane taskListPanelPlaceholder;

    @FXML
    private AnchorPane resultDisplayPlaceholder;

    @FXML
    private AnchorPane statusbarPlaceholder;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;

        // Configure the UI
        setTitle(config.getAppTitle());
        setIcon(ICON);
        setWindowMinSize();
        setWindowDefaultSize(prefs);
        Scene scene = new Scene(getRoot());
        primaryStage.setScene(scene);

        //add keyboard shortcuts
        setAccelerators();
        //@@author A0139509X
        addKeyPressedFilters(scene);

    }
    //@@author A0139509X
    /**
     * Listener and filter for keyboard shortcuts.
+    * Pressing letter keys focuses on the command box.
+    * Pressing TAB updates the task list shown, from event to task to floating task and to event again
     */
    private void addKeyPressedFilters(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code.isLetterKey()) {
                commandBox.getTextField().requestFocus();
            }
            if (code.equals(KeyCode.TAB)) {
                toggleListView();
            }
        });

    }

    //@@author A0139509X
    private void toggleListView() {
        boolean success;
        try {
            CommandResult commandResult = logic.execute(TOGGLE);

            success = true;
            commandBox.setStyleAfterTab(success);
            // process result of the command
            logger.info("Result: " + commandResult.feedbackToUser);
            raise(new NewResultAvailableEvent(commandResult.feedbackToUser));

        } catch (CommandException e) {
            success = false;
            commandBox.setStyleAfterTab(success);
            // handle command failure
            logger.info("Invalid command: " + TOGGLE);
            raise(new NewResultAvailableEvent(e.getMessage()));
        }
    }
    //@@author
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
        //@@author A0140072X
        setAccelerator(saveMenuItem, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        setAccelerator(loadMenuItem, new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    void fillInnerParts() {
        taskListPanel = new TaskListPanel(getTaskListPlaceholder(), logic.getFilteredTaskList());
        new ResultDisplay(getResultDisplayPlaceholder());
        new StatusBarFooter(getStatusbarPlaceholder(), config.getTaskManagerFilePath());
        commandBox = new CommandBox(getCommandBoxPlaceholder(), logic);
    }
    void loadLogic(Logic logic) {
        this.logic = logic;
    }


    private AnchorPane getCommandBoxPlaceholder() {
        return commandBoxPlaceholder;
    }

    private AnchorPane getStatusbarPlaceholder() {
        return statusbarPlaceholder;
    }

    private AnchorPane getResultDisplayPlaceholder() {
        return resultDisplayPlaceholder;
    }

    private AnchorPane getTaskListPlaceholder() {
        return taskListPanelPlaceholder;
    }

    void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the given image as the icon of the main window.
     * @param iconSource e.g. {@code "/images/help_icon.png"}
     */
    private void setIcon(String iconSource) {
        FxViewUtil.setStageIcon(primaryStage, iconSource);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    private void setWindowMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    @FXML
    public void handleHelp() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.show();
    }
    //@@author A0140072X
    @FXML
    public void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            raise(new SaveRequestEvent(file.toString()));
        }

    }
    //@@author A0140072X
    @FXML
    public void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Load File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            raise(new LoadRequestEvent(file.toString()));
        }

    }
    //@@author
    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    public TaskListPanel getTaskListPanel() {
        return this.taskListPanel;
    }

}
