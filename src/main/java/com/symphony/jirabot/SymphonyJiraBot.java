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

import com.symphony.api.agent.model.V2Message;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.jirabot.clients.IJiraClient;
import com.symphony.jirabot.clients.ISymphonyClient;
import com.symphony.jirabot.configurations.IConfigurationProvider;
import com.symphony.jirabot.formatters.JiraIssueFormatterMessageML;
import com.symphony.jirabot.formatters.JiraIssueFormatterPlainText;
import com.symphony.jirabot.formatters.MessageML;
import com.symphony.jirabot.models.JiraIssue;
import com.symphony.jirabot.models.JiraProject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class SymphonyJiraBot implements Runnable {

  private final Logger LOG = LoggerFactory.getLogger(SymphonyJiraBot.class);

  private final IConfigurationProvider configurationProvider;
  private final IJiraClient jiraClient;
  private final ISymphonyClient symphonyClient;

  private Set<V2RoomDetail> symphonyRooms;

  private final JiraIssueFormatterPlainText lowPriorityPlain, highPriorityPlain;
  private final JiraIssueFormatterMessageML highPriorityMessageMl;

  public SymphonyJiraBot(IConfigurationProvider configurationProvider, IJiraClient jiraClient,
      ISymphonyClient symphonyClient) {

    this.configurationProvider = configurationProvider;
    this.symphonyClient = symphonyClient;
    this.jiraClient = jiraClient;

    jiraClient.authenticate();

    symphonyClient.authenticate();
    // TODO join room is not yet implemented: symphonyClient.joinRoom();

    Set<String> namesOfSymphonyRoomsToPostIn =
        configurationProvider.getNamesOfSymphonyRoomsToPostIn();
    LOG.info("searching for these rooms: " + namesOfSymphonyRoomsToPostIn);
    if (namesOfSymphonyRoomsToPostIn != null) {
      symphonyRooms = new HashSet<V2RoomDetail>();

      for (String roomToPostIn : namesOfSymphonyRoomsToPostIn) {
        V2RoomDetail symphonyRoom = symphonyClient.getRoomForSearchQuery("Ryan Room");

        if (symphonyRoom != null) {
          LOG.info(
              "successfully found symphony room: " + symphonyRoom.getRoomAttributes().getName());
          symphonyRooms.add(symphonyRoom);
        } else {
          LOG.warn("did not find Symphony room for query: " + roomToPostIn);
        }
      }
    }

    lowPriorityPlain =
        new JiraIssueFormatterPlainText(JiraIssue.Item.Priority.Low,
            configurationProvider.getJiraBaseURL(),
            configurationProvider.getJiraRefreshRateInSeconds());

    highPriorityPlain =
        new JiraIssueFormatterPlainText(JiraIssue.Item.Priority.High,
            configurationProvider.getJiraBaseURL(),
            configurationProvider.getJiraRefreshRateInSeconds());

    highPriorityMessageMl =
        new JiraIssueFormatterMessageML(JiraIssue.Item.Priority.High,
            configurationProvider.getJiraBaseURL(),
            configurationProvider.getJiraRefreshRateInSeconds());

  }


  public void run() {
    checkJiraForUpdatesAndPostInSymphony();
  }

  private void checkJiraForUpdatesAndPostInSymphony() {

    LOG.debug("getting all JIRA Projects");
    JiraProject[] jiraProjects = jiraClient.getAllProjects();
    int numProjectsFound = jiraProjects == null ? 0 : jiraProjects.length;
    LOG.debug("got " + numProjectsFound + " JIRA projects");

    if (numProjectsFound == 0) {
      LOG.warn("no JIRA projects found...nothing to do");
      return;
    }

    boolean projectOfInterestFound = false;
    for (JiraProject project : jiraProjects) {
      if (configurationProvider.getJiraProjectsOfInterest().contains(project.getName())) {
        processJiraProject(project);
        projectOfInterestFound = true;
      }
    }

    if (!projectOfInterestFound) {
      LOG.warn("non of the configured JIRA projects of interest were found in JIRA");
    }

    LOG.debug("finished for now...");
  }

  private void processJiraProject(JiraProject project) {

    LOG.debug("getting issues for JIRA project " + project.getName());
    ArrayList<JiraIssue> jiraIssues = jiraClient.getIssuesForProject(project);
    LOG.debug("got " + jiraIssues.size() + " issues for JIRA project " + project.getName());

    for (JiraIssue jiraIssue : jiraIssues) {

      String lowPriorityIssueString = lowPriorityPlain.getPrettyIssueString(jiraIssue);
      LOG.debug("Low priority issue: " + lowPriorityIssueString);

      String highPriorityIssueString = highPriorityPlain.getPrettyIssueString(jiraIssue);
      LOG.debug("high priority issue: " + highPriorityIssueString);

      if (highPriorityIssueString.replaceAll(" ", "").length() > 0) {

        MessageML messageML = highPriorityMessageMl.getMessageMLForIssue(jiraIssue);

        if (symphonyRooms.isEmpty()) {
          LOG.warn("no symphony rooms to post in, dropping message: " + messageML);
        } else {

          // for each symphony room, post the update
          for (V2RoomDetail room : symphonyRooms) {
            V2Message message = symphonyClient.sendMessage(room, messageML);
            if (message == null) {
              LOG.error("for JIRA issue: " + jiraIssue + " failed to send messageML: "
                  + messageML.toString());
            } else {
              LOG.debug("successfully sent " + highPriorityIssueString);
            }
          }

        }
      }
    }
  }
}
