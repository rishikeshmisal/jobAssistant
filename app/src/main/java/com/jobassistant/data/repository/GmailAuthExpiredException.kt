package com.jobassistant.data.repository

import java.io.IOException

/**
 * Thrown when the stored Gmail OAuth2 access token has expired and cannot be silently refreshed.
 * Callers (GmailSyncWorker) must catch this specifically and surface a re-authentication prompt
 * to the user rather than retrying, which would fail for the same reason.
 */
class GmailAuthExpiredException(
    message: String = "Gmail access token expired — user must re-authenticate"
) : IOException(message)
