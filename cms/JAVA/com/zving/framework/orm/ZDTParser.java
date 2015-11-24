package com.zving.framework.orm;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Constant;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.utility.ZipUtil;

/**
 * 数据库导出文件解析器
 * 
 * @Author 王育春
 * @Date 2011-2-28
 * @Mail wyuch@zving.com
 */
public class ZDTParser {
	public static final String VERSION_1 = "1";
	public static final String VERSION_2 = "2";
	public static final String VERSION_3 = "3";
	public static final String VERSION_CURRENT = "3";

	private String file;

	private String Version;

	private ArrayList<ZDTTableInfo> Tables = new ArrayList<ZDTTableInfo>();

	public ZDTParser(String file) {
		this.file = file;
	}

	public void parse() {
		RandomAccessFile braf = null;
		try {
			braf = new RandomAccessFile(file, "r");
			// 读取版本
			byte[] bs = new byte[4];
			braf.read(bs);
			int len = NumberUtil.toInt(bs);
			bs = new byte[len];
			braf.read(bs);
			Version = new String(bs);
			if (!ObjectUtil.in(Version, VERSION_1, VERSION_2, VERSION_3)) {
				throw new RuntimeException("Unknown .zdt version:" + Version);
			}

			Mapx<String, ZDTTableInfo> map = new Mapx<String, ZDTTableInfo>();
			long currentPos = len + 4;
			while (braf.getFilePointer() != braf.length()) {
				// 先读取名称
				bs = new byte[4];
				braf.read(bs);
				currentPos += 4;

				len = NumberUtil.toInt(bs);
				bs = new byte[len];
				braf.read(bs);
				currentPos += len;
				String name = new String(bs);

				// 再读取字段描述
				bs = new byte[4];
				braf.read(bs);
				currentPos += 4;

				len = NumberUtil.toInt(bs);
				bs = new byte[len];
				braf.read(bs);
				currentPos += len;
				String columns = new String(bs);
				DAOColumn[] scs = parseColumns(columns);

				// 再读取索引
				bs = new byte[4];
				braf.read(bs);
				currentPos += 4;

				len = NumberUtil.toInt(bs);
				bs = new byte[len];
				braf.read(bs);
				currentPos += len;
				String indexInfo = new String(bs);
				if (Constant.Null.equals(indexInfo)) {
					indexInfo = null;
				}
				int rowCount = 0;
				// 再读取数据
				if (VERSION_1.equals(Version) || VERSION_2.equals(Version)) {
					bs = new byte[4];
					braf.read(bs);
					currentPos += 4;

					len = NumberUtil.toInt(bs);
					bs = new byte[len];
					braf.read(bs);
					currentPos += len;
				} else if (VERSION_3.equals(Version)) {
					currentPos = braf.getFilePointer();
					rowCount = braf.readInt();
					for (int i = 0; i < rowCount; i++) {
						braf.skipBytes(braf.readInt());
					}
				}
				if (!map.containsKey(name)) {
					ZDTTableInfo ti = new ZDTTableInfo();
					ti.Name = name;
					ti.Columns = scs;
					ti.IndexInfo = indexInfo;
					if (VERSION_1.equals(Version) || VERSION_2.equals(Version)) {
						ti.StartPosition = currentPos - len - 4;
					} else if (VERSION_3.equals(Version)) {
						ti.RowCount = rowCount;
						ti.StartPosition = currentPos;
					}
					ti.Positions.add(ti.StartPosition);
					map.put(name, ti);
					Tables.add(ti);
				} else {
					ZDTTableInfo ti = map.get(name);
					ti.Positions.add(currentPos - len - 4);
				}
			}
			// 计算记录数
			for (ZDTTableInfo ti : Tables) {
				if (VERSION_1.equals(Version) || VERSION_2.equals(Version)) {
					DataTable dt = readOneTable(ti, braf,
							ti.Positions.get(ti.Positions.size() - 1));
					ti.RowCount = (ti.Positions.size() - 1)
							* DBExporter.PageSize + dt.getRowCount();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (braf != null) {
				try {
					braf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public DataTable getDataTable(ZDTTableInfo ti, int start, int end) {
		RandomAccessFile braf = null;
		try {
			braf = new RandomAccessFile(file, "r");
			DataTable dt = null;
			if (VERSION_1.equals(Version) || VERSION_2.equals(Version)) {
				end = end - 1;// 最后一个不包括在内
				int startIndex = start / DBExporter.PageSize;
				int endIndex = end / DBExporter.PageSize;
				if (endIndex >= ti.Positions.size()) {
					endIndex = ti.Positions.size() - 1;
				}
				if (start / DBExporter.PageSize > ti.Positions.size()) {
					throw new RuntimeException("Invalid start position："
							+ start);
				}
				for (int i = startIndex; i <= endIndex; i++) {
					DataTable dt2 = readOneTable(ti, braf, ti.Positions.get(i));
					if (dt == null) {
						dt = new DataTable(dt2.getDataColumns(), null);
					}
					int rowStart = 0;
					if (i == startIndex) {
						rowStart = start % DBExporter.PageSize;
					}
					for (int j = rowStart; dt.getRowCount() < end + 1 - start
							&& j < dt2.getRowCount(); j++) {
						dt.insertRow(dt2.getDataRow(j));
					}
				}
			} else if (VERSION_3.equals(Version)) {
				dt = createTable(ti.Columns);
				int rowCount = braf.readInt();
				for (int i = 0; i < rowCount; i++) {
					String data = readUTF(braf);
					if (i >= start - 1 && i < end) {
						Object[] vs = getOneRow(ti.Columns, data);
						dt.insertRow(vs);
					}
				}
			}
			return dt;
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (braf != null) {
				try {
					braf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public String readUTF(RandomAccessFile braf) throws IOException{
		int len = braf.readInt();
		byte[] bs = new byte[len];
		braf.read(bs);
		return new String(bs,"UTF-8");
	}
	
	/**
	 * 读取一个表,对3以上版本无用
	 * 
	 * @param ti
	 * @param braf
	 * @param pos
	 * @return
	 * @throws Exception
	 */
	private static DataTable readOneTable(ZDTTableInfo ti,
			RandomAccessFile braf, long pos) throws Exception {
		braf.seek(pos);
		byte[] bs = new byte[4];
		braf.read(bs);
		int len = NumberUtil.toInt(bs);
		bs = new byte[len];
		braf.read(bs);
		bs = ZipUtil.unzip(bs);
		String data = new String(bs, "UTF-8");
		if (ObjectUtil.empty(data)) {
			return new DataTable();
		}
		return parseDataTable(ti, data);
	}

	private DAOColumn[] parseColumns(String columns) {
		String[] arr = StringUtil.splitEx(columns, "\n");
		ArrayList<DAOColumn> list = new ArrayList<DAOColumn>();
		for (String str : arr) {
			if (StringUtil.isEmpty(str)) {
				continue;
			}
			String[] arr2 = StringUtil.splitEx(str, "\t");
			String name = arr2[0];
			if (VERSION_1.equals(Version)) {
				int type = Integer.parseInt(arr2[1]);
				int length = Integer.parseInt(arr2[3]);
				int precision = Integer.parseInt(arr2[4]);
				boolean mandatory = "true".equals(arr2[5]);
				boolean pk = "true".equals(arr2[6]);
				DAOColumn sc = new DAOColumn(name, type, length, precision,
						mandatory, pk);
				list.add(sc);
			}
			if (VERSION_2.equals(Version) || VERSION_3.equals(Version)) {
				int type = Integer.parseInt(arr2[1]);
				int length = Integer.parseInt(arr2[2]);
				int precision = Integer.parseInt(arr2[3]);
				boolean mandatory = "true".equals(arr2[4]);
				boolean pk = "true".equals(arr2[5]);
				DAOColumn sc = new DAOColumn(name, type, length, precision,
						mandatory, pk);
				list.add(sc);
			}
		}
		DAOColumn[] scs = new DAOColumn[list.size()];
		return list.toArray(scs);
	}

	private static DataTable parseDataTable(ZDTTableInfo ti, String data) {
		DAOColumn[] scs = ti.Columns;
		DataTable dt = createTable(scs);
		String[] arr = StringUtil.splitEx(data, "\n");
		for (String str : arr) {
			Object[] vs = getOneRow(scs, str);
			dt.insertRow(vs);
		}
		return dt;
	}

	/**
	 * @param scs
	 * @return
	 */
	private static DataTable createTable(DAOColumn[] scs) {
		DataTable dt = new DataTable();
		for (DAOColumn sc : scs) {
			dt.insertColumn(new DataColumn(sc.getColumnName(), sc
					.getColumnType()));
		}
		return dt;
	}

	/**
	 * @param scs
	 * @param dt
	 * @param str
	 */
	private static Object[] getOneRow(DAOColumn[] scs, String str) {
		String[] arr2 = StringUtil.splitEx(str, "\t");
		Object[] vs = new Object[scs.length];
		int i = 0;
		for (String v : arr2) {
			if (ObjectUtil.empty(v)) {
				vs[i] = null;
			} else if (ObjectUtil.in(scs[i].getColumnType(), DataTypes.STRING,
					DataTypes.CLOB)) {
				if (v.equals("null")) {
					vs[i] = null;
				} else {
					vs[i] = StringUtil
							.javaDecode(v.substring(1, v.length() - 1));
				}
			} else if (ObjectUtil.in(scs[i].getColumnType(), DataTypes.INTEGER,
					DataTypes.SMALLINT)) {
				vs[i] = Integer.parseInt(v);
			} else if (scs[i].getColumnType() == DataTypes.LONG) {
				if ("\"\"".equals(v)) {
					vs[i] = new Long(0);
				} else {
					try {
						vs[i] = Long.parseLong(v);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (scs[i].getColumnType() == DataTypes.FLOAT) {
				vs[i] = Float.parseFloat(v);
			} else if (ObjectUtil.in(scs[i].getColumnType(), DataTypes.DOUBLE,
					DataTypes.DECIMAL)) {
				vs[i] = Double.parseDouble(v);
			} else if (scs[i].getColumnType() == DataTypes.DATETIME) {
				vs[i] = DateUtil.parseDateTime(v);
			} else if (scs[i].getColumnType() == DataTypes.BLOB) {
				vs[i] = StringUtil.base64Decode(StringUtil.javaDecode(v));
			}
			i++;
		}
		return vs;
	}

	public String getFile() {
		return file;
	}

	public String getVersion() {
		return Version;
	}

	public ArrayList<ZDTTableInfo> getTables() {
		return Tables;
	}

	public boolean importDB(ZDTTableInfo ti, DataAccess da) {
		RandomAccessFile braf = null;
		try {
			braf = new RandomAccessFile(file, "r");
			IDBType db = DBTypeService.getInstance().get(
					da.getConnection().getDBConfig().DBType);
			List<String> columns = new ArrayList<String>(ti.Columns.length);
			for (DAOColumn sc : ti.Columns) {
				columns.add(db.maskColumnName(sc.getColumnName()));
			}
			boolean isOracle = da.getConnection().getDBConfig().isOracle();
			for (long pos : ti.Positions) {
				braf.seek(pos);
				int rowCount = braf.readInt();
				Q q = new Q();
				q.setBatchMode(true);
				for (int i = 1; i <= rowCount; i++) {
					String data = readUTF(braf);
					Object[] vs = getOneRow(ti.Columns, data);
					List<Object> values = new ArrayList<Object>(
							ti.Columns.length);
					if (!checkNull(ti, isOracle, vs, values)) {
						continue;
					}
					if (q.getSQL().length() == 0) {
						q.insertInto(ti.Name, columns, values);
					} else {
						q.add(values);
					}
					q.addBatch();
					if (i % DBExporter.PageSize == 0) {
						da.executeNoQuery(q);
						q.clearBatches();
					}
				}
				if (q.getBatches().size() > 0) {
					da.executeNoQuery(q);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		} finally {
			if (braf != null) {
				try {
					braf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * @param ti
	 * @param isOracle
	 * @param vs
	 * @param values
	 * @return
	 */
	private boolean checkNull(ZDTTableInfo ti, boolean isOracle, Object[] vs,
			List<Object> values) {
		for (int j = 0; j < vs.length; j++) {
			Object v = vs[j];
			if (isOracle) {
				// NotNull的列允许空字符串，但Oracle不允许
				if (ti.Columns[j].isMandatory() && (v == null || v.equals(""))) {
					LogUtil.warn(ti.Name + "'s column "
							+ ti.Columns[j].getColumnName() + " can't be empty");
					return false;
				}
			}
			values.add(v);
		}
		return true;
	}

	public static class ZDTTableInfo {
		public String Name;
		public DAOColumn[] Columns;
		public String IndexInfo;
		public long StartPosition;
		public int RowCount;// 表中所有记录数
		public ArrayList<Long> Positions = new ArrayList<Long>();
	}

}
