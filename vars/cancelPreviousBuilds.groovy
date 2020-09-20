#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 */
def call(String buildStatus = 'STARTED', String channel) {
  def jobname = env.JOB_NAME
  def buildnum = env.BUILD_NUMBER.toInteger()

  def job = Jenkins.instance.getItemByFullName(jobname)
  for (build in job.builds) {
    if (!build.isBuilding()) { continue; }
    if (buildnum == build.getNumber().toInteger()) { continue; println "equals" } 
    build.doStop();
  }
}