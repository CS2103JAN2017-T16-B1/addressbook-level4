//@@author A0139375W
package seedu.taskManager.logic.parser;

import static seedu.taskManager.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_ENDTIME;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_RECURENDDATE;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_RECURPERIOD;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_STARTTIME;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.taskManager.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.NoSuchElementException;

import seedu.taskManager.commons.exceptions.IllegalValueException;
import seedu.taskManager.logic.commands.AddCommand;
import seedu.taskManager.logic.commands.Command;
import seedu.taskManager.logic.commands.IncorrectCommand;

/**
 * Parses input arguments and creates a new AddCommand object.
 */
public class AddCommandParser {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     */
    public Command parse(String args) {

        ArgumentTokenizer argsTokenizer =
                new ArgumentTokenizer(PREFIX_DESCRIPTION, PREFIX_STARTTIME, PREFIX_ENDTIME,  PREFIX_PRIORITY,
                PREFIX_STATUS, PREFIX_TAG, PREFIX_RECURPERIOD, PREFIX_RECURENDDATE);

        argsTokenizer.tokenize(args);
        String taskType = argsTokenizer.getCommandType(args);

        try {
            return new AddCommand(
                        argsTokenizer.getPreamble().get(),
                        argsTokenizer.getValue(PREFIX_DESCRIPTION).orElse(""),
                        argsTokenizer.getValue(PREFIX_STARTTIME).orElse(""),
                        argsTokenizer.getValue(PREFIX_ENDTIME).orElse(""),
                        argsTokenizer.getValue(PREFIX_RECURPERIOD).orElse(""),
                        argsTokenizer.getValue(PREFIX_RECURENDDATE).orElse(""),
                        argsTokenizer.getValue(PREFIX_PRIORITY).orElse("m"),
                        ParserUtil.toSet(argsTokenizer.getAllValues(PREFIX_TAG))
                );

        } catch (NoSuchElementException nsee) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

}

