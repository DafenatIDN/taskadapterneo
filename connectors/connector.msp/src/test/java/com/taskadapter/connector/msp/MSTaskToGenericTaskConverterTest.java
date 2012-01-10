package com.taskadapter.connector.msp;

import com.taskadapter.connector.common.TestUtils;
import com.taskadapter.connector.definition.Mapping;
import com.taskadapter.model.GTaskDescriptor.FIELD;
import com.taskadapter.model.GUser;
import junit.framework.Assert;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.TaskField;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class MSTaskToGenericTaskConverterTest {

    private MSTaskToGenericTaskConverter converter = new MSTaskToGenericTaskConverter();
    private ProjectFile projectFile =  MSPTestUtils.readTestProjectFile();

    private static final String FEATURE = "new feature";
    private static final String DEV_TASK = "dev task";
    private static final String BUG = "bug";

    private static final String REMOTE_ID1 = "remoteId-1";
    private static final String REMOTE_ID2 = "remoteId-2";
    private static final String REMOTE_ID3 = "remoteId-3";

    private Task task1;
    private Task task2;
    private Task task3;

    {
    	MSPConfig config = new MSPConfig();
    	converter.setConfig(config);
        converter.setHeader(projectFile.getProjectHeader());
        List<Task> allTasks = projectFile.getAllTasks();
        task1 = allTasks.get(2);
        task2 = allTasks.get(3);
        task3 = allTasks.get(4);
    }


    @Test
    public void extractAssignee1(){
    	GUser assignee = converter.extractAssignee(task1);
        Assert.assertEquals("Assignee ID must be NULL because this file was created by an old Task Adapter version", 
        		null, assignee.getId());
        Assert.assertEquals("wax", converter.extractAssignee(task1).getDisplayName());
    }

    @Test
    public void extractAssignee2(){
    	GUser assignee = converter.extractAssignee(task1);
        Assert.assertEquals("Assignee ID must be NULL because this file was created by an old Task Adapter version", 
        		null, assignee.getId());
        Assert.assertEquals("Alex", converter.extractAssignee(task2).getDisplayName());
    }

    @Test
    public void extractAssignee3(){
    	GUser assignee = converter.extractAssignee(task1);
        Assert.assertEquals("Assignee ID must be NULL because this file was created by an old Task Adapter version", 
        		null, assignee.getId());
        Assert.assertEquals("im", converter.extractAssignee(task3).getDisplayName());
    }


    @Test
    public void extractEstimatesHours1(){
    	Map<FIELD, Mapping> fieldMapped = TestUtils.getFieldMapped(FIELD.ESTIMATED_TIME, true, TaskField.WORK.toString());
    	MSPConfig config = new MSPConfig("", fieldMapped);
	    converter.setConfig(config);
        Assert.assertEquals(2.0f, converter.extractEstimatedHours(task1), 0.001f);
    }

    @Test
    public void extractEstimatesHours2(){
    	Map<FIELD, Mapping> fieldMapped = TestUtils.getFieldMapped(FIELD.ESTIMATED_TIME, true, TaskField.DURATION.toString());
    	MSPConfig config = new MSPConfig("", fieldMapped);
	    converter.setConfig(config);
        Assert.assertEquals(0.5f, converter.extractEstimatedHours(task2), 0.001f);
    }


    @Test
    public void extractEstimatesHours3(){
    	Map<FIELD, Mapping> fieldMapped = TestUtils.getFieldMapped(FIELD.ESTIMATED_TIME, true, TaskField.WORK.toString());
    	MSPConfig config = new MSPConfig("", fieldMapped);
	    converter.setConfig(config);
        Assert.assertEquals(8.0f, converter.extractEstimatedHours(task3), 0.001f);
    }


   @Test
   public void extractRemoteId(){

       Assert.assertEquals(REMOTE_ID1, converter.extractRemoteId(task1));
       Assert.assertEquals(REMOTE_ID2, converter.extractRemoteId(task2));
       Assert.assertEquals(REMOTE_ID3, converter.extractRemoteId(task3));
   }

    @Test
    public void extractType(){
        Assert.assertEquals(FEATURE, converter.extractType(task1));
        Assert.assertEquals(DEV_TASK, converter.extractType(task2));
        Assert.assertEquals(BUG, converter.extractType(task3));
    }
}