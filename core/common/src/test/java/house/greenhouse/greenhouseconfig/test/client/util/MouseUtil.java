package house.greenhouse.greenhouseconfig.test.client.util;

public class MouseUtil {
    public static boolean inBounds(double mouseX, double mouseY, float startX, float startY, float xSize, float ySize)  {
        return mouseX > startX && mouseX < startX + xSize && mouseY > startY && mouseY < startY + ySize;
    }
}
