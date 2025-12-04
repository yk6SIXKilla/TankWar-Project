public class Bullet extends GameObject {
    public static final int SIZE = 6;
    private static final int SPEED = 15;

    private int direction;
    private boolean isPlayerBullet;

    public Bullet(int x, int y, int direction, boolean isPlayerBullet) {
        super(x, y, SIZE, SIZE);
        this.direction = direction;
        this.isPlayerBullet = isPlayerBullet;
    }

    @Override
    public void update() {
        move();
        checkBoundaries();
    }

    private void move() {
        switch (direction) {
            case 0: y -= SPEED; break;
            case 1: x += SPEED; break;
            case 2: y += SPEED; break;
            case 3: x -= SPEED; break;
        }
    }

    private void checkBoundaries() {
        if (x < 0 || x > GameModel.MAP_WIDTH || y < 0 || y > GameModel.MAP_HEIGHT) {
            isAlive = false;
        }
    }

    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }
}