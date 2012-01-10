package com.taskadapter.connector.redmine;

import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.connector.definition.ValidationException;
import com.taskadapter.connector.definition.WebConfig;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.taskadapter.web.configeditor.EditorUtil;
import com.taskadapter.web.configeditor.LookupOperation;
import com.taskadapter.web.configeditor.ProjectProcessor;
import org.redmine.ta.RedmineManager;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexey Skorokhodov
 */
public class RedmineProjectProcessor implements ProjectProcessor {

	private final RedmineEditor editor;

	public RedmineProjectProcessor(RedmineEditor editor) {
		this.editor = editor;
	}

	@Override
	public void loadProject(String projectKey) {
		// validate();
		try {
			WebConfig webConfig = (WebConfig) editor.getConfig();
			if (!webConfig.getServerInfo().isHostSet()) {
				throw new ValidationException("Host URL is not set");
			}

			RedmineManager mgr = editor.getRedmineManager();
			LoadProjectJob job = new LoadProjectJob(editor, mgr, projectKey);
            // TODO implement this
            System.out.println("execute the JOB here");
		} catch (ValidationException e) {
			EditorUtil.show(editor.getWindow(), "Can't load project", e);
		}
	}

	@Override
	public Descriptor getDescriptor() {
		return RedmineDescriptor.instance;
	}

	@Override
	public LookupOperation getLoadSavedQueriesOperation(ConfigEditor editor) {
		return new LoadSavedQueriesOperation(editor, RedmineDescriptor.instance);
	}

	@Override
	public Collection<EditorFeature> getSupportedFeatures() {
		return Arrays.asList(EditorFeature.LOAD_PROJECTS, EditorFeature.LOAD_PROJECT_INFO, EditorFeature.LOAD_SAVED_QUERIES);
	}

}