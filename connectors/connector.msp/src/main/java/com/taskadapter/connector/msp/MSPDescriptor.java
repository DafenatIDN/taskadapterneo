package com.taskadapter.connector.msp;

import com.taskadapter.connector.common.PriorityLoader;
import com.taskadapter.connector.common.ProjectLoader;
import com.taskadapter.connector.common.TaskLoader;
import com.taskadapter.connector.common.TaskSaver;
import com.taskadapter.connector.definition.AvailableFieldsProvider;
import com.taskadapter.connector.definition.Connector;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Descriptor;

import java.util.Arrays;
import java.util.Collection;

public class MSPDescriptor implements Descriptor {
	private static final String INFO = "Microsoft Project connector. Supports MPP and XML files (also known as MSPDI)";

	/**
	 * Keep it the same to enable backward compatibility
	 */
	public static final String ID = "Microsoft Project";

	public static final Descriptor instance = new MSPDescriptor();

	public MSPDescriptor() {
	}
	
	@Override
	public String getDescription() {
		return INFO;
	}

	@Override
	public MSPConfig createDefaultConfig() {
		return new MSPConfig();
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String toString() {
		return getID();
	}

	@Override
	public String getLabel() {
		return MSPConfig.DEFAULT_LABEL;
	}

	@Override
	public Class<MSPConfig> getConfigClass() {
		return MSPConfig.class;
	}

	@Override
	public AvailableFieldsProvider getAvailableFieldsProvider() {
		return new MSPAvailableFieldsProvider();
	}

	@Override
	public Connector<MSPConfig> createConnector(ConnectorConfig config) {
		return new MSPConnector((MSPConfig) config);
	}

	@Override
	public ProjectLoader getProjectLoader() {
		throw new RuntimeException("Operation is not implemented for MSP");
	}

	@Override
	public TaskLoader<MSPConfig> getTaskLoader() {
		return new MSPTaskLoader();
	}

	@Override
	public TaskSaver<MSPConfig> getTaskSaver(ConnectorConfig config) {
		return new MSPTaskSaver((MSPConfig) config);
	}

	@Override
	public Collection<Feature> getSupportedFeatures() {
		return Arrays.asList(Feature.LOAD_TASK, Feature.SAVE_TASK);
	}
	
	@Override
	public PriorityLoader getPriorityLoader() {
		throw new RuntimeException("NOT READY");
	}
}