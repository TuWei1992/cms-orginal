package com.zving.platform.webservice;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.transport.DefaultEndpoint;
import org.codehaus.xfire.transport.http.SoapHttpTransport;

import com.zving.platform.handler.WebServiceHandler;

public final class XFireServletTransport extends SoapHttpTransport {
	@Override
	public Channel createNewChannel(String uri) {
		XFireServletChannel c = new XFireServletChannel(uri, this);
		c.setEndpoint(new DefaultEndpoint());
		return c;
	}

	@Override
	public String getServiceURL(Service service) {
		HttpServletRequest req = WebServiceHandler.getRequest();
		if (req == null) {
			return super.getServiceURL(service);
		}
		StringBuilder output = new StringBuilder(128);
		output.append(req.getScheme());
		output.append("://");
		output.append(req.getServerName());
		if (req.getServerPort() != 80 && req.getServerPort() != 443 && req.getServerPort() != 0) {
			output.append(':');
			output.append(req.getServerPort());
		}
		output.append(req.getRequestURI());
		return output.toString();
	}
}