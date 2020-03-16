#!/bin/bash

# exit immediately when a command fails
set -e
# only exit with zero if all commands of the pipeline exit successfully
set -o pipefail
# error on unset variables
set -u
# print each command before executing it
set -x

prepare_signing() {
  echo "Decrypting private GPG key"
  gpg --quiet --batch --yes --decrypt --passphrase=${PASSPHRASE_SIGNING_KEY_ENCRYPT} --output ./.github/signing-key.asc ./.github/signing-key.asc.gpg
  echo "Importing GPG key to system"
  gpg --no-tty --batch --allow-secret-key-import --import ./.github/signing-key.asc
  rm -rf ./.github/signing-key.asc
}

set -o errexit -o nounset -o pipefail

safe_checkout_master_or_release() {
  # We need to be on a branch for release:perform to be able to create commits,
  # and we want that branch to be master or release-X.Y, which has been checked before.
  # But we also want to make sure that we build and release exactly the tagged version, so we verify that the remote
  # branch is where our tag is.
  checkoutBranch=$(echo ${GITHUB_REF} | sed 's/.[[:digit:]]\+$//' | sed 's/^refs\/tags\///')
  if ! git ls-remote --exit-code --heads origin "$checkoutBranch" ; then
    checkoutBranch=master
  fi
  git checkout -B "${checkoutBranch}"
  git fetch origin "${checkoutBranch}":origin/"${checkoutBranch}"
  commit_local_master="$(git show --pretty='format:%H' ${checkoutBranch})"
  commit_remote_master="$(git show --pretty='format:%H' origin/${checkoutBranch})"
  if [ "$commit_local_master" != "$commit_remote_master" ]; then
    echo "${checkoutBranch} on remote 'origin' has commits since the version under release, aborting"
    exit 1
  fi
}

echo "${GITHUB_REF}"
echo "${GITHUB_SHA}"

if [[ "$GITHUB_REF" =~ ^refs/tags/release-[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+?$ ]]; then
    version=$(echo "${GITHUB_REF}" | sed 's/^refs\/tags\/release-//')
    echo "We are on release- tag"
    echo "Releasing artifacts $version"
    prepare_signing
    safe_checkout_master_or_release
    git config user.email "jaeger-maintainers@googlegroups.com"
    git config user.name "Jaeger Release"
    ./mvnw -s ./.settings.xml --batch-mode release:prepare -Prelease -nsu -DreleaseVersion=$version -DdevelopmentVersion=999-SNAPSHOT
    ./mvnw -s ./.settings.xml release:perform
fi

