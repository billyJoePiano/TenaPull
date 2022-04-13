drop table if exists `scan_info_acl`;
drop table if exists `scan_info_severity_base_selection`;

drop table if exists `scan_info`;
drop table if exists `scan`;
drop table if exists `folder`;

drop table if exists `severity_base`;
drop table if exists `acl`;
drop table if exists `license`;

drop table if exists `scan_host_severity_count`;
drop table if exists `scan_host`;
drop table if exists `scan_owner_id`;
drop table if exists `scan_owner`;
drop table if exists `scan_type`;
drop table if exists `timezone`;
drop table if exists `scan_policy`;
drop table if exists `scanner`;
drop table if exists `scan_group`;
drop table if exists `scan_status`;


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
    _extra_json  json null
);
create unique index acl_uindix
    on acl (owner, name, type, permissions, display_name, _extra_json);

create table severity_base
(
    id      int auto_increment,
    value   varchar(255) null,
    display varchar(255) null,
    _extra_json  json null,
    constraint severity_base_pk
        primary key (id)
);
create unique index severity_base_uindex
    on severity_base (value, display);

create table license
(
    id int auto_increment primary key,
    `limit` varchar(255) null,
    trimmed varchar(255) null,
    _extra_json json null
);
create unique index license_uindex
    on license (`limit`, trimmed);


create table scan_group (
    id int primary key auto_increment,
    _extra_json  longtext null
);


--
-- DATA TABLES
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
    constraint scan_info_uuid_id_fk foreign key (uuid_id) references scan_uuid (id) on update cascade,
    constraint scan_info_folder_id_fk foreign key (folder_id) references folder (id),
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

create table scan_host_severity_count (
    id int PRIMARY KEY AUTO_INCREMENT,
    scan_host_id int NOT NULL,
    severity_level int null,
    count int null,
    _extra_json longtext null,
    constraint scan_host_severity_count_scan_host_id_fk foreign key (scan_host_id) references scan_host (id),
    constraint scan_host_severity_count__host_severity_level_unique unique (scan_host_id, severity_level)
);

create table scan_host_severity_count_container (
    id int PRIMARY KEY AUTO_INCREMENT,
    _extra_json longtext null,
    constraint scan_host_severity_count_container_scan_response_id_fk foreign key (id) references scan_response (id)
    -- the scan_host_severity_count rows are directly linked to the scan_host id
    -- no need to link them to this, since this is a one-to-one record with scan_host
);

create table scan_vulnerability (
    id int PRIMARY KEY AUTO_INCREMENT,
    scan_id int not null,
    count int null,
    cpe int null,
    offline bool null,
    plugin_family varchar(255) null,
    plugin_id int null,
    score varchar(255) null,
    severity int null,
    severity_index int null,
    snoozed int null,
    vuln_index int null,
    _extra_json longtext null,
    __order_for_scan_response int not null,
    constraint scan_vulnerability_scan_response_id_fk foreign key (scan_id) references scan_response (id)
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


--
-- JOIN TABLES
--

create table scan_info_acl
(
    scan_id int not null,
    acl_id  int not null,
    __order_for_scan_info int not null,
    constraint scan_info_acl_pk
        primary key (scan_id, acl_id),
    constraint scan_info_acl_acl_id_fk
        foreign key (acl_id) references acl (id),
    constraint scan_info_acl_scan_info_id_fk
        foreign key (scan_id) references scan_info (id)
);

create table scan_info_severity_base_selection (
    scan_id int not null,
    severity_base_id int not null,
    __order_for_scan_info int not null,
    constraint scan_info_severity_base_selection_pk
        primary key (scan_id, severity_base_id),
    constraint foreign key (scan_id)
        references scan_info (id),
    constraint foreign key (severity_base_id)
        references severity_base (id)
);
