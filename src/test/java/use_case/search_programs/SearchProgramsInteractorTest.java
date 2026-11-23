package use_case.search_programs;

import entity.catalog.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.program;

class SearchProgramsInteractorTest {
    private static final Program CS_MAJOR = program("ASMAJ1689", MAJOR, null);
    private static final Program CS_MINOR = program("ASMIN1689", MINOR, null);
    private static final Program STATS_MAJOR = program("ASMAJ1165", MAJOR, null);

    private FakeProgramSearchCatalog catalog;
    private RecordingProgramsPresenter presenter;
    private SearchProgramsInteractor interactor;

    @BeforeEach
    void setUp() {
        catalog = new FakeProgramSearchCatalog();
        presenter = new RecordingProgramsPresenter();
        interactor = new SearchProgramsInteractor(catalog, presenter);
    }

    private void search(String query) {
        interactor.execute(new SearchProgramsInputData(query, Set.of()));
    }

    private List<String> resultCodes() {
        return presenter.data.getResults().stream().map(ProgramResultItem::getCode).collect(Collectors.toList());
    }

    @Test
    void notLoadedFails() {
        catalog.loaded = false;
        search("asmaj");
        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("Program data is still loading.",presenter.failMessage);
        assertEquals(0, catalog.searches);
    }

    @Test
    void passesRawQueryAndTypes() {
        interactor.execute(new SearchProgramsInputData(" cs major ", Set.of(MAJOR)));
        assertEquals(1, catalog.searches);
        assertEquals(" cs major ", catalog.lastQuery);
        assertEquals(Set.of(MAJOR), catalog.lastTypes);
    }

    @Test
    void exactCodeFirst() {
        catalog.matches = List.of(CS_MINOR, CS_MAJOR);
        search("asmaj1689");
        assertEquals(List.of("ASMAJ1689", "ASMIN1689"), resultCodes());
    }

    @Test
    void tiesSortByCode() {
        catalog.matches = List.of(CS_MINOR, CS_MAJOR, STATS_MAJOR);
        search("title");
        assertEquals(List.of("ASMAJ1165", "ASMAJ1689", "ASMIN1689"), resultCodes());
    }

    @Test
    void mapsFields() {
        catalog.matches = List.of(CS_MAJOR);
        search("asmaj1689");
        assertEquals(List.of(new ProgramResultItem("ASMAJ1689", "ASMAJ1689 title", MAJOR)),
                presenter.data.getResults());
    }

    @Test
    void noMatchesIsEmptySuccess() {
        search("quantum basket weaving");
        assertEquals(List.of("success"), presenter.calls);
        assertEquals(new SearchProgramsOutputData(List.of()), presenter.data);
    }
}
