package com.taskadapter.connector.redmine

import com.taskadapter.connector.Field
import java.util

import com.taskadapter.model._

import scala.collection.JavaConverters._

object RedmineField {
  // Redmine field names
  val id = Field("Id")
  val summary = Field("Summary")
  val description = Field("Description")
  val taskType = Field("Tracker type")
  val estimatedTime = Field.float("Estimated time")

  /**
    * %% complete (e.g. "30%"). int value
    */
  val doneRatio = Field.float("Done ratio")
  val author = Field.user("Author")
  val assignee = Field.user("Assignee")
  val dueDate = Field.date("Due Date")
  val startDate = Field.date("Start Date")
  val createdOn = Field.date("Created On")
  val updatedOn = Field.date("Updated On")
  val taskStatus = Field("Task status")
  val targetVersion = Field("Target Version")
  val priority = Field.integer("Priority")

  def fields = List(author,
    summary,
    description,
    taskType,
    estimatedTime,
    doneRatio,
    assignee,
    dueDate,
    startDate,
    createdOn,
    updatedOn,
    taskStatus,
    targetVersion,
    priority)

  def fieldsAsJava(): util.List[Field] = fields.asJava

  def suggestedStandardFields = Map(id -> Id,
    author -> Reporter,
    summary -> Summary, description -> Description, taskType -> TaskType,
    estimatedTime -> EstimatedTime,
    doneRatio -> DoneRatio,
    assignee -> Assignee,
    dueDate -> DueDate,
    startDate -> StartDate,
    createdOn -> CreatedOn,
    updatedOn -> UpdatedOn,
    taskStatus -> TaskStatus,
    targetVersion -> TargetVersion,
    priority -> Priority)

  def getSuggestedCombinations(): Map[Field, StandardField] = {
    suggestedStandardFields
  }
}
