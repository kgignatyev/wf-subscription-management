package com.xpansiv.wf.user_management;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;

import static com.xpansiv.wf.user_management.ServiceManagementActivity.QUEUE_SERVICE_MANAGEMENT;

public class ContinuityDemoRunner {

    public static final String QUEUE_WS_TASKS = "WF_TASKS";

    public static void main(String[] args) {
        try {
            WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                    WorkflowServiceStubsOptions.newBuilder()
                            .setTarget("localhost:7233").build()
            );
            WorkflowClient client = WorkflowClient.newInstance(service, WorkflowClientOptions.newBuilder()
                    .setNamespace("default").build());
            WorkerFactory factory = WorkerFactory.newInstance(client);
            factory.newWorker(QUEUE_WS_TASKS).registerWorkflowImplementationTypes(WFSubscriptionManagementImpl.class);
            factory.newWorker(QUEUE_SERVICE_MANAGEMENT).registerActivitiesImplementations(new ServiceManagementActivityTestImpl());
            factory.start();
            //let's start workflow that uses continuation
            WFSubscriptionManagement wfStub = client.newWorkflowStub(WFSubscriptionManagement.class,
                    WorkflowOptions.newBuilder()
                            .setTaskQueue(QUEUE_WS_TASKS)
                            .build()
            );
            WorkflowExecution wfExecution = WorkflowClient.start(wfStub::subscribeForService,
                    new SubscriptionRequest().setProduct("A").setUserId("user1"),
                    new PaymentInfo().setAmountCents(1).setCreditCardNumber("123456"));
            System.out.println("started wfExecution = " + wfExecution.getWorkflowId());

            Thread.sleep(55000); //we should see 3 instances of the workflow with different run IDs
            wfStub.cancel();
            Thread.sleep(5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
