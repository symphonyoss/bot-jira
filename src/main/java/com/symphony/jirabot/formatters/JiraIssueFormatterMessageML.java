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

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by ryan.dsouza on 6/6/16.
 */
public class JiraIssueFormatterMessageML extends JiraIssueFormatter {

    public JiraIssueFormatterMessageML(JiraIssue.Item.Priority priority,
            String jiraBaseUrl, int refreshRateInSeconds) {
        super(priority, jiraBaseUrl, refreshRateInSeconds);
    }

    public MessageML getMessageMLForIssue(JiraIssue issue) {
        HashMap<String, ArrayList<JiraIssue.History>> historiesForPerson = super.getHistoriesForPerson(issue);
        Date now = new Date();

        boolean shouldIncludeIssue = false;

        MessageML messageML = new MessageML();

        for(String emailAddress : historiesForPerson.keySet()) {
            ArrayList<JiraIssue.History> histories = historiesForPerson.get(emailAddress);

            for(JiraIssue.History history : histories) {

                if(history.getItems().length > 0) {

                    boolean didAddAuthorInformation = false;

                    for(JiraIssue.Item item : history.getItems()) {
                        if(item.isImportant(this.priority)) {
                            if(!didAddAuthorInformation) {
                                messageML.addParagraph(history.getAuthor().getDisplayName() + ": ");
                                didAddAuthorInformation = true;
                            }
                            shouldIncludeIssue = true;
                            formatJiraIssue(item, messageML);
                            //messageML.addLineBreak();
                        }
                    }
                }
            }
        }

        final String issueLink = jiraBaseUrl + "/browse/" + issue.getKey();

        if(messageML.getNumChildren() == 0 && super.isWithinTimeRange(issue.getCreated())) {
            messageML.addParagraph(issue.getCreator().getDisplayName() + " created this issue: ");
            messageML.addLink(issueLink);
            messageML.addParagraph(issue.getSummary()); //Usually italics
            messageML.addItalicText(" - " + getTimeBetweenAsString(issue.getCreated(), now));
            messageML.addParagraph(" :facepalm:");
        }
        else {
            if(shouldIncludeIssue) {
                messageML.addLink(issueLink);
                messageML.addParagraph(issue.getSummary());
                messageML.addItalicText(" - " + getTimeBetweenAsString(issue.getUpdated(), now));
            }
        }

        return messageML;
    }

    public String getPrettyIssueString(JiraIssue issue) {
        MessageML messageML = this.getMessageMLForIssue(issue);

        if(messageML.getNumChildren() == 0) {
            return "";
        }

        String result = messageML.toString();
        return super.cleanString(result);
    }

    private void formatJiraIssue(JiraIssue.Item item, MessageML messageML) {
        StringBuilder text = super.getFormatJiraIssue(item);
        messageML.addParagraph(text.toString());
    }
}
