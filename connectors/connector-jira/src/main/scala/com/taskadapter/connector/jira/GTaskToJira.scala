package com.taskadapter.connector.jira

import java.util

import com.atlassian.jira.rest.client.api.domain.input.{ComplexIssueInputFieldValue, FieldInput, IssueInputBuilder}
import com.atlassian.jira.rest.client.api.domain.{BasicComponent, IssueFieldId, IssueType, Priority, TimeTracking, Version}
import com.google.common.collect.ImmutableList
import com.taskadapter.connector.common.ValueTypeResolver
import com.taskadapter.connector.common.data.ConnectorConverter
import com.taskadapter.connector.definition.exceptions.ConnectorException
import com.taskadapter.model.{GTask, GUser}
import org.joda.time.DateTime

import scala.collection.JavaConverters._

object GTaskToJira {
  def getVersion(versions: Iterable[Version], versionName: String): Version = {
    versions.find(_.getName == versionName).orNull
  }

  def getComponent(objects: Iterable[BasicComponent], name: String): BasicComponent = {
    objects.find(_.getName == name).orNull
  }
}

class GTaskToJira(config: JiraConfig,
                  customFieldResolver: CustomFieldResolver,
                  issueTypeList: Iterable[IssueType],
                  versions: Iterable[Version],
                  components: Iterable[BasicComponent],
                  jiraPriorities: Iterable[Priority])
  extends ConnectorConverter[GTask, IssueWrapper] {

  val priorities = jiraPriorities.map(p => p.getName -> p).toMap

  def convertToJiraIssue(task: GTask) : IssueWrapper = {
    val issueInputBuilder = new IssueInputBuilder(config.getProjectKey, findIssueTypeId(task))
    if (task.getParentIdentity != null) {
      // See http://stackoverflow.com/questions/14699893/how-to-create-subtasks-using-jira-rest-java-client
      val parent = new util.HashMap[String, AnyRef]
      parent.put("key", task.getParentIdentity.key)
      val parentField = new FieldInput("parent", new ComplexIssueInputFieldValue(parent))
      issueInputBuilder.setFieldInput(parentField)
    }
    import scala.collection.JavaConversions._
    for (row <- task.getFields.entrySet) {
      processField(issueInputBuilder, row.getKey, row.getValue)
    }
    val affectedVersion = GTaskToJira.getVersion(versions, config.getAffectedVersion)
    val fixForVersion = GTaskToJira.getVersion(versions, config.getFixForVersion)
    val component = GTaskToJira.getComponent(components, config.getComponent)
    if (affectedVersion != null) issueInputBuilder.setAffectedVersions(ImmutableList.of(affectedVersion))
    if (fixForVersion != null) issueInputBuilder.setFixVersions(ImmutableList.of(fixForVersion))
    if (component != null) issueInputBuilder.setComponents(ImmutableList.of(component))
    val issueInput = issueInputBuilder.build
    IssueWrapper(task.getKey, issueInput)
  }

  private def processField(issueInputBuilder: IssueInputBuilder, fieldName: String, value: Any) : Unit = {
    fieldName match {
      case JiraField.summary.name => issueInputBuilder.setSummary(value.asInstanceOf[String])
      case JiraField.description.name => issueInputBuilder.setDescription(value.asInstanceOf[String])
      case JiraField.dueDate.name => if (value != null) {
        val dueDateTime = new DateTime(value)
        issueInputBuilder.setDueDate(dueDateTime)
      }
      case JiraField.assignee.name => if (value != null) {
        issueInputBuilder.setAssigneeName(value.asInstanceOf[GUser].getLoginName)
      }
      case JiraField.reporter.name => if (value != null) {
        issueInputBuilder.setReporterName(value.asInstanceOf[GUser].getLoginName)
      }
      case JiraField.priority.name =>
        val priorityNumber = value.asInstanceOf[Integer]
        val jiraPriorityName = config.getPriorities.getPriorityByMSP(priorityNumber)
        if (!jiraPriorityName.isEmpty) {
          val priority = priorities(jiraPriorityName)
          if (priority != null) issueInputBuilder.setPriority(priority)
        }
      case JiraField.estimatedTime.name => if (value != null) {
        val estimatedHours = value.asInstanceOf[Float]
        val timeTracking = new TimeTracking(Math.round(estimatedHours * 60), null, null)
        issueInputBuilder.setFieldValue(IssueFieldId.TIMETRACKING_FIELD.id, timeTracking)
      }
      case _ =>
        val fieldSchema = customFieldResolver.getId(fieldName)
        if (fieldSchema.isDefined) {
          val fullIdForSave = fieldSchema.get.fullIdForSave
          val valueWithProperJiraType = getConvertedValue(fieldSchema.get, value)
          issueInputBuilder.setFieldValue(fullIdForSave, valueWithProperJiraType)
        }
    }
  }

  /**
    * If the value is not of type String, this will throw exception. This is to fail fast rather than to attempt
    * to recover from incorrect types in the passed data.
    */
  def isNonEmptyString(value: Any) : Boolean = {
    value !=null && value.asInstanceOf[String].trim != ""
  }

  def getConvertedValue(fieldSchema: JiraFieldDefinition, value: Any) : Any = {
    fieldSchema.typeName match {
      case "array" if fieldSchema.itemsTypeIfArray.get == "string" => List(value).asJava
      case "array" if fieldSchema.itemsTypeIfArray.get == "option" => getComplexValueList(value)
      case "number" => ValueTypeResolver.getValueAsFloat(value)
      case _ => value
    }
  }

  def getComplexValueList(value: Any) : util.List[ComplexIssueInputFieldValue] = {
    if (value.isInstanceOf[Seq[String]]) {
      val seq = value.asInstanceOf[Seq[String]]
      seq.map(ComplexIssueInputFieldValue.`with`("value", _)
      )
      .toList.asJava
    } else {
      throw new RuntimeException(s"unknown value type: $value")
    }
  }

  /**
    * Finds an issue type id to use.
    *
    * @param task task to get an issue id.
    * @return issue type id.
    */
  private def findIssueTypeId(task: GTask): Long = {
    // Use explicit task type when possible.
    val value = task.getValue(JiraField.taskType.name)
    val explicitTypeId = getIssueTypeIdByName(value.asInstanceOf[String])
    if (explicitTypeId != null) return explicitTypeId
    // Use default type for the task when
    getIssueTypeIdByName(
      if (task.getParentIdentity == null) config.getDefaultTaskType
      else config.getDefaultIssueTypeForSubtasks
    )
  }

  private def getIssueTypeIdByName(issueTypeName: String) = {
    issueTypeList.find(i => i.getName == issueTypeName).map(_.getId).orNull
  }

  @throws[ConnectorException]
  override def convert(source: GTask): IssueWrapper = convertToJiraIssue(source)
}