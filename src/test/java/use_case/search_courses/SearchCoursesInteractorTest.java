package use_case.search_courses;

import entity.catalog.Campus;
import entity.catalog.Course;
import entity.catalog.Section;
import entity.catalog.Sessions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static entity.catalog.BreadthCategory.LIVING_THINGS;
import static entity.catalog.BreadthCategory.PHYSICAL_MATHEMATICAL;
import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;

class SearchCoursesInteractorTest {
    private FakeSearchCatalog catalog;
    private RecordingSearchPresenter presenter;
    private SearchCoursesInteractor interactor;

    @BeforeEach
    void setUp() {
        catalog = new FakeSearchCatalog();
        presenter = new RecordingSearchPresenter();
        interactor = new SearchCoursesInteractor(catalog, presenter);
    }

    private void search(String query) {
        interactor.execute(new SearchCoursesInputData(query, SearchFilters.none()));
    }

    private void returns(String... codes) {
        List<Course> courses = new ArrayList<>();
        for (String code : codes) {
            courses.add(course(code));
        }
        catalog.matches = courses;
    }

    private List<String> resultCodes() {
        return presenter.data.getResults().stream().map(SearchResultItem::getCode).collect(Collectors.toList());
    }

    private static List<String> codes(int from, int count) {
        List<String> codes = new ArrayList<>();
        for (int i = from; i < from + count; i++) {
            codes.add("CSC" + i + "H1");
        }
        return codes;
    }

    @Test
    void notLoadedFails() {
        catalog.loaded = false;
        search("csc148");
        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("Course data is still loading.", presenter.failMessage);
        assertEquals(0, catalog.searches);
    }

    @Test
    void blankQueryNoFiltersIsEmptySuccess() {
        search("   ");
        assertEquals(List.of("success"), presenter.calls);
        assertEquals(new SearchCoursesOutputData("   ", List.of(), 0), presenter.data);
        assertEquals(0, catalog.searches);
    }

    @Test
    void blankQueryWithFiltersSearches() {
        returns("MAT137Y1", "CSC108H1");
        SearchFilters firstYear = new SearchFilters(null, Set.of(100), Set.of(), Set.of());
        interactor.execute(new SearchCoursesInputData("", firstYear));
        assertEquals(1, catalog.searches);
        assertEquals(firstYear, catalog.lastFilters);
        assertEquals(List.of("CSC108H1", "MAT137Y1"), resultCodes());
    }

    @Test
    void passesRawQueryAndFilters() {
        SearchFilters csc = new SearchFilters("csc", Set.of(), Set.of(), Set.of());
        interactor.execute(new SearchCoursesInputData(" csc 148 ", csc));
        assertEquals(" csc 148 ", catalog.lastQuery);
        assertEquals(csc, catalog.lastFilters);
        assertEquals(" csc 148 ", presenter.data.getQuery());
    }

    @Test
    void noMatchesIsEmptySuccess() {
        search("quantum basket weaving");
        assertEquals(List.of("success"), presenter.calls);
        assertEquals(new SearchCoursesOutputData("quantum basket weaving", List.of(), 0), presenter.data);
    }

    @Test
    void exactCodeFirst() {
        returns("BIO130H1", "CSC148H1");
        search("csc148h1");
        assertEquals(List.of("CSC148H1", "BIO130H1"), resultCodes());
    }

    @Test
    void prefixBeforeContainsBeforeTitle() {
        returns("ANT100Y1", "CSC148H1", "SCI199H1");
        search("sc");
        assertEquals(List.of("SCI199H1", "CSC148H1", "ANT100Y1"), resultCodes());
    }

    @Test
    void spacedQueryRanksLikeCompactCode() {
        returns("BIO130H1", "CSC148H1");
        search("csc 148");
        assertEquals(List.of("CSC148H1", "BIO130H1"), resultCodes());
    }

    @Test
    void tiesSortByCode() {
        returns("MAT137Y1", "CSC108H1", "BIO130H1");
        search("intro");
        assertEquals(List.of("BIO130H1", "CSC108H1", "MAT137Y1"), resultCodes());
    }

    @Test
    void rankingOrdersByRankThenCode() {
        List<Course> courses = new ArrayList<>(List.of(course("CSC148H1"), course("MAT148H1"), course("CSC108H1"),
                course("BIO130H1")));
        courses.sort(SearchCoursesInteractor.ranking("CSC1"));
        assertEquals(List.of(course("CSC108H1"), course("CSC148H1"), course("BIO130H1"), course("MAT148H1")),
                courses);
    }

    @Test
    void truncatesAtMax() {
        catalog.matches = codes(100, 150).stream().map(c -> course(c)).collect(Collectors.toList());
        search("csc");
        assertEquals(SearchCoursesInteractor.MAX_RESULTS, presenter.data.getResults().size());
        assertEquals(150, presenter.data.getTotalMatches());
        assertTrue(presenter.data.isTruncated());
        assertEquals(codes(100, 100), resultCodes());
    }

    @Test
    void notTruncatedAtExactlyMax() {
        catalog.matches = codes(100, 100).stream().map(c -> course(c)).collect(Collectors.toList());
        search("csc");
        assertEquals(100, presenter.data.getResults().size());
        assertEquals(100, presenter.data.getTotalMatches());
        assertFalse(presenter.data.isTruncated());
    }

    @Test
    void mapsFields() {
        Course offered = course("ENV200Y1", null, null, null, List.of(LIVING_THINGS, PHYSICAL_MATHEMATICAL),
                new Sessions(List.of(Section.Y), List.of()));
        catalog.matches = List.of(offered, course("ENV100H1"));
        search("env");

        List<SearchResultItem> results = presenter.data.getResults();
        assertEquals(new SearchResultItem("ENV100H1", "ENV100H1 title", 0.5, Campus.UTSG, List.of(), false),
                results.get(0));
        assertEquals(new SearchResultItem("ENV200Y1", "ENV200Y1 title", 1.0, Campus.UTSG, List.of(4, 5), true),
                results.get(1));
    }
}
