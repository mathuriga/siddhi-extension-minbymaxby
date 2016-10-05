package org.wso2.extension.siddhi.window.minbymaxby.TimeWindow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class MinByTimeWindowTestCase {

    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    /**
     * Commenting out intermittent failing test case until fix this properly.
     */

    @Test
    public void minbyTimeWindowTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.minbymaxby:minbytime(price, 1 sec) select symbol,price," +
                "volume insert into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"ABC", 60.4f, 2});
        inputHandler.send(new Object[]{"IBM", 60.9f, 3});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"RRR", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 60.50f, 6});
        inputHandler.send(new Object[]{"AAA", 600.5f, 7});
        Thread.sleep(500);
        Assert.assertEquals(5, inEventCount);
        Assert.assertEquals(0,removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void minbyTimeWindowTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.minbymaxby:minbytime(price, 1 sec) select symbol,price," +
                "volume insert into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"IBM", 798f, 1});
        inputHandler.send(new Object[]{"IBM", 432f, 1});
        Thread.sleep(1100);
        Assert.assertEquals(2, inEventCount);
        Assert.assertEquals(0,removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void minbyTimeWindowTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.minbymaxby:minbytime(price, 1 sec) " +
                "select symbol, price " +
                "insert expired events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"MIT", 23.5f, 3});
        inputHandler.send(new Object[]{"GOOGLE", 45.5f, 4});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{"ORACLE", 10f, 5});
        inputHandler.send(new Object[]{"WSO2", 34.5f, 6});
        inputHandler.send(new Object[]{"GOOGLE", 65.5f, 7});
        inputHandler.send(new Object[]{"MIT", 7.5f, 8});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{"GOOGLE", 7f, 9});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 10});
        inputHandler.send(new Object[]{"MIT", 632.5f, 11});
        Thread.sleep(4000);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(0,inEventCount);
        Assert.assertEquals(3, removeEventCount);

    }
    @Test
    public void minbyTimeWindowTest4() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.minbymaxby:minbytime(price, 1 sec) select symbol,price," +
                "volume insert expired events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 798f, 2});
        inputHandler.send(new Object[]{"MIT", 432f, 3});
        Thread.sleep(1100);
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IFS", 700f, 4});
        inputHandler.send(new Object[]{"GOOGLE", 798f, 5});
        inputHandler.send(new Object[]{"YAHOO", 432f, 6});
        inputHandler.send(new Object[]{"GOOGLE", 798f, 7});
        inputHandler.send(new Object[]{"YAHOO", 432f, 8});
        Thread.sleep(1100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(0,inEventCount);
        Assert.assertEquals(2, removeEventCount);
    }

    @Test
    public void minbyTimeWindowTest6() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.minbymaxby:minbytime(price, 1 sec) select symbol,price," +
                "volume insert all events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 98f, 2});
        inputHandler.send(new Object[]{"MIT", 432f, 3});
        Thread.sleep(1100);
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IFS", 700f, 4});
        inputHandler.send(new Object[]{"GOOGLE", 798f, 5});
        inputHandler.send(new Object[]{"YAHOO", 432f, 6});
        inputHandler.send(new Object[]{"GOOGLE", 798f, 7});
        inputHandler.send(new Object[]{"YAHOO", 32f, 8});
        Thread.sleep(1100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(5, inEventCount);
        Assert.assertEquals(2, removeEventCount);
    }


}


