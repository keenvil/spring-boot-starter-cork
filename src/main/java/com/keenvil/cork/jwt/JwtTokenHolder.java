package com.keenvil.cork.jwt;

/**
 * Holder Object to obtain in statically way current Jwt User Token and
 * Community.
 * 
 * <p>It is intended to be used by API Rest Clients to forward the token
 * and community to other service calls.</p>
 */
public class JwtTokenHolder {

  private static ThreadLocal<String> tokens = new ThreadLocal<>();
  private static ThreadLocal<String> communities = new ThreadLocal<>();
  
  protected JwtTokenHolder() { }
  
  public static void holdToken(final String token) {
    tokens.set(token);
  }
  
  public static void holdCommunity(final String community) {
    communities.set(community);
  }
  
  public static String token() {
    return tokens.get();
  }
  
  public static String community() {
    return communities.get();
  }

  /**
   * Clears the token and community held for the current thread.
   *
   * <p>Must be called at the end of every request. Jetty (and most servlet
   * containers) reuse worker threads across unrelated requests, so a
   * ThreadLocal that is never cleared leaks whatever token/community an
   * earlier request on that same thread held into a later, unrelated
   * request's outgoing service-to-service calls -- including anonymous
   * requests that never presented a token themselves.</p>
   */
  public static void clear() {
    tokens.remove();
    communities.remove();
  }
}
