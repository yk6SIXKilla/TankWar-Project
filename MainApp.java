import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private GameModel model;
    private GameController controller;
    private GameView gameView;
    private MenuView menuView;

    public MainApp() {
        super("Танковый Бой");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new GameModel();
        controller = new GameController(model, this);

        menuView = new MenuView(controller);
        menuView.setup(controller);

        gameView = new GameView(model, controller);
        controller.setViews(gameView, menuView);

        mainPanel.add(menuView, "Menu");
        mainPanel.add(gameView, "Game");

        add(mainPanel);
        setSize(GameModel.MAP_WIDTH, GameModel.MAP_HEIGHT + 40);
        setResizable(false);
        setLocationRelativeTo(null);

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