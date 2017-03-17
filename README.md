[![Symphony Software Foundation - Archived](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-archived.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Archived)

##Symphony Jira Bot

###Created by Ryan D'souza and Paul Pollack

####Purpose
This bot was created to optimize the workflow between Jira and Symphony. 

It's set to poll Jira every X minutes to get the most recently updated issues.
It then assigns a priority to each issue so that only the most important updates are messaged (i.e. issues that are re-opened have high priority and are communicated, while updates like comments are low priority and not communicated) 

Future iterations of this bot can also allow autonomous reminders for people who haven't updated their tickets in a while. It can also be used to ensure proper ticket hygeine.

In addition, developers could remind other developers to look at an issue or update an issue (i.e. assign a new person for it or move it from QA to Done) 

Sentiment analysis is also used to rate each issue based on the action, and the corresponding emojis are displayed (i.e. issues that are moved from QA to Done receive happy/money emoticons, while issues that are reopened receive negative emoticons) 


####Run Instructions

- Download this ZIP 

- Uncompress this ZIP 

- Open project folder in IntelliJ 

- Install maven dependencies from pom.xml

- Run the main method of 'SymphonyJiraBotApp.java'



