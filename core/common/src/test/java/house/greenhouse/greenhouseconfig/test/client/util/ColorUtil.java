package house.greenhouse.greenhouseconfig.test.client.util;

import net.minecraft.util.Mth;

public class ColorUtil {
    public static float[] rgbToHsv(float red, float green, float blue) {
        float hue;
        float saturation;
        float brightness;
        float cMax = Math.max(red, green);
        if (blue > cMax) cMax = blue;
        float cMin = Math.min(red, green);
        if (blue < cMin) cMin = blue;

        brightness = cMax / 255.0F;
        if (cMax > 0)
            saturation = (cMax - cMin) / cMax;
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redC = (cMax - red) / (cMax - cMin);
            float greenC = (cMax - green) / (cMax - cMin);
            float blueC = (cMax - blue) / (cMax - cMin);
            if (red == cMax)
                hue = blueC - greenC;
            else if (green == cMax)
                hue = 2.0f + redC - blueC;
            else
                hue = 4.0f + greenC - redC;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        float[] hsb = new float[3];
        hsb[0] = hue;
        hsb[1] = saturation;
        hsb[2] = brightness;
        return hsb;
    }

    /**
     * Converts a HSB value to ARGB.
     *
     * @param hue The hue value
     * @param saturation The saturation value
     * @param brightness The brightness value
     * @return An array of 3 floats ordered as: red, green, blue.
     */
    public static float[] hsvToRgb(float hue, float saturation, float brightness) {
        if (saturation < 0.01F)
            return new float[] { brightness, brightness, brightness };
        float sector = (hue % 1) * 6;
        float offSetInSector = sector - Mth.floor(sector);
        float off = brightness * (1 - saturation);
        float fadeOut = brightness * (1 - saturation * offSetInSector);
        float fadeIn = brightness * (1 - saturation * (1 - offSetInSector));

        float[] rgb = new float[3];
        switch ((int)sector) {
            case 0, 6 -> {
                rgb[0] = brightness;
                rgb[1] = fadeIn;
                rgb[2] = off;
            }
            case 1 -> {
                rgb[0] = fadeOut;
                rgb[1] = brightness;
                rgb[2] = off;
            }
            case 2 -> {
                rgb[0] = off;
                rgb[1] = brightness;
                rgb[2] = fadeIn;
            }
            case 3 -> {
                rgb[0] = off;
                rgb[1] = fadeOut;
                rgb[2] = brightness;
            }
            case 4 -> {
                rgb[0] = fadeIn;
                rgb[1] = off;
                rgb[2] = brightness;
            }
            case 5 -> {
                rgb[0] = brightness;
                rgb[1] = off;
                rgb[2] = fadeOut;
            }
            default -> throw new UnsupportedOperationException("Invalid color wheel sector: " + sector);
        }

        return rgb;
    }
}
