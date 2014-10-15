package com.taskadapter.connector.common;

import java.util.List;

import com.taskadapter.connector.common.data.ConnectorConverter;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.TaskSaveResultBuilder;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GTask;

final class SimpleTaskSaver<N> {

    private final TaskSaveResultBuilder result;
    private final ConnectorConverter<GTask, N> converter;
    private final BasicIssueSaveAPI<N> saveAPI;
    private final ProgressMonitor monitor;

    protected SimpleTaskSaver(ConnectorConverter<GTask, N> converter,
            BasicIssueSaveAPI<N> saveAPI, TaskSaveResultBuilder resultBuilder,
            ProgressMonitor progressMonitor) {
        this.result = resultBuilder;
        this.converter = converter;
        this.saveAPI = saveAPI;
        this.monitor = progressMonitor == null ? ProgressMonitorUtils
                .getDummyMonitor() : progressMonitor;
    }

    void saveTasks(String parentIssueKey, List<GTask> tasks, DefaultValueSetter defaultValueSetter) {
        for (GTask task : tasks) {
            String newTaskKey = null;
            try {
                if (parentIssueKey != null) {
                    task.setParentKey(parentIssueKey);
                }
                GTask taskWithDefaultValues = defaultValueSetter.cloneAndReplaceEmptySelectedFieldsWithDefaultValues(task);
                N nativeIssueToCreateOrUpdate = converter.convert(taskWithDefaultValues);
                newTaskKey = submitTask(taskWithDefaultValues, nativeIssueToCreateOrUpdate);
            } catch (ConnectorException e) {
                result.addTaskError(task, e);
            } catch (Throwable t) {
                result.addTaskError(task, t);
                t.printStackTrace();
            }
            monitor.worked(1);

            saveTasks(newTaskKey, task.getChildren(), defaultValueSetter);
        }
    }

    /**
     * @return the newly created task's KEY
     */
    // TODO refactor? we only pass the GTask to check its IDs.
    private String submitTask(GTask task, N nativeTask)
            throws ConnectorException {
        String newTaskKey;
        if (task.getRemoteId() == null) {
            newTaskKey = saveAPI.createTask(nativeTask);
            result.addCreatedTask(task.getId(), newTaskKey);
        } else {
            newTaskKey = task.getRemoteId();
            saveAPI.updateTask(newTaskKey, nativeTask);
            result.addUpdatedTask(task.getId(), newTaskKey);
        }
        return newTaskKey;
    }
}
