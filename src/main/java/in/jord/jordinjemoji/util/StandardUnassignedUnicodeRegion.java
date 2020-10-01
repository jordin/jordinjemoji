package in.jord.jordinjemoji.util;

public enum StandardUnassignedUnicodeRegion {
    PRIVATE_USE_AREA(0xE000),
    SUPPLEMENTARY_PRIVATE_USE_AREA_A(0xF0000),
    SUPPLEMENTARY_PRIVATE_USE_AREA_B(0x100000);

    private final int baseCodePoint;

    StandardUnassignedUnicodeRegion(int baseCodePoint) {
        this.baseCodePoint = baseCodePoint;
    }

    public int getBaseCodePoint() {
        return this.baseCodePoint;
    }
}
