-- phpMyAdmin SQL Dump
-- version 4.8.0.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Erstellungszeit: 26. Jul 2018 um 19:05
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
-- Datenbank: `test`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `action_log`
--

CREATE TABLE `action_log` (
  `log_id` bigint(20) NOT NULL,
  `event` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  `guild_id` bigint(20) NOT NULL,
  `description` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `bancollect`
--

CREATE TABLE `bancollect` (
  `id` int(11) NOT NULL,
  `fk_user_id` bigint(20) NOT NULL COMMENT 'Discord ID numbers of individual users',
  `fk_guild_id` bigint(20) NOT NULL COMMENT 'ID of the discord server, the player belongs to',
  `fk_warning_id` int(11) NOT NULL COMMENT 'Foreignkey from table warnings',
  `fk_ban_id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp',
  `unmute` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT 'date and time for unmute',
  `muted` tinyint(1) NOT NULL,
  `custom_time` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `bans`
--

CREATE TABLE `bans` (
  `ban_id` int(11) NOT NULL COMMENT 'ID of table bans',
  `description` text CHARACTER SET utf8 NOT NULL COMMENT 'Defines if a user was banned or not'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `bans`
--

INSERT INTO `bans` (`ban_id`, `description`) VALUES
(1, 'not banned'),
(2, 'banned');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `channels`
--

CREATE TABLE `channels` (
  `channel_id` bigint(20) NOT NULL,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `channeltypes`
--

CREATE TABLE `channeltypes` (
  `channel_type` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `channel` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `channeltypes`
--

INSERT INTO `channeltypes` (`channel_type`, `channel`) VALUES
('all', 'All Languages'),
('bot', 'Bot'),
('eng', 'English'),
('fre', 'French'),
('ger', 'German'),
('log', 'Log'),
('mus', 'Music'),
('rus', 'Russian'),
('tra', 'Trash'),
('tur', 'Turkish');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `channel_conf`
--

CREATE TABLE `channel_conf` (
  `fk_channel_id` bigint(20) NOT NULL,
  `fk_channel_type` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_guild_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `channel_filter`
--

CREATE TABLE `channel_filter` (
  `fk_channel_id` bigint(20) NOT NULL,
  `fk_lang_abbrv` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Tabellenstruktur für Tabelle `command`
--

CREATE TABLE `command` (
  `command_id` int(11) NOT NULL,
  `guild_id` bigint(20) NOT NULL,
  `execution_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `commandexecution`
--

CREATE TABLE `commandexecution` (
  `execution_id` int(11) NOT NULL,
  `description` varchar(20) COLLATE utf8_general_mysql500_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_mysql500_ci;

--
-- Daten für Tabelle `commandexecution`
--

INSERT INTO `commandexecution` (`execution_id`, `description`) VALUES
(0, 'not allowed to use'),
(1, 'only bot channel'),
(2, 'to use everywhere');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `filter`
--

CREATE TABLE `filter` (
  `filter_id` int(11) NOT NULL,
  `word` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_lang_abbrv` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `filter_languages`
--

CREATE TABLE `filter_languages` (
  `lang_abbrv` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `language` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `filter_languages`
--

INSERT INTO `filter_languages` (`lang_abbrv`, `language`) VALUES
('all', 'All Lang.'),
('eng', 'English'),
('fre', 'French'),
('ger', 'German'),
('rus', 'Russian'),
('tur', 'Turkish');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `guild`
--

CREATE TABLE `guild` (
  `guild_id` bigint(20) NOT NULL,
  `name` text COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `names`
--

CREATE TABLE `names` (
  `name_id` int(11) NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `name_filter`
--

CREATE TABLE `name_filter` (
  `word_id` int(11) NOT NULL,
  `word` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `nickname`
--

CREATE TABLE `nickname` (
  `fk_user_id` bigint(20) NOT NULL,
  `fk_guild_id` bigint(20) NOT NULL,
  `nickname` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `name` text COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `warnings`
--

CREATE TABLE `warnings` (
  `fk_guild_id` bigint(20) NOT NULL,
  `warning_id` int(11) NOT NULL,
  `mute_time` bigint(20) NOT NULL,
  `description` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `action_log`
--
ALTER TABLE `action_log`
  ADD PRIMARY KEY (`log_id`);

--
-- Indizes für die Tabelle `bancollect`
--
ALTER TABLE `bancollect`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `fk_user_id` (`fk_user_id`,`fk_guild_id`),
  ADD UNIQUE KEY `fk_user_id_2` (`fk_user_id`,`fk_guild_id`),
  ADD KEY `warning` (`fk_warning_id`),
  ADD KEY `ban` (`fk_ban_id`),
  ADD KEY `id` (`id`);

--
-- Indizes für die Tabelle `bans`
--
ALTER TABLE `bans`
  ADD PRIMARY KEY (`ban_id`);

--
-- Indizes für die Tabelle `channels`
--
ALTER TABLE `channels`
  ADD PRIMARY KEY (`channel_id`);

--
-- Indizes für die Tabelle `channeltypes`
--
ALTER TABLE `channeltypes`
  ADD PRIMARY KEY (`channel_type`);

--
-- Indizes für die Tabelle `channel_conf`
--
ALTER TABLE `channel_conf`
  ADD PRIMARY KEY (`fk_channel_id`,`fk_guild_id`);

--
-- Indizes für die Tabelle `channel_filter`
--
ALTER TABLE `channel_filter`
  ADD PRIMARY KEY (`fk_channel_id`,`fk_lang_abbrv`),
  ADD KEY `fk_lang_abbrv` (`fk_lang_abbrv`);

--
-- Indizes für die Tabelle `command`
--
ALTER TABLE `command`
  ADD PRIMARY KEY (`command_id`),
  ADD UNIQUE KEY `guild_id_2` (`guild_id`),
  ADD KEY `guild_id` (`guild_id`),
  ADD KEY `execution_id` (`execution_id`);

--
-- Indizes für die Tabelle `commandexecution`
--
ALTER TABLE `commandexecution`
  ADD PRIMARY KEY (`execution_id`);

--
-- Indizes für die Tabelle `filter`
--
ALTER TABLE `filter`
  ADD PRIMARY KEY (`filter_id`),
  ADD UNIQUE KEY `word` (`word`,`fk_lang_abbrv`),
  ADD UNIQUE KEY `word_2` (`word`,`fk_lang_abbrv`);

--
-- Indizes für die Tabelle `filter_languages`
--
ALTER TABLE `filter_languages`
  ADD PRIMARY KEY (`lang_abbrv`);

--
-- Indizes für die Tabelle `guild`
--
ALTER TABLE `guild`
  ADD PRIMARY KEY (`guild_id`);

--
-- Indizes für die Tabelle `names`
--
ALTER TABLE `names`
  ADD PRIMARY KEY (`name_id`);

--
-- Indizes für die Tabelle `name_filter`
--
ALTER TABLE `name_filter`
  ADD PRIMARY KEY (`word_id`);

--
-- Indizes für die Tabelle `nickname`
--
ALTER TABLE `nickname`
  ADD PRIMARY KEY (`fk_user_id`,`fk_guild_id`);

--
-- Indizes für die Tabelle `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- Indizes für die Tabelle `warnings`
--
ALTER TABLE `warnings`
  ADD PRIMARY KEY (`fk_guild_id`,`warning_id`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `action_log`
--
ALTER TABLE `action_log`
  MODIFY `log_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `bancollect`
--
ALTER TABLE `bancollect`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT für Tabelle `filter`
--
ALTER TABLE `filter`
  MODIFY `filter_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1853;

--
-- AUTO_INCREMENT für Tabelle `names`
--
ALTER TABLE `names`
  MODIFY `name_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT für Tabelle `name_filter`
--
ALTER TABLE `name_filter`
  MODIFY `word_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=687;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `warnings`
--
ALTER TABLE `warnings`
  ADD CONSTRAINT `warnings_ibfk_1` FOREIGN KEY (`fk_guild_id`) REFERENCES `guild` (`guild_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
