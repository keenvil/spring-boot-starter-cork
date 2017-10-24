package com.keenvil.cork.consul;

public enum  Properties {
  /**
   * end point for resources databases in consul
   */
  DATABASES {
    public String toString() {
      return "/databases";
    }
  },
  /**
   * end point for resources queues in consul
   */
  QUEUES {
    public String toString() {
      return "/queues";
    }
  }
}

