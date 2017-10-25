package com.keenvil.cork.consul;

/**
 * the Enums here declared to supplement the endpoint y can access to resources
 */
public enum Properties {

  MYSQL {
    public String toString() {
      return rootDatabase + "/mysql";
    }
  },
  MONGO {
    public String toString() {
      return rootDatabase + "/mongo";
    }
  },
  RABBIT {
    public String toString() {
      return rootQueues + "/rabbit";
    }
  };
  private static String rootDatabase = "/databases";
  private static String rootQueues = "/queues";

}

