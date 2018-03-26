package com.taskadapter.webui.pages

import java.util.Comparator

import com.taskadapter.web.service.Sandbox
import com.taskadapter.web.uiapi.{ConfigId, UISyncConfig}
import com.taskadapter.webui.{ConfigOperations, Page, Tracker, WebUserSession}
import com.vaadin.ui._

object ConfigsPage {

  /**
    * Callback for config list page.
    */
  trait Callback {

    def startExport(config: UISyncConfig): Unit

    /**
      * Performs a forward drop-in.
      *
      * @param config config to use.
      * @param file   file to receive.
      */
    def forwardDropIn(config: UISyncConfig, file: Html5File): Unit

    /**
      * Performs a backward drop-in.
      *
      * @param config config to use.
      * @param file   file to receive.
      */
    def backwardDropIn(config: UISyncConfig, file: Html5File): Unit

    /**
      * User requested creation of a new config.
      */
    def newConfig(): Unit

    def showAllPreviousResults(configId: ConfigId): Unit

    def showLastExportResult(configId: ConfigId): Unit
  }

  /**
    * Comparator for configuration files.
    */
  class ConfigComparator(
                          /**
                            * Name of the current user.
                            */
                          val userName: String) extends Comparator[UISyncConfig] {
    override def compare(o1: UISyncConfig, o2: UISyncConfig): Int = {
      val isMyConfig1 = userName == o1.getOwnerName
      val isMyConfig2 = userName == o2.getOwnerName
      if (isMyConfig1 != isMyConfig2) return if (isMyConfig1) -1
      else 1
      val ucomp = o1.getOwnerName.compareTo(o2.getOwnerName)
      if (ucomp != 0) return ucomp
      o1.getLabel.compareTo(o2.getLabel)
    }
  }

}

class ConfigsPage(tracker: Tracker, showAll: Boolean, callback: ConfigsPage.Callback, configOperations: ConfigOperations,
                  webUserSession: WebUserSession,
                  sandbox: Sandbox) {
  val displayMode = if (showAll) DisplayMode.ALL_CONFIGS
  else DisplayMode.OWNED_CONFIGS

  private var configs = Seq[UISyncConfig]()

  val layout = new VerticalLayout
  layout.setSpacing(true)
  val actionPanel = new HorizontalLayout
  actionPanel.setWidth("100%")
  actionPanel.setSpacing(true)
  val addButton = new Button(Page.message("configsPage.buttonNewConfig"))
  addButton.addClickListener(_ => callback.newConfig())
  actionPanel.addComponent(addButton)
  actionPanel.setComponentAlignment(addButton, Alignment.MIDDLE_LEFT)

  val filterPanel = new HorizontalLayout
  val filterField = new TextField
  filterField.addTextChangeListener(e => filterFields(e.getText))
  filterPanel.addComponent(new Label(Page.message("configsPage.filter")))
  filterPanel.addComponent(filterField)
  filterPanel.setSpacing(true)
  actionPanel.addComponent(filterPanel)
  actionPanel.setComponentAlignment(filterPanel, Alignment.MIDDLE_RIGHT)
  val configsTopLevelLayout = new HorizontalLayout()
  configsTopLevelLayout.setSpacing(true)
  configsTopLevelLayout.setSizeFull()

  val configsLayout = new HorizontalLayout()
  configsLayout.setWidth("100%")

  val listSelect = configureListSelect()
  val listPanel = new Panel(listSelect)
  listPanel.setWidth("270px")
  configsLayout.addComponent(listPanel)

  val configArea = new Panel()
  configsLayout.addComponent(configArea)
  configsLayout.setExpandRatio(configArea, 1.0f)

  configsTopLevelLayout.addComponent(configsLayout)
  configsTopLevelLayout.setComponentAlignment(configsLayout, Alignment.TOP_CENTER)

  layout.addComponent(actionPanel)
  layout.addComponent(configsTopLevelLayout)
  layout.setComponentAlignment(actionPanel, Alignment.TOP_LEFT)
  refreshConfigs()

  def refreshConfigs() = {
    val loadedConfigs = if (showAll) configOperations.getManageableConfigs
    else configOperations.getOwnedConfigs
    configs = loadedConfigs.sortBy(c => c.getOwnerName)
    setDisplayedConfigs(configs)
    filterFields(filterField.getValue)
    if (webUserSession.hasCurrentConfig) {
      listSelect.select(webUserSession.getCurrentConfigId)
    } else {
      clearConfigSummaryArea()
    }
    listPanel.setVisible(loadedConfigs.nonEmpty)
  }

  private def setDisplayedConfigs(dispConfigs: Seq[UISyncConfig]): Unit = {
    listSelect.removeAllItems()
    dispConfigs.foreach(config => {
      listSelect.addItem(config.id)
      listSelect.setItemCaption(config.id, config.label)
    })
  }

  def showConfigSummary(configId: ConfigId): Unit = {
    val maybeConfig = configOperations.getConfig(configId)
    if (maybeConfig.isDefined) {
      val component = new ConfigSummaryPanel(maybeConfig.get, displayMode, callback, configOperations,
        sandbox,
        () => refreshConfigs(),
        () => callback.showAllPreviousResults(configId),
        () => callback.showLastExportResult(configId), tracker, webUserSession).ui()
      component.setMargin(true)
      configArea.setContent(component)

      tracker.trackPage("config_summary")
      webUserSession.setCurrentConfigId(configId)
    }
  }

  private def configureListSelect(): ListSelect = {
    val listSelect = new ListSelect()
    listSelect.setNullSelectionAllowed(false)
    listSelect.setWidth("100%")
    listSelect.setImmediate(true)
    listSelect.addValueChangeListener(e => {
      val configId = e.getProperty.getValue.asInstanceOf[ConfigId]
      // it will be null when an element is removed from the list
      if (configId != null) {
        showConfigSummary(configId)
      }
    })
    listSelect
  }

  private def filterFields(filterStr: String): Unit = {
    val words = if (filterStr == null) new Array[String](0)
    else filterStr.toLowerCase.split(" +")
    val res = configs.filter(config => matches(config, words))
    setDisplayedConfigs(res)
  }

  private def matches(config: UISyncConfig, filters: Array[String]): Boolean = {
    if (filters.length == 0) return true
    for (name <- filters) {
      val confName = displayMode.nameOf(config)
      if (!confName.toLowerCase.contains(name) && !config.getConnector1.getLabel.toLowerCase.contains(name) && !config.getConnector2.getLabel.toLowerCase.contains(name)) return false
    }
    true
  }

  def clearConfigSummaryArea(): Unit = {
    configArea.setContent(null)
  }

  def ui = layout
}