# Nessus Tools App


## Problem Statement

Madison College’s Technology Services employs Nessus, a vulnerability scanning app which helps to identify security vulnerabilities on Madison College’s network, devices, and servers.

Because we have only deployed the most basic level of this app, there is only one user login for viewing the data resulting from these scans.  This limits the availability of this data and makes it much harder to search and cross-reference it with other data we have available from other services and data sources (e.g. device inventories, Sysmon and Mitre Threathunting, Active Directory, Microsoft Defender/Security Center, ETC.)

Previously, there was a Python script which transferred the data from the Nessus API to Splunk.  However, due to updates this script no longer works, and so the Nessus data is effectively inaccessible to everyone but the Security Team.
Other technology service teams including Desktop Engineering, the Server Team, and the Help Desk would all benefit from this Nessus data, as well as the ability to initiate scans against specific devices.


## User Statements

### Security Team Members

Jesse is a member of the Madison College Technology Service's Security Team.  He has used Nessus frequently to get information about security vulnerabilities on the College's network and computing systems.  However, in its current format this data can be difficult to use and search.  He'd like some improved ways to utilize it.

Zach is another member of the Security Team.  He administers the Splunk server, which is the central security data logging service for all of Madison College's IT infrastructure.  Zach wants to make sure the Nessus data is included in Splunk's data set, so it can be searched and cross referenced with the other data available there.


### Desktop Engineering and Help Desk Members

Members of the Help Desk recieve frequent calls from Madison College students and staff who are using laptop, desktops, and other technology devices, both borrowed and on-campus.  Sometimes they have to help fix systems that were comprimised when a user accidently downloaded malware after clicking on a phishing link.  They would like the ability to conduct external scans on these devices, to gain additional information about possible vulnerabilities, but right now they don't have any way to do this.


### Server Team

Colleen is a the lead of the server and storage team.  There are many critical systems her team has to administer which the entire college network depends upon.  She would like to know what recent Nessus scans were done and which ones are scheduled, so her team can plan and so the scans don't trigger a panic when the servers think they are under attack.  She would also like to know the results of these scans, and the ability to start them as needed, so the server team can find any gaps in their upgrades.


## Project Timeline Plan
(as of 2-9-22)

###February

-Become familiar with Nessus, its functionality, authentication, and API
-**Target Monday, February 28th:** Setup the server on the Madison College network which will host the app

###March

-Become familiar the Splunk API, and focus on Splunk integration to record Nessus data into Splunk
-Include Unit tests for these integrations
-Start working on Dashboard, focusing on authentication with Active Directory

###April

-**Target: Monday, April 4th:**  Complete Splunk Integration.
-Focus on Dashboard design and features.
-Develop unit tests for the dashboard.
-**Target: Monday, April 18th:** Integration with authentication service (Active Directory) completed
-Get stakeholder feedback on the dashboard features & UI.

###May

-Incorporate feedback from stakeholders.
-Debug and put finishing touches.
-Make sure all unit tests pass.
-**Target: Wednesday, May 11th:** Completed fully functional MVP App.
-Afterwards: Write additional documentation for future users/maintainers.
