package seedu.address.logic.commands;

import java.util.Set;

import org.junit.Assert;

import seedu.address.model.Task.Priority;
import seedu.address.model.tag.Tag;
/**
 * Lists all archived tasks in task manager.
 * 
 */
public class ArchivedCommand extends Command {

    public static final String COMMAND_WORD = "archived";
   // private static final String HIGH_PRIORITY = "h";


    public ArchivedCommand() {
        
    }

    
    @Override
    public CommandResult execute() {
    	
    	model.updateFilteredTaskListByArchived();

    	return new CommandResult(getMessageForTaskListShownSummary(model.getFilteredTaskList().size()));
    }

	

}
