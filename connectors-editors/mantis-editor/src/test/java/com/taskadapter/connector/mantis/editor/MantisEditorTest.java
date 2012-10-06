package com.taskadapter.connector.mantis.editor;

import com.taskadapter.connector.mantis.MantisConfig;
import com.taskadapter.web.service.Services;
import org.junit.Test;

import java.io.File;

public class MantisEditorTest {
    @Test
    public void editorInstanceIsCreated() {
        new MantisEditor(new MantisConfig(), new Services(new File("tmp")));
    }
}