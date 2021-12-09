-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: mantisbtdb:3306
-- Generation Time: Dec 08, 2021 at 01:59 PM
-- Server version: 5.7.36
-- PHP Version: 7.4.20

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bugtracker`
--

-- --------------------------------------------------------

--
-- Table structure for table `mantis_api_token_table`
--

CREATE TABLE `mantis_api_token_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `name` varchar(128) NOT NULL,
  `hash` varchar(128) NOT NULL,
  `date_created` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `date_used` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_api_token_table`
--

INSERT INTO `mantis_api_token_table` (`id`, `user_id`, `name`, `hash`, `date_created`, `date_used`) VALUES
(1, 1, 'Redscan', 'a363bad81d9818f1d8471a3920af25ca123f3a25d1ff7a3a08dbf4945c2e471d', 1638524033, 1);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bugnote_table`
--

CREATE TABLE `mantis_bugnote_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `reporter_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `bugnote_text_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `note_type` int(11) DEFAULT '0',
  `note_attr` varchar(250) DEFAULT '',
  `time_tracking` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `last_modified` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `date_submitted` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bugnote_text_table`
--

CREATE TABLE `mantis_bugnote_text_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `note` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_file_table`
--

CREATE TABLE `mantis_bug_file_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `title` varchar(250) NOT NULL DEFAULT '',
  `description` varchar(250) NOT NULL DEFAULT '',
  `diskfile` varchar(250) NOT NULL DEFAULT '',
  `filename` varchar(250) NOT NULL DEFAULT '',
  `folder` varchar(250) NOT NULL DEFAULT '',
  `filesize` int(11) NOT NULL DEFAULT '0',
  `file_type` varchar(250) NOT NULL DEFAULT '',
  `content` longblob,
  `date_added` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `bugnote_id` int(10) UNSIGNED DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_history_table`
--

CREATE TABLE `mantis_bug_history_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `field_name` varchar(64) NOT NULL,
  `old_value` varchar(255) NOT NULL,
  `new_value` varchar(255) NOT NULL,
  `type` smallint(6) NOT NULL DEFAULT '0',
  `date_modified` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_monitor_table`
--

CREATE TABLE `mantis_bug_monitor_table` (
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_relationship_table`
--

CREATE TABLE `mantis_bug_relationship_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `source_bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `destination_bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `relationship_type` smallint(6) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_revision_table`
--

CREATE TABLE `mantis_bug_revision_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `bug_id` int(10) UNSIGNED NOT NULL,
  `bugnote_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `user_id` int(10) UNSIGNED NOT NULL,
  `type` int(10) UNSIGNED NOT NULL,
  `value` longtext NOT NULL,
  `timestamp` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_table`
--

CREATE TABLE `mantis_bug_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `reporter_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `handler_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `duplicate_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `priority` smallint(6) NOT NULL DEFAULT '30',
  `severity` smallint(6) NOT NULL DEFAULT '50',
  `reproducibility` smallint(6) NOT NULL DEFAULT '10',
  `status` smallint(6) NOT NULL DEFAULT '10',
  `resolution` smallint(6) NOT NULL DEFAULT '10',
  `projection` smallint(6) NOT NULL DEFAULT '10',
  `eta` smallint(6) NOT NULL DEFAULT '10',
  `bug_text_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `os` varchar(32) NOT NULL DEFAULT '',
  `os_build` varchar(32) NOT NULL DEFAULT '',
  `platform` varchar(32) NOT NULL DEFAULT '',
  `version` varchar(64) NOT NULL DEFAULT '',
  `fixed_in_version` varchar(64) NOT NULL DEFAULT '',
  `build` varchar(32) NOT NULL DEFAULT '',
  `profile_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `summary` varchar(128) NOT NULL DEFAULT '',
  `sponsorship_total` int(11) NOT NULL DEFAULT '0',
  `sticky` tinyint(4) NOT NULL DEFAULT '0',
  `target_version` varchar(64) NOT NULL DEFAULT '',
  `category_id` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `date_submitted` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `due_date` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `last_updated` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_tag_table`
--

CREATE TABLE `mantis_bug_tag_table` (
  `bug_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `tag_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `date_attached` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_bug_text_table`
--

CREATE TABLE `mantis_bug_text_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `description` longtext NOT NULL,
  `steps_to_reproduce` longtext NOT NULL,
  `additional_information` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_category_table`
--

CREATE TABLE `mantis_category_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `name` varchar(128) NOT NULL DEFAULT '',
  `status` int(10) UNSIGNED NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_category_table`
--

INSERT INTO `mantis_category_table` (`id`, `project_id`, `user_id`, `name`, `status`) VALUES
(1, 0, 0, 'General', 0);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_config_table`
--

CREATE TABLE `mantis_config_table` (
  `config_id` varchar(64) NOT NULL,
  `project_id` int(11) NOT NULL DEFAULT '0',
  `user_id` int(11) NOT NULL DEFAULT '0',
  `access_reqd` int(11) DEFAULT '0',
  `type` int(11) DEFAULT '90',
  `value` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_config_table`
--

INSERT INTO `mantis_config_table` (`config_id`, `project_id`, `user_id`, `access_reqd`, `type`, `value`) VALUES
('csv_columns', 0, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"tags\",\"status\",\"last_updated\"]'),
('csv_columns', 1, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"custom_Url\",\"tags\",\"status\",\"custom_Origin\",\"last_updated\"]'),
('database_version', 0, 0, 90, 1, '211'),
('excel_columns', 0, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"tags\",\"status\",\"last_updated\"]'),
('excel_columns', 1, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"custom_Url\",\"tags\",\"status\",\"custom_Origin\",\"last_updated\"]'),
('print_issues_page_columns', 0, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"tags\",\"status\",\"last_updated\"]'),
('print_issues_page_columns', 1, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"custom_Url\",\"tags\",\"status\",\"custom_Origin\",\"last_updated\"]'),
('view_issues_page_columns', 0, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"tags\",\"status\",\"last_updated\"]'),
('view_issues_page_columns', 1, 0, 90, 3, '[\"selection\",\"edit\",\"priority\",\"id\",\"severity\",\"summary\",\"custom_Url\",\"tags\",\"status\",\"custom_Origin\",\"last_updated\"]');

-- --------------------------------------------------------

--
-- Table structure for table `mantis_custom_field_project_table`
--

CREATE TABLE `mantis_custom_field_project_table` (
  `field_id` int(11) NOT NULL DEFAULT '0',
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `sequence` smallint(6) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_custom_field_project_table`
--

INSERT INTO `mantis_custom_field_project_table` (`field_id`, `project_id`, `sequence`) VALUES
(1, 1, 0),
(2, 1, 0),
(3, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_custom_field_string_table`
--

CREATE TABLE `mantis_custom_field_string_table` (
  `field_id` int(11) NOT NULL DEFAULT '0',
  `bug_id` int(11) NOT NULL DEFAULT '0',
  `value` varchar(255) NOT NULL DEFAULT '',
  `text` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_custom_field_table`
--

CREATE TABLE `mantis_custom_field_table` (
  `id` int(11) NOT NULL,
  `name` varchar(64) NOT NULL DEFAULT '',
  `type` smallint(6) NOT NULL DEFAULT '0',
  `possible_values` text,
  `default_value` varchar(255) NOT NULL DEFAULT '',
  `valid_regexp` varchar(255) NOT NULL DEFAULT '',
  `access_level_r` smallint(6) NOT NULL DEFAULT '0',
  `access_level_rw` smallint(6) NOT NULL DEFAULT '0',
  `length_min` int(11) NOT NULL DEFAULT '0',
  `length_max` int(11) NOT NULL DEFAULT '0',
  `require_report` tinyint(4) NOT NULL DEFAULT '0',
  `require_update` tinyint(4) NOT NULL DEFAULT '0',
  `display_report` tinyint(4) NOT NULL DEFAULT '0',
  `display_update` tinyint(4) NOT NULL DEFAULT '1',
  `require_resolved` tinyint(4) NOT NULL DEFAULT '0',
  `display_resolved` tinyint(4) NOT NULL DEFAULT '0',
  `display_closed` tinyint(4) NOT NULL DEFAULT '0',
  `require_closed` tinyint(4) NOT NULL DEFAULT '0',
  `filter_by` tinyint(4) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_custom_field_table`
--

INSERT INTO `mantis_custom_field_table` (`id`, `name`, `type`, `possible_values`, `default_value`, `valid_regexp`, `access_level_r`, `access_level_rw`, `length_min`, `length_max`, `require_report`, `require_update`, `display_report`, `display_update`, `require_resolved`, `display_resolved`, `display_closed`, `require_closed`, `filter_by`) VALUES
(1, 'InternalID', 0, '', '', '', 10, 10, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1),
(2, 'Url', 0, '', '', '', 10, 10, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1),
(3, 'Origin', 0, '', '', '', 10, 10, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_email_table`
--

CREATE TABLE `mantis_email_table` (
  `email_id` int(10) UNSIGNED NOT NULL,
  `email` varchar(191) NOT NULL DEFAULT '',
  `subject` varchar(250) NOT NULL DEFAULT '',
  `metadata` longtext NOT NULL,
  `body` longtext NOT NULL,
  `submitted` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_filters_table`
--

CREATE TABLE `mantis_filters_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `project_id` int(11) NOT NULL DEFAULT '0',
  `is_public` tinyint(4) DEFAULT NULL,
  `name` varchar(64) NOT NULL DEFAULT '',
  `filter_string` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_news_table`
--

CREATE TABLE `mantis_news_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `poster_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `announcement` tinyint(4) NOT NULL DEFAULT '0',
  `headline` varchar(64) NOT NULL DEFAULT '',
  `body` longtext NOT NULL,
  `last_modified` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `date_posted` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_plugin_table`
--

CREATE TABLE `mantis_plugin_table` (
  `basename` varchar(40) NOT NULL,
  `enabled` tinyint(4) NOT NULL DEFAULT '0',
  `protected` tinyint(4) NOT NULL DEFAULT '0',
  `priority` int(10) UNSIGNED NOT NULL DEFAULT '3'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_plugin_table`
--

INSERT INTO `mantis_plugin_table` (`basename`, `enabled`, `protected`, `priority`) VALUES
('MantisCoreFormatting', 1, 0, 3);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_project_file_table`
--

CREATE TABLE `mantis_project_file_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `title` varchar(250) NOT NULL DEFAULT '',
  `description` varchar(250) NOT NULL DEFAULT '',
  `diskfile` varchar(250) NOT NULL DEFAULT '',
  `filename` varchar(250) NOT NULL DEFAULT '',
  `folder` varchar(250) NOT NULL DEFAULT '',
  `filesize` int(11) NOT NULL DEFAULT '0',
  `file_type` varchar(250) NOT NULL DEFAULT '',
  `content` longblob,
  `date_added` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_project_hierarchy_table`
--

CREATE TABLE `mantis_project_hierarchy_table` (
  `child_id` int(10) UNSIGNED NOT NULL,
  `parent_id` int(10) UNSIGNED NOT NULL,
  `inherit_parent` tinyint(4) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_project_table`
--

CREATE TABLE `mantis_project_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(128) NOT NULL DEFAULT '',
  `status` smallint(6) NOT NULL DEFAULT '10',
  `enabled` tinyint(4) NOT NULL DEFAULT '1',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `access_min` smallint(6) NOT NULL DEFAULT '10',
  `file_path` varchar(250) NOT NULL DEFAULT '',
  `description` longtext NOT NULL,
  `category_id` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `inherit_global` tinyint(4) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_project_table`
--

INSERT INTO `mantis_project_table` (`id`, `name`, `status`, `enabled`, `view_state`, `access_min`, `file_path`, `description`, `category_id`, `inherit_global`) VALUES
(1, 'Redscan', 50, 1, 10, 10, '', '', 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_project_user_list_table`
--

CREATE TABLE `mantis_project_user_list_table` (
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `access_level` smallint(6) NOT NULL DEFAULT '10'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_project_version_table`
--

CREATE TABLE `mantis_project_version_table` (
  `id` int(11) NOT NULL,
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `version` varchar(64) NOT NULL DEFAULT '',
  `description` longtext NOT NULL,
  `released` tinyint(4) NOT NULL DEFAULT '1',
  `obsolete` tinyint(4) NOT NULL DEFAULT '0',
  `date_order` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_sponsorship_table`
--

CREATE TABLE `mantis_sponsorship_table` (
  `id` int(11) NOT NULL,
  `bug_id` int(11) NOT NULL DEFAULT '0',
  `user_id` int(11) NOT NULL DEFAULT '0',
  `amount` int(11) NOT NULL DEFAULT '0',
  `logo` varchar(128) NOT NULL DEFAULT '',
  `url` varchar(128) NOT NULL DEFAULT '',
  `paid` tinyint(4) NOT NULL DEFAULT '0',
  `date_submitted` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `last_updated` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_tag_table`
--

CREATE TABLE `mantis_tag_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `name` varchar(100) NOT NULL DEFAULT '',
  `description` longtext NOT NULL,
  `date_created` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `date_updated` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_tokens_table`
--

CREATE TABLE `mantis_tokens_table` (
  `id` int(11) NOT NULL,
  `owner` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `value` longtext NOT NULL,
  `timestamp` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `expiry` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_tokens_table`
--

INSERT INTO `mantis_tokens_table` (`id`, `owner`, `type`, `value`, `timestamp`, `expiry`) VALUES
(2, 1, 5, '{\"filter\":false,\"history\":true}', 1638971810, 1670507902),
(3, 1, 4, '1', 1638971844, 1638972202);

-- --------------------------------------------------------

--
-- Table structure for table `mantis_user_pref_table`
--

CREATE TABLE `mantis_user_pref_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `project_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `default_profile` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `default_project` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `refresh_delay` int(11) NOT NULL DEFAULT '0',
  `redirect_delay` int(11) NOT NULL DEFAULT '0',
  `bugnote_order` varchar(4) NOT NULL DEFAULT 'ASC',
  `email_on_new` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_assigned` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_feedback` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_resolved` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_closed` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_reopened` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_bugnote` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_status` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_priority` tinyint(4) NOT NULL DEFAULT '0',
  `email_on_priority_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_status_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_bugnote_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_reopened_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_closed_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_resolved_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_feedback_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_assigned_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_on_new_min_severity` smallint(6) NOT NULL DEFAULT '10',
  `email_bugnote_limit` smallint(6) NOT NULL DEFAULT '0',
  `language` varchar(32) NOT NULL DEFAULT 'english',
  `timezone` varchar(32) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_user_print_pref_table`
--

CREATE TABLE `mantis_user_print_pref_table` (
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `print_pref` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_user_profile_table`
--

CREATE TABLE `mantis_user_profile_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `platform` varchar(32) NOT NULL DEFAULT '',
  `os` varchar(32) NOT NULL DEFAULT '',
  `os_build` varchar(32) NOT NULL DEFAULT '',
  `description` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `mantis_user_table`
--

CREATE TABLE `mantis_user_table` (
  `id` int(10) UNSIGNED NOT NULL,
  `username` varchar(191) NOT NULL DEFAULT '',
  `realname` varchar(191) NOT NULL DEFAULT '',
  `email` varchar(191) NOT NULL DEFAULT '',
  `password` varchar(64) NOT NULL DEFAULT '',
  `enabled` tinyint(4) NOT NULL DEFAULT '1',
  `protected` tinyint(4) NOT NULL DEFAULT '0',
  `access_level` smallint(6) NOT NULL DEFAULT '10',
  `login_count` int(11) NOT NULL DEFAULT '0',
  `lost_password_request_count` smallint(6) NOT NULL DEFAULT '0',
  `failed_login_count` smallint(6) NOT NULL DEFAULT '0',
  `cookie_string` varchar(64) NOT NULL DEFAULT '',
  `last_visit` int(10) UNSIGNED NOT NULL DEFAULT '1',
  `date_created` int(10) UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `mantis_user_table`
--

INSERT INTO `mantis_user_table` (`id`, `username`, `realname`, `email`, `password`, `enabled`, `protected`, `access_level`, `login_count`, `lost_password_request_count`, `failed_login_count`, `cookie_string`, `last_visit`, `date_created`) VALUES
(1, 'administrator', '', 'root@localhost', '757d20b7cd71d05d01c51e996069aa9c', 1, 0, 90, 8, 0, 0, 'I8dKGs8vdPisUsRaFhzSvAgJGwul5-G2YI5h5bORM_kFwf87h1jxeeAmt325MgEy', 1638971902, 1638523843);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `mantis_api_token_table`
--
ALTER TABLE `mantis_api_token_table`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_user_id_name` (`user_id`,`name`);

--
-- Indexes for table `mantis_bugnote_table`
--
ALTER TABLE `mantis_bugnote_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_bug` (`bug_id`),
  ADD KEY `idx_last_mod` (`last_modified`);

--
-- Indexes for table `mantis_bugnote_text_table`
--
ALTER TABLE `mantis_bugnote_text_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_bug_file_table`
--
ALTER TABLE `mantis_bug_file_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_bug_file_bug_id` (`bug_id`),
  ADD KEY `idx_diskfile` (`diskfile`);

--
-- Indexes for table `mantis_bug_history_table`
--
ALTER TABLE `mantis_bug_history_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_bug_history_bug_id` (`bug_id`),
  ADD KEY `idx_history_user_id` (`user_id`),
  ADD KEY `idx_bug_history_date_modified` (`date_modified`);

--
-- Indexes for table `mantis_bug_monitor_table`
--
ALTER TABLE `mantis_bug_monitor_table`
  ADD PRIMARY KEY (`user_id`,`bug_id`),
  ADD KEY `idx_bug_id` (`bug_id`);

--
-- Indexes for table `mantis_bug_relationship_table`
--
ALTER TABLE `mantis_bug_relationship_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_relationship_source` (`source_bug_id`),
  ADD KEY `idx_relationship_destination` (`destination_bug_id`);

--
-- Indexes for table `mantis_bug_revision_table`
--
ALTER TABLE `mantis_bug_revision_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_bug_rev_type` (`type`),
  ADD KEY `idx_bug_rev_id_time` (`bug_id`,`timestamp`);

--
-- Indexes for table `mantis_bug_table`
--
ALTER TABLE `mantis_bug_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_bug_sponsorship_total` (`sponsorship_total`),
  ADD KEY `idx_bug_fixed_in_version` (`fixed_in_version`),
  ADD KEY `idx_bug_status` (`status`),
  ADD KEY `idx_project` (`project_id`);

--
-- Indexes for table `mantis_bug_tag_table`
--
ALTER TABLE `mantis_bug_tag_table`
  ADD PRIMARY KEY (`bug_id`,`tag_id`),
  ADD KEY `idx_bug_tag_tag_id` (`tag_id`);

--
-- Indexes for table `mantis_bug_text_table`
--
ALTER TABLE `mantis_bug_text_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_category_table`
--
ALTER TABLE `mantis_category_table`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_category_project_name` (`project_id`,`name`);

--
-- Indexes for table `mantis_config_table`
--
ALTER TABLE `mantis_config_table`
  ADD PRIMARY KEY (`config_id`,`project_id`,`user_id`);

--
-- Indexes for table `mantis_custom_field_project_table`
--
ALTER TABLE `mantis_custom_field_project_table`
  ADD PRIMARY KEY (`field_id`,`project_id`);

--
-- Indexes for table `mantis_custom_field_string_table`
--
ALTER TABLE `mantis_custom_field_string_table`
  ADD PRIMARY KEY (`field_id`,`bug_id`),
  ADD KEY `idx_custom_field_bug` (`bug_id`);

--
-- Indexes for table `mantis_custom_field_table`
--
ALTER TABLE `mantis_custom_field_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_custom_field_name` (`name`);

--
-- Indexes for table `mantis_email_table`
--
ALTER TABLE `mantis_email_table`
  ADD PRIMARY KEY (`email_id`);

--
-- Indexes for table `mantis_filters_table`
--
ALTER TABLE `mantis_filters_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_news_table`
--
ALTER TABLE `mantis_news_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_plugin_table`
--
ALTER TABLE `mantis_plugin_table`
  ADD PRIMARY KEY (`basename`);

--
-- Indexes for table `mantis_project_file_table`
--
ALTER TABLE `mantis_project_file_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_project_hierarchy_table`
--
ALTER TABLE `mantis_project_hierarchy_table`
  ADD UNIQUE KEY `idx_project_hierarchy` (`child_id`,`parent_id`),
  ADD KEY `idx_project_hierarchy_child_id` (`child_id`),
  ADD KEY `idx_project_hierarchy_parent_id` (`parent_id`);

--
-- Indexes for table `mantis_project_table`
--
ALTER TABLE `mantis_project_table`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_project_name` (`name`),
  ADD KEY `idx_project_view` (`view_state`);

--
-- Indexes for table `mantis_project_user_list_table`
--
ALTER TABLE `mantis_project_user_list_table`
  ADD PRIMARY KEY (`project_id`,`user_id`),
  ADD KEY `idx_project_user` (`user_id`);

--
-- Indexes for table `mantis_project_version_table`
--
ALTER TABLE `mantis_project_version_table`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_project_version` (`project_id`,`version`);

--
-- Indexes for table `mantis_sponsorship_table`
--
ALTER TABLE `mantis_sponsorship_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sponsorship_bug_id` (`bug_id`),
  ADD KEY `idx_sponsorship_user_id` (`user_id`);

--
-- Indexes for table `mantis_tag_table`
--
ALTER TABLE `mantis_tag_table`
  ADD PRIMARY KEY (`id`,`name`),
  ADD KEY `idx_tag_name` (`name`);

--
-- Indexes for table `mantis_tokens_table`
--
ALTER TABLE `mantis_tokens_table`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_typeowner` (`type`,`owner`);

--
-- Indexes for table `mantis_user_pref_table`
--
ALTER TABLE `mantis_user_pref_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_user_print_pref_table`
--
ALTER TABLE `mantis_user_print_pref_table`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `mantis_user_profile_table`
--
ALTER TABLE `mantis_user_profile_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `mantis_user_table`
--
ALTER TABLE `mantis_user_table`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_user_cookie_string` (`cookie_string`),
  ADD UNIQUE KEY `idx_user_username` (`username`),
  ADD KEY `idx_enable` (`enabled`),
  ADD KEY `idx_access` (`access_level`),
  ADD KEY `idx_email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `mantis_api_token_table`
--
ALTER TABLE `mantis_api_token_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `mantis_bugnote_table`
--
ALTER TABLE `mantis_bugnote_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bugnote_text_table`
--
ALTER TABLE `mantis_bugnote_text_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bug_file_table`
--
ALTER TABLE `mantis_bug_file_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bug_history_table`
--
ALTER TABLE `mantis_bug_history_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bug_relationship_table`
--
ALTER TABLE `mantis_bug_relationship_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bug_revision_table`
--
ALTER TABLE `mantis_bug_revision_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bug_table`
--
ALTER TABLE `mantis_bug_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_bug_text_table`
--
ALTER TABLE `mantis_bug_text_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_category_table`
--
ALTER TABLE `mantis_category_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `mantis_custom_field_table`
--
ALTER TABLE `mantis_custom_field_table`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `mantis_email_table`
--
ALTER TABLE `mantis_email_table`
  MODIFY `email_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_filters_table`
--
ALTER TABLE `mantis_filters_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_news_table`
--
ALTER TABLE `mantis_news_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_project_file_table`
--
ALTER TABLE `mantis_project_file_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_project_table`
--
ALTER TABLE `mantis_project_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `mantis_project_version_table`
--
ALTER TABLE `mantis_project_version_table`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_sponsorship_table`
--
ALTER TABLE `mantis_sponsorship_table`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_tag_table`
--
ALTER TABLE `mantis_tag_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_tokens_table`
--
ALTER TABLE `mantis_tokens_table`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `mantis_user_pref_table`
--
ALTER TABLE `mantis_user_pref_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_user_profile_table`
--
ALTER TABLE `mantis_user_profile_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `mantis_user_table`
--
ALTER TABLE `mantis_user_table`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
