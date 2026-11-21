/**
 * Evaluates a course's requisites and exclusions against what a student has done. An {@link AcademicSnapshot} describes
 * the student at one point in time and knows nothing about plans. Every check gives a {@link Status}. {@code REVIEW}
 * means the app can't decide, so the student checks it manually.
 */
package entity.evaluation;
