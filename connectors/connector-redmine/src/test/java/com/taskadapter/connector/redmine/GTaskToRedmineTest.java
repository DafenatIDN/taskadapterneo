package com.taskadapter.connector.redmine;

import com.taskadapter.connector.testlib.TestMappingUtils;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.model.GTaskDescriptor.FIELD;
import com.taskadapter.model.GUser;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import com.taskadapter.redmineapi.bean.Version;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GTaskToRedmineTest {

    private final Project project = ProjectFactory.create();

    @Test
    public void summaryIsConvertedByDefault() {
        checkSummary(createDefaultConverter(), "summary 1");
    }

    @Test
    public void summaryIsConvertedWhenSelected() {
        checkSummary(createConverterWithSelectedField(FIELD.SUMMARY, null), "summary 1");
    }

    @Test
    public void summaryIsIgnoredWhenUnselected() {
        checkSummary(createConverterWithUnselectedField(FIELD.SUMMARY), null);
    }

    private void checkSummary(GTaskToRedmine converter, String expected) {
        GTask task = new GTask();
        task.setSummary("summary 1");
        Issue redmineIssue = converter.convertToRedmineIssue(task);
        assertEquals(expected, redmineIssue.getSubject());
    }

    @Test
    public void gUserWithRedmineLoginOnLoginName() {
        GTask gtask = createDummyTaskForUser("diogo.nascimento");
        Issue task = getConverterWithAssigneeMapped().convertToRedmineIssue(gtask);
        assertNotNull(task.getAssignee());
        assertEquals("diogo.nascimento", task.getAssignee().getLogin());
    }

    @Test
    public void gUserWithRedmineFullNameOnLoginName() {
        GTask gtask = createDummyTaskForUser("Felipe Castro");
        Issue task = getConverterWithAssigneeMapped().convertToRedmineIssue(gtask);
        assertNotNull(task.getAssignee());
        assertEquals("felipe.castro", task.getAssignee().getLogin());
    }

    @Test
    public void gUserWithRedmineLoginOnDisplayName() {
        GTask gtask = createDummyTaskForUser("diogo.nascimento");

        Issue task = getConverterWithAssigneeMapped().convertToRedmineIssue(gtask);

        assertNotNull(task.getAssignee());
        assertEquals("diogo.nascimento", task.getAssignee().getLogin());
    }

    @Test
    public void gUserWithRedmineFullNameOnDisplayName() {
        GTask gtask = createDummyTaskForUser("Felipe Castro");
        Issue task = getConverterWithAssigneeMapped().convertToRedmineIssue(gtask);
        assertNotNull(task.getAssignee());
        assertEquals("felipe.castro", task.getAssignee().getLogin());
    }

    private GTask createDummyTaskForUser(String userDisplayName) {
        GTask gtask = new GTask();
        gtask.setSummary("S1");
        GUser assignee = new GUser();
        // put the resource name on displayName like in MSP Connector
        assignee.setDisplayName(userDisplayName);
        gtask.setAssignee(assignee);
        return gtask;
    }

    private GTaskToRedmine getConverterWithAssigneeMapped() {
        GTaskToRedmine converter = createConverterWithSelectedField(FIELD.ASSIGNEE, createUsers());
        return converter;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        User user1 = UserFactory.create();
        user1.setFirstName("Diogo");
        user1.setLastName("Nascimento");
        user1.setLogin("diogo.nascimento");
        users.add(user1);

        User user2 = UserFactory.create();
        user2.setFirstName("Felipe");
        user2.setLastName("Castro");
        user2.setLogin("felipe.castro");
        users.add(user2);

        return users;
    }


    @Test
    public void nullReturnedWhenNoUsersSet() {
        GTaskToRedmine converter = createDefaultConverter();
        // should not fail with NPE or anything
        assertNull(converter.findRedmineUserInCache(new GUser("mylogin")));
    }

    private GTaskToRedmine createDefaultConverter() {
        RedmineConfig config = new RedmineConfig();
        return new GTaskToRedmine(config,
                        RedmineSupportedFields.SUPPORTED_FIELDS.getSupportedFields(),
                null, project, Collections.<User>emptyList(),
                Collections.<IssueStatus>emptyList(), Collections.<Version>emptyList());
    }

    private GTaskToRedmine createConverterWithSelectedField(GTaskDescriptor.FIELD field, List<User> users) {
        return createConverterWithField(field, true, users);
    }

    private GTaskToRedmine createConverterWithUnselectedField(GTaskDescriptor.FIELD field) {
        return createConverterWithField(field, false, null);
    }

    private GTaskToRedmine createConverterWithField(GTaskDescriptor.FIELD field, boolean selected, List<User> users) {
        RedmineConfig config = new RedmineConfig();
        Collection<FIELD> selectedFields = RedmineSupportedFields.SUPPORTED_FIELDS.getSupportedFields()
                .stream()
                .filter(f -> f.equals(field) == selected)
                .collect(Collectors.toList());

//        users = users != null ? users : Collections.<User>emptyList();
        return new GTaskToRedmine(config, selectedFields, null, project, users, new ArrayList<>(), Collections.<Version>emptyList());
    }
}
