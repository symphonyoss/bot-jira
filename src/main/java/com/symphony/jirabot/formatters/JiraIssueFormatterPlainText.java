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

package com.symphony.jirabot.formatters;

import com.symphony.jirabot.models.JiraIssue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ryan.dsouza on 6/6/16.
 */
public class JiraIssueFormatterPlainText extends JiraIssueFormatter {

    public JiraIssueFormatterPlainText(JiraIssue.Item.Priority priority, String jiraBaseUrl,
            int refreshRateInSeconds) {
        super(priority, jiraBaseUrl, refreshRateInSeconds);
    }

    public String getPrettyIssueString(JiraIssue issue) {

        HashMap<String, ArrayList<JiraIssue.History>> historiesForPerson = super.getHistoriesForPerson(issue);
        Date now = new Date();

        boolean shouldIncludeIssue = false;

        StringBuilder text = new StringBuilder();
        for(String emailAddress : historiesForPerson.keySet()) {
            ArrayList<JiraIssue.History> histories = historiesForPerson.get(emailAddress);

            for (JiraIssue.History history : histories) {

                if (history.getItems().length > 0) {

                    boolean didAddAuthorInformation = false;

                    for (JiraIssue.Item item : history.getItems()) {
                        if (item.isImportant(this.priority)) {
                            if (!didAddAuthorInformation) {
                                text.append(history.getAuthor().getDisplayName() + ": ");
                                didAddAuthorInformation = true;
                            }
                            shouldIncludeIssue = true;
                            formatJiraIssue(item, text);
                        }
                    }
                }
            }
        }

        StringBuilder fullText = new StringBuilder(text);
        final String issueLink = jiraBaseUrl + "/browse/" + issue.getKey();

        if(fullText.length() == 0 && super.isWithinTimeRange(issue.getCreated())) {
            fullText.append(issue.getCreator().getDisplayName() + " created this issue: ");
            fullText.append(issueLink);
            fullText.append(issue.getSummary());
            fullText.append(" - " + getTimeBetweenAsString(issue.getCreated(), now));
            fullText.append(" :facepalm:");
        }
        else {
            if(fullText.length() > 0 && shouldIncludeIssue) {
                fullText.append(issueLink);
                fullText.append(issue.getSummary());
                fullText.append(" - " + getTimeBetweenAsString(issue.getUpdated(), now));
            }
        }

        String result = fullText.toString();
        return super.cleanString(result);
    }


    private void formatJiraIssue(JiraIssue.Item item, StringBuilder text) {
        StringBuilder formattedText = super.getFormatJiraIssue(item);
        text.append(formattedText);
    }
}
