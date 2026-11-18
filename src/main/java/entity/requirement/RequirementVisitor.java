package entity.requirement;

/** An operation over a requirement tree, with one method per node type. */
public interface RequirementVisitor<R> {
    R visitAnd(AndReq node);
    R visitOr(OrReq node);
    R visitChoose(ChooseReq node);
    R visitNot(NotReq node);
    R visitCourse(CourseReq node);
    R visitCredits(CreditsReq node);
    R visitAverage(AverageReq node);
    R visitGpa(GpaReq node);
    R visitProgram(ProgramReq node);
    R visitRowRef(RowRefReq node);
    R visitProfile(ProfileReq node);
    R visitText(TextReq node);
}