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
package org.apache.camel.component.kubernetes.consumer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kubernetes.KubernetesConstants;
import org.apache.camel.component.kubernetes.KubernetesEndpoint;
import org.apache.camel.component.kubernetes.consumer.common.ServiceEvent;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesServicesConsumer extends ScheduledPollConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesServicesConsumer.class);

    private ConcurrentMap<Long, ServiceEvent> map;

    public KubernetesServicesConsumer(KubernetesEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public KubernetesEndpoint getEndpoint() {
        return (KubernetesEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        map = new ConcurrentHashMap<Long, ServiceEvent>();

        if (ObjectHelper.isNotEmpty(getEndpoint().getKubernetesConfiguration().getOauthToken())) {
            if (ObjectHelper.isNotEmpty(getEndpoint().getKubernetesConfiguration().getNamespaceName())) {
                getEndpoint().getKubernetesClient().services()
                        .inNamespace(getEndpoint().getKubernetesConfiguration().getNamespaceName())
                        .watch(new Watcher<Service>() {

                            @Override
                            public void eventReceived(io.fabric8.kubernetes.client.Watcher.Action action,
                                    Service resource) {
                                ServiceEvent se = new ServiceEvent(action, resource);
                                map.put(System.currentTimeMillis(), se);

                            }

                            @Override
                            public void onClose(KubernetesClientException cause) {
                                if (cause != null) {
                                    LOG.error(cause.getMessage(), cause);
                                }
                            }

                        });
            } else {
                getEndpoint().getKubernetesClient().services().watch(new Watcher<Service>() {

                    @Override
                    public void eventReceived(io.fabric8.kubernetes.client.Watcher.Action action, Service resource) {
                        ServiceEvent se = new ServiceEvent(action, resource);
                        map.put(System.currentTimeMillis(), se);

                    }

                    @Override
                    public void onClose(KubernetesClientException cause) {
                        if (cause != null) {
                            LOG.error(cause.getMessage(), cause);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        map.clear();
    }

    @Override
    protected int poll() throws Exception {
        int mapSize = map.size();
        for (ConcurrentMap.Entry<Long, ServiceEvent> entry : map.entrySet()) {
            ServiceEvent serviceEvent = (ServiceEvent) entry.getValue();
            Exchange e = getEndpoint().createExchange();
            e.getIn().setBody(serviceEvent.getService());
            e.getIn().setHeader(KubernetesConstants.KUBERNETES_EVENT_ACTION, serviceEvent.getAction());
            e.getIn().setHeader(KubernetesConstants.KUBERNETES_EVENT_TIMESTAMP, entry.getKey());
            getProcessor().process(e);
            map.remove(entry.getKey());
        }
        return mapSize;
    }

}
