package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CourseList;
import entity.requirement.AndReq;
import entity.requirement.CourseReq;
import entity.requirement.CreditsReq;
import entity.requirement.Requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves course lists and row references inside one program's completion requirements. A course is on a list if it
 * matches one of the list's filters, or if it's part of a group of courses that the student has all taken. A reference
 * to a row that hasn't finished evaluating, or to a list that's missing or refers to itself, gives {@code MAYBE}.
 */
public final class CompletionResolver implements MembershipResolver {
    private final Completion completion;
    private final Set<String> snapshotCodes = new HashSet<>();
    private final Map<Integer, RowResult> finished = new HashMap<>();
    private final Set<String> visiting = new HashSet<>();

    public CompletionResolver(Completion completion, AcademicSnapshot snapshot) {
        this.completion = completion;
        for (CourseRecord r : snapshot.allRecords()) {
            snapshotCodes.add(r.getCode());
        }
    }

    void record(RowResult result) {
        finished.put(result.getNumber(), result);
    }

    @Override
    public Match onList(String listName, CourseRecord record) {
        Optional<CourseList> list = completion.list(listName);
        if (list.isEmpty() || !visiting.add(listName)) {
            return Match.MAYBE;
        }

        List<Match> matches = new ArrayList<>();
        for (Requirement item : list.get().getItems()) {
            matches.add(itemMatch(item, record));
        }

        visiting.remove(listName);
        return Match.anyOf(matches);
    }

    @Override
    public Match inRowPool(int rowNumber, CourseRecord record) {
        RowResult result = finished.get(rowNumber);
        if (result == null) {
            return Match.MAYBE;
        }

        return result.getClaimed().contains(record.getCode()) ? Match.YES : Match.NO;
    }

    @Override
    public Optional<Status> rowStatus(int rowNumber) {
        return Optional.ofNullable(finished.get(rowNumber)).map(RowResult::getStatus);
    }

    private Match itemMatch(Requirement item, CourseRecord record) {
        if (item instanceof CreditsReq) {
            return CourseMatcher.matches(((CreditsReq) item).getFilter(), record, this);
        }

        if (item instanceof AndReq && isCourseGroup((AndReq) item)) {
            return groupMatch((AndReq) item, record);
        }

        return Match.MAYBE;
    }

    private static boolean isCourseGroup(AndReq group) {
        return group.getItems().stream().allMatch(i -> i instanceof CourseReq);
    }

    private Match groupMatch(AndReq group, CourseRecord record) {
        List<String> codes = new ArrayList<>();
        for (Requirement item : group.getItems()) {
            codes.add(((CourseReq) item).getCode());
        }

        return codes.contains(record.getCode()) && snapshotCodes.containsAll(codes) ? Match.YES : Match.NO;
    }
}
