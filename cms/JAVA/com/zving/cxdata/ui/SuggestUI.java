package com.zving.cxdata.ui;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.StringUtil;

@Alias("Suggest")
public class SuggestUI extends UIFacade {
	
	@Priv
	public void bindData(DataGridAction dga) {
		Q q = new Q("select * from mssuggest order by suggest_id desc");
		DataTable  dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dga.setTotal(q);
		dga.bindData(dt);
	}
	
	@Priv
	public void delete(){
		String ids = $V("ids");
		if (!StringUtil.checkID(ids)) {
		      return;
	    }
	    Transaction trans = Current.getTransaction();
	    for (String suggestId : StringUtil.splitEx(ids, ",")) {
	      trans.add(new Q("delete from mssuggest where suggest_id=?", new Object[] {suggestId}));
	    }
	    if (trans.commit()) {
	      success(Lang.get("Common.DeleteSuccess"));
	    } else {
	      fail(Lang.get("Common.DeleteFailed"));
	    }
	}
}
