# Security Policy

## Supported versions

PyxisSapiens is under active development. Security fixes are applied to the latest release and the
`main` branch.

## Reporting a vulnerability

Please **do not** open a public issue for security problems. Instead, report privately via GitHub's
**"Report a vulnerability"** (Security → Advisories) on the repository, or contact the maintainers
listed in `CODEOWNERS`.

Include:

- a description of the issue and its impact,
- steps to reproduce,
- affected version/commit,
- any suggested mitigation.

We aim to acknowledge reports within a few days and will coordinate a fix and disclosure.

## Scope & data protection

PyxisSapiens is **offline-first** and privacy-preserving by design:

- no accounts, no telemetry, no third-party tracking SDKs;
- the local database is encrypted (SQLCipher with an Android Keystore-wrapped key);
- export archives can be encrypted (AES-GCM + PBKDF2).

Relevant areas for security review include the database key handling, archive encryption, the local
MBTiles tile server (binds to `127.0.0.1` only), and file sharing via the `FileProvider`.

## Dependencies

Dependencies are updated via Dependabot; please report known-vulnerable dependencies as an issue if
Dependabot has not already raised one.
