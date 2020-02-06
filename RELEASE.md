# Release

Release is automated on CI and is triggered by pushing a tag: `git tag release-0.4.0 && git push origin release-0.4.0`.

## Create signing-key.asc

gpg --full-generate-key
passphrase: pass1
Get public Key: gpg --armor --export E56F3940BC80201B7CBB4AD30382F3FAF1889185 
Get private Key: gpg --armor --export-secret-keys E56F3940BC80201B7CBB4AD30382F3FAF1889185 > signing-key.asc

Encrypt signing key. Choose new passphrase and store it as secret in Github actions
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
gpg  --send-key  E56F3940BC80201B7CBB4AD30382F3FAF1889185 
```

## Releasing locally
```
GH_TOKEN= GH_USER= PASSPHRASE_SIGING_KEY= mvn -s ./.settings.xml --batch-mode release:prepare -Prelease -nsu -DreleaseVersion="<version>"
SONATYPE_USER= SONATYPE_PASSWORD= mvn -s ./.settings.xml release:perform
```
