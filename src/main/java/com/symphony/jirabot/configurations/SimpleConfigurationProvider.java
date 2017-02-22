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

import java.util.Collections;
import java.util.Set;

/**
 * Created by ryan.dsouza on 5/26/16.
 * <p>
 * Returns a bunch of properties
 * Customize this for each machine
 */
public class SimpleConfigurationProvider implements IConfigurationProvider {

  public String getJiraConsumerPrivateKey() {
    return "-----BEGIN RSA PRIVATE KEY-----\n"
        + "-----END RSA PRIVATE KEY-----";
  }

  public int getJiraRefreshRateInSeconds() { return 10 * 60 * 24; }

  public Set<String> getJiraProjectsOfInterest() {
    return Collections.singleton("Core");
  }

  public int getMaxResultsForGettingJiraProjects() {
    return 10; //Usually 1000
  }

  public int getMaxResultsForGettingJiraIssuesPerProject() {
    return 500; //Usually 1000
  }

  public String getJiraBaseURL() {
    return "https://perzoinc.atlassian.net";
  }

  public String getJiraCallback() {
    return "https://placeholder";
  }

  public String getJiraCallbackURI() {
    return "http://consumer/callback";
  }

  public String getJiraConsumerKey() {
    return "sUper1337";
  }

  public String getJiraAccessToken() {
    return "To3e4eNrc9mlvQR4cCR6lVBMG86U0caz";
  }

  public String getSymphonyKeystorePassword() {
    return "cOrpB0tK3y";
  }

  public String getSymphonyKeystoreType() {
    return "pkcs12";
  }

  public String getSymphonyCertificateFileName() {
    return "symphony-jira-bot.p12";
  }

  public String getSymphonyBaseURL() {
    return "https://sym-corp-stage-guse1-aha1.symphony.com:8444";
  }

  public Set<String> getNamesOfSymphonyRoomsToPostIn() {
    return Collections.singleton("iv");
  }

}
