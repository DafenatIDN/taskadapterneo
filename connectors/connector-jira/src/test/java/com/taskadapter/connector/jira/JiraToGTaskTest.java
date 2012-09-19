package com.taskadapter.connector.jira;

import com.atlassian.jira.rest.client.domain.Issue;
import com.taskadapter.connector.definition.PriorityResolver;
import com.taskadapter.model.GTask;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JiraToGTaskTest {

    @Test
    public void summaryIsConverted() throws Exception {
        Issue issue = MockData.loadIssue("issue_jira_5.0.1.json");
        JiraToGTask jiraToGTask = new JiraToGTask(getPriorityResolver());
        GTask task = jiraToGTask.convertToGenericTask(issue);
        assertEquals("Placeholder for imported time tracking data", task.getSummary());
    }

    @Test
    public void estimatedTimeConvertedByDefault() throws Exception {
        Issue issue = MockData.loadIssue("issue_with_time_tracking_5.0.json");
        JiraToGTask jiraToGTask = new JiraToGTask(getPriorityResolver());
        GTask task = jiraToGTask.convertToGenericTask(issue);
        assertEquals((Float) 45.0f, task.getEstimatedHours());
    }


    private PriorityResolver getPriorityResolver() {
        return new PriorityResolver() {
            @Override
            public Integer getPriorityNumberByName(String priorityName) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }
}
