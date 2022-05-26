# TenaPull Installation checklist

- If you've already installed & built TenaPull, and just want to add another scanner, then skip to [Configure TenaPull for your Nessus Installation](#configure-tenapull-for-your-nessus-installation)

## Install and build TenaPull


1. Pull down the TenaPull repo
    - [ ] `sudo yum/apt/dnf install git` (if it's not already installed)
    - [ ] `git clone https://github.com/billyJoePiano/TenaPull.git`


2. Install and configure MySQL:
    - [ ] `sudo yum/apt/dnf install mysqld`
    - [ ] Run the `mysql_secure_installation` script
    - [ ] Setup the root account for the DB, and save the credentials to be used in TenaPull configuration later
    - [ ] Configure the timezone in mySQL.
        - You will first need to populate the DB's timezone table using the timezone_posix.sql script at the root of this repo
            - `mysql -u root -p < timezone_posix.sql`
        - Then enter the mysql console with `mysql -u root -p` and enter `SET time_zone = 'US/Central'` (e.g. for Central Time).
        - More information here: https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html


3. Install and configure the Java JDK (minimum version 13, but 17 is suggested)
   - [ ] `sudo yum/apt install java-17-openjdk-devel`
   - [ ] If there is another JDK version on the system (8 is standard) it may be necessary to configure the default JDK using `/usr/sbin/alternatives --config java`
     - See also: https://access.redhat.com/documentation/en-us/jboss_enterprise_application_platform/6/html/administration_and_configuration_guide/configure_the_default_jdk_on_red_hat_enterprise_linux 
   - [ ] Confirm the home directory of the JDK, and edit the two scripts `tenapull` and `build.sh` (in the root of the repo) if necessary. 
     - The `$JAVA_HOME` environmental variable must set to the correct directory 
     - Default is `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.3.0.6-2.el8_5.x86_64`
     

4. Install Maven (This is used by TenaPull for dependency management during build and runtime)
    - [ ] `sudo yum/apt/dnf install maven`


5. Build TenaPull
   - [ ] From your local root of the repo, run `./build.sh`
     - If you are having issues with the build, check the JDK version `mvn` is using.
     - You can also manually run `mvn clean install -X` (-X for debugging), but make sure you've exported the correct `$JAVA_HOME` enviromental  variable


## Configure TenaPull for your Nessus Installation

6. Make a copy of `example-config.properties`, and configure it for your Nessus Installation and DB.
   - Configuration instructions are included in `example-config.properties`
   - Best practice is to name the config file after your Nessus hostname.
   - For example `mynessusinstallation1.mylan.local` would have configuration file `mynessusinstallation1.properties`
   - `db.url.name` and `output.dir` configs could also reflect the hostname (e.g. `mynessusintallation1` in the above example)
   - You may have as many configuration files as you like (one for each Nessus Scanner!) as long as they each have unique `output.dir` and `db.url.name` values
   
    
7. Create the database associated with the configuration file you just created
    - [ ] Login to mysql console with `mysql -u root -p`
    - [ ] Enter `CREATE DATABASE <db.url.name>;` where &lt;db.url.name&gt; is the name configured in the .properties file.
      - Don't forget about the semi-colon at the end!
      - Exit the mysql console (`exit`)
    - [ ] We will now build the table structure of the database.  Enter `./tenapull <config-name> reset`
      - &lt;config-name&gt; is the name of the configuration file created above
      - The `.properties` extension can be omitted from the command-line argument, but the config file itself must have a `.properties` extension
      - You will be prompted to confirm the "reset" of the database.  Enter `YES` (all caps) to confirm

## Run TenaPull !

    ./tenapull <config-name>

### Yep, it's really that easy!

A few notes:

- The `.properties` extension may be omitted from the command-line argument, but the config file itself must have a `.properties` extension
- When searching for the config file, the working directory is checked first.  If it cannot be found in the working directory,
TenaPull will attempt to find it as a resource on the Java classpath (the `target/classes` directory and all subdirectories)


## Configure your Splunk Universal Forwarder and Heavy Forwarder/Indexer

- [Example `local/inputs.conf` entry](inputs.conf) for your Splunk Universal Forwarder on the host which runs TenaPull
- [Example `local/props.conf` entry](props.conf) for your Splunk Heavy Forwarder or Indexer
- See also:
  - https://docs.splunk.com/Documentation/Splunk/8.2.6/Admin/Inputsconf
  - https://docs.splunk.com/Documentation/Splunk/8.2.6/Admin/Propsconf


## Importing a custom Certificate Authority into Java (optional)

- See: https://stackoverflow.com/questions/6659360/how-to-solve-javax-net-ssl-sslhandshakeexception-error/6742204#6742204
    - NOTE: I have NOT tried this, and cannot guarantee it will work
    - There is always the option of disabling SSL validation (see `example-config.properties`)
    - Or you could obtain a certificate signed by a recognized CA for your Nessus installation


## Reformat outputs (optional)

If you have .json output files that need to be reformatted -- for example, you need to change the `scanner` field value, or
truncate excessively long string fields -- you can run a TenaPull reformat job.

- [ ] First, rename the directory with the incorrectly-formatted outputs by adding a `.old` extension to it.
    - For example `mynessusinstallation1/` becomes `mynessusinstallation1.old/`


- [ ] Change the configuration file to reflect what you'd like the new format to look like (e.g. `output.truncate`, `output.scanner`, `output.scanner.omit`)
    - To change the order of the fields at the root of each host vulnerability record, edit the source code in `src/main/java/tenapull/data/entity/splunk/HostVulnerabilityOutput.java`
      - Find the `@JsonPropertyOrder` annotation prior to the class declaration.  The array of strings here dictates the order that the properties will be serialized.
      - Unfortunately, the root of the record is the only object where TenaPull can re-arrange the field order ***during a reformat job*** (sorry... maybe in version 2.0?)
      - However, you could change the order of fields in other entities for all outputs going forward.  Add a similar `@JsonPropertyOrder` annotation to that entity.
      - If you modified the source code, then rebuild TenaPull with `./build.sh` (see step 5. above)
      - DISCLAIMER: No promises of correct functionality if you modify the source code.  PROCEED WITH CAUTION 
      - No need to rebuild if you only altered the configuration file


- [ ] To reformat the old outputs with the new configuration, run `tenapull <config-name> reformat`
  - You will be prompted to confirm.  Enter `YES` (all caps)
  - TenaPull will parse and reformat all of the .json files from the `.old` directory, and place the reformatted files into a new directory with the configured `output.dir` name
  - Note that the `.old` directory must match the configured `output.dir` name, except with the `.old` extension
  - The old .json files will NOT be modified or deleted.  You may want to save them just in case something went wrong with the reformat job
