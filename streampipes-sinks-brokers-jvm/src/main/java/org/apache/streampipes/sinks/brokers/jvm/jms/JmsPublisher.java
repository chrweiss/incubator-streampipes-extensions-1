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

package org.apache.streampipes.sinks.brokers.jvm.jms;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.dataformat.json.JsonDataFormatDefinition;
import org.apache.streampipes.messaging.jms.ActiveMQPublisher;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventSinkRuntimeContext;
import org.apache.streampipes.wrapper.runtime.EventSink;

import java.util.Map;

public class JmsPublisher implements EventSink<JmsParameters> {

  private ActiveMQPublisher publisher;
  private JsonDataFormatDefinition jsonDataFormatDefinition;

  public JmsPublisher() {
    this.jsonDataFormatDefinition = new JsonDataFormatDefinition();
  }

  @Override
  public void onPipelineStarted(JmsParameters params, EventSinkRuntimeContext runtimeContext) throws SpRuntimeException {
    this.publisher = new ActiveMQPublisher(params.getJmsHost() + ":" + params.getJmsPort(), params.getTopic());
    if (!this.publisher.isConnected()) {
      throw new SpRuntimeException("Could not connect to JMS server " + params.getJmsHost() + " on Port: " + params.getJmsPort() + " to topic: " + params.getTopic());
    }



  }

  @Override
  public void onEvent(Event inputEvent) {
    try {
      Map<String, Object> event = inputEvent.getRaw();
      this.publisher.publish(jsonDataFormatDefinition.fromMap(event));
    } catch (SpRuntimeException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPipelineStopped() throws SpRuntimeException {
    this.publisher.disconnect();
  }
}
