package com.ceilfors.jenkins.plugins.jirabuilder

import net.rcarz.jiraclient.*
import net.sf.json.JSONObject

/**
 * @author ceilfors
 */
class RcarzJira implements Jira {

    private JiraClient jiraClient

    private static final WEBHOOK_NAME = "Jenkins JIRA Builder"

    public RcarzJira() {
        BasicCredentials creds = new BasicCredentials("admin", "admin");
        jiraClient = new JiraClient("http://localhost:2990/jira", creds);
    }

    @Override
    String createIssue() {
        Issue issue = jiraClient.createIssue("TEST", "Task")
                .field(Field.SUMMARY, "task summary")
                .execute()
        return issue.key
    }

    @Override
    String createIssue(String description) {
        Issue issue = jiraClient.createIssue("TEST", "Task")
                .field(Field.SUMMARY, "task summary")
                .field(Field.DESCRIPTION, description)
                .execute()
        return issue.key
    }

    @Override
    void addComment(String issueKey, String comment) {
        jiraClient.getIssue(issueKey).addComment(comment)
    }

    @Override
    void registerWebHook(String url) {
        try {
            def restClient = jiraClient.restClient
            URI uri = restClient.buildURI("/rest/webhooks/latest/webhook");
            def requestBody = """
                {
                    "name": "$WEBHOOK_NAME",
                    "url": "$url",
                    "events": [
                        "${JiraWebHook.WEBHOOK_EVENT}"
                    ],
                    "excludeIssueDetails": false
                }
            """.toString()
            restClient.post(uri, JSONObject.fromObject(requestBody))
        } catch (Exception ex) {
            throw new JiraException("Failed to register webhook", ex);
        }
    }

    @Override
    def deleteAllWebHooks() {
        try {
            def restClient = jiraClient.restClient
            URI getUri = restClient.buildURI("/rest/webhooks/latest/webhook");
            def webhooks = restClient.get(getUri)

            for (jsonObject in webhooks.findAll { it["name"] == WEBHOOK_NAME}) {
                URI deleteUri = restClient.buildURI("/rest/webhooks/latest/webhook" + jsonObject["self"] - getUri)
                restClient.delete(deleteUri)
            }
        } catch (Exception ex) {
            throw new JiraException("Failed to unregister webhook", ex);
        }
    }
}
