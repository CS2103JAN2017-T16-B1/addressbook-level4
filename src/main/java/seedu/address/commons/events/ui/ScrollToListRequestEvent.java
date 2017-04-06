package seedu.address.commons.events.ui;

import seedu.address.commons.events.BaseEvent;
//@@author A0139509X
/**
 * Indicates a request to scroll to the list of tasks
 */
public class ScrollToListRequestEvent extends BaseEvent {

    public final int targetIndex;

    public ScrollToListRequestEvent(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
