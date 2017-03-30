# Issue resolver Plugin for SonarQube [![Build Status](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin.svg?branch=master)](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin)](https://sonarqube.com/dashboard/index?id=nl.future-edge.sonarqube.plugins%3Asonar-issueresolver-plugin)
*Requires SonarQube 6.3+*

This plugin allows you to synchronize and export issues that have been resolved (false positive or won't fix). After exporting the list you can import the list into a project where the issues in the project will be matched with the resolved issues; if matched the issue will be resolved. When working within one SonarQube installation the issues can be updated between projects directly.

Use cases:
- Keeping resolved issues in sync between the master and a release/feature/maintenance branch
- Using the list as a delivery for QA reports

#### Matching issues and comments
Issues are matched using the component, rule and linenumber.
If an issue cannot be matched it will be reported as 'unmatched'; if an issue cannot be resolved (its status is not 'open', 'confirmed' or 'reopened') it will be reported as 'unresolved'.

Comments are matched by comparing the markdown. If the comment is already present on the issue it will not be added when resolving an issue.

#### Resolving issues
When resolving issues and adding comment the current logged in account will be used.

## Usage
- Install the plugin
- Find the page 'Issue resolver' under the project Administration section.

#### Update
- Select the plugin in the project you want to update issues in. You will need 'Browse' and 'Administer issues' (to resolve issues) permissions for this project.
- Select the 'Update' tab.
- Select the project you want to read issues from. You will need 'Browse' permission for this project.
- Press the 'Update' button to read, match and resolve issues.

#### Export
- Select the plugin in the project you want to export issues for. You will need 'Browse' and 'Administer issues' (to be able to reach the Administration section) permissions for this project.
- Select the 'Export' tab.
- Press the 'Export' button to download a datafile containing the resolved issues from the project.

#### Import
- Select the plugin in the project you want to export issues for. You will need 'Browse' and 'Administer issues' (to resolve issues) permissions for this project.
- Select the 'Export' tab.
- Select the datafile containing the issues to import
- Press the 'Import' button to upload the datafile and match and resolve issues.

#### Failures
When a failure is encountered during the processing of issues, the action will be cancelled. However all issues that have already be resolved will not be reverted! Use the preview option to preview the matching results.