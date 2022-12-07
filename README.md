WF Subscription Management
---

This is not a complete WF, just a demo of unit testing trick for multi day delays


Build
---

    mvn test


Run continuity test
---

    mvn -Dexec.mainClass=com.xpansiv.wf.user_management.ContinuityDemoRunner -Dexec.classpathScope=test test-compile exec:java
