package com.example.xtremednotes.util;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public class VerifyUtil {
    static class VerifyInstance {
        Function<String, Boolean> verifier;
        String message;
        boolean isLetter;
        int req;
        VerifyInstance(Function<String, Boolean> v, String m, int req, boolean isLetter) {
            this.verifier = v;
            this.message = m;
            this.req = req;
            this.isLetter = isLetter;
        }
    }

    private static boolean runVerify(String password, VerifyInstance vi) {
        if (vi.isLetter) {
            int res = 0;
            for (int i = 0; i < password.length(); i++) {
                if (vi.verifier.apply(password.substring(i, i + 1))) {
                    res += 1;
                }
            }
            return res >= vi.req;
        } else {
            return vi.verifier.apply(password);
        }
    }

    private static final VerifyInstance[] verifiers = {
            new VerifyInstance((String c) -> !c.matches("[a-zA-Z]"), "have 2 special characters", 2, true),
            new VerifyInstance((String c) -> c.matches("[a-z]"), "have 2 lowercase letters", 2, true),
            new VerifyInstance((String c) -> c.matches("[A-Z]"), "have 2 uppercase letters", 2, true),
            new VerifyInstance((String s) -> s.length() >= 10, "have 10 characters", 1, false),
    };

    public static String verifyPassword(String password) {

        String message = null;
        for (VerifyInstance vi : verifiers) {
            if (!runVerify(password, vi)) {
                if (message == null) {
                    message = "Password should:\n" + vi.message;
                } else {
                    message += ",\n" + vi.message;
                }
            }
        }
        if (message != null) {
            message += ".";
        }
        return message;
    }
}
