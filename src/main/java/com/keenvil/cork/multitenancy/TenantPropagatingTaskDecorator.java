package com.keenvil.cork.multitenancy;

import org.springframework.core.task.TaskDecorator;

import com.keenvil.cork.CommunityIdentifierResolver;
import com.keenvil.cork.jwt.JwtTokenHolder;

/**
 * <p>TaskDecorator para executors {@code @Async}: resuelve el tenant (y toma el token)
 * en el hilo que encola la tarea, mientras la request todavia existe, y los fija en
 * {@link JwtTokenHolder} del hilo que la ejecuta.</p>
 *
 * <p>Reemplaza el patron de propagar los {@code RequestAttributes}: para cuando corre la
 * tarea la request ya termino y el contenedor recicla (o reutiliza para otra request) el
 * {@code HttpServletRequest}, asi que leer el tenant de su URL daba NPE o el tenant de
 * otra request. Con este decorator el hilo async no ve ninguna request y los resolvers
 * de cork caen al community del holder.</p>
 *
 * <p>Al terminar restaura los valores previos del holder en vez de limpiarlos: con
 * {@code CallerRunsPolicy} la tarea corre en el propio hilo de la request, y limpiar ahi
 * le borraria el token y el community al resto de esa request. En un hilo del pool los
 * previos son null, asi que queda limpio y no se filtra nada a la proxima tarea.</p>
 */
public class TenantPropagatingTaskDecorator implements TaskDecorator {

  private final CommunityIdentifierResolver communityResolver;

  public TenantPropagatingTaskDecorator(CommunityIdentifierResolver communityResolver) {
    this.communityResolver = communityResolver;
  }

  @Override
  public Runnable decorate(Runnable runnable) {
    String community = communityResolver.resolve();
    String token = JwtTokenHolder.token();

    return () -> {
      String previousCommunity = JwtTokenHolder.community();
      String previousToken = JwtTokenHolder.token();
      JwtTokenHolder.holdCommunity(community);
      JwtTokenHolder.holdToken(token);
      try {
        runnable.run();
      } finally {
        JwtTokenHolder.holdCommunity(previousCommunity);
        JwtTokenHolder.holdToken(previousToken);
      }
    };
  }
}
