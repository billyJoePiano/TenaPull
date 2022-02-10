# 2-2-22 Meeting with Security Team Stakeholders, Enterprise Java Project

I met with Cory Chrisinger, Zach Ward, and Jesse LaGrew.  The purpose of this meeting was to decide upon an independent project for me (Bill Anderson) to undertake which could both be used for my Enterprise Java class, and which would benefit the Security Team where I intern.

## Project ideas

- Mobile dashboard for Splunk https://apps.apple.com/us/app/splunk-mobile/id1420299852 -- cool but may not involve much Java???
- Nessus dashboard -- integrate with Splunk??? YES
- PWM (Identity service?) ??? XX  Probably not do-able???


## Nessus App, ideas

- Interface between Splunk and Nessus is broken, stopped working
- No one outside of the Security Team can read reports
- Single user for login to Nessus ... need to have a way for users to login individually with custom dashboards
- Individuals want to be able to scan themselves, but can't initiate that scan
- Want Data in Splunk to correlate with other findings/data, and give data access to other individuals with Splunk access
- Need Nessus vulnerability information to be accessible & searchable
- Need to start baselining asset inventory from network scans, and comparing to known assets
- Want to see new vulnerabilities and devices that appear

Jesse La Grew also sent this list of problems / pain points:

- Unable to see scans for my devices
- Unable to update the list of devices that I own
- Scan reports are too detailed and not easy to consume/read
- Do not see any changes in scan results over time
- Data is not available within Splunk
- Sign-ins required for individuals to see reports - would like people to see reports without logging into Nessus
- Users cannot see scan settings or schedules set up  (which subnets or devices are being scanned at any given time? Is it happening now?)
