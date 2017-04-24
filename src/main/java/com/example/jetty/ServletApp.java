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

		// ServletContextHandlerはサーブレットをハンドリングする
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		// フォームのアップロードサイズを指定
		servletHandler.setMaxFormContentSize(1024 * 1024 * 1024);

		// サーブレットを追加
		servletHandler.addServlet(new ServletHolder(new ExampleServlet()), "/api");

		// ResourceHandlerは（ざっくりいうと）静的コンテンツをハンドリングする
		final ResourceHandler resourceHandler = new ResourceHandler();

		// 静的コンテンツの置き場所を指定
		resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");

		// 静的コンテンツのファイル一覧（リスティング）表示しない
		resourceHandler.setDirectoriesListed(false);

		// 初期表示するファイルを指定
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });

		// キャッシュさせない
		resourceHandler.setCacheControl("no-store,no-cache,must-revalidate");

		HandlerList handlerList = new HandlerList();

		// resourceHandlerが先にくるように指定する（逆にすると静的コンテンツは永遠によばれない。。)
		handlerList.addHandler(resourceHandler);
		handlerList.addHandler(servletHandler);

		// デフォルトコンストラクタでサーバーを初期化する
		final Server jettyServer = new Server();
		jettyServer.setHandler(handlerList);

		final int PORT = 8080;

		// httpの設定クラス
		final HttpConfiguration httpConfig = new HttpConfiguration();

		// サーバーのバージョン情報をヘッダにのせない
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

			// queryパラメータを取得
			final String paramMessage = req.getParameter("message");

			// レスポンス格納用POJO(あとでJSONに変換する)
			final Result result = new Result();
			result.success = true;
			result.message = "You say '" + paramMessage + "'";

			// CORS(Cross-Origin Resource Sharing)を有効にする
			resp.addHeader("Access-Control-Allow-Origin", "*");
			resp.addHeader("Access-Control-Allow-Headers", "Content-Type");

			// JSONを返すのContent-TypeをJSONにする
			final String CONTENT_TYPE = "application/json; charset=UTF-8";
			resp.setContentType(CONTENT_TYPE);

			final PrintWriter out = resp.getWriter();
			// JacksonでPOJOをJSONに変換する
			final String json = mObjectMapper.writeValueAsString(result);

			// レスポンスを生成
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
