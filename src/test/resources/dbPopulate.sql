-- Had to load in timezones with this script, in order for the first command to work
-- https://mariadb.com/kb/en/mysql_tzinfo_to_sql/
-- mariadb-dump (aka mysqldump) outputs the timestamps in UTC,
-- but when re-loaded into the DB they assume local timezone

SET time_zone = 'UTC';
INSERT INTO `folder` VALUES (2,'Trash','trash',0,0,NULL, NULL),(3,'My Scans','main',1,0,0, NULL);
INSERT INTO `scan_owner` VALUES (1,'wanderson7');
INSERT INTO `scan_type` VALUES (1,'local');
INSERT INTO `timezone` VALUES (1,'Central'),(3,'Eastern'),(4,'Mountain'),(2,'Pacific');
INSERT INTO `scan_status` VALUES (1, 'running'),(2, 'completed');
INSERT INTO `scan` VALUES (5,'My Host Discovery Scan','921b5bc0-9bbc-5aca-cb7f-d83d98b26f5ab1aff440abfb51cf',3,1,1,NULL,1,0,0,1,128,2,'2022-02-26 23:35:11',NULL,'2022-02-26 23:35:49',4,0,NULL),(8,'My Basic Network Scan','3d05d2ca-b2f3-4c61-ba03-27adb92c04fd6598b45a6713e4c0',3,1,1,NULL,1,0,0,1,128,2,'2022-02-26 23:44:32',NULL,'2022-02-26 23:58:29',3,0, NULL),(11,'WebApp-Policy','79ef5bff-b81e-4c1c-4f0e-e302f6ca525810fddf45d1857d98',3,1,1,NULL,1,0,0,1,128,2,'2022-02-28 20:44:13',NULL,'2022-02-28 20:52:58',NULL,0,NULL),(15,'Credentialed-Patch-Scan','21b37df2-fa92-8f1e-0b2e-afd4143b8ddf57f7d6bd34514528',3,1,1,NULL,1,0,0,1,128,2,'2022-02-28 21:10:01',NULL,'2022-02-28 21:13:34',2,NULL,NULL),(22,'Spectre/Meltdown','3b06875f-df5a-5634-4be2-972b38d0e4be0d395d5af67a8842',3,1,1,NULL,1,0,0,1,128,2,'2022-02-28 21:28:39',NULL,'2022-02-28 21:32:09',1,NULL,NULL);
SET time_zone = 'SYSTEM';