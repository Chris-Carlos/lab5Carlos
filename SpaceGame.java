/** Project: Solo Lab 5 Assignment
* Purpose Details: A Space Game
* Course:IST 242
* Author:Christopher Carlos
* Original Author: Joe Oakes
* Date Developed: 6/20/2024
* Last Date Changed:6/21/2024
* Revision:12
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;
//keylistener allows keystrokes to work
//setting all global variables
public class SpaceGame extends JFrame implements KeyListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int POWERUP_WIDTH = 50;
    private static final int POWERUP_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 5;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private static int PLAYER_HEALTH;
    private static final int MAX_HEALTH = 100;
    private static final int HEALTH_POWER_UP_VALUE = 10;
    private static final int TIMER_DURATION = 60; // in seconds
    private static final int LEVEL_DURATION = 30; // in seconds
    private static final int CHALLENGE_LEVEL_SCORE = 100;
    //setting global images, integers, timers and labels
    private int score = 0;
    private int health = MAX_HEALTH;
    private int currentLevel = 1;
    private int timeLeft;
    private int levelTimeLeft;
    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timerLabel;
    private Timer timer;
    private Timer countdownTimer;
    private boolean isGameOver;
    private boolean isChallengeLevel;
    private int playerX, playerY;
    private int powerupX, powerupY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private List<Point> obstacles;
    private List<Point> stars;
    private List<Point> healthPowerUps; // List for health power-ups
    private BufferedImage healthPowerUpImage;
    private BufferedImage shipImage;
    private BufferedImage spriteSheet;
    private int spriteWidth = 32;
    private int spriteHeight = 42;
    private int healthPowerUpsWidth = 32;
    private int healthPowerUpsHeight = 24;
    private Clip blasterSound;
    private Clip collisionSound;
    private boolean shieldActive = false;
    private int shieldDuration = 5000;
    private long shieldStartTime;
    //main game class
    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        PLAYER_HEALTH = 100;
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        //sets score label to blue and 0
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(scoreLabel);
        //sets health label to green and 0
        healthLabel = new JLabel("Health: " + health);
        healthLabel.setForeground(Color.GREEN);
        healthLabel.setBounds(120, 10, 100, 20);
        gamePanel.add(healthLabel);
        //sets timer label to red and 60 sec duration
        timerLabel = new JLabel("Time: " + TIMER_DURATION);
        timerLabel.setForeground(Color.RED);
        timerLabel.setBounds(230, 10, 100, 20);
        gamePanel.add(timerLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);
        //initial values for x and y and methods to false.
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new ArrayList<>();
        stars = generateStars(200);
        healthPowerUps = new ArrayList<>(); // Initialize health power-ups list
        timeLeft = TIMER_DURATION;
        levelTimeLeft = LEVEL_DURATION;
        //images and sounds are loaded here.
        try {
            shipImage = ImageIO.read(new File("Starship.png"));
            spriteSheet = ImageIO.read(new File("Obstacles.png"));
            healthPowerUpImage = ImageIO.read(new File("PowerUp.png"));
            AudioInputStream audioInputStreamBlasterSound = AudioSystem.getAudioInputStream(new File("BlasterFire.wav").getAbsoluteFile());
            blasterSound = AudioSystem.getClip();
            blasterSound.open(audioInputStreamBlasterSound);
            AudioInputStream audioInputStreamCollisionSound = AudioSystem.getAudioInputStream(new File("Collision.wav").getAbsoluteFile());
            collisionSound = AudioSystem.getClip();
            collisionSound.open(audioInputStreamCollisionSound);
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
            ex.printStackTrace();
        }

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
        //countdown timer goes down by 1 sec
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    timeLeft--;
                    timerLabel.setText("Time: " + timeLeft);
                } else {
                    isGameOver = true;
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        countdownTimer.start();
    }
    //method for blaster sound
    public void playBlasterSound() {
        if (blasterSound != null) {
            blasterSound.setFramePosition(0);
            blasterSound.start();
        }
    }
    //method for collision sound
    public void playCollisionSound() {
        if (collisionSound != null) {
            collisionSound.setFramePosition(0);
            collisionSound.start();
        }
    }
    //generates stars randomly
    private List<Point> generateStars(int numStars) {
        List<Point> starsList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numStars; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starsList.add(new Point(x, y));
        }
        return starsList;
    }
    //random color generator
    public static Color generateRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r, g, b);
    }
    //background filled with black
    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        //random color fill for stars
        for (Point star : stars) {
            g.setColor(generateRandomColor());
            g.fillOval(star.x, star.y, 2, 2);
        }

        // Player Ship Image
        if (shipImage != null) {
            g.drawImage(shipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }
        //color fill for shield
        if (shieldActive) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(playerX, playerY, 60, 60);
        }
        //projectile color fill with green
        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }
        //draws obstacles at x and y, sets size to the image sprite width and length
        for (Point obstacle : obstacles) {
            if (spriteSheet != null) {
                Random random = new Random();
                int spriteIndex = random.nextInt(4);
                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0;

                g.drawImage(spriteSheet.getSubimage(spriteX, spriteY, spriteWidth, spriteHeight), obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null);
            }
        }

        // Draw health power-ups set to power up sprite width and height.
        if (healthPowerUpImage != null) {
            for (Point powerup : healthPowerUps) {
                g.drawImage(healthPowerUpImage, powerup.x, powerup.y, POWERUP_WIDTH, POWERUP_HEIGHT, null);
            }
        }
        // game over sign
        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }
    //the activate shield method with current time
    private void activateShield() {
        shieldActive = true;
        shieldStartTime = System.currentTimeMillis();
    }
    //the deactivate shield method that turns off the shield.
    private void deactivateShield() {
        shieldActive = false;
    }
    //timer for shield power up
    private boolean isShieldActive() {
        return shieldActive && (System.currentTimeMillis() - shieldStartTime) < shieldDuration;
    }
    //update method that allows game to continuously update its state
    private void update() {
        if (health <= 0) {
            isGameOver = true;
            return;
        }

        if (isShieldActive() && (System.currentTimeMillis() - shieldStartTime) >= shieldDuration) {
            deactivateShield();
        }

        if (isFiring) {
            projectileY -= PROJECTILE_SPEED;
            if (projectileY < 0) {
                isProjectileVisible = false;
                isFiring = false;
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            Point obstacle = obstacles.get(i);
            obstacle.y += OBSTACLE_SPEED;
            if (obstacle.y > HEIGHT) {
                obstacles.remove(i);
                i--;
                continue;
            }
            //obstacle, player and projectile coordinates for use in collisions
            Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            //obstacle collision
            if (obstacleRect.intersects(playerRect)) {
                playCollisionSound();
                obstacles.remove(i);
                i--;
                if (!isShieldActive()) {
                    health -= 10;
                    healthLabel.setText("Health: " + health);
                }
                continue;
            }

            if (isProjectileVisible && obstacleRect.intersects(projectileRect)) {
                obstacles.remove(i);
                i--;
                isProjectileVisible = false;
                isFiring = false;
                score += 10;
                scoreLabel.setText("Score: " + score);
            }
        }

        // Check for collisions on health power-ups
        for (int i = 0; i < healthPowerUps.size(); i++) {
            Point powerup = healthPowerUps.get(i);
            powerup.y += OBSTACLE_SPEED;

            if (powerup.y > HEIGHT) {
                healthPowerUps.remove(i);
                i--;
                continue;
            }

            Rectangle powerupRect = new Rectangle(powerup.x, powerup.y, POWERUP_WIDTH, POWERUP_HEIGHT);
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

            if (powerupRect.intersects(playerRect)) {
                healthPowerUps.remove(i);
                i--;
                health += HEALTH_POWER_UP_VALUE;
                if (health > MAX_HEALTH) {
                    health = MAX_HEALTH;
                }
                healthLabel.setText("Health: " + health);
                continue;
            }
        }
        //challenge level score. this is broken I did not finish the challenge level speed increase.
        if (isChallengeLevel && score >= CHALLENGE_LEVEL_SCORE) {
            isChallengeLevel = false;
            currentLevel++;
            levelTimeLeft = LEVEL_DURATION;
        }

        if (!isChallengeLevel) {
            levelTimeLeft--;
            if (levelTimeLeft <= 0) {
                isChallengeLevel = true;
                currentLevel++;
                levelTimeLeft = LEVEL_DURATION;
            }
        }
        //calls methods for obstacle and power up generator
        generateRandomObstacles();
        generateRandomHealthPowerUps();
    }
    //random generator for obstacles
    private void generateRandomObstacles() {
        if (Math.random() < 0.05) {
            Random random = new Random();
            int x = random.nextInt(WIDTH - OBSTACLE_WIDTH);
            obstacles.add(new Point(x, 0));
        }
    }
    //random generator for power ups
    private void generateRandomHealthPowerUps() {
        if (Math.random() < 0.01) {
            Random random = new Random();
            int x = random.nextInt(WIDTH - POWERUP_WIDTH);
            healthPowerUps.add(new Point(x, 0));
        }
    }
    //all the key press commands
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        }
        if (key == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        }
        if (key == KeyEvent.VK_UP && playerY > 0) {
            playerY -= PLAYER_SPEED;
        }
        if (key == KeyEvent.VK_DOWN && playerY < HEIGHT - PLAYER_HEIGHT) {
            playerY += PLAYER_SPEED;
        }
        if (key == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            playBlasterSound();
        }
        if (key == KeyEvent.VK_S) {
            activateShield();
        }
    }
    //no key release or key typed commands
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SpaceGame game = new SpaceGame();
            game.setVisible(true);
        });
    }
}
