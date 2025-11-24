import java.awt.event.*;
import java.awt.Container;
import javax.swing.JButton;

public class GameController extends KeyAdapter implements ActionListener {
    private GameModel model;
    private MainApp app;
    private GameView gameView;
    private MenuView menuView;

    private boolean wPressed, aPressed, sPressed, dPressed;

    public GameController(GameModel model, MainApp app) {
        this.model = model;
        this.app = app;
    }

    public void setViews(GameView gameView, MenuView menuView) {
        this.gameView = gameView;
        this.menuView = menuView;
        gameView.addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "START_GAME":
                int enemyCount = menuView.getEnemyCount();
                int mapId = menuView.getSelectedMapId();
                model.initGame(enemyCount, mapId);
                app.showGameScreen();
                break;
            case "GO_TO_MENU":
            case "GO_TO_MENU_FINAL":
                app.showMenuScreen();
                break;
            case "EXIT":
                System.exit(0);
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (model.getCurrentState() != GameModel.GameState.RUNNING) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) wPressed = true;
        else if (key == KeyEvent.VK_S) sPressed = true;
        else if (key == KeyEvent.VK_A) aPressed = true;
        else if (key == KeyEvent.VK_D) dPressed = true;
        if (key == KeyEvent.VK_SPACE) model.playerShoot();
        updateMovement();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) wPressed = false;
        else if (key == KeyEvent.VK_S) sPressed = false;
        else if (key == KeyEvent.VK_A) aPressed = false;
        else if (key == KeyEvent.VK_D) dPressed = false;
        updateMovement();
    }

    private void updateMovement() {
        if (wPressed) { model.setPlayerDirection(0); model.setPlayerMoving(true); }
        else if (sPressed) { model.setPlayerDirection(2); model.setPlayerMoving(true); }
        else if (aPressed) { model.setPlayerDirection(3); model.setPlayerMoving(true); }
        else if (dPressed) { model.setPlayerDirection(1); model.setPlayerMoving(true); }
        else { model.setPlayerMoving(false); }
    }
}