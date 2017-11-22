package com.lin.demo.domain;
import io.vertx.core.json.JsonObject;

public class TblArticle {

  private final int fld_id;

  private String fld_title;

  private String fld_content;

  public TblArticle(String title, String content) {
    this.fld_title = title;
    this.fld_content = content;
    this.fld_id = -1;
  }

  public TblArticle(JsonObject json) {
    this.fld_title = json.getString("FLD_TITLE");
    this.fld_content = json.getString("FLD_CONTENT");
    this.fld_id = json.getInteger("ID");
  }

  public TblArticle() {
    this.fld_id = -1;
  }

  public TblArticle(int id, String title, String content) {
    this.fld_id = id;
    this.fld_title = title;
    this.fld_content = content;
  }

 
}