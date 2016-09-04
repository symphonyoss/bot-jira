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
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Base class for formatting a com.symphony.jirabot.models.JiraIssue
 * Extended by PlainText and com.symphony.jirabot.formatters.MessageML formatters
 */

public abstract class JiraIssueFormatter {

    protected final JiraIssue.Item.Priority priority;
    protected final String jiraBaseUrl;
    protected final int refreshRateInSeconds;

    public JiraIssueFormatter(JiraIssue.Item.Priority priority, String jiraBaseUrl,
            int refreshRateInSeconds) {
        this.priority = priority;
        this.jiraBaseUrl = jiraBaseUrl;
        this.refreshRateInSeconds = refreshRateInSeconds;
    }

    /**
     * Returns a String with the nice formatting of a com.symphony.jirabot.models.JiraIssue
     * @param issue
     * @return Nicely formatted issue
     */
    public abstract String getPrettyIssueString(JiraIssue issue);

    /**
     * Maps each history in a com.symphony.jirabot.models.JiraIssue to a user by their email address
     * @param issue
     * @return Mapping of each user and their JiraIssues
     */
    protected HashMap<String, ArrayList<JiraIssue.History>> getHistoriesForPerson(JiraIssue issue) {
        //Associate all the items in history to a person
        HashMap<String, ArrayList<JiraIssue.History>> historiesForPerson = new HashMap<String, ArrayList<JiraIssue.History>>();
        for(JiraIssue.History history : issue.getChangeLog().getHistories()) {

            final String authorEmail = history.getAuthor().getEmailAddress();
            final ArrayList<JiraIssue.History> histories;
            if(historiesForPerson.containsKey(authorEmail)) {
                histories = historiesForPerson.get(authorEmail);
            }
            else {
                histories = new ArrayList<JiraIssue.History>();
                historiesForPerson.put(authorEmail, histories);
            }

            histories.add(history);
        }

        return historiesForPerson;
    }

    /**
     * Checks to see if text is valid
     */
    protected static boolean isValidText(String text) {
        if(text == null || text.length() == 0) {
            return false;
        }

        if(text.equalsIgnoreCase("null")) {
            return false;
        }

        return true;
    }

    /**
     * A string representing the time between two dates
     * @param start
     * @param end
     * @return Nicely formatted difference in time
     */
    protected static String getTimeBetweenAsString(Date start, Date end) {
        long duration = end.getTime() - start.getTime();
        int differenceInHours = (int) TimeUnit.MILLISECONDS.toHours(duration);

        if(differenceInHours == 0) {
            int differenceInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(duration);

            if(differenceInMinutes == 0) {
                return "Just now";
            }
            else if(differenceInMinutes == 1) {
                return "1 minute ago";
            }
            else {
                return differenceInMinutes + " minutes ago";
            }
        }
        else if(differenceInHours == 1) {
            return "1 hour ago";
        }
        else {
            return differenceInHours + " hours ago";
        }
    }

    /**
     * Checks to see if a Date is within the date range
     * @param jiraIssueDate
     */
    protected boolean isWithinTimeRange(Date jiraIssueDate) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.SECONDS.toMillis(this.refreshRateInSeconds);
        Date startDate = new Date(startTime);
        return jiraIssueDate.after(startDate);
    }

    /**
     * Formats Version type change to com.symphony.jirabot.models.JiraIssue - usually a git commit
     */
    protected void formatVersion(JiraIssue.Item item, StringBuilder text) {
        text.append("made changes to ");
        if (isValidText(item.getFromString())) {
            text.append(item.getFromString());
            if (isValidText(item.getFrom())) {
                text.append("[" + item.getFrom() + "]");
            }
        }

        if (isValidText(item.getToString())) {
            text.append(item.getToString());
            if (isValidText(item.getTo())) {
                text.append("[" + item.getTo() + "]");
            }
        }
    }

    /**
     * Formats Assignee type change to com.symphony.jirabot.models.JiraIssue
     */
    protected void formatAssignee(JiraIssue.Item item, StringBuilder text) {
        String to = isValidText(item.getToString()) ? item.getToString() : item.getTo();
        String from = isValidText(item.getFromString()) ? item.getFromString() : item.getFrom();

        text.append("Assigned to " + to + " from " + from);
    }

    /**
     * Formats add a label to a com.symphony.jirabot.models.JiraIssue
     */
    protected void formatLabels(JiraIssue.Item item, StringBuilder text) {
        text.append("Added label " + item.getToString());
    }

    /**
     * Formats adding a key to a com.symphony.jirabot.models.JiraIssue
     */
    protected void formatKey(JiraIssue.Item item, StringBuilder text) {
        String to = isValidText(item.getTo()) ? item.getTo() : item.getToString();
        String from = isValidText(item.getFrom()) ? item.getFrom() : item.getFromString();
        text.append("Added key to " + to + " from " + from);
    }

    /**
     * Formats all other changes to a com.symphony.jirabot.models.JiraIssue
     */
    protected void formatAllElse(JiraIssue.Item item, StringBuilder text) {
        if(item.getField().equals("project")) {
            text.append("Moved from");
        }
        else if(item.getField().equals("status")) {
            text.append("Set status from");
        }
        else if(item.getField().equalsIgnoreCase("Rank")) {
            text.append("Issue ");
        }
        else {
            text.append(item.getField());
        }
        if (isValidText(item.getFromString())) {
            text.append(" " + item.getFromString());
            if (isValidText(item.getFrom()) && !item.getField().equals("status")) {
                text.append("[" + item.getFrom() + "]");
            }
        }

        if (isValidText(item.getFromString()) && isValidText(item.getToString())) {
            text.append(" to ");
        }

        if (isValidText(item.getToString())) {
            text.append(" " + item.getToString());
            if (isValidText(item.getTo()) && !item.getField().equals("status")) {
                text.append("[" + item.getTo() + "]");
            }
        }
    }

    /**
     * Formats a com.symphony.jirabot.models.JiraIssue
     */
    protected StringBuilder getFormatJiraIssue(JiraIssue.Item item) {

        StringBuilder text = new StringBuilder();

        if (item.getField().equals("Version")) {
            formatVersion(item, text);
        }
        else if (item.getField().equals("assignee")) {
            formatAssignee(item, text);
        }
        else if (item.getField().equals("labels")) {
            formatLabels(item, text);
        }
        else if (item.getField().equals("Key")) {
            formatKey(item, text);
        }
        else {
            formatAllElse(item, text);
        }
        text.append(" " + item.getEmojiForSentiment() + " ");
        text.append(".  ");

        return text;
    }


    /**
     * Removes all invalid/control characters from String
     */
    protected String cleanString(String result) {
        result = result.replaceAll("[\u0000-\u001f]", "");
        return result;
    }
}
