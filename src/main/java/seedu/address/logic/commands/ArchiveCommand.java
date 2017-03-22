package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.UnmodifiableObservableList;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Task.ReadOnlyTask;
import seedu.address.model.Task.Status;
import seedu.address.model.Task.Task;
import seedu.address.model.Task.UniqueTaskList;
import seedu.address.model.Task.UniqueTaskList.TaskNotFoundException;
//@@author A0140072X
/**
 * Archive a task identified using it's last displayed index from the task manager.
 */
public class ArchiveCommand extends Command {

    public static final String COMMAND_WORD = "archive";
    public static final String MESSAGE_DUPLICATE_TASK = "This task already exists in the task manager.";
    public static final String MESSAGE_ILLEGAL_VALUE = "Illegal value detected.";
    public static final String MESSAGE_USAGE = COMMAND_WORD

            + ": Archive the item identified by the index number used in last task listing.\n"
            + "Parameters:  INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_ARCHIVE_TASK_SUCCESS = "Archived Item: %1$s";
  
    public final int targetIndex;
    

    public ArchiveCommand(int targetIndex) {
        this.targetIndex = targetIndex - 1;
        

    }


    @Override
    public CommandResult execute() throws CommandException {
        List<ReadOnlyTask> lastShownList = model.getFilteredTaskList();
        if (targetIndex >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }
        ReadOnlyTask taskToArchive = lastShownList.get(targetIndex);
                
        try {
            Task updatedTask = new Task(taskToArchive.getName(), taskToArchive.getDescription(), taskToArchive.getStartTime(),taskToArchive.getEndTime(), taskToArchive.getId(), taskToArchive.getPriority(),new Status("done"),taskToArchive.getTags());
            model.updateTask(targetIndex, updatedTask);
        }
        catch (UniqueTaskList.DuplicatetaskException dpe) {
            throw new CommandException(MESSAGE_DUPLICATE_TASK);
        } 
        catch (IllegalValueException ive) {
            throw new CommandException(MESSAGE_ILLEGAL_VALUE);
        } 
       model.updateFilteredListToShowAll();

        return new CommandResult(String.format(MESSAGE_ARCHIVE_TASK_SUCCESS, taskToArchive));

    }

}