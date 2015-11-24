package com.zving.cxdata.bl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.zving.framework.Config;
import com.zving.framework.utility.LogUtil;

public class LdapBL {
	public static boolean authUser(String username, String password) {
		String url = Config.getValue("App.ldapURL");
    	username = "dds\\" + username; //domain\\user|user@mail.com
    	Hashtable env = new Hashtable();
    	DirContext ctx;
    	env.put(Context.PROVIDER_URL, url);
    	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	env.put(Context.SECURITY_AUTHENTICATION, "simple");
    	env.put(Context.SECURITY_PRINCIPAL, username); 
    	env.put(Context.SECURITY_CREDENTIALS, password);
    	try {
    		ctx = new InitialDirContext(env);
    		ctx.close();
    		LogUtil.info("Ldap验证成功:username=" + username);
    		return true;
    	} catch (NamingException err) {
    		LogUtil.info("Ldap验证失败: username="+username +", errmsg="+err);
    		return false;
    	}
	}
}
