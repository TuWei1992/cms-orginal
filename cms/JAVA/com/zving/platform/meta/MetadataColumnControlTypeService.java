package com.zving.platform.meta;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.AbstractExtendService;

/**
 * author: 欧阳晓亮
 * Email: oyxl@zving.com
 * Date: 2013-3-12
 */
public class MetadataColumnControlTypeService extends AbstractExtendService<IMetadataColumnControlType> {

	public static MetadataColumnControlTypeService getInstance() {
		return findInstance(MetadataColumnControlTypeService.class);
	}

	public static Mapx<String, Object> getColumnControlTypeNameMap() {
		Mapx<String, Object> mapx = new Mapx<String, Object>();
		for (IMetadataColumnControlType t : MetadataColumnControlTypeService.getInstance().getAll()) {
			mapx.put(t.getExtendItemID(), t.getExtendItemName());
		}
		return mapx;
	}

	public static IMetadataColumnControlType getColumnControlTypoe(String typeID) {
		for (IMetadataColumnControlType t : MetadataColumnControlTypeService.getInstance().getAll()) {
			if (typeID.equals(t.getExtendItemID())) {
				return t;
			}
		}
		return null;
	}
}
