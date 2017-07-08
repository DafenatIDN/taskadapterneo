package com.taskadapter.connector.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.taskadapter.connector.Priorities;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GUser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JiraToGTask {
    private static final Logger logger = LoggerFactory.getLogger(JiraToGTask.class);

    private final Priorities priorities;

    public JiraToGTask(Priorities priorities) {
        this.priorities = priorities;
    }

    public List<GTask> convertToGenericTaskList(Iterable<Issue> issues) {
        // TODO see http://jira.atlassian.com/browse/JRA-6896
//        logger.info("Jira: no tasks hierarchy is supported");

        List<GTask> rootLevelTasks = new ArrayList<>();

        for (Issue issue : issues) {
            GTask genericTask = convertToGenericTask(issue);
            rootLevelTasks.add(genericTask);
        }
        return rootLevelTasks;
    }

    public GTask convertToGenericTask(Issue issue) {
        GTask task = new GTask();
        final Long longId = issue.getId();
        // do we really expect more than 2bln issues in a JIRA installation? probably not. screw it.
        task.setId(longId.intValue());
        task.setKey(issue.getKey());

        if (issue.getAssignee() != null) {
            String jiraUserLogin = issue.getAssignee().getName();
            task.setValue(JiraField.assignee(), jiraUserLogin);
        }

        task.setValue(JiraField.taskType(), issue.getIssueType().getName());
        task.setValue(JiraField.summary(), issue.getSummary());
        task.setValue(JiraField.description(), issue.getDescription());

        DateTime dueDate = issue.getDueDate();
        if (dueDate != null) {
            task.setValue(JiraField.dueDate(), dueDate.toDate());
        }

        // TODO set Done Ratio
        // task.setDoneRatio(issue.getDoneRatio());

        String jiraPriorityName = null;
        if (issue.getPriority() != null) {
            jiraPriorityName = issue.getPriority().getName();
        }
        Integer priorityValue = priorities.getPriorityByText(jiraPriorityName);
        task.setValue(JiraField.priority(), priorityValue);

        TimeTracking timeTracking = issue.getTimeTracking();
        if (timeTracking != null) {
            Integer originalEstimateMinutes = timeTracking.getOriginalEstimateMinutes();
            if (originalEstimateMinutes != null
                    && !originalEstimateMinutes.equals(0)) {
                task.setValue(JiraField.estimatedTime(), (float) (originalEstimateMinutes / 60.0));
            }
        }

        IssueField environmentField = issue.getField("environment");
        if (environmentField != null) {
            task.setValue(JiraField.environment(), environmentField.getValue());
        }

        processRelations(issue, task);
        processParentTask(issue, task);
        return task;
    }

    private static void processParentTask(Issue issue, GTask task) {
        if (issue.getIssueType().isSubtask()) {
            Object parent = issue.getField("parent").getValue();
            JSONObject json = (JSONObject) parent;
            try {
                String parentKey = (String) json.get("key");
                task.setParentKey(parentKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processRelations(Issue issue, GTask genericTask) {
        Iterable<IssueLink> links = issue.getIssueLinks();
        if (links != null) {
            for (IssueLink link : links) {
                if (link.getIssueLinkType().getDirection().equals(IssueLinkType.Direction.OUTBOUND)) {
                    String name = link.getIssueLinkType().getName();
                    if (name.equals(JiraConstants.getJiraLinkNameForPrecedes())) {
                        GRelation r = new GRelation(issue.getId()+"", JiraUtils.getIdFromURI(link.getTargetIssueUri()), GRelation.TYPE.precedes);
                        genericTask.getRelations().add(r);
                    } else {
                        logger.info("Relation type is not supported: " + link.getIssueLinkType() +
                                " - this link will be skipped for issue " + issue.getKey());
                    }
                }
            }
        }
    }

}
