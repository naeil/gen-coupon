CREATE DATABASE IF NOT EXISTS `gen-coupon`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_uca1400_ai_ci;

USE `gen-coupon`;

-- `gen-coupon`.config definition

CREATE TABLE `config` (
  `config_id` int(11) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(255) DEFAULT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


-- `gen-coupon`.customer definition

CREATE TABLE `customer` (
  `customer_id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_email` varchar(255) DEFAULT NULL,
  `customer_htel` varchar(255) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


-- `gen-coupon`.shop definition

CREATE TABLE `shop` (
  `shop_id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_code` varchar(255) DEFAULT NULL,
  `shop_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


-- `gen-coupon`.coupon_issue definition

CREATE TABLE `coupon_issue` (
  `customer_id` int(11) DEFAULT NULL,
  `issue_id` int(11) NOT NULL AUTO_INCREMENT,
  `create_date` datetime(6) DEFAULT NULL,
  `imweb_coupon_code` varchar(255) DEFAULT NULL,
  `imweb_coupon_name` varchar(255) DEFAULT NULL,
  `issued_coupon_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`issue_id`),
  UNIQUE KEY `UK3lealojvxy07wfaxktu52jvyh` (`issued_coupon_code`),
  KEY `FKqyfyntl4nn5elyev0ofognv4j` (`customer_id`),
  CONSTRAINT `FKqyfyntl4nn5elyev0ofognv4j` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


-- `gen-coupon`.order_history definition

CREATE TABLE `order_history` (
  `customer_id` int(11) DEFAULT NULL,
  `order_history_id` int(11) NOT NULL AUTO_INCREMENT,
  `pay_amt` int(11) DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `create_date` datetime(6) DEFAULT NULL,
  `shop_ord_no_real` varchar(255) DEFAULT NULL,
  `shop_sale_name` varchar(255) DEFAULT NULL,
  `uniq` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_history_id`),
  KEY `FKj7nwdjdh5ipolmlcaso4oygd4` (`customer_id`),
  KEY `FKcwfpf6n2onxukxabvx3yt6704` (`shop_id`),
  CONSTRAINT `FKcwfpf6n2onxukxabvx3yt6704` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`shop_id`),
  CONSTRAINT `FKj7nwdjdh5ipolmlcaso4oygd4` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


-- `gen-coupon`.stamp definition

CREATE TABLE `stamp` (
  `customer_id` int(11) DEFAULT NULL,
  `issue_id` int(11) DEFAULT NULL,
  `order_history_id` int(11) DEFAULT NULL,
  `stamp_id` int(11) NOT NULL AUTO_INCREMENT,
  `create_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`stamp_id`),
  UNIQUE KEY `UK6bpw6g4wjtp8tsbhwuy71s0ax` (`order_history_id`),
  KEY `FK8lptdtpdf59x4lg151h3qt0o3` (`customer_id`),
  CONSTRAINT `FK8lptdtpdf59x4lg151h3qt0o3` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `FKo5yjpw8n1cj9inor6hwylrfug` FOREIGN KEY (`order_history_id`) REFERENCES `order_history` (`order_history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;