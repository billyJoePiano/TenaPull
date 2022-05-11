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