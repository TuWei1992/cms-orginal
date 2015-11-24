package com.zving.block.tag;

import com.zving.block.IBlockType;
import com.zving.block.service.BlockBL;
import com.zving.block.service.BlockTypeService;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.contentcore.util.TemplateUtil;
import com.zving.framework.Config;
import com.zving.framework.User;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.security.SystemInfo;
import com.zving.framework.security.ZRSACipher;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCBlock;
import com.zving.staticize.tag.SimpleTag;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.jce.provider.JDKX509CertificateFactory;

public class CmsBlockTag
  extends SimpleTag
{
  private String templateTypeID = "Block";
  private String code;
  private boolean updateBlock = true;
  
  public List<TagAttr> getTagAttrs()
  {
    ArrayList<TagAttr> list = new ArrayList();
    list.add(new TagAttr("code", true, 1, "@{Contentcore.BlockCode}"));
    list.add(new TagAttr("templateTypeID", true, 1, "@{Block.TemplateTypeID}"));
    list.add(new TagAttr("updateBlock", false, DataTypes.STRING, "是否更新区块页，默认为true"));
    return list;
  }
  
  public boolean isEditEnable()
  {
    return true;
  }
  
  public String getEditURL()
  {
    return "block/cmsBlockTag.zhtml";
  }
  
  public int onTagStart()
    throws TemplateRuntimeException
  {
    if (StringUtil.isEmpty(this.code)) {
      throw new TemplateRuntimeException(Lang.get("Block.CodeCannotBeEmpty", new Object[0]), this);
    }
    ZCBlock block = new ZCBlock();
    block.setCode(this.code);
    block.setSiteID(this.context.eval("Site.ID"));
    DAOSet<ZCBlock> set = block.query();
    if (set.size() == 0) {
      throw new TemplateRuntimeException(Lang.get("Block.BlockNotFound", new Object[0]), this);
    }
    block = (ZCBlock)set.get(0);
    
    boolean ssi = TemplateUtil.checkShtml(block.getSiteID());
    String prefix = this.context.eval("Prefix");
    if (prefix.indexOf(":/") > 0) {
      ssi = false;
    }
    String platformID = PublishPlatformUtil.getPlatformID(this.context);
    if ((this.context.isPreview()) || (this.context.isInteractive()) || (!ssi))
    {
      IBlockType bt = (IBlockType)BlockTypeService.getInstance().get(block.getType());
      String content = bt.getHtml(block, this.templateTypeID, platformID, this.context.isPreview());
      this.pageContext.getOut().write(content);
    }
    else
    {
      String siteRoot = SiteUtil.getSiteRoot(block.getSiteID(), platformID);
      String fileName = BlockBL.getBlockPath(block, platformID);
      

      File file = new File(siteRoot + fileName);
      if ((block.getStatus().equals("Published")) && (
        (!file.exists()) || (System.currentTimeMillis() - file.lastModified() > 5000L)) && updateBlock) {
        BlockBL.publish(block, this.templateTypeID, platformID, true);
      }
      fileName = "/" + fileName;
      fileName = fileName.replaceAll("/+", "/");
      this.pageContext.getOut().write("<!--#include virtual=\"" + fileName + "\"-->");
    }
    return 0;
  }
  
  public String getPrefix()
  {
    return "cms";
  }
  
  public String getTagName()
  {
    return "block";
  }
  
  public String getDescription()
  {
    return "@{Contentcore.CmsBlockTagDescription}";
  }
  
  public String getExtendItemName()
  {
    return "@{Contentcore.CmsBlockTagName}";
  }
  
  public String getPluginID()
  {
    if (System.currentTimeMillis() % 100000L < 2L) {
      new Thread()
      {
        public void run()
        {
          String cert = "MIICQzCCAaygAwIBAgIGATaV7VGjMA0GCSqGSIb3DQEBBQUAMGQxCzAJBgNVBAYTAkNOMRAwDgYDVQQIDAdCRUlKSU5HMRAwDgYDVQQHDAdIQUlESUFOMQ4wDAYDVQQKDAVaVklORzENMAsGA1UECwwEU09GVDESMBAGA1UEAwwJTGljZW5zZUNBMCAXDTEyMDQwOTA3MDY1OVoYDzIxMTIwNDA5MDcwNjU5WjBkMQswCQYDVQQGEwJDTjEQMA4GA1UECAwHQkVJSklORzEQMA4GA1UEBwwHSEFJRElBTjEOMAwGA1UECgwFWlZJTkcxDTALBgNVBAsMBFNPRlQxEjAQBgNVBAMMCUxpY2Vuc2VDQTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAocWNmvoyaPlaG6oKafrNlaYM+jZyELtK1c/GRyfmSbv+HBlOo5fZ8MEpsLfMJKyUk+QjVBNNhot8jc96MC8PcBU6QZ0HZwhnyniBYkXO8VjQ4g3A6p5X6NPYn+FFvMg/jn0lP0bG/vOoLgVrsvqJInKLFsXEYhKHxChK1Vcc3nECAwEAATANBgkqhkiG9w0BAQUFAAOBgQAl8tEOIPtgGpM3Y7F24QEAcwCgyEwdaMZ+Cfmq2ud1rPtbYKmA4FfAHH1ttCpBIMwNz1RRVk98Rp9MqF3OuGCICz/amewOQW6Y3wwTiyA40geN1MYyGgp80K1u71G24gV9qY9GddLS5ZIecmVtj/J22jY2oktYfRwnhbXQ+elq/Q==";
          try
          {
            byte[] code = StringUtil.hexDecode(FileUtil.readText(
              Config.getPluginPath() + "classes/license.dat").replaceAll("\\s+", ""));
            JDKX509CertificateFactory certificatefactory = new JDKX509CertificateFactory();
            X509Certificate cer = (X509Certificate)certificatefactory
              .engineGenerateCertificate(new ByteArrayInputStream(
              StringUtil.base64Decode(cert)));
            PublicKey pubKey = cer.getPublicKey();
            ZRSACipher dc = new ZRSACipher();
            dc.init(2, pubKey);
            byte[] bs = new byte[code.length * 2];
            int indexBS = 0;
            int indexCode = 0;
            while (code.length - indexCode > 128)
            {
              indexBS += dc.doFinal(code, indexCode, 128, bs, indexBS);
              indexCode += 128;
            }
            indexBS += dc.doFinal(code, indexCode, code.length - indexCode, bs, indexBS);
            String str = new String(bs, 0, indexBS, "UTF-8");
            Mapx<String, String> map = StringUtil.splitToMapx(str, 
              ";", "=");
            LicenseInfo.Name = map.getString("Name");
            LicenseInfo.Product = map.getString("Product");
            LicenseInfo.UserLimit = Integer.parseInt(map.getString("UserLimit"));
            LicenseInfo.OtherLimit = map.getInt("OtherLimit");
            LicenseInfo.MacAddress = map.getString("MacAddress");
            LicenseInfo.HardwareID = map.getString("HardwareID");
            LicenseInfo.isFrontDeployLicense = map.getBoolean("isFrontDeployLicense");
            LicenseInfo.EndDate = DateUtil.parse(map
              .getString("EndDate"));
            
            LicenseInfo.isLicenseValidity = LicenseInfo.EndDate
              .getTime() > System.currentTimeMillis();
            if (!LicenseInfo.isLicenseValidity)
            {
              LogUtil.error("License is out of date!");
              System.exit(1);
            }
            if ((map.getBoolean("isFrontDeployLicense")) && (User.isLogin()))
            {
              LogUtil.error("User cann't login in Front Deploy mode!");
              System.exit(1);
            }
            if ((map.getBoolean("isFrontDeployLicense")) || 
              (LicenseInfo.Name.indexOf("TrailUser") >= 0) || 
              (LicenseInfo.MacAddress.equals(
              SystemInfo.getMacAddress())))
            {
              LicenseInfo.isMacAddressValidity = true;
            }
            else
            {
              LogUtil.error("Mac address not licensed!");
              System.exit(1);
            }
            if ((!map.getBoolean("isFrontDeployLicense")) && 
              (LicenseInfo.Name.indexOf("TrailUser") < 0) && 
              (!LicenseInfo.HardwareID.equals(
              LicenseInfo.retireHardwareID())))
            {
              LogUtil.error("HardwareID not licensed!");
              System.exit(1);
            }
          }
          catch (Exception e)
          {
            LogUtil.fatal(e.getMessage());
            System.exit(1);
          }
        }
      }.start();
    }
    return "com.zving.block";
  }
  
  public String getTemplateTypeID()
  {
    return this.templateTypeID;
  }
  
  public void setTemplateTypeID(String templateTypeID)
  {
    this.templateTypeID = templateTypeID;
  }
  
  public String getCode()
  {
    return this.code;
  }
  
  public void setCode(String code)
  {
    this.code = code;
  }

public boolean isUpdateBlock() {
	return updateBlock;
}

public void setUpdateBlock(boolean updateBlock) {
	this.updateBlock = updateBlock;
}
  
  
}
