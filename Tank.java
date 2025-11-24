import java.awt.Color;
import java.awt.Rectangle;

public class Tank extends GameObject {
    public static final int SIZE = 40;
    private int direction;
    private int health;
    private Color color;
    private boolean isPlayer;
    private long lastShotTime = 0;
    private final long SHOOT_COOLDOWN = 600;

    private boolean movingForward = false;

    public Tank(int x, int y, int direction, Color color, boolean isPlayer, int initialHealth) {
        super(x, y, SIZE, SIZE);
        this.direction = direction;
        this.color = color;
        this.isPlayer = isPlayer;
        this.health = initialHealth;
        this.movingForward = !isPlayer;
    }

    @Override
    public void update() {
        if (!isPlayer && Math.random() < 0.02) {
            direction = (int) (Math.random() * 4);
        }
    }

    public Rectangle getFutureBounds(int step) {
        int newX = x;
        int newY = y;

        switch (direction) {
            case 0: newY -= step; break;
            case 1: newX += step; break;
            case 2: newY += step; break;
            case 3: newX -= step; break;
        }
        return new Rectangle(newX, newY, SIZE, SIZE);
    }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime > SHOOT_COOLDOWN;
    }

    public Bullet shoot() {
        if (!canShoot()) return null;
        lastShotTime = System.currentTimeMillis();

        int bX = x + SIZE / 2 - Bullet.SIZE / 2;
        int bY = y + SIZE / 2 - Bullet.SIZE / 2;
        int offset = SIZE / 2 + 15;

        if (direction == 0) bY -= offset;
        else if (direction == 1) bX += offset;
        else if (direction == 2) bY += offset;
        else if (direction == 3) bX -= offset;

        return new Bullet(bX, bY, direction, isPlayer);
    }

    public void takeDamage() {
        health--;
        if (health <= 0) isAlive = false;
    }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }
    public void setMoving(boolean moving) { this.movingForward = moving; }
    public boolean isMovingForward() { return movingForward; }
    public int getHealth() { return health; }
    public Color getColor() { return color; }
    public boolean isPlayer() { return isPlayer; }
}