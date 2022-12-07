package com.xpansiv.wf.user_management;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.filter.v1.WorkflowExecutionFilter;
import io.temporal.api.workflowservice.v1.ListClosedWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListClosedWorkflowExecutionsResponse;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WFSubscriptionManagementTest {

    static Logger logger = LoggerFactory.getLogger(WFSubscriptionManagementTest.class);
    @Rule
    public TestWatcher watchman =
            new TestWatcher() {
                @Override
                protected void failed(Throwable e, Description description) {
                    if (testEnv != null) {
                        System.err.println(testEnv.getDiagnostics());
                        testEnv.close();
                    }
                }
            };

    private TestWorkflowEnvironment testEnv;
    private Worker wfWorker;
    private Worker notificationWorker;

    private ServiceManagementActivityTestImpl notificationActivityTest = new ServiceManagementActivityTestImpl();
     WorkflowClient wfClient;
    private WorkflowServiceStubs wfService;
    private String wfNamespace;


    @Before
    public void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        wfWorker = testEnv.newWorker(WFSubscriptionManagement.WF_TASKS_QUEUE);
        wfWorker.registerWorkflowImplementationTypes(WFSubscriptionManagementImpl.class);
        notificationWorker = testEnv.newWorker("SVC_MANAGEMENT");
        notificationWorker.registerActivitiesImplementations(notificationActivityTest);
        testEnv.start();
        wfNamespace = "default";
        wfService = testEnv.getWorkflowServiceStubs();
        wfClient = testEnv.getWorkflowClient();
    }

    @After
    public void tearDown() {
        testEnv.close();
    }

    @Test
    public void testManyDaysWorkflowAutoCancellation(){

        Map<String, Object> searchAttributes = createSA( "" + System.nanoTime());
        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
                .setTaskQueue(WFSubscriptionManagement.WF_TASKS_QUEUE)
                .setSearchAttributes(searchAttributes)
                .setMemo(searchAttributes)
                .build();

        WFSubscriptionManagement workflow = wfClient.newWorkflowStub(WFSubscriptionManagement.class, workflowOptions);
        SubscriptionRequest subscription = new SubscriptionRequest().setProduct("Super Service").setUserId("u-a1");
        WorkflowExecution wfExecution = WorkflowClient.start(workflow::subscribeForService, subscription, null);
        String wfID = wfExecution.getWorkflowId();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub svc = wfService.blockingStub();
        testEnv.sleep(Duration.ofDays(6));
        ListClosedWorkflowExecutionsResponse closedWorkflowsList = svc.listClosedWorkflowExecutions( ListClosedWorkflowExecutionsRequest.newBuilder()
                .setNamespace( wfNamespace)
                .setExecutionFilter( WorkflowExecutionFilter.newBuilder().setWorkflowId( wfID).build())
                .build());
        assertEquals( "we should not have finished workflows yet", 0,closedWorkflowsList.getExecutionsCount());
        testEnv.sleep(Duration.ofDays(9));

        Awaitility.await("workflow "+wfID+" finished").atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).until(() -> {
            try {
                ListClosedWorkflowExecutionsResponse closedWorkflows = svc.listClosedWorkflowExecutions( ListClosedWorkflowExecutionsRequest.newBuilder()
                        .setNamespace( wfNamespace)
                        .setExecutionFilter( WorkflowExecutionFilter.newBuilder().setWorkflowId( wfID).build())
                        .build());
                System.out.println("# closed workflows = " + closedWorkflows.getExecutionsCount());
                return closedWorkflows.getExecutionsCount()==1 ;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        assertEquals( 1, notificationActivityTest.receivedNotifications.size());

    }

    private Map<String, Object> createSA( String correlationId) {
        HashMap<String,Object> res = new HashMap<>();
        res.put("CustomTextField", correlationId);
        return res;
    }

}
