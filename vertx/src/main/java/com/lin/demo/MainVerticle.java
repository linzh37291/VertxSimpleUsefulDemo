package com.lin.demo;
  
import com.lin.demo.database.ArticleDaoVerticle;
import com.lin.demo.http.ArticleRouter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;



public class MainVerticle extends AbstractVerticle {
	public static final Logger LOGGER = LoggerFactory.getLogger(ArticleRouter.class);
	 @Override
	  public void start(Future<Void> startFuture) throws Exception {
		    LOGGER.info("启动verticle");
		   
		 	Router router = Router.router(vertx);
		 	ArticleRouter articleRouter = new ArticleRouter(router);
		 	articleRouter.start();
		 	  vertx.deployVerticle(new ArticleDaoVerticle());
		 	HttpServer server = vertx.createHttpServer();
		 	router.route("/").handler(routingContext -> {
		 	   HttpServerResponse response = routingContext.response();
		 	   response
		 	       .putHeader("content-type", "text/html")
		 	       .end("<h1>Hello from my first Vert.x 3 application</h1>");
		 	 });
       
		 	
		 	server.requestHandler(router::accept).listen(8080);
		 
		
	  }
}
