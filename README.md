# TenaPull App

## The Problem

Nessus is a vulnerability scanning app which helps to identify security vulnerabilities on networks, devices, and servers.  Previously, there was a Python script which transferred data from the Nessus API into Splunk, a NoSQL database used for logging machine-generated data by many CyberSecurity and Technology Services teams.  However, this Python script no longer works, and so the Nessus data is effectively inaccessible to most of the people in any organization which uses Splunk for data storage.  Often technology service teams beyond just Security, including Desktop Engineering, Server Teams, and Help Desks, would benefit from Nessus data.

## The Solution

TenaPull is a configurable Java application which fetches and processes the data from one or more Nessus APIs, and converts it into JSON ouputs that are usable by Splunk, and possibly by other NoSQL databases.  TenaPull uses a MySQL database with the Hibernate ORM for its local cache of data, Jackson for serialization/deserialization, and the Jersey client to reach the Nessus API.

For a complete demonstration, click the below picture which will take you to a YouTube video.

<a href="https://www.youtube.com/watch?v=aHoMRjRHHrc" rel="Demo video">![Demo video](https://raw.githubusercontent.com/billyJoePiano/TenaPull/master/screenshots/Splunk-table%20(best%20format%20for%20viewing).png)</a>


## Resources

### TenaPull Documentation

- [Install and run instructions](installAndRun.md)
- JavaDocs: https://billyjoepiano.github.io/TenaPull/

### Tenable / Nessus

- API intro: https://docs.tenable.com/tenablesc/Content/APIKeyAuthentication.htm
- API documentation: https://docs.tenable.com/tenablesc/api/index.htm
- API best practices: https://docs.tenable.com/tenablesc/api_best_practices/Content/ScApiBestPractices/AboutScApiBestPrac.htm
