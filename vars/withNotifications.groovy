def call(String channel, Closure body) {
  sendNotifications('STARTED', channel)
  try {
    body()
    sendNotifications('SUCCESSFUL', channel)
  } catch (err) {
    sendNotifications('FAILED', channel)
    error "Build failed, caught exception: ${err}"
  }
}