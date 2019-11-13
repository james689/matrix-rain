
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MatrixRainGUI extends JPanel {

    private List<Streamer> listStreamers = null; // list containing all the streamers
    private int nMaxStreamers = 100; // maximum number of streamers that can appear on screen
    private int numCols, numRows; // the MatrixRainGUI panel is divided into a grid of size
    // numRows*numCols. Each cell in the grid has space to display a single character.
    private Font monospaceFont = new Font("Courier New", Font.PLAIN, 10);
    private Dimension oldPanelSize = new Dimension(400, 400); // used to detect whether
    // there has been a change in the panel size.
    private char[][] charMatrix; // this is used so that only one character can
    // be drawn in a cell on the screen, to prevent the Streamer's from
    // overlapping each other. Each Streamer's characters are added to the charMatrix,
    // potentially overwriting characters from other streamers already existing in the
    // char matrix. The charMatrix can then be blitted to the screen.
    private boolean useCharMatrixRendering = false; // gives the option to switch between
    // either rendering streamers with the potential to overlap/be drawn on top of each other (i.e. non
    // matrix rendering, or drawing non-overlapping streamers (matrix rendering)

    public MatrixRainGUI() {
        setPreferredSize(oldPanelSize);
        setBackground(Color.BLACK);
        Timer timer = new Timer(10, new TimerListener());
        timer.start();
    }

    private class Streamer {

        int column = 0; // x coordinate of streamer (in columns)
        float fPosition = 0.0f; // y coordinate, the head of the streamer
        double fSpeed = 10.0; // speed the streamer falls at
        String text; // text of the streamer

        public Streamer() {
            prepareStreamer();
        }

        public void prepareStreamer() {
            column = (int) (Math.random() * numCols);
            fPosition = 0.0f;
            fSpeed = (Math.random() * 40) + 5;
            text = "ABCDEFGHIJKLMNO";
        }
    }
    
    // called either when there are no existing streamers or when the panel is resized
    // and new streamers need to be created
    private void createStreamers(int charWidth, int charHeight) {
        System.out.println("create streamers called");
        listStreamers = new ArrayList<>();
        double panelWidth = getWidth();
        double panelHeight = getHeight();
        System.out.println("panel width = " + panelWidth + " panel height = " + panelHeight);
        // calculate how many rows and columns the panel can support
        // based upon the font size.
        numCols = (int) Math.ceil(panelWidth / charWidth);
        numRows = (int) Math.ceil(panelHeight / charHeight);
        charMatrix = new char[numRows][numCols];
        System.out.println("numCols = " + numCols + " numRows = " + numRows);
        for (int i = 0; i < nMaxStreamers; i++) {
            Streamer s = new Streamer();
            listStreamers.add(s);
        }
    }

    class TimerListener implements ActionListener {

        private long lastTimeMillis = System.currentTimeMillis();

        public void actionPerformed(ActionEvent ae) {

            if (listStreamers == null) {
                return; // no streamers created yet so don't try to update them
            }

            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTimeMillis = currentTimeMillis - lastTimeMillis;
            double elapsedTimeSeconds = elapsedTimeMillis / 1000.0;
            lastTimeMillis = currentTimeMillis;

            for (Streamer s : listStreamers) {
                s.fPosition += s.fSpeed * elapsedTimeSeconds;
                if (s.fPosition - s.text.length() > numRows) {
                    // streamer has gone past the bottom of the screen
                    // so reset it
                    s.prepareStreamer();
                }
            }
            repaint();
        }
    }

    // draws the panel
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(monospaceFont);
        FontMetrics metrics = g.getFontMetrics();
        /* debugging code to check that all characters have the same width
        for (char c = 'a'; c <= 'z'; c++) {
            System.out.println("width of " + c + " = " + metrics.charWidth(c));
        }*/
        int charWidth = metrics.charWidth('c'); // all characters in a monospace
        // font should have the same width, so just pick any character to base
        // the charWidth off of
        //System.out.println("charWidth = " + charWidth);
        int charHeight = metrics.getAscent() + metrics.getDescent();
        //System.out.println("charHeight = " + charHeight);
        
        Dimension currentPanelSize = getSize();
        // re-populate the list of streamers if either this is the first time
        // creating the streamers or the panel has been resized
        if (listStreamers == null || !currentPanelSize.equals(oldPanelSize)) {
            createStreamers(charWidth, charHeight);
            oldPanelSize = currentPanelSize;
        }

        // draw streamers on screen
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(Color.GREEN);
        if (useCharMatrixRendering) {
            clearCharMatrix();
            for (Streamer s : listStreamers) {
                addStreamerToMatrix(s);
            }
            drawCharMatrix(g,charWidth,charHeight);
        } else {
            for (Streamer s : listStreamers) {
                drawStreamer(g2d, s, metrics.getAscent(), charWidth, charHeight);
            }
        }
    }
    
    // ------------- char matrix methods -----------------
    
    private void clearCharMatrix() {
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                charMatrix[row][col] = ' ';
            }
        }
    }
    
    // draw the char matrix to the screen
    private void drawCharMatrix(Graphics g, int charWidth, int charHeight) {
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                g.drawString("" + charMatrix[row][col], col*charWidth, row*charHeight);
            }
        }
    }
    
    private void addStreamerToMatrix(Streamer s) {
        for (int i = 0; i < s.text.length(); i++) {
            char c = s.text.charAt(i);
            int row = (int) s.fPosition - i;
            if (row < 0 || row >= numRows) {
                continue;
            }
            charMatrix[row][s.column] = c;
        }
    }
    
    // ------- alternate rendering ----------
    
    private void drawStreamer(Graphics2D g2d, Streamer s, int ascent, 
            int charWidth, int charHeight) {
        for (int i = 0; i < s.text.length(); i++) {
            char c = s.text.charAt(i);
            g2d.drawString("" + c, s.column * charWidth, (s.fPosition - i) * charHeight);
        }
    }
}
