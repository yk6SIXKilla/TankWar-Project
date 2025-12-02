import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.geom.AffineTransform;

public class GameView extends JPanel implements ActionListener {
    private GameModel model;
    private GameController controller;
    private Timer timer;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JButton menuButton;
    private final int INFO_PANEL_HEIGHT = 30;

    public GameView(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        setPreferredSize(new Dimension(GameModel.MAP_WIDTH, GameModel.MAP_HEIGHT + INFO_PANEL_HEIGHT));
        setFocusable(true);
        setLayout(null);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setBounds(0, 0, GameModel.MAP_WIDTH, INFO_PANEL_HEIGHT);

        scoreLabel = new JLabel("Счет: 0");
        scoreLabel.setForeground(Color.WHITE);
        healthLabel = new JLabel(" | Жизни: 3");
        healthLabel.setForeground(Color.WHITE);

        infoPanel.add(scoreLabel);
        infoPanel.add(healthLabel);

        menuButton = new JButton("Меню");
        menuButton.setBackground(new Color(100, 100, 100));
        menuButton.setForeground(Color.WHITE);

        menuButton.setActionCommand("GO_TO_MENU");
        menuButton.addActionListener(controller);
        menuButton.setBounds(GameModel.MAP_WIDTH - 100, 2, 80, 26);
        infoPanel.add(menuButton);

        add(infoPanel);

        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.updateGame();
        if (model.getPlayerTank() != null) {
            scoreLabel.setText("Уничтожено: " + model.getScore());
            healthLabel.setText(" | HP: " + model.getPlayerTank().getHealth());
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(0, INFO_PANEL_HEIGHT);

        drawMap(g2d);

        for (Explosion exp : model.getExplosions()) drawExplosion(g2d, exp);
        if (model.getPlayerTank() != null) drawTank(g2d, model.getPlayerTank());
        for (Tank enemy : model.getEnemies()) drawTank(g2d, enemy);
        for (Bullet bullet : model.getBullets()) drawBullet(g2d, bullet);

        g2d.translate(0, -INFO_PANEL_HEIGHT);

        GameModel.GameState state = model.getCurrentState();
        if (state == GameModel.GameState.DEFEAT || state == GameModel.GameState.VICTORY) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            String message = (state == GameModel.GameState.VICTORY) ? "ПОБЕДА!" : "ИГРА ОКОНЧЕНА";
            Color msgColor = (state == GameModel.GameState.VICTORY) ? new Color(150, 255, 150) : new Color(255, 150, 150);

            g2d.setColor(msgColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 80));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(message, getWidth()/2 - fm.stringWidth(message)/2, getHeight()/2 - 100);

            ensureFinalMenuButton();
        } else {
            removeFinalMenuButton();
        }
    }

    private void ensureFinalMenuButton() {
        boolean exists = false;
        for(Component c : getComponents()) {
            if (c instanceof JButton && "GO_TO_MENU_FINAL".equals(((JButton)c).getActionCommand())) {
                exists = true; break;
            }
        }
        if (!exists) {
            JButton btn = new JButton("Вернуться в меню");
            btn.setFont(new Font("Arial", Font.BOLD, 30));
            btn.setActionCommand("GO_TO_MENU_FINAL");
            btn.addActionListener(controller);
            btn.setBounds(getWidth()/2 - 200, getHeight()/2, 400, 60);
            btn.setBackground(new Color(100, 100, 100));
            btn.setForeground(Color.WHITE);
            add(btn);
            revalidate();
        }
    }

    private void removeFinalMenuButton() {
        for(Component c : getComponents()) {
            if (c instanceof JButton && "GO_TO_MENU_FINAL".equals(((JButton)c).getActionCommand())) {
                remove(c);
                revalidate();
                repaint();
                return;
            }
        }
    }

    private void drawMap(Graphics2D g2d) {
        Color grassColor, wallColor;
        if (model.getCurrentTheme() == GameModel.MapTheme.DESERT) {
            grassColor = new Color(210, 180, 140);
            wallColor = new Color(139, 69, 19);
        } else {
            grassColor = new Color(34, 139, 34);
            wallColor = new Color(105, 105, 105);
        }

        g2d.setColor(grassColor);
        g2d.fillRect(0, 0, GameModel.MAP_WIDTH, GameModel.MAP_HEIGHT);

        g2d.setColor(wallColor);
        for(Rectangle wall : model.getObstacles()) {
            g2d.fill(wall);
            g2d.setColor(wallColor.darker());
            g2d.draw(wall);
            g2d.setColor(wallColor);
        }
    }

    private void drawTank(Graphics2D g2d, Tank tank) {
        if (!tank.isAlive()) return;
        int x = tank.getX();
        int y = tank.getY();
        int size = Tank.SIZE;
        int d = tank.getDirection();

        AffineTransform oldTransform = g2d.getTransform();
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        double angle = 0;
        if (d == 1) angle = Math.toRadians(90);
        else if (d == 2) angle = Math.toRadians(180);
        else if (d == 3) angle = Math.toRadians(270);

        g2d.rotate(angle, centerX, centerY);

        Color c = tank.getColor();

        // 1. Гусеницы (Широкие, без подложки)
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x - 8, y, 10, size); // Левая
        g2d.fillRect(x + size - 2, y, 10, size); // Правая

        // Колеса (детализация)
        g2d.setColor(new Color(80, 80, 80));
        for (int i = 0; i < 4; i++) {
            g2d.fillOval(x - 5, y + 4 + i * 10, 4, 4);
            g2d.fillOval(x + size + 1, y + 4 + i * 10, 4, 4);
        }

        // 2. Корпус (Просто цветной прямоугольник, без теней)
        g2d.setColor(c);
        g2d.fillRect(x, y + 2, size, size - 4);

        // Детализация на корпусе (чуть светлее центр)
        g2d.setColor(c.brighter());
        g2d.fillRect(x + 5, y + 5, size - 10, size - 10);

        // Обводка корпуса (Черная линия)
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y + 2, size, size - 4);

        // 3. Башня
        int turretSize = size - 10;
        int turretX = x + 5;
        int turretY = y + 5;

        g2d.setColor(c.brighter()); // Основной цвет башни
        g2d.fillOval(turretX, turretY, turretSize, turretSize);

        g2d.setColor(Color.BLACK);
        g2d.drawOval(turretX, turretY, turretSize, turretSize); // Контур

        // 4. Люк
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillOval(centerX - 5, centerY - 5, 10, 10);

        // 5. Пушка (Массивная)
        g2d.setColor(new Color(40, 40, 40));
        int barrelLength = size / 2 + 15;

        g2d.setStroke(new BasicStroke(10f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2d.drawLine(centerX, centerY, centerX, centerY - barrelLength);

        // Кончик пушки (Черный)
        g2d.setStroke(new BasicStroke(12f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2d.setColor(Color.BLACK);
        g2d.drawLine(centerX, centerY - barrelLength - 2, centerX, centerY - barrelLength + 2);

        g2d.setStroke(new BasicStroke(1f));
        g2d.setTransform(oldTransform);
    }

    private void drawBullet(Graphics2D g2d, Bullet bullet) {
        g2d.setColor(Color.ORANGE); g2d.fillOval(bullet.getX()-2, bullet.getY()-2, 10, 10);
        g2d.setColor(Color.YELLOW); g2d.fillOval(bullet.getX(), bullet.getY(), 6, 6);
    }

    private void drawExplosion(Graphics2D g2d, Explosion exp) {
        int phase = exp.getPhase();
        int x = exp.getX(); int y = exp.getY(); int size = exp.getWidth();
        g2d.setColor(Color.RED); g2d.fillOval(x + phase*2, y + phase*2, size - phase*4, size - phase*4);
        g2d.setColor(Color.ORANGE); g2d.drawOval(x, y, size, size);
    }
}