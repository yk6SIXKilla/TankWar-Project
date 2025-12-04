public class Explosion extends GameObject {
    private static final int INITIAL_LIFETIME = 12;
    private static final int PHASE_DIVIDER = 2;

    private int lifetime = INITIAL_LIFETIME;

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
        return (INITIAL_LIFETIME - lifetime) / PHASE_DIVIDER + 1;
    }
}