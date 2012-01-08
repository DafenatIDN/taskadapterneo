package com.taskadapter.connector.github;

import com.taskadapter.connector.common.AbstractConnector;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.SyncResult;

public class GithubConnector extends AbstractConnector<GithubConfig> {

    public Descriptor getDescriptor() {
        return GithubDescriptor.instance;
    }

	public GithubConnector(GithubConfig config) {
		super(config);
	}

    public void updateRemoteIDs(ConnectorConfig sourceConfig, SyncResult actualSaveResult, ProgressMonitor monitor) {
		throw new RuntimeException("not implemented for this connector");
    }

}
