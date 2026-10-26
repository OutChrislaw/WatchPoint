package model;

import java.util.ArrayList;
import java.util.List;

public enum ReportStatus {

    // ------------------------------------------------------------ CONSTANTS

    PENDING,
    VERIFIED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED;

    // ------------------------------------------------------------ WORKFLOW

    public boolean isFinal() {
        return this == RESOLVED || this == REJECTED;
    }

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

    // ------------------------------------------------------------ DISPLAY NAME

    public String displayName() {
        return name().replace('_', ' ');
    }
}
