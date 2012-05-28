package com.taskadapter.connector.common;

import com.taskadapter.connector.definition.Connector;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.SyncResult;
import com.taskadapter.model.GTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractConnector<T extends ConnectorConfig> implements Connector<T> {

    protected T config;

    protected AbstractConnector(T config) {
        super();
        this.config = config;
    }

    @Override
    public List<GTask> loadData(ProgressMonitor monitorIGNORED) {
        try {
            TaskLoader<T> taskLoader = getTaskLoader();
            taskLoader.beforeTasksLoad(config);
            List<GTask> tasks = taskLoader.loadTasks(config);
            Collections.sort(tasks, new Comparator<GTask>() {
                @Override
                public int compare(GTask o1, GTask o2) {
                    return o1.getId() - o2.getId();
                }
            });
            return tasks;
        } catch (TransportException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected abstract TaskLoader<T> getTaskLoader();

    @Override
    public GTask loadTaskByKey(String key) {
        TaskLoader<T> taskLoader = getTaskLoader();
        taskLoader.beforeTasksLoad(config);
        return taskLoader.loadTask(config, key);
    }

    @Override
    public SyncResult saveData(List<GTask> tasks, ProgressMonitor monitor) {
        try {
            return getDescriptor().getTaskSaver(config).saveData(tasks, monitor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getConfig() {
        return config;
    }

}
