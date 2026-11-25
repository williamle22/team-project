package use_case;

import entity.degree.Degree;
import entity.degree.DegreeType;

import java.util.List;
import java.util.Optional;

public interface DegreeReader {
    Optional<Degree> findDegree(DegreeType type);
    List<Degree> allDegrees();
}
