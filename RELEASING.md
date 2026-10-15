# Releasing

1. Bump the version in `pom.xml` and merge it to `main`.
2. Tag that commit and push the tag:

   ```sh
   git tag v0.1.1 && git push origin v0.1.1
   ```

3. The Publish workflow checks that the tag matches the version, runs the checks, and
   waits for a maintainer to approve the `release` environment. Approve it in the
   workflow run.

Published versions are immutable, and the repository blocks moving or deleting `v*`
tags. To fix a bad release, publish a new version.

## One-time setup

Maven Central has no trusted publishing, so the `release` environment holds four
secrets. `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` are a user token from
central.sonatype.com. `MAVEN_GPG_PRIVATE_KEY` is the armored private key from
`gpg --armor --export-secret-keys <KEY_ID>`, and `MAVEN_GPG_PASSPHRASE` is its passphrase.
The public key must be on keyserver.ubuntu.com, and the `com.ternwire` namespace must be
verified on central.sonatype.com.

The upload does not go live by itself. Open Deployments on central.sonatype.com and
publish it there.
