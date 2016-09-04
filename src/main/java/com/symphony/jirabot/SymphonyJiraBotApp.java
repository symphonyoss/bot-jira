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

package com.symphony.jirabot;

import com.symphony.jirabot.clients.IJiraClient;
import com.symphony.jirabot.clients.ISymphonyClient;
import com.symphony.jirabot.clients.JiraOauthClient;
import com.symphony.jirabot.clients.SymphonyClient;
import com.symphony.jirabot.configurations.IConfigurationProvider;
import com.symphony.jirabot.configurations.SimpleConfigurationProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * This class starts a process running a bot that monitors JIRA for notable events and posts
 * them in Symphony chats.
 *
 * Created by ryan.dsouza on 5/26/16.
 */
public class SymphonyJiraBotApp {

    private static final Logger LOG = LoggerFactory.getLogger(SymphonyJiraBotApp.class);

    public static void main(String[] args) {

        // TODO make configuration provider determined by args, rather than hardwired as it is here
        IConfigurationProvider configurationProvider = new SimpleConfigurationProvider();

        ISymphonyClient symphonyClient = new SymphonyClient(configurationProvider);

        IJiraClient jiraClient = new JiraOauthClient(configurationProvider);

        SymphonyJiraBot symphonyJiraBot =
            new SymphonyJiraBot(configurationProvider, jiraClient, symphonyClient);

        LOG.info("starting bot with jiraBaseUrl={}, symphonyBaseUrl={}, refreshIntervalInSeconds={}",
            configurationProvider.getJiraBaseURL(), configurationProvider.getSymphonyBaseURL(),
            configurationProvider.getJiraRefreshRateInSeconds());

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(symphonyJiraBot, 0,
            configurationProvider.getJiraRefreshRateInSeconds(), TimeUnit.SECONDS);

        LOG.info("bot started");

    }
}