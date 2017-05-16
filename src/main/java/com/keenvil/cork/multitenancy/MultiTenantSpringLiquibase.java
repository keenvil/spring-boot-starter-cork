package com.keenvil.cork.multitenancy;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

import static org.slf4j.LoggerFactory.getLogger;


public class MultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {

  private static Logger log = getLogger(MultitenancyAutoConfiguration.class);

  private final List<DataSource> dataSources = new ArrayList<DataSource>();

  private String changeLog;

  private String contexts;

  private String labels;

  private Map<String, String> parameters;

  private boolean dropFirst = false;

  private boolean shouldRun = true;

  private File rollbackFile;

  private ResourceLoader resourceLoader;

  @Override
  public void afterPropertiesSet() throws Exception {
    runOnAllDataSources();
  }

  private void runOnAllDataSources() throws LiquibaseException {
    for(DataSource aDataSource : dataSources) {
      log.info("Initializing Liquibase for data source " + aDataSource);
      SpringLiquibase liquibase = getSpringLiquibase(aDataSource);
      liquibase.afterPropertiesSet();
      log.info("Liquibase ran for data source " + aDataSource);
    }
  }

  private SpringLiquibase getSpringLiquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setChangeLog(changeLog);
    liquibase.setChangeLogParameters(parameters);
    liquibase.setContexts(contexts);
    liquibase.setLabels(labels);
    liquibase.setDropFirst(dropFirst);
    liquibase.setShouldRun(shouldRun);
    liquibase.setRollbackFile(rollbackFile);
    liquibase.setResourceLoader(resourceLoader);
    liquibase.setDataSource(dataSource);
    return liquibase;
  }

  public String getChangeLog() {
    return changeLog;
  }

  public void setChangeLog(String changeLog) {
    this.changeLog = changeLog;
  }

  public String getContexts() {
    return contexts;
  }

  public void setContexts(String contexts) {
    this.contexts = contexts;
  }

  public String getLabels() {
    return labels;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public boolean isDropFirst() {
    return dropFirst;
  }

  public void setDropFirst(boolean dropFirst) {
    this.dropFirst = dropFirst;
  }

  public boolean isShouldRun() {
    return shouldRun;
  }

  public void setShouldRun(boolean shouldRun) {
    this.shouldRun = shouldRun;
  }

  public File getRollbackFile() {
    return rollbackFile;
  }

  public void setRollbackFile(File rollbackFile) {
    this.rollbackFile = rollbackFile;
  }

  public void addDataSource(DataSource theDataSource) {
    log.info("Adding Datasource {}", theDataSource);
    dataSources.add(theDataSource);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

}
