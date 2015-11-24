-- --------------------------------------------------------
-- 主机:                           10.32.140.161
-- 服务器版本:                        5.5.31-log - MySQL Community Server (GPL)
-- 服务器操作系统:                      Linux
-- HeidiSQL 版本:                  9.1.0.4867
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- 导出  表 zcms.mssuggest 结构
CREATE TABLE IF NOT EXISTS `mssuggest` (
  `suggest_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `suggest_content` varchar(1000) DEFAULT NULL COMMENT '投诉建议内容',
  `contact_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `source_url` varchar(2000) DEFAULT NULL COMMENT '源url',
  PRIMARY KEY (`suggest_id`)
) ENGINE=InnoDB AUTO_INCREMENT=222 DEFAULT CHARSET=utf8;

-- 正在导出表  zcms.mssuggest 的数据：~41 rows (大约)
/*!40000 ALTER TABLE `mssuggest` DISABLE KEYS */;
INSERT INTO `mssuggest` (`suggest_id`, `suggest_content`, `contact_mobile`, `create_time`, `source_url`) VALUES
	(175, 'dasdasd', 'asdsad', '2015-01-07 16:03:32', NULL),
	(176, 'dsadsad', 'asdasd', '2015-01-07 16:04:10', NULL),
	(177, 'dfsdfsdf', 'sdfsdfsd', '2015-01-07 17:51:42', 'http://cu.dds.com/brand/promotion/showProm.htm?brand=-1&orderByItem=endingTime&currentPage=1&activity=all&status=all'),
	(178, 'asdasdas', 'sadsad', '2015-01-08 13:18:47', 'http://www.dds.com/gycx/about.shtml'),
	(179, 'dsdsfdsfd', 'sdfsdf', '2015-01-08 13:19:17', 'http://www.dds.com/gycx/contact.shtml'),
	(180, 'wefsf', 'sdfdsf', '2015-01-08 13:19:50', 'http://www.dds.com/gycx/terms.shtml'),
	(181, 'ewfsdf', 'sdfsdf', '2015-01-08 13:20:36', 'http://www.dds.com/gycx/impunity.shtml'),
	(182, '隐私权声明', '啊实打实', '2015-01-08 13:22:59', 'http://www.dds.com/gycx/privacy.shtml'),
	(183, '用户服务协议\n', '时发生地方', '2015-01-08 13:27:50', 'http://www.dds.com/gycx/agreement.shtml'),
	(184, 'test123', '123', '2015-01-08 13:28:35', 'www.dds.com/gycx/complaint.shtml'),
	(185, '车享价说明', '是的第三方', '2015-01-08 13:28:40', 'http://www.dds.com/gycx/carPrice.shtml'),
	(186, '测试测速', '水电费水电费', '2015-01-08 13:29:29', 'http://www.dds.com/'),
	(189, 'adasdsad', 'asdasdas', '2015-01-08 13:33:11', 'http://www.dds.com/gycx/about.shtml'),
	(190, '撒大声地', '是顺丰到付', '2015-01-08 13:51:07', 'http://www.dds.com/gycx/about.shtml'),
	(192, '22222', '', '2015-01-08 14:09:44', 'http://www.dds.com/gycx/complaint.shtml'),
	(194, 'cms测试测试啦', '13123', '2015-01-08 14:12:46', 'http://www.dds.com/gycx/complaint.shtml'),
	(195, '请留下您的意见和建议，帮助我们做的更好 xczxcxczx', 'zxczxc', '2015-01-08 14:14:55', 'http://www.dds.com/gycx/impunity.shtml'),
	(196, 'zxvzxvxvxcvxc', 'xcvxcvxcv', '2015-01-08 14:15:38', 'http://www.dds.com/'),
	(197, 'safsdfsd', 'sdfsdf', '2015-01-08 14:17:01', 'http://cu.dds.com/brand/promotion/showProm.htm?brand=-1&orderByItem=endingTime&currentPage=1&activity=all&status=all'),
	(198, 'adasdsad', 'sadad', '2015-01-08 14:18:48', 'http://www.dds.com/'),
	(199, 'sdcdsfsd', 'sdfsdf', '2015-01-08 14:20:18', 'http://www.dds.com/'),
	(200, 'dsfsdf', 'sdfsd', '2015-01-08 14:27:28', 'http://www.dds.com/'),
	(201, 'sfdfsfsdfsd', 'sdfsdf', '2015-01-08 14:31:40', 'http://www.dds.com/gycx/carPrice.shtml'),
	(202, 'zzczxc', 'zxczxc', '2015-01-08 14:42:38', 'http://www.dds.com/gycx/carPrice.shtml'),
	(203, 'wwww', '', '2015-01-08 14:43:59', 'http://www.dds.com/gycx/complaint.shtml'),
	(204, 'www', '', '2015-01-08 14:44:13', 'http://www.dds.com/'),
	(205, 'www', '', '2015-01-08 14:44:37', 'http://cu.dds.com//brand/promotion/showProm.htm?brand=-1&orderByItem=endingTime&currentPage=1&activity=all&status=all'),
	(206, 'sdfsdfs', 'sdfsdf', '2015-01-08 14:45:25', 'h'),
	(207, 'dsfdfd', 'dfgdfg', '2015-01-08 14:52:56', 'http://member.dds.com/member/member.htm'),
	(208, 'adasdasda', 'asdasd', '2015-01-08 15:20:15', 'http://member.dds.com/member/benefits/orderlist.htm'),
	(209, 'fsdfsdf', 'sdfsf', '2015-01-08 15:20:46', ''),
	(210, 'sfsdfsd', 'dffsd', '2015-01-08 15:21:20', ''),
	(213, 'dasfasf', 'afasfas', '2015-01-08 16:38:37', 'http://www.dds.com/'),
	(214, 'sdasdasd', 'asdsad', '2015-01-08 16:39:33', 'http://www.dds.com/'),
	(215, 'dasdasd', 'asdsad', '2015-01-09 10:52:33', 'https://account.dds.com/account/login.htm?backUrl=http%3A%2F%2Fwww.dds.com%2F%23'),
	(216, 'dasdasdasd', 'asdsad', '2015-01-09 11:15:00', 'http://www.dds.com/'),
	(217, 'sadasd', 'asdasd', '2015-01-09 11:15:53', 'http://www.dds.com/gycx/about.shtml'),
	(218, 'adasdasd', 'asdas', '2015-01-09 11:17:53', 'http://www.dds.com/gycx/about.shtml'),
	(219, 'ssafsfsfdf', '', '2015-03-22 11:04:34', 'http%3A%2F%2Fwww.dds.com%2F'),
	(220, 'fsdfsf', '15658031695', '2015-03-25 15:50:01', 'http://www.dds.com/'),
	(221, 'qweqweqweq', '15658031695', '2015-03-25 18:23:21', 'http://www.dds.com/');
/*!40000 ALTER TABLE `mssuggest` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
