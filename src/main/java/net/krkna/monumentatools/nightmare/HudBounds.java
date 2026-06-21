package net.krkna.monumentatools.nightmare;

public record HudBounds(int x, int y, int width, int height) {
    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }
}
