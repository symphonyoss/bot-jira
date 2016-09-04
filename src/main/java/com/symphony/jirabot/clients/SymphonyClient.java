/*
 *
 *
 * Copyright 2016 Symphony Communication Services, LLC
 *
 * Licensed to Symphony Communication Services, LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.symphony.jirabot.clients;

import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.jirabot.configurations.IConfigurationProvider;
import com.symphony.jirabot.formatters.MessageML;
import com.symphony.jirabot.configurations.SymphonyClientConfiguration;
import com.symphony.api.agent.api.MessagesApi;
import com.symphony.api.agent.model.V2Message;
import com.symphony.api.auth.api.AuthenticationApi;
import com.symphony.api.auth.model.Token;
import com.symphony.api.pod.api.PresenceApi;
import com.symphony.api.pod.api.RoomMembershipApi;
import com.symphony.api.pod.api.StreamsApi;
import com.symphony.api.pod.api.UsersApi;
import com.symphony.api.pod.model.RoomSearchCriteria;
import com.symphony.api.pod.model.RoomSearchResults;
import com.symphony.api.pod.model.V2RoomDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Interacts with the Symphony platform API
 *
 * TODO add single reauthentication attempt on unauthorized API call
 *
 * Created by ryan.dsouza on 5/26/16.
 */

public class SymphonyClient implements ISymphonyClient {

    private final Logger LOG = LoggerFactory.getLogger(SymphonyClient.class);

    private final SymphonyClientConfiguration clientConfiguration;

    private final AuthenticationApi sbeApi;
    private final AuthenticationApi keyManagerApi;
    private final MessagesApi messagesApi;
    private final PresenceApi presenceApi;
    private final UsersApi usersApi;
    private final StreamsApi streamsApi;
    private final RoomMembershipApi roomMembershipApi;

    private Token sessionToken;
    private Token keyManagerToken;

    public SymphonyClient(IConfigurationProvider configurationProvider) {
        this.clientConfiguration = new SymphonyClientConfiguration(configurationProvider);

        com.symphony.api.agent.client.ApiClient agentClient = new com.symphony.api.agent.client.ApiClient();
        com.symphony.api.auth.client.ApiClient keyManagerClient = new com.symphony.api.auth.client.ApiClient();
        com.symphony.api.pod.client.ApiClient podApiClient = new com.symphony.api.pod.client.ApiClient();
        com.symphony.api.auth.client.ApiClient sbeClient = new com.symphony.api.auth.client.ApiClient();

        podApiClient.setBasePath(clientConfiguration.getPodBasePath());
        agentClient.setBasePath(clientConfiguration.getAgentBasePath());
        sbeClient.setBasePath(clientConfiguration.getSBEBasePath());
        keyManagerClient.setBasePath(clientConfiguration.getKeyManagerBasePath());

        this.messagesApi = new MessagesApi(agentClient);
        this.keyManagerApi = new AuthenticationApi(keyManagerClient);
        this.sbeApi = new AuthenticationApi(sbeClient);

        this.usersApi = new UsersApi(podApiClient);
        this.presenceApi = new PresenceApi(podApiClient);
        this.streamsApi = new StreamsApi(podApiClient);
        this.roomMembershipApi = new RoomMembershipApi(podApiClient);

        File certificate = clientConfiguration.getCertificateFile();
        System.setProperty("javax.net.ssl.keyStore", certificate.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", clientConfiguration.getKeystorePassword());
        System.setProperty("javax.net.ssl.keyStoreType", clientConfiguration.getKeystoreType());
    }

    /**
     * Authenticates the com.symphony.jirabot.clients.SymphonyClient
     */
    public void authenticate() {

        try {
            Token sessionToken = sbeApi.v1AuthenticatePost();
            if(sessionToken.getToken() != null && sessionToken.getToken().length() != 0) {
                this.sessionToken = sessionToken;
                Token keyManagerToken = keyManagerApi.v1AuthenticatePost();

                if(keyManagerToken.getToken() != null && keyManagerToken.getToken().length() != 0) {
                    this.keyManagerToken = keyManagerToken;
                    LOG.debug("successfully authenticated symphony client");
                    return;
                }
            }
        } catch(com.symphony.api.auth.client.ApiException e) {
            throw new RuntimeException("failed to authenticate symphony client", e);
        }
        throw new RuntimeException("failed to authenticate symphony client");
    }

    /**
     * Returns the Symphony Chat Room associated with the query
     * @param query
     * @return Symphony Chat Room
     */
    public V2RoomDetail getRoomForSearchQuery(String query) {

        RoomSearchCriteria searchCriteria = new RoomSearchCriteria();
        searchCriteria.setQuery(query);

        //String labels = prompt("Enter room labels to query (comma-separated):", "customer,season");
        //searchCriteria.setLabels(Arrays.asList(labels.split(",")));

        try {
            RoomSearchResults results =
                streamsApi.v2RoomSearchPost(this.sessionToken.getToken(), searchCriteria, 0, 100);

            //TODO: Exact name match
            if(results.getCount() > 0) {
                return results.getRooms().get(0);
            }
        } catch(com.symphony.api.pod.client.ApiException e) {
            throw new RuntimeException("failed while searching for room with query " + query, e);
        }
        throw new RuntimeException("no rooms found for query " + query);
    }

    public V2Message sendMessage(String roomID, MessageML messageML) {
        V2MessageSubmission messageSubmission = new V2MessageSubmission();
        messageSubmission.setFormat(V2MessageSubmission.FormatEnum.MESSAGEML);
        messageSubmission.setMessage(messageML.toString());

        V2Message response = this.sendMessage(roomID, messageSubmission);
        return response;
    }

    public V2Message sendMessage(String roomID, String text) {
        V2MessageSubmission messageSubmission = new V2MessageSubmission();
        messageSubmission.setFormat(V2MessageSubmission.FormatEnum.TEXT);
        messageSubmission.setMessage(text);

        V2Message response = this.sendMessage(roomID, messageSubmission);
        return response;
    }

    public V2Message sendMessage(V2RoomDetail roomDetail, MessageML messageML) {
        String roomID = roomDetail.getRoomSystemInfo().getId();
        return this.sendMessage(roomID, messageML);
    }

    private V2Message sendMessage(String roomID, V2MessageSubmission message) {

        if(message.getMessage().replaceAll(" ", "").length() == 0) {
            return null;
        }

        try {
            V2Message result = messagesApi.v2StreamSidMessageCreatePost(roomID,
                    sessionToken.getToken(), keyManagerToken.getToken(), message);

            if(result != null && result.getId() != null) {
                LOG.debug("successfully sent message: " + message);
                return result;
            }
        } catch(com.symphony.api.agent.client.ApiException e) {
            throw new RuntimeException("failed while sending message: " + message, e);
        }
        throw new RuntimeException("failed while sending message: " + message);
    }

}
