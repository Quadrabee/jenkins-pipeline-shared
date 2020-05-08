#!groovy

import groovy.json.JsonOutput
import java.util.Optional
import hudson.tasks.test.AbstractTestResultAction
import hudson.model.Actionable
import hudson.tasks.junit.CaseResult

def call(String channel) {

  def speedUp = '--configure-on-demand --daemon --parallel'
  def nebulaReleaseScope = (env.GIT_BRANCH == 'origin/master') ? '' : "-Prelease.scope=patch"
  def nebulaRelease = "-x prepare -x release snapshot ${nebulaReleaseScope}"
  def gradleDefaultSwitches = "${speedUp} ${nebulaRelease}"
  def gradleAdditionalTestTargets = "integrationTest"
  def gradleAdditionalSwitches = "shadowJar"
  def author = ""
  def message = ""
  def testSummary = ""
  def total = 0
  def failed = 0
  def skipped = 0

  def isPublishingBranch = { ->
      return env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH =~ /release.+/
  }

  def isResultGoodForPublishing = { ->
      return currentBuild.result == null
  }

  def notifySlack(text, channel, attachments) {
      def slackURL = '[SLACK_WEBHOOK_URL]'
      def jenkinsIcon = 'https://wiki.jenkins-ci.org/download/attachments/2916393/logo.png'

      def payload = [
          text: text,
          channel: channel,
          username: "Jenkins",
          icon_url: jenkinsIcon,
          attachments: attachments
      ]

      slackSend(payload)
  }

  def getGitAuthor = {
      def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
      author = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${commit}").trim()
  }

  def getLastCommitMessage = {
      message = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
  }

  @NonCPS
  def getTestSummary = { ->
      def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
      def summary = ""

      if (testResultAction != null) {
          total = testResultAction.getTotalCount()
          failed = testResultAction.getFailCount()
          skipped = testResultAction.getSkipCount()

          summary = "Passed: " + (total - failed - skipped)
          summary = summary + (", Failed: " + failed)
          summary = summary + (", Skipped: " + skipped)
      } else {
          summary = "No tests found"
      }
      return summary
  }

  @NonCPS
  def getFailedTests = { ->
      def testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
      def failedTestsString = "```"

      if (testResultAction != null) {
          def failedTests = testResultAction.getFailedTests()

          if (failedTests.size() > 9) {
              failedTests = failedTests.subList(0, 8)
          }

          for(CaseResult cr : failedTests) {
              failedTestsString = failedTestsString + "${cr.getFullDisplayName()}:\n${cr.getErrorDetails()}\n\n"
          }
          failedTestsString = failedTestsString + "```"
      }
      return failedTestsString
  }

  def populateGlobalVariables = {
      getLastCommitMessage()
      getGitAuthor()
      testSummary = getTestSummary()
  }

  sh "./gradlew ${gradleDefaultSwitches} clean build ${gradleAdditionalTestTargets} ${gradleAdditionalSwitches} --refresh-dependencies"
  step $class: 'JUnitResultArchiver', testResults: '**/rspec*.xml'
  populateGlobalVariables()

  def buildColor = currentBuild.result == null ? "good" : "warning"
  def buildStatus = currentBuild.result == null ? "Success" : currentBuild.result
  def jobName = "${env.JOB_NAME}"

  // Strip the branch name out of the job name (ex: "Job Name/branch1" -> "Job Name")
  jobName = jobName.getAt(0..(jobName.indexOf('/') - 1))

  if (failed > 0) {
      buildStatus = "Failed"
      buildColor = "danger"
      def failedTestsString = getFailedTests()

      notifySlack("", channel, [
          [
              title: "${jobName}, build #${env.BUILD_NUMBER}",
              title_link: "${env.BUILD_URL}",
              color: "${buildColor}",
              text: "${buildStatus}\n${author}",
              "mrkdwn_in": ["fields"],
              fields: [
                  [
                      title: "Branch",
                      value: "${env.GIT_BRANCH}",
                      short: true
                  ],
                  [
                      title: "Test Results",
                      value: "${testSummary}",
                      short: true
                  ],
                  [
                      title: "Last Commit",
                      value: "${message}",
                      short: false
                  ]
              ]
          ],
          [
              title: "Failed Tests",
              color: "${buildColor}",
              text: "${failedTestsString}",
              "mrkdwn_in": ["text"],
          ]
      ])
  } else {
      notifySlack("", channel, [
          [
              title: "${jobName}, build #${env.BUILD_NUMBER}",
              title_link: "${env.BUILD_URL}",
              color: "${buildColor}",
              author_name: "${author}",
              text: "${buildStatus}\n${author}",
              fields: [
                  [
                      title: "Branch",
                      value: "${env.GIT_BRANCH}",
                      short: true
                  ],
                  [
                      title: "Test Results",
                      value: "${testSummary}",
                      short: true
                  ],
                  [
                      title: "Last Commit",
                      value: "${message}",
                      short: false
                  ]
              ]
          ]
      ])
  }
}