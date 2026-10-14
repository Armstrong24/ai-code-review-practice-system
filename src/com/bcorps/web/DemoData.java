package com.bcorps.web;

import com.bcorps.app.Platform;
import java.util.Locale;

/**
 * Creates a demo student so anyone with the link can try the app at once.
 * Email: demo@codereview.app   Password: demo1234
 * Set DEMO_ACCOUNT=false to turn this off.
 */
final class DemoData {
    static final String EMAIL = "demo@codereview.app";
    static final String PASSWORD = "demo1234";

    private DemoData() { }

    static void ensureDemoAccount(Platform platform) throws Exception {
        String flag = System.getenv("DEMO_ACCOUNT");
        if (flag != null && flag.strip().toLowerCase(Locale.ROOT).equals("false")) {
            return;
        }
        if (platform.auth().login(EMAIL, PASSWORD.toCharArray()).isEmpty()
                && !platform.students().emailExists(EMAIL)) {
            platform.auth().register("Demo Student", EMAIL, PASSWORD.toCharArray());
        }
    }
}
