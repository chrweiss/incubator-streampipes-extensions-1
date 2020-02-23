/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.streampipes.processors.transformation.jvm.processor.task;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.runtime.EventProcessor;

public class TaskDuration implements EventProcessor<TaskDurationParameters> {

  private String taskFieldSelector;
  private String timestampFieldSelector;

  private String lastValue;
  private Long lastTimestamp;
  private Double outputDivisor;

  @Override
  public void onPipelineStarted(TaskDurationParameters parameters, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext runtimeContext) throws SpRuntimeException {
    this.taskFieldSelector = parameters.getTaskFieldSelector();
    this.timestampFieldSelector = parameters.getTimestampFieldSelector();
    this.outputDivisor = parameters.getOutputDivisor();
  }

  @Override
  public void onEvent(Event event, SpOutputCollector collector) throws SpRuntimeException {
    String taskValue = event.getFieldBySelector(taskFieldSelector).getAsPrimitive().getAsString();
    Long timestampValue =
            event.getFieldBySelector(timestampFieldSelector).getAsPrimitive().getAsLong();

    if (lastValue == null) {
      this.lastValue = taskValue;
      this.lastTimestamp = timestampValue;
    } else {
      if (!this.lastValue.equals(taskValue)) {
        Long duration = timestampValue - this.lastTimestamp;


        double result = duration / this.outputDivisor;

        Event outEvent = new Event();
        outEvent.addField("processId", makeProcessId(taskValue));
        outEvent.addField("duration", result);

        this.lastValue = taskValue;
        this.lastTimestamp = timestampValue;

        collector.collect(outEvent);
      }
    }
  }

  private String makeProcessId(String taskValue) {
    return this.lastValue + "-" + taskValue;
  }

  @Override
  public void onPipelineStopped() throws SpRuntimeException {
    this.lastValue = null;
  }
}
