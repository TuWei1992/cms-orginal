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

-- 导出  表 zcms.bcxdatamodel 结构
CREATE TABLE IF NOT EXISTS `bcxdatamodel` (
  `ID` bigint(20) NOT NULL,
  `APIName` varchar(200) NOT NULL,
  `Code` varchar(40) NOT NULL,
  `Name` varchar(200) NOT NULL,
  `Memo` varchar(200) DEFAULT NULL,
  `PageFlag` varchar(2) DEFAULT NULL,
  `OrderFlag` bigint(20) DEFAULT NULL,
  `AddTime` datetime NOT NULL,
  `AddUser` varchar(200) NOT NULL,
  `ModifyTime` datetime DEFAULT NULL,
  `ModifyUser` varchar(200) DEFAULT NULL,
  `BackupNo` varchar(15) NOT NULL,
  `BackupOperator` varchar(50) NOT NULL,
  `BackupTime` datetime NOT NULL,
  `BackupMemo` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`,`BackupNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 正在导出表  zcms.bcxdatamodel 的数据：~10 rows (大约)
/*!40000 ALTER TABLE `bcxdatamodel` DISABLE KEYS */;
INSERT INTO `bcxdatamodel` (`ID`, `APIName`, `Code`, `Name`, `Memo`, `PageFlag`, `OrderFlag`, `AddTime`, `AddUser`, `ModifyTime`, `ModifyUser`, `BackupNo`, `BackupOperator`, `BackupTime`, `BackupMemo`) VALUES
	(13, 'CarLib.SeriesStatisticsService.statAttrValuesBySeriesId', 'statAttrValuesBySeriesId', '车系的指定属性统计信息', '功能描述:\n查询指定车系的指定属性统计信息,如变速箱，车身结构等\n\n参数:\n    seriesId - 车系ID\n    attrTypeIds - 属性类型', NULL, 141949493797200, '2014-12-25 16:08:57', 'liuxue', '2014-12-26 10:33:29', 'zhangmengjia', '419500820827', 'zhangmengjia', '2014-12-26 10:35:31', 'Delete'),
	(14, 'CarLib.VelColorService.findVelColorById', 'carColor', '车系颜色', '', NULL, 141949571269400, '2014-12-25 16:21:52', 'zhangmengjia', '2014-12-25 16:26:23', 'zhangmengjia', '419490079688', 'zhangmengjia', '2014-12-25 16:32:56', 'Delete'),
	(15, 'CarLib.VelColorService.findVelColorById', 'carColor', '车系颜色id查找车系颜色', '', NULL, 141949649066200, '2014-12-25 16:34:50', 'zhangmengjia', '2014-12-25 16:34:58', 'zhangmengjia', '419490079697', 'zhangmengjia', '2014-12-25 17:18:52', 'Delete'),
	(17, 'CarLib.BrandService.findAllBrands', 'MAP', '所有品牌Map', '功能描述:\n查询所有品牌Map。key为品牌Id，value为品牌基本信息对象', NULL, 141949744701700, '2014-12-25 16:50:47', 'zhangmengjia', '2014-12-25 16:50:55', 'zhangmengjia', '419490079690', 'zhangmengjia', '2014-12-25 16:51:18', 'Delete'),
	(18, 'CarLib.VelModelInfoService.findColorListByModelId', 'MODE', '文本框', '功能描述:\n查询所有车型基本信息列表', NULL, 141949757406400, '2014-12-25 16:52:54', 'zhangmengjia', '2014-12-25 17:14:05', 'zhangmengjia', '419490079714', 'zhangmengjia', '2014-12-25 17:36:42', 'Delete'),
	(20, 'CarLib.VelModelInfoService.findVelModelByYearStyleId2', 'test', '多行文本框', '功能描述:\n根据年款ID,销售状态和数据状态获取车型信息', NULL, 141949859839100, '2014-12-25 17:09:58', 'zhangmengjia', '2014-12-25 17:10:05', 'zhangmengjia', '419500820825', 'zhangmengjia', '2014-12-25 18:10:56', 'Delete'),
	(22, 'CarLib.VelColorService.findVelColorAll', 'findVelColorAll', 'asdasd', '', NULL, 141950190719400, '2014-12-25 18:05:07', 'zhangmengjia', '2014-12-26 11:00:02', 'zhangmengjia', '419500820829', 'zhangmengjia', '2014-12-26 11:01:14', 'Delete'),
	(28, 'CarLib.SeriesStatisticsService.statOilConsumeBySeriesId', 'statOilConsumeBySeriesId', '多选框', '功能描述:\n统计指定车系油耗范围区间', NULL, 141956317108900, '2014-12-26 11:06:11', 'zhangmengjia', NULL, NULL, '419500820830', 'zhangmengjia', '2014-12-26 11:14:36', 'Delete'),
	(29, 'sadsadasd', 'asdsad', 'adasd', '', NULL, 141957245718600, '2014-12-26 13:40:57', 'zhangmengjia', NULL, NULL, '419500820833', 'zhangmengjia', '2014-12-26 13:41:55', 'Delete'),
	(35, 'CarLib.VelSeriesClient.findNewlySeries', 'newlySeriesList', '新上市车系', '', NULL, 142648908604700, '2015-03-16 14:58:06', 'sunjiangtao', '2015-03-16 15:06:35', 'sunjiangtao', '426230901143', 'sunjiangtao', '2015-03-16 15:23:58', 'Delete');
/*!40000 ALTER TABLE `bcxdatamodel` ENABLE KEYS */;


-- 导出  表 zcms.bcxdatamodelsearchcolumn 结构
CREATE TABLE IF NOT EXISTS `bcxdatamodelsearchcolumn` (
  `ID` bigint(20) NOT NULL,
  `ModelID` bigint(20) NOT NULL,
  `Code` varchar(50) NOT NULL,
  `Name` varchar(2000) NOT NULL,
  `ControlType` varchar(20) NOT NULL,
  `MandatoryFlag` varchar(2) NOT NULL,
  `ListOptions` varchar(2000) DEFAULT NULL,
  `DefaultValue` varchar(1000) DEFAULT NULL,
  `VerifyRule` varchar(200) DEFAULT NULL,
  `VerifyCondition` varchar(200) DEFAULT NULL,
  `StyleClass` varchar(200) DEFAULT NULL,
  `StyleText` varchar(400) DEFAULT NULL,
  `Memo` varchar(1000) DEFAULT NULL,
  `OrderFlag` bigint(20) DEFAULT NULL,
  `AddUser` varchar(50) NOT NULL,
  `AddTime` datetime NOT NULL,
  `ModifyUser` varchar(50) DEFAULT NULL,
  `ModifyTime` datetime DEFAULT NULL,
  `BackupNo` varchar(15) NOT NULL,
  `BackupOperator` varchar(50) NOT NULL,
  `BackupTime` datetime NOT NULL,
  `BackupMemo` varchar(200) DEFAULT NULL,
  `ArrayFlag` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`ID`,`BackupNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 正在导出表  zcms.bcxdatamodelsearchcolumn 的数据：~13 rows (大约)
/*!40000 ALTER TABLE `bcxdatamodelsearchcolumn` DISABLE KEYS */;
INSERT INTO `bcxdatamodelsearchcolumn` (`ID`, `ModelID`, `Code`, `Name`, `ControlType`, `MandatoryFlag`, `ListOptions`, `DefaultValue`, `VerifyRule`, `VerifyCondition`, `StyleClass`, `StyleText`, `Memo`, `OrderFlag`, `AddUser`, `AddTime`, `ModifyUser`, `ModifyTime`, `BackupNo`, `BackupOperator`, `BackupTime`, `BackupMemo`, `ArrayFlag`) VALUES
	(11, 12, 'attrTypeId', '车系名称', 'Text', 'Y', '', '', 'NotNull&&Int', '', '', '', '', 141949476425300, 'zhangmengjia', '2014-12-25 16:06:04', 'zhangmengjia', '2014-12-25 16:09:57', '419490079687', 'zhangmengjia', '2014-12-25 16:12:15', 'Delete', 'N'),
	(12, 13, 'attrTypeId', 'attrTypeId', 'Text', 'Y', '', '1', 'NotNull&&Int', '', '', '', '', 141949560785100, 'liuxue', '2014-12-25 16:20:07', 'liuxue', '2014-12-25 16:45:34', '419500820828', 'zhangmengjia', '2014-12-26 10:35:31', 'Delete', 'N'),
	(13, 14, 'velColorId', '车系颜色ID', 'Text', 'Y', 'Input:', '', 'NotNull', '', '', '', '', 141949586172000, 'zhangmengjia', '2014-12-25 16:24:21', 'zhangmengjia', '2014-12-25 16:32:00', '419490079689', 'zhangmengjia', '2014-12-25 16:32:56', 'Delete', NULL),
	(14, 15, 'velColorId', '车系颜色ID', 'Text', 'Y', 'Code:', '0', 'NotNull&&Int', '', '', '', '', 141949657819500, 'zhangmengjia', '2014-12-25 16:36:18', 'liuxue', '2014-12-25 16:45:06', '419490079698', 'zhangmengjia', '2014-12-25 17:18:52', 'Delete', 'N'),
	(17, 18, 'modelId', '车型Id', 'Text', 'N', '', '', 'Int', '', '', '', '', 141949890101200, 'zhangmengjia', '2014-12-25 17:15:01', NULL, NULL, '419490079715', 'zhangmengjia', '2014-12-25 17:36:42', 'Delete', NULL),
	(21, 20, 'seriesId', '车系ID', 'TextArea', 'N', '', '', 'NotNull&&Int', '', '', '', '', 141950219167600, 'zhangmengjia', '2014-12-25 18:09:51', NULL, NULL, '419500820823', 'zhangmengjia', '2014-12-25 18:10:52', 'Delete', 'N'),
	(22, 20, 'saleStatusList', '销售状态列表', 'TextArea', 'N', '', '', '', '', '', '', '', 141950223724000, 'zhangmengjia', '2014-12-25 18:10:37', NULL, NULL, '419500820824', 'zhangmengjia', '2014-12-25 18:10:52', 'Delete', 'N'),
	(25, 28, 'seriesId', '车系ID', 'Checkbox', 'Y', 'Input:3\n4\n2\n1', '', 'NotNull', '', '', '', '', 141956324017600, 'zhangmengjia', '2014-12-26 11:07:20', 'zhangmengjia', '2014-12-26 11:10:17', '419500820831', 'zhangmengjia', '2014-12-26 11:14:36', 'Delete', 'N'),
	(26, 29, 'sfsdf', 'sdf', 'ImageUpload', 'Y', '', '', 'NotNull', '', '', '', '', 141957248265400, 'zhangmengjia', '2014-12-26 13:41:22', 'zhangmengjia', '2014-12-26 13:41:34', '419500820834', 'zhangmengjia', '2014-12-26 13:41:55', 'Delete', 'N'),
	(27, 9, 'brandId', '品牌Id', 'Select', 'Y', 'Code:brandId', '', 'NotNull', '', '', '', '', 142597606534300, 'sunjiangtao', '2015-03-10 16:27:45', NULL, NULL, '423731235701', 'sunjiangtao', '2015-03-10 16:28:43', 'Delete', NULL),
	(29, 1, 'seriesId', '车系id', 'Text', 'Y', '', '', 'NotNull', '', '', '', '', 142605763392500, 'sunjiangtao', '2015-03-11 15:07:13', NULL, NULL, '423731235702', 'zhouquan', '2015-03-11 15:51:28', 'Delete', NULL),
	(32, 35, 'size', '查询数量', 'Text', 'Y', '', '6', 'NotNull', '', '', '', '', 142648913201100, 'sunjiangtao', '2015-03-16 14:58:52', NULL, NULL, '426230901144', 'sunjiangtao', '2015-03-16 15:23:58', 'Delete', NULL),
	(34, 19, 'seriesId', '车系ID', 'Text', 'N', '', '', '', '', '', '', '', 142664887041700, 'zhouquan', '2015-03-18 11:21:10', NULL, NULL, '426647949820', 'zhouquan', '2015-03-18 11:21:19', 'Delete', NULL);
/*!40000 ALTER TABLE `bcxdatamodelsearchcolumn` ENABLE KEYS */;


-- 导出  表 zcms.cxdatamodel 结构
CREATE TABLE IF NOT EXISTS `cxdatamodel` (
  `ID` bigint(20) NOT NULL,
  `APIName` varchar(200) NOT NULL,
  `Code` varchar(40) NOT NULL,
  `Name` varchar(200) NOT NULL,
  `Memo` varchar(200) DEFAULT NULL,
  `PageFlag` varchar(2) DEFAULT NULL,
  `OrderFlag` bigint(20) DEFAULT NULL,
  `AddTime` datetime NOT NULL,
  `AddUser` varchar(200) NOT NULL,
  `ModifyTime` datetime DEFAULT NULL,
  `ModifyUser` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 正在导出表  zcms.cxdatamodel 的数据：~34 rows (大约)
/*!40000 ALTER TABLE `cxdatamodel` DISABLE KEYS */;
INSERT INTO `cxdatamodel` (`ID`, `APIName`, `Code`, `Name`, `Memo`, `PageFlag`, `OrderFlag`, `AddTime`, `AddUser`, `ModifyTime`, `ModifyUser`) VALUES
	(1, 'ms.CityStationService.cityStations', 'citys', '城市', '', NULL, 141948675811900, '2014-12-25 13:52:38', 'zhouquan', NULL, NULL),
	(2, 'ms.DealerService.queryDealer', 'dealer', '经销商', '', NULL, 141948678504200, '2014-12-25 13:53:05', 'zhouquan', NULL, NULL),
	(3, 'ms.chexiangpaiService.indexPaiLists', 'chexiangPai', '车享拍', '', NULL, 141948689029300, '2014-12-25 13:54:50', 'zhouquan', NULL, NULL),
	(4, 'ms.AdvertiseService.bannersNew', 'adv', '广告', '', NULL, 141948694240700, '2014-12-25 13:55:42', 'zhouquan', NULL, NULL),
	(5, 'ms.AdvertiseService.homeBannersNew', 'adv_focusPicture', '焦点广告', '', NULL, 141948711840500, '2014-12-25 13:58:38', 'zhouquan', NULL, NULL),
	(6, 'ms.chexiangpaiService.indexPaiCitys', 'paiCitys', '车享拍城市', '', NULL, 141948725355300, '2014-12-25 14:00:53', 'zhouquan', NULL, NULL),
	(7, 'ms.HotPictureService.queryIndexHotPictures', 'hotPicture', '精美图库', '', NULL, 141948726778100, '2014-12-25 14:01:07', 'zhouquan', NULL, NULL),
	(8, 'ms.PromotionService.indexPromotions', 'promotions', '促销活动', '', NULL, 141948731267400, '2014-12-25 14:01:52', 'zhouquan', NULL, NULL),
	(9, 'ms.HotVelSeriesService.brands', 'brands', '品牌', '', NULL, 141948737597700, '2014-12-25 14:02:55', 'zhouquan', NULL, NULL),
	(10, 'ms.HotVelSeriesService.hotVelSeries', 'hotVelSeries', '热门车型', '', NULL, 141948738963900, '2014-12-25 14:03:09', 'zhouquan', NULL, NULL),
	(11, 'CarLib.VelSeriesService.findSeriesAll', 'carServies', '车型系列', '', NULL, 141949272349500, '2014-12-25 15:32:03', 'admin', '2014-12-25 15:32:46', 'admin'),
	(12, 'CarLib.VelColorService.findVelColorAll', 'findVelColorAll', '所有车系颜色集合', '', NULL, 141949417502000, '2014-12-25 15:56:15', 'zhangmengjia', '2014-12-26 11:01:27', 'zhangmengjia'),
	(16, 'CarLib.VelColorService.findVelColorsAfterDate', 'findVelColorsAfterDate', '时间日期选择框', '功能描述:查询最近更新的颜色\n查询最近更新的颜色', NULL, 141949714416300, '2014-12-25 16:45:44', 'zhangmengjia', '2014-12-26 11:00:31', 'zhangmengjia'),
	(19, 'CarLib.VelModelInfoService.findVelModelById', 'findVelModelById', '下拉框--手动输入', '功能描述:\n根据车型ID查询车型基本信息', NULL, 141949767338100, '2014-12-25 16:54:33', 'zhangmengjia', '2014-12-26 11:00:22', 'zhangmengjia'),
	(21, 'CarLib.VelModelInfoService.findVelModelBySeriesId2', 'findVelModelBySeriesId2', '车型信息', '功能描述:\n根据车系ID查询指定销售状态和数据状态的车型信息\n\n参数:\n    seriesId - 车系ID\n    saleStatusList - 销售状态列表\n    rowStatusList - 数据状态列表', NULL, 141950032634400, '2014-12-25 17:38:46', 'zhangmengjia', '2014-12-26 11:00:13', 'zhangmengjia'),
	(23, 'CarLib.VelColorService.findVelColorById', 'findVelColorById', '多行文本框', '', NULL, 141950194379000, '2014-12-25 18:05:43', 'zhangmengjia', '2014-12-26 10:59:53', 'zhangmengjia'),
	(24, 'userManageService.findUserInfoByUserId', 'findUserInfoByUserId', 'DOP', '', NULL, 141950226658900, '2014-12-25 18:11:06', 'admin', '2014-12-26 10:59:38', 'zhangmengjia'),
	(25, 'CarLib.BrandService.findBrandById', 'findBrandById', '单选框', '功能描述:\n根据ID查询品牌基本信息\n\n参数:\n    brandId - 品牌ID', NULL, 141950434513700, '2014-12-25 18:45:45', 'zhangmengjia', '2014-12-26 10:59:24', 'zhangmengjia'),
	(26, 'CarLib.BrandService.findAllBrands', 'findAllBrands', '测试测试', '功能描述:\n查询所有品牌Map。key为品牌Id，value为品牌基本信息对象\n\n返回:\n    Map 所有品牌信息', NULL, 141955963509900, '2014-12-26 10:07:15', 'zhangmengjia', '2014-12-26 10:58:57', 'zhangmengjia'),
	(27, 'CarLib.SeriesStatisticsService.findAllSeriesSize', 'findAllSeriesSize', 'ceshi', '功能描述:\n查询所有车系的排量统计信息', NULL, 141956083582400, '2014-12-26 10:27:15', 'zhangmengjia', '2014-12-26 10:58:47', 'zhangmengjia'),
	(30, 'sadsad', 'asdasd', 'sadsad', '', NULL, 141957755989700, '2014-12-26 15:05:59', 'zhangmengjia', NULL, NULL),
	(31, 'CarLib.VelSeriesClient.findSeriesListByBrandId', 'findSeriesListByBrandId', '根据品牌查车系', 'findSeriesByBrandId', NULL, 142597258665300, '2015-03-10 15:29:46', 'sunjiangtao', '2015-03-22 13:57:40', 'sunjiangtao'),
	(32, 'CarLib.VelSeriesClient.findVelModelListBySeriesId', 'findVelModelListBySeriesId', '可售车型列表', '查询待上市、已上市、已下市车型', NULL, 142605757435600, '2015-03-11 15:06:14', 'sunjiangtao', '2015-03-11 15:13:07', 'sunjiangtao'),
	(33, 'QuestionService.findQuestionForCMS', 'findQuestList', '车知道问题列表', '', NULL, 142606144718100, '2015-03-11 16:10:47', 'sunjiangtao', '2015-03-12 15:09:36', 'sunjiangtao'),
	(34, 'siteListService.list', 'storeList', '服务网点门店', '', NULL, 142621781627600, '2015-03-13 11:36:56', 'sunjiangtao', '2015-03-13 14:34:23', 'sunjiangtao'),
	(36, 'CarLib.VelSeriesClient.findNewlySeries', 'newlySeries', '新上市车系', '', NULL, 142649067619900, '2015-03-16 15:24:36', 'sunjiangtao', NULL, NULL),
	(37, 'CarLib.VelImgClient.findSeriesVelImageByPosId', 'OBDImage', 'OBD接口图片', '', NULL, 142664884289600, '2015-03-18 11:20:42', 'zhouquan', NULL, NULL),
	(38, 'CarLib.VelSeriesClient.findSeriesByBrandId', 'findSeriesByBrandId1', 'findSeriesByBrandId1', '', NULL, 142666341838500, '2015-03-18 15:23:38', 'zhouquan', NULL, NULL),
	(39, 'CarLib.VelSeriesClient.findSeriesDropDownList', 'findSeriesByBrandId', '车系下拉列表', '', NULL, 142700375479000, '2015-03-22 13:55:54', 'sunjiangtao', '2015-04-21 17:09:44', 'sunjiangtao'),
	(40, 'QuestionService.findQuestionByPageSize', 'findQuestionByPageSize', '车知道列表', '车享汇页面车知道list 16个', NULL, 142838707825400, '2015-04-07 14:11:18', 'zhangfujin', NULL, NULL),
	(41, 'PeccancyAreaService.getProvinceList', 'getProvinceList', '获取省份列表', '车享汇 违章查询获取省份列表', NULL, 142839328551200, '2015-04-07 15:54:45', 'zhangfujin', NULL, NULL),
	(42, 'PeccancyAreaService.getCityByProvince', 'getCityByProvince', '根据省份查询城市列表', '根据省份查询城市列表', NULL, 142839343052500, '2015-04-07 15:57:10', 'zhangfujin', NULL, NULL),
	(43, 'PeccancyAreaService.getHotCity', 'getHotCity', '获取所有城市', '获取所有城市', NULL, 142839365487200, '2015-04-07 16:00:54', 'zhangfujin', '2015-04-07 16:01:30', 'zhangfujin'),
	(44, 'IAdvertiseService.getAdvertise', 'ZAdTest', 'ZAdTest', '', NULL, 143167595521500, '2015-05-15 15:45:55', 'zhouquan', '2015-05-15 15:46:10', 'zhouquan');
/*!40000 ALTER TABLE `cxdatamodel` ENABLE KEYS */;


-- 导出  表 zcms.cxdatamodelsearchcolumn 结构
CREATE TABLE IF NOT EXISTS `cxdatamodelsearchcolumn` (
  `ID` bigint(20) NOT NULL,
  `ModelID` bigint(20) NOT NULL,
  `Code` varchar(50) NOT NULL,
  `Name` varchar(2000) NOT NULL,
  `ControlType` varchar(20) NOT NULL,
  `MandatoryFlag` varchar(2) NOT NULL,
  `ListOptions` varchar(2000) DEFAULT NULL,
  `DefaultValue` varchar(1000) DEFAULT NULL,
  `VerifyRule` varchar(200) DEFAULT NULL,
  `VerifyCondition` varchar(200) DEFAULT NULL,
  `StyleClass` varchar(200) DEFAULT NULL,
  `StyleText` varchar(400) DEFAULT NULL,
  `Memo` varchar(1000) DEFAULT NULL,
  `OrderFlag` bigint(20) DEFAULT NULL,
  `AddUser` varchar(50) NOT NULL,
  `AddTime` datetime NOT NULL,
  `ModifyUser` varchar(50) DEFAULT NULL,
  `ModifyTime` datetime DEFAULT NULL,
  `ArrayFlag` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 正在导出表  zcms.cxdatamodelsearchcolumn 的数据：~30 rows (大约)
/*!40000 ALTER TABLE `cxdatamodelsearchcolumn` DISABLE KEYS */;
INSERT INTO `cxdatamodelsearchcolumn` (`ID`, `ModelID`, `Code`, `Name`, `ControlType`, `MandatoryFlag`, `ListOptions`, `DefaultValue`, `VerifyRule`, `VerifyCondition`, `StyleClass`, `StyleText`, `Memo`, `OrderFlag`, `AddUser`, `AddTime`, `ModifyUser`, `ModifyTime`, `ArrayFlag`) VALUES
	(1, 2, 'cityId', '城市', 'Select', 'Y', 'Method:CXDataModel.getCitysMap', '', 'NotNull', '', '', '', '', 141948681699900, 'zhouquan', '2014-12-25 13:53:36', 'zhouquan', '2014-12-25 14:09:04', NULL),
	(2, 3, 'cityId', '城市', 'Select', 'Y', 'Method:CXDataModel.getCitysMap', '', 'NotNull', '', '', '', '', 141948691977300, 'zhouquan', '2014-12-25 13:55:19', 'zhouquan', '2014-12-25 14:09:00', NULL),
	(3, 4, 'cityId', '城市', 'Select', 'Y', 'Method:CXDataModel.getCitysMap', '', 'NotNull', '', '', '', '', 141948705194900, 'zhouquan', '2014-12-25 13:57:31', 'zhouquan', '2014-12-25 14:08:56', NULL),
	(4, 4, 'blockCodes', '广告位', 'Select', 'Y', 'Code:SaicAdvertiseType', '', 'NotNull', '', '', '', '', 141948709146500, 'zhouquan', '2014-12-25 13:58:11', 'zhouquan', '2014-12-25 14:08:51', 'Y'),
	(5, 5, 'cityId', '城市', 'Select', 'Y', 'Method:CXDataModel.getCitysMap', '', 'NotNull', '', '', '', '', 141948717613700, 'zhouquan', '2014-12-25 13:59:36', 'zhouquan', '2014-12-25 14:08:43', NULL),
	(6, 5, 'blockCode', '广告类型', 'Select', 'Y', 'Code:SaicFocusPictureType', '', 'NotNull', '', '', '', '', 141948722900800, 'zhouquan', '2014-12-25 14:00:29', 'zhouquan', '2014-12-25 14:08:46', NULL),
	(7, 7, 'cityId', '城市', 'Select', 'Y', 'Method:CXDataModel.getCitysMap', '', 'NotNull', '', '', '', '', 141948729458200, 'zhouquan', '2014-12-25 14:01:34', 'zhouquan', '2014-12-25 14:08:38', NULL),
	(8, 8, 'cityId', '城市', 'Select', 'Y', 'Method:CXDataModel.getCitysMap', '', 'NotNull', '', '', '', '', 141948735731300, 'zhouquan', '2014-12-25 14:02:37', 'zhouquan', '2014-12-25 14:08:34', NULL),
	(9, 10, 'hotType', '类型', 'Select', 'Y', 'Code:hotType', '', 'NotNull', '', '', '', '', 141948765215000, 'zhouquan', '2014-12-25 14:07:32', 'zhouquan', '2014-12-25 14:08:28', NULL),
	(10, 10, 'num', '查询条数', 'Text', 'Y', '', '8', 'Int&&NotNull', '', '', '', '', 141948770506200, 'zhouquan', '2014-12-25 14:08:25', NULL, NULL, NULL),
	(15, 16, 'tDate', '时间', 'DateTime', 'Y', '', '', 'Time', '', '', '', '', 141949728534500, 'zhangmengjia', '2014-12-25 16:48:05', 'zhangmengjia', '2014-12-29 15:20:14', NULL),
	(16, 19, 'velModelId', '车型ID', 'Select', 'Y', 'Code:test', '', 'NotNull&&Int', '', '', '', '', 141949794962100, 'zhangmengjia', '2014-12-25 16:59:09', 'zhangmengjia', '2014-12-26 11:24:06', 'N'),
	(18, 21, 'seriesId', '车系ID', 'Text', 'Y', '', '', 'NotNull&&Int', '', '', '', '', 141950098454000, 'zhangmengjia', '2014-12-25 17:49:44', NULL, NULL, NULL),
	(19, 21, 'saleStatusList', '销售状态列表', 'Text', 'Y', '', '', 'NotNull&&Int', '', '', '', '', 141950103886200, 'zhangmengjia', '2014-12-25 17:50:38', NULL, NULL, 'Y'),
	(20, 23, 'velColorId', '车系颜色ID', 'Text', 'Y', '', '', 'NotNull&&CnTel', '', '', '', '', 141950197995400, 'zhangmengjia', '2014-12-25 18:06:19', 'zhangmengjia', '2014-12-26 18:51:21', 'N'),
	(23, 24, 'userId', '用户ID', 'Text', 'N', '', '100', 'NotNull&&Int', '', '', '', '', 141950231375900, 'admin', '2014-12-25 18:11:53', NULL, NULL, 'N'),
	(24, 25, 'brandId', '品牌ID', 'Radio', 'N', 'Input:3\n2\n11\n3\n6\n5', '', 'NotNull', '', '', '', '', 141950438641000, 'zhangmengjia', '2014-12-25 18:46:26', 'zhangmengjia', '2014-12-26 13:25:03', 'N'),
	(28, 31, 'brandId', '品牌Id', 'Select', 'Y', 'Code:brandId', '', 'NotNull', '', '', '', '', 142597616018700, 'sunjiangtao', '2015-03-10 16:29:20', 'sunjiangtao', '2015-03-20 11:54:46', NULL),
	(30, 32, 'seriesId', '车系id', 'Text', 'Y', '', '', 'NotNull', '', '', '', '', 142605768707400, 'sunjiangtao', '2015-03-11 15:08:07', NULL, NULL, NULL),
	(31, 34, 'size', '门店数量', 'Text', 'Y', '', '6', 'NotNull', '', '', '', '', 142621786265300, 'sunjiangtao', '2015-03-13 11:37:42', 'sunjiangtao', '2015-03-13 11:43:52', NULL),
	(33, 36, 'size', '数量', 'Text', 'Y', '', '8', 'NotNull', '', '', '', '', 142649069326800, 'sunjiangtao', '2015-03-16 15:24:53', 'liuxue', '2015-03-22 13:35:53', NULL),
	(35, 37, 'seriesId', '车系ID', 'Text', 'N', '', '', '', '', '', '', '', 142664889436000, 'zhouquan', '2015-03-18 11:21:34', NULL, NULL, NULL),
	(36, 37, 'posId', 'posId', 'Text', 'N', '', 'ns085', '', '', '', '', '', 142664891835800, 'zhouquan', '2015-03-18 11:21:58', NULL, NULL, NULL),
	(37, 38, 'brandId', 'brandId', 'Text', 'N', '', '', '', '', '', '', '', 142666343634000, 'zhouquan', '2015-03-18 15:23:56', NULL, NULL, NULL),
	(38, 39, 'brandId', '品牌Id', 'Text', 'Y', '', '', 'NotNull', '', '', '', '', 142700420780800, 'sunjiangtao', '2015-03-22 14:03:27', NULL, NULL, NULL),
	(39, 40, 'pageSize', '数据大小', 'Text', 'Y', '', '', 'NotNull', '', '', '', '', 142838715628800, 'zhangfujin', '2015-04-07 14:12:36', NULL, NULL, NULL),
	(40, 42, 'provinceId', '省份id', 'Text', 'Y', '', '', 'NotNull', '', '', '', '', 142839346219200, 'zhangfujin', '2015-04-07 15:57:42', NULL, NULL, NULL),
	(41, 44, 'territoryId', '城市ID', 'Select', 'N', 'Method:CXDataModel.getCitysMap', '', '', '', '', '', '', 143167610141700, 'zhouquan', '2015-05-15 15:48:21', NULL, NULL, NULL),
	(42, 44, 'channelCode', '频道ID', 'Text', 'N', '', '', '', '', '', '', '', 143167612157600, 'zhouquan', '2015-05-15 15:48:41', NULL, NULL, NULL),
	(43, 44, 'plateCode', '版块ID', 'Text', 'N', '', '', '', '', '', '', '', 143167613975000, 'zhouquan', '2015-05-15 15:48:59', NULL, NULL, NULL);
/*!40000 ALTER TABLE `cxdatamodelsearchcolumn` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
