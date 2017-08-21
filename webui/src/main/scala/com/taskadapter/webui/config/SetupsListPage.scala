package com.taskadapter.webui.config

import com.taskadapter.web.PopupDialog
import com.taskadapter.web.uiapi.SetupId
import com.taskadapter.webui.{ConfigOperations, Page, Tracker}
import com.vaadin.server.Sizeable
import com.vaadin.ui._
import com.vaadin.ui.themes.ValoTheme

class SetupsListPage(tracker: Tracker,
                     configOperations: ConfigOperations,
                     showEditSetup: (SetupId) => Unit,
                     showNewSetup: () => Unit) {

  val layout = new VerticalLayout

  val introLabel = new Label(Page.message("setupsListPage.intro"))
  introLabel.addStyleName(ValoTheme.LABEL_H3)
  introLabel.setWidth(null)
  val addButton = new Button(Page.message("setupsListPage.addButton"))
  addButton.addClickListener(_ => showNewSetup())

  private val introRow = new HorizontalLayout(introLabel, addButton)
  introRow.setWidth(850, Sizeable.Unit.PIXELS)
  introRow.setComponentAlignment(introLabel, Alignment.MIDDLE_LEFT)
  introRow.setComponentAlignment(addButton, Alignment.MIDDLE_RIGHT)
  introRow.setExpandRatio(introLabel, 1)

  layout.addComponent(introRow)

  val elementsComponent = new VerticalLayout()
  layout.addComponent(elementsComponent)

  refresh()

  private def refresh(): Unit = {
    elementsComponent.removeAllComponents()
    addElements()
  }

  private def addElements(): Unit = {
    val setups = configOperations.getConnectorSetups()
    setups.sortBy(x => x.connectorId)
      .foreach { setup =>
        val setupId = SetupId(setup.id.get)
        val editButton = new Button(Page.message("setupsListPage.editButton"))
        editButton.addClickListener(_ => {
          showEditSetup(setupId)
        })
        val connectorIdLabel = new Label(setup.connectorId)
        connectorIdLabel.setData(setupId)
        connectorIdLabel.setWidth(null)
        connectorIdLabel.addStyleName(ValoTheme.LABEL_BOLD)

        val connectorIdLayout = new HorizontalLayout(connectorIdLabel)
        connectorIdLayout.setWidth("200px")
        connectorIdLayout.setHeight("100%")
        connectorIdLayout.setComponentAlignment(connectorIdLabel, Alignment.MIDDLE_CENTER)

        val setupLabel = new Label(setup.label)
        setupLabel.addStyleName(ValoTheme.LABEL_BOLD)
        setupLabel.setData(setupId)
        setupLabel.setWidth(null)
        val usedByConfigs = configOperations.getConfigIdsUsingThisSetup(setupId)
        val usedByLabel = new Label(Page.message("setupsListPage.usedByConfigs", usedByConfigs.size + ""))
        usedByLabel.setData(setupId)

        val descriptionLayout = new VerticalLayout(setupLabel, usedByLabel)
        descriptionLayout.setWidth("450px")
        descriptionLayout.setHeight("80px")

        val editLayout = new VerticalLayout(editButton)
        editLayout.setComponentAlignment(editButton, Alignment.MIDDLE_CENTER)
        editLayout.setWidth("100px")
        editLayout.setHeight("100%")

        val deleteButton = new Button(Page.message("setupsListPage.deleteButton"))
        deleteButton.addClickListener(_ => {
          showDeleteDialog(setupId)
        })
        deleteButton.setEnabled(usedByConfigs.isEmpty)
        val deleteLayout = new VerticalLayout(deleteButton)
        deleteLayout.setComponentAlignment(deleteButton, Alignment.MIDDLE_CENTER)
        deleteLayout.setWidth("100px")
        deleteLayout.setHeight("100%")

        val row = new HorizontalLayout(connectorIdLayout,
          descriptionLayout,
          editLayout,
          deleteLayout
        )

        val panel = new Panel()
        panel.setContent(row)
        elementsComponent.addComponent(panel)
      }
  }

  val ui = layout

  private def showDeleteDialog(setupId: SetupId): Unit = {
    val messageDialog = PopupDialog.confirm(Page.message("setupsListPage.confirmDelete.question"),
      () => {
        configOperations.deleteConnectorSetup(setupId)
        tracker.trackEvent("setup", "deleted", "")
        refresh()
      })

    layout.getUI.addWindow(messageDialog)
  }
}