package com.taskadapter.connector.trello

import com.julienvey.trello.domain.{Card, TList}
import com.taskadapter.connector.definition.TaskId
import com.taskadapter.model.GTask

object TrelloToGTask {
  def convert(listCache: ListCache, card: Card) : GTask = {
    val task = new GTask()

    val key = card.getId
    task.setValue(TrelloField.id, key)
    task.setKey(key)
    // must set source system id, otherwise "update task" is impossible later
    task.setSourceSystemId(TaskId(0, key))

    task.setValue(TrelloField.name, card.getName)
    task.setValue(TrelloField.listId, card.getIdList)
    task.setValue(TrelloField.listName, listCache.getListNameById(card.getIdList))

    task
  }
}