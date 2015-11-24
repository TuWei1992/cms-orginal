package com.zving.cxdata.item;

import java.util.List;
import java.util.Map;

import com.zving.cxdata.ICXDataCondition;
import com.zving.cxdata.bl.CXDataModelBL;
import com.zving.framework.data.DataTable;

/**
 * 
 * @author v_zhouquan
 * 城市条件扩展项，以城市代码作为查询参数查询数据，参数名称为cityId，参数项通过城市数据接口获取
 */
public class CityCondition extends ICXDataCondition {
	public static final String ID = "CityCondition";
	public static final String NAME = "城市";
	@Override
	public String getExtendItemID() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public String getExtendItemName() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public String getArgName() {
		// TODO Auto-generated method stub
		return "cityId";
	}

	@Override
	public DataTable getOptions() {
		// TODO Auto-generated method stub
		List<Map> citys = CXDataModelBL.searchDataList(CXDataModelBL.getModelByCode("citys"), null);
		DataTable dt = new DataTable();
		dt.insertColumn("code");
		dt.insertColumn("name");
		if (citys != null && citys.size() > 0) {
			for (Map city : citys) {
				dt.insertRow(new Object[]{city.get("code"), city.get("name")});
			}
		}
		return dt;
	}
	
}
