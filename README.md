# Nessus Tools App

![Demon of Nessus data in Splunk](https://raw.githubusercontent.com/billyJoePiano/NessusTools/master/screenshots/Splunk-table%20(best%20format%20for%20viewing).png)

## Problem Statement

Nessus is a vulnerability scanning app which helps to identify security vulnerabilities on networks, devices, and servers.  Previously, there was a Python script which transferred data from the Nessus API into Splunk, a NoSQL database used for logging machine-generated data by many CyberSecurity and Technology Services organization.  However, this Python script no longer works, and so the Nessus data is effectively inaccessible to most of the people in any organization which uses Splunk for data storage.  Often technology service teams beyond just Security, including Desktop Engineering, Server Teams, and Help Desks, would benefit from this Nessus data.


## Minimum Viable Product

### Key Features:

- Splunk Integration
- Dashboard for users/stakeholders
- User Authentication through Active Directory


### Splunk Integration

The app should be able to pull all data from the Nessus API and load it into Splunk via the Splunk API, thus ensuring an accessible record of the data external to the Nessus server.


### Dashboard

The Dashboard should provide an overview of data from recent Nessus scans, and allow users to initiate scans of various devices (...Nessus API permitting)
*MAYBE, time permitting:*  The Dashboard could also include search and data analysis features, to dig deeper into the data.  Alternatively, these features could be built in Splunk, with the Nessus dashboard providing direct links to the applicable Splunk searches.


### Authentication

The Dashboard login should be integrated with an external authentication service, ideally an Active Directory Domain Controller.



## Project Timeline Plan
(as of 2-9-22)

### February
- Become familiar with Nessus, its functionality, authentication, and API
- **Target Monday, February 28th:** set up the server on the Madison College network which will host the app

### March
- Become familiar with the Splunk API, and focus on Splunk integration to upload Nessus data into Splunk
- Write unit tests for these integrations
- Start working on the dashboard, focusing on authentication with Active Directory

### April
- **Target: Monday, April 4th:**  Complete Splunk Integration.
- Focus on Dashboard design and features.
- Develop unit tests for the dashboard.
- **Target: Monday, April 18th:** Integration with authentication service (Active Directory) completed
- Get stakeholder feedback on the dashboard features & UI.

### May
- Incorporate feedback from stakeholders.
- Debug and put on finishing touches.
- Make sure all unit tests pass.
- **Target: Wednesday, May 11th:** Completed fully functional MVP App.
- *Afterwards:* Write additional documentation for future users/maintainers.


## Resources

### Tenable / Nessus

- API intro: https://docs.tenable.com/tenablesc/Content/APIKeyAuthentication.htm
- API documentation: https://docs.tenable.com/tenablesc/api/index.htm
- API best practices: https://docs.tenable.com/tenablesc/api_best_practices/Content/ScApiBestPractices/AboutScApiBestPrac.htm
