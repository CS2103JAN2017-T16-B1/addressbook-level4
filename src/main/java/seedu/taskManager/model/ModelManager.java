package seedu.taskManager.model;

import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.transformation.FilteredList;
import seedu.taskManager.commons.core.ComponentManager;
import seedu.taskManager.commons.core.LogsCenter;
import seedu.taskManager.commons.core.UnmodifiableObservableList;
import seedu.taskManager.commons.events.model.TaskManagerChangedEvent;
import seedu.taskManager.commons.events.ui.ScrollToListRequestEvent;
import seedu.taskManager.commons.exceptions.IllegalValueException;
import seedu.taskManager.commons.util.CollectionUtil;
import seedu.taskManager.commons.util.StringUtil;
import seedu.taskManager.logic.commands.EditCommand;
import seedu.taskManager.logic.commands.exceptions.CommandException;
import seedu.taskManager.model.Task.EndTime;
import seedu.taskManager.model.Task.ReadOnlyTask;
import seedu.taskManager.model.Task.Task;
import seedu.taskManager.model.Task.TaskStringReference;
import seedu.taskManager.model.Task.UniqueTaskList;
import seedu.taskManager.model.Task.UniqueTaskList.TaskNotFoundException;

/**
 * Represents the in-memory model of the task manager data. All changes to any
 * model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private TaskManager taskManager;
    private TaskManager previousTaskMgr;
    private final FilteredList<ReadOnlyTask> filteredTasks;
    private String currentToggleStatus;

    /**
     * Initializes a ModelManager with the given taskManager and userPrefs.
     */
    public ModelManager(ReadOnlyTaskManager taskManager, UserPrefs userPrefs) {
        super();
        assert !CollectionUtil.isAnyNull(taskManager, userPrefs);

        logger.fine("Initializing with task manager: " + taskManager + " and user prefs " + userPrefs);

        this.taskManager = new TaskManager(taskManager);

        filteredTasks = new FilteredList<>(this.taskManager.getTaskList());

        filteredTasks.setPredicate(task -> {
            if (task.getStatus().toString().equals(TaskStringReference.STATUS_UNDONE)) {
                return true;
            } else {
                return false;
            }
        });
        //@@author A0139509X
        setCurrentToggleStatus (TaskStringReference.SHOWING_ALL);
    }
    //@@author
    public ModelManager() {
        this(new TaskManager(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyTaskManager newData) {
        taskManager.resetData(newData);
        indicateTaskManagerChanged();
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    @Override
    public ReadOnlyTaskManager getTaskManager() {
        return taskManager;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateTaskManagerChanged() {
        raise(new TaskManagerChangedEvent(taskManager));
    }
    @Override
    public void indicateLoadEvent() {
        raise(new TaskManagerChangedEvent(taskManager));
    }

    @Override
    public synchronized void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
        setPrevious();
        taskManager.removeTask(target);
        indicateTaskManagerChanged();
    }

    @Override
    public synchronized void addTask(Task task) throws UniqueTaskList.DuplicatetaskException {
        setPrevious();
        taskManager.addTask(task);
        updateFilteredListToShowAll();
        indicateTaskManagerChanged();
        //@@author A0139509X
        sortTasksByEndTime();
        raise (new ScrollToListRequestEvent(filteredTasks.indexOf(task)));
    }

    //@@author A0139375W
    public boolean isRecurringTask(Task task) {
        return task.getRecurPeriod().hasRecurPeriod() && !task.getEndTime().isEmpty();
    }

    public boolean hasPassedEndDate(Task task, EndTime endTime) {
        return !task.getRecurEndDate().hasRecurEndDate()
                || !task.getRecurEndDate().hasPassedEndDate(endTime.toString());
    }

    // @@author A0140072X
    private void setPrevious() {
        previousTaskMgr = new TaskManager(taskManager);
    }

    // @@author A0140072X
    public boolean undoTask() {
        if (previousTaskMgr == null) {
            return false;
        }
        else {
            TaskManager currentTaskMgr = new TaskManager(taskManager);
            taskManager.resetData(previousTaskMgr);
            previousTaskMgr.resetData(currentTaskMgr);
            return true;
        }
    }
    //@@author A0138998B
    @Override
    public void sortTasksByEndTime() {
        try {
            taskManager.sortTasksByEndTime();
            indicateTaskManagerChanged();
        } catch (IllegalValueException e) {
            //sortTasksByEndTime creates new sets of tasks from the existing set.
            // No IllegalValueException expected
        }
    }

    @Override
    public void sortTasksByName() {
        taskManager.sortTasksByName();
        indicateTaskManagerChanged();
    }

    @Override
    public void sortTasksByPriority() {
        taskManager.sortTaskByPriority();
        indicateTaskManagerChanged();
    }


    @Override
    public void updateTask (int filteredTaskListIndex, ReadOnlyTask editedTask) throws CommandException {
        assert editedTask != null;
        setPrevious();
        int taskManagerIndex = filteredTasks.getSourceIndex(filteredTaskListIndex);
        try {
            taskManager.updateTask(taskManagerIndex, editedTask);
        } catch (IllegalValueException e) {
            throw new CommandException(EditCommand.MESSAGE_DUPLICATE_TASK);
        }


        indicateTaskManagerChanged();
        //sortTasksByEndTime();
        //@@author A0139509X
        raise (new ScrollToListRequestEvent(filteredTasks.indexOf(editedTask)));
    }

    //@@author A0139509X
    public String getCurrentToggleStatus() {
        return currentToggleStatus;
    }

    //@@author A0139509X
    public void setCurrentToggleStatus(String currentToggleStatus) {
        this.currentToggleStatus = currentToggleStatus;
    }

    //@@author
    // =========== Filtered Person List Accessors
    // =============================================================

    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList() {
        return new UnmodifiableObservableList<>(filteredTasks);
    }

    @Override
    public void updateFilteredListToShowAll() {
        // filteredTasks.setPredicate(null);
        filteredTasks.setPredicate(task -> {
            if (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE)) {
                return true;
            } else {
                return false;
            }
        });
        //@@author A0139509X
        setCurrentToggleStatus(TaskStringReference.SHOWING_ALL);
    }

    // @@author A0140072X
    public void updateFilteredTaskListByArchived() {
        updateFilteredListToShowAll();
        filteredTasks.setPredicate(task -> {
            if (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_DONE)) {
                return true;
            }
            else {
                return false;
            }
        });
        //@@author A0139509X
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    // @@author
    @Override
    public void updateFilteredTaskListByKeywords(Set<String> keywords) {
        updateFilteredTaskListByKeywords(new PredicateExpression(new NameQualifier(keywords)));
        //@@author A0139509X
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    //@@author
    private void updateFilteredTaskListByKeywords(Expression expression) {
        filteredTasks.setPredicate(expression::satisfies);
    }

    //@@author A0139509X
    @Override
    public void updateFilteredTaskListByEvent() {
        filteredTasks.setPredicate(task -> {
            if ((!(task.getStartTime().startTime.equals(TaskStringReference.EMPTY_TIME))
                    && !(task.getEndTime().endTime.equals(TaskStringReference.EMPTY_TIME))
                    && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE)))) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_EVENT);
    }

    @Override
    public void updateFilteredTaskListByTask() {
        filteredTasks.setPredicate(task -> {
            if (((task.getStartTime().startTime.equals(TaskStringReference.EMPTY_TIME))
                    && !(task.getEndTime().endTime.equals(TaskStringReference.EMPTY_TIME))
                    && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE)))) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_TASK);
    }

    @Override
    public void updateFilteredTaskListByFloatingTask() {
        filteredTasks.setPredicate(task -> {
            if (((task.getStartTime().startTime.equals(TaskStringReference.EMPTY_TIME))
                    && (task.getEndTime().endTime.equals(TaskStringReference.EMPTY_TIME))
                    && (task.getStatus().toString().equalsIgnoreCase((TaskStringReference.STATUS_UNDONE))))) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_FLOATING_TASK);
    }

    @Override
    public void updateFilteredTaskListByHighPriority() {
        filteredTasks.setPredicate(task -> {
            if ((task.getPriority().toString().equalsIgnoreCase(TaskStringReference.PRIORITY_HIGH))
                    && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE))) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    @Override
    public void updateFilteredTaskListByMediumPriority() {
        filteredTasks.setPredicate(task -> {
            if ((task.getPriority().toString().equalsIgnoreCase(TaskStringReference.PRIORITY_MEDIUM))
                    && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE))) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    @Override
    public void updateFilteredTaskListByLowPriority() {
        filteredTasks.setPredicate(task -> {
            if ((task.getPriority().toString().equalsIgnoreCase(TaskStringReference.PRIORITY_LOW))
                    && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE))) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    @Override
    public void updateFilteredTaskListByDoneStatus() {
        filteredTasks.setPredicate(task -> {
            if (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_DONE)) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    @Override
    public void updateFilteredTaskListByUnDoneStatus() {
        filteredTasks.setPredicate(task -> {
            if (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE)) {
                return true;
            } else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }

    @Override
    public void updateArchivedFilteredTaskListByKeyword(String archive) {
        filteredTasks.setPredicate(task -> {
            if (((StringUtil.containsWordIgnoreCase(task.getName().fullName, archive))
                    || (StringUtil.containsWordIgnoreCase(task.getDescription().description, archive))
                    || (StringUtil.containsTagIgnoreCase(task.getTags(), archive)))
                    && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_DONE))) {
                return true;
            }
            else {
                return false;
            }
        });
        setCurrentToggleStatus(TaskStringReference.SHOWING_SPECIAL);
    }
    //@@author
    // ========== Inner classes/interfaces used for filtering
    // =================================================

    interface Expression {
        boolean satisfies(ReadOnlyTask task);

        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyTask task) {
            return qualifier.run(task);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyTask task);

        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        // @@author A0139509X
        @Override
        public boolean run(ReadOnlyTask task) {
            for (String keyword : nameKeyWords) {
                if (((StringUtil.containsWordIgnoreCase(task.getName().fullName, keyword))
                        || (StringUtil.containsWordIgnoreCase(task.getDescription().description, keyword))
                        || (StringUtil.containsTagIgnoreCase(task.getTags(), keyword)))
                        && (task.getStatus().toString().equalsIgnoreCase(TaskStringReference.STATUS_UNDONE))) {
                    return true;
                }
            }
            return false;

        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }

}
