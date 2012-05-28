package com.taskadapter.connector.msp;

import com.taskadapter.connector.definition.AvailableFields;
import com.taskadapter.connector.definition.AvailableFieldsBuilder;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.model.GTaskDescriptor.FIELD;

import java.util.Arrays;
import java.util.Collection;

public class MSPDescriptor implements Descriptor {
    private static final String INFO = "Microsoft Project connector. Supports MPP and XML files (also known as MSPDI)";

    /**
     * Keep it the same to enable backward compatibility
     */
    public static final String ID = "Microsoft Project";

    /**
     * Supported fields.
     */
    private static final AvailableFields SUPPORTED_FIELDS;
    
    static {
    	final AvailableFieldsBuilder builder = AvailableFieldsBuilder.start();
    	builder.addField(FIELD.SUMMARY, "Task Name");
    	builder.addField(FIELD.DESCRIPTION, "Notes");
    	builder.addField(FIELD.TASK_TYPE, MSPUtils.getAllTextFieldNames());
    	builder.addField(FIELD.ESTIMATED_TIME, MSPUtils.getEstimatedTimeOptions());
    	builder.addField(FIELD.DONE_RATIO, "Percent complete");
    	builder.addField(FIELD.ASSIGNEE, "Resource Name");
    	builder.addField(FIELD.DUE_DATE, MSPUtils.getDueDateOptions());
    	builder.addField(FIELD.START_DATE, MSPUtils.getStartDateOptions());
    	builder.addField(FIELD.REMOTE_ID, MSPUtils.getAllTextFieldNames());
    	builder.addField(FIELD.TASK_STATUS, MSPUtils.getAllTextFieldNames());
    	SUPPORTED_FIELDS = builder.end();
    }

    public static final Descriptor instance = new MSPDescriptor();

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
    public AvailableFields getAvailableFields() {
        return SUPPORTED_FIELDS;
    }

    @Override
    public Collection<Feature> getSupportedFeatures() {
        return Arrays.asList(Feature.LOAD_TASK, Feature.SAVE_TASK);
    }
}
