package com.taskadapter.connector.redmine.editor;

import com.taskadapter.connector.redmine.RedmineConfig;
import com.taskadapter.web.service.Services;
import org.junit.Test;

public class RedmineEditorTest {
    @Test
    public void editorInstanceIsCreated() {
        new RedmineEditor(new RedmineConfig(), new Services());
    }
}
