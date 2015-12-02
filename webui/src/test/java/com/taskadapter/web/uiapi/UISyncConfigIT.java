package com.taskadapter.web.uiapi;

import com.taskadapter.connector.common.ProgressMonitorUtils;
import com.taskadapter.connector.definition.TaskSaveResult;
import com.taskadapter.connector.testlib.TestUtils;
import com.taskadapter.model.GTask;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * This is a FULL-stack test using the same class as UI when "export" button is clicked.
 */
public class UISyncConfigIT {

    // TODO maybe use a temporary project?

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private UISyncConfig config;
    private UISyncConfig toRedmineConfig;

    @Before
    public void beforeEachTest() throws IOException {
        config = ConfigLoader.loadConfig("Redmine_Microsoft-Project_3.ta_conf");
        toRedmineConfig = config.reverse();
    }

    @Test
    public void tasksCanBeSavedToRedmine() throws Exception {
        List<GTask> gTasks = TestUtils.generateTasks(1);
        final UISyncConfig.TaskExportResult taskExportResult = toRedmineConfig.saveTasks(gTasks, ProgressMonitorUtils.getDummyMonitor());
        final TaskSaveResult saveResult = taskExportResult.saveResult;
        assertThat(saveResult.hasErrors()).isFalse();
        assertThat(saveResult.getCreatedTasksNumber()).isEqualTo(1);
        assertThat(saveResult.getUpdatedTasksNumber()).isEqualTo(0);
    }

    /**
     * regression test for https://bitbucket.org/taskadapter/taskadapter/issues/43/tasks-are-not-updated-in-redmine-404-not
     */
    @Test
    public void taskWithRemoteIdIsUpdatedInRedmine() throws Exception {
        List<GTask> gTasks = TestUtils.generateTasks(1);
        final UISyncConfig toRedmineConfig = config.reverse();
        final UISyncConfig.TaskExportResult taskExportResult = toRedmineConfig.saveTasks(gTasks, ProgressMonitorUtils.getDummyMonitor());
        final TaskSaveResult saveResult = taskExportResult.saveResult;

        final String key = saveResult.getRemoteKeys().iterator().next();

        final GTask createdTask = gTasks.get(0);
        createdTask.setRemoteId(key);
        createdTask.setSummary("updated summary");

        final UISyncConfig.TaskExportResult secondResultWrapper = toRedmineConfig.saveTasks(gTasks, ProgressMonitorUtils.getDummyMonitor());
        final TaskSaveResult secondResult = secondResultWrapper.saveResult;
        assertThat(secondResult.hasErrors()).isFalse();
        assertThat(secondResult.getCreatedTasksNumber()).isEqualTo(0);
        assertThat(secondResult.getUpdatedTasksNumber()).isEqualTo(1);
    }
}
