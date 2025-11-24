import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.Color;
import java.awt.Rectangle;

public class GameModel {

    public enum GameState { RUNNING, DEFEAT, VICTORY, MENU }
    public enum MapTheme { SUMMER, DESERT }

    private Tank playerTank;
    private List<Tank> enemies;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    private List<Rectangle> obstacles;
    private int score = 0;
    private GameState currentState = GameState.MENU;
    private MapTheme currentTheme;

    public static final int MAP_WIDTH = 1200;
    public static final int MAP_HEIGHT = 900;
    public static final int TILE_SIZE = 50;

    private final int[][] map = new int[MAP_WIDTH / TILE_SIZE][MAP_HEIGHT / TILE_SIZE];
    private int initialEnemyCount;

    public GameModel() {}

    private void initMap(int mapId) {
        obstacles = new ArrayList<>();

        // Очистка
        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[0].length; y++) {
                map[x][y] = 0;
                // Границы карты
                if (x == 0 || x == map.length - 1 || y == 0 || y == map[0].length - 1) {
                    map[x][y] = 1;
                }
            }
        }

        // Карта 1: Лето (Открытая, ЧУТЬ БОЛЬШЕ ПРЕПЯТСТВИЙ)
        if (mapId == 1) {
            currentTheme = MapTheme.SUMMER;
            // Старые блоки
            map[5][5] = 1; map[6][5] = 1;
            map[15][5] = 1; map[15][6] = 1;
            map[10][10] = 1; map[11][10] = 1; map[12][10] = 1;
            map[5][12] = 1;
            map[18][12] = 1; map[19][12] = 1;
            map[8][3] = 1; map[8][4] = 1;

            // НОВЫЕ ПРЕПЯТСТВИЯ (добавлены две небольшие стены)
            map[3][8] = 1;
            map[3][9] = 1;

            map[16][15] = 1;
            map[17][15] = 1;
        }
        // Карта 2: Пустыня (Узкая)
        else if (mapId == 2) {
            currentTheme = MapTheme.DESERT;
            for (int x = 4; x < map.length - 4; x += 4) {
                for (int y = 3; y < map[0].length - 3; y+=3) {
                    map[x][y] = 1;
                }
            }
            map[10][8] = 1; map[11][8] = 1; map[12][8] = 1;
        }

        // Преобразуем массив карты в список прямоугольников (Hitboxes)
        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[0].length; y++) {
                if (map[x][y] == 1) {
                    obstacles.add(new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private int[] findSafePosition() {
        int maxAttempts = 200;
        for (int i = 0; i < maxAttempts; i++) {
            int x = 50 + (int) (Math.random() * (MAP_WIDTH - 100));
            int y = 50 + (int) (Math.random() * (MAP_HEIGHT - 100));
            Rectangle testRect = new Rectangle(x, y, Tank.SIZE, Tank.SIZE);

            boolean safe = true;
            for (Rectangle wall : obstacles) {
                if (testRect.intersects(wall)) {
                    safe = false;
                    break;
                }
            }
            if (safe) return new int[]{x, y};
        }
        return new int[]{100, 100};
    }

    public void initGame(int enemyCount, int mapId) {
        initMap(mapId);
        this.initialEnemyCount = enemyCount;

        int[] playerPos = findSafePosition();
        playerTank = new Tank(playerPos[0], playerPos[1], 0, Color.BLUE, true, 3);

        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        score = 0;

        for (int i = 0; i < initialEnemyCount; i++) {
            int[] enemyPos = findSafePosition();
            enemies.add(new Tank(enemyPos[0], enemyPos[1], 2, Color.RED.darker(), false, 1));
        }

        currentState = GameState.RUNNING;
    }

    public void updateGame() {
        if (currentState != GameState.RUNNING) return;

        bullets.forEach(Bullet::update);
        explosions.forEach(Explosion::update);

        if(playerTank.isAlive() && playerTank.isMovingForward()) {
            processTankMovement(playerTank, 5);
        }

        for (Tank enemy : enemies) {
            enemy.update();
            if (enemy.isAlive() && enemy.isMovingForward()) {
                processTankMovement(enemy, 3);
            }
            if (enemy.isAlive() && enemy.canShoot() && Math.random() < 0.05) {
                if (isPlayerInLineOfSight(enemy, playerTank, 500)) {
                    Bullet newBullet = enemy.shoot();
                    if (newBullet != null) bullets.add(newBullet);
                }
            }
        }

        checkCollisions();

        enemies.removeIf(tank -> !tank.isAlive());
        bullets.removeIf(bullet -> !bullet.isAlive());
        explosions.removeIf(exp -> !exp.isAlive());

        if (score >= initialEnemyCount && initialEnemyCount > 0) {
            currentState = GameState.VICTORY;
        }
        if (playerTank != null && !playerTank.isAlive()) {
            currentState = GameState.DEFEAT;
        }
    }

    private void processTankMovement(Tank tank, int step) {
        Rectangle future = tank.getFutureBounds(step);

        if (future.x < 0 || future.x + future.width > MAP_WIDTH ||
                future.y < 0 || future.y + future.height > MAP_HEIGHT) {
            if (!tank.isPlayer()) tank.setDirection((int)(Math.random()*4));
            return;
        }

        boolean collision = false;
        for (Rectangle wall : obstacles) {
            if (future.intersects(wall)) {
                collision = true;
                break;
            }
        }

        if (!collision) {
            tank.setPosition(future.x, future.y);
        } else {
            if (!tank.isPlayer()) {
                tank.setDirection((int) (Math.random() * 4));
            }
        }
    }

    private boolean isPlayerInLineOfSight(Tank shooter, Tank target, int range) {
        int dx = Math.abs(shooter.getX() - target.getX());
        int dy = Math.abs(shooter.getY() - target.getY());
        return (dx < 50 && dy < range) || (dy < 50 && dx < range);
    }

    private void checkCollisions() {
        List<Bullet> currentBullets = new ArrayList<>(bullets);
        for (Bullet bullet : currentBullets) {
            if (!bullet.isAlive()) continue;

            Rectangle bRect = bullet.getBounds();

            for (Rectangle wall : obstacles) {
                if (bRect.intersects(wall)) {
                    bullet.setAlive(false);
                    explosions.add(new Explosion(bullet.getX() - 15, bullet.getY() - 15));
                    break;
                }
            }
            if (!bullet.isAlive()) continue;

            if (bullet.isPlayerBullet()) {
                for (Tank enemy : enemies) {
                    if (enemy.isAlive() && enemy.getBounds().intersects(bRect)) {
                        enemy.takeDamage();
                        bullet.setAlive(false);
                        if (!enemy.isAlive()) {
                            score++;
                            explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        }
                        break;
                    }
                }
            } else {
                if (playerTank != null && playerTank.isAlive() && playerTank.getBounds().intersects(bRect)) {
                    playerTank.takeDamage();
                    bullet.setAlive(false);
                    explosions.add(new Explosion(playerTank.getX(), playerTank.getY()));
                }
            }
        }
    }

    public int[][] getMap() { return map; }
    public List<Rectangle> getObstacles() { return obstacles; }
    public List<Tank> getEnemies() { return Collections.unmodifiableList(enemies); }
    public List<Bullet> getBullets() { return Collections.unmodifiableList(bullets); }
    public List<Explosion> getExplosions() { return Collections.unmodifiableList(explosions); }
    public Tank getPlayerTank() { return playerTank; }
    public int getScore() { return score; }
    public GameState getCurrentState() { return currentState; }
    public MapTheme getCurrentTheme() { return currentTheme; }
    public void setPlayerMoving(boolean isMoving) {
        if (playerTank != null && playerTank.isAlive()) playerTank.setMoving(isMoving);
    }
    public void setPlayerDirection(int direction) {
        if (playerTank != null && playerTank.isAlive()) playerTank.setDirection(direction);
    }
    public void playerShoot() {
        if (playerTank != null && playerTank.isAlive()) {
            Bullet newBullet = playerTank.shoot();
            if (newBullet != null) bullets.add(newBullet);
        }
    }
}