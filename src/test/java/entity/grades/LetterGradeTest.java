package entity.grades;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class LetterGradeTest {

    @ParameterizedTest
    @CsvSource({
            "100, A_PLUS", "90, A_PLUS", "89, A", "85, A", "84, A_MINUS", "80, A_MINUS",
            "79, B_PLUS", "77, B_PLUS", "76, B", "73, B", "72, B_MINUS", "70, B_MINUS",
            "69, C_PLUS", "67, C_PLUS", "66, C", "63, C", "62, C_MINUS", "60, C_MINUS",
            "59, D_PLUS", "57, D_PLUS", "56, D", "53, D", "52, D_MINUS", "50, D_MINUS",
            "49, F", "0, F"
    })
    void fromPercentAtBoundaries(int percent, LetterGrade expected) {
        assertEquals(expected, LetterGrade.fromPercent(percent));
    }

    @Test
    void rejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> LetterGrade.fromPercent(-1));
        assertThrows(IllegalArgumentException.class, () -> LetterGrade.fromPercent(101));
    }

    @Test
    void onlyFIsFailing() {
        for (LetterGrade g : LetterGrade.values()) {
            assertEquals(g != LetterGrade.F, g.isPassing());
        }
    }
}
