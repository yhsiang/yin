package org.yinwang.yin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

public class _ {

    @Nullable
    public static String readFile(@NotNull String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
        } catch (IOException e) {
            return null;
        }
    }


    public static void msg(String m) {
        System.out.println(m);
    }


    public static void abort(String m) {
        System.err.println(m);
        System.err.flush();
        Thread.dumpStack();
        System.exit(1);
    }


    @NotNull
    public static String joinWithSep(@NotNull Collection<? extends Object> ls, String sep) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object s : ls) {
            if (i > 0) {
                sb.append(sep);
            }
            sb.append(s.toString());
            i++;
        }
        return sb.toString();
    }

}
