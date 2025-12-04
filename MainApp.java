import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    private static final String GAME_TITLE = "Танковый Бой";
    private static final int WINDOW_HEIGHT_OFFSET = 40;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private GameModel model;
    private GameController controller;
    private GameView gameView;
    private MenuView menuView;

    public MainApp() {
        super(GAME_TITLE);
        initializeApplication();
    }

    private void initializeApplication() {
        configureFrame();
        initializeComponents();
        setupUI();
        displayWindow();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        model = new GameModel();
        controller = new GameController(model, this);

        menuView = new MenuView(controller);
        menuView.setup(controller);

        gameView = new GameView(model, controller);
        controller.setViews(gameView, menuView);
    }

    private void setupUI() {
        mainPanel.add(menuView, "Menu");
        mainPanel.add(gameView, "Game");
        add(mainPanel);
        setSize(GameModel.MAP_WIDTH, GameModel.MAP_HEIGHT + WINDOW_HEIGHT_OFFSET);
    }

    private void displayWindow() {
        setVisible(true);
    }

    public void showMenuScreen() {
        cardLayout.show(mainPanel, "Menu");
    }

    public void showGameScreen() {
        cardLayout.show(mainPanel, "Game");
        gameView.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}