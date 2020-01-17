-- phpMyAdmin SQL Dump
-- version 3.2.3
-- http://www.phpmyadmin.net
--
-- 호스트: localhost
-- 처리한 시간: 19-12-19 14:12 
-- 서버 버전: 5.1.41
-- PHP 버전: 5.2.12

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- 데이터베이스: `projectdb`
--

-- --------------------------------------------------------

--
-- 테이블 구조 `board`
--

CREATE TABLE IF NOT EXISTS `board` (
  `num` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(50) DEFAULT NULL,
  `ddate` datetime DEFAULT NULL,
  `edate` datetime DEFAULT NULL,
  `egroup` int(11) DEFAULT NULL,
  `mgroup` int(11) DEFAULT NULL,
  `orgPrice` int(11) DEFAULT NULL,
  `gbPrice` int(11) DEFAULT NULL,
  `image` text,
  `way` text,
  `description` text,
  `uid` varchar(20) DEFAULT NULL,
  `address` text,
  `maddress` text,
  `amount` text,
  `orgWeight` text,
  `gbWeight` text,
  `danwi` text,
  PRIMARY KEY (`num`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=97 ;


-- --------------------------------------------------------

--
-- 테이블 구조 `participate`
--

CREATE TABLE IF NOT EXISTS `participate` (
  `num` int(11) NOT NULL DEFAULT '0',
  `id` varchar(20) NOT NULL DEFAULT '',
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`num`,`id`),
  KEY `p_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- 테이블 구조 `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` varchar(20) NOT NULL,
  `pw` varchar(20) DEFAULT NULL,
  `nickname` varchar(10) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;





--
-- Constraints for table `participate`
--
ALTER TABLE `participate`
  ADD CONSTRAINT `p_id` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `p` FOREIGN KEY (`num`) REFERENCES `board` (`num`) ON DELETE CASCADE,
  ADD CONSTRAINT `participate_ibfk_1` FOREIGN KEY (`id`) REFERENCES `user` (`id`);
