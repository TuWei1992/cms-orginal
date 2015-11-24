package com.zving.contentcore.property;

import com.zving.contentcore.IProperty;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PropertyService extends AbstractExtendService<IProperty> {
	public static PropertyService getInstance() {
		return (PropertyService) findInstance(PropertyService.class);
	}

	public static List<IProperty> getSiteProperties() {
		List<IProperty> props = new ArrayList();
		props.addAll(getInstance().getAll());
		for (int i = props.size() - 1; i >= 0; i--) {
			IProperty prop = (IProperty) props.get(i);
			if (prop == null) {
				props.remove(prop);
			} else if (!prop.hasUseType(1)) {
				props.remove(prop);
			}
		}
		return props;
	}

	public static List<IProperty> getCatalogProperties(String contentType) {
		List<IProperty> props = new ArrayList();
		props.addAll(getInstance().getAll());
		for (int i = props.size() - 1; i >= 0; i--) {
			IProperty prop = (IProperty) props.get(i);
			if (prop == null) {
				props.remove(prop);
			} else if (!prop.hasUseType(2)) {
				props.remove(prop);
			} else if ((prop.getContentType() != null) && (!prop.getContentType().equals(contentType))) {
				props.remove(prop);
			}
		}
		return props;
	}

	public static List<IProperty> getContentProperties(String contentType) {
		List<IProperty> props = new ArrayList();
		props.addAll(getInstance().getAll());
		for (int i = props.size() - 1; i >= 0; i--) {
			IProperty prop = (IProperty) props.get(i);
			if (prop == null) {
				props.remove(prop);
			} else if (!prop.hasUseType(4)) {
				props.remove(prop);
			} else if ((prop.getContentType() != null) && (!prop.getContentType().equals(contentType))) {
				props.remove(prop);
			}
		}
		return props;
	}

	public void validateSite(Mapx<String, Object> map)
			throws PropertyInvalidException {
		for (String key : map.keySet()) {
			IProperty prop = (IProperty) getInstance().get(key);
			if (prop != null) {
				if (prop.hasUseType(1)) {
					String value = map.getString(key);
					if ((StringUtil.isNotEmpty(value)) && (!prop.validate(value))) {
						throw new PropertyInvalidException(key + "(" + Lang.get(prop.getExtendItemName(), new Object[0]) + ")" + "'s value invalid:" + value);
					}
				}
			}
		}
	}

	public void validateContent(Mapx<String, Object> map, String contentType)
			throws PropertyInvalidException {
		for (String key : map.keySet()) {
			IProperty prop = (IProperty) getInstance().get(key);
			if (prop != null) {
				if ((!ObjectUtil.notEmpty(contentType)) || (!ObjectUtil.notEmpty(prop.getContentType())) || (prop.getContentType().equals(contentType))) {
					if (prop.hasUseType(4)) {
						String value = map.getString(key);
						if ((StringUtil.isNotEmpty(value)) && (!prop.validate(value))) {
							throw new PropertyInvalidException(key + "(" + Lang.get(prop.getExtendItemName(), new Object[0]) + ")" + "'s value invalid:" + value);
						}
					}
				}
			}
		}
	}

	public void validateCatalog(Mapx<String, Object> map, String contentType)
			throws PropertyInvalidException {
		for (String key : map.keySet()) {
			IProperty prop = (IProperty) getInstance().get(key);
			if (prop != null) {
				if ((!ObjectUtil.notEmpty(contentType)) || (!ObjectUtil.notEmpty(prop.getContentType())) || (prop.getContentType().equals(contentType))) {
					if (prop.hasUseType(2)) {
						String value = map.getString(key);
						if ((StringUtil.isNotEmpty(value)) && (!prop.validate(value))) {
							throw new PropertyInvalidException(key + "(" + Lang.get(prop.getExtendItemName(), new Object[0]) + ")" + "'s value invalid:" + value);
						}
					}
				}
			}
		}
	}

	public void addSiteDefaultValues(Mapx<String, String> map) {
		List<IProperty> props = getSiteProperties();
		for (IProperty prop : props) {
			String v = map.getString(prop.getExtendItemID());
			if (ObjectUtil.empty(v)) {
				map.put(prop.getExtendItemID(), prop.defaultValue());
			}
		}
	}

	public void addContentDefaultValues(Mapx<String, String> map, String contentType) {
		List<IProperty> props = getContentProperties(contentType);
		for (IProperty prop : props) {
			String v = map.getString(prop.getExtendItemID());
			if (ObjectUtil.empty(v)) {
				map.put(prop.getExtendItemID(), prop.defaultValue());
			}
		}
	}

	public void addCatalogDefaultValues(Mapx<String, String> map, String contentType) {
		List<IProperty> props = getCatalogProperties(contentType);
		for (IProperty prop : props) {
			String v = map.getString(prop.getExtendItemID());
			if (ObjectUtil.empty(v)) {
				map.put(prop.getExtendItemID(), prop.defaultValue());
			}
		}
	}

	public Mapx<String, String> processSite(String configProps, Mapx<String, Object> request) {
		Mapx<String, String> map = PropertyUtil.parse(configProps);
		List<IProperty> props = getSiteProperties();
		for (IProperty prop : props) {
			String v = prop.process(request);
			if (ObjectUtil.equal(v, prop.defaultValue())) {
				map.remove(prop.getExtendItemID());
			} else {
				map.put(prop.getExtendItemID(), v);
			}
		}
		clearProperty(props, map);
		return map;
	}

	public Mapx<String, String> processCatalog(String configProps, Mapx<String, Object> request, String contentType) {
		Mapx<String, String> map = PropertyUtil.parse(configProps);
		List<IProperty> props = getCatalogProperties(contentType);
		for (IProperty prop : props) {
			String v = prop.process(request);
			if (ObjectUtil.equal(v, prop.defaultValue())) {
				map.remove(prop.getExtendItemID());
			} else {
				map.put(prop.getExtendItemID(), v);
			}
		}
		clearProperty(props, map);
		return map;
	}

	public Mapx<String, String> processContent(String configProps, Mapx<String, Object> request, String contentType) {
		Mapx<String, String> map = PropertyUtil.parse(configProps);
		List<IProperty> props = getContentProperties(contentType);
		for (IProperty prop : props) {
			String v = prop.process(request);
			if (ObjectUtil.equal(v, prop.defaultValue())) {
				map.remove(prop.getExtendItemID());
			} else {
				map.put(prop.getExtendItemID(), v);
			}
			prop.addSupplement(map, request);
		}
		clearProperty(props, map);
		return map;
	}

	private void clearProperty(List<IProperty> props, Mapx<String, String> map) {
		if (map.size() > 0) {
			List<String> clearList = new ArrayList();
			for (String key : map.keySet()) {
				boolean clear = true;
				for (IProperty prop : props) {
					if (!prop.keepParam(key)) {
						clear = false;
						break;
					}
				}
				if (clear) {
					map.remove(key);
					clearList.add(key);
				}
			}
			if (clearList.size() > 0) {
				LogUtil.warn("被清理的属性：" + StringUtil.join(clearList));
			}
		}
	}
}
