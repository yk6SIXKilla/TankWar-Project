import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MenuView extends JPanel {
    private static final int DEFAULT_ENEMY_COUNT = 5;
    private static final int MIN_ENEMY_COUNT = 1;
    private static final int MAX_ENEMY_COUNT = 50;

    private JTextField enemyCountField;
    private JComboBox<String> mapChooser;
    private JButton startButton;

    public MenuView(ActionListener controller) {
        setLayout(new GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = createBackgroundGradient();
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private GradientPaint createBackgroundGradient() {
        Color startColor = new Color(15, 15, 15);
        Color endColor = new Color(50, 50, 50);
        return new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
    }

    public void setup(ActionListener controller) {
        initializeUI(controller);
    }

    private void initializeUI(ActionListener controller) {
        removeAll();
        JPanel controlPanel = createControlPanel(controller);
        add(controlPanel);
        revalidate();
        repaint();
    }

    private JPanel createControlPanel(ActionListener controller) {
        JPanel panel = new JPanel(new GridBagLayout());
        configurePanelAppearance(panel);

        GridBagConstraints constraints = createGridConstraints();
        addTitleToPanel(panel, constraints);
        addEnemyCountControls(panel, constraints);
        addMapSelectionControls(panel, constraints);
        addActionButtons(panel, constraints, controller);

        return panel;
    }

    private void configurePanelAppearance(JPanel panel) {
        panel.setBackground(new Color(255, 255, 255, 15));
        panel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
    }

    private GridBagConstraints createGridConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(15, 15, 15, 15);
        constraints.anchor = GridBagConstraints.CENTER;
        return constraints;
    }

    private void addTitleToPanel(JPanel panel, GridBagConstraints constraints) {
        JLabel title = createTitleLabel();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(title, constraints);
    }

    private JLabel createTitleLabel() {
        JLabel title = new JLabel("ТАНКОВЫЙ БОЙ");
        title.setFont(new Font("Impact", Font.BOLD, 72));
        title.setForeground(new Color(200, 200, 200));
        return title;
    }

    private void addEnemyCountControls(JPanel panel, GridBagConstraints constraints) {
        addEnemyCountLabel(panel, constraints);
        addEnemyCountField(panel, constraints);
    }

    private void addEnemyCountLabel(JPanel panel, GridBagConstraints constraints) {
        JLabel enemyLabel = createStyledLabel("Врагов (число):");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        panel.add(enemyLabel, constraints);
    }

    private void addEnemyCountField(JPanel panel, GridBagConstraints constraints) {
        enemyCountField = createEnemyCountField();
        constraints.gridx = 1;
        panel.add(enemyCountField, constraints);
    }

    private JTextField createEnemyCountField() {
        JTextField field = new JTextField(String.valueOf(DEFAULT_ENEMY_COUNT));
        field.setFont(new Font("Arial", Font.PLAIN, 24));
        field.setPreferredSize(new Dimension(100, 30));
        field.setBackground(new Color(200, 200, 200));
        return field;
    }

    private void addMapSelectionControls(JPanel panel, GridBagConstraints constraints) {
        addMapSelectionLabel(panel, constraints);
        addMapSelectionComboBox(panel, constraints);
    }

    private void addMapSelectionLabel(JPanel panel, GridBagConstraints constraints) {
        JLabel mapLabel = createStyledLabel("Локация:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(mapLabel, constraints);
    }

    private void addMapSelectionComboBox(JPanel panel, GridBagConstraints constraints) {
        mapChooser = createMapSelectionComboBox();
        constraints.gridx = 1;
        panel.add(mapChooser, constraints);
    }

    private JComboBox<String> createMapSelectionComboBox() {
        String[] mapOptions = {"Лето (Трава)", "Пустыня (Песок)"};
        JComboBox<String> comboBox = new JComboBox<>(mapOptions);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 24));
        comboBox.setBackground(new Color(200, 200, 200));
        return comboBox;
    }

    private void addActionButtons(JPanel panel, GridBagConstraints constraints, ActionListener controller) {
        addStartButton(panel, constraints, controller);
        addExitButton(panel, constraints, controller);
    }

    private void addStartButton(JPanel panel, GridBagConstraints constraints, ActionListener controller) {
        startButton = createStartButton(controller);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(startButton, constraints);
    }

    private JButton createStartButton(ActionListener controller) {
        JButton button = new JButton("В БОЙ!");
        button.setFont(new Font("Arial", Font.BOLD, 36));
        button.setBackground(new Color(100, 100, 100));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setActionCommand("START_GAME");
        button.addActionListener(controller);
        return button;
    }

    private void addExitButton(JPanel panel, GridBagConstraints constraints, ActionListener controller) {
        JButton exitButton = createExitButton(controller);
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(exitButton, constraints);
    }

    private JButton createExitButton(ActionListener controller) {
        JButton button = new JButton("Выход");
        button.setFont(new Font("Arial", Font.PLAIN, 20));
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setActionCommand("EXIT");
        button.addActionListener(controller);
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }

    public int getEnemyCount() {
        try {
            int count = Integer.parseInt(enemyCountField.getText());
            return Math.max(MIN_ENEMY_COUNT, Math.min(count, MAX_ENEMY_COUNT));
        } catch (NumberFormatException e) {
            return DEFAULT_ENEMY_COUNT;
        }
    }

    public int getSelectedMapId() {
        return mapChooser.getSelectedIndex() + 1;
    }
}