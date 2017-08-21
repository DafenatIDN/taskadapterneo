package com.taskadapter.connector.trello

import com.julienvey.trello.Trello
import com.taskadapter.connector.definition.exceptions.ConnectorException
import com.taskadapter.model.GTask

import scala.collection.JavaConverters._

class TrelloTaskLoader(api: Trello) {
  @throws[ConnectorException]
  def loadTasks(config: TrelloConfig): Seq[GTask] = {
    try {
      val cards = api.getBoardCards(config.boardId).asScala
      val listsCache = new ListCache(api.getBoardLists(config.boardId).asScala)
      val gtasks = cards.map(c => TrelloToGTask.convert(listsCache, c))
      gtasks
    } catch {
      // TODO Trello process exceptions
      case e: Exception => throw e
    }
  }

  def loadTask(config: TrelloConfig, taskKey: String): GTask = {
    val listsCache = new ListCache(api.getBoardLists(config.boardId).asScala)
    val card = api.getCard(taskKey)
    TrelloToGTask.convert(listsCache, card)
  }
}