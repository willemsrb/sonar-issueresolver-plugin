# Issue resolver Plugin for SonarQube [![Build Status](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin.svg?branch=master)](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin)](https://sonarqube.com/dashboard/index?id=nl.future-edge.sonarqube.plugins%3Asonar-issueresolver-plugin)
*Requires SonarQube 6.3+*

This plugin allows you to synchronize and export issue data (status, resolution, assignee and comments) of issues that have been confirmed, reopened or resolved. After exporting the data you can import it into a project where the issues in that project will be matched with the exported issue data; if matched the issue will be confirmed, reopened or resolved. Optionally the matched issue can be assigned to the same user and missing comments can be added.
When working within one SonarQube installation the issues can be updated between projects directly.

##### Use cases:
- Keeping resolved issues in sync between the master and a release/feature/maintenance branch
- Using the list as a delivery for QA reports

#### Matching issues, assignees and comments
Issues are matched using the component, rule and linenumber.
If an issue is  matched it will be reported as 'matched'; if no transition can be determined to reach the exported status and resolution a 'matchFailure' will be reported. If the transition could not be succesfully completed a 'transitionFailure' will be reported.

Assignee are matched using the username; the issue will be assigned to the assignee if the username is different and the issue will be reported as 'assigned'. If the assignment could not be succesfully completed an 'assignFailure' will be reported.

Comments are matched by comparing the markdown. If a comment is not present on the issue it will be added and the issue will be reported as 'commented'. If a comment could not be succesfully added a 'commentFailure' will be reported.

#### Resolving issues
When transitioning, assigning issues or adding comments the current logged in account will be used.

## Usage
- Install the plugin
- Find the page 'Issue resolver' under the project Administration section.

##### Update
- Select the plugin in the project you want to update issues in. You will need 'Browse' and 'Administer issues' (to resolve issues) permissions for this project.
- Select the 'Update' tab.
- Select the project you want to read issues from. You will need 'Browse' permission for this project.
- Press the 'Update' button to read, match and resolve issues.

##### Export
- Select the plugin in the project you want to export issues for. You will need 'Browse' and 'Administer issues' (to be able to reach the Administration section) permissions for this project.
- Select the 'Export' tab.
- Press the 'Export' button to download a datafile containing the resolved issues from the project.

##### Import
- Select the plugin in the project you want to export issues for. You will need 'Browse' and 'Administer issues' (to resolve issues) permissions for this project.
- Select the 'Export' tab.
- Select the datafile containing the issues to import
- Press the 'Import' button to upload the datafile and match and resolve issues.

##### Preview
Use the preview option to preview the matching results. No actual changes will be made to the  project.