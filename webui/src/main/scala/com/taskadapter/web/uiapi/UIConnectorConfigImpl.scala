package com.taskadapter.web.uiapi

import com.taskadapter.connector.NewConnector
import com.taskadapter.connector.definition._
import com.taskadapter.connector.definition.exceptions.BadConfigException
import com.taskadapter.model.Field
import com.taskadapter.web.service.Sandbox
import com.taskadapter.web.{DroppingNotSupportedException, PluginEditorFactory}
import com.taskadapter.webui.data.ExceptionFormatter
import com.vaadin.ui.ComponentContainer

/**
  * Implementation of RichConfig. Hides implementation details inside and keeps a
  * config-type magic inside.
  *
  * @tparam C type of a connector config.
  * @tparam S type of setup, e.g. [[FileSetup]], [[WebConnectorSetup]]
  */
class UIConnectorConfigImpl[C <: ConnectorConfig, S <: ConnectorSetup]
(connectorFactory: PluginFactory[C, S], editorFactory: PluginEditorFactory[C, S], config: C,
 connectorTypeId: String) extends UIConnectorConfig {

  private var setup: S = null.asInstanceOf[S]

  override def getConnectorSetup: S = setup

  override def setConnectorSetup(setup: ConnectorSetup): Unit = {
    this.setup = setup.asInstanceOf[S]
  }

  override def getConnectorTypeId: String = connectorTypeId

  override def getConfigString: String = connectorFactory.writeConfig(config).toString

  override def getLabel: String = getConnectorSetup.label

  @throws[BadConfigException]
  override def validateForLoad(): Unit = {
    editorFactory.validateForLoad(config, setup)
  }

  @throws[BadConfigException]
  override def validateForSave(): Unit = {
    editorFactory.validateForSave(config, setup)
  }

  @throws[BadConfigException]
  @throws[DroppingNotSupportedException]
  override def validateForDropIn(): Unit = {
    editorFactory.validateForDropInLoad(config)
  }

  @throws[BadConfigException]
  override def updateForSave(sandbox: Sandbox): ConnectorSetup = editorFactory.updateForSave(config, sandbox, setup)

  override def createConnectorInstance: NewConnector = connectorFactory.createConnector(config, setup)

  override def createMiniPanel(sandbox: Sandbox): ComponentContainer = editorFactory.getMiniPanelContents(sandbox, config, setup)

  override def getAllFields: Seq[Field[_]] = connectorFactory.getAllFields

  override def getDefaultFieldsForNewConfig: Seq[Field[_]] = connectorFactory.getDefaultFieldsForNewConfig

  override def getSourceLocation: String = editorFactory.describeSourceLocation(config, setup)

  override def getDestinationLocation: String = editorFactory.describeDestinationLocation(config, setup)

  override def decodeException(e: Throwable): String = {
    val guess = editorFactory.formatError(e)
    if (guess != null) return guess
    ExceptionFormatter.format(e)
  }

  override def toString: String = "UIConnectorConfigImpl{" + "connectorTypeId='" + connectorTypeId + '\'' + '}'
}
