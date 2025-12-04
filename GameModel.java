import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.Color;
import java.awt.Rectangle;

public class GameModel {
    public enum GameState { RUNNING, DEFEAT, VICTORY, MENU }
    public enum MapTheme { SUMMER, DESERT }

    public static final int MAP_WIDTH = 1200;
    public static final int MAP_HEIGHT = 900;
    public static final int TILE_SIZE = 50;
    private static final int MAX_ENEMY_COUNT = 50;
    private static final int DEFAULT_ENEMY_COUNT = 5;
    private static final int AI_SHOOT_PROBABILITY = 5;
    private static final int LINE_OF_SIGHT_RANGE = 500;
    private static final int PLAYER_MOVEMENT_STEP = 5;
    private static final int ENEMY_MOVEMENT_STEP = 3;
    private static final int MAX_POSITION_ATTEMPTS = 200;
    private static final int MIN_SAFE_DISTANCE = 50;

    private Tank playerTank;
    private List<Tank> enemies;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    private List<Rectangle> obstacles;
    private int score = 0;
    private GameState currentState = GameState.MENU;
    private MapTheme currentTheme;
    private int[][] map;
    private int initialEnemyCount;
    private GameLogicProcessor logicProcessor;

    public GameModel() {
        map = new int[MAP_WIDTH / TILE_SIZE][MAP_HEIGHT / TILE_SIZE];
        logicProcessor = new GameLogicProcessor(this);
    }

    public void initGame(int enemyCount, int mapId) {
        initializeMap(mapId);
        this.initialEnemyCount = Math.min(Math.max(1, enemyCount), MAX_ENEMY_COUNT);

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

    private void initializeMap(int mapId) {
        obstacles = new ArrayList<>();
        resetMapAndBorders();

        if (mapId == 1) {
            currentTheme = MapTheme.SUMMER;
            setupSummerMap();
        } else if (mapId == 2) {
            currentTheme = MapTheme.DESERT;
            setupDesertMap();
        }

        buildObstacleHitboxes();
    }

    private void resetMapAndBorders() {
        int mapWidth = map.length;
        int mapHeight = map[0].length;

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = 0;
                if (x == 0 || x == mapWidth - 1 || y == 0 || y == mapHeight - 1) {
                    map[x][y] = 1;
                }
            }
        }
    }

    private void setupSummerMap() {
        map[5][5] = 1; map[6][5] = 1;
        map[15][5] = 1; map[15][6] = 1;
        map[10][10] = 1; map[11][10] = 1; map[12][10] = 1;
        map[5][12] = 1;
        map[18][12] = 1; map[19][12] = 1;
        map[8][3] = 1; map[8][4] = 1;
        map[3][8] = 1; map[3][9] = 1;
        map[16][15] = 1; map[17][15] = 1;
    }

    private void setupDesertMap() {
        for (int x = 4; x < map.length - 4; x += 4) {
            for (int y = 3; y < map[0].length - 3; y += 3) {
                map[x][y] = 1;
            }
        }
        map[10][8] = 1; map[11][8] = 1; map[12][8] = 1;
    }

    private void buildObstacleHitboxes() {
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (map[x][y] == 1) {
                    obstacles.add(new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private int[] findSafePosition() {
        for (int attempt = 0; attempt < MAX_POSITION_ATTEMPTS; attempt++) {
            int x = MIN_SAFE_DISTANCE + (int) (Math.random() * (MAP_WIDTH - 2 * MIN_SAFE_DISTANCE));
            int y = MIN_SAFE_DISTANCE + (int) (Math.random() * (MAP_HEIGHT - 2 * MIN_SAFE_DISTANCE));
            Rectangle testRect = new Rectangle(x, y, Tank.SIZE, Tank.SIZE);

            if (isPositionSafe(testRect)) {
                return new int[]{x, y};
            }
        }
        return new int[]{100, 100};
    }

    private boolean isPositionSafe(Rectangle position) {
        for (Rectangle wall : obstacles) {
            if (position.intersects(wall)) {
                return false;
            }
        }
        return true;
    }

    public void updateGame() {
        if (currentState != GameState.RUNNING) {
            return;
        }

        logicProcessor.updateGameState();
    }

    public void playerShoot() {
        if (playerTank != null && playerTank.isAlive()) {
            Bullet newBullet = playerTank.shoot();
            if (newBullet != null) {
                bullets.add(newBullet);
            }
        }
    }

    // Геттеры
    public int[][] getMap() {
        return map.clone();
    }

    public List<Rectangle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public List<Tank> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    public List<Explosion> getExplosions() {
        return Collections.unmodifiableList(explosions);
    }

    public Tank getPlayerTank() {
        return playerTank;
    }

    public int getScore() {
        return score;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public MapTheme getCurrentTheme() {
        return currentTheme;
    }

    public void setPlayerMoving(boolean isMoving) {
        if (playerTank != null && playerTank.isAlive()) {
            playerTank.setMoving(isMoving);
        }
    }

    public void setPlayerDirection(int direction) {
        if (playerTank != null && playerTank.isAlive()) {
            playerTank.setDirection(direction);
        }
    }

    // Внутренний класс для обработки игровой логики
    class GameLogicProcessor {
        private GameModel model;

        public GameLogicProcessor(GameModel model) {
            this.model = model;
        }

        public void updateGameState() {
            updateEntities();
            handleMovements();
            handleAIShooting();
            checkCollisions();
            cleanupEntities();
            checkGameStatus();
        }

        private void updateEntities() {
            bullets.forEach(Bullet::update);
            explosions.forEach(Explosion::update);
        }

        private void handleMovements() {
            if (playerTank.isAlive() && playerTank.isMovingForward()) {
                processTankMovement(playerTank, PLAYER_MOVEMENT_STEP);
            }

            for (Tank enemy : enemies) {
                enemy.update();
                if (enemy.isAlive() && enemy.isMovingForward()) {
                    processTankMovement(enemy, ENEMY_MOVEMENT_STEP);
                }
            }
        }

        private void handleAIShooting() {
            for (Tank enemy : enemies) {
                if (enemy.isAlive() && enemy.canShoot() && Math.random() * 100 < AI_SHOOT_PROBABILITY) {
                    if (isPlayerInLineOfSight(enemy, playerTank)) {
                        Bullet newBullet = enemy.shoot();
                        if (newBullet != null) {
                            bullets.add(newBullet);
                        }
                    }
                }
            }
        }

        private void checkCollisions() {
            List<Bullet> currentBullets = new ArrayList<>(bullets);

            for (Bullet bullet : currentBullets) {
                if (!bullet.isAlive()) {
                    continue;
                }

                checkWallCollision(bullet);

                if (!bullet.isAlive()) {
                    continue;
                }

                if (bullet.isPlayerBullet()) {
                    checkPlayerBulletCollision(bullet);
                } else {
                    checkEnemyBulletCollision(bullet);
                }
            }
        }

        private void checkWallCollision(Bullet bullet) {
            Rectangle bulletRect = bullet.getBounds();

            for (Rectangle wall : obstacles) {
                if (bulletRect.intersects(wall)) {
                    bullet.setAlive(false);
                    explosions.add(new Explosion(bullet.getX() - 15, bullet.getY() - 15));
                    return;
                }
            }
        }

        private void checkPlayerBulletCollision(Bullet bullet) {
            Rectangle bulletRect = bullet.getBounds();

            for (Tank enemy : enemies) {
                if (enemy.isAlive() && enemy.getBounds().intersects(bulletRect)) {
                    enemy.takeDamage();
                    bullet.setAlive(false);

                    if (!enemy.isAlive()) {
                        score++;
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    }
                    return;
                }
            }
        }

        private void checkEnemyBulletCollision(Bullet bullet) {
            if (playerTank != null && playerTank.isAlive() &&
                    playerTank.getBounds().intersects(bullet.getBounds())) {
                playerTank.takeDamage();
                bullet.setAlive(false);
                explosions.add(new Explosion(playerTank.getX(), playerTank.getY()));
            }
        }

        private void cleanupEntities() {
            enemies.removeIf(tank -> !tank.isAlive());
            bullets.removeIf(bullet -> !bullet.isAlive());
            explosions.removeIf(exp -> !exp.isAlive());
        }

        private void checkGameStatus() {
            if (score >= initialEnemyCount && initialEnemyCount > 0) {
                currentState = GameState.VICTORY;
            }

            if (playerTank != null && !playerTank.isAlive()) {
                currentState = GameState.DEFEAT;
            }
        }

        private void processTankMovement(Tank tank, int step) {
            Rectangle futureBounds = tank.getFutureBounds(step);

            if (isOutOfBounds(futureBounds)) {
                if (!tank.isPlayer()) {
                    tank.setDirection((int) (Math.random() * 4));
                }
                return;
            }

            if (hasWallCollision(futureBounds)) {
                if (!tank.isPlayer()) {
                    tank.setDirection((int) (Math.random() * 4));
                }
                return;
            }

            tank.setPosition(futureBounds.x, futureBounds.y);
        }

        private boolean isOutOfBounds(Rectangle bounds) {
            return bounds.x < 0 || bounds.x + bounds.width > MAP_WIDTH ||
                    bounds.y < 0 || bounds.y + bounds.height > MAP_HEIGHT;
        }

        private boolean hasWallCollision(Rectangle bounds) {
            for (Rectangle wall : obstacles) {
                if (bounds.intersects(wall)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isPlayerInLineOfSight(Tank shooter, Tank target) {
            int dx = Math.abs(shooter.getX() - target.getX());
            int dy = Math.abs(shooter.getY() - target.getY());
            return (dx < 50 && dy < LINE_OF_SIGHT_RANGE) ||
                    (dy < 50 && dx < LINE_OF_SIGHT_RANGE);
        }
    }
}