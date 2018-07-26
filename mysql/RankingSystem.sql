-- phpMyAdmin SQL Dump
-- version 4.8.0.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Erstellungszeit: 26. Jul 2018 um 19:07
-- Server-Version: 10.1.32-MariaDB
-- PHP-Version: 5.6.36

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `rankingsystem`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `dailies_usage`
--

CREATE TABLE `dailies_usage` (
  `fk_user_id` bigint(20) NOT NULL,
  `opened` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `next_daily` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `daily_experience`
--

CREATE TABLE `daily_experience` (
  `user_id` bigint(20) NOT NULL,
  `guild_id` bigint(20) NOT NULL,
  `experience` bigint(20) NOT NULL,
  `reset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `daily_items`
--

CREATE TABLE `daily_items` (
  `item_id` int(11) NOT NULL,
  `description` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `weight` int(11) NOT NULL,
  `fk_type` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(4) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `daily_items`
--

INSERT INTO `daily_items` (`item_id`, `description`, `weight`, `fk_type`, `action`) VALUES
(1, '200000 PEN', 10, 'cur', 'use'),
(4, '100000 PEN', 10, 'cur', 'use'),
(9, 'EXP Plus 100', 80, 'exp', 'use');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `daily_type`
--

CREATE TABLE `daily_type` (
  `type` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `daily_type`
--

INSERT INTO `daily_type` (`type`, `description`) VALUES
('cur', 'Currency'),
('exp', 'Experience Item');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `experience_bar`
--

CREATE TABLE `experience_bar` (
  `bar_id` int(11) NOT NULL,
  `color` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `experience_bar`
--

INSERT INTO `experience_bar` (`bar_id`, `color`) VALUES
(1, 'dark-blue'),
(2, 'orange'),
(3, 'white'),
(4, 'black');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `guilds`
--

CREATE TABLE `guilds` (
  `guild_id` bigint(20) NOT NULL,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_level` int(11) DEFAULT NULL,
  `fk_level_id` int(11) NOT NULL,
  `fk_rank_id` int(11) NOT NULL,
  `fk_profile_id` int(11) NOT NULL,
  `fk_icon_id` int(11) NOT NULL,
  `ranking_state` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `inventory`
--

CREATE TABLE `inventory` (
  `fk_user_id` bigint(20) NOT NULL,
  `fk_item_id` int(11) NOT NULL,
  `position` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `number` int(11) NOT NULL,
  `fk_status` varchar(5) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `item_status`
--

CREATE TABLE `item_status` (
  `status` varchar(5) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `item_status`
--

INSERT INTO `item_status` (`status`, `description`) VALUES
('limit', 'Limited Item'),
('perm', 'Permanent Item');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `max_exp`
--

CREATE TABLE `max_exp` (
  `max_exp_id` int(11) NOT NULL,
  `experience` bigint(20) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `fk_guild_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ranking_icons`
--

CREATE TABLE `ranking_icons` (
  `icon_id` int(11) NOT NULL,
  `description` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `ranking_icons`
--

INSERT INTO `ranking_icons` (`icon_id`, `description`) VALUES
(1, 'S4League Icons 80 Lvs');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ranking_level`
--

CREATE TABLE `ranking_level` (
  `level_id` int(11) NOT NULL,
  `description` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tcolor_r` smallint(6) NOT NULL,
  `tcolor_g` smallint(6) NOT NULL,
  `tcolor_b` smallint(6) NOT NULL,
  `rankx` int(11) NOT NULL,
  `ranky` int(11) NOT NULL,
  `rank_width` int(11) NOT NULL,
  `rank_height` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `ranking_level`
--

INSERT INTO `ranking_level` (`level_id`, `description`, `tcolor_r`, `tcolor_g`, `tcolor_b`, `rankx`, `ranky`, `rank_width`, `rank_height`) VALUES
(1, 'S4League Level Up', 255, 255, 255, 23, 23, 50, 50),
(2, 'Puglie Level Up', 255, 255, 255, 23, 23, 50, 50),
(3, 'Kitten Level Up', 255, 255, 255, 23, 23, 50, 50),
(4, 'Lilith Level Up', 255, 255, 255, 23, 23, 50, 50),
(5, 'Ophelia Level Up', 255, 255, 255, 23, 23, 50, 50),
(6, 'Glitch Level Up', 255, 255, 255, 23, 23, 50, 50),
(7, 'Iron Eyes Level Up', 255, 255, 255, 23, 23, 50, 50);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ranking_profile`
--

CREATE TABLE `ranking_profile` (
  `profile_id` int(11) NOT NULL COMMENT 'ID of the following template',
  `description` text CHARACTER SET utf8 COLLATE utf8_general_mysql500_ci NOT NULL COMMENT 'template Rank description',
  `fk_bar_id` int(11) NOT NULL,
  `exp_percent_txt` tinyint(1) NOT NULL,
  `tcolor_r` smallint(6) NOT NULL,
  `tcolor_g` smallint(6) NOT NULL,
  `tcolor_b` smallint(6) NOT NULL,
  `rankx` int(11) NOT NULL,
  `ranky` int(11) NOT NULL,
  `rank_width` int(11) NOT NULL,
  `rank_height` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `ranking_profile`
--

INSERT INTO `ranking_profile` (`profile_id`, `description`, `fk_bar_id`, `exp_percent_txt`, `tcolor_r`, `tcolor_g`, `tcolor_b`, `rankx`, `ranky`, `rank_width`, `rank_height`) VALUES
(1, 'S4League Profile', 1, 1, 255, 255, 255, 89, 35, 26, 26),
(2, 'Puglie Profile', 2, 0, 255, 255, 255, 89, 35, 26, 26),
(3, 'Kitten Profile', 3, 0, 255, 255, 255, 89, 35, 26, 26),
(4, 'Lilith Profile', 3, 1, 255, 255, 255, 89, 35, 26, 26),
(5, 'Ophelia Profile', 3, 1, 255, 255, 255, 89, 35, 26, 26),
(6, 'Glitch Profile', 3, 1, 255, 255, 255, 89, 35, 26, 26),
(7, 'Iron Eyes Profile', 3, 1, 255, 255, 255, 89, 35, 26, 26);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ranking_rank`
--

CREATE TABLE `ranking_rank` (
  `rank_id` int(11) NOT NULL,
  `description` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_bar_id` int(11) NOT NULL,
  `exp_percent_txt` tinyint(1) NOT NULL,
  `tcolor_r` smallint(6) NOT NULL,
  `tcolor_g` smallint(6) NOT NULL,
  `tcolor_b` smallint(6) NOT NULL,
  `rankx` int(11) NOT NULL,
  `ranky` int(11) NOT NULL,
  `rank_width` int(11) NOT NULL,
  `rank_height` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `ranking_rank`
--

INSERT INTO `ranking_rank` (`rank_id`, `description`, `fk_bar_id`, `exp_percent_txt`, `tcolor_r`, `tcolor_g`, `tcolor_b`, `rankx`, `ranky`, `rank_width`, `rank_height`) VALUES
(1, 'S4League Rank', 1, 1, 255, 255, 255, 73, 26, 26, 26),
(2, 'Puglie Rank', 2, 0, 255, 255, 255, 73, 26, 26, 26),
(3, 'Kitten Rank', 3, 0, 255, 255, 255, 73, 26, 26, 26),
(4, 'Lilith Rank', 3, 1, 255, 255, 255, 73, 26, 26, 26),
(5, 'Ophelia Rank', 3, 1, 255, 255, 255, 73, 26, 26, 26),
(6, 'Glitch Rank', 3, 1, 255, 255, 255, 73, 26, 26, 26),
(7, 'Iron Eyes Rank', 3, 1, 255, 255, 255, 73, 26, 26, 26);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `roles`
--

CREATE TABLE `roles` (
  `role_id` bigint(20) NOT NULL,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level_requirement` int(11) NOT NULL,
  `fk_guild_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `shop_content`
--

CREATE TABLE `shop_content` (
  `item_id` int(11) NOT NULL,
  `description` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` bigint(20) NOT NULL,
  `fk_skin` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `shop_content`
--

INSERT INTO `shop_content` (`item_id`, `description`, `price`, `fk_skin`) VALUES
(1, 'S4League Profile', 300000, 'pro'),
(2, 'S4League Rank', 200000, 'ran'),
(3, 'S4League Level Up', 100000, 'lev'),
(4, 'S4League Icons 80 Lvs', 50000, 'ico'),
(5, 'Puglie Profile', 300000, 'pro'),
(6, 'Puglie Rank', 200000, 'ran'),
(7, 'Puglie Level Up', 100000, 'lev'),
(8, 'EXP Plus 100', 200000, 'ite'),
(9, 'Kitten Profile', 300000, 'pro'),
(10, 'Kitten Rank', 200000, 'ran'),
(11, 'Kitten Level Up', 100000, 'lev'),
(12, 'Lilith Profile', 300000, 'pro'),
(13, 'Ophelia Profile', 300000, 'pro'),
(14, 'Lilith Rank', 200000, 'ran'),
(15, 'Ophelia Rank', 200000, 'ran'),
(16, 'Lilith Level Up', 100000, 'lev'),
(17, 'Ophelia Level Up', 100000, 'lev'),
(18, 'Glitch Profile', 300000, 'pro'),
(19, 'Glitch Rank', 200000, 'ran'),
(20, 'Glitch Level Up', 100000, 'lev'),
(21, 'Iron Eyes Profile', 300000, 'pro'),
(22, 'Iron Eyes Rank', 200000, 'ran'),
(23, 'Iron Eyes Level Up', 100000, 'lev');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `skin_type`
--

CREATE TABLE `skin_type` (
  `skin` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `skin_type`
--

INSERT INTO `skin_type` (`skin`, `description`) VALUES
('ico', 'Icons'),
('ite', 'Items'),
('lev', 'Level Ups'),
('pro', 'Profiles'),
('ran', 'Ranks');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level_skin` int(11) NOT NULL,
  `rank_skin` int(11) NOT NULL,
  `profile_skin` int(11) NOT NULL,
  `icon_skin` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user_details`
--

CREATE TABLE `user_details` (
  `fk_user_id` bigint(20) NOT NULL,
  `fk_guild_id` bigint(20) NOT NULL,
  `level` int(11) NOT NULL,
  `current_experience` int(11) NOT NULL,
  `rank_up_experience` int(11) NOT NULL,
  `total_experience` bigint(20) NOT NULL,
  `currency` bigint(20) NOT NULL,
  `current_role` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `dailies_usage`
--
ALTER TABLE `dailies_usage`
  ADD PRIMARY KEY (`fk_user_id`);

--
-- Indizes für die Tabelle `daily_experience`
--
ALTER TABLE `daily_experience`
  ADD PRIMARY KEY (`user_id`,`guild_id`);

--
-- Indizes für die Tabelle `daily_items`
--
ALTER TABLE `daily_items`
  ADD PRIMARY KEY (`item_id`),
  ADD UNIQUE KEY `description` (`description`),
  ADD KEY `fk_type` (`fk_type`);

--
-- Indizes für die Tabelle `daily_type`
--
ALTER TABLE `daily_type`
  ADD PRIMARY KEY (`type`);

--
-- Indizes für die Tabelle `experience_bar`
--
ALTER TABLE `experience_bar`
  ADD PRIMARY KEY (`bar_id`);

--
-- Indizes für die Tabelle `guilds`
--
ALTER TABLE `guilds`
  ADD PRIMARY KEY (`guild_id`),
  ADD KEY `fk_setup` (`fk_profile_id`),
  ADD KEY `fk_level_id` (`fk_level_id`),
  ADD KEY `fk_rank_id` (`fk_rank_id`),
  ADD KEY `fk_icon_id` (`fk_icon_id`);

--
-- Indizes für die Tabelle `inventory`
--
ALTER TABLE `inventory`
  ADD UNIQUE KEY `fk_user_id` (`fk_user_id`,`fk_item_id`,`fk_status`),
  ADD UNIQUE KEY `fk_user_id_2` (`fk_user_id`,`fk_item_id`,`fk_status`),
  ADD KEY `fk_item_id` (`fk_item_id`),
  ADD KEY `fk_status` (`fk_status`);

--
-- Indizes für die Tabelle `item_status`
--
ALTER TABLE `item_status`
  ADD PRIMARY KEY (`status`);

--
-- Indizes für die Tabelle `max_exp`
--
ALTER TABLE `max_exp`
  ADD PRIMARY KEY (`max_exp_id`),
  ADD UNIQUE KEY `fk_guild_id` (`fk_guild_id`);

--
-- Indizes für die Tabelle `ranking_icons`
--
ALTER TABLE `ranking_icons`
  ADD PRIMARY KEY (`icon_id`);

--
-- Indizes für die Tabelle `ranking_level`
--
ALTER TABLE `ranking_level`
  ADD PRIMARY KEY (`level_id`);

--
-- Indizes für die Tabelle `ranking_profile`
--
ALTER TABLE `ranking_profile`
  ADD PRIMARY KEY (`profile_id`),
  ADD KEY `LvSetting` (`profile_id`),
  ADD KEY `fk_bar_id` (`fk_bar_id`);

--
-- Indizes für die Tabelle `ranking_rank`
--
ALTER TABLE `ranking_rank`
  ADD PRIMARY KEY (`rank_id`);

--
-- Indizes für die Tabelle `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`);

--
-- Indizes für die Tabelle `shop_content`
--
ALTER TABLE `shop_content`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `fk_skin` (`fk_skin`);

--
-- Indizes für die Tabelle `skin_type`
--
ALTER TABLE `skin_type`
  ADD PRIMARY KEY (`skin`);

--
-- Indizes für die Tabelle `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD KEY `skin` (`profile_skin`),
  ADD KEY `profile_rank` (`rank_skin`),
  ADD KEY `profile_icon` (`icon_skin`),
  ADD KEY `level_skin` (`level_skin`);

--
-- Indizes für die Tabelle `user_details`
--
ALTER TABLE `user_details`
  ADD PRIMARY KEY (`fk_user_id`,`fk_guild_id`),
  ADD KEY `fk_guild_id` (`fk_guild_id`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `daily_items`
--
ALTER TABLE `daily_items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT für Tabelle `max_exp`
--
ALTER TABLE `max_exp`
  MODIFY `max_exp_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT für Tabelle `ranking_profile`
--
ALTER TABLE `ranking_profile`
  MODIFY `profile_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID of the following template', AUTO_INCREMENT=8;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `dailies_usage`
--
ALTER TABLE `dailies_usage`
  ADD CONSTRAINT `dailies_usage_ibfk_1` FOREIGN KEY (`fk_user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints der Tabelle `daily_items`
--
ALTER TABLE `daily_items`
  ADD CONSTRAINT `daily_items_ibfk_1` FOREIGN KEY (`fk_type`) REFERENCES `daily_type` (`type`);

--
-- Constraints der Tabelle `guilds`
--
ALTER TABLE `guilds`
  ADD CONSTRAINT `guilds_ibfk_1` FOREIGN KEY (`fk_profile_id`) REFERENCES `ranking_profile` (`profile_id`),
  ADD CONSTRAINT `guilds_ibfk_2` FOREIGN KEY (`fk_level_id`) REFERENCES `ranking_level` (`level_id`),
  ADD CONSTRAINT `guilds_ibfk_3` FOREIGN KEY (`fk_rank_id`) REFERENCES `ranking_rank` (`rank_id`),
  ADD CONSTRAINT `guilds_ibfk_4` FOREIGN KEY (`fk_icon_id`) REFERENCES `ranking_icons` (`icon_id`);

--
-- Constraints der Tabelle `inventory`
--
ALTER TABLE `inventory`
  ADD CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`fk_user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `inventory_ibfk_2` FOREIGN KEY (`fk_item_id`) REFERENCES `shop_content` (`item_id`),
  ADD CONSTRAINT `inventory_ibfk_3` FOREIGN KEY (`fk_status`) REFERENCES `item_status` (`status`);

--
-- Constraints der Tabelle `ranking_profile`
--
ALTER TABLE `ranking_profile`
  ADD CONSTRAINT `ranking_profile_ibfk_1` FOREIGN KEY (`fk_bar_id`) REFERENCES `experience_bar` (`bar_id`),
  ADD CONSTRAINT `ranking_profile_ibfk_2` FOREIGN KEY (`fk_bar_id`) REFERENCES `experience_bar` (`bar_id`);

--
-- Constraints der Tabelle `shop_content`
--
ALTER TABLE `shop_content`
  ADD CONSTRAINT `shop_content_ibfk_1` FOREIGN KEY (`fk_skin`) REFERENCES `skin_type` (`skin`);

--
-- Constraints der Tabelle `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`level_skin`) REFERENCES `ranking_level` (`level_id`);

--
-- Constraints der Tabelle `user_details`
--
ALTER TABLE `user_details`
  ADD CONSTRAINT `user_details_ibfk_1` FOREIGN KEY (`fk_user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `user_details_ibfk_2` FOREIGN KEY (`fk_guild_id`) REFERENCES `guilds` (`guild_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
