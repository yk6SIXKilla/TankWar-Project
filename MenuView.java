import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MenuView extends JPanel {
    private JTextField enemyCountField;
    private JComboBox<String> mapChooser;
    private JButton startButton;

    public MenuView(ActionListener controller) {
        setLayout(new GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Градиентный фон: от почти черного до темно-серого
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(15, 15, 15), 0, getHeight(), new Color(50, 50, 50));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initUI(ActionListener controller) {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;

        // Панель управления: полупрозрачная темно-серая
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255, 15)); // Очень слабое затемнение
        panel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));

        // Заголовок
        JLabel title = new JLabel("ТАНКОВЫЙ БОЙ");
        title.setFont(new Font("Impact", Font.BOLD, 72));
        title.setForeground(new Color(200, 200, 200)); // Светло-серый, почти белый
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // Стиль меток
        Font labelFont = new Font("Arial", Font.BOLD, 24);
        Color labelColor = Color.LIGHT_GRAY;

        // Ввод количества врагов
        JLabel enemyLabel = new JLabel("Врагов (число):");
        enemyLabel.setFont(labelFont);
        enemyLabel.setForeground(labelColor);
        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(enemyLabel, gbc);

        enemyCountField = new JTextField("5");
        enemyCountField.setFont(new Font("Arial", Font.PLAIN, 24));
        enemyCountField.setPreferredSize(new Dimension(100, 30));
        enemyCountField.setBackground(new Color(200, 200, 200)); // Светлый фон для ввода
        gbc.gridx = 1;
        panel.add(enemyCountField, gbc);

        // Выбор карты
        JLabel mapLabel = new JLabel("Локация:");
        mapLabel.setFont(labelFont);
        mapLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(mapLabel, gbc);

        String[] mapOptions = {"Лето (Трава)", "Пустыня (Песок)"};
        mapChooser = new JComboBox<>(mapOptions);
        mapChooser.setFont(new Font("Arial", Font.PLAIN, 24));
        mapChooser.setBackground(new Color(200, 200, 200));
        gbc.gridx = 1;
        panel.add(mapChooser, gbc);

        // Кнопка старта (акцентный, но нейтральный цвет)
        startButton = new JButton("В БОЙ!");
        startButton.setFont(new Font("Arial", Font.BOLD, 36));
        startButton.setBackground(new Color(100, 100, 100)); // Темно-серый
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setActionCommand("START_GAME");
        startButton.addActionListener(controller);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(startButton, gbc);

        // Кнопка выхода
        JButton exitButton = new JButton("Выход");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 20));
        exitButton.setBackground(new Color(70, 70, 70)); // Еще темнее
        exitButton.setForeground(Color.WHITE);
        exitButton.setActionCommand("EXIT");
        exitButton.addActionListener(controller);
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(exitButton, gbc);

        add(panel);
        revalidate();
        repaint();
    }

    public void setup(ActionListener controller) {
        initUI(controller);
    }

    public int getEnemyCount() {
        try {
            int count = Integer.parseInt(enemyCountField.getText());
            return Math.max(1, Math.min(count, 50));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public int getSelectedMapId() {
        return mapChooser.getSelectedIndex() + 1;
    }
}