package com.taskadapter.connector.mantis

import java.math.BigInteger
import java.util.Calendar
import biz.futureware.mantis.rpc.soap.client.ProjectData
import com.taskadapter.connector.FieldRow
import com.taskadapter.connector.testlib.{CommonTestChecks, ITFixture, TestSaver}
import com.taskadapter.model.{AssigneeFullName, Description, DueDate, GTaskBuilder, Summary}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class MantisIT extends FunSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val logger = LoggerFactory.getLogger(classOf[MantisIT])
  val setup = MantisTestConfig.getSetup

  logger.info("Running Mantis BT tests using: " + setup.getHost)

  val mgr = new MantisManager(setup.getHost, setup.getUserName, setup.getPassword)
  val junitTestProject = new ProjectData
  junitTestProject.setName("test project" + Calendar.getInstance.getTimeInMillis)
  junitTestProject.setDescription("test" + Calendar.getInstance.getTimeInMillis)
  val mantisUser = mgr.getCurrentUser
  val projectId = mgr.createProject(junitTestProject)
  val projectKey = projectId.toString
  val config = new MantisConfig
  config.setProjectKey(projectKey)
  val mantisConnector = new MantisConnector(config, setup)

  private val fixture = new ITFixture(setup.getHost, mantisConnector, id => CommonTestChecks.skipCleanup(id))

  override def afterAll() {
    if (mgr != null) mgr.deleteProject(new BigInteger(projectKey))
  }

  it("task is created and loaded") {
    fixture.taskIsCreatedAndLoaded(GTaskBuilder.withSummary()
      .setValue(AssigneeFullName, mantisUser.getReal_name)
      .setValue(Description, "123"),
      java.util.List.of(AssigneeFullName, Summary, Description, DueDate))
  }

  it("task created and updated") {
    val task = GTaskBuilder.withSummary()
    fixture.taskCreatedAndUpdatedOK(MantisFieldBuilder.getDefault().asJava, task, Summary, "new value")
  }

  private def getTestSaver(rows: java.util.List[FieldRow[_]]) = new TestSaver(getConnector(), rows)

  private def getConnector(): MantisConnector = getConnector(config)

  private def getConnector(config: MantisConfig) = new MantisConnector(config, setup)
}
