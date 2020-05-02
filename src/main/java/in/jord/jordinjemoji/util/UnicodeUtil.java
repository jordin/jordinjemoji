package in.jord.jordinjemoji.util;

public final class UnicodeUtil {
    public static String codePointsToUnicode(final String name) {
        final String[] codePointsStr = name.split("-");
        final int[] codePoints = new int[codePointsStr.length];

        for (int index = 0; index < codePoints.length; index++) {
            codePoints[index] = Integer.parseInt(codePointsStr[index], 0x10);
        }

        return new String(codePoints, 0, codePoints.length);
    }
}
