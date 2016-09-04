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
        + "MIICXAIBAAKBgQDWUx733WLaKEMPC/dzdlho0EaeFjY8WfV9ug8PkRQkT/414mTt\n"
        + "N+I7yq8vVBRlI1/1KH9g9aeCO1Drb0O3Euglexctsj+gkv88F6Og29JGRdMA6Wlt\n"
        + "urErK3IvC0g8F/5/Y5b7bKNXoEBKyxvBVEZPnY2yLxCU4fQLl+FhPg1SjQIDAQAB\n"
        + "AoGAJws/cgIntvxssvoIG0Ws93Mx2izLtpTgzwWtJrXUSIU2F1Tl8/0hPqk+3s1f\n"
        + "Zcla+stk4SH/YQ8zP6CmYlyUY6IIRhtfXtHG9kCLsdsWCJEQZ7+f5CcwmUBuoNsS\n"
        + "iN6X1yQd5eiHLC8fuF0xg7O1/BbnCJgaD6Sh2xp59DeelqECQQD7inXkHqDkn1m5\n"
        + "DaNCQMhQ+CQ+yoXBZS+p9lsVWvY7V4xwJRUUJhf8C9enX7LsFjYnrNcXMTexc44+\n"
        + "eiE42sdZAkEA2h/EOlaOvcDeBvlCkChTJ81xbshsW2aatuYs/uGhhnBR1Y2wQktx\n"
        + "uW9R5+RDTOYFPiWnVByF5MHECZf+vM/yVQJAKfhdWVW+9Mad2uGqpuhWRCRTL+Ls\n"
        + "1GsEu/AuHG8T/KzL8v5M+RKuF9EGB5hRK1E9cftF3EnLCCHGzyfjmS/v6QJBAIYu\n"
        + "nIw2y0C38N3hYK1F4UHPBETeTqo7iFmTZ4K0UqVdWzpAG3ns51znuj8pEK2xovAE\n"
        + "QddgZI3BFVPBiRel5LECQEU+lh5VeaKmB1CtBKXHLOPng9Uw6V9fRhqpJfy2Y+XQ\n"
        + "eMr7H2smcfKxDKZ6ghPdFMGi903yy9UvZOq0Sk0G5sg=\n"
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
