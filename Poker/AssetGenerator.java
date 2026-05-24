import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class AssetGenerator {

    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;
    private static final String ASSETS_DIR = "assets";

    public static void ensureAssetsExist() {
        File dir = new File(ASSETS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File backFile = new File(dir, "card_back.png");
        if (!backFile.exists()) {
            generateCardBack(backFile);
        }

        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {
            "2", "3", "4", "5", "6", "7", "8",
            "9", "10", "J", "Q", "K", "A"
        };

        for (String suit : suits) {
            for (String rank : ranks) {
                String fileName = rank.toLowerCase() + "_of_" + suit.toLowerCase() + ".png";
                File cardFile = new File(dir, fileName);
                if (!cardFile.exists()) {
                    generateCardFace(cardFile, rank, suit);
                }
            }
        }
    }

    private static void generateCardBack(File file) {
        BufferedImage image = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 12, 12);

        g2.setColor(new Color(25, 60, 150));
        g2.fillRoundRect(4, 4, CARD_WIDTH - 9, CARD_HEIGHT - 9, 8, 8);

        g2.setColor(new Color(40, 90, 200));
        for (int y = 10; y < CARD_HEIGHT - 10; y += 12) {
            for (int x = 10; x < CARD_WIDTH - 10; x += 12) {
                g2.fillRect(x, y, 6, 6);
            }
        }

        g2.setColor(new Color(200, 180, 50));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(8, 8, CARD_WIDTH - 17, CARD_HEIGHT - 17, 6, 6);

        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 12, 12);

        g2.dispose();
        try {
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateCardFace(File file, String rank, String suit) {
        BufferedImage image = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Card background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 12, 12);

        // Card border
        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 12, 12);

        // Suit color & symbol
        Color suitColor = (suit.equals("Hearts") || suit.equals("Diamonds")) 
                ? new Color(200, 30, 30) : new Color(30, 30, 30);
        g2.setColor(suitColor);

        String suitSymbol = "";
        switch (suit) {
            case "Hearts":   suitSymbol = "\u2665"; break;
            case "Diamonds": suitSymbol = "\u2666"; break;
            case "Clubs":    suitSymbol = "\u2663"; break;
            case "Spades":   suitSymbol = "\u2660"; break;
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(rank, 6, 20);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(suitSymbol, 7, 34);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 36));
        FontMetrics fm = g2.getFontMetrics();
        int centerX = (CARD_WIDTH - fm.stringWidth(suitSymbol)) / 2;
        int centerY = (CARD_HEIGHT + fm.getAscent()) / 2 - 4;
        g2.drawString(suitSymbol, centerX, centerY);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fmRank = g2.getFontMetrics();
        int brX = CARD_WIDTH - fmRank.stringWidth(rank) - 6;
        int brY = CARD_HEIGHT - 22;
        g2.drawString(rank, brX, brY);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fmSuit = g2.getFontMetrics();
        g2.drawString(suitSymbol,
                CARD_WIDTH - fmSuit.stringWidth(suitSymbol) - 7,
                CARD_HEIGHT - 8);

        g2.dispose();
        try {
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
