package com.taskadapter.connector.msp.editor;

import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.msp.MSPConfig;
import com.taskadapter.connector.msp.editor.error.InputFileNameNotSetException;
import com.taskadapter.connector.testlib.FileBasedTest;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.WindowProvider;
import com.taskadapter.web.service.EditableCurrentUserInfo;
import com.taskadapter.web.service.EditorManager;
import com.taskadapter.web.service.Services;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class MSPEditorFactoryTest extends FileBasedTest {
    @Test(expected = InputFileNameNotSetException.class)
    public void noInputFileNameFailsValidation() throws BadConfigException {
        MSPConfig config = new MSPConfig();
        new MSPEditorFactory().validateForLoad(config);
    }

    @Test
    public void miniPanelInstanceIsCreated() {
        Services services = new Services(tempFolder, new EditorManager(Collections.<String, PluginEditorFactory<?>>emptyMap()));
        ((EditableCurrentUserInfo) services.getCurrentUserInfo()).setUserName("admin");
        MSPEditorFactory factory = new MSPEditorFactory();
        WindowProvider provider = mock(WindowProvider.class);
        factory.getMiniPanelContents(provider, services, new MSPConfig());
    }

}
