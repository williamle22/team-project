/**
 * Requirement trees that mirror the backend's requirement JSON. Every prerequisite, corequisite, program enrolment
 * rule, and program completion row is a tree of {@link Requirement} nodes. Each {@link RequirementVisitor} implements
 * one operation over a tree, such as printing it or collecting its course codes.
 */
package entity.requirement;
