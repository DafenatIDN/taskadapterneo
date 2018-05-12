package com.taskadapter.connector.github;

import com.taskadapter.connector.definition.TaskId;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GUser;
import org.eclipse.egit.github.core.Issue;

import java.util.ArrayList;
import java.util.List;

public class GithubToGTask {

    public List<GTask> toGTaskList(List<Issue> issues) {
        List<GTask> tasks = new ArrayList<>();
        for (Issue issue : issues) {
            GTask task = toGtask(issue);
            tasks.add(task);
        }
        return tasks;
    }

    protected GTask toGtask(Issue issue) {
        GTask task = new GTask();

        String stringKey = Integer.toString(issue.getNumber());
        task.setId(Long.parseLong(stringKey));
        task.setKey(stringKey);
        task.setSourceSystemId(new TaskId(issue.getId(), stringKey));

        task.setValue(GithubField.summary(), issue.getTitle());
        task.setValue(GithubField.description(), issue.getBody());
        task.setValue(GithubField.createdOn(), issue.getCreatedAt());
        task.setValue(GithubField.updatedOn(), issue.getUpdatedAt());

        if (issue.getAssignee() != null && !"".equals(issue.getAssignee().getLogin())) {
            GUser user = new GUser(issue.getAssignee().getLogin());
            task.setValue(GithubField.assignee(), user.getLoginName());
        }
        return task;
    }

}
