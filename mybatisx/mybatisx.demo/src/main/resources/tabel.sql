
CREATE DATABASE `mybatisx`;

USE `mybatisx`;

DROP TABLE IF EXISTS `address`;

CREATE TABLE `address` (
  `adid` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(500) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`adid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert  into `address`(`adid`,`address`) values (1,'beijing');

DROP TABLE IF EXISTS `brand`;

CREATE TABLE `brand` (
  `brid` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `code` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `desc` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`brid`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert  into `brand`(`brid`,`name`,`code`,`desc`) values (1,'sony','sony','sony'),(2,'apple','apple','apple');


DROP TABLE IF EXISTS `picture`;

CREATE TABLE `picture` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `shid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert  into `picture`(`id`,`url`,`shid`) values (1,'www.goole.com',1);


DROP TABLE IF EXISTS `shop`;

CREATE TABLE `shop` (
  `shId` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `adid` int(11) DEFAULT NULL,
  PRIMARY KEY (`shId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert  into `shop`(`shId`,`name`,`adid`) values (1,'testShop',1);

DROP TABLE IF EXISTS `shopbrand`;

CREATE TABLE `shopbrand` (
  `brid` int(11) DEFAULT NULL,
  `shid` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

insert  into `shopbrand`(`brid`,`shid`) values (1,1),(2,1);