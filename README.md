# dgx: Degree Explorer (but better)

## Team Information

### Team Members
- Kameel Ahamed
- Jonathan Zhu
- William Le
- Karishma Persad

---

## Project Description

### Domain

**Education:** academic planning for undergraduate students in the Faculty of Arts & Science (FAS).

The University of Toronto provides an application called Degree Explorer, which allows students to plan out courses and programs in their degree and automatically assess requirement completion. However, this application is old and slow, powered entirely by server-side calls.

Our version improves on the original in a number of ways:
1. We use a local database containing all courses and programs in the FAS to greatly speed up lookups and requirement evaluations.
2. We integrate another UofT application, Time Table Builder, so that users can view their timetable based directly on the course plan they create.
3. We integrate UofT's Course Evaluations, so that while creating a plan, users can explore past evaluations for courses without leaving the program.
4. We allow users to track grades received for courses (or assignments in courses), to provide an up-to-date record of their SGPA/CGPA and credits completed.

---

# User Stories

| # | User Story | Lead |
|---|------------|------|
| 1 | As a user, I want to be able to search up courses and see their description and requirements. | Kameel/Karishma |
| 2 | As a user, I want to be able to see past course evaluations for a given course. | Karishma |
| 3 | As a user, I want to be able to create course plans for any number of semesters. | Jonathan |
| 4 | As a user, I want to be able to create multiple different course plans. | Jonathan |
| 5 | As a user, I want to be able to import and export my course plans. | William |
| 6 | As a user, I want to be able to see whether or not I am eligible to take a given course in a given semester. | Kameel |
| 7 | As a user, I want to be able to record my achieved grade for a given course and see my CGPA automatically calculated. | William |
| 8 | As a user, I want to be able to see whether my plan meets all the program and degree requirements I need to graduate. | Kameel |

---

## Use Cases

### Use Case 1: Sync Catalog
**Actor:** System (on app launch)

**Main flow:**
1. The app checks for a cached catalog at `data/dgx.db` in the app's folder.
2. It requests `GET /db` from dgx-backend, sending `If-Modified-Since` with the cached file's timestamp.
3. If the server returns `304 Not Modified`, the app keeps the cached catalog.
4. If the server returns `200 OK`, the app downloads to a temporary file, checks that it loads and has the expected schema version, and then replaces the cache.
5. The app loads the catalog into memory.

**Alternative flows:**
- The network is unavailable: the app uses the cached catalog.
- The network is unavailable and there's no cache: the app explains that it needs an internet connection the first time it runs.
- The server rate-limits the request (`429`): the app waits for `Retry-After` and retries once.
- The cached catalog has an older schema version: the app downloads a fresh copy.

### Use Case 2: Search Courses (User Story 1)
**Actor:** Student

**Main flow:**
1. The student types a query, either a course code like `CSC207` or words from a title.
2. The app searches the in-memory catalog and shows matching courses with their code, title, and credit weight.
3. The student can narrow the results by designator, level, campus, or breadth category.

**Alternative flow:** No courses match: the app shows an empty state suggesting a broader search.

### Use Case 3: View Course Details (User Story 1)
**Actor:** Student

**Main flow:**
1. The student selects a course from search results or from a plan.
2. The app shows its title, description, credit weight, breadth categories, the sessions it's offered in this year, and its prerequisites, corequisites, and exclusions as readable requirement trees alongside the original calendar text.
3. If a plan is open, each requirement is marked Met, Not Met, or Review against that plan.

### Use Case 4: View Course Evaluations (User Story 2)
**Actor:** Student

**Main flow:**
1. From a course's detail view, the student opens its evaluations.
2. The app fetches and shows past course evaluation results for that course.

**Alternative flows:**
- No evaluation data exists for the course: the app says so.
- The evaluation source can't be reached: the app shows an error, and the rest of the course details still work.

_The data source is still to be decided; see [API Usage](#api-usage)._

### Use Case 5: Edit a Plan (User Story 3)
**Actor:** Student

**Main flow:**
1. With a plan open, the student adds as many sessions as they need, each one a Fall–Winter session or a Summer session in a given year.
2. The student adds a course to a placement: a session and a section (`F`, `S`, or `Y`).
3. The app adds the course and re-checks eligibility for the whole plan (Use Case 8).
4. The student can also move a course to another placement, remove it, or remove an empty session.

**Alternative flows:**
- The course code isn't in the catalog: the app shows an error and doesn't add it.
- The course is already in the plan: the app still adds it, and flags it as a retake.
- The course has unmet requirements, or isn't offered in that term: the app still adds it, and marks it with warnings (Use Case 8).
- The student tries to remove a session that still has courses: the app asks them to move or remove the courses first.

### Use Case 6: Manage Plans (User Story 4)
**Actor:** Student

**Main flow:**
1. The student opens the list of saved plans.
2. The student creates, renames, duplicates, or deletes a plan, or opens one to edit.
3. The app saves every change automatically.

**Alternative flow:** The student deletes a plan: the app asks for confirmation first.

### Use Case 7: Import and Export Plans (User Story 5)
**Actor:** Student

**Main flow:**
1. The student exports a plan, and the app writes it to a JSON file the student chooses.
2. The student imports a plan file, and the app validates it and adds it to their saved plans.

**Alternative flows:**
- The file is malformed or uses an unsupported format version: the app shows an error and imports nothing.
- The plan has the same id as an existing plan: the app gives it a new id instead of overwriting.
- The plan has course codes that aren't in the current catalog: it's imported, and those courses are flagged.

### Use Case 8: Check Eligibility (User Story 6)
**Actor:** Student (runs automatically whenever the plan changes)

**Main flow:**
1. For each course in the plan, the app builds a snapshot of the student's record as of that course's term. Courses that ended before it count as taken, and courses running at the same time count as concurrent. A Summer F course counts as taken for a Summer S course in the same year. Failed courses don't count, a retaken course counts once with its best grade, and transfer credits count as taken.
2. The app checks the course's prerequisites against taken courses, and its corequisites against taken and concurrent courses. It checks exclusions against taken courses, and against concurrent courses for exclusions that say "not concurrently with".
3. For courses in the current academic year, the app checks that the course is offered in the chosen term and section.
4. Each course shows Met, Not Met, or Review, with a reason for every requirement that isn't met.
5. The app also warns about problems across the plan: retakes, codes that aren't in the current calendar, and two courses in the same term that exclude each other.

**Alternative flows:**
- A requirement needs a minimum grade in a course with no recorded grade yet: Review.
- A requirement needs human judgement (for example, instructor permission): Review.
- A course the student has taken excludes this one: Not Met.
- An exclusion is free text, or has a qualifying note: Review.
- The course isn't offered in the chosen term and section: Not Met.
- The course is outside the current academic year: the app skips the offering check.
- The course isn't in the current calendar: Review, plus a plan warning.
- The plan has an unspecified transfer credit, like `MAT1**`: a requirement it could meet, like `MAT135H1`, is Review, and the app warns that it counts only with the department's permission.

### Use Case 9: Record Grades and Calculate CGPA (User Story 7)
**Actor:** Student

**Main flow:**
1. The student opens a course in their plan and enters the final grade they achieved, as a percentage.
2. The app converts it to UofT's grade-point scale and recalculates the credit-weighted CGPA and total credits completed.
3. Recorded grades are also used when checking requirements with minimum grades (Use Case 8).

**Alternative flows:**
- The student enters an invalid grade: the app rejects it with a message.
- The course was taken Credit/No Credit: it counts toward credits but not toward the CGPA.

### Use Case 10: Check Graduation Eligibility (User Story 8)
**Actor:** Student

**Main flow:**
1. The student opens the graduation check for a plan, and chooses whether to count every course in the plan or only completed courses, which are courses with a passing final grade.
2. The app checks the degree's requirements: total credits, credits by level, CGPA, and breadth.
3. The app checks each program in the plan: whether the student can enrol in it, and each of its completion requirements.
4. The app checks that the student's completed programs form a valid combination, such as one Specialist, two Majors, or one Major and two Minors. A combination of more than one program must include 12.0 different credits.
5. The app shows each requirement as Met, Not Met, or Review, with a reason and the courses it counts, and an overall result. The overall result is Met when the degree requirements and the program combination are met.

**Alternative flows:**
- The plan has no degree: the app asks the student to choose one.
- A program in the plan isn't in the current calendar: Review, and it doesn't count toward the combination.
- The student has completed a program they intend to enter but aren't enrolled in: Review, because they still need to be admitted.
- The plan's degree is a BCom: only the Specialists in Accounting, Finance and Economics, and Management count toward the combination.
- A limited-enrolment program whose requirements the student meets: Met, with a reason saying that admission isn't guaranteed.
- A requirement needs human judgement: Review, with a note.
- Two requirements could use the same course: the app counts the course toward only one of them, unless the calendar lets them share it. If a different choice of courses might meet a requirement, Review.
- The plan has more than 10.0 transfer credits, or more than 15.0 credits with one designator: the extra credits don't count toward the degree's 20.0 credits.
- The plan has transfer credits from a University of Toronto exchange program: the app treats them like other transfer credits, and shows the calendar's note that they count toward the 10.0 Faculty of Arts & Science credits.
- The programs share courses, so they might not include 12.0 different credits: Review.
- The degree's CGPA requirement, when the plan has no final grades: Review, until the student enters final grades.
- A course in the plan isn't in the current calendar: it doesn't count toward any requirement, and the app notes it.
- The plan has an unspecified transfer credit, like `MAT1**`: a requirement it could meet, like `MAT135H1`, is Review, and the app notes that it counts only with the department's permission.

---

## Minimal Viable Product (MVP)

| Team Member | MVP Use Case |
|-------------|--------------|
| Kameel Ahamed | Use Case 1, 2, 8 |
| Jonathan Zhu | Use Case 5, 6 |
| William Le | Use Case 7, 9 |
| Karishma Persad | Use Case 3, 4 |

---

## Entities

### Catalog

**`Course`**: one course from the FAS calendar.
- Variables: `code` (`CSC207H1`), `designator` (`CSC`), `number` (`207`), `level` (100–400), `weight` (0.5 or 1.0), `campus`, `title`, `description`, `prerequisites`, `corequisites`, `exclusions`, `breadth` (a list of `BreadthCategory`), `sessions`
- Methods: getters, and `isOfferedThisYear()`. Course data is read-only.

**`Sessions`**: the sections the Timetable Builder offers a course in this year.
- Variables: `fallWinter` and `summer`, each a list of `Section` (`F`, `S`, `Y`)
- Methods: `offers(SessionPeriod period, Section section)`

**`Program`**: a Specialist, Major, Minor, Focus, or Certificate.
- Variables: `code` (`ASMIN1001`), `type`, `number` (`1001`), `title`, `areas`, `enrolment`, `completion`
- Methods: getters only.

**`Completion`** and **`CompletionRow`**: a program's numbered completion requirements.
- Variables: each row has a `number`, `kind` (allocation, constraint, limit, or choice), optional `label` and `parent`, a `Requirement`, and the calendar's `raw` text. `Completion` also holds the program's named course lists (`CourseList`).

**`Degree`**: the rules for one degree, in the same format as a program's completion requirements. The app loads them from JSON files in `src/main/resources/degrees`.
- Variables: `type` (`HBA`, `HBSC`, `BCOM`), `completion`, `programCombinations` (a `ProgramCombinationRule`: the allowed counts of each program type, like 2 Majors), `eligiblePrograms` (if it isn't empty, only these programs count toward a combination, like BCom's three Specialists)
- Methods: getters, `countsTowardCombination(code)`. `getTitle()` returns the type's title.

### Requirements

**`Requirement`**: an abstract class for one node in a requirement tree. Every prerequisite, corequisite, enrolment rule, and completion rule is a tree of these.
- Subclasses: logical nodes (`AndReq`, `OrReq`, `ChooseReq`, `NotReq`), academic nodes (`CourseReq`, `CreditsReq`, `AverageReq`, `GpaReq`), and context nodes (`ProgramReq`, `RowRefReq`, `ProfileReq`, `TextReq`)
- Methods: `accept(RequirementVisitor<T> visitor)`. Visitors include evaluation, printing, and other operations.

### Evaluation

**`AcademicSnapshot`**: what a student has done as of one term, for checking requirements.
- Variables: `taken` and `concurrent` (course code to `CourseRecord`), `enrolledPrograms`, `intendedPrograms`, `cgpa`, `profileFacts` (like `min_year_of_study`), `asOf`
- Methods: getters, `builder()`, `restrictedTo(codes)`

**`CourseRecord`**: one course as it counts toward requirements: the `Course`, its `weight`, its final `grade` if there is one, and whether it's a transfer credit or CR/NCR. An unspecified transfer credit, like `MAT1**`, gets a placeholder course.

**`Status`**: the result of evaluating a requirement: `MET`, `NOT_MET`, or `REVIEW`.

**`EvaluationNode`**: the result of checking one requirement node: a `status`, a `reason`, `children` in the same shape as the requirement tree, and the course codes it `counted`.

**`EligibilityResult`**: one course's full check: `prerequisites` and `corequisites` (`EvaluationNode`s), `exclusions` (an `ExclusionOutcome`), `offering` (`OfferingStatus`), and `warnings`.
- Methods: getters, `getOverall()`

**`EnrolmentResult`**: whether the student can enrol in one program: a `status`, a `reason`, the evaluated enrolment requirement as `detail`, and the calendar's `notes`.

**`RowResult`**: one evaluated completion row: its `status`, whether it's `applicable` to the student, a `reason`, the evaluated requirement as `detail`, and the course codes it `claimed`.

**`CompletionResult`**: a program's or degree's evaluated completion requirements: a `RowResult` for every row, and an `overall` status.

**`ProgramProgress`**: one program's `EnrolmentResult` and `CompletionResult`.

**`DegreeProgress`**: a degree's `CompletionResult`, and the status and reason of its program combination.
- Methods: getters, `getOverall()`

### Plans

**`Plan`**: one of the student's named plans. It's the only mutable entity. Change it only through its methods.
- Variables: `id`, `name`, `degree`, `programs` (a list of `ProgramSelection`), `terms` (sorted), `courses` (a list of `PlannedCourse`), `transferCredits` (a list of `TransferCredit`)
- Methods: `addTerm`, `addCourse`, `moveCourse`, `replaceCourse`, `removeCourse`, `coursesIn(AcademicTerm term)`, `copy()`. A plan enforces only structural rules, such as every course's terms being in the plan. Requirement problems are warnings from evaluation.

**`PlannedCourse`**: a course placed in a plan.
- Variables: `id` (identifies the entry, because a retake puts the same code in a plan twice), `code`, `placement`, `crNcr`, `grade` (a `CourseGrade`), `timetable` (a `TimetableChoice`)

**`ProgramSelection`**: a program on a plan.
- Variables: `code`, `status` (`ENROLLED` or `INTENDED`)

**`TransferCredit`**: credit from another institution. A specific credit, like `MAT135H1`, counts as taken. An unspecified one, like `MAT1**`, counts toward total credits. A requirement it could meet is Review, because UofT counts it only with the department's permission.
- Variables: `code`, `weight`

**`Placement`**: where a course sits in the plan.
- Variables: `period` (Fall–Winter or Summer), `year`, `section` (`F`, `S`, or `Y`)
- Methods: `startTerm()`, `endTerm()`, `terms()`, `occupies(AcademicTerm term)`, `academicYear()`

**`AcademicTerm`**: a season and a year, like Winter 2027.
- Variables: `season`, `year`
- Methods: `compareTo` (implements `Comparable`, so terms sort in time order)

### Grades

**`CourseGrade`**: the grade for one planned course.
- Variables: `finalGrade` (empty until recorded), `assessments`
- Methods: `getFinalGrade()`, `currentAverage()` (the predicted grade), `guaranteedMinimum()`, `neededOnRemaining(double target)`. There's no method that returns "the grade" without saying which kind, so a predicted grade can't be used where a final one is required.

**`Assessment`**: one graded piece of work.
- Variables: `name`, `weight` (percent of the final grade), `score` (empty until graded)

**`LetterGrade`**: an enum of UofT's grade scale, with each grade's minimum percent and grade points.
- Methods: `fromPercent(int percent)`

**`GpaCalculator`**: calculates a plan's GPAs from final grades, following the FAS calendar.
- Methods: `cgpa(Plan plan)`, `sgpa(Plan plan, AcademicTerm term)`, `cgpaBefore(Plan plan, AcademicTerm term)`, `projectedCgpa(Plan plan)`. GPAs exclude CR/NCR courses, transfer credits, and repeated attempts marked Extra.

**`CreditSummary`**: a plan's credits, split into completed, transfer, failed, in progress, awaiting a grade, and planned.
- Methods: `of(Plan plan, CreditWeights weights, AcademicTerm current)`, `getCompleted()` (the total credits completed in Use Case 9)

### Timetable

**`CourseOffering`**: one course's sections in one placement, from the Timetable Builder.
- Variables: `courseCode`, `placement`, `activities`

**`Activity`**: one lecture, tutorial, or practical section, like `LEC0101`.
- Variables: `code`, `type` (`LEC`, `TUT`, `PRA`), `meetings`, `instructors`

**`MeetingTime`**: one weekly meeting in one term.
- Variables: `term`, `day`, `start`, `end`, `location`

**`TimetableChoice`**: the section the student chose for each activity type, like `LEC0101` and `TUT0201`.

**`Timetable`**: a plan's chosen sections.
- Methods: `meetingsIn(AcademicTerm term)`, `conflicts()`, `missingChoices()`, `staleChoices()`

---

## API Usage

### dgx-backend

**Documentation:** [github.com/kam-eeel/dgx-backend](https://github.com/kam-eeel/dgx-backend)

**How it's used:**
- Our own backend, which scrapes every course and program in the FAS academic calendar, parses their requirements into JSON requirement trees, and stores them in SQLite. It refreshes daily.
- On launch, the app downloads the whole database with `GET /db`. On future runs, the app checks `If-Modified-Since` and only re-downloads if there is a newer version.

### UofT Timetable Builder (EASI)

**Documentation:** [https://apis.io/apis/university-of-toronto/university-of-toronto-courses-api/](https://apis.io/apis/university-of-toronto/university-of-toronto-courses-api/)

**How it's used:**
- dgx-backend calls it daily to record which sections (`F`, `S`, `Y`) each course is offered in this year. The app uses those to mark a course Not Met when it's placed in a term it isn't offered in (Use Case 8).
- The app will call it directly to get each course's lecture, tutorial, and practical sections and meeting times for the timetable (*no use case yet*; see Use Cases).

### Course evaluations

**Documentation:** To be decided.

**How it's used:** Shows past evaluation results for a course (Use Case 4). dgx-backend doesn't provide this data, so we still need to choose a source, and decide whether the app calls it directly or the backend adds an endpoint for it.

---

## Design Diagrams

### UML Class Diagram


### Sequence Diagrams

---

## How to Run

**Requirements:** Java 11 (Temurin 11) and Maven.

1. Clone the repo:
   ```sh
   git clone https://github.com/williamle22/team-project.git
   cd team-project
   ```
2. Run the tests:
   ```sh
   mvn test
   ```
   This also writes a coverage report to `target/site/jacoco/index.html`. To run the same check as CI, run `mvn verify`, which fails if `entity` or `use_case` has less than 100% line or branch coverage.

   If you save a copy of the catalog at `data/dgx.db`, `SqliteCatalogLoaderTest` also checks that every row of the real catalog loads. Otherwise, JUnit skips that test.
3. Run `app.Main` from IntelliJ.

To see eligibility checking without the planner, run the demo from the repo root. It needs the catalog at `data/dgx.db`, and it shows two sample plans with each course's status and requirement tree:
```sh
mvn -q test-compile exec:java -Dexec.mainClass=demo.EligibilityDemo -Dexec.classpathScope=test
```

To see graduation checking without the planner, run its demo the same way. It shows four sample plans, including one that's ready to graduate:
```sh
mvn -q test-compile exec:java -Dexec.mainClass=demo.GraduationDemo -Dexec.classpathScope=test
```

The first launch needs an internet connection to download the catalog, which is saved to `data/dgx.db` in the folder the app runs from (the repo root when run from IntelliJ). The `data/` folder is in `.gitignore`. After that, the app works offline, except for course evaluations.

To use a different server or data folder, pass system properties to the JVM. In IntelliJ, add them in the run configuration's **VM options** field. `-Ddgx.url=http://localhost:8080` points the app at another dgx-backend server, and `-Ddgx.data=/some/folder` stores the catalog cache and plans there instead of in `data/`.

---

## Notes

[Any additional project information.]
