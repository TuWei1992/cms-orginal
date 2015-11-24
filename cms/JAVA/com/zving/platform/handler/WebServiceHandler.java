package com.zving.platform.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.attachments.Attachments;
import org.codehaus.xfire.attachments.StreamedAttachments;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.handler.AbstractHandler;
import org.codehaus.xfire.handler.Phase;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.ServiceRegistry;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.ObjectInvoker;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.transport.Transport;
import org.codehaus.xfire.transport.TransportManager;
import org.codehaus.xfire.transport.http.HtmlServiceWriter;
import org.codehaus.xfire.transport.http.SoapHttpTransport;
import org.codehaus.xfire.transport.http.XFireHttpSession;
import org.codehaus.xfire.transport.http.XFireServletController;
import org.codehaus.xfire.util.STAXUtils;

import com.zving.framework.Config;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.utility.LogUtil;
import com.zving.platform.webservice.APIMethod;
import com.zving.platform.webservice.XFireServletTransport;

public class WebServiceHandler implements IURLHandler {
	public static final String ID = "com.zving.platform.WebServiceHandler";
	public static final String HTTP_SERVLET_REQUEST = "XFireServletController.httpServletRequest";
	public static final String HTTP_SERVLET_RESPONSE = "XFireServletController.httpServletResponse";
	public static final String HTTP_SERVLET_CONTEXT = "XFireServletController.httpServletContext";
	public static final String PREFIX = "/api/webservice/";

	private static ThreadLocal<HttpServletRequest> requests = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> responses = new ThreadLocal<HttpServletResponse>();

	protected XFire xfire;
	protected SoapHttpTransport transport;
	protected ServletContext servletContext;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "WebService URL Processor";
	}

	@Override
	public void init() {
		if (xfire == null) {
			xfire = XFireFactory.newInstance().getXFire();
		}
		ObjectServiceFactory factory = new ObjectServiceFactory();
		Service service = factory.create(APIMethod.class);
		service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, APIMethod.class);
		xfire.getServiceRegistry().register(service);
		transport = new XFireServletTransport();
		transport.addFaultHandler(new FaultResponseCodeHandler());

		Transport oldSoap = getTransportManager().getTransport(SoapHttpTransport.SOAP11_HTTP_BINDING);
		if (oldSoap != null) {
			getTransportManager().unregister(oldSoap);
		}

		getTransportManager().register(transport);
	}

	public static HttpServletRequest getRequest() {
		return requests.get();
	}

	public static HttpServletResponse getResponse() {
		return responses.get();
	}

	protected TransportManager getTransportManager() {
		return getXFire().getTransportManager();
	}

	@Override
	public void destroy() {
		for (Iterator<?> iterator = xfire.getTransportManager().getTransports().iterator(); iterator.hasNext();) {
			Transport transport = (Transport) iterator.next();
			transport.dispose();
		}
	}

	@Override
	public int getOrder() {
		return 9990;
	}

	@Override
	public boolean match(String url) {
		if (url.startsWith(PREFIX)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String serviceName = getServiceName(url);
		if (serviceName == null) {
			serviceName = "";
		}
		ServiceRegistry reg = xfire.getServiceRegistry();
		response.setHeader("Content-Type", Config.getGlobalCharset());
		try {
			requests.set(request);
			responses.set(response);
			boolean hasService = reg.hasService(serviceName);
			if (serviceName.length() == 0 || !hasService) {
				if (!hasService) {
					response.setStatus(404);
				}
				generateServices(request, response);
				return true;
			}
			if (isWSDLRequest(request)) {
				generateWSDL(response, serviceName);
			} else {
				invoke(request, response, serviceName);
			}
		} catch (Exception e) {
			LogUtil.error("Couldn't invoke servlet request.");
			if (e instanceof ServletException) {
				throw (ServletException) e;
			} else {
				throw new ServletException(e);
			}
		} finally {
			requests.set(null);
			responses.set(null);
		}
		return true;
	}

	protected boolean isWSDLRequest(HttpServletRequest request) {
		return request.getQueryString() != null && request.getQueryString().trim().equalsIgnoreCase("wsdl");
	}

	protected void generateService(HttpServletResponse response, String serviceName) throws ServletException, IOException {
		response.setContentType("text/html");
		Service endpoint = getServiceRegistry().getService(serviceName);
		HtmlServiceWriter writer = new HtmlServiceWriter();
		try {
			writer.write(response.getOutputStream(), endpoint);
		} catch (XMLStreamException e) {
			throw new ServletException("Error writing HTML services list", e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void generateServices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		try {
			Object value = XFireFactory.newInstance().getXFire().getProperty(XFire.SERVICES_LIST_DISABLED);
			if (value != null && "true".equals(value.toString().toLowerCase())) {
				response.getOutputStream().write("Services list disabled".getBytes());
			} else {
				Collection<?> services = getServiceRegistry().getServices();
				XMLStreamWriter writer = STAXUtils.createXMLStreamWriter(response.getOutputStream(), null, null);
				writer.writeStartDocument();
				writePreamble(writer, "XFire Services");
				writer.writeStartElement("body");
				writer.writeStartElement("p");
				writer.writeEndElement();
				if (!services.isEmpty()) {
					writer.writeStartElement("p");
					writer.writeCharacters("Available Services:");
					writer.writeEndElement();
					writer.writeStartElement("ul");
					int port = request.getServerPort();
					StringBuffer sb = new StringBuffer();
					sb.append(request.getScheme()).append("://").append(request.getServerName());
					if (port != 80 && port != 443 && port != 0) {
						sb.append(':').append(port);
					}
					sb.append("".equals(request.getContextPath()) ? "/" : request.getContextPath());
					sb.append(PREFIX);
					String base = sb.toString();
					List servicesList = new ArrayList();
					servicesList.addAll(services);
					Collections.sort(servicesList, new ServiceComperator());
					services = servicesList;
					for (Iterator iterator = services.iterator(); iterator.hasNext(); writer.writeEndElement()) {
						Service service = (Service) iterator.next();
						String url = base + service.getSimpleName() + "?wsdl";
						writer.writeStartElement("li");
						writer.writeCharacters(service.getSimpleName());
						Object obj = service.getProperty("wsdl.generation.disabled");
						if (obj == null || "false".equals(obj.toString().toLowerCase())) {
							writer.writeCharacters(" ");
							writer.writeStartElement("a");
							writer.writeAttribute("href", url);
							writer.writeCharacters("[wsdl]");
							writer.writeEndElement();
						}
					}

				}
				writer.writeComment("Just filling space according to http://support.microsoft.com/default.aspx?scid=kb;en-us;Q294807");
				writer.writeComment("Just filling space according to http://support.microsoft.com/default.aspx?scid=kb;en-us;Q294807");
				writer.writeEmptyElement("br");
				writer.writeEmptyElement("br");
				writer.writeEmptyElement("br");
				writer.writeEmptyElement("br");
				writer.writeEmptyElement("br");
				writer.writeCharacters("       Generated by XFire ( http://xfire.codehaus.org ) ");
				writer.writeEmptyElement("hr");
				writer.writeEndDocument();
				writer.flush();
			}
		} catch (XMLStreamException e) {
			throw new ServletException("Error writing HTML services list", e);
		}
	}

	@SuppressWarnings("rawtypes")
	class ServiceComperator implements Comparator {
		@Override
		public int compare(Object s1, Object s2) {
			return ((Service) s1).getSimpleName().compareToIgnoreCase(((Service) s2).getSimpleName());
		}

	}

	private void writePreamble(XMLStreamWriter writer, String title) throws XMLStreamException {
		writer.writeDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		writer.writeStartElement("html");
		writer.writeStartElement("head");
		writer.writeStartElement("title");
		writer.writeCharacters(title);
		writer.writeEndElement();
		writer.writeEndElement();
	}

	protected MessageContext createMessageContext(HttpServletRequest request, HttpServletResponse response, String service) {
		XFireHttpSession session = new XFireHttpSession(request);
		MessageContext context = new MessageContext();
		context.setXFire(getXFire());
		context.setSession(session);
		context.setService(getService(service));
		context.setProperty(HTTP_SERVLET_REQUEST, request);
		context.setProperty(HTTP_SERVLET_RESPONSE, response);

		if (servletContext != null) {
			context.setProperty(HTTP_SERVLET_CONTEXT, servletContext);
		}
		return context;
	}

	protected Channel createChannel(MessageContext context) throws ServletException {
		HttpServletRequest request = (HttpServletRequest) context.getProperty(HTTP_SERVLET_REQUEST);
		try {
			return transport.createChannel(request.getRequestURI());
		} catch (Exception e) {
			LogUtil.debug("Couldn't open channel.");
			throw new ServletException("Couldn't open channel.", e);
		}
	}

	protected void invoke(HttpServletRequest request, HttpServletResponse response, String service) throws ServletException, IOException,
			UnsupportedEncodingException {
		response.setStatus(200);
		response.setBufferSize(1024 * 8);

		MessageContext context = createMessageContext(request, response, service);
		Channel channel = createChannel(context);

		String soapAction = getSoapAction(request);
		String contentType = request.getContentType();
		if (null == contentType) {
			response.setContentType("text/html; charset=UTF-8");

			response.getWriter().write("<html><body>Invalid SOAP request.</body></html>");
			response.getWriter().close();
		} else if (contentType.toLowerCase().indexOf("multipart/related") != -1) {
			// Nasty Hack to workaraound bug with lowercasing contenttype by some serwers what cause problems with finding message parts.
			// There should be better fix for this.
			String ct = request.getContentType().replaceAll("--=_part_", "--=_Part_");

			Attachments atts = new StreamedAttachments(context, request.getInputStream(), ct);
			String encoding = getEncoding(atts.getSoapContentType());
			XMLStreamReader reader = STAXUtils.createXMLStreamReader(atts.getSoapMessage().getDataHandler().getInputStream(), encoding,
					context);
			InMessage message = new InMessage(reader, request.getRequestURI());
			message.setProperty(SoapConstants.SOAP_ACTION, soapAction);
			message.setAttachments(atts);

			channel.receive(context, message);
			try {
				reader.close();
			} catch (XMLStreamException e) {
				throw new XFireRuntimeException("Could not close XMLStreamReader.");
			}
		} else {
			// Remove " and ' char
			String charEncoding = request.getCharacterEncoding();
			charEncoding = dequote(charEncoding);
			XMLStreamReader reader = STAXUtils.createXMLStreamReader(request.getInputStream(), charEncoding, context);

			InMessage message = new InMessage(reader, request.getRequestURI());
			message.setProperty(SoapConstants.SOAP_ACTION, soapAction);
			channel.receive(context, message);

			try {
				reader.close();
			} catch (XMLStreamException e) {
				throw new XFireRuntimeException("Could not close XMLStreamReader.");
			}
		}
	}

	protected String dequote(String charEncoding) {
		if (charEncoding != null && charEncoding.length() > 0) {
			if (charEncoding.charAt(0) == '"' && charEncoding.charAt(charEncoding.length() - 1) == '"' || charEncoding.charAt(0) == '\''
					&& charEncoding.charAt(charEncoding.length() - 1) == '\'') {
				charEncoding = charEncoding.substring(1, charEncoding.length() - 1);
			}
		}
		return charEncoding;
	}

	protected String getSoapAction(HttpServletRequest request) {
		String action = request.getHeader(SoapConstants.SOAP_ACTION);
		if (action != null && action.startsWith("\"") && action.endsWith("\"") && action.length() >= 2) {
			action = action.substring(1, action.length() - 1);
		}
		return action;
	}

	protected String getEncoding(String enc) throws ServletException {
		if (enc == null) {
			return "UTF-8";
		}
		int typeI = enc.indexOf("type=");
		if (typeI == -1) {
			return null;
		}
		int charI = enc.indexOf("charset=", typeI);
		if (charI == -1) {
			return null;
		}
		int end = enc.indexOf("\"", charI);
		if (end == -1) {
			end = enc.indexOf(";", charI);
		}
		if (end == -1) {
			throw new ServletException("Invalid content type: " + enc);
		}
		return enc.substring(charI + 8, end);
	}

	protected void generateWSDL(HttpServletResponse response, String service) throws ServletException, IOException {
		Service userService = getXFire().getServiceRegistry().getService(service);
		Object value = userService.getProperty(Service.DISABLE_WSDL_GENERATION);
		boolean isWSDLDisabled = "true".equalsIgnoreCase(value != null ? value.toString() : null);
		if (isWSDLDisabled) {
			LogUtil.warn("WSDL generation disabled for service :" + service);
			response.sendError(404, "No wsdl is avaiable for this service");
			return;
		}

		response.setStatus(200);
		response.setContentType("text/xml");
		getXFire().generateWSDL(service, response.getOutputStream());
	}

	protected String getServiceName(String url) {
		String serviceName = url.substring(PREFIX.length());
		if (serviceName.indexOf("?") > 0) {
			serviceName = serviceName.substring(0, serviceName.indexOf("?"));
		}
		return serviceName;
	}

	protected Service getService(String name) {
		return getXFire().getServiceRegistry().getService(name);
	}

	public XFire getXFire() {
		return xfire;
	}

	public ServiceRegistry getServiceRegistry() {
		return xfire.getServiceRegistry();
	}

	public static class FaultResponseCodeHandler extends AbstractHandler {
		public FaultResponseCodeHandler() {
			super();
			setPhase(Phase.TRANSPORT);
		}

		@Override
		public void invoke(MessageContext context) {
			HttpServletResponse response = XFireServletController.getResponse();
			if (response != null) {
				response.setStatus(500);
			}
		}
	}
}
