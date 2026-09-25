// FILE: src/main/java/com/impostorparty/util/RoomCodeGenerator.java
package com.impostorparty.util;

import java.security.SecureRandom;

public class RoomCodeGenerator {

    // Sin caracteres ambiguos (0/O, 1/I) para que sea facil de leer/escribir a mano
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}