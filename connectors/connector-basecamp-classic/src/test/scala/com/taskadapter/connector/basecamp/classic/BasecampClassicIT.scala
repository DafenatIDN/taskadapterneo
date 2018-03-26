package com.taskadapter.connector.basecamp.classic

import java.util

import com.taskadapter.connector.basecamp.classic.beans.{BasecampProject, TodoList}
import com.taskadapter.connector.basecamp.classic.exceptions.ObjectNotFoundException
import com.taskadapter.connector.basecamp.classic.transport.{BaseCommunicator, ObjectAPI, ObjectAPIFactory}
import com.taskadapter.connector.testlib.CommonTestChecks
import com.taskadapter.model.GTaskBuilder
import org.junit.Assert.{assertEquals, assertNotNull, assertTrue}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class BasecampClassicIT extends FunSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with BasecampClassicTestProject {
  private val factory = new ObjectAPIFactory(new BaseCommunicator)

  it("task is created and loaded") {
    withTempProject { todoListKey =>
      CommonTestChecks.taskIsCreatedAndLoaded(getConnector(todoListKey),
        GTaskBuilder.withRandom(BasecampClassicField.content),
        BasecampClassicFieldBuilder.getDefault(), BasecampClassicField.content,
        CommonTestChecks.skipCleanup)
    }
  }
  it("task is updated") {
    withTempProject { todoListKey =>
      CommonTestChecks.taskCreatedAndUpdatedOK(TestBasecampConfig.setup.host,
        getConnector(todoListKey), BasecampClassicFieldBuilder.getDefault(),
        GTaskBuilder.withRandom(BasecampClassicField.content),
        BasecampClassicField.content.name, "new value",
        CommonTestChecks.skipCleanup)
    }
  }

  def getConnector(todoListKey: String): BasecampClassicConnector = {
    val config = TestBasecampConfig.config
    config.setTodoKey(todoListKey)
    new BasecampClassicConnector(config, TestBasecampConfig.setup, factory)
  }

  it("smokeTest") {
    val api: ObjectAPI = factory.createObjectAPI(setup)
    assertNotNull(api.getObject("people/me.xml"))
  }

  it("someProjectsAreLoaded") {
    val projects: util.List[BasecampProject] = BasecampUtils.loadProjects(factory, setup)
    assertTrue(projects.size > 0)
  }

  it("someTodoListsAreLoaded") {
    val lists: util.List[TodoList] = BasecampUtils.loadTodoLists(factory, config, setup)
    assertTrue(lists.size > 0)
  }

  it("projectIsLoaded") {
    assertNotNull(BasecampUtils.loadProject(factory, config, setup))
  }

  it("todoListIsCreatedAndDeleted") {
    val time: Long = System.currentTimeMillis
    val todoListName: String = "list" + time
    val todoListDescription: String = "some description here" + time
    val todoList: TodoList = BasecampUtils.createTodoList(factory, config, setup, todoListName, todoListDescription)
    assertEquals(todoListName, todoList.getName)
    assertEquals(todoListDescription, todoList.getDescription)
    val key: String = todoList.getKey
    config.setTodoKey(key)
    BasecampUtils.deleteTodoList(factory, config, setup)
    try {
      BasecampUtils.loadTodoList(factory, config, setup)
      fail("Must have failed with ObjectNotFoundException.")
    } catch {
      case e: ObjectNotFoundException =>
        System.out.println("Got expected ObjectNotFoundException")
    }
  }
}