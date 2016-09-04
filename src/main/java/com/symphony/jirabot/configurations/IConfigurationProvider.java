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

package com.symphony.jirabot.configurations;

import java.util.Set;

/**
 * Defines the contract that a configuration provider must satisfy, in this context
 * (i.e. SymphonyJiraBot).
 */
public interface IConfigurationProvider {
  /**
   * Private key used for JIRA OAUTH
   */
  String getJiraConsumerPrivateKey();

  /**
   * JIRA base URL
   */
  String getJiraBaseURL();

  /**
   * JIRA OAUTH callback
   * TODO how is this different from JiraCallbackUrl?
   */
  String getJiraCallback();

  /**
   * JIRA OAUTH callback URL
   */
  String getJiraCallbackURI();

  /**
   * JIRA OAUTH consumer key
   */
  String getJiraConsumerKey();

  /**
   * JIRA OAUTH access token
   */
  String getJiraAccessToken();

  /**
   * How often to poll JIRA for updates
   */
  int getJiraRefreshRateInSeconds();

  /**
   * A set of JIRA project names that the bot will take interest in
   */
  Set<String> getJiraProjectsOfInterest();

  /**
   * Maximum JIRA results for query seeking JIRA projects
   */
  int getMaxResultsForGettingJiraProjects();

  /**
   * Maximum JIRA results for query seeking issues for a single project
   */
  int getMaxResultsForGettingJiraIssuesPerProject();


  /**
   * Password of keystore for authentication with Symphony
   */
  String getSymphonyKeystorePassword();

  /**
   * Type of keystore for authentication with Symphony (e.g. pkcs12)
   */
  String getSymphonyKeystoreType();

  /**
   * Name of certificate file for authentication with Symphony
   */
  String getSymphonyCertificateFileName();

  /**
   * Symphony base URL
   */
  String getSymphonyBaseURL();

  /**
   * A set of names of Symphony rooms to post notable JIRA events into
   */
  Set<String> getNamesOfSymphonyRoomsToPostIn();
}
