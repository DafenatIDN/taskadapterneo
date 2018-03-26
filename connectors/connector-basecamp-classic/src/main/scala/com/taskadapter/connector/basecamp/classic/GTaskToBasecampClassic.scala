package com.taskadapter.connector.basecamp.classic

import java.util.Date

import com.taskadapter.connector.basecamp.{BasecampTaskWrapper, UserResolver}
import com.taskadapter.connector.common.data.ConnectorConverter
import com.taskadapter.model.{GTask, GUser}
import org.w3c.dom.{Document, Element}

import scala.collection.JavaConverters._

class GTaskToBasecampClassic(resolver: UserResolver) extends ConnectorConverter[GTask, BasecampTaskWrapper] {

  /**
    * Convert a task from source to target format.
    *
    * @param source source object to convert.
    * @return converted object
    */
  override def convert(source: GTask): BasecampTaskWrapper = {

    val d = BasecampUtils.newXDoc
    val root = d.createElement("todo-item")
    d.appendChild(root)

    source.getFields.asScala.foreach { x =>
      processField(d, root, x._1, x._2)
    }
    val str = BasecampUtils.stringify(d)

    BasecampTaskWrapper(source.getKey, str, source.getValue(BasecampClassicField.doneRatio).asInstanceOf[Float])
  }

  def processField(d: Document, e: Element, fieldName: String, value: Any): Unit = {
    fieldName match {
      case BasecampClassicField.content.name =>
        val stringValue = value.asInstanceOf[String]
        XmlUtils.setString(d, e, "content", stringValue)
      case BasecampClassicField.doneRatio.name =>
        val booleanValue: Boolean = if (value == null) false
        else if (value.asInstanceOf[Float] >= 100) true
        else false

        val compl = d.createElement("completed")
        compl.setAttribute("type", "boolean")
        compl.appendChild(d.createTextNode(booleanValue.toString))
        e.appendChild(compl)

      case BasecampClassicField.dueDate.name =>
        XmlUtils.setLong(d, e, "due-at", value.asInstanceOf[Date])
      case BasecampClassicField.assignee.name =>
        writeAssignee(d, e, value.asInstanceOf[GUser])

      case _ => // ignore unknown fields
    }
  }

  def writeAssignee(d: Document, root: Element, assignee: GUser): Unit = {
    val field = "assignee"
    val elt = d.createElement("responsible-party")
    root.appendChild(elt)

    val resolvedAssignee = resolver.resolveUser(assignee)
    if (resolvedAssignee == null || resolvedAssignee.getId == null) elt.setAttribute("nil", "true")
    else elt.appendChild(d.createTextNode(resolvedAssignee.getId.toString))
  }
}
