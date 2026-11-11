package testutil;

import entity.catalog.Course;
import entity.catalog.ProgramType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatalogFixturesTest {

    @Test
    void derivesFieldsFromCode() {
        Course csc148 = CatalogFixtures.course("CSC148H1");
        assertEquals("CSC", csc148.getDesignator());
        assertEquals("148", csc148.getNumber());
        assertEquals(100, csc148.getLevel());
        assertEquals(0.5, csc148.getWeight());
        assertEquals(1.0, CatalogFixtures.course("PHL100Y1").getWeight());
        assertEquals(100, CatalogFixtures.course("MATA31H3").getLevel());
        assertEquals(400, CatalogFixtures.course("CSC490H1").getLevel());
    }

    @Test
    void derivesProgramNumberFromCode() {
        assertEquals("1001", CatalogFixtures.program("ASMIN1001", ProgramType.MINOR, null).getNumber());
        assertEquals("1469A", CatalogFixtures.program("ASFOC1469A", ProgramType.FOCUS, null).getNumber());
    }
}
