package com.sengled.cloud.tomcat.session.redis;

import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;


public class RedisSessionHandlerValve extends ValveBase {
  private static Logger log = Logger.getLogger("RedisSessionHandlerValve");
  private RedisSessionManager manager;

  public void setRedisSessionManager(RedisSessionManager manager) {
    this.manager = manager;
  }

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException {
    try {
      getNext().invoke(request, response);
    } finally {
      final Session session = request.getSessionInternal(false);
      storeOrRemoveSession(session);
      manager.afterRequest();
    }
  }

  private void storeOrRemoveSession(Session session) {
    try {
      if (session != null) {
        if (session.isValid()) {
          log.info("Request with session completed, saving session " + session.getId());
          if (session.getSession() != null) {
            log.info("HTTP Session present, saving " + session.getId());
            manager.save(session);
          } else {
            log.info("No HTTP Session present, Not saving " + session.getId());
          }
        } else {
          log.info("HTTP Session has been invalidated, removing :" + session.getId());
          manager.remove(session);
        }
      }
    } catch (Exception e) {
      // Do nothing.
    }
  }
}
