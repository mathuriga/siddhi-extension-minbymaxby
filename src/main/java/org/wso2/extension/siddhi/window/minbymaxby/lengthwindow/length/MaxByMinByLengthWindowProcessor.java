/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.window.minbymaxby.lengthwindow.length;

import org.wso2.extension.siddhi.window.minbymaxby.MaxByMinByConstants;
import org.wso2.extension.siddhi.window.minbymaxby.MaxByMinByExecutor;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.wso2.siddhi.core.util.collection.operator.Finder;
//import org.wso2.siddhi.core.util.parser.CollectionOperatorParser;

/**
 * Abstract class which gives the min/max event in a Length window
 */
public abstract class MaxByMinByLengthWindowProcessor extends WindowProcessor implements FindableProcessor {
    private ExpressionExecutor minByMaxByExecutorAttribute;
    protected String minByMaxByExecutorType;
    protected String minByMaxByExtensionType;
    private MaxByMinByExecutor maxByMinByExecutor;
    private int length;
    private int count = 0;
    private ComplexEventChunk<StreamEvent> internalWindowChunk = null;
    private ComplexEventChunk<StreamEvent> outputStreamEventChunk = new ComplexEventChunk<StreamEvent>(true);
    private ExecutionPlanContext executionPlanContext;
    private StreamEvent outputStreamEvent;
    private List<StreamEvent> events = new ArrayList<StreamEvent>();
    StreamEvent toBeExpiredEvent = null;


    public void setOutputStreamEvent(StreamEvent outputSreamEvent) {
        this.outputStreamEvent = outputSreamEvent;
    }


    @Override
    protected void init(ExpressionExecutor[] expressionExecutors, ExecutionPlanContext executionPlanContext) {

        this.executionPlanContext = executionPlanContext;
        this.internalWindowChunk = new ComplexEventChunk<StreamEvent>(false);
        maxByMinByExecutor = new MaxByMinByExecutor();
        //this.events=new ArrayList<StreamEvent>();
        if (minByMaxByExecutorType.equals(MaxByMinByConstants.MIN_BY)) {
            maxByMinByExecutor.setMinByMaxByExecutorType(minByMaxByExecutorType);
        } else {
            maxByMinByExecutor.setMinByMaxByExecutorType(minByMaxByExecutorType);
        }

        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to minbymaxby:" + minByMaxByExecutorType + " window, " +
                    "required 2, but found " + attributeExpressionExecutors.length);
        }

        Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();


        if (!((attributeType == Attribute.Type.DOUBLE)
                || (attributeType == Attribute.Type.INT)
                || (attributeType == Attribute.Type.STRING)
                || (attributeType == Attribute.Type.FLOAT)
                || (attributeType == Attribute.Type.LONG))) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of minbymaxby:" + minByMaxByExecutorType + " window, " +
                    "required " + Attribute.Type.INT + " or " + Attribute.Type.LONG +
                    " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.DOUBLE + "or" + Attribute.Type.STRING +
                    ", but found " + attributeType.toString());
        }
        attributeType = attributeExpressionExecutors[1].getReturnType();
        if (!((attributeType == Attribute.Type.LONG)
                || (attributeType == Attribute.Type.INT))) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of minbymaxby:" + minByMaxByExecutorType + " window, " +
                    "required " + Attribute.Type.INT + " or " + Attribute.Type.LONG +
                    ", but found " + attributeType.toString());
        }

        if (attributeExpressionExecutors.length == 2) {
            minByMaxByExecutorAttribute = attributeExpressionExecutors[0];
            length = (Integer) (((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue());

        }

    }


    @Override
    protected void process(ComplexEventChunk<StreamEvent> complexEventChunk, Processor processor, StreamEventCloner streamEventCloner) {
        List<ComplexEventChunk<StreamEvent>> streamEventChunks = new ArrayList<ComplexEventChunk<StreamEvent>>();
        synchronized (this) {
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
            while (complexEventChunk.hasNext()) {
                StreamEvent streamEvent = complexEventChunk.next();
                StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent); //this cloned event used to
                // insert the event into treemap

                if (count != 0) {
                    outputStreamEventChunk.clear();
                    internalWindowChunk.clear();
                }

                //get the parameter value for every events
                Object parameterValue = getParameterValue(minByMaxByExecutorAttribute, streamEvent);

                //insert the cloned event into treemap
                maxByMinByExecutor.insert(clonedStreamEvent, parameterValue);

                if (count < length) {
                    count++;

                    //get the output event
                    setOutputStreamEvent(maxByMinByExecutor.getResult(maxByMinByExecutor.getMinByMaxByExecutorType()));

                    if (toBeExpiredEvent != null) {
                        if (outputStreamEvent != toBeExpiredEvent) {
                            toBeExpiredEvent.setTimestamp(currentTime);
                            toBeExpiredEvent.setType(StateEvent.Type.EXPIRED);
                            outputStreamEventChunk.add(toBeExpiredEvent);
                        }
                    }

                    outputStreamEventChunk.add(outputStreamEvent);
                    //add the event which is to be expired
                    internalWindowChunk.add(streamEventCloner.copyStreamEvent(outputStreamEvent));
                    toBeExpiredEvent = outputStreamEvent;

                    //System.out.println(outputStreamEventChunk);
                    if (outputStreamEventChunk.getFirst() != null) {
                        streamEventChunks.add(outputStreamEventChunk);
                    }

                    events.add(clonedStreamEvent);
                } else {
                    StreamEvent firstEvent = events.get(0);
                    if (firstEvent != null) {
                        firstEvent.setTimestamp(currentTime);

                        //remove the expired event from treemap
                        Object expiredEventParameterValue = getParameterValue(minByMaxByExecutorAttribute, firstEvent);

                        maxByMinByExecutor.getSortedEventMap().remove(expiredEventParameterValue);
                        events.remove(0);

                        //get the output event
                        setOutputStreamEvent(maxByMinByExecutor.getResult(maxByMinByExecutor.getMinByMaxByExecutorType()));
                        if (toBeExpiredEvent != null) {
                            if (outputStreamEvent != toBeExpiredEvent) {
                                toBeExpiredEvent.setTimestamp(currentTime);
                                toBeExpiredEvent.setType(StateEvent.Type.EXPIRED);
                                outputStreamEventChunk.add(toBeExpiredEvent);
                            }

                        }
                        outputStreamEventChunk.add(outputStreamEvent);
                        internalWindowChunk.add(streamEventCloner.copyStreamEvent(outputStreamEvent));
                        toBeExpiredEvent = outputStreamEvent;

//                        System.out.println(outputStreamEventChunk);
                        if (outputStreamEventChunk.getFirst() != null) {
                            streamEventChunks.add(outputStreamEventChunk);
                        }
                        events.add(clonedStreamEvent);

                    }
                }
            }
        }

        System.out.println(outputStreamEventChunk);
        for (ComplexEventChunk<StreamEvent> outputStreamEventChunk : streamEventChunks) {
            nextProcessor.process(outputStreamEventChunk);
        }

    }

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }

    @Override
    public Object[] currentState() {
        return new Object[]{new AbstractMap.SimpleEntry<String, Object>("ExpiredEvent", toBeExpiredEvent), new AbstractMap.SimpleEntry<String, Object>("Count", count)};
    }

    @Override
    public void restoreState(Object[] state) {
        toBeExpiredEvent = null;
        Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
        toBeExpiredEvent = (StreamEvent) stateEntry.getValue();
        Map.Entry<String, Object> stateEntry2 = (Map.Entry<String, Object>) state[1];
        count = (Integer) stateEntry2.getValue();
    }


    /**
     * To find the parameter value of given parameter for each event .
     *
     * @param functionParameter name of the parameter of the event data
     * @param streamEvent       event  at processor
     * @return the parameterValue
     */

    public Object getParameterValue(ExpressionExecutor functionParameter, StreamEvent streamEvent) {
        Object parameterValue;
        parameterValue = functionParameter.execute(streamEvent);
        return parameterValue;
    }


    public synchronized StreamEvent find(StateEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, this.internalWindowChunk, this.streamEventCloner);
    }

    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        return OperatorParser.constructOperator(this.internalWindowChunk, expression, matchingMetaStateHolder, executionPlanContext, variableExpressionExecutors, eventTableMap);
    }
}