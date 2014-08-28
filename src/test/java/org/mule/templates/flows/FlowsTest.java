/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.flows;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.config.MuleProperties;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.templates.transformers.WorkersAndUsersMerge;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * The objective of this class is to validate the correct behavior of the flows for this Mule Template.
 *
 * @author aurel.medvegy
 */
public class FlowsTest extends FunctionalTestCase {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Override
    protected String getConfigResources() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("./src/main/app/mule-deploy.properties"));
            return props.getProperty("config.resources");
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not find mule-deploy.properties file on classpath. Please add any of those files or override the getConfigResources() method to provide the resources by your own.");
        }
    }

    @Override
    protected Properties getStartUpProperties() {
        Properties properties = new Properties(super.getStartUpProperties());

        String pathToResource = "./mappings";
        File graphFile = new File(pathToResource);

        properties.put(MuleProperties.APP_HOME_DIRECTORY_PROPERTY, graphFile.getAbsolutePath());

        return properties;
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testAggregationFlow() throws Exception {
        List<Map<String, String>> workersFromWorkday = createUserLists("A", 0, 1);
        List<Map<String, String>> usersFromSalesforce = createUserLists("B", 1, 2);

        MuleEvent testEvent = getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE);
        testEvent.getMessage().setInvocationProperty(WorkersAndUsersMerge.WORKDAY_WORKERS, workersFromWorkday);
        testEvent.getMessage().setInvocationProperty(WorkersAndUsersMerge.SALESFORCE_USERS, usersFromSalesforce.iterator());

        SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("aggregationFlow");
        flow.initialise();
        MuleEvent event = flow.process(testEvent);

        Assert.assertTrue("The payload should not be null.", event.getMessage().getPayload() != null);
        Assert.assertFalse("The user list should not be empty.", ((List) event.getMessage().getPayload()).isEmpty());
    }

    @Test
    public void testFormatOutputFlow() throws Exception {
        List<Map<String, String>> workersFromWorkday = createUserLists("A", 0, 1);
        List<Map<String, String>> usersFromSalesforce = createUserLists("B", 1, 2);

        MuleEvent testEvent = getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE);
        testEvent.getMessage().setInvocationProperty(WorkersAndUsersMerge.WORKDAY_WORKERS, workersFromWorkday);
        testEvent.getMessage().setInvocationProperty(WorkersAndUsersMerge.SALESFORCE_USERS, usersFromSalesforce.iterator());

        SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("aggregationFlow");
        flow.initialise();
        MuleEvent event = flow.process(testEvent);

        flow = getSubFlow("formatOutputFlow");
        flow.initialise();
        event = flow.process(event);

        Assert.assertTrue("The payload should not be null.", event.getMessage().getPayload() != null);
    }

    private List<Map<String, String>> createUserLists(String orgId, int start, int end) {

        List<Map<String, String>> userList = new ArrayList<Map<String, String>>();
        for (int i = start; i <= end; i++) {
            userList.add(createUser(orgId, i));
        }
        return userList;
    }

    private Map<String, String> createUser(String orgId, int sequence) {
        Map<String, String> user = new HashMap<String, String>();

        user.put("Id", new Integer(sequence).toString());
        user.put("Username", "username_" + sequence + "_" + orgId);
        user.put("Name", "SomeName_" + sequence);
        user.put("Email", "some.email." + sequence + "@fakemail.com");

        return user;
    }

}
