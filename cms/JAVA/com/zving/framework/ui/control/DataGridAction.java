package com.zving.framework.ui.control;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.zving.framework.collection.Mapx;
import com.zving.framework.config.MaxPageSize;
import com.zving.framework.data.DBUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.ui.IPageEnableAction;
import com.zving.framework.ui.UIException;
import com.zving.framework.ui.control.grid.AbstractGridFeature;
import com.zving.framework.ui.control.grid.DataGridBody;
import com.zving.framework.ui.control.grid.FeatureManager;
import com.zving.framework.ui.control.grid.GridScript;
import com.zving.framework.ui.control.grid.GridSort;
import com.zving.framework.ui.control.grid.GridTree;
import com.zving.framework.ui.control.grid.GridTree.GridTreeParam;
import com.zving.framework.ui.tag.ListTag;
import com.zving.framework.ui.zhtml.ZhtmlExecuteContext;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;
import com.zving.framework.utility.FastStringBuilder;

/**
 * DataGrid绑定行为类
 * 
 * @Author 王育春
 * @Date 2007-6-21
 * @Mail wyuch@zving.com
 */
public class DataGridAction implements IPageEnableAction {

	String ID;

	boolean multiSelect = true;

	boolean autoFill = true;

	boolean autoPageSize = false;

	boolean scroll = true;

	boolean lazy = false;

	int cacheSize;

	int pageSize;

	int pageIndex;

	int total;

	boolean pageEnabled;

	String method;

	DataTable dataSource;

	String result = "";

	Mapx<String, Object> params = new Mapx<String, Object>();

	boolean totalRecalFlag = false;// 是否需要重新计算记录总数

	DataGridBody tagBody;

	boolean isAjaxRequest;

	public void bindData(QueryBuilder qb, boolean pageFlag) {
		if (pageFlag) {
			if (!totalRecalFlag) {// 需要重新计算总数
				setTotal(DBUtil.getCount(qb));
			}
			bindData(qb.executePagedDataTable(pageSize, pageIndex));
		} else {
			bindData(qb.executeDataTable());
		}
	}

	public void bindData(DataTable dt) {
		// toExcelFlag 为1时表示导出为Excel
		if ("1".equals(params.get("_ExcelFlag"))) {
			String[] columnNames = (String[]) params.get("_ColumnNames");
			String[] widths = (String[]) params.get("_Widths");
			String[] columnIndexes = (String[]) params.get("_ColumnIndexes");
			int indexes[] = new int[columnIndexes.length];
			int i = 0;
			for (String str : columnIndexes) {
				indexes[i++] = Integer.parseInt(str);
			}
			Arrays.sort(indexes);
			for (i = indexes.length - 1; i >= 0; i++) {
				dt.deleteColumn(indexes[i]);
			}
			try {
				Class<?> clazz = Class.forName("com.zving.framework.data.DataTableUtil");
				Method dataTableToExcel = clazz.getMethod("dataTableToExcel", new Class<?>[] { DataTable.class, OutputStream.class,
						String[].class, String[].class });
				dataTableToExcel.invoke(null, new Object[] { dt, (OutputStream) params.get("_OutputStream"), columnNames, widths });
			} catch (Exception e) {
			}
		} else {
			dataSource = dt;
			try {
				this.bindData();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void addVariables(AbstractExecuteContext context) {
		// 执行所有feature的beforeDatabind()
		for (AbstractGridFeature f : FeatureManager.getInstance().getAll()) {
			f.beforeDataBind(this, context, dataSource);
		}
		FastStringBuilder scriptSB = new FastStringBuilder();
		for (AbstractGridFeature f : FeatureManager.getInstance().getAll()) {
			f.appendScript(this, scriptSB);
		}
		context.addRootVariable(GridScript.Var, scriptSB.toStringAndClose());

		LangUtil.decodeDataTable(dataSource, context.getLanguage()); // 检查国际化字符串

		context.addDataVariable(ListTag.ZListDataNameKey, dataSource);
		context.addDataVariable(ListTag.ZListItemNameKey, "DataRow");
		context.addDataVariable("_DataGridAction", this);
	}

	private void bindData() throws Exception {
		if (!pageEnabled) {
			total = dataSource.getRowCount();
		}
		if (dataSource == null) {
			throw new UIException("DataSource must set before bindData()");
		}
		if (isAjaxRequest) {
			ZhtmlExecuteContext context = new ZhtmlExecuteContext(ZhtmlManagerContext.getInstance(), null, null);
			addVariables(context);
			tagBody.getExecutor().execute(context);
			result = context.getOut().getResult();
		}
	}

	public DataTable getDataSource() {
		return dataSource;
	}

	public void bindData(QueryBuilder qb) {
		bindData(qb, pageEnabled);
	}

	@Override
	public int getPageIndex() {
		return pageIndex;
	}

	@Override
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public void setPageSize(int pageSize) {
		if (pageSize > MaxPageSize.getValue()) {
			pageSize = MaxPageSize.getValue();
		}
		this.pageSize = pageSize;
	}

	public String getParam(String key) {
		return params.getString(key);
	}

	public Mapx<String, Object> getParams() {
		return params;
	}

	public void setParams(Mapx<String, Object> params) {
		this.params = params;
	}

	public boolean isPageEnabled() {
		return pageEnabled;
	}

	/**
	 * 请使用isPageEnabled()代替
	 */
	@Deprecated
	public boolean isPageFlag() {
		return pageEnabled;
	}

	public void setPageEnabled(boolean pageFlag) {
		pageEnabled = pageFlag;
	}

	public boolean isSortFlag() {
		return GridSort.isSortFlag(this);
	}

	public String getID() {
		return ID;
	}

	public void setID(String id) {
		ID = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public int getTotal() {
		return total;
	}

	@Override
	public void setTotal(int total) {
		if (total < 0) {
			return;
		}
		this.total = total;
		if (pageIndex > Math.ceil(total * 1.0 / pageSize)) {
			pageIndex = new Double(Math.floor(total * 1.0 / pageSize)).intValue();
		}
		totalRecalFlag = true;
	}

	@Override
	public void setTotal(QueryBuilder qb) {
		if (pageIndex == 0 || !totalRecalFlag) {
			setTotal(DBUtil.getCount(qb));
		}
	}

	public String getSortString() {
		return GridSort.getSortString(this);
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public boolean isAutoFill() {
		return autoFill;
	}

	public void setAutoFill(boolean autoFill) {
		this.autoFill = autoFill;
	}

	public boolean isAutoPageSize() {
		return autoPageSize;
	}

	public void setAutoPageSize(boolean autoPageSize) {
		this.autoPageSize = autoPageSize;
	}

	public boolean isScroll() {
		return scroll;
	}

	public void setScroll(boolean scroll) {
		this.scroll = scroll;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public static DataTable sortTreeDataTable(DataTable dt, String identifierColumnName, String parentIdentifierColumnName) {
		GridTreeParam gtp = new GridTreeParam();
		gtp.IdentifierColumnName = identifierColumnName;
		gtp.ParentIdentifierColumnName = parentIdentifierColumnName;
		gtp.StartLevel = 999;
		return GridTree.sortTreeDataTable(dt, gtp);
	}

	public DataGridBody getTagBody() {
		return tagBody;
	}

	public void setTagBody(DataGridBody tagBody) {
		this.tagBody = tagBody;
	}

	public boolean isAjaxRequest() {
		return isAjaxRequest;
	}

	public void setAjaxRequest(boolean isAjaxRequest) {
		this.isAjaxRequest = isAjaxRequest;
	}

	public String getResult() {
		return result == null ? "" : result;
	}

	public void setResult(String html) {
		result = html;
	}
}
