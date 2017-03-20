# Issue resolver Plugin for SonarQube [![Build Status](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin.svg?branch=master)](https://travis-ci.org/willemsrb/sonar-issueresolver-plugin) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin)](https://sonarqube.com/dashboard/index?id=nl.future-edge.sonarqube.plugins%3Asonar-issueresolver-plugin)
*Requires SonarQube 6.3+*

This plugin allows you to export a list of issues that have been resolved (false positive or won't fix). After exporting the list you can import the list into a project where the issues in the project will be matched with the resolved issues; if matched the issue will be resolved.

Use cases:
- 'Copying' resolved issues from a master branch to a release branch
- Using the list as a delivery for QA reports