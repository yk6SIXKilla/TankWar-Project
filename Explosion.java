public class Explosion extends GameObject {
    private int lifetime = 12;

    public Explosion(int x, int y) {
        super(x, y, Tank.SIZE, Tank.SIZE);
    }

    @Override
    public void update() {
        lifetime--;
        if (lifetime <= 0) {
            isAlive = false;
        }
    }

    public int getPhase() {
        return (12 - lifetime) / 2 + 1;
    }
}