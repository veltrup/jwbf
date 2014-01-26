package net.sourceforge.jwbf.core.actions;

import java.util.Map;

import net.sourceforge.jwbf.core.actions.util.HttpAction;

import com.google.common.collect.Maps;

public class Post implements HttpAction {

  private final String req;
  private final Map<String, Object> params = Maps.newHashMap();
  private final String charset;

  public Post(String req, String charset) {
    this.req = req;
    this.charset = charset;
  }

  /**
   * Use utf-8 as default charset
   */
  public Post(String url) {
    this(url, "utf-8");
  }

  public void addParam(String key, Object value) {
    params.put(key, value);
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public String getRequest() {
    return req;
  }

  public String getCharset() {
    return charset;
  }

}
