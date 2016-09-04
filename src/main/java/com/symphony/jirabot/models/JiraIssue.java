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

package com.symphony.jirabot.models; /**
 * Created by ryan.dsouza on 5/27/16.
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

/**
 * Represents a com.symphony.jirabot.models.JiraIssue
 */
public class JiraIssue {

    private String id;
    private String self;
    private String key;

    private Date created;
    private Date updated;

    private String watchesLink;
    private String summary;

    private Priority priority;
    private Priority status;

    private JiraUser assignee;
    private JiraUser creator;
    private JiraUser reporter;

    private ChangeLog changeLog;

    public JiraIssue(JSONObject object) {

        this.id = object.getString("id");
        this.self = object.getString("self");
        this.key = object.getString("key");

        if(this.key.equals("DES-9763") || this.self.contains("DES-9763")) {
            //System.out.println(object.toString());
        }

        this.changeLog = new ChangeLog(object.getJSONObject("changelog"));

        JSONObject fields = object.getJSONObject("fields");
        this.created = getDateFromUSLocale(fields.get("created").toString());
        this.updated = getDateFromUSLocale(fields.get("updated").toString());
        this.summary = fields.get("summary").toString();

        JSONObject watches = fields.getJSONObject("watches");
        this.watchesLink = watches.get("self").toString();

        if(fields.has("priority") && fields.getJSONObject("priority") != null) {
            this.priority = new Priority(fields.getJSONObject("priority"));
        }
        else {
            this.priority = null;
        }
        this.status = new Priority(fields.getJSONObject("status"));

        if(fields.has("assignee")) {
            if(isValidText(fields.get("assignee").toString())) {
                this.assignee = new JiraUser(fields.getJSONObject("assignee"));
            }
        }
        else {
            this.assignee = null;
        }

        this.creator = new JiraUser(fields.getJSONObject("creator"));
        this.reporter = new JiraUser(fields.getJSONObject("reporter"));
    }

    public String getId() {
        return id;
    }

    public String getSelf() {
        return self;
    }

    public String getKey() {
        return key;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getWatchesLink() {
        return watchesLink;
    }

    public String getSummary() {
        return summary;
    }

    public Priority getPriority() {
        return priority;
    }

    public Priority getStatus() {
        return status;
    }

    public JiraUser getAssignee() {
        return assignee;
    }

    public JiraUser getCreator() {
        return creator;
    }

    public JiraUser getReporter() {
        return reporter;
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }


    /**
     * Represents the ChangeLog (History) for each Issue
     */
    public class ChangeLog {
        private int startAt;
        private int maxResults;
        private int total;
        private History[] histories;

        public ChangeLog(JSONObject object) {
            this.startAt = object.getInt("startAt");
            this.maxResults = object.getInt("maxResults");
            this.total = object.getInt("total");

            JSONArray histories = object.getJSONArray("histories");
            this.histories = new History[histories.length()];
            for(int i = 0; i < histories.length(); i++) {
                this.histories[i] = new History(histories.getJSONObject(i));
            }
        }

        public int getStartAt() {
            return startAt;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public int getTotal() {
            return total;
        }

        public History[] getHistories() {
            return histories;
        }

        public void setHistories(History[] histories) {
            this.histories = histories;
        }
    }

    /**
     * Represents a History object in an Issue's ChangeLog
     */
    public class History {
        private String id;
        private Date created;
        private JiraUser author;
        private Item[] items;

        public History(JSONObject object) {
            this.id = object.getString("id");
            this.created = getDateFromUSLocale(object.getString("created"));

            //The default condition
            if(object.has("author")) {
                this.author = new JiraUser(object.getJSONObject("author"));
            }
            //Automated testing conditions ie. issue:
            else if(object.has("historyMetadata")) {
                JSONObject historyData = object.getJSONObject("historyMetadata");
                JSONObject actor = historyData.getJSONObject("actor");
                this.author = new JiraUser();
                this.author.setDisplayName(historyData.get("emailDescription").toString());
            }
            else {
                this.author = null;
            }

            JSONArray items = object.getJSONArray("items");
            this.items = new Item[items.length()];
            for(int i = 0; i < items.length(); i++) {
                this.items[i] = new Item(items.getJSONObject(i));
            }
        }

        public String getId() {
            return id;
        }

        public Date getCreated() {
            return created;
        }

        public JiraUser getAuthor() {
            return author;
        }

        public Item[] getItems() {
            return items;
        }
    }


    /**
     * Represents each item in the History of a Issue's ChangeLog
     */
    public static class Item {

        public enum Priority {
            Low,
            Medium,
            High;

            public static final EnumSet<Item.Priority> HIGH = EnumSet.of(High, Medium, Low);
            public static final EnumSet<Item.Priority> MEDIUM = EnumSet.of(Low, Medium);
            public static final EnumSet<Item.Priority> LOW = EnumSet.of(Low);
        }

        public enum Sentiment {
            Excellent,
            Good,
            Average,
            Poor,
            Bad;

            public static final EnumSet<Item.Sentiment> GREAT = EnumSet.of(Excellent);
            public static final EnumSet<Item.Sentiment> GOOD = EnumSet.of(Excellent, Good, Average);
            public static final EnumSet<Item.Sentiment> NEUTRAL = EnumSet.of(Average);
            public static final EnumSet<Item.Sentiment> BAD = EnumSet.of(Poor, Bad);
        }

        public enum SpecialEmoji {
            Red_Exclamation_Mark,
            Face_Palm,
            Sob_And_Face_Palm,
            None;
        }

        private String field;
        private String fieldType;
        private String from;
        private String fromString;
        private String to;
        private String toString;

        private EnumSet<Item.Priority> priority;
        private EnumSet<Item.Sentiment> sentiment;
        private Item.SpecialEmoji specialEmoji = SpecialEmoji.None;

        public Item(JSONObject object) {

            this.field = object.has("field") ? object.get("field").toString() : null;
            this.fieldType = object.has("fieldtype") ? object.get("fieldtype").toString() : null;
            this.from = object.has("from") ? object.get("from").toString() : null;
            this.fromString = object.has("fromString") ? object.get("fromString").toString() : null;
            this.to = object.has("to") ? object.get("to").toString() : null;
            this.toString = object.has("toString") ? object.get("toString").toString() : null;

            this.assignPriority();
        }

        private void assignPriority() {

            //For changes in status
            if(this.field.equalsIgnoreCase("status")) {
                if(this.toString.equals("Open")) {
                    this.priority = Priority.LOW;
                    this.sentiment = Sentiment.NEUTRAL;
                }
                else if(this.toString.equals("In Progress")) {
                    this.priority = Priority.MEDIUM;
                    this.sentiment = Sentiment.GOOD;

                    if(this.fromString.equals("Reopened")) {
                        this.priority = Priority.HIGH;
                        this.sentiment = Sentiment.BAD;
                        this.specialEmoji = SpecialEmoji.Sob_And_Face_Palm;
                    }
                }
                else if(this.toString.equals("Resolved")) {
                    this.priority = Priority.HIGH;
                    this.sentiment = Sentiment.GREAT;
                }
                else if(this.toString.equals("Reopened")) {
                    this.priority = Priority.HIGH;
                    this.sentiment = Sentiment.BAD;
                    this.specialEmoji = SpecialEmoji.Face_Palm;
                }
                else if(this.toString.equals("Closed")) {
                    this.priority = Priority.HIGH;
                    this.sentiment = Sentiment.GREAT;
                }
                else if(this.toString.equals("ready for code review")) {
                    this.priority = Priority.HIGH;
                    this.sentiment = Sentiment.GREAT;
                }
                else if(this.toString.equals("Ready for QA")) {
                    this.priority = Priority.HIGH;
                    this.sentiment = Sentiment.GREAT;
                }
                else {
                    this.priority = Priority.LOW;
                    this.sentiment = Sentiment.GOOD;
                }
            }

            //Assigned to a new person
            else if(this.field.equalsIgnoreCase("assignee")) {
                this.priority = Priority.MEDIUM;
                this.sentiment = Sentiment.NEUTRAL;
            }

            //Adding/removing a key
            else if(this.field.equalsIgnoreCase("key")) {
                this.priority = Priority.LOW;
                this.sentiment = Sentiment.NEUTRAL;
            }

            //Adding/removing a label
            else if(this.field.equalsIgnoreCase("labels")) {
                this.priority = Priority.LOW;
                this.sentiment = Sentiment.NEUTRAL;
            }

            //Changing projects
            else if(this.field.equalsIgnoreCase("project")) {
                this.priority = Priority.LOW;
                this.sentiment = Sentiment.NEUTRAL;
            }

            //Git commits, usually
            else if(this.field.equalsIgnoreCase("version")) {
                this.priority = Priority.MEDIUM;
                this.sentiment = Sentiment.NEUTRAL;
            }

            //Change in priority
            else if(this.field.equalsIgnoreCase("rank")) {
                if(this.toString.contains("higher")) {
                    this.priority = Priority.HIGH;
                    this.sentiment = Sentiment.BAD;
                    this.specialEmoji = SpecialEmoji.Red_Exclamation_Mark;
                }
                else {
                    this.priority = Priority.LOW;
                    this.sentiment = Sentiment.GOOD;
                }
            }

            //Linked to something else
            else if(this.field.equalsIgnoreCase("Link")) {
                this.priority = Priority.MEDIUM;
                this.sentiment = Sentiment.NEUTRAL;
            }
            else {
                this.priority = Priority.LOW;
                this.sentiment = Sentiment.NEUTRAL;
            }
        }

        public String getEmojiForSentiment() {

            //SpecialEmoji overrides all existing
            switch(this.specialEmoji) {
                case Red_Exclamation_Mark:
                    return ":exclamation:";
                case Face_Palm:
                    return ":facepalm:";
                case Sob_And_Face_Palm:
                    return ":sob: :facepalm:";
                case None:
                    break;
            }

            if(this.isSentimental(Sentiment.BAD)) {
                return ":facepalm:"; //rage, sob, cry, disappointed, confused, frowning, fearful, sad
            }
            else if(this.isSentimental(Sentiment.Poor)) {
                return ":sob:";
            }
            else if(this.isSentimental(Sentiment.Average)) {
                return ":+1:";
            }
            else if(this.isSentimental(Sentiment.NEUTRAL)) {
                return ":grin:";
            }
            else if(this.isSentimental(Sentiment.Good)) {
                return ":blush:"; //smile
            }
            else if(this.isSentimental(Sentiment.Excellent)) {
                return ":sunglasses: :clap: :beer: :dollar:";
            }
            else {
                return ":question:";
            }
        }

        public boolean isSentimental(EnumSet<Item.Sentiment> sentiment) {
            return this.sentiment.containsAll(sentiment);
        }

        public boolean isSentimental(Item.Sentiment sentiment) {
            return this.sentiment.contains(sentiment);
        }

        public boolean isImportant(EnumSet<Item.Priority> priority) {
            return this.priority.containsAll(priority);
        }

        public boolean isImportant(Item.Priority priority) {
            return this.priority.contains(priority);
        }

        public String getField() {
            return field;
        }

        public String getFieldType() {
            return fieldType;
        }

        public String getFrom() {
            return from;
        }

        public String getFromString() {
            return fromString;
        }

        public String getTo() {
            return to;
        }

        public String getToString() {
            return toString;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("Jira Issue");
            if(isValidText(this.fromString)) {
                result.append("From String: " + this.fromString + " ");
            }
            if(isValidText(this.from)) {
                result.append("From: " + this.from + " ");
            }
            if(isValidText(this.toString)) {
                result.append("To String: " + this.toString + " ");
            }
            if(isValidText(this.to)) {
                result.append("To: " + this.to + " ");
            }
            if(isValidText(this.fieldType)) {
                result.append("FieldType: " + this.fieldType + " ");
            }
            if(isValidText(this.field)) {
                result.append("Field: " + this.field + " ");
            }

            return result.toString();
        }
    }

    public static class Priority {
        private String name;
        private String id;

        public Priority(JSONObject object) {
            this.name = object.get("name").toString();
            this.id = object.get("id").toString();
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Priority: " + name + ", ID: " + id;
        }
    }

    public static Date getDateFromUSLocale(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        try {
            return formatter.parse(date);
        }
        catch(Exception e) {
            return new Date();
        }
    }

    private static boolean isValidText(String text) {
        if(text == null || text.length() == 0 || text.equalsIgnoreCase("null")) {
            return false;
        }
        return true;
    }
}
