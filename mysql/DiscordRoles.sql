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
-- Datenbank: `discordroles`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `guilds`
--

CREATE TABLE `guilds` (
  `guild_id` bigint(20) NOT NULL,
  `name` text COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `roles`
--

CREATE TABLE `roles` (
  `role_id` bigint(20) NOT NULL,
  `name` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_category_abv` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_guild_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `role_category`
--

CREATE TABLE `role_category` (
  `category_abv` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rank` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `role_category`
--

INSERT INTO `role_category` (`category_abv`, `rank`) VALUES
('adm', 'Administrator'),
('bot', 'Bot'),
('com', 'Community'),
('mod', 'Moderator'),
('mut', 'Mute');

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `guilds`
--
ALTER TABLE `guilds`
  ADD PRIMARY KEY (`guild_id`);

--
-- Indizes für die Tabelle `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`),
  ADD KEY `fk_guild_id` (`fk_guild_id`),
  ADD KEY `fk_category_abv` (`fk_category_abv`);

--
-- Indizes für die Tabelle `role_category`
--
ALTER TABLE `role_category`
  ADD PRIMARY KEY (`category_abv`);

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `roles`
--
ALTER TABLE `roles`
  ADD CONSTRAINT `roles_ibfk_1` FOREIGN KEY (`fk_guild_id`) REFERENCES `guilds` (`guild_id`),
  ADD CONSTRAINT `roles_ibfk_2` FOREIGN KEY (`fk_category_abv`) REFERENCES `role_category` (`category_abv`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
