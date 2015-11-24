package com.zving.cxdata.bl;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.zving.advertise.ui.PublishIndexTask;
import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.IPublishPlatform;
import com.zving.contentcore.bl.PublishBL;
import com.zving.contentcore.bl.PublishLogBL;
import com.zving.contentcore.bl.PublishPlatformBL;
import com.zving.contentcore.property.impl.PublishPlatform;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.contentcore.task.PublishTask;
import com.zving.contentcore.task.PublishTheadPool;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.cxdata.ICXDataCondition;
import com.zving.cxdata.property.CXDataPublishConditionPlatform;
import com.zving.cxdata.property.CXDataPublishConditionProp;
import com.zving.cxdata.service.CXDataConditionService;
import com.zving.deploy.bl.DeployBL;
import com.zving.deploy.util.DeployManager;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCSite;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;

import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;

public class CXPublishBL {
	public static void publishSiteIndex(final ZCSite site, LongTimeTask lts) { 
	    String ptStr = PublishPlatform.getValue(site.getConfigProps());
	    String[] ptArr = StringUtil.splitEx(ptStr, ",");
	    for (String ptID : ptArr) {
	      final IPublishPlatform pt = (IPublishPlatform)PublishPlatformService.getInstance().get(ptID);
	      if (pt != null) {
	        PublishTask task = new PublishTask("Site", site.getID() + "," + ptID) {
	          public void execute() {
	            pt.publishSiteIndex(site);
	          }
	        };
	        task.setDescription(site.getName());
	        PublishTheadPool.getInstance().addTask(task);
	        ExtendManager.invoke("com.zving.cxdata.point.AfterSiteIndexPublish", new Object[] { site, ptID, lts });
	      }
	    }
	  }

	/*
	* 通过主动通知模式生产首页文件
	* */
	public static void publishForAdviseSiteIndex(final ZCSite site) {
		String ptStr = PublishPlatform.getValue(site.getConfigProps());
		String[] ptArr = StringUtil.splitEx(ptStr, ",");
		for (String ptID : ptArr) {
			final IPublishPlatform pt = (IPublishPlatform)PublishPlatformService.getInstance().get(ptID);
			if (pt != null) {
				publishSiteAllCity(site,pt);
			}
		}
	}
	
	public static void publishConditionSiteIndex(final ZCSite site, String staticFileName, Map cxParam) {
				if (StringUtil.isNotEmpty(staticFileName)) {
				    String ptStr = PublishPlatform.getValue(site.getConfigProps());
				    String[] ptArr = StringUtil.splitEx(ptStr, ",");
				    for (String ptID : ptArr) {
				      final IPublishPlatform pt = (IPublishPlatform)PublishPlatformService.getInstance().get(ptID);
				      if (pt != null) {
				    	 /*
				        PublishTask task = new PublishTask("Site", site.getID() + "," + ptID) {
				          public void execute() {
				            pt.publishSite(site);
				          }
				        };
				       
				        task.setDescription(site.getName());
				        PublishTheadPool.getInstance().addTask(task);
				         */
				    	  publishSite(site, pt.getExtendItemID(), staticFileName, cxParam);
				      }
				    }
				}
	  }
	
	public static void publishAllConditionSiteIndex(final ZCSite site) {
		String publishCondition = CXDataPublishConditionProp.getValue(site.getConfigProps());
		if (StringUtil.isNotEmpty(publishCondition)) {
			ICXDataCondition con = CXDataConditionService.getInstance().get(publishCondition);
			if (con != null) {
				DataTable conditions = con.getOptions();
				for (DataRow dr : conditions) {
					Map cxParam = new HashMap();
					cxParam.put(con.getArgName(), dr.get(0));
					publishConditionSiteIndex(site, dr.getString(0), cxParam);
				}
			}
		}
	}
	
	public static void publishSite(ZCSite site, String platformID, String staticFileName, Map cxParam) {
	    String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(site.getID(), platformID);
	    String fileName = siteRoot + staticFileName + PublishPlatformUtil.getSuffix(site.getID(), platformID);
	    String indexTemplate = PublishPlatformUtil.getSiteIndexTemplate(site.getID(), platformID);
	    if (ObjectUtil.empty(indexTemplate)) {
	      PublishBL.message(Lang.get("Contentcore.NoSetSiteIndexTemplate") + Lang.get("Contentcore.SiteName") + ":" + site.getName(), platformID);
	      return;
	    }
	    ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType("SiteIndex");
	    indexTemplate = siteRoot + indexTemplate;
	    if (!FileUtil.exists(indexTemplate)) {
	    	PublishBL.message(Lang.get("Contentcore.TemplateNotFount") + site.getName() + ":" + indexTemplate, platformID);
	      return;
	    }
	    File f = new File(siteRoot);
	    if (!f.exists()) {
	      f.mkdirs();
	    }
	    try {
	      AbstractExecuteContext context = tt.getContext(site.getID(), platformID, false);
	      context.addDataVariable("cxParam", cxParam);
	      context = PublishPlatformBL.dealPlatformProperty(context, 0L, site.getID(), platformID);
	      context.addDataVariable("PlatformID", platformID);
	      //23184-24115
	      //TemplateContextUtil.addPublishVariables(context);
	      
	      TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(indexTemplate);
	      tpl.setContext(context);
	      
	      TemplateWriter writer = new TemplateWriter();
	      tpl.setWriter(writer);
	      
	      tpl.execute();
	      FileUtil.writeText(fileName, writer.getResult());
	      LogUtil.info("首页发布完成：" + cxParam);
	    }
	    catch (Exception e) {
	      //e.printStackTrace();
	    	String errorMessage  = "Publish index error：" + cxParam + "\n<br />" + e.getMessage();
	    	//LogUtil.error(errorMessage);
	    	System.err.println(errorMessage);
	    	PublishLogBL.addSiteIndexPublishLog(site, platformID, e.getMessage());
	    	Errorx.addError(errorMessage);
	    }
	}

	private static void publishSiteAllCity(final ZCSite site, final IPublishPlatform platform) {

		final long startTime=System.currentTimeMillis();

		try {
			final String publishCondition = CXDataPublishConditionProp.getValue(site.getConfigProps());
			final String publishConditionPlatform = CXDataPublishConditionPlatform.getValue(site.getConfigProps());

			if (StringUtil.isNotEmpty(publishCondition) && StringUtil.isNotEmpty(publishConditionPlatform)
					&& publishConditionPlatform.indexOf(platform.getExtendItemID()) != -1) {
				final ICXDataCondition con = CXDataConditionService.getInstance().get(publishCondition);
				if (con != null) {
					final DataTable conditions = con.getOptions();
					int total = conditions.getRowCount();

					final String platformID=platform.getExtendItemID();
					//站点根目录 用于读取生成的文件的所在的目录
					final String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(site.getID(), platformID);

					//线程中 的计数器 线程中执行 每次减一，当值为0 的时候，可唤醒 因为endCycle.await 等待的线程。
					final CountDownLatch endCycle = new CountDownLatch(total);

					//创建任务列表
					final List<Integer> taskList=new LinkedList<Integer>();
					for (int i = 0; i < total; i++) {
						taskList.add(i);
					}

					//创建消费者列表 15个消费者
					for (int i = 0; i < 15; i++) {
						//开启线程 生成首页shtml
						PublishIndexTask publishIndexTask=new PublishIndexTask(taskList,i){
							public void execute(){
								LogUtil.debug("publishIndexTask serial num:"+this.getSerialNum()+" have started !!!");
								while (true){
									if(this.isTimeOut(180000)){//线程运行超过3分钟  认为超时。强制退出。
										break;
									}else{
										try{
											//获取task 的id
											Integer index=this.getTaskId();
											LogUtil.debug("publishIndexTask serial num:"+this.getSerialNum()+" have cycling index:"+index);

											if(index==null){//获取不到新的task 跳出循环
												break;
											}

											final Map cxParam = new HashMap();
											cxParam.put(con.getArgName(), conditions.get(index, 0));
											cxParam.put(con.getArgName()+"_Text", conditions.get(index, 1));

											final String cityId=conditions.getString(index, 0);

											//根据城市生成首页文件
											CXPublishBL.publishSite(site, platformID,cityId, cxParam);

											//存储要分发的内容到队列中。
											DeployBL.tryToDeploy(siteRoot + cityId + PublishPlatformUtil.getSuffix(site.getID(), platformID));

											LogUtil.debug("publishIndexTask serial num:"+this.getSerialNum()+" publish shtml :"+cxParam+" successful !!!");
										}catch (Exception e){
											Errorx.addError(e.getMessage());
										}finally {
											endCycle.countDown();
										}
									}
								}
								LogUtil.debug("publishIndexTask serial num:"+this.getSerialNum()+" have finished !!!");
							}
						};

						//开启线程
						publishIndexTask.start();
					}

					//所以生成shtml页面的线程运行完毕 之后，执行分发文件到各个服务器
					Thread deployThread=new Thread(){
						@Override
						public void run() {
							try {
								LogUtil.debug("DeployManager Wait To Distribute File !!!");
								endCycle.await(5, TimeUnit.MINUTES);//最长等待5分钟，否则直接执行
								//将分发队列里边的文件 分发到各个服务器
								DeployManager.getInstance().deploy();
								long endTime=System.currentTimeMillis();
								LogUtil.info("Publish Index and Distribute Finish spend time:"+(endTime-startTime)+" ms");

								LogUtil.debug("DeployManager Distribute File Finish !!!");
							} catch (InterruptedException e) {
								Errorx.addError(e.getMessage());
							}

						}
					};
					deployThread.start();

				}
			}
		} catch (Exception e) {

			Errorx.addError(e.getMessage());
		}
	}
}
