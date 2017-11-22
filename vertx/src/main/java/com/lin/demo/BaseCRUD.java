package com.lin.demo;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface BaseCRUD {

	
	public  void find(Message<JsonObject> msg);
	
	public  void add(Message<JsonObject> msg);
	public  void update(Message<JsonObject> msg);
	
	
	public  void delete(Message<JsonObject> msg);
	
	public  void findAll(Message<JsonObject> msg);
}
