# Contributing guide

**Want to contribute to simple tutorials? That's great!**

We try to make it easy, and all contributions, even the smaller ones, are more than welcome.
This includes bug reports, fixes, documentation, new tutorials...

But first, please read this page.

## Reporting an issue

This project uses JIRA issues to manage the issues, but feel free to create an issue in this repository and
we will handle the JIRA lifecycle.

## Before you contribute and structure of the repository

To contribute, use GitHub Pull Requests, from your **own** fork.

* The `main` branch uses the latest release of Infinispan (development or final version)
* The `x.x.x` branch uses the latest stable release corresponding the name of the branch.

All contributions must target the `main` branch.

### Code reviews

All submissions, including submissions by project members, need to be reviewed before being merged.

### Continuous Integration

Because we are all humans, the project use a continuous integration approach and each pull request triggers a full build.
Please make sure to monitor the output of the build and act accordingly.

### Documentation is not optional

The documentation under documentation must be updated accordingly.

## Build with Maven

* Clone the repository: `git clone https://github.com/infinispan/infinispan-simple-tutorials.git`
* Navigate to the directory: `cd infinispan-simple-tutorials`
* Invoke `mvn clean package` from the root directory

Thanks!! :)
