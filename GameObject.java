import java.awt.Rectangle;

public abstract class GameObject {
    protected int x, y;
    protected int width, height;
    protected boolean isAlive = true;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update();

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}