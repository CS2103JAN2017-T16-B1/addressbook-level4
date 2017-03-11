package seedu.address.model;


import javafx.collections.ObservableList;
import seedu.address.model.Task.ReadOnlyTask;
import seedu.address.model.tag.Tag;

/**
 * Unmodifiable view of an task manager
 */
public interface ReadOnlyTaskManager {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<ReadOnlyTask> getPersonList();

    /**
     * Returns an unmodifiable view of the tags list.
     * This list will not contain any duplicate tags.
     */
    ObservableList<Tag> getTagList();

}
