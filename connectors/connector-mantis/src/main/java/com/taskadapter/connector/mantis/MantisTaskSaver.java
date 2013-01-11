package com.taskadapter.connector.mantis;

import com.taskadapter.connector.common.AbstractTaskSaver;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.definition.exceptions.EntityProcessingException;
import com.taskadapter.mantisapi.MantisManager;
import com.taskadapter.mantisapi.RequiredItemException;
import com.taskadapter.mantisapi.beans.AccountData;
import com.taskadapter.mantisapi.beans.IssueData;
import com.taskadapter.mantisapi.beans.ProjectData;
import com.taskadapter.model.GTask;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MantisTaskSaver extends AbstractTaskSaver<MantisConfig, IssueData> {

    private final MantisManager mgr;
    private final ProjectData mntProject;
    private final MantisDataConverter converter;
	private Mappings mappings;

    public MantisTaskSaver(MantisConfig config, Mappings mappings, ProgressMonitor monitor) throws ConnectorException {
        super(config, monitor);
		this.mappings = mappings;
        this.mgr = MantisManagerFactory.createMantisManager(config.getServerInfo());
        try {
            mntProject = mgr.getProjectById(new BigInteger(config.getProjectKey()));
            final List<AccountData> users = config.isFindUserByName() ? mgr
                    .getUsers() : new ArrayList<AccountData>();
            converter = new MantisDataConverter(config, users);
        } catch (RemoteException e) {
            throw MantisUtils.convertException(e);
        } 
    }

    @Override
    protected IssueData convertToNativeTask(GTask task) {
        return converter.convertToMantisIssue(mntProject, task, mappings);
    }

    @Override
    protected GTask createTask(IssueData nativeTask) throws ConnectorException {
        try {
            BigInteger issueId = mgr.createIssue(nativeTask);
            IssueData createdIssue = mgr.getIssueById(issueId);
            return MantisDataConverter.convertToGenericTask(createdIssue);
        } catch (RemoteException e) {
            throw MantisUtils.convertException(e);
        } catch (RequiredItemException e) {
            throw new EntityProcessingException(e);
        }
    }

    @Override
    protected void updateTask(String taskId, IssueData nativeTask) throws ConnectorException {
        try {
            mgr.updateIssue(new BigInteger(taskId), nativeTask);
        } catch (RemoteException e) {
            throw MantisUtils.convertException(e);
        } 
    }
}
