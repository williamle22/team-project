package use_case;

import entity.degree.Degree;
import entity.degree.DegreeType;

import java.util.List;
import java.util.Optional;

/** Read-only access to degree rules. Use cases depend on this interface, not on an implementation. */
public interface DegreeReader {
    Optional<Degree> findDegree(DegreeType type);
    /** Returns every degree, in no particular order. */
    List<Degree> allDegrees();
}
