import java.awt.event.*;
import javax.swing.JButton;

public class GameController extends KeyAdapter implements ActionListener {
    private GameModel model;
    private MainApp application;
    private GameView gameView;
    private MenuView menuView;

    private boolean isWPressed, isAPressed, isSPressed, isDPressed;

    public GameController(GameModel model, MainApp application) {
        this.model = model;
        this.application = application;
    }

    public void setViews(GameView gameView, MenuView menuView) {
        this.gameView = gameView;
        this.menuView = menuView;
        gameView.addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        handleActionCommand(command);
    }

    private void handleActionCommand(String command) {
        switch (command) {
            case "START_GAME":
                startNewGame();
                break;
            case "GO_TO_MENU":
            case "GO_TO_MENU_FINAL":
                returnToMenu();
                break;
            case "EXIT":
                exitApplication();
                break;
        }
    }

    private void startNewGame() {
        int enemyCount = menuView.getEnemyCount();
        int mapId = menuView.getSelectedMapId();
        model.initGame(enemyCount, mapId);
        application.showGameScreen();
    }

    private void returnToMenu() {
        application.showMenuScreen();
    }

    private void exitApplication() {
        System.exit(0);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (model.getCurrentState() != GameModel.GameState.RUNNING) {
            return;
        }

        handleKeyPress(event.getKeyCode());
    }

    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W: isWPressed = true; break;
            case KeyEvent.VK_S: isSPressed = true; break;
            case KeyEvent.VK_A: isAPressed = true; break;
            case KeyEvent.VK_D: isDPressed = true; break;
            case KeyEvent.VK_SPACE: model.playerShoot(); break;
        }
        updatePlayerMovement();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        handleKeyRelease(event.getKeyCode());
    }

    private void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W: isWPressed = false; break;
            case KeyEvent.VK_S: isSPressed = false; break;
            case KeyEvent.VK_A: isAPressed = false; break;
            case KeyEvent.VK_D: isDPressed = false; break;
        }
        updatePlayerMovement();
    }

    private void updatePlayerMovement() {
        if (isWPressed) {
            setPlayerMovement(0, true);
        } else if (isSPressed) {
            setPlayerMovement(2, true);
        } else if (isAPressed) {
            setPlayerMovement(3, true);
        } else if (isDPressed) {
            setPlayerMovement(1, true);
        } else {
            model.setPlayerMoving(false);
        }
    }

    private void setPlayerMovement(int direction, boolean isMoving) {
        model.setPlayerDirection(direction);
        model.setPlayerMoving(isMoving);
    }
}