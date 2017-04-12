package com.keenvil.cork.security.jwt;

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
}
