drop table if exists `scan_info_acl`;
drop table if exists `scan_info_severity_base_selection`;

drop table if exists `scan_info`;
drop table if exists `scan`;
drop table if exists `folder`;

drop table if exists `severity_base`;
drop table if exists `acl`;
drop table if exists `license`;

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
    nessus_id int primary key,
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
    display_name varchar(255) null
);
create unique index acl_uindix
    on acl (name, type, permissions, display_name);

create table severity_base
(
    id      int auto_increment,
    value   varchar(255) null,
    display varchar(255) null,
    constraint severity_base_pk
        primary key (id)
);
create unique index severity_base_uindex
    on severity_base (value, display);

create table license
(
    id int auto_increment primary key,
    `limit` varchar(255) null,
    trimmed varchar(255) null
);
create unique index license_uindex
    on license (`limit`, trimmed);


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
                        constraint folder_pk primary key (id)
);
alter table folder disable keys;

create table scan_group (
    id int primary key auto_increment
);

create table scan (
    id              int          not null primary key,
    name            varchar(255) null,
    uuid            varchar(255) null,
    folder_id       int          null,
    owner_id        int          null,
    type_id         int          null,
    rrules          varchar(255) null,
    `read`          boolean      null,
    shared          boolean      null,
    enabled         boolean      null,
    control         boolean      null,
    user_permissions int         null,
    status          varchar(255) null,
    creation_date   timestamp    null,
    start_time      varchar(255) null,
    last_modification_date  timestamp null,
    timezone_id     int          null,
    live_results    int          null,
    constraint scan_folder_id_fk foreign key (folder_id) references folder (id) on delete cascade on update cascade,
    constraint scan_scan_owner_id_fk foreign key (owner_id) references scan_owner (id),
    constraint scan_scan_type_id_fk foreign key (type_id) references scan_type (id),
    constraint scan_timezone_id_fk foreign key (timezone_id) references timezone (id)
);
alter table scan disable keys;

create table scan_info (
    id int primary key,
    name varchar(255) null,
    folder_id int null,
    uuid  varchar(255) null,
    scan_type_id int null,
    edit_allowed bool null,
    current_severity_base_id int null,
    -- current_severity_base varchar(255) null,
    -- current_severity_base_display varchar(255) null,
    scan_group_id int null,
    targets varchar(255) null,
    scanner_start timestamp null,
    scanner_end timestamp null,
    selected_severity_base varchar(255) not null,
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
    policy_template_uuid varchar(255) null,
    known_accounts bool null,
    offline bool null,
    status_id int null,


    constraint scan_info_id_fk foreign key (id) references scan (id),
    constraint scan_info_folder_id_fk foreign key (folder_id) references folder (id),
    constraint scan_info_scan_type_id_fk foreign key (scan_type_id) references scan_type(id) on update cascade,
    constraint scan_info_current_severity_base_id_fk foreign key (current_severity_base_id) references severity_base (id) on update cascade,
    constraint scan_info_scan_group_id_fk foreign key (scan_group_id) references scan_group (id)  on update cascade,
    constraint scan_info_policy_id foreign key (policy_id) references scan_policy (id) on update cascade,
    constraint scan_info_scanner_id foreign key (scanner_id) references scanner (id) on update cascade,
    constraint scan_info_scan_type_id foreign key (scan_type_id) references scan_type (id) on update cascade,
    constraint scan_info_license_id_fk foreign key (license_id) references license (id) on update cascade,
    constraint scan_info_status_id foreign key (status_id) references scan_status (id) on update cascade
);
alter table scan_info disable keys;



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
