package com.taskadapter.webui.pages

import com.taskadapter.webui.Page.message
import com.vaadin.ui.{Alignment, Button, GridLayout, TextField}

class NewConfigGiveDescription(saveClicked: String => Unit) extends WizardStep[String] {
  var result = ""
  val grid = new GridLayout(2, 2)
  grid.setSpacing(true)
  grid.setMargin(true)

  val descriptionTextField = new TextField(message("createConfigPage.description"))
  descriptionTextField.setInputPrompt(message("createConfigPage.optional"))
  descriptionTextField.setWidth("400px")
  grid.addComponent(descriptionTextField, 0, 0)
  grid.setComponentAlignment(descriptionTextField, Alignment.MIDDLE_CENTER)


  val saveButton = new Button(message("createConfigPage.create"))
  saveButton.addClickListener(_ => saveClicked(descriptionTextField.getValue))
  grid.addComponent(saveButton, 0, 1)
  grid.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT)

  override def getResult: String = result

  override def ui(config: Any) = grid
}