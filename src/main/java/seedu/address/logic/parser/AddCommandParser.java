package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENDTIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STARTTIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.NoSuchElementException;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.IncorrectCommand;

/**
 * Parses input arguments and creates a new AddCommand object.
 */
public class AddCommandParser {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     */
    public Command parse(String args) {
    //String arguments = args.toString();
   // String taskType = arguments.split(" ")[1];
    //String argument = arguments.substring(taskType.length()+1);
        ArgumentTokenizer argsTokenizer =  new ArgumentTokenizer(PREFIX_DESCRIPTION, PREFIX_STARTTIME, PREFIX_ENDTIME, PREFIX_TAG);
        argsTokenizer.tokenize(args);
        String taskType = argsTokenizer.getCommandType();
        try {
            switch(taskType) {
            case "task":
                return new AddCommand(
                        argsTokenizer.getPreamble().get(),
                        argsTokenizer.getValue(PREFIX_DESCRIPTION).get(),
                        argsTokenizer.getValue(PREFIX_ENDTIME).get(),
                        ParserUtil.toSet(argsTokenizer.getAllValues(PREFIX_TAG))
                );

            case "event":
                return new AddCommand(
                        argsTokenizer.getPreamble().get(),
                        argsTokenizer.getValue(PREFIX_DESCRIPTION).get(),
                        argsTokenizer.getValue(PREFIX_STARTTIME).get(),
                        argsTokenizer.getValue(PREFIX_ENDTIME).get(),
                        ParserUtil.toSet(argsTokenizer.getAllValues(PREFIX_TAG))
                );
            case "floating":
                return new AddCommand(
                        argsTokenizer.getPreamble().get(),
                        argsTokenizer.getValue(PREFIX_DESCRIPTION).get(),
                        ParserUtil.toSet(argsTokenizer.getAllValues(PREFIX_TAG))
                );
            default:
                System.out.println("Please specify the type of Task to add (task/event/floating)");
            }
            return null;

        } catch (NoSuchElementException nsee) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

}
