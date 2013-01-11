package com.taskadapter.connector.common;

import java.util.List;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.TaskSaveResultBuilder;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;

/**
 * Task saving utils.
 * 
 */
public class TaskSavingUtils {
    private TaskSavingUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Saves a relations between tasks. Result builder must have correct
     * mappings between old and new task identifiers. Exceptions reported as a
     * general exceptions in a result builder.
     * 
     * @param config
     *            to use. This config may prevent update of relations.
     * @param tasks
     *            tasks to save.
     * @param saver
     *            relation saver.
     * @param resultBuilder
     *            result builder.
     */
    public static void saveRemappedRelations(ConnectorConfig config,
            List<GTask> tasks, RelationSaver saver,
            TaskSaveResultBuilder resultBuilder) {
        if (!config.getSaveIssueRelations()) {
            return;
        }
        
        final List<GRelation> result = RelationUtils.convertRelationIds(tasks,
                resultBuilder);
        try {
            saver.saveRelations(result);
        } catch (ConnectorException e) {
            resultBuilder.addGeneralError(e);
        }
    }

}
