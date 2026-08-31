---
description: Run the reactividad Maven test suite with the JDK 17 override this machine needs.
argument-hint: [TestClass[#method]]
---

Run this project's test suite via the Bash tool.

This machine's default JAVA_HOME points at JDK 8, which breaks the build (parser errors
unrelated to any real change) — always override it to the JDK 17 install documented in
CLAUDE.md's Testing policy: `/c/Users/oscar.vega/.jdks/jbr-17.0.14`.

Target: $ARGUMENTS

- If a target was given above (a test class name, optionally `Class#method`), run only
  that: `JAVA_HOME=/c/Users/oscar.vega/.jdks/jbr-17.0.14 ./mvnw test -Dtest=<target>`.
- If no target was given, run the full suite:
  `JAVA_HOME=/c/Users/oscar.vega/.jdks/jbr-17.0.14 ./mvnw test`.

Report whether it passed with zero failures/errors, per CLAUDE.md's Testing policy. If
anything failed, summarize the failing test(s) and the relevant error.
