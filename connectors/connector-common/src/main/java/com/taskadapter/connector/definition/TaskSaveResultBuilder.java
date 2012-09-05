package com.taskadapter.connector.definition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Save result builder.
 * 
 * @author maxkar
 * 
 */
public final class TaskSaveResultBuilder {
    private String targetFileAbsolutePath;

    /**
     * Number of updated tasks.
     */
    private int updatedTasksNumber;

    /**
     * Number of created tasks.
     */
    private int createdTasksNumber;

    // maps ID --> remote KEY when new tasks are created
    private final Map<Integer, String> idToRemoteKeyMap = new HashMap<Integer, String>();

    public void setTargetFileAbsolutePath(String targetFileAbsolutePath) {
        this.targetFileAbsolutePath = targetFileAbsolutePath;
    }
    
    public void addCreatedTask(Integer originalId, String newId) {
        idToRemoteKeyMap.put(originalId, newId);
        createdTasksNumber++;
    }

    public void addUpdatedTask(Integer originalId, String newId) {
        idToRemoteKeyMap.put(originalId, newId);
        updatedTasksNumber++;
    }
    
    public String getRemoteKey(Integer originalId) {
        return idToRemoteKeyMap.get(originalId);
    }

    public TaskSaveResult getResult() {
        return new TaskSaveResult(targetFileAbsolutePath, updatedTasksNumber,
                createdTasksNumber,
                Collections.unmodifiableMap(new HashMap<Integer, String>(
                        idToRemoteKeyMap)));
    }
}