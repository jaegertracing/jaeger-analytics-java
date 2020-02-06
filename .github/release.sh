#!/usr/bin/bash

prepare_signing() {
  echo "Decrypting private GPG key"
  gpg --quiet --batch --yes --decrypt --passphrase=${PASSPHRASE_SIGNING_KEY_ENCRYPT} --output .github/signing-key.asc signing-key.asc.gpg
  echo "Importing GPG key to system"
  gpg --no-tty --batch --allow-secret-key-import --import ./github/signing-key.asc
}

set -o errexit -o nounset -o pipefail

safe_checkout_master_or_release() {
  # We need to be on a branch for release:perform to be able to create commits,
  # and we want that branch to be master or release-X.Y, which has been checked before.
  # But we also want to make sure that we build and release exactly the tagged version, so we verify that the remote
  # branch is where our tag is.
  checkoutBranch=$(echo ${GITHUB_REF} | sed 's/.[[:digit:]]\+$//')
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

echo "$GITHUB_REF"
echo "$GITHUB_SHA"

if [[ "$GITHUB_REF" =~ ^release-[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+?$ ]]; then
    echo "We are on release- tag"
    echo "Releasing artifacts"
    prepare_signing
    safe_checkout_master_or_release
    version=$(echo "${GITHUB_REF}" | sed 's/^release-//')
    ./mvnw -s ./.settings.xml --batch-mode release:prepare -Prelease -nsu -DreleaseVersion=$version
    ./mvnw -s ./.settings.xml release:perform
fi

