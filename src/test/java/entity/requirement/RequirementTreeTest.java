package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequirementTreeTest {

    private static OrReq phy152Prerequisite() {
        return new OrReq(List.of(new CourseReq("PHY131H1", null, false), new CourseReq("PHY151H1", null, false)), false);
    }

    @Test
    void buildsPhy152Prerequisite() {
        OrReq tree = phy152Prerequisite();
        assertEquals(2, tree.getItems().size());
        assertEquals("PHY131H1", ((CourseReq) tree.getItems().get(0)).getCode());
        assertEquals("PHY151H1", ((CourseReq) tree.getItems().get(1)).getCode());
        assertEquals(phy152Prerequisite(), tree);
    }
}
