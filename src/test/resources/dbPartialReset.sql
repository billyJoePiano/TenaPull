-- Drop tables in reverse order from create

-- Join tables
drop table if exists `scan_host_vulnerability`;
drop table if exists `scan_vulnerability`;
drop table if exists `scan_plugin_host`;
drop table if exists `scan_host_severity_level_count`;
drop table if exists `scan_info_severity_base_selection`;
drop table if exists `scan_info_acl`;
drop table if exists `plugin_attributes_see_also`;
drop table if exists `plugin_ref_information_value`;
drop table if exists `plugin_attributes_ref_information`;
drop table if exists `index_response_scan`;
drop table if exists `index_response_folder`;

-- Scan and host data
drop table if exists `host_vulnerability_output`;
drop table if exists `host_output`;
drop table if exists `scan_host_info`;
drop table if exists `scan_host_response`;
drop table if exists `scan_history`;
drop table if exists `scan_remediation`;
drop table if exists `scan_remediations_summary`;
drop table if exists `scan_plugin`;
drop table if exists `scan_host`;
drop table if exists `scan_info`;
drop table if exists `scan_response`;
drop table if exists `scan`;
drop table if exists `folder`;
drop table if exists `index_response`;

--
-- SCAN DATA TABLES
--

create table index_response (
    id int primary key auto_increment,
    timestamp timestamp null,
    _extra_json int null,
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);

create table folder (
    id           int          not null,
    name         varchar(255) null,
    type         varchar(255) null,
    default_tag  int          null,
    custom       int          null,
    unread_count int          null,
    _extra_json  int null,
    constraint folder_pk primary key (id),
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);

create table scan (
    id              int          not null primary key,
    name            varchar(255) null,
    uuid_id         int          null,
    folder_id       int          null,
    owner_id        int          null, -- SQL owner id (surrogate key) in the varchar lookup table -- NOT nessus owner id
    scan_type_id    int          null,
    rrules          varchar(255) null,
    `read`          boolean      null,
    shared          boolean      null,
    enabled         boolean      null,
    control         boolean      null,
    user_permissions int         null,
    status_id       int          null,
    creation_date   timestamp    null,
    start_time      varchar(255) null,
    last_modification_date  timestamp null,
    timezone_id     int          null,
    live_results    int          null,
    _extra_json  int null,
    constraint scan_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade,
    -- constraint scan_folder_id_fk foreign key (folder_id) references folder (id) on delete cascade on update cascade,
    constraint scan_scan_owner_id_fk foreign key (owner_id) references scan_owner (id) on update cascade,
    constraint scan_scan_type_id_fk foreign key (scan_type_id) references scan_type (id) on update cascade,
    constraint scan_status_id foreign key (status_id) references scan_status (id) on update cascade,
    constraint scan_timezone_id_fk foreign key (timezone_id) references timezone (id) on update cascade,
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);


create table scan_response (
    id int PRIMARY KEY,
    timestamp timestamp not null,
    threat_level int null, -- JSON is actually nested in the 'prioritzation' container that wraps the "plugins" array,
                           -- but there is no db entity for the prioritization container
    _extra_json int null,
    constraint scan_response_scan_id_fk foreign key (id) references scan (id) on update cascade,
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);


create table scan_info (
    id int primary key,
    uuid_id int null,
    name varchar(255) null,
    folder_id int null,
    scan_type_id int null,
    edit_allowed bool null,
    scan_group int null,
    scan_targets_id int null,
    scanner_start timestamp null,
    scanner_end timestamp null,
    oses_found bool null,
    host_count int null,
    haskb bool null,
    exploitable_vulns bool null,
    hosts_vulns bool null,
    migrated int null,
    timestamp timestamp null,
    policy_id int null,
    year_old_vulns bool null,
    top10 bool null,
    pci_can_upload bool null,
    node_name varchar(255) null,
    scan_start timestamp null,
    scan_end timestamp null,
    has_audit_trail bool null,
    control bool null,
    scanner_id int null,
    unsupported_software bool null,
    object_id int null,
    license_id int null,
    no_target bool null,
    node_host varchar(255), -- ??? not sure about data type of this API field
    severity_processed varchar(255) null, -- timestamp formatted as string -- "yyyyddmmhhmm"
    node_id int null,
    alt_targets_used bool null,
    user_permissions int null,
    policy_template_uuid_id int null,
    known_accounts bool null,
    offline bool null,
    status_id int null,
    current_severity_base_id int null,
    selected_severity_base_id int null,
    _extra_json  int null,

    constraint scan_info_scan_response_id_fk foreign key (id) references scan_response (id) on update cascade,
    constraint scan_info_folder_id_fk foreign key (folder_id) references folder (id),
    constraint scan_info_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade,
    constraint scan_info_scan_type_id_fk foreign key (scan_type_id) references scan_type(id) on update cascade,
    -- constraint scan_info_scan_group_id_fk foreign key (scan_group_id) references scan_group (id)  on update cascade,
    constraint foreign key (scan_targets_id) references scan_targets (id) on update cascade,
    constraint scan_info_policy_id_fk foreign key (policy_id) references scan_policy (id) on update cascade,
    constraint scan_info_scanner_id_fk foreign key (scanner_id) references scanner (id) on update cascade,
    constraint scan_info_license_id_fk foreign key (license_id) references license (id) on update cascade,
    constraint scan_info_policy_template_uuid_id_fk foreign key (policy_template_uuid_id) references policy_template_uuid (id) on update cascade,
    constraint scan_info_status_id_fk foreign key (status_id) references scan_status (id) on update cascade,
    constraint scan_info_current_severity_base_id_fk foreign key (current_severity_base_id) references severity_base (id) on update cascade,
    constraint scan_info_selected_severity_base_id_fk foreign key (selected_severity_base_id) references severity_base (id) on update cascade,
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);


create table scan_host (
    id int PRIMARY KEY AUTO_INCREMENT,
    scan_id int not null,
    host_id int null,
    total_checks_considered int null,
    num_checks_considered int null,
    scan_progress_total int null,
    scan_progress_current int null,
    host_index int null,
    score int null,
    progress varchar(255) null,
    offline_critical int null,
    offline_high int null,
    offline_medium int null,
    offline_low int null,
    offline_info int null,
    critical int null,
    high int null,
    medium int null,
    low int null,
    info int null,
    severity int null,
    hostname_id int null,
    _extra_json int null,
    constraint scan_host_scan_response_id_fk foreign key (scan_id) references scan_response (id),
    constraint scan_host__host_scan_unique unique (scan_id, host_id),
    constraint foreign key (hostname_id) references hostname (id),
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);

create table scan_plugin (
    -- sort-of join table between scan_response and plugin, while also providing a join-point for plugin_host to join
    -- with plugin and scan_response via this record.  Plus has "host_count" field.  Thus, surrogate key needed...
    id int primary key auto_increment,
    scan_id int not null, -- effectively, also scan_id
    plugin_id int null,
    host_count int null,
    -- extra json goes into the plugin lookup
    __order_for_scan_plugin int not null default 0,
    constraint unique (scan_id, plugin_id),
    constraint foreign key (scan_id) references scan_response(id), -- effectively, also scan_response(id) and scan_prioritization(id)
    constraint foreign key (plugin_id) references plugin(id) on update cascade
);

create table scan_remediations_summary (
    id int PRIMARY KEY,
    num_hosts int null,
    num_cves int null,
    num_impacted_hosts int null,
    num_remediated_cves int null,
    _extra_json int null,
    constraint scan_remediations_summary_scan_response_id_fk foreign key (id) references scan_response(id),
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);

create table scan_remediation (
    id int primary key auto_increment,
    scan_id int not null,
    remediation_id int not null,
    hosts int null,
    vulns int null,
    __order_for_scan_remediation int not null default 0,
    constraint foreign key (scan_id) references scan_response (id) on update cascade,
    constraint foreign key (remediation_id) references remediation_details (id) on update cascade
);

create table scan_history (
    id int PRIMARY KEY AUTO_INCREMENT,
    scan_id int not null,
    history_id int null,
    alt_targets_used bool null,
    scheduler int null,
    node_name varchar(255) null,
    node_host varchar(255) null,
    scan_group int null,
    node_id int null,
    schedule_type_id int null,
    status_id int null,
    scan_type_id int null,
    uuid_id int null,
    last_modification_date timestamp null,
    creation_date timestamp null,
    owner_id int null, -- probably a foreign key to scan_owner_id (id)
                -- but without knowing the owner name or if the corresponding record in scan_owner_id
                -- has been created, it is possible a FK constraint would fail ???
    -- __order_for_scan_response int not null, -- they seem to be ordered naturally by history_id
    _extra_json int null,

    constraint scan_history_scan_response_id_fk foreign key (scan_id) references scan_response (id),
    -- constraint scan_history_scan_group_id_fk foreign key (scan_group_id) references scan_group (id)  on update cascade,
    constraint scan_history_status_id_fk foreign key (status_id) references scan_status (id) on update cascade,
    constraint foreign key (schedule_type_id) references scan_schedule_type (id) on update cascade,
    constraint scan_history_scan_type_id_fk foreign key (scan_type_id) references scan_type (id) on update cascade,
    constraint scan_history_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade,
    constraint unique (scan_id, history_id),
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);

create table scan_host_response (
    id int primary key,
    timestamp timestamp null,
    _extra_json int null,
    constraint foreign key (id) references scan_host(id),
    constraint foreign key (_extra_json) references extra_json(id) on update cascade
);

create table scan_host_info (
    id int primary key,
    operating_system_id int null,
    host_ip_id int null,
    host_fqdn_id int null,
    netbios_name_id int null,
    host_start varchar(255) null,
    host_end varchar(255) null,
    _extra_json int null,
    constraint foreign key (id) references scan_host_response (id),
    constraint foreign key (operating_system_id) references operating_system (id) on update cascade,
    constraint foreign key (host_fqdn_id) references host_fqdn (id) on update cascade,
    constraint foreign key (netbios_name_id) references host_netbios_name (id) on update cascade,
    constraint foreign key (host_ip_id) references host_ip (id) on update cascade,
    constraint foreign key (_extra_json) references extra_json (id) on update cascade
);

create table host_output (
    id int primary key,
    scan_timestamp timestamp null,
    output_timestamp timestamp null,
    filename varchar(255) null,
    constraint foreign key (id) references scan_host_response (id)
);

create table host_vulnerability_output (
    id int primary key auto_increment,
    scan_timestamp timestamp null,
    host_id int not null,
    vulnerability_id int not null,
    scan_plugin_id int null,
    plugin_best_guess_id int null,
    -- __order_for_host_vulnerability_output int null,
    constraint foreign key (host_id) references host_output (id),
    constraint foreign key (vulnerability_id) references vulnerability (id),
    constraint foreign key (scan_plugin_id) references scan_plugin (id),
    constraint foreign key (plugin_best_guess_id) references plugin (id),
    constraint unique (host_id, vulnerability_id)
);


--
-- JOIN TABLES
--

create table index_response_folder (
    response_id int not null,
    folder_id int not null,
    __order_for_index_response_folder int not null,
    primary key (response_id, __order_for_index_response_folder),
    constraint foreign key (response_id) references index_response(id) on update cascade,
    constraint foreign key (folder_id) references folder(id) on update cascade
);

create table index_response_scan (
    response_id int not null,
    scan_id int not null,
    __order_for_index_response_scan int not null,
    primary key (response_id, __order_for_index_response_scan),
    constraint foreign key (response_id) references index_response(id) on update cascade,
    constraint foreign key (scan_id) references scan(id) on update cascade
);

create table plugin_attributes_ref_information (
    attributes_id int not null,
    ref_id int not null,
    __order_for_plugin_attributes_ref_information int not null,
    constraint primary key (attributes_id, __order_for_plugin_attributes_ref_information),
    constraint foreign key (attributes_id) references plugin_attributes (id) on update cascade,
    constraint foreign key (ref_id) references plugin_ref_information (id) on update cascade
);

create table plugin_ref_information_value (
    information_id int not null,
    value_id int not null,
    __order_for_plugin_ref_value int not null,
    constraint primary key (information_id, __order_for_plugin_ref_value),
    constraint foreign key (information_id) references plugin_ref_information (id) on update cascade,
    constraint foreign key (value_id) references plugin_ref_value (id) on update cascade
);

create table plugin_attributes_see_also (
    attributes_id int not null,
    see_also_id int not null,
    __order_for_plugin_attributes_see_also int not null,
    constraint primary key (attributes_id, __order_for_plugin_attributes_see_also),
    constraint foreign key (attributes_id) references plugin_attributes (id) on update cascade,
    constraint foreign key (see_also_id) references plugin_see_also (id) on update cascade
);

create table scan_info_acl (
    scan_id int not null,
    acl_id  int not null,
    __order_for_scan_info int not null,
    constraint primary key (scan_id, __order_for_scan_info),
    constraint foreign key (acl_id) references acl (id) on update cascade,
    constraint foreign key (scan_id) references scan_info (id) on update cascade
);

create table scan_info_severity_base_selection (
    scan_id int not null,
    severity_base_id int not null,
    __order_for_scan_info int not null,
    constraint primary key (scan_id, __order_for_scan_info),
    constraint foreign key (scan_id) references scan_info (id) on update cascade,
    constraint foreign key (severity_base_id) references severity_base (id) on update cascade
);

create table scan_host_severity_level_count (
    scan_host_id int not null,
    severity_id int not null,
    -- natural ordering by severity_count's severity_level
    -- _extra_json int null,
    constraint primary key (scan_host_id, severity_id),
    constraint foreign key (scan_host_id) references scan_host (id) on update cascade,
    constraint foreign key (severity_id) references severity_level_count (id) on update cascade
);

create table scan_plugin_host (
    scan_plugin_id int not null,
    plugin_host_id int not null,
    __order_for_scan_plugin_host int not null,
    constraint primary key (scan_plugin_id, __order_for_scan_plugin_host),
    constraint foreign key (scan_plugin_id) references scan_plugin (id) on update cascade,
    constraint foreign key (plugin_host_id) references plugin_host (id) on update cascade
);

create table scan_vulnerability (
    vulnerability_id int not null,
    scan_id int not null,
    __order_for_scan_response_vulnerability int not null,
    constraint primary key (scan_id, __order_for_scan_response_vulnerability),
    constraint foreign key (scan_id) references scan_response (id) on update cascade,
    constraint foreign key (vulnerability_id) references vulnerability (id) on update cascade
);

create table scan_host_vulnerability (
    host_id int not null,
    vulnerability_id int not null,
    __order_for_scan_host_vulnerability int not null,
    constraint primary key (host_id, __order_for_scan_host_vulnerability),
    constraint foreign key (host_id) references scan_host_response (id) on update cascade,
    constraint foreign key (vulnerability_id) references vulnerability (id) on update cascade
);





-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: localhost    Database: TenaPull
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


--
-- Dumping data for table `folder`
--

LOCK TABLES `folder` WRITE;
/*!40000 ALTER TABLE `folder` DISABLE KEYS */;
INSERT INTO `folder` VALUES (2,'Trash','trash',0,0,NULL,NULL),(3,'My Scans','main',1,0,0,NULL);
/*!40000 ALTER TABLE `folder` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `host_output`
--

LOCK TABLES `host_output` WRITE;
/*!40000 ALTER TABLE `host_output` DISABLE KEYS */;
INSERT INTO `host_output` VALUES (1,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_2.json'),(2,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_3.json'),(3,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_4.json'),(4,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_5.json'),(5,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_6.json'),(6,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_7.json'),(7,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_8.json'),(8,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_9.json'),(9,'2022-02-26 23:35:49','2022-05-11 14:44:47','demo-output/2022-02-26_17.35.49_5_10.json'),(10,'2022-02-26 23:35:49','2022-05-11 14:44:50','demo-output/2022-02-26_17.35.49_5_11.json'),(11,'2022-02-26 23:35:49','2022-05-11 14:44:50','demo-output/2022-02-26_17.35.49_5_12.json'),(12,'2022-02-26 23:35:49','2022-05-11 14:44:50','demo-output/2022-02-26_17.35.49_5_13.json'),(13,'2022-02-26 23:35:49','2022-05-11 14:44:49','demo-output/2022-02-26_17.35.49_5_14.json'),(14,'2022-02-28 21:32:09','2022-05-11 14:45:17','demo-output/2022-02-28_15.32.09_22_2.json'),(15,'2022-02-28 21:32:09','2022-05-11 14:45:18','demo-output/2022-02-28_15.32.09_22_3.json'),(16,'2022-02-28 21:32:09','2022-05-11 14:45:18','demo-output/2022-02-28_15.32.09_22_4.json'),(17,'2022-02-28 21:32:09','2022-05-11 14:45:19','demo-output/2022-02-28_15.32.09_22_5.json'),(18,'2022-02-28 21:32:09','2022-05-11 14:45:19','demo-output/2022-02-28_15.32.09_22_6.json'),(19,'2022-02-28 21:32:09','2022-05-11 14:45:20','demo-output/2022-02-28_15.32.09_22_7.json'),(20,'2022-02-28 21:32:09','2022-05-11 14:45:19','demo-output/2022-02-28_15.32.09_22_8.json'),(21,'2022-02-28 21:32:09','2022-05-11 14:45:20','demo-output/2022-02-28_15.32.09_22_9.json'),(22,'2022-02-28 21:32:09','2022-05-11 14:45:20','demo-output/2022-02-28_15.32.09_22_10.json'),(23,'2022-02-28 20:52:58','2022-05-11 14:45:05','demo-output/2022-02-28_14.52.58_11_2.json'),(24,'2022-02-28 20:52:58','2022-05-11 14:45:18','demo-output/2022-02-28_14.52.58_11_3.json'),(25,'2022-02-28 20:52:58','2022-05-11 14:45:16','demo-output/2022-02-28_14.52.58_11_4.json'),(26,'2022-02-28 20:52:58','2022-05-11 14:45:16','demo-output/2022-02-28_14.52.58_11_5.json'),(27,'2022-02-26 23:58:29','2022-05-11 14:44:56','demo-output/2022-02-26_17.58.29_8_2.json'),(28,'2022-02-26 23:58:29','2022-05-11 14:44:56','demo-output/2022-02-26_17.58.29_8_3.json'),(29,'2022-02-26 23:58:29','2022-05-11 14:44:51','demo-output/2022-02-26_17.58.29_8_4.json'),(30,'2022-02-26 23:58:29','2022-05-11 14:45:02','demo-output/2022-02-26_17.58.29_8_5.json'),(31,'2022-02-26 23:58:29','2022-05-11 14:44:53','demo-output/2022-02-26_17.58.29_8_6.json'),(32,'2022-02-26 23:58:29','2022-05-11 14:45:17','demo-output/2022-02-26_17.58.29_8_7.json'),(33,'2022-02-26 23:58:29','2022-05-11 14:45:08','demo-output/2022-02-26_17.58.29_8_8.json'),(34,'2022-02-26 23:58:29','2022-05-11 14:45:25','demo-output/2022-02-26_17.58.29_8_9.json'),(35,'2022-02-26 23:58:29','2022-05-11 14:44:52','demo-output/2022-02-26_17.58.29_8_10.json'),(36,'2022-02-26 23:58:29','2022-05-11 14:45:07','demo-output/2022-02-26_17.58.29_8_11.json'),(37,'2022-02-26 23:58:29','2022-05-11 14:45:08','demo-output/2022-02-26_17.58.29_8_12.json'),(38,'2022-02-26 23:58:29','2022-05-11 14:45:13','demo-output/2022-02-26_17.58.29_8_13.json'),(39,'2022-02-26 23:58:29','2022-05-11 14:45:12','demo-output/2022-02-26_17.58.29_8_14.json'),(40,'2022-02-28 21:13:34','2022-05-11 14:45:47','demo-output/2022-02-28_15.13.34_15_2.json'),(41,'2022-02-28 21:13:34','2022-05-11 14:45:41','demo-output/2022-02-28_15.13.34_15_3.json'),(42,'2022-02-28 21:13:34','2022-05-11 14:45:21','demo-output/2022-02-28_15.13.34_15_5.json');
/*!40000 ALTER TABLE `host_output` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `host_vulnerability_output`
--

LOCK TABLES `host_vulnerability_output` WRITE;
/*!40000 ALTER TABLE `host_vulnerability_output` DISABLE KEYS */;
INSERT INTO `host_vulnerability_output` VALUES (1,'2022-02-26 23:35:49',8,661,NULL,NULL),(2,'2022-02-26 23:35:49',8,662,NULL,NULL),(3,'2022-02-26 23:35:49',4,661,NULL,NULL),(4,'2022-02-26 23:35:49',4,662,NULL,NULL),(5,'2022-02-26 23:35:49',1,661,NULL,NULL),(6,'2022-02-26 23:35:49',1,662,NULL,NULL),(7,'2022-02-26 23:35:49',2,661,NULL,NULL),(8,'2022-02-26 23:35:49',2,662,NULL,NULL),(9,'2022-02-26 23:35:49',5,661,NULL,NULL),(10,'2022-02-26 23:35:49',5,662,NULL,NULL),(11,'2022-02-26 23:35:49',9,661,NULL,NULL),(12,'2022-02-26 23:35:49',9,662,NULL,NULL),(13,'2022-02-26 23:35:49',6,661,NULL,NULL),(14,'2022-02-26 23:35:49',6,662,NULL,NULL),(15,'2022-02-26 23:35:49',7,661,NULL,NULL),(16,'2022-02-26 23:35:49',7,662,NULL,NULL),(17,'2022-02-26 23:35:49',3,661,NULL,NULL),(18,'2022-02-26 23:35:49',3,662,NULL,NULL),(19,'2022-02-26 23:35:49',13,661,NULL,NULL),(20,'2022-02-26 23:35:49',13,662,NULL,NULL),(21,'2022-02-26 23:35:49',12,661,NULL,NULL),(22,'2022-02-26 23:35:49',12,662,NULL,NULL),(23,'2022-02-26 23:35:49',10,661,NULL,NULL),(24,'2022-02-26 23:35:49',10,662,NULL,NULL),(25,'2022-02-26 23:35:49',11,661,NULL,NULL),(26,'2022-02-26 23:35:49',11,662,NULL,NULL),(27,'2022-02-26 23:58:29',29,679,NULL,NULL),(28,'2022-02-26 23:58:29',29,680,NULL,NULL),(29,'2022-02-26 23:58:29',29,706,NULL,NULL),(30,'2022-02-26 23:58:29',35,693,NULL,NULL),(31,'2022-02-26 23:58:29',35,694,NULL,NULL),(32,'2022-02-26 23:58:29',35,695,NULL,NULL),(33,'2022-02-26 23:58:29',35,722,NULL,NULL),(34,'2022-02-26 23:58:29',35,723,NULL,NULL),(35,'2022-02-26 23:58:29',31,674,NULL,NULL),(36,'2022-02-26 23:58:29',31,675,NULL,NULL),(37,'2022-02-26 23:58:29',31,686,NULL,NULL),(38,'2022-02-26 23:58:29',31,687,NULL,NULL),(39,'2022-02-26 23:58:29',31,688,NULL,NULL),(40,'2022-02-26 23:58:29',31,699,NULL,NULL),(41,'2022-02-26 23:58:29',31,717,NULL,NULL),(42,'2022-02-26 23:58:29',31,735,NULL,NULL),(43,'2022-02-26 23:58:29',31,736,NULL,NULL),(44,'2022-02-26 23:58:29',28,676,NULL,NULL),(45,'2022-02-26 23:58:29',28,677,NULL,NULL),(46,'2022-02-26 23:58:29',28,678,NULL,NULL),(47,'2022-02-26 23:58:29',28,681,NULL,NULL),(48,'2022-02-26 23:58:29',28,682,NULL,NULL),(49,'2022-02-26 23:58:29',28,683,NULL,NULL),(50,'2022-02-26 23:58:29',28,684,NULL,NULL),(51,'2022-02-26 23:58:29',28,685,NULL,NULL),(52,'2022-02-26 23:58:29',28,702,NULL,NULL),(53,'2022-02-26 23:58:29',28,715,NULL,NULL),(54,'2022-02-26 23:58:29',28,739,NULL,NULL),(55,'2022-02-26 23:58:29',28,740,NULL,NULL),(56,'2022-02-26 23:58:29',28,743,NULL,NULL),(57,'2022-02-26 23:58:29',28,763,NULL,NULL),(58,'2022-02-26 23:58:29',27,663,NULL,NULL),(59,'2022-02-26 23:58:29',27,664,NULL,NULL),(60,'2022-02-26 23:58:29',27,665,NULL,NULL),(61,'2022-02-26 23:58:29',27,666,NULL,NULL),(62,'2022-02-26 23:58:29',27,667,NULL,NULL),(63,'2022-02-26 23:58:29',27,668,NULL,NULL),(64,'2022-02-26 23:58:29',27,669,NULL,NULL),(65,'2022-02-26 23:58:29',27,670,NULL,NULL),(66,'2022-02-26 23:58:29',27,671,NULL,NULL),(67,'2022-02-26 23:58:29',27,672,NULL,NULL),(68,'2022-02-26 23:58:29',27,692,NULL,NULL),(69,'2022-02-26 23:58:29',27,724,NULL,NULL),(70,'2022-02-26 23:58:29',27,725,NULL,NULL),(71,'2022-02-26 23:58:29',27,726,NULL,NULL),(72,'2022-02-26 23:58:29',27,727,NULL,NULL),(73,'2022-02-26 23:58:29',27,728,NULL,NULL),(74,'2022-02-26 23:58:29',27,729,NULL,NULL),(75,'2022-02-26 23:58:29',27,756,NULL,NULL),(76,'2022-02-26 23:58:29',30,673,NULL,NULL),(77,'2022-02-26 23:58:29',30,689,NULL,NULL),(78,'2022-02-26 23:58:29',30,690,NULL,NULL),(79,'2022-02-26 23:58:29',30,691,NULL,NULL),(80,'2022-02-26 23:58:29',30,698,NULL,NULL),(81,'2022-02-26 23:58:29',30,718,NULL,NULL),(82,'2022-02-26 23:58:29',30,719,NULL,NULL),(83,'2022-02-26 23:58:29',30,732,NULL,NULL),(84,'2022-02-26 23:58:29',30,733,NULL,NULL),(85,'2022-02-26 23:58:29',30,734,NULL,NULL),(86,'2022-02-26 23:58:29',30,748,NULL,NULL),(87,'2022-02-26 23:58:29',30,749,NULL,NULL),(88,'2022-02-26 23:58:29',30,760,NULL,NULL),(89,'2022-02-26 23:58:29',30,786,NULL,NULL),(90,'2022-02-26 23:58:29',30,787,11,NULL),(91,'2022-02-28 20:52:58',23,812,NULL,NULL),(92,'2022-02-28 20:52:58',23,813,NULL,NULL),(93,'2022-02-28 20:52:58',23,814,NULL,NULL),(94,'2022-02-28 20:52:58',23,815,NULL,NULL),(95,'2022-02-28 20:52:58',23,816,NULL,NULL),(96,'2022-02-28 20:52:58',23,817,NULL,NULL),(97,'2022-02-28 20:52:58',23,818,NULL,NULL),(98,'2022-02-28 20:52:58',23,819,NULL,NULL),(99,'2022-02-28 20:52:58',23,824,NULL,NULL),(100,'2022-02-28 20:52:58',23,825,NULL,NULL),(101,'2022-02-28 20:52:58',23,826,NULL,NULL),(102,'2022-02-28 20:52:58',23,827,NULL,NULL),(103,'2022-02-28 20:52:58',23,828,NULL,NULL),(104,'2022-02-28 20:52:58',23,829,NULL,NULL),(105,'2022-02-28 20:52:58',23,861,NULL,NULL),(106,'2022-02-28 20:52:58',23,862,NULL,NULL),(107,'2022-02-26 23:58:29',36,750,NULL,NULL),(108,'2022-02-26 23:58:29',36,751,NULL,NULL),(109,'2022-02-26 23:58:29',36,752,NULL,NULL),(110,'2022-02-26 23:58:29',36,753,NULL,NULL),(111,'2022-02-26 23:58:29',36,754,NULL,NULL),(112,'2022-02-26 23:58:29',36,758,NULL,NULL),(113,'2022-02-26 23:58:29',36,759,NULL,NULL),(114,'2022-02-26 23:58:29',36,788,NULL,NULL),(115,'2022-02-26 23:58:29',36,790,NULL,NULL),(116,'2022-02-26 23:58:29',36,791,NULL,NULL),(117,'2022-02-26 23:58:29',36,792,NULL,NULL),(118,'2022-02-26 23:58:29',36,793,NULL,NULL),(119,'2022-02-26 23:58:29',36,794,NULL,NULL),(120,'2022-02-26 23:58:29',36,811,NULL,NULL),(121,'2022-02-26 23:58:29',36,830,NULL,NULL),(122,'2022-02-26 23:58:29',36,831,NULL,NULL),(123,'2022-02-26 23:58:29',36,832,NULL,NULL),(124,'2022-02-26 23:58:29',36,860,NULL,NULL),(125,'2022-02-26 23:58:29',36,868,NULL,NULL),(126,'2022-02-26 23:58:29',36,890,NULL,NULL),(127,'2022-02-26 23:58:29',33,700,NULL,NULL),(128,'2022-02-26 23:58:29',33,701,NULL,NULL),(129,'2022-02-26 23:58:29',33,716,NULL,NULL),(130,'2022-02-26 23:58:29',33,737,NULL,NULL),(131,'2022-02-26 23:58:29',33,738,NULL,NULL),(132,'2022-02-26 23:58:29',33,744,NULL,NULL),(133,'2022-02-26 23:58:29',33,745,NULL,NULL),(134,'2022-02-26 23:58:29',33,746,NULL,NULL),(135,'2022-02-26 23:58:29',33,747,NULL,NULL),(136,'2022-02-26 23:58:29',33,761,NULL,NULL),(137,'2022-02-26 23:58:29',33,762,NULL,NULL),(138,'2022-02-26 23:58:29',33,782,NULL,NULL),(139,'2022-02-26 23:58:29',33,783,NULL,NULL),(140,'2022-02-26 23:58:29',33,784,NULL,NULL),(141,'2022-02-26 23:58:29',33,785,NULL,NULL),(142,'2022-02-26 23:58:29',33,795,NULL,NULL),(143,'2022-02-26 23:58:29',33,805,NULL,NULL),(144,'2022-02-26 23:58:29',33,806,NULL,NULL),(145,'2022-02-26 23:58:29',33,807,NULL,NULL),(146,'2022-02-26 23:58:29',33,808,NULL,NULL),(147,'2022-02-26 23:58:29',33,809,NULL,NULL),(148,'2022-02-26 23:58:29',33,810,NULL,NULL),(149,'2022-02-26 23:58:29',33,833,NULL,NULL),(150,'2022-02-26 23:58:29',33,859,NULL,NULL),(151,'2022-02-26 23:58:29',33,869,NULL,NULL),(152,'2022-02-26 23:58:29',33,889,9,NULL),(153,'2022-02-26 23:58:29',37,764,NULL,NULL),(154,'2022-02-26 23:58:29',37,774,NULL,NULL),(155,'2022-02-26 23:58:29',37,775,NULL,NULL),(156,'2022-02-26 23:58:29',37,776,NULL,NULL),(157,'2022-02-26 23:58:29',37,777,NULL,NULL),(158,'2022-02-26 23:58:29',37,778,NULL,NULL),(159,'2022-02-26 23:58:29',37,779,NULL,NULL),(160,'2022-02-26 23:58:29',37,780,NULL,NULL),(161,'2022-02-26 23:58:29',37,781,NULL,NULL),(162,'2022-02-26 23:58:29',37,796,NULL,NULL),(163,'2022-02-26 23:58:29',37,804,NULL,NULL),(164,'2022-02-26 23:58:29',37,834,NULL,NULL),(165,'2022-02-26 23:58:29',37,858,NULL,NULL),(166,'2022-02-26 23:58:29',37,870,NULL,NULL),(167,'2022-02-26 23:58:29',37,871,NULL,NULL),(168,'2022-02-26 23:58:29',37,872,NULL,NULL),(169,'2022-02-26 23:58:29',37,873,NULL,NULL),(170,'2022-02-26 23:58:29',37,874,NULL,NULL),(171,'2022-02-26 23:58:29',37,875,NULL,NULL),(172,'2022-02-26 23:58:29',37,876,NULL,NULL),(173,'2022-02-26 23:58:29',37,877,NULL,NULL),(174,'2022-02-26 23:58:29',37,878,NULL,NULL),(175,'2022-02-26 23:58:29',37,887,NULL,NULL),(176,'2022-02-26 23:58:29',37,897,NULL,NULL),(177,'2022-02-26 23:58:29',39,797,NULL,NULL),(178,'2022-02-26 23:58:29',39,803,NULL,NULL),(179,'2022-02-26 23:58:29',39,835,NULL,NULL),(180,'2022-02-26 23:58:29',39,836,NULL,NULL),(181,'2022-02-26 23:58:29',39,837,NULL,NULL),(182,'2022-02-26 23:58:29',39,838,NULL,NULL),(183,'2022-02-26 23:58:29',39,839,NULL,NULL),(184,'2022-02-26 23:58:29',39,840,NULL,NULL),(185,'2022-02-26 23:58:29',39,841,NULL,NULL),(186,'2022-02-26 23:58:29',39,842,NULL,NULL),(187,'2022-02-26 23:58:29',39,843,NULL,NULL),(188,'2022-02-26 23:58:29',39,844,NULL,NULL),(189,'2022-02-26 23:58:29',39,845,NULL,NULL),(190,'2022-02-26 23:58:29',39,857,NULL,NULL),(191,'2022-02-26 23:58:29',39,879,NULL,NULL),(192,'2022-02-26 23:58:29',39,880,NULL,NULL),(193,'2022-02-26 23:58:29',39,885,NULL,NULL),(194,'2022-02-26 23:58:29',39,886,NULL,NULL),(195,'2022-02-26 23:58:29',39,898,NULL,NULL),(196,'2022-02-26 23:58:29',38,798,NULL,NULL),(197,'2022-02-26 23:58:29',38,801,NULL,NULL),(198,'2022-02-26 23:58:29',38,802,NULL,NULL),(199,'2022-02-26 23:58:29',38,846,NULL,NULL),(200,'2022-02-26 23:58:29',38,847,NULL,NULL),(201,'2022-02-26 23:58:29',38,848,NULL,NULL),(202,'2022-02-26 23:58:29',38,849,NULL,NULL),(203,'2022-02-26 23:58:29',38,856,NULL,NULL),(204,'2022-02-26 23:58:29',38,881,NULL,NULL),(205,'2022-02-26 23:58:29',38,883,NULL,NULL),(206,'2022-02-26 23:58:29',38,884,NULL,NULL),(207,'2022-02-26 23:58:29',38,899,NULL,NULL),(208,'2022-02-26 23:58:29',38,900,NULL,NULL),(209,'2022-02-26 23:58:29',38,902,NULL,NULL),(210,'2022-02-26 23:58:29',38,903,NULL,NULL),(211,'2022-02-26 23:58:29',38,904,NULL,NULL),(212,'2022-02-28 20:52:58',26,925,NULL,NULL),(213,'2022-02-28 20:52:58',26,926,NULL,NULL),(214,'2022-02-28 20:52:58',26,927,NULL,NULL),(215,'2022-02-28 20:52:58',26,928,NULL,NULL),(216,'2022-02-28 20:52:58',26,929,NULL,NULL),(217,'2022-02-28 20:52:58',26,930,NULL,NULL),(218,'2022-02-28 20:52:58',26,931,NULL,NULL),(219,'2022-02-28 20:52:58',26,932,NULL,NULL),(220,'2022-02-28 20:52:58',26,938,NULL,NULL),(221,'2022-02-28 20:52:58',26,939,NULL,NULL),(222,'2022-02-28 20:52:58',26,940,NULL,NULL),(223,'2022-02-28 20:52:58',25,905,NULL,NULL),(224,'2022-02-28 20:52:58',25,906,NULL,NULL),(225,'2022-02-28 20:52:58',25,907,NULL,NULL),(226,'2022-02-28 20:52:58',25,908,NULL,NULL),(227,'2022-02-28 20:52:58',25,921,NULL,NULL),(228,'2022-02-28 20:52:58',25,922,NULL,NULL),(229,'2022-02-28 20:52:58',25,923,NULL,NULL),(230,'2022-02-28 20:52:58',25,924,NULL,NULL),(231,'2022-02-28 20:52:58',25,943,NULL,NULL),(232,'2022-02-28 20:52:58',25,944,NULL,NULL),(233,'2022-02-28 20:52:58',25,945,NULL,NULL),(234,'2022-02-28 21:32:09',14,966,NULL,NULL),(235,'2022-02-28 21:32:09',14,967,NULL,NULL),(236,'2022-02-28 21:32:09',15,975,NULL,NULL),(237,'2022-02-28 21:32:09',15,990,NULL,NULL),(238,'2022-02-28 21:32:09',15,991,1,NULL),(239,'2022-02-28 20:52:58',24,888,NULL,NULL),(240,'2022-02-28 20:52:58',24,896,NULL,NULL),(241,'2022-02-28 20:52:58',24,909,NULL,NULL),(242,'2022-02-28 20:52:58',24,914,NULL,NULL),(243,'2022-02-28 20:52:58',24,915,NULL,NULL),(244,'2022-02-28 20:52:58',24,916,NULL,NULL),(245,'2022-02-28 20:52:58',24,917,NULL,NULL),(246,'2022-02-28 20:52:58',24,918,NULL,NULL),(247,'2022-02-28 20:52:58',24,919,NULL,NULL),(248,'2022-02-28 20:52:58',24,920,NULL,NULL),(249,'2022-02-28 20:52:58',24,946,NULL,NULL),(250,'2022-02-28 20:52:58',24,965,NULL,NULL),(251,'2022-02-28 20:52:58',24,976,NULL,NULL),(252,'2022-02-28 20:52:58',24,989,2,NULL),(253,'2022-02-28 21:32:09',16,995,NULL,NULL),(254,'2022-02-28 21:32:09',16,996,NULL,NULL),(255,'2022-02-28 21:32:09',16,997,NULL,NULL),(256,'2022-02-28 21:32:09',16,998,NULL,NULL),(257,'2022-02-28 21:32:09',18,1014,NULL,NULL),(258,'2022-02-28 21:32:09',17,1000,NULL,NULL),(259,'2022-02-28 21:32:09',17,680,NULL,NULL),(260,'2022-02-28 21:32:09',17,1010,NULL,NULL),(261,'2022-02-28 21:32:09',20,1016,NULL,NULL),(262,'2022-02-28 21:32:09',20,1017,NULL,NULL),(263,'2022-02-28 21:32:09',20,1018,NULL,NULL),(264,'2022-02-28 21:32:09',19,1016,NULL,NULL),(265,'2022-02-28 21:32:09',19,1024,NULL,NULL),(266,'2022-02-28 21:32:09',19,1047,NULL,NULL),(267,'2022-02-28 21:32:09',21,1035,NULL,NULL),(268,'2022-02-28 21:32:09',21,662,NULL,NULL),(269,'2022-02-28 21:32:09',22,1016,NULL,NULL),(270,'2022-02-28 21:32:09',22,1024,NULL,NULL),(271,'2022-02-28 21:32:09',22,1047,NULL,NULL),(272,'2022-02-26 23:58:29',32,703,NULL,NULL),(273,'2022-02-26 23:58:29',32,704,NULL,NULL),(274,'2022-02-26 23:58:29',32,705,NULL,NULL),(275,'2022-02-26 23:58:29',32,707,NULL,NULL),(276,'2022-02-26 23:58:29',32,708,NULL,NULL),(277,'2022-02-26 23:58:29',32,709,NULL,NULL),(278,'2022-02-26 23:58:29',32,710,NULL,NULL),(279,'2022-02-26 23:58:29',32,711,NULL,NULL),(280,'2022-02-26 23:58:29',32,712,NULL,NULL),(281,'2022-02-26 23:58:29',32,713,NULL,NULL),(282,'2022-02-26 23:58:29',32,714,NULL,NULL),(283,'2022-02-26 23:58:29',32,741,NULL,NULL),(284,'2022-02-26 23:58:29',32,742,NULL,NULL),(285,'2022-02-26 23:58:29',32,765,NULL,NULL),(286,'2022-02-26 23:58:29',32,766,NULL,NULL),(287,'2022-02-26 23:58:29',32,767,NULL,NULL),(288,'2022-02-26 23:58:29',32,768,NULL,NULL),(289,'2022-02-26 23:58:29',32,769,NULL,NULL),(290,'2022-02-26 23:58:29',32,770,NULL,NULL),(291,'2022-02-26 23:58:29',32,771,NULL,NULL),(292,'2022-02-26 23:58:29',32,772,NULL,NULL),(293,'2022-02-26 23:58:29',32,773,NULL,NULL),(294,'2022-02-26 23:58:29',32,799,NULL,NULL),(295,'2022-02-26 23:58:29',32,800,NULL,NULL),(296,'2022-02-26 23:58:29',32,850,NULL,NULL),(297,'2022-02-26 23:58:29',32,851,NULL,NULL),(298,'2022-02-26 23:58:29',32,852,NULL,NULL),(299,'2022-02-26 23:58:29',32,853,NULL,NULL),(300,'2022-02-26 23:58:29',32,854,NULL,NULL),(301,'2022-02-26 23:58:29',32,855,NULL,NULL),(302,'2022-02-26 23:58:29',32,882,NULL,NULL),(303,'2022-02-26 23:58:29',32,901,NULL,NULL),(304,'2022-02-26 23:58:29',32,933,NULL,NULL),(305,'2022-02-26 23:58:29',32,934,NULL,NULL),(306,'2022-02-26 23:58:29',32,935,NULL,NULL),(307,'2022-02-26 23:58:29',32,936,NULL,NULL),(308,'2022-02-26 23:58:29',32,937,9,NULL),(309,'2022-02-26 23:58:29',32,971,3,NULL),(310,'2022-02-26 23:58:29',32,972,7,NULL),(311,'2022-02-28 21:13:34',42,941,NULL,NULL),(312,'2022-02-28 21:13:34',42,942,NULL,NULL),(313,'2022-02-28 21:13:34',42,968,NULL,NULL),(314,'2022-02-28 21:13:34',42,969,NULL,NULL),(315,'2022-02-28 21:13:34',42,970,NULL,NULL),(316,'2022-02-28 21:13:34',42,973,NULL,NULL),(317,'2022-02-28 21:13:34',42,974,NULL,NULL),(318,'2022-02-28 21:13:34',42,992,NULL,NULL),(319,'2022-02-28 21:13:34',42,993,NULL,NULL),(320,'2022-02-28 21:13:34',42,994,NULL,NULL),(321,'2022-02-28 21:13:34',42,999,NULL,NULL),(322,'2022-02-28 21:13:34',42,1011,NULL,NULL),(323,'2022-02-28 21:13:34',42,1012,NULL,NULL),(324,'2022-02-28 21:13:34',42,1013,NULL,NULL),(325,'2022-02-28 21:13:34',42,1015,NULL,11),(326,'2022-02-28 21:13:34',42,1029,19,NULL),(327,'2022-02-28 21:13:34',42,1059,NULL,5),(328,'2022-02-28 21:13:34',42,1060,NULL,NULL),(329,'2022-02-28 21:13:34',42,1068,NULL,6),(330,'2022-02-28 21:13:34',42,1084,NULL,4),(331,'2022-02-26 23:58:29',34,696,NULL,NULL),(332,'2022-02-26 23:58:29',34,697,NULL,NULL),(333,'2022-02-26 23:58:29',34,720,NULL,NULL),(334,'2022-02-26 23:58:29',34,721,NULL,NULL),(335,'2022-02-26 23:58:29',34,730,NULL,NULL),(336,'2022-02-26 23:58:29',34,731,NULL,NULL),(337,'2022-02-26 23:58:29',34,755,NULL,NULL),(338,'2022-02-26 23:58:29',34,757,NULL,NULL),(339,'2022-02-26 23:58:29',34,789,NULL,NULL),(340,'2022-02-26 23:58:29',34,820,NULL,NULL),(341,'2022-02-26 23:58:29',34,821,NULL,NULL),(342,'2022-02-26 23:58:29',34,822,NULL,NULL),(343,'2022-02-26 23:58:29',34,823,NULL,NULL),(344,'2022-02-26 23:58:29',34,863,NULL,NULL),(345,'2022-02-26 23:58:29',34,864,NULL,NULL),(346,'2022-02-26 23:58:29',34,865,NULL,NULL),(347,'2022-02-26 23:58:29',34,866,NULL,NULL),(348,'2022-02-26 23:58:29',34,867,NULL,NULL),(349,'2022-02-26 23:58:29',34,891,NULL,NULL),(350,'2022-02-26 23:58:29',34,892,NULL,NULL),(351,'2022-02-26 23:58:29',34,893,NULL,NULL),(352,'2022-02-26 23:58:29',34,894,NULL,NULL),(353,'2022-02-26 23:58:29',34,895,NULL,NULL),(354,'2022-02-26 23:58:29',34,910,NULL,NULL),(355,'2022-02-26 23:58:29',34,911,NULL,NULL),(356,'2022-02-26 23:58:29',34,912,NULL,NULL),(357,'2022-02-26 23:58:29',34,913,NULL,NULL),(358,'2022-02-26 23:58:29',34,953,NULL,NULL),(359,'2022-02-26 23:58:29',34,954,NULL,NULL),(360,'2022-02-26 23:58:29',34,955,NULL,NULL),(361,'2022-02-26 23:58:29',34,956,NULL,NULL),(362,'2022-02-26 23:58:29',34,981,NULL,NULL),(363,'2022-02-26 23:58:29',34,982,NULL,NULL),(364,'2022-02-26 23:58:29',34,983,NULL,NULL),(365,'2022-02-26 23:58:29',34,984,NULL,NULL),(366,'2022-02-26 23:58:29',34,1005,NULL,NULL),(367,'2022-02-26 23:58:29',34,1023,NULL,NULL),(368,'2022-02-26 23:58:29',34,1048,NULL,NULL),(369,'2022-02-26 23:58:29',34,1064,NULL,NULL),(370,'2022-02-26 23:58:29',34,1074,NULL,NULL),(371,'2022-02-26 23:58:29',34,1087,NULL,NULL),(372,'2022-02-26 23:58:29',34,1088,NULL,NULL),(373,'2022-02-26 23:58:29',34,1089,NULL,NULL),(374,'2022-02-26 23:58:29',34,1094,12,NULL),(375,'2022-02-26 23:58:29',34,1095,10,NULL),(376,'2022-02-26 23:58:29',34,1096,8,NULL),(377,'2022-02-26 23:58:29',34,1097,3,NULL),(378,'2022-02-26 23:58:29',34,1103,5,NULL),(379,'2022-02-26 23:58:29',34,1104,NULL,NULL),(380,'2022-02-26 23:58:29',34,1108,6,NULL),(381,'2022-02-26 23:58:29',34,1123,4,NULL),(382,'2022-02-28 21:13:34',41,947,NULL,NULL),(383,'2022-02-28 21:13:34',41,948,NULL,NULL),(384,'2022-02-28 21:13:34',41,959,NULL,NULL),(385,'2022-02-28 21:13:34',41,960,NULL,NULL),(386,'2022-02-28 21:13:34',41,961,NULL,NULL),(387,'2022-02-28 21:13:34',41,962,NULL,NULL),(388,'2022-02-28 21:13:34',41,963,NULL,NULL),(389,'2022-02-28 21:13:34',41,964,NULL,NULL),(390,'2022-02-28 21:13:34',41,977,NULL,NULL),(391,'2022-02-28 21:13:34',41,978,NULL,NULL),(392,'2022-02-28 21:13:34',41,979,NULL,NULL),(393,'2022-02-28 21:13:34',41,988,NULL,NULL),(394,'2022-02-28 21:13:34',41,1001,NULL,NULL),(395,'2022-02-28 21:13:34',41,1009,NULL,NULL),(396,'2022-02-28 21:13:34',41,1019,NULL,NULL),(397,'2022-02-28 21:13:34',41,1020,NULL,NULL),(398,'2022-02-28 21:13:34',41,1021,NULL,NULL),(399,'2022-02-28 21:13:34',41,1026,NULL,NULL),(400,'2022-02-28 21:13:34',41,1027,NULL,NULL),(401,'2022-02-28 21:13:34',41,1028,NULL,NULL),(402,'2022-02-28 21:13:34',41,1030,NULL,NULL),(403,'2022-02-28 21:13:34',41,1031,NULL,NULL),(404,'2022-02-28 21:13:34',41,1032,NULL,NULL),(405,'2022-02-28 21:13:34',41,1033,NULL,NULL),(406,'2022-02-28 21:13:34',41,1034,NULL,NULL),(407,'2022-02-28 21:13:34',41,1054,NULL,NULL),(408,'2022-02-28 21:13:34',41,1055,NULL,NULL),(409,'2022-02-28 21:13:34',41,1056,NULL,NULL),(410,'2022-02-28 21:13:34',41,1057,NULL,NULL),(411,'2022-02-28 21:13:34',41,1058,NULL,NULL),(412,'2022-02-28 21:13:34',41,1061,NULL,NULL),(413,'2022-02-28 21:13:34',41,1062,NULL,NULL),(414,'2022-02-28 21:13:34',41,1067,NULL,NULL),(415,'2022-02-28 21:13:34',41,1069,NULL,NULL),(416,'2022-02-28 21:13:34',41,1070,NULL,NULL),(417,'2022-02-28 21:13:34',41,1071,NULL,NULL),(418,'2022-02-28 21:13:34',41,1072,NULL,NULL),(419,'2022-02-28 21:13:34',41,1081,NULL,NULL),(420,'2022-02-28 21:13:34',41,1082,NULL,NULL),(421,'2022-02-28 21:13:34',41,1083,NULL,NULL),(422,'2022-02-28 21:13:34',41,1085,NULL,NULL),(423,'2022-02-28 21:13:34',41,1092,NULL,NULL),(424,'2022-02-28 21:13:34',41,1099,NULL,NULL),(425,'2022-02-28 21:13:34',41,1100,NULL,NULL),(426,'2022-02-28 21:13:34',41,1101,NULL,NULL),(427,'2022-02-28 21:13:34',41,1106,NULL,NULL),(428,'2022-02-28 21:13:34',41,1114,NULL,NULL),(429,'2022-02-28 21:13:34',41,1115,NULL,NULL),(430,'2022-02-28 21:13:34',41,1127,NULL,NULL),(431,'2022-02-28 21:13:34',41,1128,NULL,NULL),(432,'2022-02-28 21:13:34',41,1129,NULL,NULL),(433,'2022-02-28 21:13:34',41,1134,NULL,NULL),(434,'2022-02-28 21:13:34',41,1135,NULL,NULL),(435,'2022-02-28 21:13:34',41,1136,NULL,NULL),(436,'2022-02-28 21:13:34',41,1141,NULL,NULL),(437,'2022-02-28 21:13:34',41,1144,NULL,NULL),(438,'2022-02-28 21:13:34',41,1145,NULL,NULL),(439,'2022-02-28 21:13:34',41,1146,NULL,NULL),(440,'2022-02-28 21:13:34',41,1147,NULL,NULL),(441,'2022-02-28 21:13:34',41,1150,NULL,NULL),(442,'2022-02-28 21:13:34',41,1152,NULL,NULL),(443,'2022-02-28 21:13:34',41,1153,NULL,NULL),(444,'2022-02-28 21:13:34',41,1154,NULL,NULL),(445,'2022-02-28 21:13:34',41,1156,NULL,NULL),(446,'2022-02-28 21:13:34',41,1157,NULL,NULL),(447,'2022-02-28 21:13:34',41,1158,NULL,NULL),(448,'2022-02-28 21:13:34',41,1159,NULL,NULL),(449,'2022-02-28 21:13:34',41,1160,NULL,NULL),(450,'2022-02-28 21:13:34',41,1161,NULL,NULL),(451,'2022-02-28 21:13:34',41,1162,NULL,NULL),(452,'2022-02-28 21:13:34',41,1163,NULL,NULL),(453,'2022-02-28 21:13:34',41,1165,NULL,NULL),(454,'2022-02-28 21:13:34',41,1167,NULL,NULL),(455,'2022-02-28 21:13:34',41,1169,NULL,NULL),(456,'2022-02-28 21:13:34',41,1171,NULL,NULL),(457,'2022-02-28 21:13:34',41,1174,NULL,NULL),(458,'2022-02-28 21:13:34',41,1176,NULL,NULL),(459,'2022-02-28 21:13:34',41,1179,NULL,NULL),(460,'2022-02-28 21:13:34',41,1182,NULL,NULL),(461,'2022-02-28 21:13:34',41,1184,NULL,NULL),(462,'2022-02-28 21:13:34',41,1185,NULL,NULL),(463,'2022-02-28 21:13:34',41,1187,NULL,NULL),(464,'2022-02-28 21:13:34',41,1188,NULL,NULL),(465,'2022-02-28 21:13:34',41,1189,NULL,NULL),(466,'2022-02-28 21:13:34',41,1190,NULL,NULL),(467,'2022-02-28 21:13:34',41,1192,NULL,NULL),(468,'2022-02-28 21:13:34',41,1193,NULL,NULL),(469,'2022-02-28 21:13:34',41,1194,NULL,NULL),(470,'2022-02-28 21:13:34',41,1195,NULL,NULL),(471,'2022-02-28 21:13:34',41,1200,NULL,NULL),(472,'2022-02-28 21:13:34',41,1203,NULL,NULL),(473,'2022-02-28 21:13:34',41,1205,NULL,NULL),(474,'2022-02-28 21:13:34',41,1207,NULL,NULL),(475,'2022-02-28 21:13:34',41,1209,NULL,NULL),(476,'2022-02-28 21:13:34',41,1211,NULL,NULL),(477,'2022-02-28 21:13:34',41,1213,NULL,NULL),(478,'2022-02-28 21:13:34',41,1215,NULL,NULL),(479,'2022-02-28 21:13:34',41,1217,NULL,NULL),(480,'2022-02-28 21:13:34',41,1219,NULL,NULL),(481,'2022-02-28 21:13:34',41,1221,NULL,NULL),(482,'2022-02-28 21:13:34',41,1223,NULL,NULL),(483,'2022-02-28 21:13:34',41,1225,NULL,NULL),(484,'2022-02-28 21:13:34',41,1227,NULL,NULL),(485,'2022-02-28 21:13:34',41,1229,NULL,NULL),(486,'2022-02-28 21:13:34',41,1231,NULL,NULL),(487,'2022-02-28 21:13:34',41,1233,NULL,NULL),(488,'2022-02-28 21:13:34',41,1235,NULL,NULL),(489,'2022-02-28 21:13:34',41,1237,NULL,NULL),(490,'2022-02-28 21:13:34',41,1239,NULL,NULL),(491,'2022-02-28 21:13:34',41,1242,NULL,NULL),(492,'2022-02-28 21:13:34',41,1244,NULL,NULL),(493,'2022-02-28 21:13:34',41,1246,NULL,NULL),(494,'2022-02-28 21:13:34',41,1248,NULL,NULL),(495,'2022-02-28 21:13:34',41,1249,NULL,NULL),(496,'2022-02-28 21:13:34',41,1251,NULL,9),(497,'2022-02-28 21:13:34',41,1253,NULL,NULL),(498,'2022-02-28 21:13:34',41,1254,NULL,NULL),(499,'2022-02-28 21:13:34',41,1255,NULL,NULL),(500,'2022-02-28 21:13:34',41,1256,NULL,NULL),(501,'2022-02-28 21:13:34',41,1257,NULL,NULL),(502,'2022-02-28 21:13:34',41,1258,NULL,NULL),(503,'2022-02-28 21:13:34',41,1259,NULL,NULL),(504,'2022-02-28 21:13:34',41,1261,NULL,NULL),(505,'2022-02-28 21:13:34',41,1262,NULL,NULL),(506,'2022-02-28 21:13:34',41,1264,NULL,NULL),(507,'2022-02-28 21:13:34',41,1265,NULL,NULL),(508,'2022-02-28 21:13:34',41,1267,18,NULL),(509,'2022-02-28 21:13:34',41,1269,NULL,NULL),(510,'2022-02-28 21:13:34',41,1270,NULL,NULL),(511,'2022-02-28 21:13:34',41,1271,NULL,NULL),(512,'2022-02-28 21:13:34',41,1272,NULL,NULL),(513,'2022-02-28 21:13:34',41,1273,NULL,NULL),(514,'2022-02-28 21:13:34',41,1274,NULL,NULL),(515,'2022-02-28 21:13:34',41,1275,NULL,NULL),(516,'2022-02-28 21:13:34',41,1277,NULL,NULL),(517,'2022-02-28 21:13:34',41,1279,NULL,NULL),(518,'2022-02-28 21:13:34',41,1280,NULL,NULL),(519,'2022-02-28 21:13:34',41,1282,NULL,NULL),(520,'2022-02-28 21:13:34',41,1283,NULL,NULL),(521,'2022-02-28 21:13:34',41,1285,NULL,NULL),(522,'2022-02-28 21:13:34',41,1286,NULL,NULL),(523,'2022-02-28 21:13:34',41,1288,NULL,NULL),(524,'2022-02-28 21:13:34',41,1290,NULL,NULL),(525,'2022-02-28 21:13:34',41,1291,NULL,NULL),(526,'2022-02-28 21:13:34',41,1292,NULL,NULL),(527,'2022-02-28 21:13:34',41,1293,NULL,NULL),(528,'2022-02-28 21:13:34',41,1294,NULL,NULL),(529,'2022-02-28 21:13:34',41,1296,NULL,NULL),(530,'2022-02-28 21:13:34',41,1298,NULL,NULL),(531,'2022-02-28 21:13:34',41,1300,NULL,NULL),(532,'2022-02-28 21:13:34',41,1301,NULL,NULL),(533,'2022-02-28 21:13:34',41,1302,NULL,NULL),(534,'2022-02-28 21:13:34',41,1303,NULL,NULL),(535,'2022-02-28 21:13:34',41,1305,NULL,NULL),(536,'2022-02-28 21:13:34',41,1307,NULL,NULL),(537,'2022-02-28 21:13:34',41,1309,NULL,NULL),(538,'2022-02-28 21:13:34',41,1310,NULL,NULL),(539,'2022-02-28 21:13:34',41,1311,NULL,NULL),(540,'2022-02-28 21:13:34',41,1312,NULL,NULL),(541,'2022-02-28 21:13:34',41,1313,NULL,NULL),(542,'2022-02-28 21:13:34',41,1315,NULL,NULL),(543,'2022-02-28 21:13:34',41,1317,NULL,NULL),(544,'2022-02-28 21:13:34',41,1319,NULL,NULL),(545,'2022-02-28 21:13:34',41,1320,NULL,NULL),(546,'2022-02-28 21:13:34',41,1322,NULL,NULL),(547,'2022-02-28 21:13:34',41,1325,NULL,NULL),(548,'2022-02-28 21:13:34',41,1326,NULL,NULL),(549,'2022-02-28 21:13:34',41,1328,NULL,NULL),(550,'2022-02-28 21:13:34',41,1330,NULL,NULL),(551,'2022-02-28 21:13:34',41,1333,NULL,NULL),(552,'2022-02-28 21:13:34',41,1335,NULL,NULL),(553,'2022-02-28 21:13:34',41,1337,NULL,NULL),(554,'2022-02-28 21:13:34',41,1339,NULL,NULL),(555,'2022-02-28 21:13:34',41,1343,NULL,NULL),(556,'2022-02-28 21:13:34',41,1346,NULL,NULL),(557,'2022-02-28 21:13:34',41,1350,NULL,NULL),(558,'2022-02-28 21:13:34',41,1354,NULL,NULL),(559,'2022-02-28 21:13:34',41,1357,NULL,NULL),(560,'2022-02-28 21:13:34',41,1359,NULL,NULL),(561,'2022-02-28 21:13:34',41,1362,NULL,NULL),(562,'2022-02-28 21:13:34',41,1364,NULL,NULL),(563,'2022-02-28 21:13:34',41,1367,NULL,NULL),(564,'2022-02-28 21:13:34',41,1369,NULL,NULL),(565,'2022-02-28 21:13:34',41,1376,NULL,NULL),(566,'2022-02-28 21:13:34',41,1378,NULL,NULL),(567,'2022-02-28 21:13:34',41,1380,NULL,NULL),(568,'2022-02-28 21:13:34',41,1383,NULL,NULL),(569,'2022-02-28 21:13:34',41,1386,NULL,NULL),(570,'2022-02-28 21:13:34',41,1389,NULL,NULL),(571,'2022-02-28 21:13:34',41,1397,NULL,NULL),(572,'2022-02-28 21:13:34',41,1399,NULL,NULL),(573,'2022-02-28 21:13:34',41,1403,NULL,NULL),(574,'2022-02-28 21:13:34',41,1411,NULL,NULL),(575,'2022-02-28 21:13:34',41,1413,NULL,NULL),(576,'2022-02-28 21:13:34',41,1415,NULL,NULL),(577,'2022-02-28 21:13:34',41,1417,NULL,NULL),(578,'2022-02-28 21:13:34',41,1419,NULL,NULL),(579,'2022-02-28 21:13:34',41,1422,NULL,NULL),(580,'2022-02-28 21:13:34',41,1425,NULL,NULL),(581,'2022-02-28 21:13:34',41,1427,NULL,NULL),(582,'2022-02-28 21:13:34',41,1429,NULL,NULL),(583,'2022-02-28 21:13:34',41,1431,NULL,NULL),(584,'2022-02-28 21:13:34',41,1433,NULL,NULL),(585,'2022-02-28 21:13:34',41,1436,NULL,NULL),(586,'2022-02-28 21:13:34',41,1439,NULL,NULL),(587,'2022-02-28 21:13:34',41,1441,NULL,NULL),(588,'2022-02-28 21:13:34',41,1444,NULL,NULL),(589,'2022-02-28 21:13:34',41,1449,NULL,NULL),(590,'2022-02-28 21:13:34',41,1454,22,NULL),(591,'2022-02-28 21:13:34',41,1458,NULL,NULL),(592,'2022-02-28 21:13:34',41,1462,NULL,NULL),(593,'2022-02-28 21:13:34',41,1468,NULL,NULL),(594,'2022-02-28 21:13:34',41,1471,NULL,NULL),(595,'2022-02-28 21:13:34',41,1475,NULL,NULL),(596,'2022-02-28 21:13:34',41,1478,NULL,NULL),(597,'2022-02-28 21:13:34',41,1482,NULL,NULL),(598,'2022-02-28 21:13:34',41,1485,NULL,NULL),(599,'2022-02-28 21:13:34',41,1487,NULL,NULL),(600,'2022-02-28 21:13:34',41,1488,16,NULL),(601,'2022-02-28 21:13:34',41,1490,NULL,NULL),(602,'2022-02-28 21:13:34',41,1492,NULL,NULL),(603,'2022-02-28 21:13:34',41,1494,NULL,NULL),(604,'2022-02-28 21:13:34',41,1496,NULL,NULL),(605,'2022-02-28 21:13:34',41,1497,NULL,NULL),(606,'2022-02-28 21:13:34',41,1498,NULL,NULL),(607,'2022-02-28 21:13:34',41,1499,NULL,NULL),(608,'2022-02-28 21:13:34',41,1501,NULL,NULL),(609,'2022-02-28 21:13:34',41,1503,NULL,NULL),(610,'2022-02-28 21:13:34',41,1505,NULL,NULL),(611,'2022-02-28 21:13:34',41,1506,NULL,NULL),(612,'2022-02-28 21:13:34',41,1507,NULL,NULL),(613,'2022-02-28 21:13:34',41,1509,NULL,NULL),(614,'2022-02-28 21:13:34',41,1511,14,NULL),(615,'2022-02-28 21:13:34',41,1513,NULL,NULL),(616,'2022-02-28 21:13:34',41,1514,NULL,NULL),(617,'2022-02-28 21:13:34',41,1516,NULL,NULL),(618,'2022-02-28 21:13:34',41,1517,NULL,NULL),(619,'2022-02-28 21:13:34',41,1518,NULL,NULL),(620,'2022-02-28 21:13:34',41,1520,NULL,NULL),(621,'2022-02-28 21:13:34',41,1522,NULL,NULL),(622,'2022-02-28 21:13:34',41,1523,NULL,NULL),(623,'2022-02-28 21:13:34',41,1524,NULL,NULL),(624,'2022-02-28 21:13:34',41,1526,NULL,NULL),(625,'2022-02-28 21:13:34',41,1528,NULL,NULL),(626,'2022-02-28 21:13:34',41,1529,NULL,NULL),(627,'2022-02-28 21:13:34',41,1530,NULL,NULL),(628,'2022-02-28 21:13:34',41,1531,NULL,NULL),(629,'2022-02-28 21:13:34',41,1533,NULL,NULL),(630,'2022-02-28 21:13:34',41,1534,NULL,NULL),(631,'2022-02-28 21:13:34',41,1536,NULL,NULL),(632,'2022-02-28 21:13:34',41,1538,NULL,NULL),(633,'2022-02-28 21:13:34',41,1539,NULL,NULL),(634,'2022-02-28 21:13:34',41,1541,NULL,NULL),(635,'2022-02-28 21:13:34',41,1543,NULL,NULL),(636,'2022-02-28 21:13:34',41,1544,NULL,NULL),(637,'2022-02-28 21:13:34',41,1546,NULL,NULL),(638,'2022-02-28 21:13:34',41,1547,NULL,NULL),(639,'2022-02-28 21:13:34',41,1548,NULL,NULL),(640,'2022-02-28 21:13:34',41,1550,NULL,1),(641,'2022-02-28 21:13:34',41,1551,NULL,NULL),(642,'2022-02-28 21:13:34',41,1552,NULL,NULL),(643,'2022-02-28 21:13:34',41,1554,NULL,NULL),(644,'2022-02-28 21:13:34',41,1556,NULL,NULL),(645,'2022-02-28 21:13:34',41,1557,NULL,NULL),(646,'2022-02-28 21:13:34',41,1559,NULL,NULL),(647,'2022-02-28 21:13:34',41,1561,NULL,NULL),(648,'2022-02-28 21:13:34',41,1563,NULL,NULL),(649,'2022-02-28 21:13:34',41,1565,NULL,NULL),(650,'2022-02-28 21:13:34',41,1567,NULL,NULL),(651,'2022-02-28 21:13:34',41,1568,NULL,NULL),(652,'2022-02-28 21:13:34',41,1569,NULL,NULL),(653,'2022-02-28 21:13:34',41,1570,NULL,NULL),(654,'2022-02-28 21:13:34',41,1571,NULL,NULL),(655,'2022-02-28 21:13:34',41,1572,NULL,NULL),(656,'2022-02-28 21:13:34',41,1573,NULL,NULL),(657,'2022-02-28 21:13:34',41,1574,NULL,NULL),(658,'2022-02-28 21:13:34',41,1575,NULL,NULL),(659,'2022-02-28 21:13:34',41,1576,NULL,NULL),(660,'2022-02-28 21:13:34',41,1577,NULL,NULL),(661,'2022-02-28 21:13:34',41,1578,NULL,NULL),(662,'2022-02-28 21:13:34',41,1579,NULL,NULL),(663,'2022-02-28 21:13:34',40,949,NULL,NULL),(664,'2022-02-28 21:13:34',40,950,NULL,NULL),(665,'2022-02-28 21:13:34',40,951,NULL,NULL),(666,'2022-02-28 21:13:34',40,952,NULL,NULL),(667,'2022-02-28 21:13:34',40,957,NULL,NULL),(668,'2022-02-28 21:13:34',40,958,NULL,NULL),(669,'2022-02-28 21:13:34',40,980,NULL,NULL),(670,'2022-02-28 21:13:34',40,985,NULL,NULL),(671,'2022-02-28 21:13:34',40,986,NULL,NULL),(672,'2022-02-28 21:13:34',40,987,NULL,NULL),(673,'2022-02-28 21:13:34',40,1002,NULL,NULL),(674,'2022-02-28 21:13:34',40,1003,NULL,NULL),(675,'2022-02-28 21:13:34',40,1004,NULL,NULL),(676,'2022-02-28 21:13:34',40,1006,NULL,NULL),(677,'2022-02-28 21:13:34',40,1007,NULL,NULL),(678,'2022-02-28 21:13:34',40,1008,NULL,NULL),(679,'2022-02-28 21:13:34',40,1022,NULL,NULL),(680,'2022-02-28 21:13:34',40,1025,NULL,NULL),(681,'2022-02-28 21:13:34',40,1036,NULL,NULL),(682,'2022-02-28 21:13:34',40,1037,NULL,NULL),(683,'2022-02-28 21:13:34',40,1038,NULL,NULL),(684,'2022-02-28 21:13:34',40,1039,NULL,NULL),(685,'2022-02-28 21:13:34',40,1040,NULL,NULL),(686,'2022-02-28 21:13:34',40,1041,NULL,NULL),(687,'2022-02-28 21:13:34',40,1042,NULL,NULL),(688,'2022-02-28 21:13:34',40,1043,NULL,NULL),(689,'2022-02-28 21:13:34',40,1044,NULL,NULL),(690,'2022-02-28 21:13:34',40,1045,NULL,NULL),(691,'2022-02-28 21:13:34',40,1046,NULL,NULL),(692,'2022-02-28 21:13:34',40,1049,NULL,NULL),(693,'2022-02-28 21:13:34',40,1050,NULL,NULL),(694,'2022-02-28 21:13:34',40,1051,NULL,NULL),(695,'2022-02-28 21:13:34',40,1052,NULL,NULL),(696,'2022-02-28 21:13:34',40,1053,NULL,NULL),(697,'2022-02-28 21:13:34',40,1063,NULL,NULL),(698,'2022-02-28 21:13:34',40,1065,NULL,NULL),(699,'2022-02-28 21:13:34',40,1066,NULL,NULL),(700,'2022-02-28 21:13:34',40,1073,NULL,NULL),(701,'2022-02-28 21:13:34',40,1075,NULL,NULL),(702,'2022-02-28 21:13:34',40,1076,NULL,NULL),(703,'2022-02-28 21:13:34',40,1077,NULL,NULL),(704,'2022-02-28 21:13:34',40,1078,NULL,NULL),(705,'2022-02-28 21:13:34',40,1079,NULL,NULL),(706,'2022-02-28 21:13:34',40,1080,NULL,NULL),(707,'2022-02-28 21:13:34',40,1086,NULL,NULL),(708,'2022-02-28 21:13:34',40,1090,NULL,NULL),(709,'2022-02-28 21:13:34',40,1091,NULL,NULL),(710,'2022-02-28 21:13:34',40,1093,NULL,NULL),(711,'2022-02-28 21:13:34',40,1098,NULL,NULL),(712,'2022-02-28 21:13:34',40,1102,NULL,NULL),(713,'2022-02-28 21:13:34',40,1105,NULL,NULL),(714,'2022-02-28 21:13:34',40,1107,NULL,NULL),(715,'2022-02-28 21:13:34',40,1109,NULL,NULL),(716,'2022-02-28 21:13:34',40,1110,NULL,NULL),(717,'2022-02-28 21:13:34',40,1111,NULL,NULL),(718,'2022-02-28 21:13:34',40,1112,NULL,NULL),(719,'2022-02-28 21:13:34',40,1113,NULL,NULL),(720,'2022-02-28 21:13:34',40,1116,NULL,NULL),(721,'2022-02-28 21:13:34',40,1117,NULL,NULL),(722,'2022-02-28 21:13:34',40,1118,NULL,NULL),(723,'2022-02-28 21:13:34',40,1119,NULL,NULL),(724,'2022-02-28 21:13:34',40,1120,NULL,NULL),(725,'2022-02-28 21:13:34',40,1121,NULL,NULL),(726,'2022-02-28 21:13:34',40,1122,NULL,NULL),(727,'2022-02-28 21:13:34',40,1124,NULL,NULL),(728,'2022-02-28 21:13:34',40,1125,NULL,NULL),(729,'2022-02-28 21:13:34',40,1126,NULL,NULL),(730,'2022-02-28 21:13:34',40,1130,NULL,NULL),(731,'2022-02-28 21:13:34',40,1131,NULL,NULL),(732,'2022-02-28 21:13:34',40,1132,NULL,NULL),(733,'2022-02-28 21:13:34',40,1133,NULL,NULL),(734,'2022-02-28 21:13:34',40,1137,NULL,NULL),(735,'2022-02-28 21:13:34',40,1138,NULL,NULL),(736,'2022-02-28 21:13:34',40,1139,NULL,NULL),(737,'2022-02-28 21:13:34',40,1140,NULL,NULL),(738,'2022-02-28 21:13:34',40,1142,NULL,NULL),(739,'2022-02-28 21:13:34',40,1143,NULL,NULL),(740,'2022-02-28 21:13:34',40,1148,NULL,NULL),(741,'2022-02-28 21:13:34',40,1149,NULL,NULL),(742,'2022-02-28 21:13:34',40,1151,NULL,NULL),(743,'2022-02-28 21:13:34',40,1155,NULL,NULL),(744,'2022-02-28 21:13:34',40,1164,NULL,NULL),(745,'2022-02-28 21:13:34',40,1166,NULL,NULL),(746,'2022-02-28 21:13:34',40,1168,NULL,NULL),(747,'2022-02-28 21:13:34',40,1170,NULL,NULL),(748,'2022-02-28 21:13:34',40,1172,NULL,NULL),(749,'2022-02-28 21:13:34',40,1173,NULL,NULL),(750,'2022-02-28 21:13:34',40,1175,NULL,NULL),(751,'2022-02-28 21:13:34',40,1177,NULL,NULL),(752,'2022-02-28 21:13:34',40,1178,NULL,NULL),(753,'2022-02-28 21:13:34',40,1180,NULL,NULL),(754,'2022-02-28 21:13:34',40,1181,NULL,NULL),(755,'2022-02-28 21:13:34',40,1183,NULL,NULL),(756,'2022-02-28 21:13:34',40,1186,NULL,NULL),(757,'2022-02-28 21:13:34',40,1191,NULL,NULL),(758,'2022-02-28 21:13:34',40,1196,NULL,NULL),(759,'2022-02-28 21:13:34',40,1197,NULL,NULL),(760,'2022-02-28 21:13:34',40,1198,NULL,NULL),(761,'2022-02-28 21:13:34',40,1199,NULL,NULL),(762,'2022-02-28 21:13:34',40,1201,NULL,NULL),(763,'2022-02-28 21:13:34',40,1202,NULL,NULL),(764,'2022-02-28 21:13:34',40,1204,NULL,NULL),(765,'2022-02-28 21:13:34',40,1206,NULL,NULL),(766,'2022-02-28 21:13:34',40,1208,NULL,NULL),(767,'2022-02-28 21:13:34',40,1210,NULL,NULL),(768,'2022-02-28 21:13:34',40,1212,NULL,NULL),(769,'2022-02-28 21:13:34',40,1214,NULL,NULL),(770,'2022-02-28 21:13:34',40,1216,NULL,NULL),(771,'2022-02-28 21:13:34',40,1218,NULL,NULL),(772,'2022-02-28 21:13:34',40,1220,NULL,NULL),(773,'2022-02-28 21:13:34',40,1222,NULL,NULL),(774,'2022-02-28 21:13:34',40,1224,NULL,NULL),(775,'2022-02-28 21:13:34',40,1226,NULL,NULL),(776,'2022-02-28 21:13:34',40,1228,NULL,NULL),(777,'2022-02-28 21:13:34',40,1230,NULL,NULL),(778,'2022-02-28 21:13:34',40,1232,NULL,NULL),(779,'2022-02-28 21:13:34',40,1234,NULL,NULL),(780,'2022-02-28 21:13:34',40,1236,NULL,NULL),(781,'2022-02-28 21:13:34',40,1238,NULL,NULL),(782,'2022-02-28 21:13:34',40,1240,NULL,NULL),(783,'2022-02-28 21:13:34',40,1241,NULL,NULL),(784,'2022-02-28 21:13:34',40,1243,NULL,NULL),(785,'2022-02-28 21:13:34',40,1245,NULL,NULL),(786,'2022-02-28 21:13:34',40,1247,NULL,NULL),(787,'2022-02-28 21:13:34',40,1250,NULL,NULL),(788,'2022-02-28 21:13:34',40,1252,NULL,NULL),(789,'2022-02-28 21:13:34',40,1260,NULL,NULL),(790,'2022-02-28 21:13:34',40,1263,NULL,NULL),(791,'2022-02-28 21:13:34',40,1266,NULL,NULL),(792,'2022-02-28 21:13:34',40,1268,NULL,NULL),(793,'2022-02-28 21:13:34',40,1276,NULL,NULL),(794,'2022-02-28 21:13:34',40,1278,NULL,NULL),(795,'2022-02-28 21:13:34',40,1281,NULL,NULL),(796,'2022-02-28 21:13:34',40,1284,NULL,NULL),(797,'2022-02-28 21:13:34',40,1287,NULL,NULL),(798,'2022-02-28 21:13:34',40,1289,NULL,NULL),(799,'2022-02-28 21:13:34',40,1295,NULL,NULL),(800,'2022-02-28 21:13:34',40,1297,NULL,NULL),(801,'2022-02-28 21:13:34',40,1299,NULL,NULL),(802,'2022-02-28 21:13:34',40,1304,NULL,NULL),(803,'2022-02-28 21:13:34',40,1306,NULL,NULL),(804,'2022-02-28 21:13:34',40,1308,NULL,NULL),(805,'2022-02-28 21:13:34',40,1314,NULL,NULL),(806,'2022-02-28 21:13:34',40,1316,NULL,NULL),(807,'2022-02-28 21:13:34',40,1318,NULL,NULL),(808,'2022-02-28 21:13:34',40,1321,NULL,9),(809,'2022-02-28 21:13:34',40,1323,NULL,NULL),(810,'2022-02-28 21:13:34',40,1324,NULL,NULL),(811,'2022-02-28 21:13:34',40,1327,NULL,NULL),(812,'2022-02-28 21:13:34',40,1329,NULL,NULL),(813,'2022-02-28 21:13:34',40,1331,20,NULL),(814,'2022-02-28 21:13:34',40,1332,NULL,NULL),(815,'2022-02-28 21:13:34',40,1334,NULL,NULL),(816,'2022-02-28 21:13:34',40,1336,NULL,NULL),(817,'2022-02-28 21:13:34',40,1338,NULL,NULL),(818,'2022-02-28 21:13:34',40,1340,NULL,NULL),(819,'2022-02-28 21:13:34',40,1341,NULL,NULL),(820,'2022-02-28 21:13:34',40,1342,NULL,NULL),(821,'2022-02-28 21:13:34',40,1344,NULL,NULL),(822,'2022-02-28 21:13:34',40,1345,NULL,NULL),(823,'2022-02-28 21:13:34',40,1347,NULL,NULL),(824,'2022-02-28 21:13:34',40,1348,NULL,NULL),(825,'2022-02-28 21:13:34',40,1349,NULL,NULL),(826,'2022-02-28 21:13:34',40,1351,NULL,NULL),(827,'2022-02-28 21:13:34',40,1352,NULL,NULL),(828,'2022-02-28 21:13:34',40,1353,NULL,NULL),(829,'2022-02-28 21:13:34',40,1355,NULL,NULL),(830,'2022-02-28 21:13:34',40,1356,NULL,NULL),(831,'2022-02-28 21:13:34',40,1358,NULL,NULL),(832,'2022-02-28 21:13:34',40,1360,NULL,NULL),(833,'2022-02-28 21:13:34',40,1361,NULL,NULL),(834,'2022-02-28 21:13:34',40,1363,NULL,NULL),(835,'2022-02-28 21:13:34',40,1365,NULL,NULL),(836,'2022-02-28 21:13:34',40,1366,NULL,NULL),(837,'2022-02-28 21:13:34',40,1368,NULL,NULL),(838,'2022-02-28 21:13:34',40,1370,NULL,NULL),(839,'2022-02-28 21:13:34',40,1371,NULL,NULL),(840,'2022-02-28 21:13:34',40,1372,NULL,NULL),(841,'2022-02-28 21:13:34',40,1373,NULL,NULL),(842,'2022-02-28 21:13:34',40,1374,NULL,NULL),(843,'2022-02-28 21:13:34',40,1375,NULL,NULL),(844,'2022-02-28 21:13:34',40,1377,NULL,NULL),(845,'2022-02-28 21:13:34',40,1379,NULL,NULL),(846,'2022-02-28 21:13:34',40,1381,NULL,NULL),(847,'2022-02-28 21:13:34',40,1382,NULL,NULL),(848,'2022-02-28 21:13:34',40,1384,NULL,NULL),(849,'2022-02-28 21:13:34',40,1385,NULL,NULL),(850,'2022-02-28 21:13:34',40,1387,NULL,NULL),(851,'2022-02-28 21:13:34',40,1388,NULL,NULL),(852,'2022-02-28 21:13:34',40,1390,NULL,NULL),(853,'2022-02-28 21:13:34',40,1391,NULL,NULL),(854,'2022-02-28 21:13:34',40,1392,NULL,NULL),(855,'2022-02-28 21:13:34',40,1393,NULL,NULL),(856,'2022-02-28 21:13:34',40,1394,NULL,NULL),(857,'2022-02-28 21:13:34',40,1395,NULL,NULL),(858,'2022-02-28 21:13:34',40,1396,19,NULL),(859,'2022-02-28 21:13:34',40,1398,18,NULL),(860,'2022-02-28 21:13:34',40,1400,NULL,NULL),(861,'2022-02-28 21:13:34',40,1401,NULL,NULL),(862,'2022-02-28 21:13:34',40,1402,NULL,NULL),(863,'2022-02-28 21:13:34',40,1404,NULL,NULL),(864,'2022-02-28 21:13:34',40,1405,NULL,NULL),(865,'2022-02-28 21:13:34',40,1406,NULL,NULL),(866,'2022-02-28 21:13:34',40,1407,NULL,NULL),(867,'2022-02-28 21:13:34',40,1408,NULL,NULL),(868,'2022-02-28 21:13:34',40,1409,NULL,NULL),(869,'2022-02-28 21:13:34',40,1410,NULL,NULL),(870,'2022-02-28 21:13:34',40,1412,NULL,NULL),(871,'2022-02-28 21:13:34',40,1414,NULL,NULL),(872,'2022-02-28 21:13:34',40,1416,NULL,NULL),(873,'2022-02-28 21:13:34',40,1418,NULL,NULL),(874,'2022-02-28 21:13:34',40,1420,NULL,NULL),(875,'2022-02-28 21:13:34',40,1421,NULL,NULL),(876,'2022-02-28 21:13:34',40,1423,NULL,NULL),(877,'2022-02-28 21:13:34',40,1424,NULL,NULL),(878,'2022-02-28 21:13:34',40,1426,NULL,NULL),(879,'2022-02-28 21:13:34',40,1428,NULL,NULL),(880,'2022-02-28 21:13:34',40,1430,NULL,NULL),(881,'2022-02-28 21:13:34',40,1432,NULL,NULL),(882,'2022-02-28 21:13:34',40,1434,NULL,NULL),(883,'2022-02-28 21:13:34',40,1435,NULL,NULL),(884,'2022-02-28 21:13:34',40,1437,NULL,NULL),(885,'2022-02-28 21:13:34',40,1438,NULL,NULL),(886,'2022-02-28 21:13:34',40,1440,NULL,NULL),(887,'2022-02-28 21:13:34',40,1442,NULL,NULL),(888,'2022-02-28 21:13:34',40,1443,NULL,NULL),(889,'2022-02-28 21:13:34',40,1445,NULL,NULL),(890,'2022-02-28 21:13:34',40,1446,NULL,NULL),(891,'2022-02-28 21:13:34',40,1447,NULL,NULL),(892,'2022-02-28 21:13:34',40,1448,NULL,NULL),(893,'2022-02-28 21:13:34',40,1450,NULL,NULL),(894,'2022-02-28 21:13:34',40,1451,NULL,NULL),(895,'2022-02-28 21:13:34',40,1452,NULL,NULL),(896,'2022-02-28 21:13:34',40,1453,NULL,NULL),(897,'2022-02-28 21:13:34',40,1455,NULL,NULL),(898,'2022-02-28 21:13:34',40,1456,NULL,NULL),(899,'2022-02-28 21:13:34',40,1457,NULL,NULL),(900,'2022-02-28 21:13:34',40,1459,NULL,NULL),(901,'2022-02-28 21:13:34',40,1460,NULL,NULL),(902,'2022-02-28 21:13:34',40,1461,NULL,NULL),(903,'2022-02-28 21:13:34',40,1463,NULL,NULL),(904,'2022-02-28 21:13:34',40,1464,NULL,NULL),(905,'2022-02-28 21:13:34',40,1465,NULL,NULL),(906,'2022-02-28 21:13:34',40,1466,NULL,NULL),(907,'2022-02-28 21:13:34',40,1467,NULL,NULL),(908,'2022-02-28 21:13:34',40,1469,NULL,NULL),(909,'2022-02-28 21:13:34',40,1470,NULL,NULL),(910,'2022-02-28 21:13:34',40,1472,NULL,NULL),(911,'2022-02-28 21:13:34',40,1473,NULL,NULL),(912,'2022-02-28 21:13:34',40,1474,NULL,NULL),(913,'2022-02-28 21:13:34',40,1476,NULL,NULL),(914,'2022-02-28 21:13:34',40,1477,NULL,NULL),(915,'2022-02-28 21:13:34',40,1479,NULL,NULL),(916,'2022-02-28 21:13:34',40,1480,NULL,NULL),(917,'2022-02-28 21:13:34',40,1481,NULL,NULL),(918,'2022-02-28 21:13:34',40,1483,NULL,NULL),(919,'2022-02-28 21:13:34',40,1484,NULL,NULL),(920,'2022-02-28 21:13:34',40,1486,NULL,NULL),(921,'2022-02-28 21:13:34',40,1489,NULL,NULL),(922,'2022-02-28 21:13:34',40,1491,NULL,NULL),(923,'2022-02-28 21:13:34',40,1493,NULL,NULL),(924,'2022-02-28 21:13:34',40,1495,NULL,NULL),(925,'2022-02-28 21:13:34',40,1500,NULL,NULL),(926,'2022-02-28 21:13:34',40,1502,NULL,NULL),(927,'2022-02-28 21:13:34',40,1504,NULL,NULL),(928,'2022-02-28 21:13:34',40,1508,NULL,NULL),(929,'2022-02-28 21:13:34',40,1510,NULL,NULL),(930,'2022-02-28 21:13:34',40,1512,NULL,NULL),(931,'2022-02-28 21:13:34',40,1515,NULL,NULL),(932,'2022-02-28 21:13:34',40,1519,NULL,NULL),(933,'2022-02-28 21:13:34',40,1521,NULL,NULL),(934,'2022-02-28 21:13:34',40,1525,NULL,NULL),(935,'2022-02-28 21:13:34',40,1527,NULL,NULL),(936,'2022-02-28 21:13:34',40,1532,NULL,NULL),(937,'2022-02-28 21:13:34',40,1535,NULL,NULL),(938,'2022-02-28 21:13:34',40,1537,NULL,NULL),(939,'2022-02-28 21:13:34',40,1540,NULL,NULL),(940,'2022-02-28 21:13:34',40,1542,NULL,NULL),(941,'2022-02-28 21:13:34',40,1545,NULL,NULL),(942,'2022-02-28 21:13:34',40,1549,NULL,NULL),(943,'2022-02-28 21:13:34',40,1553,NULL,NULL),(944,'2022-02-28 21:13:34',40,1555,NULL,NULL),(945,'2022-02-28 21:13:34',40,1558,NULL,NULL),(946,'2022-02-28 21:13:34',40,1560,NULL,NULL),(947,'2022-02-28 21:13:34',40,1562,NULL,NULL),(948,'2022-02-28 21:13:34',40,1564,NULL,NULL),(949,'2022-02-28 21:13:34',40,1566,NULL,NULL),(950,'2022-02-28 21:13:34',40,1580,NULL,NULL),(951,'2022-02-28 21:13:34',40,1581,NULL,NULL),(952,'2022-02-28 21:13:34',40,1582,NULL,NULL),(953,'2022-02-28 21:13:34',40,1583,NULL,NULL),(954,'2022-02-28 21:13:34',40,1584,NULL,NULL),(955,'2022-02-28 21:13:34',40,1585,NULL,NULL),(956,'2022-02-28 21:13:34',40,1586,NULL,NULL),(957,'2022-02-28 21:13:34',40,1587,NULL,NULL),(958,'2022-02-28 21:13:34',40,1588,NULL,NULL),(959,'2022-02-28 21:13:34',40,1589,NULL,NULL),(960,'2022-02-28 21:13:34',40,1590,NULL,NULL),(961,'2022-02-28 21:13:34',40,1591,NULL,NULL),(962,'2022-02-28 21:13:34',40,1592,NULL,NULL),(963,'2022-02-28 21:13:34',40,1593,NULL,NULL),(964,'2022-02-28 21:13:34',40,1594,NULL,NULL),(965,'2022-02-28 21:13:34',40,1595,NULL,NULL),(966,'2022-02-28 21:13:34',40,1596,NULL,NULL),(967,'2022-02-28 21:13:34',40,1597,NULL,NULL),(968,'2022-02-28 21:13:34',40,1598,NULL,NULL),(969,'2022-02-28 21:13:34',40,1599,NULL,NULL),(970,'2022-02-28 21:13:34',40,1600,NULL,NULL),(971,'2022-02-28 21:13:34',40,1601,NULL,NULL),(972,'2022-02-28 21:13:34',40,1602,NULL,NULL),(973,'2022-02-28 21:13:34',40,1603,NULL,NULL),(974,'2022-02-28 21:13:34',40,1604,NULL,NULL),(975,'2022-02-28 21:13:34',40,1605,NULL,NULL),(976,'2022-02-28 21:13:34',40,1606,NULL,NULL),(977,'2022-02-28 21:13:34',40,1607,22,NULL),(978,'2022-02-28 21:13:34',40,1608,NULL,NULL),(979,'2022-02-28 21:13:34',40,1609,NULL,NULL),(980,'2022-02-28 21:13:34',40,1610,NULL,NULL),(981,'2022-02-28 21:13:34',40,1611,NULL,NULL),(982,'2022-02-28 21:13:34',40,1612,NULL,NULL),(983,'2022-02-28 21:13:34',40,1613,NULL,NULL),(984,'2022-02-28 21:13:34',40,1614,NULL,NULL),(985,'2022-02-28 21:13:34',40,1615,NULL,NULL),(986,'2022-02-28 21:13:34',40,1616,NULL,NULL),(987,'2022-02-28 21:13:34',40,1617,NULL,NULL),(988,'2022-02-28 21:13:34',40,1618,NULL,NULL),(989,'2022-02-28 21:13:34',40,1619,NULL,NULL),(990,'2022-02-28 21:13:34',40,1620,NULL,NULL),(991,'2022-02-28 21:13:34',40,1621,NULL,NULL),(992,'2022-02-28 21:13:34',40,1622,NULL,NULL),(993,'2022-02-28 21:13:34',40,1623,NULL,NULL),(994,'2022-02-28 21:13:34',40,1624,NULL,NULL),(995,'2022-02-28 21:13:34',40,1625,NULL,NULL),(996,'2022-02-28 21:13:34',40,1626,NULL,NULL),(997,'2022-02-28 21:13:34',40,1627,NULL,NULL),(998,'2022-02-28 21:13:34',40,1628,NULL,NULL),(999,'2022-02-28 21:13:34',40,1629,NULL,NULL),(1000,'2022-02-28 21:13:34',40,1630,NULL,NULL),(1001,'2022-02-28 21:13:34',40,1631,NULL,NULL),(1002,'2022-02-28 21:13:34',40,1632,NULL,NULL),(1003,'2022-02-28 21:13:34',40,1633,NULL,NULL),(1004,'2022-02-28 21:13:34',40,1634,NULL,NULL),(1005,'2022-02-28 21:13:34',40,1635,NULL,NULL),(1006,'2022-02-28 21:13:34',40,1636,NULL,NULL),(1007,'2022-02-28 21:13:34',40,1637,NULL,NULL),(1008,'2022-02-28 21:13:34',40,1638,NULL,NULL),(1009,'2022-02-28 21:13:34',40,1639,NULL,NULL),(1010,'2022-02-28 21:13:34',40,1640,NULL,NULL),(1011,'2022-02-28 21:13:34',40,1641,NULL,NULL),(1012,'2022-02-28 21:13:34',40,1642,NULL,NULL),(1013,'2022-02-28 21:13:34',40,1643,NULL,NULL),(1014,'2022-02-28 21:13:34',40,1644,NULL,NULL),(1015,'2022-02-28 21:13:34',40,1645,NULL,NULL),(1016,'2022-02-28 21:13:34',40,1646,21,NULL),(1017,'2022-02-28 21:13:34',40,1647,NULL,NULL),(1018,'2022-02-28 21:13:34',40,1648,NULL,NULL),(1019,'2022-02-28 21:13:34',40,1649,NULL,NULL),(1020,'2022-02-28 21:13:34',40,1650,NULL,NULL),(1021,'2022-02-28 21:13:34',40,1651,NULL,NULL),(1022,'2022-02-28 21:13:34',40,1652,NULL,NULL),(1023,'2022-02-28 21:13:34',40,1653,NULL,NULL),(1024,'2022-02-28 21:13:34',40,1654,NULL,NULL),(1025,'2022-02-28 21:13:34',40,1655,NULL,NULL),(1026,'2022-02-28 21:13:34',40,1656,NULL,NULL),(1027,'2022-02-28 21:13:34',40,1657,NULL,NULL),(1028,'2022-02-28 21:13:34',40,1658,NULL,NULL),(1029,'2022-02-28 21:13:34',40,1659,NULL,NULL),(1030,'2022-02-28 21:13:34',40,1660,NULL,NULL),(1031,'2022-02-28 21:13:34',40,1661,NULL,NULL),(1032,'2022-02-28 21:13:34',40,1662,NULL,NULL),(1033,'2022-02-28 21:13:34',40,1663,NULL,NULL),(1034,'2022-02-28 21:13:34',40,1664,NULL,NULL),(1035,'2022-02-28 21:13:34',40,1665,NULL,NULL),(1036,'2022-02-28 21:13:34',40,1666,NULL,NULL),(1037,'2022-02-28 21:13:34',40,1667,NULL,NULL),(1038,'2022-02-28 21:13:34',40,1668,NULL,7),(1039,'2022-02-28 21:13:34',40,1669,NULL,NULL),(1040,'2022-02-28 21:13:34',40,1670,17,NULL),(1041,'2022-02-28 21:13:34',40,1671,NULL,NULL),(1042,'2022-02-28 21:13:34',40,1672,NULL,NULL),(1043,'2022-02-28 21:13:34',40,1673,NULL,NULL),(1044,'2022-02-28 21:13:34',40,1674,NULL,NULL),(1045,'2022-02-28 21:13:34',40,1675,NULL,NULL),(1046,'2022-02-28 21:13:34',40,1676,NULL,NULL),(1047,'2022-02-28 21:13:34',40,1677,NULL,NULL),(1048,'2022-02-28 21:13:34',40,1678,NULL,NULL),(1049,'2022-02-28 21:13:34',40,1679,NULL,NULL),(1050,'2022-02-28 21:13:34',40,1680,NULL,NULL),(1051,'2022-02-28 21:13:34',40,1681,NULL,NULL),(1052,'2022-02-28 21:13:34',40,1682,NULL,NULL),(1053,'2022-02-28 21:13:34',40,1683,NULL,NULL),(1054,'2022-02-28 21:13:34',40,1684,15,NULL),(1055,'2022-02-28 21:13:34',40,1685,NULL,NULL),(1056,'2022-02-28 21:13:34',40,1686,NULL,NULL),(1057,'2022-02-28 21:13:34',40,1687,NULL,NULL),(1058,'2022-02-28 21:13:34',40,1688,NULL,NULL),(1059,'2022-02-28 21:13:34',40,1689,NULL,NULL),(1060,'2022-02-28 21:13:34',40,1690,NULL,NULL),(1061,'2022-02-28 21:13:34',40,1691,NULL,NULL),(1062,'2022-02-28 21:13:34',40,1692,NULL,NULL),(1063,'2022-02-28 21:13:34',40,1693,NULL,NULL),(1064,'2022-02-28 21:13:34',40,1694,NULL,NULL),(1065,'2022-02-28 21:13:34',40,1695,NULL,NULL),(1066,'2022-02-28 21:13:34',40,1696,NULL,NULL),(1067,'2022-02-28 21:13:34',40,1697,NULL,NULL),(1068,'2022-02-28 21:13:34',40,1698,NULL,NULL),(1069,'2022-02-28 21:13:34',40,1699,NULL,NULL),(1070,'2022-02-28 21:13:34',40,1700,NULL,NULL),(1071,'2022-02-28 21:13:34',40,1701,NULL,NULL),(1072,'2022-02-28 21:13:34',40,1702,NULL,NULL),(1073,'2022-02-28 21:13:34',40,1703,NULL,NULL),(1074,'2022-02-28 21:13:34',40,1704,NULL,NULL),(1075,'2022-02-28 21:13:34',40,1705,NULL,NULL),(1076,'2022-02-28 21:13:34',40,1706,NULL,NULL),(1077,'2022-02-28 21:13:34',40,1707,NULL,NULL),(1078,'2022-02-28 21:13:34',40,1708,NULL,NULL),(1079,'2022-02-28 21:13:34',40,1709,NULL,NULL),(1080,'2022-02-28 21:13:34',40,1710,NULL,NULL),(1081,'2022-02-28 21:13:34',40,1711,NULL,NULL),(1082,'2022-02-28 21:13:34',40,1712,NULL,NULL),(1083,'2022-02-28 21:13:34',40,1713,NULL,NULL),(1084,'2022-02-28 21:13:34',40,1714,13,NULL),(1085,'2022-02-28 21:13:34',40,1715,NULL,NULL),(1086,'2022-02-28 21:13:34',40,1716,NULL,NULL),(1087,'2022-02-28 21:13:34',40,1717,NULL,NULL),(1088,'2022-02-28 21:13:34',40,1718,NULL,NULL),(1089,'2022-02-28 21:13:34',40,1719,NULL,NULL),(1090,'2022-02-28 21:13:34',40,1720,NULL,NULL),(1091,'2022-02-28 21:13:34',40,1721,NULL,NULL),(1092,'2022-02-28 21:13:34',40,1722,NULL,NULL),(1093,'2022-02-28 21:13:34',40,1723,NULL,NULL),(1094,'2022-02-28 21:13:34',40,1724,NULL,NULL),(1095,'2022-02-28 21:13:34',40,1725,NULL,NULL),(1096,'2022-02-28 21:13:34',40,1726,NULL,NULL),(1097,'2022-02-28 21:13:34',40,1727,NULL,NULL),(1098,'2022-02-28 21:13:34',40,1728,NULL,NULL),(1099,'2022-02-28 21:13:34',40,1729,NULL,NULL),(1100,'2022-02-28 21:13:34',40,1730,NULL,NULL),(1101,'2022-02-28 21:13:34',40,1731,NULL,NULL),(1102,'2022-02-28 21:13:34',40,1732,NULL,NULL),(1103,'2022-02-28 21:13:34',40,1733,NULL,NULL),(1104,'2022-02-28 21:13:34',40,1734,NULL,NULL),(1105,'2022-02-28 21:13:34',40,1735,NULL,NULL),(1106,'2022-02-28 21:13:34',40,1736,NULL,NULL),(1107,'2022-02-28 21:13:34',40,1737,NULL,NULL),(1108,'2022-02-28 21:13:34',40,1738,NULL,NULL),(1109,'2022-02-28 21:13:34',40,1739,NULL,NULL),(1110,'2022-02-28 21:13:34',40,1740,NULL,NULL),(1111,'2022-02-28 21:13:34',40,1741,NULL,NULL),(1112,'2022-02-28 21:13:34',40,1742,NULL,NULL);
/*!40000 ALTER TABLE `host_vulnerability_output` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `index_response`
--

LOCK TABLES `index_response` WRITE;
/*!40000 ALTER TABLE `index_response` DISABLE KEYS */;
INSERT INTO `index_response` VALUES (1,'2022-05-11 14:43:17',NULL);
/*!40000 ALTER TABLE `index_response` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `index_response_folder`
--

LOCK TABLES `index_response_folder` WRITE;
/*!40000 ALTER TABLE `index_response_folder` DISABLE KEYS */;
INSERT INTO `index_response_folder` VALUES (1,2,0),(1,3,1);
/*!40000 ALTER TABLE `index_response_folder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `index_response_scan`
--

LOCK TABLES `index_response_scan` WRITE;
/*!40000 ALTER TABLE `index_response_scan` DISABLE KEYS */;
INSERT INTO `index_response_scan` VALUES (1,5,4),(1,8,3),(1,11,2),(1,15,1),(1,22,0);
/*!40000 ALTER TABLE `index_response_scan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan`
--

LOCK TABLES `scan` WRITE;
/*!40000 ALTER TABLE `scan` DISABLE KEYS */;
INSERT INTO `scan` VALUES (5,'My Host Discovery Scan',5,3,1,1,NULL,1,0,0,1,128,1,'2022-02-26 23:35:11',NULL,'2022-02-26 23:35:49',NULL,0,NULL),(8,'My Basic Network Scan',4,3,1,1,NULL,1,0,0,1,128,1,'2022-02-26 23:44:32',NULL,'2022-02-26 23:58:29',NULL,0,NULL),(11,'WebApp-Policy',3,3,1,1,NULL,1,0,0,1,128,1,'2022-02-28 20:44:13',NULL,'2022-02-28 20:52:58',NULL,0,NULL),(15,'Credentialed-Patch-Scan',2,3,1,1,NULL,1,0,0,1,128,1,'2022-02-28 21:10:01',NULL,'2022-02-28 21:13:34',NULL,NULL,NULL),(22,'Spectre/Meltdown',1,3,1,1,NULL,1,0,0,1,128,1,'2022-02-28 21:28:39',NULL,'2022-02-28 21:32:09',NULL,NULL,NULL);
/*!40000 ALTER TABLE `scan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_history`
--

LOCK TABLES `scan_history` WRITE;
/*!40000 ALTER TABLE `scan_history` DISABLE KEYS */;
INSERT INTO `scan_history` VALUES (1,5,6,0,0,NULL,NULL,NULL,NULL,1,1,1,5,'2022-02-26 23:35:49','2022-02-26 23:35:11',2,NULL),(2,22,24,0,0,NULL,NULL,NULL,NULL,1,1,1,1,'2022-02-28 21:32:09','2022-02-28 21:28:39',2,NULL),(3,11,12,0,0,NULL,NULL,NULL,NULL,1,1,1,3,'2022-02-28 20:52:58','2022-02-28 20:44:13',2,NULL),(4,8,9,0,0,NULL,NULL,NULL,NULL,1,1,1,4,'2022-02-26 23:58:29','2022-02-26 23:44:32',2,NULL),(5,15,16,0,0,NULL,NULL,NULL,NULL,1,1,1,6,'2022-02-28 21:07:18','2022-02-28 21:07:10',2,NULL),(6,15,18,0,0,NULL,NULL,NULL,NULL,1,1,1,2,'2022-02-28 21:13:34','2022-02-28 21:10:01',2,NULL);
/*!40000 ALTER TABLE `scan_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_host`
--

LOCK TABLES `scan_host` WRITE;
/*!40000 ALTER TABLE `scan_host` DISABLE KEYS */;
INSERT INTO `scan_host` VALUES (1,5,2,221,221,100,100,0,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,1,NULL),(2,5,3,221,221,100,100,1,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,4,NULL),(3,5,4,221,221,100,100,2,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,6,NULL),(4,5,5,221,221,100,100,3,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,3,NULL),(5,5,6,221,221,100,100,4,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,9,NULL),(6,5,7,221,221,100,100,5,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,2,NULL),(7,5,8,221,221,100,100,6,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,5,NULL),(8,5,9,221,221,100,100,7,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,8,NULL),(9,5,10,221,221,100,100,8,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,11,NULL),(10,5,11,221,221,100,100,9,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,12,NULL),(11,5,12,221,221,100,100,10,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,13,NULL),(12,5,13,221,221,100,100,11,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,7,NULL),(13,5,14,221,221,100,100,12,2,'100-100/221-221',0,0,0,0,0,0,0,0,0,2,2,14,NULL),(14,22,2,705,705,100,100,0,37,'100-100/705-705',0,0,0,0,0,0,0,0,0,37,37,2,NULL),(15,22,3,705,705,100,100,1,1021,'100-100/705-705',0,0,0,0,0,0,1,0,0,21,22,5,NULL),(16,22,4,705,705,100,100,2,7,'100-100/705-705',0,0,0,0,0,0,0,0,0,7,7,8,NULL),(17,22,5,705,705,100,100,3,3,'100-100/705-705',0,0,0,0,0,0,0,0,0,3,3,10,NULL),(18,22,6,705,705,100,100,4,1,'100-100/705-705',0,0,0,0,0,0,0,0,0,1,1,11,NULL),(19,22,7,705,705,100,100,5,3,'100-100/705-705',0,0,0,0,0,0,0,0,0,3,3,12,NULL),(20,22,8,705,705,100,100,6,6,'100-100/705-705',0,0,0,0,0,0,0,0,0,6,6,13,NULL),(21,22,9,705,705,100,100,7,2,'100-100/705-705',0,0,0,0,0,0,0,0,0,2,2,7,NULL),(22,22,10,705,705,100,100,8,3,'100-100/705-705',0,0,0,0,0,0,0,0,0,3,3,14,NULL),(23,11,2,7140,7140,100,100,0,35,'100-100/7140-7140',0,0,0,0,0,0,0,0,2,15,17,1,NULL),(24,11,3,7140,7140,100,100,1,124,'100-100/7140-7140',0,0,0,0,0,0,0,1,1,14,16,3,NULL),(25,11,4,7140,7140,100,100,2,22,'100-100/7140-7140',0,0,0,0,0,0,0,0,0,22,22,2,NULL),(26,11,5,7140,7140,100,100,3,11,'100-100/7140-7140',0,0,0,0,0,0,0,0,0,11,11,7,NULL),(27,8,2,138290,138290,100,100,0,119,'100-100/138290-138290',0,0,0,0,0,0,0,1,0,19,20,1,NULL),(28,8,3,138290,138290,100,100,1,117,'100-100/138290-138290',0,0,0,0,0,0,0,1,0,17,18,4,NULL),(29,8,4,138290,138290,100,100,2,3,'100-100/138290-138290',0,0,0,0,0,0,0,0,0,3,3,6,NULL),(30,8,5,138290,138290,100,100,3,117,'100-100/138290-138290',0,0,0,0,0,0,0,1,0,17,18,3,NULL),(31,8,6,138290,138290,100,100,4,10,'100-100/138290-138290',0,0,0,0,0,0,0,0,0,10,10,9,NULL),(32,8,7,138290,138290,100,100,5,2262,'100-100/138290-138290',0,0,0,0,0,0,2,2,0,62,66,2,NULL),(33,8,8,138290,138290,100,100,6,335,'100-100/138290-138290',0,0,0,0,0,0,0,3,0,35,38,5,NULL),(34,8,9,138290,138290,100,100,7,34856,'100-100/138290-138290',0,0,0,0,0,3,4,8,1,46,62,8,NULL),(35,8,10,138290,138290,100,100,8,5,'100-100/138290-138290',0,0,0,0,0,0,0,0,0,5,5,11,NULL),(36,8,11,138290,138290,100,100,9,38,'100-100/138290-138290',0,0,0,0,0,0,0,0,2,18,20,12,NULL),(37,8,12,138290,138290,100,100,10,143,'100-100/138290-138290',0,0,0,0,0,0,0,1,2,23,26,13,NULL),(38,8,13,138290,138290,100,100,11,16,'100-100/138290-138290',0,0,0,0,0,0,0,0,0,16,16,7,NULL),(39,8,14,138290,138290,100,100,12,37,'100-100/138290-138290',0,0,0,0,0,0,0,0,2,17,19,14,NULL),(40,15,2,127035,127035,100,100,0,598446,'100-100/127035-127035',0,0,0,0,0,32,272,62,9,156,531,2,NULL),(41,15,3,127035,127035,100,100,1,368176,'100-100/127035-127035',0,0,0,0,0,22,144,40,6,116,328,5,NULL),(42,15,5,127035,127035,100,100,2,32125,'100-100/127035-127035',0,0,0,0,0,3,2,1,0,25,31,8,NULL);
/*!40000 ALTER TABLE `scan_host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_host_info`
--

LOCK TABLES `scan_host_info` WRITE;
/*!40000 ALTER TABLE `scan_host_info` DISABLE KEYS */;
INSERT INTO `scan_host_info` VALUES (1,NULL,8,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:47 2022',NULL),(2,NULL,5,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:17 2022',NULL),(3,NULL,7,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:17 2022',NULL),(4,NULL,1,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:17 2022',NULL),(5,NULL,6,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:17 2022',NULL),(6,NULL,3,2,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:37 2022',NULL),(7,NULL,2,1,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:20 2022',NULL),(8,NULL,4,3,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:39 2022',NULL),(9,NULL,9,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:29 2022',NULL),(10,NULL,12,5,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:19 2022',NULL),(11,NULL,13,6,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:17 2022',NULL),(12,NULL,11,4,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:12 2022',NULL),(13,NULL,10,NULL,NULL,'Sat Feb 26 17:35:11 2022','Sat Feb 26 17:35:17 2022',NULL),(14,4,3,2,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:30:51 2022',NULL),(15,5,2,1,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:29:30 2022',NULL),(16,6,4,3,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:32:07 2022',NULL),(17,8,14,7,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:29:32 2022',NULL),(18,NULL,9,NULL,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:30:43 2022',NULL),(19,9,12,5,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:29:36 2022',NULL),(20,9,13,6,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:30:10 2022',NULL),(21,2,11,4,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:29:21 2022',NULL),(22,9,10,NULL,NULL,'Mon Feb 28 15:28:39 2022','Mon Feb 28 15:29:37 2022',NULL),(23,1,8,NULL,NULL,'Mon Feb 28 14:44:13 2022','Mon Feb 28 14:52:41 2022',NULL),(24,3,1,NULL,NULL,'Mon Feb 28 14:44:13 2022','Mon Feb 28 14:52:57 2022',NULL),(25,4,3,2,NULL,'Mon Feb 28 14:44:13 2022','Mon Feb 28 14:52:26 2022',NULL),(26,2,11,4,NULL,'Mon Feb 28 14:44:13 2022','Mon Feb 28 14:50:37 2022',NULL),(27,1,8,NULL,NULL,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:57:29 2022',NULL),(28,2,5,NULL,NULL,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:45:53 2022',NULL),(29,NULL,7,NULL,NULL,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:45:51 2022',NULL),(30,3,1,NULL,NULL,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:53:51 2022',NULL),(31,3,6,NULL,NULL,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:47:04 2022',NULL),(32,4,3,2,1,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:58:21 2022',34),(33,5,2,1,2,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:49:56 2022',33),(34,6,4,3,3,'Sat Feb 26 17:44:32 2022','Sat Feb 26 17:52:52 2022',35),(35,NULL,9,NULL,NULL,'Sat Feb 26 17:44:33 2022','Sat Feb 26 17:52:31 2022',32),(36,7,12,5,NULL,'Sat Feb 26 17:44:33 2022','Sat Feb 26 17:46:15 2022',NULL),(37,7,13,6,NULL,'Sat Feb 26 17:44:33 2022','Sat Feb 26 17:46:03 2022',NULL),(38,2,11,4,NULL,'Sat Feb 26 17:44:33 2022','Sat Feb 26 17:52:31 2022',NULL),(39,7,10,NULL,NULL,'Sat Feb 26 17:44:33 2022','Sat Feb 26 17:46:18 2022',NULL),(40,4,3,2,1,'Mon Feb 28 15:10:01 2022','Mon Feb 28 15:13:29 2022',NULL),(41,5,2,1,2,'Mon Feb 28 15:10:01 2022','Mon Feb 28 15:13:06 2022',NULL),(42,6,4,3,3,'Mon Feb 28 15:10:01 2022','Mon Feb 28 15:12:51 2022',NULL);
/*!40000 ALTER TABLE `scan_host_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_host_response`
--

LOCK TABLES `scan_host_response` WRITE;
/*!40000 ALTER TABLE `scan_host_response` DISABLE KEYS */;
INSERT INTO `scan_host_response` VALUES (1,'2022-05-11 14:44:47',31),(2,'2022-05-11 14:44:47',31),(3,'2022-05-11 14:44:47',31),(4,'2022-05-11 14:44:47',31),(5,'2022-05-11 14:44:47',31),(6,'2022-05-11 14:44:47',31),(7,'2022-05-11 14:44:47',31),(8,'2022-05-11 14:44:47',31),(9,'2022-05-11 14:44:47',31),(10,'2022-05-11 14:44:49',31),(11,'2022-05-11 14:44:49',31),(12,'2022-05-11 14:44:49',31),(13,'2022-05-11 14:44:49',31),(14,'2022-05-11 14:45:16',31),(15,'2022-05-11 14:45:17',31),(16,'2022-05-11 14:45:17',31),(17,'2022-05-11 14:45:18',31),(18,'2022-05-11 14:45:18',31),(19,'2022-05-11 14:45:19',31),(20,'2022-05-11 14:45:19',31),(21,'2022-05-11 14:45:19',31),(22,'2022-05-11 14:45:20',31),(23,'2022-05-11 14:45:02',31),(24,'2022-05-11 14:45:05',31),(25,'2022-05-11 14:45:08',31),(26,'2022-05-11 14:45:13',31),(27,'2022-05-11 14:44:49',31),(28,'2022-05-11 14:44:49',31),(29,'2022-05-11 14:44:49',31),(30,'2022-05-11 14:44:49',31),(31,'2022-05-11 14:44:49',31),(32,'2022-05-11 14:44:50',31),(33,'2022-05-11 14:44:50',31),(34,'2022-05-11 14:44:51',31),(35,'2022-05-11 14:44:51',31),(36,'2022-05-11 14:44:52',31),(37,'2022-05-11 14:44:54',31),(38,'2022-05-11 14:45:00',31),(39,'2022-05-11 14:45:01',31),(40,'2022-05-11 14:45:14',31),(41,'2022-05-11 14:45:15',31),(42,'2022-05-11 14:45:16',31);
/*!40000 ALTER TABLE `scan_host_response` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_host_severity_level_count`
--

LOCK TABLES `scan_host_severity_level_count` WRITE;
/*!40000 ALTER TABLE `scan_host_severity_level_count` DISABLE KEYS */;
INSERT INTO `scan_host_severity_level_count` VALUES (1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1),(9,1),(10,1),(11,1),(12,1),(13,1),(21,1),(1,2),(2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(8,2),(9,2),(10,2),(11,2),(12,2),(13,2),(14,2),(15,2),(16,2),(17,2),(18,2),(19,2),(20,2),(21,2),(22,2),(25,2),(26,2),(27,2),(28,2),(29,2),(30,2),(31,2),(32,2),(33,2),(35,2),(38,2),(42,2),(1,3),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),(9,3),(10,3),(11,3),(12,3),(13,3),(14,3),(15,3),(16,3),(17,3),(18,3),(19,3),(20,3),(21,3),(22,3),(23,3),(25,3),(26,3),(29,3),(31,3),(35,3),(36,3),(38,3),(39,3),(1,4),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(8,4),(9,4),(10,4),(11,4),(12,4),(13,4),(14,4),(16,4),(17,4),(18,4),(19,4),(20,4),(21,4),(22,4),(23,4),(24,4),(25,4),(26,4),(27,4),(28,4),(29,4),(30,4),(31,4),(33,4),(35,4),(36,4),(37,4),(38,4),(39,4),(1,5),(2,5),(3,5),(4,5),(5,5),(6,5),(7,5),(8,5),(9,5),(10,5),(11,5),(12,5),(13,5),(14,5),(15,5),(16,5),(17,5),(18,5),(19,5),(20,5),(21,5),(22,5),(23,5),(24,5),(25,5),(26,5),(27,5),(28,5),(29,5),(30,5),(31,5),(32,5),(33,5),(35,5),(36,5),(37,5),(38,5),(39,5),(1,6),(2,6),(3,6),(4,6),(5,6),(6,6),(7,6),(8,6),(9,6),(10,6),(11,6),(12,6),(13,6),(14,6),(15,6),(16,6),(17,6),(18,6),(19,6),(20,6),(21,6),(22,6),(23,6),(24,6),(25,6),(26,6),(27,6),(28,6),(29,6),(30,6),(31,6),(32,6),(33,6),(34,6),(35,6),(36,6),(37,6),(38,6),(39,6),(40,6),(41,6),(42,6),(1,7),(2,7),(3,7),(4,7),(5,7),(6,7),(7,7),(8,7),(9,7),(10,7),(11,7),(12,7),(13,7),(14,7),(15,7),(16,7),(17,7),(18,7),(19,7),(20,7),(21,7),(22,7),(23,7),(24,7),(25,7),(26,7),(27,7),(28,7),(29,7),(30,7),(31,7),(32,7),(33,7),(34,7),(35,7),(36,7),(37,7),(38,7),(39,7),(40,7),(41,7),(42,7),(1,8),(2,8),(3,8),(4,8),(5,8),(6,8),(7,8),(8,8),(9,8),(10,8),(11,8),(12,8),(13,8),(14,8),(15,8),(16,8),(17,8),(18,8),(19,8),(20,8),(21,8),(22,8),(23,8),(24,8),(25,8),(26,8),(27,8),(28,8),(29,8),(30,8),(31,8),(32,8),(33,8),(34,8),(35,8),(36,8),(37,8),(38,8),(39,8),(40,8),(41,8),(42,8),(1,9),(2,9),(3,9),(4,9),(5,9),(6,9),(7,9),(8,9),(9,9),(10,9),(11,9),(12,9),(13,9),(14,9),(15,9),(16,9),(17,9),(18,9),(19,9),(20,9),(21,9),(22,9),(23,9),(24,9),(25,9),(26,9),(27,9),(28,9),(29,9),(30,9),(31,9),(32,9),(33,9),(34,9),(35,9),(36,9),(37,9),(38,9),(39,9),(40,9),(41,9),(42,9),(1,10),(2,10),(3,10),(4,10),(5,10),(6,10),(7,10),(8,10),(9,10),(10,10),(11,10),(12,10),(13,10),(14,10),(15,10),(16,10),(17,10),(18,10),(19,10),(20,10),(21,10),(22,10),(23,10),(24,10),(25,10),(26,10),(27,10),(28,10),(29,10),(30,10),(31,10),(32,10),(33,10),(34,10),(35,10),(36,10),(37,10),(38,10),(39,10),(40,10),(41,10),(42,10),(14,11),(15,12),(15,13),(16,14),(17,15),(19,15),(22,15),(29,15),(18,16),(20,17),(23,18),(23,19),(36,19),(37,19),(39,19),(24,20),(24,21),(34,21),(24,22),(27,22),(28,22),(30,22),(37,22),(42,22),(25,23),(26,24),(27,25),(28,26),(30,26),(39,26),(31,27),(32,28),(32,29),(32,30),(42,30),(33,31),(33,32),(34,33),(34,34),(34,35),(34,36),(42,36),(35,37),(36,38),(37,39),(38,40),(40,41),(40,42),(40,43),(40,44),(40,45),(41,46),(41,47),(41,48),(41,49),(41,50),(42,51);
/*!40000 ALTER TABLE `scan_host_severity_level_count` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_host_vulnerability`
--

LOCK TABLES `scan_host_vulnerability` WRITE;
/*!40000 ALTER TABLE `scan_host_vulnerability` DISABLE KEYS */;
INSERT INTO `scan_host_vulnerability` VALUES (1,661,0),(2,661,0),(3,661,0),(4,661,0),(5,661,0),(6,661,0),(7,661,0),(8,661,0),(9,661,0),(10,661,0),(11,661,0),(12,661,0),(13,661,0),(1,662,1),(2,662,1),(3,662,1),(4,662,1),(5,662,1),(6,662,1),(7,662,1),(8,662,1),(9,662,1),(10,662,1),(11,662,1),(12,662,1),(13,662,1),(21,662,1),(27,663,0),(27,664,1),(27,665,2),(27,666,3),(27,667,4),(27,668,5),(27,669,6),(27,670,7),(27,671,8),(27,672,9),(30,673,0),(31,674,0),(31,675,1),(28,676,0),(28,677,1),(28,678,2),(29,679,0),(17,680,1),(29,680,1),(28,681,3),(28,682,4),(28,683,5),(28,684,6),(28,685,7),(31,686,2),(31,687,3),(31,688,4),(30,689,1),(30,690,2),(30,691,3),(27,692,10),(35,693,0),(35,694,1),(35,695,2),(34,696,0),(34,697,1),(30,698,4),(31,699,5),(33,700,0),(33,701,1),(28,702,8),(32,703,0),(32,704,1),(32,705,2),(29,706,2),(32,707,3),(32,708,4),(32,709,5),(32,710,6),(32,711,7),(32,712,8),(32,713,9),(32,714,10),(28,715,9),(33,716,2),(31,717,6),(30,718,5),(30,719,6),(34,720,2),(34,721,3),(35,722,3),(35,723,4),(27,724,11),(27,725,12),(27,726,13),(27,727,14),(27,728,15),(27,729,16),(34,730,4),(34,731,5),(30,732,7),(30,733,8),(30,734,9),(31,735,7),(31,736,8),(33,737,3),(33,738,4),(28,739,10),(28,740,11),(32,741,11),(32,742,12),(28,743,12),(33,744,5),(33,745,6),(33,746,7),(33,747,8),(30,748,10),(30,749,11),(36,750,0),(36,751,1),(36,752,2),(36,753,3),(36,754,4),(34,755,6),(27,756,17),(34,757,7),(36,758,5),(36,759,6),(30,760,12),(33,761,9),(33,762,10),(28,763,13),(37,764,0),(32,765,13),(32,766,14),(32,767,15),(32,768,16),(32,769,17),(32,770,18),(32,771,19),(32,772,20),(32,773,21),(37,774,1),(37,775,2),(37,776,3),(37,777,4),(37,778,5),(37,779,6),(37,780,7),(37,781,8),(33,782,11),(33,783,12),(33,784,13),(33,785,14),(30,786,13),(30,787,14),(36,788,7),(34,789,8),(36,790,8),(36,791,9),(36,792,10),(36,793,11),(36,794,12),(33,795,15),(37,796,9),(39,797,0),(38,798,0),(32,799,22),(32,800,23),(38,801,1),(38,802,2),(39,803,1),(37,804,10),(33,805,16),(33,806,17),(33,807,18),(33,808,19),(33,809,20),(33,810,21),(36,811,13),(23,812,0),(23,813,1),(23,814,2),(23,815,3),(23,816,4),(23,817,5),(23,818,6),(23,819,7),(34,820,9),(34,821,10),(34,822,11),(34,823,12),(23,824,8),(23,825,9),(23,826,10),(23,827,11),(23,828,12),(23,829,13),(36,830,14),(36,831,15),(36,832,16),(33,833,22),(37,834,11),(39,835,2),(39,836,3),(39,837,4),(39,838,5),(39,839,6),(39,840,7),(39,841,8),(39,842,9),(39,843,10),(39,844,11),(39,845,12),(38,846,3),(38,847,4),(38,848,5),(38,849,6),(32,850,24),(32,851,25),(32,852,26),(32,853,27),(32,854,28),(32,855,29),(38,856,7),(39,857,13),(37,858,12),(33,859,23),(36,860,17),(23,861,14),(23,862,15),(34,863,13),(34,864,14),(34,865,15),(34,866,16),(34,867,17),(36,868,18),(33,869,24),(37,870,13),(37,871,14),(37,872,15),(37,873,16),(37,874,17),(37,875,18),(37,876,19),(37,877,20),(37,878,21),(39,879,14),(39,880,15),(38,881,8),(32,882,30),(38,883,9),(38,884,10),(39,885,16),(39,886,17),(37,887,22),(24,888,0),(33,889,25),(36,890,19),(34,891,18),(34,892,19),(34,893,20),(34,894,21),(34,895,22),(24,896,1),(37,897,23),(39,898,18),(38,899,11),(38,900,12),(32,901,31),(38,902,13),(38,903,14),(38,904,15),(25,905,0),(25,906,1),(25,907,2),(25,908,3),(24,909,2),(34,910,23),(34,911,24),(34,912,25),(34,913,26),(24,914,3),(24,915,4),(24,916,5),(24,917,6),(24,918,7),(24,919,8),(24,920,9),(25,921,4),(25,922,5),(25,923,6),(25,924,7),(26,925,0),(26,926,1),(26,927,2),(26,928,3),(26,929,4),(26,930,5),(26,931,6),(26,932,7),(32,933,32),(32,934,33),(32,935,34),(32,936,35),(32,937,36),(26,938,8),(26,939,9),(26,940,10),(42,941,0),(42,942,1),(25,943,8),(25,944,9),(25,945,10),(24,946,10),(41,947,0),(41,948,1),(40,949,0),(40,950,1),(40,951,2),(40,952,3),(34,953,27),(34,954,28),(34,955,29),(34,956,30),(40,957,4),(40,958,5),(41,959,2),(41,960,3),(41,961,4),(41,962,5),(41,963,6),(41,964,7),(24,965,11),(14,966,0),(14,967,1),(42,968,2),(42,969,3),(42,970,4),(32,971,37),(32,972,38),(42,973,5),(42,974,6),(15,975,0),(24,976,12),(41,977,8),(41,978,9),(41,979,10),(40,980,6),(34,981,31),(34,982,32),(34,983,33),(34,984,34),(40,985,7),(40,986,8),(40,987,9),(41,988,11),(24,989,13),(15,990,1),(15,991,2),(42,992,7),(42,993,8),(42,994,9),(16,995,0),(16,996,1),(16,997,2),(16,998,3),(42,999,10),(17,1000,0),(41,1001,12),(40,1002,10),(40,1003,11),(40,1004,12),(34,1005,35),(40,1006,13),(40,1007,14),(40,1008,15),(41,1009,13),(17,1010,2),(42,1011,11),(42,1012,12),(42,1013,13),(18,1014,0),(42,1015,14),(19,1016,0),(20,1016,0),(22,1016,0),(20,1017,1),(20,1018,2),(41,1019,14),(41,1020,15),(41,1021,16),(40,1022,16),(34,1023,36),(19,1024,1),(22,1024,1),(40,1025,17),(41,1026,17),(41,1027,18),(41,1028,19),(42,1029,15),(41,1030,20),(41,1031,21),(41,1032,22),(41,1033,23),(41,1034,24),(21,1035,0),(40,1036,18),(40,1037,19),(40,1038,20),(40,1039,21),(40,1040,22),(40,1041,23),(40,1042,24),(40,1043,25),(40,1044,26),(40,1045,27),(40,1046,28),(19,1047,2),(22,1047,2),(34,1048,37),(40,1049,29),(40,1050,30),(40,1051,31),(40,1052,32),(40,1053,33),(41,1054,25),(41,1055,26),(41,1056,27),(41,1057,28),(41,1058,29),(42,1059,16),(42,1060,17),(41,1061,30),(41,1062,31),(40,1063,34),(34,1064,38),(40,1065,35),(40,1066,36),(41,1067,32),(42,1068,18),(41,1069,33),(41,1070,34),(41,1071,35),(41,1072,36),(40,1073,37),(34,1074,39),(40,1075,38),(40,1076,39),(40,1077,40),(40,1078,41),(40,1079,42),(40,1080,43),(41,1081,37),(41,1082,38),(41,1083,39),(42,1084,19),(41,1085,40),(40,1086,44),(34,1087,40),(34,1088,41),(34,1089,42),(40,1090,45),(40,1091,46),(41,1092,41),(40,1093,47),(34,1094,43),(34,1095,44),(34,1096,45),(34,1097,46),(40,1098,48),(41,1099,42),(41,1100,43),(41,1101,44),(40,1102,49),(34,1103,47),(34,1104,48),(40,1105,50),(41,1106,45),(40,1107,51),(34,1108,49),(40,1109,52),(40,1110,53),(40,1111,54),(40,1112,55),(40,1113,56),(41,1114,46),(41,1115,47),(40,1116,57),(40,1117,58),(40,1118,59),(40,1119,60),(40,1120,61),(40,1121,62),(40,1122,63),(34,1123,50),(40,1124,64),(40,1125,65),(40,1126,66),(41,1127,48),(41,1128,49),(41,1129,50),(40,1130,67),(40,1131,68),(40,1132,69),(40,1133,70),(41,1134,51),(41,1135,52),(41,1136,53),(40,1137,71),(40,1138,72),(40,1139,73),(40,1140,74),(41,1141,54),(40,1142,75),(40,1143,76),(41,1144,55),(41,1145,56),(41,1146,57),(41,1147,58),(40,1148,77),(40,1149,78),(41,1150,59),(40,1151,79),(41,1152,60),(41,1153,61),(41,1154,62),(40,1155,80),(41,1156,63),(41,1157,64),(41,1158,65),(41,1159,66),(41,1160,67),(41,1161,68),(41,1162,69),(41,1163,70),(40,1164,81),(41,1165,71),(40,1166,82),(41,1167,72),(40,1168,83),(41,1169,73),(40,1170,84),(41,1171,74),(40,1172,85),(40,1173,86),(41,1174,75),(40,1175,87),(41,1176,76),(40,1177,88),(40,1178,89),(41,1179,77),(40,1180,90),(40,1181,91),(41,1182,78),(40,1183,92),(41,1184,79),(41,1185,80),(40,1186,93),(41,1187,81),(41,1188,82),(41,1189,83),(41,1190,84),(40,1191,94),(41,1192,85),(41,1193,86),(41,1194,87),(41,1195,88),(40,1196,95),(40,1197,96),(40,1198,97),(40,1199,98),(41,1200,89),(40,1201,99),(40,1202,100),(41,1203,90),(40,1204,101),(41,1205,91),(40,1206,102),(41,1207,92),(40,1208,103),(41,1209,93),(40,1210,104),(41,1211,94),(40,1212,105),(41,1213,95),(40,1214,106),(41,1215,96),(40,1216,107),(41,1217,97),(40,1218,108),(41,1219,98),(40,1220,109),(41,1221,99),(40,1222,110),(41,1223,100),(40,1224,111),(41,1225,101),(40,1226,112),(41,1227,102),(40,1228,113),(41,1229,103),(40,1230,114),(41,1231,104),(40,1232,115),(41,1233,105),(40,1234,116),(41,1235,106),(40,1236,117),(41,1237,107),(40,1238,118),(41,1239,108),(40,1240,119),(40,1241,120),(41,1242,109),(40,1243,121),(41,1244,110),(40,1245,122),(41,1246,111),(40,1247,123),(41,1248,112),(41,1249,113),(40,1250,124),(41,1251,114),(40,1252,125),(41,1253,115),(41,1254,116),(41,1255,117),(41,1256,118),(41,1257,119),(41,1258,120),(41,1259,121),(40,1260,126),(41,1261,122),(41,1262,123),(40,1263,127),(41,1264,124),(41,1265,125),(40,1266,128),(41,1267,126),(40,1268,129),(41,1269,127),(41,1270,128),(41,1271,129),(41,1272,130),(41,1273,131),(41,1274,132),(41,1275,133),(40,1276,130),(41,1277,134),(40,1278,131),(41,1279,135),(41,1280,136),(40,1281,132),(41,1282,137),(41,1283,138),(40,1284,133),(41,1285,139),(41,1286,140),(40,1287,134),(41,1288,141),(40,1289,135),(41,1290,142),(41,1291,143),(41,1292,144),(41,1293,145),(41,1294,146),(40,1295,136),(41,1296,147),(40,1297,137),(41,1298,148),(40,1299,138),(41,1300,149),(41,1301,150),(41,1302,151),(41,1303,152),(40,1304,139),(41,1305,153),(40,1306,140),(41,1307,154),(40,1308,141),(41,1309,155),(41,1310,156),(41,1311,157),(41,1312,158),(41,1313,159),(40,1314,142),(41,1315,160),(40,1316,143),(41,1317,161),(40,1318,144),(41,1319,162),(41,1320,163),(40,1321,145),(41,1322,164),(40,1323,146),(40,1324,147),(41,1325,165),(41,1326,166),(40,1327,148),(41,1328,167),(40,1329,149),(41,1330,168),(40,1331,150),(40,1332,151),(41,1333,169),(40,1334,152),(41,1335,170),(40,1336,153),(41,1337,171),(40,1338,154),(41,1339,172),(40,1340,155),(40,1341,156),(40,1342,157),(41,1343,173),(40,1344,158),(40,1345,159),(41,1346,174),(40,1347,160),(40,1348,161),(40,1349,162),(41,1350,175),(40,1351,163),(40,1352,164),(40,1353,165),(41,1354,176),(40,1355,166),(40,1356,167),(41,1357,177),(40,1358,168),(41,1359,178),(40,1360,169),(40,1361,170),(41,1362,179),(40,1363,171),(41,1364,180),(40,1365,172),(40,1366,173),(41,1367,181),(40,1368,174),(41,1369,182),(40,1370,175),(40,1371,176),(40,1372,177),(40,1373,178),(40,1374,179),(40,1375,180),(41,1376,183),(40,1377,181),(41,1378,184),(40,1379,182),(41,1380,185),(40,1381,183),(40,1382,184),(41,1383,186),(40,1384,185),(40,1385,186),(41,1386,187),(40,1387,187),(40,1388,188),(41,1389,188),(40,1390,189),(40,1391,190),(40,1392,191),(40,1393,192),(40,1394,193),(40,1395,194),(40,1396,195),(41,1397,189),(40,1398,196),(41,1399,190),(40,1400,197),(40,1401,198),(40,1402,199),(41,1403,191),(40,1404,200),(40,1405,201),(40,1406,202),(40,1407,203),(40,1408,204),(40,1409,205),(40,1410,206),(41,1411,192),(40,1412,207),(41,1413,193),(40,1414,208),(41,1415,194),(40,1416,209),(41,1417,195),(40,1418,210),(41,1419,196),(40,1420,211),(40,1421,212),(41,1422,197),(40,1423,213),(40,1424,214),(41,1425,198),(40,1426,215),(41,1427,199),(40,1428,216),(41,1429,200),(40,1430,217),(41,1431,201),(40,1432,218),(41,1433,202),(40,1434,219),(40,1435,220),(41,1436,203),(40,1437,221),(40,1438,222),(41,1439,204),(40,1440,223),(41,1441,205),(40,1442,224),(40,1443,225),(41,1444,206),(40,1445,226),(40,1446,227),(40,1447,228),(40,1448,229),(41,1449,207),(40,1450,230),(40,1451,231),(40,1452,232),(40,1453,233),(41,1454,208),(40,1455,234),(40,1456,235),(40,1457,236),(41,1458,209),(40,1459,237),(40,1460,238),(40,1461,239),(41,1462,210),(40,1463,240),(40,1464,241),(40,1465,242),(40,1466,243),(40,1467,244),(41,1468,211),(40,1469,245),(40,1470,246),(41,1471,212),(40,1472,247),(40,1473,248),(40,1474,249),(41,1475,213),(40,1476,250),(40,1477,251),(41,1478,214),(40,1479,252),(40,1480,253),(40,1481,254),(41,1482,215),(40,1483,255),(40,1484,256),(41,1485,216),(40,1486,257),(41,1487,217),(41,1488,218),(40,1489,258),(41,1490,219),(40,1491,259),(41,1492,220),(40,1493,260),(41,1494,221),(40,1495,261),(41,1496,222),(41,1497,223),(41,1498,224),(41,1499,225),(40,1500,262),(41,1501,226),(40,1502,263),(41,1503,227),(40,1504,264),(41,1505,228),(41,1506,229),(41,1507,230),(40,1508,265),(41,1509,231),(40,1510,266),(41,1511,232),(40,1512,267),(41,1513,233),(41,1514,234),(40,1515,268),(41,1516,235),(41,1517,236),(41,1518,237),(40,1519,269),(41,1520,238),(40,1521,270),(41,1522,239),(41,1523,240),(41,1524,241),(40,1525,271),(41,1526,242),(40,1527,272),(41,1528,243),(41,1529,244),(41,1530,245),(41,1531,246),(40,1532,273),(41,1533,247),(41,1534,248),(40,1535,274),(41,1536,249),(40,1537,275),(41,1538,250),(41,1539,251),(40,1540,276),(41,1541,252),(40,1542,277),(41,1543,253),(41,1544,254),(40,1545,278),(41,1546,255),(41,1547,256),(41,1548,257),(40,1549,279),(41,1550,258),(41,1551,259),(41,1552,260),(40,1553,280),(41,1554,261),(40,1555,281),(41,1556,262),(41,1557,263),(40,1558,282),(41,1559,264),(40,1560,283),(41,1561,265),(40,1562,284),(41,1563,266),(40,1564,285),(41,1565,267),(40,1566,286),(41,1567,268),(41,1568,269),(41,1569,270),(41,1570,271),(41,1571,272),(41,1572,273),(41,1573,274),(41,1574,275),(41,1575,276),(41,1576,277),(41,1577,278),(41,1578,279),(41,1579,280),(40,1580,287),(40,1581,288),(40,1582,289),(40,1583,290),(40,1584,291),(40,1585,292),(40,1586,293),(40,1587,294),(40,1588,295),(40,1589,296),(40,1590,297),(40,1591,298),(40,1592,299),(40,1593,300),(40,1594,301),(40,1595,302),(40,1596,303),(40,1597,304),(40,1598,305),(40,1599,306),(40,1600,307),(40,1601,308),(40,1602,309),(40,1603,310),(40,1604,311),(40,1605,312),(40,1606,313),(40,1607,314),(40,1608,315),(40,1609,316),(40,1610,317),(40,1611,318),(40,1612,319),(40,1613,320),(40,1614,321),(40,1615,322),(40,1616,323),(40,1617,324),(40,1618,325),(40,1619,326),(40,1620,327),(40,1621,328),(40,1622,329),(40,1623,330),(40,1624,331),(40,1625,332),(40,1626,333),(40,1627,334),(40,1628,335),(40,1629,336),(40,1630,337),(40,1631,338),(40,1632,339),(40,1633,340),(40,1634,341),(40,1635,342),(40,1636,343),(40,1637,344),(40,1638,345),(40,1639,346),(40,1640,347),(40,1641,348),(40,1642,349),(40,1643,350),(40,1644,351),(40,1645,352),(40,1646,353),(40,1647,354),(40,1648,355),(40,1649,356),(40,1650,357),(40,1651,358),(40,1652,359),(40,1653,360),(40,1654,361),(40,1655,362),(40,1656,363),(40,1657,364),(40,1658,365),(40,1659,366),(40,1660,367),(40,1661,368),(40,1662,369),(40,1663,370),(40,1664,371),(40,1665,372),(40,1666,373),(40,1667,374),(40,1668,375),(40,1669,376),(40,1670,377),(40,1671,378),(40,1672,379),(40,1673,380),(40,1674,381),(40,1675,382),(40,1676,383),(40,1677,384),(40,1678,385),(40,1679,386),(40,1680,387),(40,1681,388),(40,1682,389),(40,1683,390),(40,1684,391),(40,1685,392),(40,1686,393),(40,1687,394),(40,1688,395),(40,1689,396),(40,1690,397),(40,1691,398),(40,1692,399),(40,1693,400),(40,1694,401),(40,1695,402),(40,1696,403),(40,1697,404),(40,1698,405),(40,1699,406),(40,1700,407),(40,1701,408),(40,1702,409),(40,1703,410),(40,1704,411),(40,1705,412),(40,1706,413),(40,1707,414),(40,1708,415),(40,1709,416),(40,1710,417),(40,1711,418),(40,1712,419),(40,1713,420),(40,1714,421),(40,1715,422),(40,1716,423),(40,1717,424),(40,1718,425),(40,1719,426),(40,1720,427),(40,1721,428),(40,1722,429),(40,1723,430),(40,1724,431),(40,1725,432),(40,1726,433),(40,1727,434),(40,1728,435),(40,1729,436),(40,1730,437),(40,1731,438),(40,1732,439),(40,1733,440),(40,1734,441),(40,1735,442),(40,1736,443),(40,1737,444),(40,1738,445),(40,1739,446),(40,1740,447),(40,1741,448),(40,1742,449);
/*!40000 ALTER TABLE `scan_host_vulnerability` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_info`
--

LOCK TABLES `scan_info` WRITE;
/*!40000 ALTER TABLE `scan_info` DISABLE KEYS */;
INSERT INTO `scan_info` VALUES (5,5,'My Host Discovery Scan',NULL,1,1,NULL,1,'2022-02-26 23:35:11','2022-02-26 23:35:47',0,14,1,0,0,0,'2022-02-26 23:35:49',1,0,0,0,NULL,'2022-02-26 23:35:11','2022-02-26 23:35:49',1,1,1,0,5,1,0,NULL,'202202261215',NULL,0,128,1,0,0,1,2,3,2),(8,4,'My Basic Network Scan',NULL,1,1,NULL,5,'2022-02-26 23:44:32','2022-02-26 23:58:24',0,14,1,0,0,0,'2022-02-26 23:58:29',5,0,0,0,NULL,'2022-02-26 23:44:32','2022-02-26 23:58:29',1,1,1,0,8,2,0,NULL,'202202261215',NULL,0,128,5,0,0,1,2,3,2),(11,3,'WebApp-Policy',NULL,1,1,NULL,2,'2022-02-28 20:44:13','2022-02-28 20:52:57',0,5,1,0,0,0,'2022-02-28 20:52:58',2,0,0,0,NULL,'2022-02-28 20:44:13','2022-02-28 20:52:58',1,1,1,0,11,2,0,NULL,'202202281705',NULL,0,128,2,0,0,1,2,3,2),(15,2,'Credentialed-Patch-Scan',NULL,1,1,NULL,4,'2022-02-28 21:10:01','2022-02-28 21:13:29',0,4,1,0,0,0,'2022-02-28 21:13:34',4,0,0,0,NULL,'2022-02-28 21:10:01','2022-02-28 21:13:34',1,1,1,0,15,2,0,NULL,'202202281705',NULL,0,128,4,0,0,1,2,3,2),(22,1,'Spectre/Meltdown',NULL,1,1,NULL,3,'2022-02-28 21:28:39','2022-02-28 21:32:07',0,10,1,0,0,0,'2022-02-28 21:32:09',3,0,0,0,NULL,'2022-02-28 21:28:39','2022-02-28 21:32:09',1,1,1,0,22,2,0,NULL,'202202281705',NULL,0,128,3,0,0,1,2,3,2);
/*!40000 ALTER TABLE `scan_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_info_acl`
--

LOCK TABLES `scan_info_acl` WRITE;
/*!40000 ALTER TABLE `scan_info_acl` DISABLE KEYS */;
INSERT INTO `scan_info_acl` VALUES (5,1,0),(8,1,0),(11,1,0),(15,1,0),(22,1,0),(5,2,1),(8,2,1),(11,2,1),(15,2,1),(22,2,1);
/*!40000 ALTER TABLE `scan_info_acl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_info_severity_base_selection`
--

LOCK TABLES `scan_info_severity_base_selection` WRITE;
/*!40000 ALTER TABLE `scan_info_severity_base_selection` DISABLE KEYS */;
INSERT INTO `scan_info_severity_base_selection` VALUES (5,1,0),(8,1,0),(11,1,0),(15,1,0),(22,1,0),(5,2,1),(8,2,1),(11,2,1),(15,2,1),(22,2,1),(5,3,2),(8,3,2),(11,3,2),(15,3,2),(22,3,2);
/*!40000 ALTER TABLE `scan_info_severity_base_selection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_plugin`
--

LOCK TABLES `scan_plugin` WRITE;
/*!40000 ALTER TABLE `scan_plugin` DISABLE KEYS */;
INSERT INTO `scan_plugin` VALUES (1,22,1,1,0),(2,11,2,1,0),(3,8,3,2,0),(4,8,4,1,1),(5,8,5,1,2),(6,8,6,1,3),(7,8,7,1,4),(8,8,8,1,5),(9,8,9,2,6),(10,8,10,1,7),(11,8,2,1,8),(12,8,11,1,9),(13,15,12,1,0),(14,15,13,1,1),(15,15,14,1,2),(16,15,15,1,3),(17,15,16,1,4),(18,15,17,2,5),(19,15,18,2,6),(20,15,19,1,7),(21,15,20,1,8),(22,15,21,2,9);
/*!40000 ALTER TABLE `scan_plugin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_plugin_host`
--

LOCK TABLES `scan_plugin_host` WRITE;
/*!40000 ALTER TABLE `scan_plugin_host` DISABLE KEYS */;
INSERT INTO `scan_plugin_host` VALUES (1,1,0),(14,1,0),(16,1,0),(18,1,1),(22,1,1),(2,2,0),(3,3,0),(7,3,0),(9,3,0),(3,4,1),(4,4,0),(5,4,0),(6,4,0),(8,4,0),(10,4,0),(12,4,0),(9,5,1),(11,6,0),(13,7,0),(15,7,0),(17,7,0),(18,7,0),(19,7,0),(20,7,0),(21,7,0),(22,7,0),(19,8,1);
/*!40000 ALTER TABLE `scan_plugin_host` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `scan_remediation`
--

LOCK TABLES `scan_remediation` WRITE;
/*!40000 ALTER TABLE `scan_remediation` DISABLE KEYS */;
INSERT INTO `scan_remediation` VALUES (1,8,1,1,2,0),(2,15,2,1,159,0),(3,15,3,1,96,1),(4,15,4,1,59,2),(5,15,5,2,24,3),(6,15,6,1,21,4),(7,15,7,2,16,5),(8,15,8,2,8,6),(9,15,9,2,8,7),(10,15,10,2,8,8),(11,15,11,2,6,9),(12,15,12,2,6,10),(13,15,13,1,5,11),(14,15,14,2,4,12),(15,15,15,2,4,13),(16,15,16,2,4,14),(17,15,17,1,3,15),(18,15,18,1,3,16),(19,15,1,1,2,17),(20,15,19,1,2,18),(21,15,20,1,2,19),(22,15,21,1,2,20),(23,15,22,2,2,21),(24,15,23,2,2,22),(25,15,24,2,2,23),(26,15,25,2,2,24),(27,15,26,2,2,25),(28,15,27,2,2,26),(29,15,28,2,2,27),(30,15,29,2,2,28),(31,15,30,1,1,29),(32,15,31,1,1,30),(33,15,32,1,1,31),(34,15,33,1,1,32),(35,15,34,1,1,33),(36,15,35,1,1,34),(37,15,36,1,1,35),(38,15,37,1,1,36),(39,15,38,1,1,37),(40,15,39,1,1,38);
/*!40000 ALTER TABLE `scan_remediation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_remediations_summary`
--

LOCK TABLES `scan_remediations_summary` WRITE;
/*!40000 ALTER TABLE `scan_remediations_summary` DISABLE KEYS */;
INSERT INTO `scan_remediations_summary` VALUES (5,13,0,0,0,NULL),(8,13,57,1,2,NULL),(11,4,0,0,0,NULL),(15,3,5291,3,468,NULL),(22,9,15,0,0,NULL);
/*!40000 ALTER TABLE `scan_remediations_summary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_response`
--

LOCK TABLES `scan_response` WRITE;
/*!40000 ALTER TABLE `scan_response` DISABLE KEYS */;
INSERT INTO `scan_response` VALUES (5,'2022-05-11 14:43:19',0,1),(8,'2022-05-11 14:43:21',4,1),(11,'2022-05-11 14:43:19',2,1),(15,'2022-05-11 14:43:20',4,1),(22,'2022-05-11 14:43:19',3,1);
/*!40000 ALTER TABLE `scan_response` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `scan_vulnerability`
--

LOCK TABLES `scan_vulnerability` WRITE;
/*!40000 ALTER TABLE `scan_vulnerability` DISABLE KEYS */;
INSERT INTO `scan_vulnerability` VALUES (1,5,0),(2,5,1),(3,22,0),(4,22,1),(5,22,2),(6,22,3),(7,22,4),(8,22,5),(9,22,6),(10,22,7),(11,22,8),(12,11,0),(13,11,1),(14,11,2),(15,11,3),(16,11,4),(17,11,5),(18,11,6),(19,11,7),(20,11,8),(21,11,9),(22,11,10),(23,11,11),(24,11,12),(25,11,13),(26,11,14),(27,11,15),(28,11,16),(29,11,17),(30,11,18),(31,11,19),(32,11,20),(33,11,21),(34,11,22),(35,8,0),(36,8,1),(37,8,2),(38,8,3),(39,8,4),(40,8,5),(41,8,6),(42,8,7),(43,8,8),(44,8,9),(45,8,10),(46,8,11),(47,8,12),(48,8,13),(49,8,14),(50,8,15),(51,8,16),(52,8,17),(53,8,18),(54,8,19),(55,8,20),(56,8,21),(57,8,22),(58,8,23),(59,8,24),(60,8,25),(61,8,26),(62,8,27),(63,8,28),(64,8,29),(65,8,30),(66,8,31),(67,8,32),(68,8,33),(69,8,34),(70,8,35),(71,8,36),(72,8,37),(73,8,38),(74,8,39),(75,8,40),(76,8,41),(77,8,42),(78,8,43),(79,8,44),(80,8,45),(81,8,46),(82,8,47),(83,8,48),(84,8,49),(85,8,50),(86,8,51),(87,8,52),(88,8,53),(89,8,54),(90,8,55),(91,8,56),(92,8,57),(93,8,58),(94,8,59),(95,8,60),(96,8,61),(97,8,62),(98,8,63),(99,8,64),(100,8,65),(101,8,66),(102,8,67),(103,8,68),(104,8,69),(105,8,70),(106,8,71),(107,8,72),(108,8,73),(109,8,74),(110,8,75),(111,8,76),(112,8,77),(113,8,78),(114,8,79),(115,8,80),(116,8,81),(117,8,82),(118,8,83),(119,8,84),(120,8,85),(121,8,86),(122,8,87),(123,8,88),(124,8,89),(125,8,90),(126,15,0),(127,15,1),(128,15,2),(129,15,3),(130,15,4),(131,15,5),(132,15,6),(133,15,7),(134,15,8),(135,15,9),(136,15,10),(137,15,11),(138,15,12),(139,15,13),(140,15,14),(141,15,15),(142,15,16),(143,15,17),(144,15,18),(145,15,19),(146,15,20),(147,15,21),(148,15,22),(149,15,23),(150,15,24),(151,15,25),(152,15,26),(153,15,27),(154,15,28),(155,15,29),(156,15,30),(157,15,31),(158,15,32),(159,15,33),(160,15,34),(161,15,35),(162,15,36),(163,15,37),(164,15,38),(165,15,39),(166,15,40),(167,15,41),(168,15,42),(169,15,43),(170,15,44),(171,15,45),(172,15,46),(173,15,47),(174,15,48),(175,15,49),(176,15,50),(177,15,51),(178,15,52),(179,15,53),(180,15,54),(181,15,55),(182,15,56),(183,15,57),(184,15,58),(185,15,59),(186,15,60),(187,15,61),(188,15,62),(189,15,63),(190,15,64),(191,15,65),(192,15,66),(193,15,67),(194,15,68),(195,15,69),(196,15,70),(197,15,71),(198,15,72),(199,15,73),(200,15,74),(201,15,75),(202,15,76),(203,15,77),(204,15,78),(205,15,79),(206,15,80),(207,15,81),(208,15,82),(209,15,83),(210,15,84),(211,15,85),(212,15,86),(213,15,87),(214,15,88),(215,15,89),(216,15,90),(217,15,91),(218,15,92),(219,15,93),(220,15,94),(221,15,95),(222,15,96),(223,15,97),(224,15,98),(225,15,99),(226,15,100),(227,15,101),(228,15,102),(229,15,103),(230,15,104),(231,15,105),(232,15,106),(233,15,107),(234,15,108),(235,15,109),(236,15,110),(237,15,111),(238,15,112),(239,15,113),(240,15,114),(241,15,115),(242,15,116),(243,15,117),(244,15,118),(245,15,119),(246,15,120),(247,15,121),(248,15,122),(249,15,123),(250,15,124),(251,15,125),(252,15,126),(253,15,127),(254,15,128),(255,15,129),(256,15,130),(257,15,131),(258,15,132),(259,15,133),(260,15,134),(261,15,135),(262,15,136),(263,15,137),(264,15,138),(265,15,139),(266,15,140),(267,15,141),(268,15,142),(269,15,143),(270,15,144),(271,15,145),(272,15,146),(273,15,147),(274,15,148),(275,15,149),(276,15,150),(277,15,151),(278,15,152),(279,15,153),(280,15,154),(281,15,155),(282,15,156),(283,15,157),(284,15,158),(285,15,159),(286,15,160),(287,15,161),(288,15,162),(289,15,163),(290,15,164),(291,15,165),(292,15,166),(293,15,167),(294,15,168),(295,15,169),(296,15,170),(297,15,171),(298,15,172),(299,15,173),(300,15,174),(301,15,175),(302,15,176),(303,15,177),(304,15,178),(305,15,179),(306,15,180),(307,15,181),(308,15,182),(309,15,183),(310,15,184),(311,15,185),(312,15,186),(313,15,187),(314,15,188),(315,15,189),(316,15,190),(317,15,191),(318,15,192),(319,15,193),(320,15,194),(321,15,195),(322,15,196),(323,15,197),(324,15,198),(325,15,199),(326,15,200),(327,15,201),(328,15,202),(329,15,203),(330,15,204),(331,15,205),(332,15,206),(333,15,207),(334,15,208),(335,15,209),(336,15,210),(337,15,211),(338,15,212),(339,15,213),(340,15,214),(341,15,215),(342,15,216),(343,15,217),(344,15,218),(345,15,219),(346,15,220),(347,15,221),(348,15,222),(349,15,223),(350,15,224),(351,15,225),(352,15,226),(353,15,227),(354,15,228),(355,15,229),(356,15,230),(357,15,231),(358,15,232),(359,15,233),(360,15,234),(361,15,235),(362,15,236),(363,15,237),(364,15,238),(365,15,239),(366,15,240),(367,15,241),(368,15,242),(369,15,243),(370,15,244),(371,15,245),(372,15,246),(373,15,247),(374,15,248),(375,15,249),(376,15,250),(377,15,251),(378,15,252),(379,15,253),(380,15,254),(381,15,255),(382,15,256),(383,15,257),(384,15,258),(385,15,259),(386,15,260),(387,15,261),(388,15,262),(389,15,263),(390,15,264),(391,15,265),(392,15,266),(393,15,267),(394,15,268),(395,15,269),(396,15,270),(397,15,271),(398,15,272),(399,15,273),(400,15,274),(401,15,275),(402,15,276),(403,15,277),(404,15,278),(405,15,279),(406,15,280),(407,15,281),(408,15,282),(409,15,283),(410,15,284),(411,15,285),(412,15,286),(413,15,287),(414,15,288),(415,15,289),(416,15,290),(417,15,291),(418,15,292),(419,15,293),(420,15,294),(421,15,295),(422,15,296),(423,15,297),(424,15,298),(425,15,299),(426,15,300),(427,15,301),(428,15,302),(429,15,303),(430,15,304),(431,15,305),(432,15,306),(433,15,307),(434,15,308),(435,15,309),(436,15,310),(437,15,311),(438,15,312),(439,15,313),(440,15,314),(441,15,315),(442,15,316),(443,15,317),(444,15,318),(445,15,319),(446,15,320),(447,15,321),(448,15,322),(449,15,323),(450,15,324),(451,15,325),(452,15,326),(453,15,327),(454,15,328),(455,15,329),(456,15,330),(457,15,331),(458,15,332),(459,15,333),(460,15,334),(461,15,335),(462,15,336),(463,15,337),(464,15,338),(465,15,339),(466,15,340),(467,15,341),(468,15,342),(469,15,343),(470,15,344),(471,15,345),(472,15,346),(473,15,347),(474,15,348),(475,15,349),(476,15,350),(477,15,351),(478,15,352),(479,15,353),(480,15,354),(481,15,355),(482,15,356),(483,15,357),(484,15,358),(485,15,359),(486,15,360),(487,15,361),(488,15,362),(489,15,363),(490,15,364),(491,15,365),(492,15,366),(493,15,367),(494,15,368),(495,15,369),(496,15,370),(497,15,371),(498,15,372),(499,15,373),(500,15,374),(501,15,375),(502,15,376),(503,15,377),(504,15,378),(505,15,379),(506,15,380),(507,15,381),(508,15,382),(509,15,383),(510,15,384),(511,15,385),(512,15,386),(513,15,387),(514,15,388),(515,15,389),(516,15,390),(517,15,391),(518,15,392),(519,15,393),(520,15,394),(521,15,395),(522,15,396),(523,15,397),(524,15,398),(525,15,399),(526,15,400),(527,15,401),(528,15,402),(529,15,403),(530,15,404),(531,15,405),(532,15,406),(533,15,407),(534,15,408),(535,15,409),(536,15,410),(537,15,411),(538,15,412),(539,15,413),(540,15,414),(541,15,415),(542,15,416),(543,15,417),(544,15,418),(545,15,419),(546,15,420),(547,15,421),(548,15,422),(549,15,423),(550,15,424),(551,15,425),(552,15,426),(553,15,427),(554,15,428),(555,15,429),(556,15,430),(557,15,431),(558,15,432),(559,15,433),(560,15,434),(561,15,435),(562,15,436),(563,15,437),(564,15,438),(565,15,439),(566,15,440),(567,15,441),(568,15,442),(569,15,443),(570,15,444),(571,15,445),(572,15,446),(573,15,447),(574,15,448),(575,15,449),(576,15,450),(577,15,451),(578,15,452),(579,15,453),(580,15,454),(581,15,455),(582,15,456),(583,15,457),(584,15,458),(585,15,459),(586,15,460),(587,15,461),(588,15,462),(589,15,463),(590,15,464),(591,15,465),(592,15,466),(593,15,467),(594,15,468),(595,15,469),(596,15,470),(597,15,471),(598,15,472),(599,15,473),(600,15,474),(601,15,475),(602,15,476),(603,15,477),(604,15,478),(605,15,479),(606,15,480),(607,15,481),(608,15,482),(609,15,483),(610,15,484),(611,15,485),(612,15,486),(613,15,487),(614,15,488),(615,15,489),(616,15,490),(617,15,491),(618,15,492),(619,15,493),(620,15,494),(621,15,495),(622,15,496),(623,15,497),(624,15,498),(625,15,499),(626,15,500),(627,15,501),(628,15,502),(629,15,503),(630,15,504),(631,15,505),(632,15,506),(633,15,507),(634,15,508),(635,15,509),(636,15,510),(637,15,511),(638,15,512),(639,15,513),(640,15,514),(641,15,515),(642,15,516),(643,15,517),(644,15,518),(645,15,519),(646,15,520),(647,15,521),(648,15,522),(649,15,523),(650,15,524),(651,15,525),(652,15,526),(653,15,527),(654,15,528),(655,15,529),(656,15,530),(657,15,531),(658,15,532),(659,15,533),(660,15,534);
/*!40000 ALTER TABLE `scan_vulnerability` ENABLE KEYS */;
UNLOCK TABLES;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-05-11 10:00:20
