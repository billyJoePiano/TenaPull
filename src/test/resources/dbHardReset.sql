-- Drop tables in reverse order from create

-- Join tables
drop table if exists `scan_host_vulnerability`;
drop table if exists `scan_vulnerability`;
drop table if exists `scan_plugin_scan_host`;
drop table if exists `scan_host_severity_level_count`;
drop table if exists `scan_info_severity_base_selection`;
drop table if exists `scan_info_acl`;
drop table if exists `plugin_attributes_see_also`;
drop table if exists `plugin_ref_information_value`;
drop table if exists `plugin_attributes_ref_information`;

-- Scan data
drop table if exists `scan_host_info`;
drop table if exists `scan_host_response`;
drop table if exists `scan_history`;
drop table if exists `scan_remediation`;
drop table if exists `scan_remediations_summary`;
drop table if exists `scan_plugin_host`;
drop table if exists `scan_plugin`;
drop table if exists `scan_prioritization`;
drop table if exists `scan_host`;
drop table if exists `scan_info`;
drop table if exists `scan_response`;
drop table if exists `scan`;
drop table if exists `folder`;

-- Plugin lookups, and other complex lookups
drop table if exists `vulnerability`;
drop table if exists `plugin_ref_information`;
drop table if exists `plugin_attributes`;
drop table if exists `plugin_vuln_information`;
drop table if exists `plugin_information`;
drop table if exists `plugin_risk_information`;
drop table if exists `severity_level_count`;
drop table if exists `scan_group`;
drop table if exists `license`;
drop table if exists `severity_base`;
drop table if exists `acl`;

-- Simple lookup tables
drop table if exists `host_ip`;
drop table if exists `operating_system`;
drop table if exists `plugin_see_also`;
drop table if exists `plugin_ref_value`;
drop table if exists `plugin_family`;
drop table if exists `plugin_name`;
drop table if exists `policy_template_uuid`;
drop table if exists `scan_uuid`;
drop table if exists `scan_schedule_type`;
drop table if exists `scan_status`;
drop table if exists `scanner`;
drop table if exists `timezone`;
drop table if exists `scan_type`;
drop table if exists `scan_policy`;
drop table if exists `scan_owner_id`;
drop table if exists `scan_owner`;



--
-- SIMPLE LOOKUPS
--

create table scan_owner (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table scan_owner_id (
    id int primary key, -- nessus id
    lookup_id int unique,
    constraint foreign key (lookup_id) references scan_owner (id)
);

create table scan_type (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table timezone (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table scan_policy (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table scanner (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table scan_status (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table scan_schedule_type (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table scan_uuid (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table policy_template_uuid (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table plugin_name (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table plugin_family (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table plugin_ref_value (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table plugin_see_also (
    id int auto_increment primary key,
    value varchar(255) not null unique
);

create table operating_system (
    id    int auto_increment primary key,
    value varchar(255) not null unique
);

create table host_ip (
    id    int auto_increment primary key,
    value varchar(255) not null unique
);


--
-- COMPLEX LOOKUPS
--

create table acl
(
    id           int          not null  primary key,
    owner        int          null,
    name         varchar(255) null,
    type         varchar(255) null,
    permissions  int          null,
    display_name varchar(255) null,
    _extra_json  longtext null
);

create table severity_base
(
    id      int auto_increment,
    value   varchar(255) null,
    display varchar(255) null,
    _extra_json  longtext null,
    constraint severity_base_pk
        primary key (id)
);

create table license
(
    id int auto_increment primary key,
    `limit` varchar(255) null,
    trimmed varchar(255) null,
    _extra_json longtext null
);


create table scan_group (
    id int primary key auto_increment,
    _extra_json  longtext null
);

create table severity_level_count (
    id int PRIMARY KEY AUTO_INCREMENT,
    `count` int null,
    severity_level int null,
    _extra_json longtext null
);

create table plugin_risk_information (
    id int auto_increment primary key,
    cvss_temporal_vector varchar(255) null,
    risk_factor varchar(255) null,
    cvss_vector varchar(255) null,
    cvss_temporal_score varchar(255) null,
    cvss3_base_score varchar(255) null,
    cvss3_temporal_vector varchar(255) null,
    cvss3_temporal_score varchar(255) null,
    cvss3_vector varchar(255) null,
    _extra_json longtext null
);

create table plugin_information (
    id int auto_increment primary key,
    plugin_version varchar(255) null,
    plugin_id int null,
    plugin_type varchar(255) null,
    plugin_publication_date varchar(255) null,
    plugin_family varchar(255) null,
    plugin_modification_date varchar(255) null,
    _extra_json longtext null   
);

create table plugin_vuln_information (
    id int auto_increment primary key,
    exploitability_ease varchar(255) null,
    exploit_available varchar(255) null,
    in_the_news  varchar(255) null,
    vuln_publication_date varchar(255) null,
    patch_publication_date varchar(255) null,
    _extra_json longtext null
);

create table plugin_attributes (
    id int auto_increment primary key,
    threat_intensity_last_28 varchar(255) null,
    script_copyright varchar(255) null,
    description longtext null,
    risk_information_id int null,
    threat_sources_last_28 varchar(255) null,
    plugin_name_id int null,
    vpr_score varchar(255) null,
    cvss_score_source varchar(255) null,
    product_coverage varchar(255) null,
    threat_recency varchar(255) null,
    fname varchar(255) null,
    cvss_v3_impact_score varchar(255) null,
    plugin_information_id int null,
    required_port varchar(255) null,
    dependency varchar(255) null,
    solution varchar(255) null,
    vuln_information_id int null,
    age_of_vuln varchar(255) null,
    exploit_code_maturity varchar(255) null,
    _extra_json longtext null,
    constraint foreign key (risk_information_id) references plugin_risk_information (id) on update cascade,
    constraint foreign key (plugin_name_id) references plugin_name (id) on update cascade,
    constraint foreign key (plugin_information_id) references plugin_information (id) on update cascade,
    constraint foreign key (vuln_information_id) references plugin_vuln_information (id) on update cascade
);

create table plugin_ref_information (
    id int auto_increment primary key,
    name varchar(255) null,
    url varchar(255) null,
    _extra_json longtext null
);

create table vulnerability (
    id int PRIMARY KEY AUTO_INCREMENT,
    `count` int null,
    cpe int null,
    offline bool null,
    plugin_name_id int null,
    plugin_family_id int null,
    plugin_id int null,
    score varchar(255) null,
    severity int null,
    severity_index int null,
    snoozed int null,
    vuln_index int null,
    _extra_json longtext null,
    constraint foreign key (plugin_family_id) references plugin_family (id) on update cascade,
    constraint foreign key (plugin_name_id) references plugin_name (id) on update cascade
);


--
-- SCAN DATA TABLES
--

create table folder (
    id           int          not null,
    name         varchar(255) null,
    type         varchar(255) null,
    default_tag  int          null,
    custom       int          null,
    unread_count int          null,
    _extra_json  longtext null,
    constraint folder_pk primary key (id)
);

create table scan (
    id              int          not null primary key,
    name            varchar(255) null,
    uuid_id         int          null,
    folder_id       int          null,
    owner_id        int          null, -- SQL owner id in the varchar lookup table -- NOT nessus owner id
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
    _extra_json  longtext null,
    constraint scan_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade,
    constraint scan_folder_id_fk foreign key (folder_id) references folder (id) on delete cascade on update cascade,
    constraint scan_scan_owner_id_fk foreign key (owner_id) references scan_owner (id) on update cascade,
    constraint scan_scan_type_id_fk foreign key (scan_type_id) references scan_type (id) on update cascade,
    constraint scan_status_id foreign key (status_id) references scan_status (id) on update cascade,
    constraint scan_timezone_id_fk foreign key (timezone_id) references timezone (id) on update cascade
);


create table scan_response (
    id int PRIMARY KEY,
    timestamp Timestamp not null,
    _extra_json longtext null,
    constraint scan_response_scan_id_fk foreign key (id) references scan (id)
);


create table scan_info (
    id int primary key,
    uuid_id int null,
    name varchar(255) null,
    folder_id int null,
    scan_type_id int null,
    edit_allowed bool null,
    scan_group_id int null,
    targets varchar(255) null,
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
    _extra_json  longtext null,

    constraint scan_info_scan_response_id_fk foreign key (id) references scan_response (id),
    constraint scan_info_folder_id_fk foreign key (folder_id) references folder (id),
    constraint scan_info_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade,
    constraint scan_info_scan_type_id_fk foreign key (scan_type_id) references scan_type(id) on update cascade,
    constraint scan_info_scan_group_id_fk foreign key (scan_group_id) references scan_group (id)  on update cascade,
    constraint scan_info_policy_id_fk foreign key (policy_id) references scan_policy (id) on update cascade,
    constraint scan_info_scanner_id_fk foreign key (scanner_id) references scanner (id) on update cascade,
    constraint scan_info_license_id_fk foreign key (license_id) references license (id) on update cascade,
    constraint scan_info_policy_template_uuid_id_fk foreign key (policy_template_uuid_id) references policy_template_uuid (id) on update cascade,
    constraint scan_info_status_id_fk foreign key (status_id) references scan_status (id) on update cascade,
    constraint scan_info_current_severity_base_id_fk foreign key (current_severity_base_id) references severity_base (id) on update cascade,
    constraint scan_info_selected_severity_base_id_fk foreign key (selected_severity_base_id) references severity_base (id) on update cascade
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
    hostname varchar(255) null,
    _extra_json longtext null,
    constraint scan_host_scan_response_id_fk foreign key (scan_id) references scan_response (id),
    constraint scan_host__host_scan_unique unique (scan_id, host_id)
);

create table scan_prioritization (
    id int primary key,
    threat_level int null,
    _extra_json longtext null,
    constraint scan_prioritization_scan_response_id_fk foreign key (id) references scan_response (id)
);

create table scan_plugin (
    id int primary key auto_increment,
    severity int null,
    plugin_name_id int null,
    plugin_attributes_id int null,
    plugin_family_id int null,
    host_count int null,
    plugin_id varchar(255) null,
    _extra_json longtext null,
    constraint foreign key scan_plugin_plugin_name_id_fk (plugin_name_id) references plugin_name (id),
    constraint foreign key scan_plugin_plugin_attributes_id_fk (plugin_attributes_id) references plugin_attributes (id),
    constraint foreign key scan_plugin_plugin_family_id_fk (plugin_family_id) references plugin_family (id)
);

create table scan_plugin_host (
    id int primary key auto_increment,
    scan_id int not null,
    host_id int null,
    host_fqdn varchar(255) null,
    hostname varchar(255) null,
    _extra_json varchar(255) null,
    constraint foreign key (scan_id) references scan_response(id),
    constraint unique (scan_id, host_id)
);

create table scan_remediations_summary (
    id int PRIMARY KEY,
    num_hosts int null,
    num_cves int null,
    num_impacted_hosts int null,
    num_remediated_cves int null,
    _extra_json longtext null,
    constraint scan_remediations_summary_scan_response_id_fk foreign key (id) references scan_response (id)
);

create table scan_remediation (
    id int PRIMARY KEY AUTO_INCREMENT,
    scan_id int not null,
    remediation varchar(255) null,
    hosts int null,
    value varchar(255) null,
    vulns int null,
    _extra_json longtext null,
    __order_for_scan_response int not null,
    constraint scan_remediation_scan_response_id foreign key (scan_id) references scan_response (id)
);

create table scan_history (
    id int PRIMARY KEY AUTO_INCREMENT,
    alt_targets_used bool null,
    scheduler int null,
    node_name varchar(255) null,
    node_host varchar(255) null,
    scan_group_id int null,
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
    history_id int null,
    _extra_json longtext null,

    constraint scan_history_scan_response_id_fk foreign key (id) references scan_response (id),
    constraint scan_history_scan_group_id_fk foreign key (scan_group_id) references scan_group (id)  on update cascade,
    constraint scan_history_status_id_fk foreign key (status_id) references scan_status (id) on update cascade,
    constraint scan_history_scan_type_id_fk foreign key (scan_type_id) references scan_type (id) on update cascade,
    constraint scan_history_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade
);

create table scan_host_response (
    id int primary key,
    _extra_json longtext null,
    constraint foreign key (id) references scan_host(id)
);

create table scan_host_info (
    id int primary key,
    operating_system_id int null,
    host_ip_id int null,
    host_start varchar(255) null,
    host_end varchar(255) null,
    _extra_json longtext null,
    constraint foreign key (id) references scan_host_response (id),
    constraint foreign key (operating_system_id) references operating_system(id),
    constraint foreign key (host_ip_id) references host_ip(id)
);


--
-- JOIN TABLES
--

create table plugin_attributes_ref_information (
    attributes_id int not null,
    ref_id int not null,
    __order_for_plugin_attributes_ref_information int not null,
    constraint primary key (attributes_id, ref_id),
    constraint foreign key (attributes_id) references plugin_attributes (id),
    constraint foreign key (ref_id) references plugin_ref_information (id)
);

create table plugin_ref_information_value (
    information_id int not null,
    value_id int not null,
    __order_for_plugin_ref_value int not null,
    constraint primary key (information_id, value_id),
    constraint foreign key (information_id) references plugin_ref_information (id),
    constraint foreign key (value_id) references plugin_ref_value (id)
);

create table plugin_attributes_see_also (
    attributes_id int not null,
    see_also_id int not null,
    __order_for_plugin_attributes_see_also int not null,
    constraint primary key (attributes_id, see_also_id),
    constraint foreign key (attributes_id) references plugin_attributes (id),
    constraint foreign key (see_also_id) references plugin_see_also (id)
);

create table scan_info_acl
(
    scan_id int not null,
    acl_id  int not null,
    __order_for_scan_info int not null,
    constraint primary key (scan_id, acl_id),
    constraint foreign key (acl_id) references acl (id),
    constraint foreign key (scan_id) references scan_info (id)
);

create table scan_info_severity_base_selection (
    scan_id int not null,
    severity_base_id int not null,
    __order_for_scan_info int not null,
    constraint primary key (scan_id, severity_base_id),
    constraint foreign key (scan_id) references scan_info (id),
    constraint foreign key (severity_base_id) references severity_base (id)
);

create table scan_host_severity_level_count (
    scan_host_id int not null,
    severity_id int not null,
    -- ordered by severity_count's severity_level
    _extra_json longtext null,
    constraint primary key (scan_host_id, severity_id),
    constraint foreign key (scan_host_id) references scan_host (id),
    constraint foreign key (severity_id) references severity_level_count (id)
);

create table scan_plugin_scan_host (
    plugin_id int not null,
    host_id int not null,
    __order_for_scan_plugin_host int not null,
    constraint primary key (plugin_id, host_id),
    constraint foreign key (plugin_id) references scan_plugin (id),
    constraint foreign key (host_id) references scan_plugin_host (id)
);

create table scan_vulnerability (
    vulnerability_id int not null,
    scan_id int not null,
    __order_for_scan_response_vulnerability int not null,
    constraint primary key (vulnerability_id, scan_id),
    constraint foreign key (scan_id) references scan_response (id),
    constraint foreign key (vulnerability_id) references vulnerability (id)
);

create table scan_host_vulnerability (
    host_id int not null,
    vulnerability_id int not null,
    __order_for_scan_host_vulnerability int not null,
    constraint primary key (host_id, vulnerability_id),
    constraint foreign key (host_id) references scan_host_response (id),
    constraint foreign key (vulnerability_id) references vulnerability (id)
);