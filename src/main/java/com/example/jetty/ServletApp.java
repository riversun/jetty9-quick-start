package com.example.jetty;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jetty9 Quick Start Example
 */
public class ServletApp
{
	public static void main(String[] args) {

		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		// set max size of form contents
		servletHandler.setMaxFormContentSize(1024 * 1024 * 1024);

		servletHandler.addServlet(new ServletHolder(new ExampleServlet()), "/api");

		final ResourceHandler resourceHandler = new ResourceHandler();

		// set content dir
		resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");

		// hide file listings
		resourceHandler.setDirectoriesListed(false);

		// set welcome files
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });

		// no cache
		resourceHandler.setCacheControl("no-store,no-cache,must-revalidate");

		// setup Server and Handler List
		HandlerList handlerList = new HandlerList();
		handlerList.addHandler(resourceHandler);
		handlerList.addHandler(servletHandler);

		final Server jettyServer = new Server();
		jettyServer.setHandler(handlerList);

		final int PORT = 8080;

		final HttpConfiguration httpConfig = new HttpConfiguration();

		// hide server info on header
		httpConfig.setSendServerVersion(false);
		final HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpConfig);
		final ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnFactory);
		httpConnector.setPort(PORT);
		jettyServer.setConnectors(new Connector[] { httpConnector });

		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("serial")
	public static class ExampleServlet extends HttpServlet {

		final ObjectMapper mObjectMapper = new ObjectMapper();

		final class Result {
			public boolean success;
			public String message;
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			// get qurey parameter
			final String paramMessage = req.getParameter("message");

			// create result object
			final Result result = new Result();
			result.success = true;
			result.message = "You say '" + paramMessage + "'";

			// enable CORS
			resp.addHeader("Access-Control-Allow-Origin", "*");
			resp.addHeader("Access-Control-Allow-Headers", "Content-Type");

			// set content type
			final String CONTENT_TYPE = "application/json; charset=UTF-8";
			resp.setContentType(CONTENT_TYPE);

			// respond as JSON
			final PrintWriter out = resp.getWriter();
			final String json = mObjectMapper.writeValueAsString(result);
			out.println(json);
			out.close();

		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sorry, POST is not supported");
		}

		@Override
		protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sorry, PUT is not supported");
		}

		@Override
		protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sorry, DELETE is not supported");
		}

	}
}
