package com.lin.demo.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.lin.demo.BaseCRUD;
import com.lin.demo.ConstantSettings;
import com.lin.demo.http.ArticleRouter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class ArticleDaoVerticle extends AbstractVerticle implements BaseCRUD {
	public static final Logger LOGGER = LoggerFactory.getLogger(ArticleRouter.class);
	private JDBCClient jdbc;
	private HashMap<SqlQuery, String> sqlQueries;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("部署 ArticleDaoVerticle");
		sqlQueries = loadSqlQueries();

		jdbc = JDBCClient.createShared(vertx,
				new JsonObject()
						.put("url",
								config().getString(ConstantSettings.CONFIG_JDBC_URL,
										"jdbc:mysql://localhost:3306/vertx?useCursorFetch=true&useSSL=false"))
						.put("user", config().getString(ConstantSettings.CONFIG_JDBC_USER, "lin"))
						.put("password", config().getString(ConstantSettings.CONFIG_JDBC_PASSWORD, "qq451791119"))
						.put("driver_class",
								config().getString(ConstantSettings.CONFIG_JDBC_DRIVER_CLASS, "com.mysql.jdbc.Driver"))
						.put("max_pool_size", config().getInteger(ConstantSettings.CONFIG_JDBC_MAX_POOL_SIZE, 30)));

		jdbc.getConnection(ar -> {
			if (ar.failed()) {
				LOGGER.error("Could not open a database connection", ar.cause());

			} else {
				SQLConnection connection = ar.result();
				connection.execute(sqlQueries.get(SqlQuery.CREATE_TABLE), create -> {
					connection.close();
					if (create.failed()) {
						LOGGER.error("Database preparation error", create.cause());

					} else {
						LOGGER.info("注册Dao方法到事件总线");
						vertx.eventBus().consumer("dao://article/find", this::find);
						vertx.eventBus().consumer("dao://article/add", this::add);
						vertx.eventBus().consumer("dao://article/update", this::update);
						vertx.eventBus().consumer("dao://article/delete", this::delete);
						vertx.eventBus().consumer("dao://article/findAll", this::findAll);
					}
				});
			}
		});

	}

	public void find(Message<JsonObject> msg) {
		String id = msg.body().getString("fld_id");
		JsonArray params = new JsonArray().add(id);
		jdbc.getConnection(con -> {

			if (con.succeeded()) {
				SQLConnection sqlConnection = con.result();

				sqlConnection.queryWithParams(sqlQueries.get(SqlQuery.GET), params, res -> {

					if (res.succeeded()) {

						msg.reply(new JsonObject());
					} else {
						LOGGER.info(res.cause());
					}
				});
			}
		});
	}

	public void add(Message<JsonObject> msg) {
		LOGGER.info("执行add方法");
		String title = msg.body().getString("fld_title", "标题");
		String content = msg.body().getString("fld_content", "内容");
		JsonArray params = new JsonArray().add(title).add(content);
		jdbc.getConnection(con -> {

			if (con.succeeded()) {
				SQLConnection sqlConnection = con.result();

				sqlConnection.updateWithParams(sqlQueries.get(SqlQuery.ADD), params, res -> {

					if (res.succeeded()) {

						msg.reply(res.result().toJson());
					} else {
						LOGGER.info(res.cause());
					}
					

				});
			}

		});
	}

	public void update(Message<JsonObject> msg) {

		String id = msg.body().getString("fld_id", "");
		String title = msg.body().getString("fld_title", "");
		String content = msg.body().getString("fld_content", "");
		JsonArray params = new JsonArray().add(title).add(content).add(id);
		jdbc.getConnection(con -> {

			if (con.succeeded()) {
				SQLConnection sqlConnection = con.result();

				sqlConnection.updateWithParams(sqlQueries.get(SqlQuery.UPDATE), params, res -> {
					if (res.succeeded()) {

						msg.reply(res.result().toJson());
					} else {
						LOGGER.info(res.cause());
					}
				});
			}

		});
	}

	public void delete(Message<JsonObject> msg) {
		String id = msg.body().getString("fld_id", "");

		JsonArray params = new JsonArray().add(id);
		jdbc.getConnection(con -> {

			if (con.succeeded()) {
				SQLConnection sqlConnection = con.result();

				sqlConnection.updateWithParams(sqlQueries.get(SqlQuery.DELETE), params, res -> {

					if (res.succeeded()) {

						msg.reply(res.result().toJson());
					} else {
						LOGGER.info(res.cause());
					}
				});
			}

		});
	}

	public void findAll(Message<JsonObject> msg) {

		jdbc.getConnection(con -> {

			if (con.succeeded()) {
				SQLConnection sqlConnection = con.result();

				sqlConnection.query(sqlQueries.get(SqlQuery.ALL), res -> {
					if (res.succeeded()) {

						msg.reply(res.result().toJson());
					} else {
						LOGGER.info(res.cause());
					}
				});
			}

		});
	}

	/*
	 * Note: this uses blocking APIs, but data is small...
	 */
	public HashMap<SqlQuery, String> loadSqlQueries() throws IOException {

		HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
		sqlQueries.put(SqlQuery.CREATE_TABLE,
				"create table if not exists tbl_article (fld_id int not null primary key auto_increment unique, fld_title varchar(255) , fld_content blob)");
		sqlQueries.put(SqlQuery.ALL, "select * from tbl_article");
		sqlQueries.put(SqlQuery.GET, "select fld_id, fld_content from tbl_article where fld_id = ?");
		sqlQueries.put(SqlQuery.ADD, "insert into tbl_article values (NULL, ?, ?)");
		sqlQueries.put(SqlQuery.UPDATE, "update tbl_article set fld_title = ?,fld_content = ? where fld_id = ?");
		sqlQueries.put(SqlQuery.DELETE, "delete from tbl_article where fld_id = ?");
		return sqlQueries;
	}

}