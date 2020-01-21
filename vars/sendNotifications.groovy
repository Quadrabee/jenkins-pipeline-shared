#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 */
def call(String buildStatus = 'STARTED', String channel) {
  // build status of null means successful
  buildStatus = buildStatus ?: 'SUCCESS'

  // Default values
  def buildPhase = 'failed';
  def color = '#c21f2d'

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = '#d59e27'
    buildPhase = 'started';
  } else if (buildStatus == 'SUCCESS') {
    color = '#349b4a'
    buildPhase = 'succeeded';
  }

  def subject = "BuildJob ${buildPhase} for _${env.JOB_NAME} [${env.BUILD_NUMBER}]_"

  if (env.CHANGE_ID) {
    subject = "BuildJob ${buildPhase} for PR *<${pullRequest.url}|#${pullRequest.number} ${pullRequest.title}>*"
  }

  def summary = "<${env.BUILD_URL}|See build logs>"

  def blocks = [
    [
      "type": "section",
      "text": [
        "type": "mrkdwn",
        "text": subject
      ]
    ],
    [
      "type": "context",
      "elements": [
        // [
        //   "type": "image",
        //   "image_url": "https://img2.freepng.es/20180515/zxe/kisspng-jenkins-docker-continuous-delivery-installation-so-5afa799e222331.1197773615263645741398.jpg",
        //   "alt_text": "images"
        // ],
        [
          "type": "mrkdwn",
          "text": summary
        ]
      ]
    ]
  ]

  def attachments = [
    [
      "blocks": blocks,
      "color": color
    ]
  ]

  print attachments

  // Send notifications
  slackSend (attachments: attachments, channel: channel)
  slackSend (attachments: attachments, notifyCommitters: true)
}
