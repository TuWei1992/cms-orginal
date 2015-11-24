package com.zving.advertise.ui;

import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.bl.CXPublishBL;
import com.zving.cxdata.config.AdvertiseNoticeEnable;
import com.zving.deploy.bl.DeployBL;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.LogUtil;
import com.zving.schema.ZCSite;

/**
 * Created by duxinming on 2015/7/28.
 */
public class AdvertiseNoticeUI extends UIFacade {
    //表示无需登录即可访问的方法
    @Priv(login=false)
    @Alias(value = "advertise/notice", alone = true)
    public void notice(ZAction za){

        LogUtil.info("Receive the notify from advertise start");
        if (!AdvertiseNoticeEnable.enable()) {
        	LogUtil.info("广告推送：广告推送发布未开启");
        	return;
        }
        final String siteID =$V("SiteID");
        LogUtil.info("siteID "+siteID);
        final ZCSite zcSite=SiteUtil.getDAO(siteID);

        if(zcSite!=null){
            Thread t1= new Thread(){
                @Override
                public void run() {
                    CXPublishBL.publishForAdviseSiteIndex(zcSite);
                }
            };
            t1.start();
        }
        LogUtil.info("Receive the notify from advertise end ");

    }
}