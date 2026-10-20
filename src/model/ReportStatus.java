package model;

import java.util.ArrayList;
import java.util.List;

/**
 * ReportStatus.java
 * The statuses a report can have, plus the workflow between them. The rules
 * live here so the dashboards, the managers and the file handling all share one
 * definition:
 *
 * PENDING     -> VERIFIED or REJECTED
 * VERIFIED    -> IN_PROGRESS or REJECTED
 * IN_PROGRESS -> RESOLVED or REJECTED
 * RESOLVED    -> nothing (final)
 * REJECTED    -> nothing (only an administrator may reopen it)
 */
public enum ReportStatus {

    PENDING,
    VERIFIED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED;

    /** True when no normal status change is allowed from this status. */
    public boolean isFinal() {
        return this == RESOLVED || this == REJECTED;
    }

    /** True when a report may move from this status to the given one. */
    public boolean canMoveTo(ReportStatus target) {
        if (target == null || target == this) {
            return false;
        }
        if (this == PENDING) {
            return target == VERIFIED || target == REJECTED;
        }
        if (this == VERIFIED) {
            return target == IN_PROGRESS || target == REJECTED;
        }
        if (this == IN_PROGRESS) {
            return target == RESOLVED || target == REJECTED;
        }
        return false;
    }

    /** The statuses this one may move to, in workflow order. */
    public List<ReportStatus> nextStatuses() {
        List<ReportStatus> next = new ArrayList<ReportStatus>();
        ReportStatus[] all = values();
        for (int i = 0; i < all.length; i++) {
            if (canMoveTo(all[i])) {
                next.add(all[i]);
            }
        }
        return next;
    }

    /** The name with a space instead of the underscore, for messages. */
    public String displayName() {
        return name().replace('_', ' ');
    }
}