package seedu.taskManager;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.taskManager.commons.core.Config;
import seedu.taskManager.commons.core.EventsCenter;
import seedu.taskManager.commons.core.LogsCenter;
import seedu.taskManager.commons.core.Version;
import seedu.taskManager.commons.events.ui.ExitAppRequestEvent;
import seedu.taskManager.commons.events.ui.HideWindowEvent;
import seedu.taskManager.commons.events.ui.LoadRequestEvent;
import seedu.taskManager.commons.events.ui.SaveRequestEvent;
import seedu.taskManager.commons.events.ui.ShowWindowEvent;
import seedu.taskManager.commons.exceptions.DataConversionException;
import seedu.taskManager.commons.util.ConfigUtil;
import seedu.taskManager.commons.util.StringUtil;
import seedu.taskManager.logic.Logic;
import seedu.taskManager.logic.LogicManager;
import seedu.taskManager.model.Model;
import seedu.taskManager.model.ModelManager;
import seedu.taskManager.model.ReadOnlyTaskManager;
import seedu.taskManager.model.TaskManager;
import seedu.taskManager.model.UserPrefs;
import seedu.taskManager.model.util.SampleDataUtil;
import seedu.taskManager.storage.Storage;
import seedu.taskManager.storage.StorageManager;
import seedu.taskManager.ui.Ui;
import seedu.taskManager.ui.UiManager;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {
    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    public static final Version VERSION = new Version(1, 0, 0, true);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;
    protected UserPrefs userPrefs;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing TaskManager ]===========================");
        super.init();
        Platform.setImplicitExit(false);

        config = initConfig("config.json");

        storage = new StorageManager(config.getTaskManagerFilePath(), config.getUserPrefsFilePath());

        userPrefs = initPrefs(config);

        initLogging(config);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, config, userPrefs);

        initEventsCenter();
    }

    private String getApplicationParameter(String parameterName) {
        Map<String, String> applicationParameters = getParameters().getNamed();
        return applicationParameters.get(parameterName);
    }

    private Model initModelManager(Storage storage, UserPrefs userPrefs) {
        Optional<ReadOnlyTaskManager> taskManagerOptional;
        ReadOnlyTaskManager initialData;
        try {
            taskManagerOptional = storage.readTaskManager();
            if (!taskManagerOptional.isPresent()) {
                logger.info("Data file not found. Will be starting with a sample TaskManager");
            }
            initialData = taskManagerOptional.orElseGet(SampleDataUtil::getSampleTaskManager);
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty TaskManager");
            initialData = new TaskManager();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty TaskManager");
            initialData = new TaskManager();
        }

        return new ModelManager(initialData, userPrefs);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    protected Config initConfig(String configFilePath) {
        Config initializedConfig;
        String configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + configFilePathUsed + " is not in the correct format. "
                    + "Using default config properties");
            initializedConfig = new Config();
        }

        // Update config file in case it was missing to begin with or there are
        // new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    protected UserPrefs initPrefs(Config config) {
        assert config != null;

        String prefsFilePath = config.getUserPrefsFilePath();
        logger.info("Using prefs file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataConversionException e) {
            logger.warning("UserPrefs file at " + prefsFilePath + " is not in the correct format. "
                    + "Using default user prefs");
            initializedPrefs = new UserPrefs();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty TaskManager");
            initializedPrefs = new UserPrefs();
        }

        // Update prefs file in case it was missing to begin with or there are
        // new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    private void initEventsCenter() {
        EventsCenter.getInstance().registerHandler(this);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting TaskManager " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping Task Manager ] =============================");
        ui.stop();
        try {
            storage.saveUserPrefs(userPrefs);
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
        Platform.exit();
        System.exit(0);
    }

    // @@author A0140072X
    @Subscribe
    private void handleDataSavingEvent(SaveRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        storage.setTaskManagerFilePath(event.getFilePath());
        config.setTaskManagerFilePath(event.filePath.toString());
        ui.refresh();
        try {
            storage.saveTaskManager(model.getTaskManager(), event.getFilePath());
            ConfigUtil.saveConfig(config, Config.DEFAULT_CONFIG_FILE);
        } catch (IOException e) {
            logger.warning("Problem while trying to save config file");
        }
    }

    // @@author A0140072X
    @Subscribe
    private void handleDataLoadingEvent(LoadRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        storage.setTaskManagerFilePath(event.getFilePath());
        config.setTaskManagerFilePath(event.filePath.toString());
        model = initModelManager(storage, userPrefs);
        logic = new LogicManager(model, storage);
        ui.loadData(logic);
        try {
            ConfigUtil.saveConfig(config, Config.DEFAULT_CONFIG_FILE);
        } catch (IOException e) {
            logger.warning("Problem while trying to save config file");
        }
    }

    // @@author A0140072X
    @Subscribe
    private void handleShowWindowEvent(ShowWindowEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ui.show();
            }
        });
    }

    @Subscribe
    private void handleHideWindowEvent(HideWindowEvent event) {
        // ui.hide();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ui.hide();

            }
        });

    }

    @Subscribe
    public void handleExitAppRequestEvent(ExitAppRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        this.stop();

    }

    public static void main(String[] args) {

        launch(args);
    }
}