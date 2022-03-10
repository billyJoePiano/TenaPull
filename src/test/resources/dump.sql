-- MariaDB dump 10.19  Distrib 10.5.13-MariaDB, for FreeBSD13.0 (amd64)
--
-- Host: localhost    Database: NessusTools
-- ------------------------------------------------------
-- Server version       10.5.13-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `folder`
--

DROP TABLE IF EXISTS `folder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `folder` (
                          `id` int(11) NOT NULL,
                          `name` varchar(255) DEFAULT NULL,
                          `type` varchar(255) DEFAULT NULL,
                          `default_tag` int(11) DEFAULT NULL,
                          `custom` int(11) DEFAULT NULL,
                          `unread_count` int(11) DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `folder`
--

LOCK TABLES `folder` WRITE;
/*!40000 ALTER TABLE `folder` DISABLE KEYS */;
INSERT INTO `folder` VALUES (2,'Trash','trash',0,0,NULL),(3,'My Scans','main',1,0,0);
/*!40000 ALTER TABLE `folder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scan`
--

DROP TABLE IF EXISTS `scan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan` (
                        `id` int(11) NOT NULL,
                        `name` varchar(255) DEFAULT NULL,
                        `uuid` varchar(255) DEFAULT NULL,
                        `folder_id` int(11) DEFAULT NULL,
                        `owner_id` int(11) DEFAULT NULL,
                        `type_id` int(11) DEFAULT NULL,
                        `rrules` varchar(255) DEFAULT NULL,
                        `read` tinyint(1) DEFAULT NULL,
                        `shared` tinyint(1) DEFAULT NULL,
                        `enabled` tinyint(1) DEFAULT NULL,
                        `control` tinyint(1) DEFAULT NULL,
                        `user_permissions` int(11) DEFAULT NULL,
                        `status` varchar(255) DEFAULT NULL,
                        `creation_date` timestamp NULL DEFAULT NULL,
                        `start_time` varchar(255) DEFAULT NULL,
                        `last_modification_date` timestamp NULL DEFAULT NULL,
                        `timezone_id` int(11) DEFAULT NULL,
                        `live_results` int(11) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        KEY `scan_folder_id_fk` (`folder_id`),
                        KEY `scan_scan_owner_id_fk` (`owner_id`),
                        KEY `scan_scan_type_id_fk` (`type_id`),
                        KEY `scan_timezone_id_fk` (`timezone_id`),
                        CONSTRAINT `scan_folder_id_fk` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`),
                        CONSTRAINT `scan_scan_owner_id_fk` FOREIGN KEY (`owner_id`) REFERENCES `scan_owner` (`id`),
                        CONSTRAINT `scan_scan_type_id_fk` FOREIGN KEY (`type_id`) REFERENCES `scan_type` (`id`),
                        CONSTRAINT `scan_timezone_id_fk` FOREIGN KEY (`timezone_id`) REFERENCES `timezone` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scan`
--

LOCK TABLES `scan` WRITE;
/*!40000 ALTER TABLE `scan` DISABLE KEYS */;
INSERT INTO `scan` VALUES (5,'My Host Discovery Scan','921b5bc0-9bbc-5aca-cb7f-d83d98b26f5ab1aff440abfb51cf',3,1,1,NULL,1,0,0,1,128,'completed','2022-02-26 23:35:11',NULL,'2022-02-26 23:35:49',4,0),(8,'My Basic Network Scan','3d05d2ca-b2f3-4c61-ba03-27adb92c04fd6598b45a6713e4c0',3,1,1,NULL,1,0,0,1,128,'completed','2022-02-26 23:44:32',NULL,'2022-02-26 23:58:29',3,0),(11,'WebApp-Policy','79ef5bff-b81e-4c1c-4f0e-e302f6ca525810fddf45d1857d98',3,1,1,NULL,1,0,0,1,128,'completed','2022-02-28 20:44:13',NULL,'2022-02-28 20:52:58',NULL,0),(15,'Credentialed-Patch-Scan','21b37df2-fa92-8f1e-0b2e-afd4143b8ddf57f7d6bd34514528',3,1,1,NULL,1,0,0,1,128,'completed','2022-02-28 21:10:01',NULL,'2022-02-28 21:13:34',2,NULL),(22,'Spectre/Meltdown','3b06875f-df5a-5634-4be2-972b38d0e4be0d395d5af67a8842',3,1,1,NULL,1,0,0,1,128,'completed','2022-02-28 21:28:39',NULL,'2022-02-28 21:32:09',1,NULL);
/*!40000 ALTER TABLE `scan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scan_info`
--

DROP TABLE IF EXISTS `scan_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_info` (
                             `id` int(11) NOT NULL,
                             PRIMARY KEY (`id`),
                             CONSTRAINT `scan_info_scan_id_fk` FOREIGN KEY (`id`) REFERENCES `scan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scan_info`
--

LOCK TABLES `scan_info` WRITE;
/*!40000 ALTER TABLE `scan_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `scan_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scan_owner`
--

DROP TABLE IF EXISTS `scan_owner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_owner` (
                              `id` int(11) NOT NULL AUTO_INCREMENT,
                              `owner` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `scan_owner_owner_uindex` (`owner`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scan_owner`
--

LOCK TABLES `scan_owner` WRITE;
/*!40000 ALTER TABLE `scan_owner` DISABLE KEYS */;
INSERT INTO `scan_owner` VALUES (1,'wanderson7');
/*!40000 ALTER TABLE `scan_owner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scan_type`
--

DROP TABLE IF EXISTS `scan_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_type` (
                             `id` int(11) NOT NULL AUTO_INCREMENT,
                             `type` varchar(255) NOT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `scan_type_type_uindex` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scan_type`
--

LOCK TABLES `scan_type` WRITE;
/*!40000 ALTER TABLE `scan_type` DISABLE KEYS */;
INSERT INTO `scan_type` VALUES (1,'local');
/*!40000 ALTER TABLE `scan_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timezone`
--

DROP TABLE IF EXISTS `timezone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `timezone` (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `timezone` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `timezone_timezone_uindex` (`timezone`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timezone`
--

LOCK TABLES `timezone` WRITE;
/*!40000 ALTER TABLE `timezone` DISABLE KEYS */;
INSERT INTO `timezone` VALUES (1,'Central'),(3,'Eastern'),(4,'Mountain'),(2,'Pacific');
/*!40000 ALTER TABLE `timezone` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-03-10  1:16:38
