-- lock tables scan_info write concurrent, scan write concurrent, folder write concurrent, scan_owner write concurrent, scan_type write concurrent, timezone write concurrent;
-- lock tables scan_info write, scan write, folder write, scan_owner write, scan_type write, timezone write;
delete from scan_info;
delete from scan;
delete from folder;
delete from scan_owner;
delete from scan_type;
delete from timezone;
-- unlock tables;

-- lock tables scan_owner write concurrent, scan_type write concurrent, timezone write concurrent;
-- alter table scan_owner auto_increment = 1;
-- alter table scan_type auto_increment = 1;
-- alter table timezone auto_increment = 1;
-- unlock tables;