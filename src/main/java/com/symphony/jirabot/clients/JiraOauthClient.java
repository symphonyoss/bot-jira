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

import com.symphony.jirabot.configurations.IConfigurationProvider;
import com.symphony.jirabot.models.JiraIssue;
import com.symphony.jirabot.models.JiraProject;
import com.symphony.jirabot.models.JiraUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.signature.RSA_SHA1;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Communicates with JIRA via OAUTH
 *
 * TODO add single reauthentication attempt on unauthorized API call
 *
 * Created by ryan.dsouza on 5/26/16
 */
public class JiraOauthClient implements IJiraClient {

  private final Logger LOG = LoggerFactory.getLogger(JiraOauthClient.class);

  private static final String OAUTH_PATH = "/plugins/servlet/oauth/";

  private final IConfigurationProvider configurationProvider;
  private final OAuthAccessor accessor;
  private final ObjectMapper objectMapper;

  private final String jiraRestApiBaseUrl;

  private Date startDate;

  public JiraOauthClient(IConfigurationProvider configurationProvider) {

    this.configurationProvider = configurationProvider;

    jiraRestApiBaseUrl = configurationProvider.getJiraBaseURL() + "/rest/api/2/";

    this.updateStartAndEndDate();
    this.objectMapper = new ObjectMapper();

    OAuthServiceProvider serviceProvider = new OAuthServiceProvider(getRequestTokenUrl(),
        getAuthorizeUrl(), getAccessTokenUrl());

    OAuthConsumer consumer = new OAuthConsumer(configurationProvider.getJiraCallback(),
        configurationProvider.getJiraConsumerKey(), null, serviceProvider);
    consumer.setProperty(RSA_SHA1.PRIVATE_KEY, configurationProvider.getJiraConsumerPrivateKey());
    consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

    this.accessor = new OAuthAccessor(consumer);
  }

  /**
   * JIRA authentication via a HTTP GET
   */
  public void authenticate() {
    LOG.debug("attempting JIRA authentication...");
    String urlEndpoint = jiraRestApiBaseUrl + "myself";

    String jsonResponse = this.makeAuthenticatedRequest(urlEndpoint, null, "GET");
    JSONObject jsonObject = new JSONObject(jsonResponse);
    if (jsonObject.has("self") && jsonObject.has("name") && jsonObject.has("emailAddress")) {
      LOG.debug("successfully authenticated");
      return;
    } else {
      throw new RuntimeException("JIRA authentication failed, response: " + jsonResponse);
    }
  }

  /**
   * Returns the com.symphony.jirabot.models.JiraUser object associated with an emailAddress
   *
   * @param emailAddress
   * @return Associated com.symphony.jirabot.models.JiraUser
   */
  public JiraUser getJiraUserForEmailAddress(final String emailAddress) {

    String urlEndpoint = jiraRestApiBaseUrl + "user/search?";

    String parameters = "username=" + emailAddress;
    parameters += "&includeInactive=true";
    parameters += "&includeActive=true";

    String jsonResponse = "";

    try {
      jsonResponse = this.makeAuthenticatedRequest(urlEndpoint, parameters, "GET");
      JiraUser[] users = objectMapper.readValue(jsonResponse, JiraUser[].class);

      //TODO: Try different email format - class for finding com.symphony.jirabot.models.JiraUser
      // from SymphonyUser
      if (users.length > 0) {
        return users[0];
      }
    } catch (IOException e) {
      LOG.error("Failed to find user with email address " + emailAddress, e);
      throw new RuntimeException("Failed to make an find user with address: " + emailAddress, e);
    }

    LOG.warn("No users found", jsonResponse);
    throw new RuntimeException("No users found: " + jsonResponse);
  }

  /**
   * Gets all JiraProjects
   *
   * @return All JiraProjects
   */
  public JiraProject[] getAllProjects() {
    try {
      String urlEndpoint = jiraRestApiBaseUrl + "project?";
      String parameters = "expand=description,url,projectKeys";
      parameters += "&maxResults=" + configurationProvider.getMaxResultsForGettingJiraProjects();

      String jsonResponse = this.makeAuthenticatedRequest(urlEndpoint, parameters, "GET");
      JiraProject[] projects = this.objectMapper.readValue(jsonResponse, JiraProject[].class);

      return projects;
    } catch (IOException exception) {
      LOG.error("ERROR MAPPING PROJECTS", exception);
      throw new RuntimeException("Error mapping projects");
    }
  }

  /**
   * Returns all the relevant issues for that com.symphony.jirabot.models.JiraProject
   *
   * @param project
   * @return Relevant issues for that com.symphony.jirabot.models.JiraProject
   */
  public ArrayList<JiraIssue> getIssuesForProject(JiraProject project) {

    ArrayList<JiraIssue> jiraIssues = new ArrayList<JiraIssue>();

    String urlEndpoint = jiraRestApiBaseUrl + "search?";
    String parameters = "";
    parameters += "jql=project=%22" + project.getKey() + "%22"; //Project Key
    parameters += "%20AND%20updatedDate%20%3E" + this.startDate.getTime() + "%20"; //Start date
    parameters += "ORDER%20BY%20updated%20DESC"; //Order by updated time
    //parameters += "&orderBy=-updated";
    parameters += "&expand=changelog"; //operations,editmeta,renderedFields,name,schema
    parameters += "&properties=all";
    parameters +=
        "&maxResults=" + configurationProvider.getMaxResultsForGettingJiraIssuesPerProject();

    String jsonStringResponse = this.makeAuthenticatedRequest(urlEndpoint, parameters, "GET");

    JSONObject jsonObjectResponse = new JSONObject(jsonStringResponse);
    JSONArray jsonIssues = jsonObjectResponse.getJSONArray("issues");

    for (int i = 0; i < jsonIssues.length(); i++) {

      JSONObject jsonIssue = jsonIssues.getJSONObject(i);
      try {
        JiraIssue issue = new JiraIssue(jsonIssue);
        if (issue.getUpdated().after(this.startDate)) {
          jiraIssues.add(issue);
        }

        JiraIssue.History[] histories = issue.getChangeLog().getHistories();
        ArrayList<JiraIssue.History> validHistories = new ArrayList<JiraIssue.History>();

        for (JiraIssue.History history : histories) {
          if (history.getCreated().after(this.startDate)) {
            validHistories.add(history);
          }
        }

        histories = validHistories.toArray(new JiraIssue.History[validHistories.size()]);
        issue.getChangeLog().setHistories(histories);

      } catch (JSONException parsingException) {
        LOG.error("Error parsing: " + jsonIssue.toString(), parsingException);
        continue;
      }
    }

    return jiraIssues;
  }

  /**
   * Makes an authenticated request to the JIRA API
   *
   * @param urlEndpoint
   * @param parameters
   * @param requestType
   * @return Response as String
   */
  public String makeAuthenticatedRequest(String urlEndpoint, String parameters,
      String requestType) {
    try {
      String url;
      if (parameters == null) {
        url = urlEndpoint;
      } else {
        url = urlEndpoint + parameters;
      }

      LOG.debug("Making request to " + url);

      OAuthClient client = new OAuthClient(new HttpClient4());
      this.accessor.accessToken = configurationProvider.getJiraAccessToken();
      OAuthMessage response =
          client.invoke(this.accessor, requestType, url, Collections.<Map.Entry<?, ?>>emptySet());
      return response.readBodyAsString();
    } catch (IOException ioException) {
      LOG.error("Unable to make authenticated request", ioException);
      throw new RuntimeException("Unable to make authenticated request", ioException);
    } catch (URISyntaxException syntaxException) {
      LOG.error("Unable to make authenticated request", syntaxException);
      throw new RuntimeException("Unable to make authenticated request", syntaxException);
    } catch (OAuthException oauthException) {
      LOG.warn("OAuthException in makeAuthenticatedRequest; attempting to reauthenticate",
          oauthException);
      this.authenticate();
      return this.makeAuthenticatedRequest(urlEndpoint, parameters, requestType);
    }
  }


  /**
   * Everytime this method is called, the global startDate variable
   * is initialized to the current time - configuration.getRefreshInterval()
   */
  private void updateStartAndEndDate() {
    long endTime = System.currentTimeMillis();
    long startTime = endTime - TimeUnit.SECONDS.toMillis(
        configurationProvider.getJiraRefreshRateInSeconds());
    this.startDate = new Date(startTime);
  }

  private String getAccessTokenUrl() {
    return configurationProvider.getJiraBaseURL() + OAUTH_PATH + "access-token";
  }

  private String getRequestTokenUrl() {
    return configurationProvider.getJiraBaseURL() + OAUTH_PATH + "request-token";
  }

  private String getAuthorizeUrl() {
    return configurationProvider.getJiraBaseURL() + OAUTH_PATH + "authorize";
  }
}