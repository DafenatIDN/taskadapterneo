package com.taskadapter.web

import com.taskadapter.connector.definition.exceptions.BadConfigException
import com.taskadapter.connector.definition.{ConnectorConfig, ConnectorSetup}
import com.taskadapter.web.service.Sandbox
import com.vaadin.ui.ComponentContainer

trait PluginEditorFactory[C <: ConnectorConfig, S <: ConnectorSetup] extends ExceptionFormatter {
  def getMiniPanelContents(sandbox: Sandbox, config: C, setup: S): ComponentContainer

  def isWebConnector: Boolean

  def getEditSetupPanel(sandbox: Sandbox, setup: Option[S]): ConnectorSetupPanel

  /**
    * Validates a connector config for save mode. If validation fails, plugin
    * editor factory should provide appropriate user-friendly message.
    *
    * @param config config to check.
    * @throws BadConfigException if validation fails.
    */
  @throws[BadConfigException]
  def validateForSave(config: C, setup: S): Unit

  /**
    * Validates a connector config for load mode. If validation fails, plugin
    * editor factory should provide appropriate user-friendly message.
    *
    * @param config config to check.
    * @throws BadConfigException if validation fails.
    */
  @throws[BadConfigException]
  def validateForLoad(config: C, setup: S): Unit

  /**
    * Validates config for "drop-in" loading.
    *
    * @param config config to validate.
    * @throws BadConfigException            if config cannot accept a drop for some reason.
    * @throws DroppingNotSupportedException if dropping is not supported either by this plugin or by
    *                                       "config type" (i.e. it's not a "configuration
    *                                       mistake", it is a definitely an "unsupported configuration").
    */
  @throws[BadConfigException]
  @throws[DroppingNotSupportedException]
  def validateForDropInLoad(config: C): Unit

  /**
    * Updates config for save (if it is possible). Primary purpose of this
    * method is to give a plugin editor last chance to update configuration
    * before save (for example, create a file). However, non-file plugins also
    * may update config if it is feasible.
    *
    * @param config  config to update.
    * @param sandbox local filesystem sandbox.
    * @return new setup, possibly updated
    * @throws BadConfigException if config cannot be automatically updated to a "valid for save" state.
    */
  @throws[BadConfigException]
  def updateForSave(config: C, sandbox: Sandbox, setup: S): S

  /**
    * Describes source location in a user-friendly manner.
    *
    * @return user-friendly description of a source location.
    */
  def describeSourceLocation(config: C, setup: S): String

  /**
    * Describes destination location in a user-friendly manner.
    *
    * @return user-friendly description of destination location.
    */
  def describeDestinationLocation(config: C, setup: S): String
}