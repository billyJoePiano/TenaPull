drop table if exists `host_vulnerability_output`;
drop table if exists `host_output`;

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