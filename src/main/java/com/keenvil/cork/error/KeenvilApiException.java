package com.keenvil.cork.error;

import java.util.List;

import org.apache.commons.lang3.Validate;

/**
 * Keenvil API exceptions.
 * 
 * <p>Common Generic Exceptions provided here are the ones that must be used
 * along Keenvil APIs. Each Module cannot not define his owns exceptions.</p>
 *
 */
public class KeenvilApiException extends KeenvilException {

  private static final long serialVersionUID = 1L;

  /**
   * Base API exception.
   */
  public KeenvilApiException() { 
    super();
  }

  public KeenvilApiException(final String message) {
    super(message);
  }

  public KeenvilApiException(final String message, final Throwable throwable) {
    super(message, throwable);
  }

  /**
   * Thrown when a service resource passed to a service has not been found.
   * 
   * <p>Commons examples of this situation are PUT/DELETE calls over a domain
   * resource with a non existing or invalid resource id.</p>
   *
   */
  public static class ResourceNotFound extends KeenvilApiException {

    private static final long serialVersionUID = 1L;


    public ResourceNotFound() {
      super();
    }

    public ResourceNotFound(final String message) {
      super(message);
    }

    
    public ResourceNotFound(final String message, final Throwable throwable) {
      super(message, throwable);
    }
  }

  /**
   * Thrown when a service tries to create an already existing resource.
   * 
   * <p>Commons examples of this situation are POST calls over a domain resource
   * with id or fields already taken.</p>
   */
  public static class ResourceAlreadyExists extends KeenvilApiException {

    private static final long serialVersionUID = 1L;

    public ResourceAlreadyExists() {
      super();
    }

    public ResourceAlreadyExists(final String message) {
      super(message);
    }

    public ResourceAlreadyExists(final String message,
        final Throwable throwable) {
      super(message, throwable);
    }
  }

  /**
   * Throw when a service tries to modify a resource to an invalid state where
   * a resource invariant is not meet.
   * 
   * <p>Commons examples of this situation are PUT calls over a domain resource
   * with invalid fields values.</p>
   * 
   * <p>Payload for this exception are {@link KeenvilApiError} which gave
   * more detail about invariants not meet.</p>
   */
  public static class InvalidResourceState extends KeenvilApiException {

    private static final long serialVersionUID = 1L;

    private List<KeenvilApiError> errors ;

    public InvalidResourceState() {
      super();
    }

    public InvalidResourceState(final String message) {
      super(message);
    }
    
    public InvalidResourceState(final String message,
        final Throwable throwable) {
      super(message, throwable);
    }

    public InvalidResourceState(final List<KeenvilApiError> theErrors) {
      Validate.notNull(theErrors, "Errors cannot be null.");
      errors = theErrors;
    }
    
    public List<KeenvilApiError> getErrors() {
      return errors;
    }
  }

  /**
   * Thrown when a service gets an invalid argument as a parameter.
   * 
   * <p>There is some scenarios where method arguments cannot be validated
   * with an predefined annotation.</p>
   */
  public static class InvalidArgument extends KeenvilApiException {

    private static final long serialVersionUID = 1L;

    public InvalidArgument() { }

    public InvalidArgument(final String message) {
      super(message);
    }

    public InvalidArgument(final String message, Throwable throwable) {
      super(message, throwable);
    }
  }

  /** 
   * Throw when there was a problem trying to authenticate a user into
   * the platform.
   * 
   * <p>Commons examples of this situation are POST calls to the authentication
   * service with invalid credentials.</p>
   */
  public static class Authorization extends KeenvilApiException {

    private static final long serialVersionUID = 1L;

    public Authorization() {
      super();
    }

    public Authorization(final String message) {
      super(message);
    }

    public Authorization(final String message, final Throwable throwable) {
      super(message, throwable);
    }
  }

  /**
   * Thrown when a call to a resource is made with valid credentials but they
   * can not grant access to the requested resource.
   * 
   * <p>Commons examples of this situation are calls to the resources with
   * the wrong set of roles.</p>
   */
  public static class Forbidden extends KeenvilApiException {

    private static final long serialVersionUID = 1L;

    public Forbidden() {
      super();
    }

    public Forbidden(final String message) {
      super(message);
    }

    public Forbidden(final String message, final Throwable throwable) {
      super(message, throwable);
    }
  }

  /**
   * Throw when no such a tenant properties in {@link
   * com.keenvil.cork.consul.ConsulService}
   * <p>this happens when try get configuration of a tenant what not
   * exist. In http this status code is 422</p>
   */
  public static class UnprocessedEntity extends KeenvilApiException{
    private static final long serialVersionUID = 1L;

    public UnprocessedEntity() {
      super();
    }

    public UnprocessedEntity(final String message) {
      super(message);
    }

    public UnprocessedEntity(final String message, final Throwable throwable) {
      super(message, throwable);
    }
  }

  /**
   * Thrown when a call to a resource is made but the server could not
   * understand the request due to invalid syntax or invalid body.
   *
   * <p>Commons examples of this situation are calls to the resources with
   * incomplete data or errors in the body.</p>
   */
  public static class BadRequest extends KeenvilApiException {

    private static final long serialVersionUID = 1L;

    public BadRequest() {
      super();
    }

    public BadRequest(final String message) {
      super(message);
    }

    public BadRequest(final String message, final Throwable throwable) {
      super(message, throwable);
    }
  }
}
