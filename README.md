# TenaPull: Processing Nessus Data for Splunk

## The Problem

Nessus is a powerful vulnerability scanning app which helps to identify security vulnerabilities on networks, devices, and servers.  Many CyberSecurity and Technology Services teams who utilize Nessus also use Splunk, a NoSQL database designed for logging machine-generated data.  Previously, there was a Python script which transferred data from the Nessus API into Splunk.  However, this Python script no longer works, and so Nessus data is effectively inaccessible to most of the people in any organization which uses Splunk for data storage.  Often technology service teams beyond just Security -- including Desktop Engineering, Server Teams, and Help Desks, among others -- would benefit from Nessus data.

## The Solution

TenaPull is a configurable Java application which fetches and processes the data from one or more Nessus APIs, and converts it into JSON ouputs that are usable by Splunk, and possibly by other NoSQL databases.  TenaPull uses a MySQL database with the Hibernate ORM for its local cache of data, Jackson for serialization/deserialization, and the Jersey client to reach the Nessus API.

For a complete demonstration, click the below picture which will take you to a YouTube video.

<a href="https://www.youtube.com/watch?v=aHoMRjRHHrc" rel="Demo video">![Demo video](https://raw.githubusercontent.com/billyJoePiano/TenaPull/master/screenshots/Splunk-table%20(best%20format%20for%20viewing).png)</a>



## Resources


### TenaPull Documentation

- [Install and run instructions](installAndRun.md)
- [Example configuration](example-config.properties)
- [JavaDocs](https://billyjoepiano.github.io/TenaPull/)


### Tenable / Nessus

- API intro: https://docs.tenable.com/tenablesc/Content/APIKeyAuthentication.htm
- API documentation: https://docs.tenable.com/tenablesc/api/index.htm
- API best practices: https://docs.tenable.com/tenablesc/api_best_practices/Content/ScApiBestPractices/AboutScApiBestPrac.htm


### Importing a custom Certificate Authority into Java

- See: https://stackoverflow.com/questions/6659360/how-to-solve-javax-net-ssl-sslhandshakeexception-error/6742204#6742204
    - NOTE: I have NOT tried this, and cannot guarantee it will work
    - There is always the option of disabling SSL validation (see `example-config.properties`)
    - Or you could obtain a certificate signed by a recognized CA for your Nessus installation
