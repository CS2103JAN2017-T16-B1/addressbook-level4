package seedu.address.model.util;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.TaskManager;
import seedu.address.model.Task.EndTime;
import seedu.address.model.Task.ID;

import seedu.address.model.Task.Priority;
import seedu.address.model.Task.Status;

import seedu.address.model.Task.StartTime;
import seedu.address.model.Task.Status;
import seedu.address.model.Task.Name;
import seedu.address.model.Task.Priority;
import seedu.address.model.Task.Task;
import seedu.address.model.Task.Description;
import seedu.address.model.Task.UniqueTaskList.DuplicatetaskException;
import seedu.address.model.ReadOnlyTaskManager;
import seedu.address.model.tag.UniqueTagList;

public class SampleDataUtil {
    public static Task[] getSampleTasks() {
        try {
            return new Task[] {


            		new Task(new Name("buy milk"), new Description("87438807"), new StartTime(""),
                            new EndTime("2017-03-04-1000"), new ID("1"), new Priority("h"), new Status("undone"),
                            new UniqueTagList("friends"))

            };
        } catch (IllegalValueException e) {
            throw new AssertionError("sample data cannot be invalid", e);
        }
    }

    public static ReadOnlyTaskManager getSampleTaskManager() {
        try {
            TaskManager sampleAB = new TaskManager();

            for (Task sampleTask : getSampleTasks()) {
                sampleAB.addTask(sampleTask);

            }
            return sampleAB;
        } catch (DuplicatetaskException e) {
            throw new AssertionError("sample data cannot contain duplicate tasks", e);
        }
    }
}
