package com.zving.platform.point;

import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

public abstract class BeforeLocalUpdate implements IExtendAction {
	public final static String ExtendPointID = "com.zving.platform.BeforeLocalUpdate";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		Transaction trans = (Transaction) args[0];
		return execute(trans);
	}

	public abstract Object execute(Transaction trans);

	@Override
	public boolean isUsable() {
		return true;
	}

}
