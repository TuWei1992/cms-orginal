package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.orm.DAOSet;
import com.zving.schema.ZDMetaModel;

public abstract class BeforeMetaModelDelete implements IExtendAction {
	public final static String ExtendPointID = "com.zving.platform.BeforeMetaModelDelete";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		@SuppressWarnings("unchecked")
		DAOSet<ZDMetaModel> set = (DAOSet<ZDMetaModel>) args[0];
		return execute(set);
	}

	public abstract Object execute(DAOSet<ZDMetaModel> models);

}
