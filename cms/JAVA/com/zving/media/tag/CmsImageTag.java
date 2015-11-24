package com.zving.media.tag;

import com.zving.framework.data.DBUtil;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.media.bl.ImageBL;
import com.zving.staticize.tag.AbstractListTag;
import java.util.List;

public class CmsImageTag extends AbstractListTag {
	private long imageID;
	private long imageGroupID;
	private String orderFlag;

	public String getOrderFlag() {
		return this.orderFlag;
	}

	public void setOrderFlag(String orderFlag) {
		this.orderFlag = orderFlag;
	}

	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = super.getTagAttrs();
		list.add(new TagAttr("imageID", false, 8, "@{Media.ImageTag.ImageIDUsage}"));
		list.add(new TagAttr("imageGroupID", false, 8, "@{Media.ImageTag.ImageGroupIDUsage}"));
		list.add(new TagAttr("orderFlag", false, 8, "@{Media.OrderFlag}"));
		return list;
	}

	public void prepareData() throws TemplateRuntimeException,
			ExpressionException {
		this.item = "Image";
		long siteid = this.context.evalLong("Site.ID");
		if (this.imageID == 0L) {
			this.imageID = this.context.evalLong("Image.ID");
		}
		if (this.imageGroupID == 0L) {
			this.imageGroupID = this.context.evalLong("ImageGroup.ID");
		}
		DataTable dt = new Q("select CopyType, CopyID from ZCContent where ID=?", new Object[] { Long.valueOf(this.imageGroupID) }).fetch();
		if ((dt != null) && (dt.getRowCount() > 0) && (dt.getLong(0, "CopyType") > 1L) && (dt.getLong(0, "CopyID") > 0L)) {
			this.imageGroupID = dt.getLong(0, "CopyID");
		}
		Q qb = new Q("select * from ZCImage where SiteID=?", new Object[] { Long.valueOf(siteid) });
		if (this.imageID != 0L) {
			qb.append(" and ID=?", new Object[] { Long.valueOf(this.imageID) });
		} else if (this.imageGroupID != 0L) {
			qb.append(" and GroupID=?", new Object[] { Long.valueOf(this.imageGroupID) });
		} else {
			qb.append(" and 1=2");
		}
		if (ObjectUtil.notEmpty(this.condition)) {
			qb.append(" and ", new Object[0]).append(this.condition, new Object[0]);
		}
		qb.append(" order by OrderFlag", new Object[0]);
		if (StringUtil.isEmpty(this.orderFlag)) {
			qb.append(" desc", new Object[0]);
		} else if ("asc".equals(this.orderFlag.toLowerCase())) {
			qb.append(" asc", new Object[0]);
		}
		if (this.page) {
			this.pageTotal = DBUtil.getCount(qb);
			this.context.setPageSize(this.pageSize);
			this.data = qb.fetch(this.context.getPageSize(), this.context.getPageIndex());
		} else {
			this.count = (this.count <= 0 ? 20 : this.count);
			this.count += this.begin;
			this.data = qb.fetch(this.count, 0);
		}
		ImageBL.decodeImagePath(this.context, this.data);
	}

	public long getImageID() {
		return this.imageID;
	}

	public void setImageID(long imageID) {
		this.imageID = imageID;
	}

	public long getImageGroupID() {
		return this.imageGroupID;
	}

	public void setImageGroupID(long imageGroupID) {
		this.imageGroupID = imageGroupID;
	}

	public int getPageTotal() {
		return this.pageTotal;
	}

	public boolean isEditEnable() {
		return false;
	}

	public String getPrefix() {
		return "cms";
	}

	public String getTagName() {
		return "image";
	}

	public String getDescription() {
		return "@{Media.Tag.ImageDescription}";
	}

	public String getExtendItemName() {
		return "@{Media.Tag.Image}";
	}

	public String getPluginID() {
		return "com.zving.media";
	}
}
