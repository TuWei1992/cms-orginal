package com.zving.cxdata.tag;

import java.util.List;

import com.meidusa.fastjson.JSON;
import com.zving.contentcore.service.TagCatalogConditionProviderService;
import com.zving.contentcore.tag.AbstractContentListTag;
import com.zving.cxdata.CXDataContentType;
import com.zving.cxdata.CXDataPlugin;
import com.zving.cxdata.bl.CXDataModelBL;
import com.zving.cxdata.property.CXDataModelProp;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.CXDataModel;
/**
 * 
 * @author v_zhouquan
 * 车享数据获取标签
 */
public class CXDataTag extends AbstractContentListTag {
	private String code;
	private String api;
	@Override
	public void prepareData() {
	    this.loadContent = "true".equals(this.attributes.getString("loadcontent"));
	    this.loadExtend = "true".equals(this.attributes.getString("loadextend"));
	    this.hasLogo = "true".equals(this.attributes.getString("hasLogo"));
	    
	    this.item = getItemName();
	    if (this.siteID == 0L) {
	      this.siteID = this.context.evalLong("Site.ID");
	    }
	    DataTable dt = new DataTable();
		String param = "{}";
		if(StringUtil.isNotEmpty(this.condition)) {
			condition = condition.trim();
			if (condition.startsWith("{")){
				param = condition;
			} else {
				param =  JSON.toJSONString(StringUtil.splitToMapx(this.condition, ",", "="));
			}
		}
	    if  (StringUtil.isNotEmpty(api)) {
	    	dt = CXDataModelBL.searchData(api, param);
	    }  else {
		    CXDataModel dm = null;
		    if (StringUtil.isNotEmpty(code)) {
		    	dm = CXDataModelBL.getModelByCode(code);
		    } else {
			    this.catalogID = StringUtil.join(TagCatalogConditionProviderService.getCatalogID(this.context, this.catalogID, this.catalogAlias, this.catalog));
			    if (StringUtil.isNotEmpty(catalogID)) {
			    	String modelID = CXDataModelProp.getValue(Long.parseLong(catalogID));
					if (StringUtil.isNotEmpty(modelID)) {
						CXDataModel dmi = new CXDataModel();
						dmi.setID(Long.parseLong(modelID));
						if (dmi.fill()) { 
							dm = dmi;
						}
					}
			    }
		    }
		    if (dm == null) {
		    	throw new RuntimeException("未配置venus接口信息，code=" + code +", catalogAlias="+catalogAlias);
		    }
		    dt = CXDataModelBL.searchData(dm, CXDataModelBL.modifyParam(dm,param));
	    }
	    
	    this.data = dt;
	    this.count = dt.getRowCount();
	    this.pageTotal = dt.getRowCount();
	}
	
	@Override
	public void dealContent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dealExtend() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dealLink(DataRow dr) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return CXDataContentType.ID;
	}

	@Override
	public String getItemName() {
		// TODO Auto-generated method stub
		return "CXData";
	}

	@Override
	public void invokeTagSqlExtend(AbstractExecuteContext arg0, Q arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Q loadContentQueryBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "车享数据标签";
	}

	@Override
	public String getPluginID() {
		// TODO Auto-generated method stub
		return CXDataPlugin.ID;
	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return "cms";
	}

	@Override
	public String getTagName() {
		// TODO Auto-generated method stub
		return "cxdata";
	}
	public static void main(String[] args) {
		String s = "name='ZQ', age=21";
		System.out.println(StringUtil.splitToMapx(s, ",", "="));
	}

	@Override
	public String getExtendItemName() {
		// TODO Auto-generated method stub
		return "车享数据标签";
	}
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> attrs = super.getTagAttrs();
		attrs.add(new TagAttr("code", false, DataTypes.STRING, "车享数据代码"));
		attrs.add(new TagAttr("api", false, DataTypes.STRING, "venus api"));
		return attrs;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}
	
	
}
