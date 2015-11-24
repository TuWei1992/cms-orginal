package com.zving.cxdata.ui;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zving.cxdata.bl.CXDataModelBL;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.IMetadataColumnControlType;
import com.zving.platform.meta.MetadataColumnControlTypeService;
import com.zving.platform.util.NoUtil;
import com.zving.platform.util.OrderUtil;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.CXDataModel;
import com.zving.schema.CXDataModelSearchColumn;
import com.zving.schema.ZDMetaColumn;
import com.zving.schema.ZDMetaColumnGroup;
import com.zving.schema.ZDMetaModel;
import com.zving.schema.ZDMetaValue;
import com.zving.schema.ZDModelTemplate;

@Alias("CXDataModel")
public class CXDataModelUI extends UIFacade {
	
	@Priv
	public void bindGrid(DataGridAction dga) {
		String search = $V("SearchContent");
		Q q = new Q("select * from CXDataModel");
		if (StringUtil.isNotEmpty(search)) {
			q.where();
			q.like("Code", search).or();
			q.like("Name", search).or();
			q.like("APIName", search);
		}
		q.orderby("OrderFlag desc");
		dga.setTotal(q);
		dga.bindData(q.fetch(dga.getPageSize(), dga.getPageIndex()));
		
	}
	
	@Priv
	public void add() {
		Transaction trans = new Transaction();
		if(CXDataModelBL.isNameCodeExists($V("Name"), $V("Code"), $L("ID"))) {
			fail("名称或代码已存在");
			return;
		}
		CXDataModel dm = new CXDataModel();
		dm.setID(NoUtil.getMaxID("CXDataModelID"));
		dm.setValue(Request);
		dm.setOrderFlag(OrderUtil.getDefaultOrder());
		dm.setAddUser(User.getUserName());
		dm.setAddTime(new Date());
		
		trans.insert(dm);
		if (trans.commit()) {
			success("车享数据模型新建成功");
		} else {
			fail("车享数据模型新建失败：" + trans.getExceptionMessage());
		}
	}
	
	@Priv
	public void save() {
		Transaction trans = new Transaction();
		Long id = $L("ID");
		if (ObjectUtil.empty(id)) {
			fail(Lang.get("Common.InvalidID"));
			return;
		}
		if(CXDataModelBL.isNameCodeExists($V("Name"), $V("Code"), $L("ID"))) {
			fail("名称或代码已存在");
			return;
		}
		CXDataModel dm = new CXDataModel();
		dm.setID($L("ID"));
		if (dm.fill()) {
			dm.setValue(Request);
			dm.setModifyUser(User.getUserName());
			dm.setModifyTime(new Date());
		} else {
			fail(Lang.get("Common.InvalidID"));
		}

		trans.update(dm);
		if (trans.commit()) {
			success("车享数据模型保存成功");
		} else {
			fail("车享数据模型保存失败：" + trans.getExceptionMessage());
		}
	}
	
	@Priv
	public void init() {
		if (ObjectUtil.notEmpty($L("ID"))) {
			CXDataModel dm = new CXDataModel();
			dm.setID($L("ID"));
			if (dm.fill()) {
				this.Response.putAll(dm.toMapx());
			}
		}
	}
	
	@Priv
	public void bindSearchColumnGrid(DataGridAction dga) {
		   DataTable dt = null;
		    long id = $L("ID");
		    if (id != 0L)
		    {
		      Q q= new Q("select * from CXDataModelSearchColumn").where("ModelID", id).orderby("OrderFlag asc");
		      dt = q.fetch();
		      dt.decodeColumn("MandatoryFlag", PlatformUtil.getCodeMap("YesOrNo"));
		      dga.bindData(dt);
		    }
	}
	
	@Priv
	public void initColumnDialog() {
		 List<IMetadataColumnControlType> list = MetadataColumnControlTypeService.getInstance().getAll();
		    String sdt = "var sdt = [];";
		    for (IMetadataColumnControlType mcc : list) {
		      if (ObjectUtil.notEmpty(mcc.getSaveDataType())) {
		        sdt = sdt + "\n" + "sdt[\"" + mcc.getExtendItemID() + "\"] =\"" + mcc.getSaveDataType() + "\";";
		      }
		    }
		    $S("sdt", sdt);
		    
		    long id = $L("ID");
		    if (id != 0L) {
		      CXDataModelSearchColumn msc = new CXDataModelSearchColumn();
		      msc.setID(id);
		      msc.fill();
		      this.Response.putAll(msc.toMapx());
		      if (StringUtil.isNotEmpty(msc.getListOptions())) {
		        this.Response.put("ListOptions", msc.getListOptions().replaceAll("\n", "<br>"));
		      }
		    } else { 
		      $S("MandatoryFlag", "N");
		    }
	}
	
	@Priv
	public void saveSearchColumn () {
		Transaction trans = Current.getTransaction();
	    long modelID = $L("ModelID");
	    if (NumberUtil.isNumber(StringUtil.subString($V("Code"), 1))) {
	      fail(Lang.get("Platform.Code.Error2", new Object[0]));
	      return;
	    }
	    if (CXDataModelBL.isColumnCodeExists($V("Code"), $L("ID"), modelID)) {
	      fail(Lang.get("Platform.CodeExists", new Object[0]));
	      return;
	    }
	    CXDataModelSearchColumn mdc = new CXDataModelSearchColumn();
	    
	    if (ObjectUtil.notEmpty($L("ID"))) {
	    	mdc.setID($L("ID"));
	    	mdc.fill();
	    	mdc.setValue(Request);
	    	mdc.setModifyUser(User.getUserName());
	    	mdc.setModifyTime(new Date());
	    	trans.update(mdc);
	    } else {
	    	mdc.setID(NoUtil.getMaxID("CXDataSearchColumnID"));
	     	mdc.setValue(Request);
	    	mdc.setOrderFlag(OrderUtil.getDefaultOrder());
	    	mdc.setAddUser(User.getUserName());
	    	mdc.setAddTime(new Date());
	    	trans.insert(mdc);
	    }

	    if (Current.getTransaction().commit()) {
	      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
	    } else {
	      fail(Lang.get("Common.ExecuteFailed", new Object[0]) + ":" + Current.getTransaction().getExceptionMessage());
	    }
	}
	
	@Priv
	public void deleteColumn() {
	 	String ids = $V("IDs");
	 	DAOSet<CXDataModelSearchColumn> set = new CXDataModelSearchColumn().query(new Q().where().in("ID", ids));
		Current.getTransaction().deleteAndBackup(set);
	    if (Current.getTransaction().commit()) {
	      success(Lang.get("Common.DeleteSuccess", new Object[0]));
	    }
	    else {
	      fail(Lang.get("Common.FailedSuccess", new Object[0]));
	    }
	}
	
	@Priv
	public void initCXDataSearch() {
		Long id = $L("ID");
		if (ObjectUtil.notEmpty(id)) {
			$S("Conditions", CXDataModelBL.parseModelFieldTag(null, id));
		}
	}
	
	@Priv
	public void bindDataGird(DataGridAction dga) {
		Long id = $L("ID");
		if (ObjectUtil.notEmpty(id)) {
			String conditions = $V("searchConditions");
			CXDataModel dm = new CXDataModel();
			dm.setID(id);
			dm.fill();
			try {
				if (!(StringUtil.isEmpty(conditions) && CXDataModelBL.hasSearchCondition(id)) || StringUtil.isNotEmpty($V("searchFlag"))) {
					dga.bindData(CXDataModelBL.searchDataStr(dm, CXDataModelBL.modifyParam(dm, conditions)));
				}
			} catch (Exception e) {
				fail("数据获取出错：" + e.getMessage());
			}
		}
	}
	
	@Priv
	public Mapx getCitysMap() {
		String code = "citys";
		Mapx m = new Mapx();
		List<Map> data = CXDataModelBL.searchDataList(CXDataModelBL.getModelByCode(code), null);
		if (!ObjectUtil.empty(data)) {
			for (Map d : data) {
				m.put(d.get("code"), d.get("name"));
			}
		}
		return m;
	}
	
	@Priv
	public DataTable getAllModels() {
		return new Q("select id,name from CXDataModel order by OrderFlag desc").fetch();
	}
	
	@Priv
	public void del() {
		String ids = $V("IDs");
	    Transaction trans = Current.getTransaction();
	    DAOSet<CXDataModel> set = new CXDataModel().query(new Q().where().in("ID", ids));
	    trans.deleteAndBackup(set);
	    
	    DAOSet<CXDataModelSearchColumn> values = new CXDataModelSearchColumn().query(new Q().where().in("ModelID", ids));
	    trans.deleteAndBackup(values);

	    if (trans.commit()) {
	       success(Lang.get("Common.DeleteSuccess", new Object[0]));
	    } else {
	       fail(Lang.get("Common.DeleteFail: " + trans.getExceptionMessage()));
	    }
	}
	
	@Priv
	public void sortColumn() {
	    long targetOrderFlag = $L("TargetOrderFlag");
	    long id = $L("ID");
	    if ((id == 0L) || (targetOrderFlag < 0L)) {
	      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
	      return;
	    }
	    CXDataModelSearchColumn schema = new CXDataModelSearchColumn();
	    schema.setID(id);
	    if (!schema.fill()) {
	      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
	      return;
	    }
	    Q wherePart = new Q().and().eq("ModelID", Long.valueOf(schema.getModelID()));
	    OrderUtil.updateOrder(new CXDataModelSearchColumn().table(), "OrderFlag", targetOrderFlag, wherePart, schema, Current.getTransaction());
	    if (Current.getTransaction().commit()) {
	      success(Lang.get("Common.ExecuteSuccess", new Object[0]));
	    } else {
	      fail(Lang.get("Common.ExecuteFailed", new Object[0]));
	    }
	  }
	public static void main(String[] args) {
		int[] a = {1,2};
		int[] b = {3,4};
		System.arraycopy(b, 0, a, a.length, b.length);
		System.out.println(Arrays.toString(a));
	}
}
