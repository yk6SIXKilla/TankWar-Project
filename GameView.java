import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.geom.AffineTransform;

public class GameView extends JPanel implements ActionListener {
    private GameModel model;
    private GameController controller;
    private Timer gameTimer;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private final int INFO_PANEL_HEIGHT = 30;
    private final int TURRET_SIZE_OFFSET = 10;
    private final int BARREL_LENGTH_OFFSET = 15;
    private final int BARREL_STROKE_WIDTH = 8;
    private final int BARREL_TIP_STROKE_WIDTH = 10;
    private final int EXPLOSION_PHASE_MULTIPLIER = 2;

    public GameView(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        initializeView();
        startGameTimer();
    }

    private void initializeView() {
        setPreferredSize(new Dimension(GameModel.MAP_WIDTH, GameModel.MAP_HEIGHT + INFO_PANEL_HEIGHT));
        setFocusable(true);
        setLayout(null);
        setupInfoPanel();
    }

    private void setupInfoPanel() {
        JPanel infoPanel = createInfoPanel();
        add(infoPanel);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBounds(0, 0, GameModel.MAP_WIDTH, INFO_PANEL_HEIGHT);

        scoreLabel = new JLabel("Уничтожено: 0");
        scoreLabel.setForeground(Color.WHITE);

        healthLabel = new JLabel(" | HP: 3");
        healthLabel.setForeground(Color.WHITE);

        panel.add(scoreLabel);
        panel.add(healthLabel);
        panel.add(createMenuButton());

        return panel;
    }

    private JButton createMenuButton() {
        JButton menuButton = new JButton("Меню");
        menuButton.setBackground(new Color(100, 100, 100));
        menuButton.setForeground(Color.WHITE);
        menuButton.setActionCommand("GO_TO_MENU");
        menuButton.addActionListener(controller);
        menuButton.setBounds(GameModel.MAP_WIDTH - 100, 2, 80, 26);
        return menuButton;
    }

    private void startGameTimer() {
        gameTimer = new Timer(20, this);
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.updateGame();
        updateGameInfo();
        repaint();
    }

    private void updateGameInfo() {
        if (model.getPlayerTank() != null) {
            scoreLabel.setText("Уничтожено: " + model.getScore());
            healthLabel.setText(" | HP: " + model.getPlayerTank().getHealth());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(0, INFO_PANEL_HEIGHT);
        renderGameWorld(g2d);
        g2d.translate(0, -INFO_PANEL_HEIGHT);

        renderGameOverlay(g2d);
    }

    private void renderGameWorld(Graphics2D g2d) {
        drawMap(g2d);
        drawExplosions(g2d);
        drawPlayerTank(g2d);
        drawEnemyTanks(g2d);
        drawBullets(g2d);
    }

    private void renderGameOverlay(Graphics2D g2d) {
        GameModel.GameState state = model.getCurrentState();

        if (state == GameModel.GameState.DEFEAT || state == GameModel.GameState.VICTORY) {
            drawGameEndScreen(g2d, state);
        } else {
            removeFinalMenuButton();
        }
    }

    private void drawGameEndScreen(Graphics2D g2d, GameModel.GameState state) {
        drawDarkOverlay(g2d);
        drawEndMessage(g2d, state);
        ensureFinalMenuButton();
    }

    private void drawDarkOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawEndMessage(Graphics2D g2d, GameModel.GameState state) {
        String message = (state == GameModel.GameState.VICTORY) ? "ПОБЕДА!" : "ИГРА ОКОНЧЕНА";
        Color msgColor = (state == GameModel.GameState.VICTORY) ?
                new Color(150, 255, 150) : new Color(255, 150, 150);

        g2d.setColor(msgColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 80));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(message, getWidth() / 2 - fm.stringWidth(message) / 2, getHeight() / 2 - 100);
    }

    private void ensureFinalMenuButton() {
        if (!hasFinalMenuButton()) {
            addFinalMenuButton();
        }
    }

    private boolean hasFinalMenuButton() {
        for (Component component : getComponents()) {
            if (component instanceof JButton &&
                    "GO_TO_MENU_FINAL".equals(((JButton) component).getActionCommand())) {
                return true;
            }
        }
        return false;
    }

    private void addFinalMenuButton() {
        JButton finalMenuButton = createFinalMenuButton();
        add(finalMenuButton);
        revalidate();
    }

    private JButton createFinalMenuButton() {
        JButton button = new JButton("Вернуться в меню");
        button.setFont(new Font("Arial", Font.BOLD, 30));
        button.setActionCommand("GO_TO_MENU_FINAL");
        button.addActionListener(controller);
        button.setBounds(getWidth() / 2 - 200, getHeight() / 2, 400, 60);
        button.setBackground(new Color(100, 100, 100));
        button.setForeground(Color.WHITE);
        return button;
    }

    private void removeFinalMenuButton() {
        for (Component component : getComponents()) {
            if (component instanceof JButton &&
                    "GO_TO_MENU_FINAL".equals(((JButton) component).getActionCommand())) {
                remove(component);
                revalidate();
                repaint();
                return;
            }
        }
    }

    private void drawMap(Graphics2D g2d) {
        Color[] themeColors = getMapThemeColors();
        g2d.setColor(themeColors[0]); // grass/desert color
        g2d.fillRect(0, 0, GameModel.MAP_WIDTH, GameModel.MAP_HEIGHT);
        drawObstacles(g2d, themeColors[1]);
    }

    private Color[] getMapThemeColors() {
        if (model.getCurrentTheme() == GameModel.MapTheme.DESERT) {
            return new Color[]{
                    new Color(210, 180, 140),  // desert color
                    new Color(139, 69, 19)     // wall color
            };
        } else {
            return new Color[]{
                    new Color(34, 139, 34),    // grass color
                    new Color(105, 105, 105)   // wall color
            };
        }
    }

    private void drawObstacles(Graphics2D g2d, Color wallColor) {
        g2d.setColor(wallColor);
        for (Rectangle wall : model.getObstacles()) {
            g2d.fill(wall);
            g2d.setColor(wallColor.darker());
            g2d.draw(wall);
            g2d.setColor(wallColor);
        }
    }

    private void drawExplosions(Graphics2D g2d) {
        for (Explosion exp : model.getExplosions()) {
            drawExplosion(g2d, exp);
        }
    }

    private void drawPlayerTank(Graphics2D g2d) {
        if (model.getPlayerTank() != null) {
            drawTank(g2d, model.getPlayerTank());
        }
    }

    private void drawEnemyTanks(Graphics2D g2d) {
        for (Tank enemy : model.getEnemies()) {
            drawTank(g2d, enemy);
        }
    }

    private void drawBullets(Graphics2D g2d) {
        for (Bullet bullet : model.getBullets()) {
            drawBullet(g2d, bullet);
        }
    }

    private void drawTank(Graphics2D g2d, Tank tank) {
        if (!tank.isAlive()) {
            return;
        }

        AffineTransform oldTransform = g2d.getTransform();
        applyTankRotation(g2d, tank);
        drawTankComponents(g2d, tank);
        g2d.setTransform(oldTransform);
    }

    private void applyTankRotation(Graphics2D g2d, Tank tank) {
        int centerX = tank.getX() + Tank.SIZE / 2;
        int centerY = tank.getY() + Tank.SIZE / 2;
        double rotationAngle = getRotationAngle(tank.getDirection());

        g2d.rotate(rotationAngle, centerX, centerY);
    }

    private double getRotationAngle(int direction) {
        switch (direction) {
            case 1: return Math.toRadians(90);
            case 2: return Math.toRadians(180);
            case 3: return Math.toRadians(270);
            default: return 0;
        }
    }

    private void drawTankComponents(Graphics2D g2d, Tank tank) {
        int x = tank.getX();
        int y = tank.getY();
        Color tankColor = tank.getColor();

        drawTracks(g2d, x, y);
        drawHull(g2d, x, y, tankColor);
        drawTurret(g2d, x, y, tankColor);
        drawHatch(g2d, x, y);
        drawBarrel(g2d, x, y);
    }

    private void drawTracks(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x - 8, y, 10, Tank.SIZE);
        g2d.fillRect(x + Tank.SIZE - 2, y, 10, Tank.SIZE);
        drawWheels(g2d, x, y);
    }

    private void drawWheels(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(80, 80, 80));
        for (int i = 0; i < 4; i++) {
            g2d.fillOval(x - 5, y + 4 + i * 10, 4, 4);
            g2d.fillOval(x + Tank.SIZE + 1, y + 4 + i * 10, 4, 4);
        }
    }

    private void drawHull(Graphics2D g2d, int x, int y, Color tankColor) {
        g2d.setColor(tankColor);
        g2d.fillRect(x, y + 2, Tank.SIZE, Tank.SIZE - 4);

        g2d.setColor(tankColor.brighter());
        g2d.fillRect(x + 5, y + 5, Tank.SIZE - 10, Tank.SIZE - 10);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y + 2, Tank.SIZE, Tank.SIZE - 4);
    }

    private void drawTurret(Graphics2D g2d, int x, int y, Color tankColor) {
        int turretSize = Tank.SIZE - TURRET_SIZE_OFFSET;
        int turretX = x + 5;
        int turretY = y + 5;

        g2d.setColor(tankColor.brighter());
        g2d.fillOval(turretX, turretY, turretSize, turretSize);

        g2d.setColor(Color.BLACK);
        g2d.drawOval(turretX, turretY, turretSize, turretSize);
    }

    private void drawHatch(Graphics2D g2d, int x, int y) {
        int centerX = x + Tank.SIZE / 2;
        int centerY = y + Tank.SIZE / 2;

        g2d.setColor(new Color(30, 30, 30));
        g2d.fillOval(centerX - 5, centerY - 5, 10, 10);
    }

    private void drawBarrel(Graphics2D g2d, int x, int y) {
        int centerX = x + Tank.SIZE / 2;
        int centerY = y + Tank.SIZE / 2;
        int barrelLength = Tank.SIZE / 2 + BARREL_LENGTH_OFFSET;

        drawBarrelMain(g2d, centerX, centerY, barrelLength);
        drawBarrelTip(g2d, centerX, centerY, barrelLength);
        resetStroke(g2d);
    }

    private void drawBarrelMain(Graphics2D g2d, int centerX, int centerY, int barrelLength) {
        // Отрисовка ровного цилиндрического ствола с закруглёнными концами
        int barrelWidth = BARREL_STROKE_WIDTH;

        // Основная часть ствола
        g2d.setColor(new Color(60, 60, 60)); // Тёмно-серый для ствола
        g2d.setStroke(new BasicStroke(barrelWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Рисуем линию с закруглёнными концами
        g2d.drawLine(centerX, centerY, centerX, centerY - barrelLength);

        // Добавляем тень/градиент для объёма
        g2d.setColor(new Color(80, 80, 80)); // Светлее для верхней части
        g2d.setStroke(new BasicStroke(barrelWidth - 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX, centerY - 5, centerX, centerY - barrelLength + 5);
    }

    private void drawBarrelTip(Graphics2D g2d, int centerX, int centerY, int barrelLength) {
        // Толстый кончик пушки (дульный тормоз/срез)
        int tipWidth = BARREL_TIP_STROKE_WIDTH;
        int tipLength = 8; // Длина кончика

        g2d.setColor(new Color(40, 40, 40)); // Очень тёмный для кончика
        g2d.setStroke(new BasicStroke(tipWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // Вертикальная линия кончика
        g2d.drawLine(centerX, centerY - barrelLength, centerX, centerY - barrelLength - tipLength);

        // Горизонтальные линии для объёмного эффекта (срез)
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2d.setColor(new Color(30, 30, 30)); // Ещё темнее

        // Верхняя горизонтальная линия (торцевая часть)
        int tipY = centerY - barrelLength - tipLength / 2;
        g2d.drawLine(centerX - tipWidth/2 + 1, tipY, centerX + tipWidth/2 - 1, tipY);

        // Дополнительные детали для реалистичности
        g2d.setColor(new Color(100, 100, 100)); // Светлые детали
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // Блеск на кончике
        g2d.drawLine(centerX - tipWidth/4, centerY - barrelLength - 2,
                centerX - tipWidth/4, centerY - barrelLength - tipLength + 2);
    }

    private void resetStroke(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1f));
    }

    private void drawBullet(Graphics2D g2d, Bullet bullet) {
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(bullet.getX() - 2, bullet.getY() - 2, 10, 10);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(bullet.getX(), bullet.getY(), 6, 6);
    }

    private void drawExplosion(Graphics2D g2d, Explosion exp) {
        int phase = exp.getPhase();
        int x = exp.getX();
        int y = exp.getY();
        int size = exp.getWidth();

        g2d.setColor(Color.RED);
        g2d.fillOval(x + phase * EXPLOSION_PHASE_MULTIPLIER,
                y + phase * EXPLOSION_PHASE_MULTIPLIER,
                size - phase * 4,
                size - phase * 4);

        g2d.setColor(Color.ORANGE);
        g2d.drawOval(x, y, size, size);
    }
}