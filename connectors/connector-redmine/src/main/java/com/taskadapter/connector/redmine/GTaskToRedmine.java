package com.taskadapter.connector.redmine;

import com.taskadapter.connector.common.data.ConnectorConverter;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor.FIELD;
import com.taskadapter.model.GUser;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import com.taskadapter.redmineapi.bean.Version;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GTaskToRedmine implements ConnectorConverter<GTask, Issue> {

    private final RedmineConfig config;
    private final Collection<FIELD> fieldsToExport;
    private final List<User> users;
    private final List<IssueStatus> statusList;
    private final List<Version> versions;
    private final Map<String, Integer> priorities;
    private final Project project;

    public GTaskToRedmine(RedmineConfig config, Collection<FIELD> fieldsToExport,
                          Map<String, Integer> priorities, Project project, List<User> users,
                          List<IssueStatus> statusList,
                          List<Version> versions) {
        this.config = config;
        this.fieldsToExport = fieldsToExport;
        this.priorities = priorities;
        this.project = project;
        this.users = users;
        this.statusList = statusList;
        this.versions = versions;
    }

    private static Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    // TODO refactor this into multiple tiny testable methods
    Issue convertToRedmineIssue(GTask task) {
        final String key = task.getKey();
        Integer numericKey = parseIntOrNull(key);
        Issue issue = IssueFactory.create(numericKey);
        issue.setParentId(parseIntOrNull(task.getParentKey()));
        issue.setProject(project);

        if (fieldsToExport.contains(FIELD.SUMMARY)) {
            issue.setSubject(task.getSummary());
        }
        if (fieldsToExport.contains(FIELD.START_DATE)) {
            issue.setStartDate(task.getStartDate());
        }
        if (fieldsToExport.contains(FIELD.DUE_DATE)) {
            issue.setDueDate(task.getDueDate());
        }

        if (fieldsToExport.contains(FIELD.ESTIMATED_TIME)) {
            issue.setEstimatedHours(task.getEstimatedHours());
        }

        if (fieldsToExport.contains(FIELD.DONE_RATIO)) {
            issue.setDoneRatio(task.getDoneRatio());
        }

        if (fieldsToExport.contains(FIELD.TASK_TYPE)) {
            String trackerName = task.getType();
            if (trackerName == null) {
                trackerName = config.getDefaultTaskType();
            }
            issue.setTracker(project.getTrackerByName(trackerName));
        }

        if (fieldsToExport.contains(FIELD.TASK_STATUS)) {
            String statusName = task.getStatus();
            if (statusName == null) {
                statusName = config.getDefaultTaskStatus();
            }

            IssueStatus status = getStatusByName(statusName);
            if (status != null) {
                issue.setStatusId(status.getId());
                issue.setStatusName(status.getName());
            }
        }
        
        if (fieldsToExport.contains(FIELD.DESCRIPTION)) {
            issue.setDescription(task.getDescription());
        }
        
        if (fieldsToExport.contains(FIELD.PRIORITY)) {
            Integer priority = task.getPriority();
            if (priority != null) {
                final String priorityName = config.getPriorities()
                        .getPriorityByMSP(priority);
                final Integer val = priorities.get(priorityName);
                if (val != null) {
                    issue.setPriorityId(val);
                    issue.setPriorityText(priorityName);
                }
            }
        }

        if (fieldsToExport.contains(FIELD.TARGET_VERSION)) {
            Version version = getVersionByName(task.getTargetVersionName());
            issue.setTargetVersion(version);
        }
        
        issue.setCreatedOn(task.getCreatedOn());
        issue.setUpdatedOn(task.getUpdatedOn());

        processAssignee(task, issue);
        processTaskStatus(task, issue);

        return issue;
    }

    private Version getVersionByName(String versionName) {
        if (versions == null || versionName == null) {
            return null;
        }
        for (Version version : versions) {
            if (version.getName().equals(versionName)) {
                return version;
            }
        }
        return null;
    }

    private void processAssignee(GTask genericTask, Issue redmineIssue) {
        if (fieldsToExport.contains(FIELD.ASSIGNEE)) {
            GUser ass = genericTask.getAssignee();
            if ((ass != null) && (ass.getLoginName() != null || ass.getDisplayName() != null)) {
                User rmAss;
                if (config.isFindUserByName() || ass.getId() == null) {
                    rmAss = findRedmineUserInCache(ass);
                } else {
                    rmAss = UserFactory.create(ass.getId());
                    rmAss.setLogin(ass.getLoginName());
                }
                redmineIssue.setAssignee(rmAss);
            }
        }
    }

    private void processTaskStatus(GTask task, Issue issue) {
        if (fieldsToExport.contains(FIELD.TASK_STATUS)) {
            String statusName = task.getStatus();
            if (statusName == null) {
                statusName = config.getDefaultTaskStatus();
            }

            IssueStatus status = getStatusByName(statusName);
            if (status != null) {
                issue.setStatusId(status.getId());
                issue.setStatusName(status.getName());
            }
        }
    }

    /**
     * @return NULL if the user is not found or if "users" weren't previously set via setUsers() method
     */
    User findRedmineUserInCache(GUser ass) {
        // getting best name to search
        String nameToSearch = ass.getLoginName();
        if (nameToSearch == null || "".equals(nameToSearch)) {
            nameToSearch = ass.getDisplayName();
        }
        if (users == null || nameToSearch == null || "".equals(nameToSearch)) {
            return null;
        }

        // Searching for the user
        User foundUser = null;
        for (User user : users) {
            if (nameToSearch.equalsIgnoreCase(user.getLogin())
                    || nameToSearch.equalsIgnoreCase(user.getFullName())) {
                foundUser = user;
                break;
            }
        }
        return foundUser;
    }

    /**
     * @return NULL if the status is not found or if "statusList" weren't previously set via setStatusList() method
     */
    private IssueStatus getStatusByName(String name) {
        if (statusList == null || name == null) {
            return null;
        }

        IssueStatus foundStatus = null;
        for (IssueStatus status : statusList) {
            if (status.getName().equalsIgnoreCase(name)) {
                foundStatus = status;
                break;
            }
        }

        return foundStatus;
    }

    @Override
    public Issue convert(GTask source) throws ConnectorException {
        return convertToRedmineIssue(source);
    }

}
