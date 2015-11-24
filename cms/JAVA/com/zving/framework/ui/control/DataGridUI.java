package com.zving.framework.ui.control;

import java.io.OutputStream;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.Verify;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.security.VerifyCheck;
import com.zving.framework.ui.control.grid.DataGridBodyManager;
import com.zving.framework.ui.html.HtmlParser;
import com.zving.framework.ui.html.HtmlTR;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringUtil;

/**
 * DataGrid服务器端响应UI类
 * 
 * @Author 王育春
 * @Date 2008-1-5
 * @Mail wyuch@zving.com
 */
public class DataGridUI extends UIFacade {
	public static final int MaxPageSize = 10000;

	/*
	 * 方法本身不需要检查权限，但其method属性调用的方法需要检查权限
	 */
	@Verify(ignoreAll = true)
	@Priv(login = false)
	public void doWork() {
		DataGridAction dga = new DataGridAction();
		String method = $V(Constant.Method);
		dga.setMethod(method);

		dga.setID($V(Constant.ID));
		dga.setAjaxRequest(true);
		dga.setPageEnabled($B(Constant.Page));
		dga.setMultiSelect(!"false".equalsIgnoreCase($V(Constant.DataGridMultiSelect)));
		dga.setAutoFill(!"false".equalsIgnoreCase($V(Constant.DataGridAutoFill)));
		dga.setScroll($B(Constant.DataGridScroll));
		dga.setLazy($B(Constant.Lazy));
		dga.setCacheSize($I(Constant.CacheSize));
		dga.setParams(Current.getRequest());

		if (dga.isPageEnabled()) {
			dga.setPageIndex(0);
			dga.setPageIndex($I(Constant.DataGridPageIndex));
			if (dga.getPageIndex() < 0) {
				dga.setPageIndex(0);
			}
			if (dga.getPageIndex() != 0) {
				dga.setTotal($I(Constant.DataGridPageTotal));
			}
			dga.setPageSize($I(Constant.Size));
			if (dga.getPageSize() > MaxPageSize) {// 每页最大条数为10000
				dga.setPageSize(MaxPageSize);
			}
		}

		dga.setTagBody(DataGridBodyManager.get(Request.getString(Constant.TagBody)));

		// 响应DataGrid.insertRow
		String strInsertRowIndex = Request.getString(Constant.DataGridInsertRow);
		if (StringUtil.isNotEmpty(strInsertRowIndex)) {
			DataTable dt = Request.getDataTable(Constant.DataTable);
			Request.remove(Constant.DataTable);
			Request.remove(Constant.DataGridInsertRow);
			dga.bindData(dt);

			HtmlParser parser = new HtmlParser(dga.getResult());
			parser.parse();
			HtmlTR tr = new HtmlTR(parser.getDocument().getTopElementsByTagName("tr").get(1));
			$S("TRAttr", tr.getAttributes());
			for (int i = 0; i < tr.elements().size(); i++) {
				$S("TDAttr" + i, tr.getTD(i).getAttributes());
				$S("TDHtml" + i, tr.getTD(i).getInnerHTML());
			}
		} else {
			IMethodLocator m = MethodLocatorUtil.find(method);
			PrivCheck.check(m);
			// 参数检查
			if (!VerifyCheck.check(m)) {
				String message = "Verify check failed:method=" + method + ",data=" + Current.getRequest();
				LogUtil.warn(message);
				Current.getResponse().setFailedMessage(message);
				return;
			}
			m.execute(dga);
			$S("HTML", dga.getResult());
		}
	}

	@Priv(login = false)
	public void toExcel(ZAction za) throws Exception {
		HttpServletRequest request = za.getRequest();
		HttpServletResponse response = za.getResponse();
		request.setCharacterEncoding(Config.getGlobalCharset());
		response.reset();
		response.setContentType("application/octet-stream");
		String suffix = ".xls";
		if ("2007".equals(Config.getExcelVersion())) {
			suffix = ".xlsx";
		}
		response.setHeader("Content-Disposition", "attachment; filename=Excel_" + DateUtil.getCurrentDateTime("yyyyMMddhhmmss") + suffix);

		try {
			String xls = "_Excel_";
			Mapx<String, String> params = ServletUtil.getParameterMap(request);
			String ID = params.getString(xls + Constant.ID);
			String tagBody = params.getString(xls + Constant.TagBody);
			String pageIndex = params.getString(xls + Constant.DataGridPageIndex);
			String pageSize = params.getString(xls + Constant.Size);
			String rowTotal = params.getString(xls + Constant.DataGridPageTotal);
			String method = params.getString(xls + Constant.Method);
			String pageFlag = params.getString(xls + Constant.Page);
			String excelPageFlag = params.getString(xls + "_ZVING_ToExcelPageFlag");
			String strWidths = params.getString(xls + "_ZVING_Widths");
			String strIndexes = params.getString(xls + "_ZVING_Indexes");
			String strRows = params.getString(xls + "_ZVING_Rows");

			DataGridAction dga = new DataGridAction();
			dga.getParams().putAll(params);
			dga.setTagBody(DataGridBodyManager.get(tagBody));

			dga.setMethod(method);
			dga.setID(ID);
			dga.setAjaxRequest(true);
			OutputStream os = response.getOutputStream();
			try {
				Class<?> clazz = Class.forName("com.zving.framework.data.DataTableUtil");
				Method htmlTableToExcel = clazz.getMethod("prepareHtmlTableToExcel", new Class<?>[] { OutputStream.class,
						DataGridAction.class, String.class, String.class, String.class, String.class, String.class, String.class,
						String.class, String.class, String.class, String.class });
				htmlTableToExcel.invoke(null, new Object[] { os, dga, rowTotal, excelPageFlag, pageIndex, pageSize, pageFlag, method, xls,
						strIndexes, strRows, strWidths });
			} catch (Exception e) {
				e.printStackTrace();
			}
			os.flush();
			os.close();
			os = null;
			response.flushBuffer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
