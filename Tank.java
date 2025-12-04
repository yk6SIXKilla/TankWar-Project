import java.awt.Color;
import java.awt.Rectangle;

public class Tank extends GameObject {
    public static final int SIZE = 40;
    private static final long SHOOT_COOLDOWN_MS = 600;
    private static final int AI_DIRECTION_CHANGE_PROBABILITY = 2;
    private static final int BULLET_OFFSET = 15;

    private int direction;
    private int health;
    private Color color;
    private boolean isPlayer;
    private long lastShotTime = 0;
    private boolean isMovingForward = false;

    public Tank(int x, int y, int direction, Color color, boolean isPlayer, int initialHealth) {
        super(x, y, SIZE, SIZE);
        this.direction = direction;
        this.color = color;
        this.isPlayer = isPlayer;
        this.health = initialHealth;
        this.isMovingForward = !isPlayer;
    }

    @Override
    public void update() {
        if (!isPlayer && Math.random() * 100 < AI_DIRECTION_CHANGE_PROBABILITY) {
            changeRandomDirection();
        }
    }

    private void changeRandomDirection() {
        direction = (int) (Math.random() * 4);
    }

    public Rectangle getFutureBounds(int movementStep) {
        int futureX = x;
        int futureY = y;

        switch (direction) {
            case 0: futureY -= movementStep; break;
            case 1: futureX += movementStep; break;
            case 2: futureY += movementStep; break;
            case 3: futureX -= movementStep; break;
        }
        return new Rectangle(futureX, futureY, SIZE, SIZE);
    }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    private boolean isShootCooldownPassed() {
        return System.currentTimeMillis() - lastShotTime > SHOOT_COOLDOWN_MS;
    }

    public boolean canShoot() { // Изменен модификатор доступа с private на public
        return isShootCooldownPassed();
    }

    public Bullet shoot() {
        if (!canShoot()) {
            return null;
        }

        lastShotTime = System.currentTimeMillis();
        int[] bulletPosition = calculateBulletStartPosition();

        return new Bullet(bulletPosition[0], bulletPosition[1], direction, isPlayer);
    }

    private int[] calculateBulletStartPosition() {
        int bulletX = x + SIZE / 2 - Bullet.SIZE / 2;
        int bulletY = y + SIZE / 2 - Bullet.SIZE / 2;

        switch (direction) {
            case 0: bulletY -= SIZE / 2 + BULLET_OFFSET; break;
            case 1: bulletX += SIZE / 2 + BULLET_OFFSET; break;
            case 2: bulletY += SIZE / 2 + BULLET_OFFSET; break;
            case 3: bulletX -= SIZE / 2 + BULLET_OFFSET; break;
        }

        return new int[]{bulletX, bulletY};
    }

    public void takeDamage() {
        health--;
        if (health <= 0) {
            isAlive = false;
        }
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setMoving(boolean moving) {
        this.isMovingForward = moving;
    }

    public boolean isMovingForward() {
        return isMovingForward;
    }

    public int getHealth() {
        return health;
    }

    public Color getColor() {
        return color;
    }

    public boolean isPlayer() {
        return isPlayer;
    }
}