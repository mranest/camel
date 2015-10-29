/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.kubernetes.consumer.common;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Watcher.Action;

public class ServiceEvent {
    private io.fabric8.kubernetes.client.Watcher.Action action;

    private Service service;

    public ServiceEvent(Action action, Service service) {
        super();
        this.action = action;
        this.service = service;
    }

    public io.fabric8.kubernetes.client.Watcher.Action getAction() {
        return action;
    }

    public void setAction(io.fabric8.kubernetes.client.Watcher.Action action) {
        this.action = action;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
