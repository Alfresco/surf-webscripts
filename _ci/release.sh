#!/usr/bin/env bash
set -e

# Use full history for release
git checkout -B "$BRANCH_NAME"

#sync the branch by getting the latest changes
git pull

# Add email to link commits to user
git config user.email "${GIT_EMAIL}"
git config user.name "${GIT_USERNAME}"

mvn install -DskipTests=true -B -V

mvn --batch-mode -q \
-DskipTests \
-Dusername="${GIT_USERNAME}" \
-Dpassword="${GIT_PASSWORD}" \
-DscmCommentPrefix="[maven-release-plugin][skip ci]" \
"-Darguments=-DskipTests " \
release:clean release:prepare release:perform

