# Issue resolver Plugin for SonarQube [![Build Status](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin.svg?branch=master)](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin)](https://sonarqube.com/dashboard/index?id=nl.future-edge.sonarqube.plugins%3Asonar-issueresolver-plugin)
*Requires SonarQube 6.3+*

This plugin allows you to export a list of issues that have been resolved (false positive or won't fix). After exporting the list you can import the list into a project where the issues in the project will be matched with the resolved issues; if matched the issue will be resolved.

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
- Find the page 'Issue resolver' under the global Administration > Configuration section.

#### Export and import
- Select the project to export and press the 'Export' button to download a datafile containing the resolved issues from the project. You will need 'browse issues' permission for this project.
- Select the project to resolve issues in, select the datafile containing the issues to import and press the 'Import' button to match and resolve issues. You will need 'Browse' and 'Administer issues' persmissions for this project.

#### Failures
When a failure is encountered during the import of issues, the import will be cancelled. However all issues that have already be resolved will not be reverted! Use the preview option to preview the matching results.