package com.zving.cxdata.bl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.meidusa.fastjson.JSON;
import com.zving.adapter.VenusSocketConnect;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.MetaUtil;
import com.zving.schema.CXDataModel;
import com.zving.schema.CXDataModelSearchColumn;
import com.zving.schema.ZDMetaColumn;

public class CXDataModelBL {
	public static final Pattern FieldCodePattern = Pattern.compile("<model:field .*?code=\"(.*?)\">", 34);
	public static final String DefaultTemplate = "<table id=\"table\"  border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bordercolor=\"#eeeeee\"><model:field><tr><td width=\"80\">@{Field.Name}：</td><td>@{Field.ControlHtml}</td></tr></model:field></table>";
	public static boolean isNameCodeExists(String name, String code, Long modelID) {
		 Q q = new Q().select("count(1)").from("CXDataModel").where().braceLeft().eq("Name", name).or().eq("Code", code)
	      .braceRight();
	    if (modelID != 0L) {
	      q.and().ne("ID", Long.valueOf(modelID));
	    }
	    return q.executeInt() > 0;
	}
	
	public static boolean isColumnCodeExists(String code, long id, long modelID) {
		Q q = new Q().select("count(1)").from("CXDataModelSearchColumn").where("Code", code).and().eq("ModelID", modelID);
		if (ObjectUtil.notEmpty(id)) {
			q.and().ne("ID", id);
		}
		return q.executeInt() > 0;
	}
	
	public static String parseModelFieldTag(String tmpl, Long modeID) {
	    StringBuilder sb = new StringBuilder();
	    if (StringUtil.isEmpty(tmpl)) {
	    	tmpl = DefaultTemplate;
	    }
	    while (tmpl.indexOf("<model:field") > -1)
	    {
	      int beginIndex = tmpl.indexOf("<model:field");
	      int endIndex = tmpl.indexOf("</model:field>", beginIndex);
	      sb.append(tmpl.substring(0, beginIndex));
	      
	      String tagStr = tmpl.substring(beginIndex, endIndex + 14);
	      
	      Matcher matcher = FieldCodePattern.matcher(tagStr);
	      String code = matcher.find() ? matcher.group(1) : "";
	      Q qbTemp = new Q();
	      qbTemp.where("ModelID", modeID);
	      if (StringUtil.isNotEmpty(code)) {
	        qbTemp.append(" and Code=?", new Object[] { code });
	      }
	      qbTemp.append(" order by OrderFlag asc ");
	      DAOSet<CXDataModelSearchColumn> cols = new CXDataModelSearchColumn().query(qbTemp);
	      String body = tagStr.substring(tagStr.indexOf(">") + 1, tagStr.indexOf("</model:field>"));
	      for (CXDataModelSearchColumn col : cols){
	    	ZDMetaColumn mc = new ZDMetaColumn();
	    	mc.setValue(col.toMapx());
	    	mc.setID(null);
	    	mc.setDataType("MediumText");
	        String newBody = body;
	        Mapx<String, Object> colMap = col.toMapx();
	        colMap.put("ControlHtml", MetaUtil.getControlHTML(mc));
	        for (String key : colMap.keySet()) {
	          newBody = StringUtil.replaceEx(newBody, "@{Field." + key + "}", colMap.getString(key));
	        }
	        sb.append(newBody);
	      }
	      tmpl = tmpl.substring(endIndex + 14);
	    }
	    sb.append(tmpl);
	    return sb.toString();
	  }
	public static List searchDataList(CXDataModel dm, String params) {
		return searchDataList(dm.getAPIName(), params);
	}
	  public static List searchDataList(String apiName, String params) {
		  String result = null;
		  if (StringUtil.isEmpty(params)) {
			 result = VenusSocketConnect.getInstance().execute(apiName);
		  } else {
			  result = VenusSocketConnect.getInstance().execute(apiName, params);
		  }
	      if (StringUtil.isEmpty(result)) {
	          return new ArrayList<Object>();
	      }
	      Object ro = JSON.parse(result);
	      List data = new ArrayList();
	      if (List.class.isAssignableFrom(ro.getClass())) {
	    	  data = (List)ro;
	      } else {
	    	  data.add(ro);
	      }
	      return data;
	  }
	  
	  public static DataTable searchData(CXDataModel dm, String params) {
		  List<Map> list = searchDataList(dm, params);
		  DataTable dt = new DataTable();
		  dt.insertColumn("data");
		  if (list != null && list.size() > 0) {
			  for (int i = 0; i < list.size(); i++) {
				  dt.insertRow(list.get(i));
			  }
		  } 
		  return dt;
	  }
	  
	  public static DataTable searchData(String apiName, String params) {
		  List<Map> list = searchDataList(apiName, params);
		  DataTable dt = new DataTable();
		  dt.insertColumn("data");
		  if (list != null && list.size() > 0) {
			  for (int i = 0; i < list.size(); i++) {
				  dt.insertRow(list.get(i));
			  }
		  } 
		  return dt;
	  }
	  
	  public static DataTable searchDataStr(CXDataModel dm, String params) {
		  DataTable dt = searchData(dm, params);
		  for (DataRow dr : dt) {
			  dr.set("data", JSON.toJSONString(dr.get("data")));
		  }
		  return dt;
	  }
	  
	  public static CXDataModel getModelByCode(String code) {
		  CXDataModel dm = new CXDataModel();
		  dm.setCode(code);
		  DAOSet<CXDataModel> dmSet = dm.fetch();
		  if (dmSet != null && dmSet.size() > 0) {
			  return dmSet.get(0);
		  }
		  return	null;
	  }
	  
	  public static boolean hasSearchCondition(Long id) {
		  Q q = new Q("select count(1) from CXDataModelSearchColumn").where("ModelID", id);
		  return q.executeInt() > 0;
	  }
	  
	  public static String modifyParam(CXDataModel dm, String param) {
		  if (StringUtil.isEmpty(param)) {
			  return null;
		  }
		  param = param.replaceAll("MetaValue_", "");
		  Map column = new Q("select Code, ArrayFlag from CXDataModelSearchColumn").where("ModelID", dm.getID()).executeDataTable().toMapx(0, 1);
		  Map pm = JSON.parseObject(param, Map.class);
		  for (Object key : pm.keySet()) {
			  String value = (String)pm.get(key);
			  if (column.containsKey(key) && ObjectUtil.notEmpty(value)) {
				  value = value.trim();
				  String columnType = (String)column.get(key);
				  //处理集合类型
				  if (StringUtil.isNotEmpty(columnType) && columnType.equals("Y")) {
					  pm.put(key, value.split(","));
				  }
				  
				  //处理JSON串
				  if (StringUtil.isNotEmpty(columnType) && columnType.equals("J")) {
					  pm.put(key, JSON.parse(value));
				  }
			  }
		  }
		  return JSON.toJSONString(pm);
	  }
	  public static void main(String[] args) {
		  /*
		Map m = new HashMap();
		m.put("codes", Arrays.asList("1231", "123123"));
		m.put("a", 12312);
		System.out.println(JSON.toJSONString(m));
		*/
		  String a = "{'name':'zq','age':100}";
		  String b = "[2,4,'a','b']";
		  String c = "123";
		  //String d = "4,5,6";
		  Object ao = JSON.parse(a);
		  Object bo = JSON.parse(b);
		  Object co = JSON.parse(c);
		  //Object cd = JSON.parse(d);
		  
		  System.out.println(ao.getClass() + ": " + ao);
		  System.out.println(bo.getClass() + ": " + bo);
		  System.out.println(co.getClass() + ": " + co);
		  //System.out.println(cd);
	}
}
