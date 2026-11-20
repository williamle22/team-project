package use_case;

import entity.term.Placement;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public final class CurrentAcademicYear {
    private final Clock clock;

    public CurrentAcademicYear(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean isCurrent(Placement placement) {
        LocalDate today = LocalDate.now(clock);
        int month = today.getMonthValue();

        if (month >= 5 && month <= 8) {
            return placement.isSummer() && placement.getYear() == today.getYear();
        }

        int academicYear = month >= 9 ? today.getYear() : today.getYear() - 1;

        return !placement.isSummer() && placement.academicYear() == academicYear;
    }
}
