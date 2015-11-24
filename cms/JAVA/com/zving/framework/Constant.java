package com.zving.framework;

/**
 * 框架中的一些全局常量。
 * 
 * @Author 王育春
 * @Date 2007-7-16
 * @Mail wyuch@zving.com
 */
public class Constant {
	/**
	 * Session中的User对象的属性名
	 */
	public static final String UserAttrName = "_ZVING_USER";

	/**
	 * Cookie中SessionID对应的Cookie项名称，不同的中间件可能有所不同
	 */
	public static final String SessionIDCookieName = "JSESSIONID";

	/**
	 * Response对象中需要游览器执行的JS段
	 */
	public static final String ResponseScriptAttr = "_ZVING_SCRIPT";

	/**
	 * Response对象中需要浏览器显示的消息文本
	 */
	public static final String ResponseMessageAttrName = "_ZVING_MESSAGE";//

	/**
	 * Response对象中反馈给游览器的状态值，一般0表示有错误，1表示执行成功
	 */
	public static final String ResponseStatusAttrName = "_ZVING_STATUS";

	/**
	 * DataGrid中的表示当前页数的属性名，从0开始，0表示第一页
	 */
	public static final String DataGridPageIndex = "_ZVING_PAGEINDEX";

	/**
	 * DataGrid中表示记录总数的属性名
	 */
	public static final String DataGridPageTotal = "_ZVING_PAGETOTAL";

	/**
	 * DataGrid中表示排序方式的属性名，其值形如id desc,name asc
	 */
	public static final String DataGridSortString = "_ZVING_SORTSTRING";

	/**
	 * DataGrid中表示当前动作是插入空白行的属性名
	 */
	public static final String DataGridInsertRow = "_ZVING_INSERTROW";

	/**
	 * DataGrid中表示允许多选的属性名
	 */
	public static final String DataGridMultiSelect = "_ZVING_MULTISELECT";

	/**
	 * DataGrid中表示要求自动填充空白行以保持DataGrid高度的属性名
	 */
	public static final String DataGridAutoFill = "_ZVING_AUTOFILL";

	/**
	 * DataGrid中表示允许内容滚动的属性
	 */
	public static final String DataGridScroll = "_ZVING_SCROLL";

	/**
	 * 表示属性值是一个DataTable
	 */
	public static final String DataTable = "_ZVING_DATATABLE";

	/**
	 * 表示属性值是唯一ID
	 */
	public static final String ID = "_ZVING_ID";

	/**
	 * 表示属性值是一个后台Page类的方法
	 */
	public static final String Method = "_ZVING_METHOD";

	/**
	 * 表示是否允许分页，值为字 符串true和false
	 */
	public static final String Page = "_ZVING_PAGE";

	/**
	 * 表示大小的属性名，例如分页大小
	 */
	public static final String Size = "_ZVING_SIZE";

	/**
	 * 表示拖拽的方法名，例如DataList的SortEnd
	 */
	public static final String SortEnd = "_ZVING_SORTEND";

	/**
	 * 表示拖拽的样式名，例如DataList的DragClass
	 */
	public static final String DragClass = "_ZVING_DRAGCLASS";

	/**
	 * 表示控件标签包含的HTML内容的属性名
	 */
	public static final String TagBody = "_ZVING_TAGBODY";

	/**
	 * 表示控件标签包含的HTML内容的属性名
	 */
	public static final String TemplateTR = "_ZVING_TEMPLATE_TR";

	/**
	 * 表示树形结构中层级的属性名
	 */
	public static final String TreeLevel = "_ZVING_TREE_LEVEL";

	/**
	 * 树形控件中表示是否延迟加载的属性名
	 */
	public static final String TreeLazy = "_ZVING_TREE_LAZY"; // 延迟加载

	/**
	 * 树形控件中节点是否使用多选框
	 */
	public static final String TreeCheckbox = "_ZVING_TREE_CHECKBOX"; // 多选框
	/**
	 * 多选联动
	 */
	public static final String TreeCascade = "_ZVING_TREE_CASCADE"; // 多选联动

	/**
	 * 树形控件中节点是否使用单选框
	 */
	public static final String TreeRadio = "_ZVING_TREE_RADIO"; // 单选框

	/**
	 * 树形控件中表示是否全部展开的属性名
	 */
	public static final String TreeExpand = "_ZVING_TREE_EXPAND"; // 是否在延迟加载是全部展开

	/**
	 * 树形控件中表示关联父节点的字段名
	 */
	public static final String TreeParentColumn = "_ZVING_TREE_PARENTCOLUMN"; // 是否在延迟加载是全部展开

	/**
	 * 树形控件中表示css的属性名
	 */
	public static final String TreeStyle = "_ZVING_TREE_STYLE";

	/**
	 * 表示属性值是一个DataCollection对象
	 */
	public static final String Data = "_ZVING_DATA";

	/**
	 * 表示属性值是一个URL
	 */
	public static final String URL = "_ZVING_URL";

	/**
	 * 设置数据的输出格式
	 */
	public static final String DataFormat = "_ZVING_DATA_FORMAT";

	/**
	 * 表示一个空字符串。（某些场合下如果直接传空字符串，会被过滤掉，如URL中）
	 */
	public static final String Null = "_ZVING_NULL";

	/**
	 * 控件Action在PageContext中的属性名
	 */
	public static final String ActionInPageContext = "_ZVING_ACTION";

	/**
	 * 用户当前语言在Cookie中的属性名
	 */
	public static final String LanguageCookieName = "_ZVING_LANGUAGE";

	/**
	 * 当前请求不使用Session
	 */
	public static final String NoSession = "_ZVING_NOSESSION";

	/**
	 * 缓存数量
	 */
	public static final String CacheSize = "_ZVING_CACHESIZE";

	/**
	 * 延迟加载
	 */
	public static final String Lazy = "_ZVING_LAZY";

}
