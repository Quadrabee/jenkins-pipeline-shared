pipeline {
  agent any

  triggers {
    issueCommentTrigger('.*test this please.*')
  }

  environment {
    SLACK_CHANNEL = '#test-q8s'
  }

  stages {

    stage ('Start') {
      steps {
        sendNotifications('STARTED', SLACK_CHANNEL)
      }
    }
  }
  
  post {
    success {
      sendNotifications('SUCCESS', SLACK_CHANNEL)
    }
    failure {
      sendNotifications('FAILED', SLACK_CHANNEL)
    }
  }
}


