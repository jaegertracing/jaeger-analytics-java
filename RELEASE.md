# Release

Release is automated on CI and is triggered by pushing a tag: `git tag release-0.4.0 && git push origin release-0.4.0`.
Make sure not changes are make to 

## Maintenance branches

Use `release-M.N` naming convention for maintenance branches.

## Releasing locally

Use local release as a backup solution:

```
GH_TOKEN= GH_USER= PASSPHRASE_SIGING_KEY= ./mvnw -s ./.settings.xml --batch-mode release:prepare -Prelease -nsu -DreleaseVersion="<version>" #-Dgpg.passphrase=
SONATYPE_USER= SONATYPE_PASSWORD= ./mvnw -s ./.settings.xml release:perform
```

## Create signing-key.asc

This section describes how to create private GPG key and encrypt it so it can be
checked into source code and used by public CI.

```
gpg --full-generate-key
# get public key
gpg --armor --export E56F3940BC80201B7CBB4AD30382F3FAF1889185 
# get private key
gpg --armor --export-secret-keys E56F3940BC80201B7CBB4AD30382F3FAF1889185 > signing-key.asc
```
Passphrase store as secret in Github actions.

Encrypt signing key and choose new passphrase and store it as secret in Github actions:
```
gpg --symmetric --cipher-algo AES256 signing-key.asc
# passphrase: pass2
```

Test key decryption:
```
gpg --quiet --batch --yes --decrypt --passphrase="pass2" --output /tmp/hoo.asc signing-key.asc.gpg
```

Publish key:
```
gpg --keyserver keys.openpgp.org --send-key E56F3940BC80201B7CBB4AD30382F3FAF1889185
```
