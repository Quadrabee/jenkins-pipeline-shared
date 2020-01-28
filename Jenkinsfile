pipeline {
  agent any

  triggers {
    issueCommentTrigger('.*test this please.*')
  }

  environment {
    SLACK_CHANNEL = '#test-q8s'
    DOCKER_TAG = get_docker_tag()
  }

  stages {

    stage ('Start') {
      steps {
        sendNotifications('STARTED', SLACK_CHANNEL)
      }
    }

    stage('Build') {
      steps {
        sh "echo Building with tag ${DOCKER_TAG}"
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

def get_docker_tag() {
  def TAG_NAME = binding.variables.get("TAG_NAME")
  if (TAG_NAME != null) {
    return TAG_NAME
  }
  return 'latest'
}
