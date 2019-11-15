
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

/**
 * The MatrixRainGUI class is the JPanel used to display the matrix rain effect.
 * This panel is divided into a grid of rows and columns, the specific number
 * of rows and columns used depends upon the font size of the text to be displayed
 * and the dimensions of the panel. So, for example, using a smaller font size
 * or resizing the window the make the panel larger will result in more rows/columns.
 * Each cell in the grid is big enough to display a single character. Each cell's width/height
 * is the same size as the font width/height. The program assumes a monospaced font is
 * being used i.e. a font where all characters have the same width.
 */
public class MatrixRainGUI extends JPanel {
    
    private static final Color DARK_GREEN = new Color(0,100,0); // colour used for slow moving streamers
    private static final Color LIGHT_GREEN = new Color(144,238,144); // colour used for fast moving streamers

    private List<Streamer> listStreamers = null; // list containing all the streamers
    private int nMaxStreamers = 200; // maximum number of streamers that can appear on screen
    private int numCols, numRows; // the MatrixRainGUI panel is divided into a grid of size
    // numRows*numCols. Each cell in the grid has space to display a single character.
    private Font monospaceFont = new Font("Courier New", Font.PLAIN, 15); // font used to display
    // the characters on screen
    private Dimension oldPanelSize = new Dimension(400, 400); // used to detect whether
    // there has been a change in the panel size.
    private boolean[][] charMatrix;
    //private char[][] charMatrix; // this is used so that only one character can
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

    /**
     * This class represents a falling stream of text displayed on screen, a 'streamer'.
     * Each Streamer instance has a position on screen (given in columns/rows
     * rather than pixels), a speed it moves/falls at and text the streamer
     * displays.
     */
    private class Streamer {

        int column = 0; // x coordinate of streamer (in columns)
        float fPosition = 0.0f; // y coordinate of the head of the streamer (in rows)
        // (the head of the streamer is the character closest to the bottom of the screen)
        double fSpeed = 10.0; // speed the streamer falls at
        StringBuilder text = new StringBuilder(); // text of the streamer, use
        // StringBuilder instead of String to make appending random characters to the streamer
        // more efficient.

        public Streamer() {
            prepareStreamer();
        }

        // resets the Streamer to a new random position, speed and text
        public void prepareStreamer() {
            // set position and speed of streamer
            column = HelperMethods.mathRandom(0,numCols); // start streamer at a random column
            fPosition = 0.0f; // always start streamer at the top row of the screen
            fSpeed = HelperMethods.mathRandom(5,46); // random speed between 5 and 45
            //fSpeed = 2; // fixed speed for debugging
            
            // set text of streamer
            text.setLength(0); // clear the string builder's previous text (see https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder)
            //text.append("ABCDEFGHIJKLMNO"); // if you want the streamer to have fixed text for debugging
            int nStreamerLength = HelperMethods.mathRandom(10,91); // random length between 10 and 90
            for (int i = 0; i < nStreamerLength; i++) {
                text.append(randomCharacter());
            }
            System.out.println("in prepareStreamer(). column = " + column + " fSpeed = " + fSpeed + " streamerLength = " + nStreamerLength);
        }
    }
    
    // called either when there are no existing streamers or when the panel is resized
    // and new streamers need to be created.
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
        //charMatrix = new char[numRows][numCols];
        charMatrix = new boolean[numRows][numCols];
        System.out.println("numCols = " + numCols + " numRows = " + numRows);
        for (int i = 0; i < nMaxStreamers; i++) {
            listStreamers.add(new Streamer());
        }
    }

    class TimerListener implements ActionListener {

        private long lastTimeMillis = System.currentTimeMillis();

        // called every time the timer fires
        public void actionPerformed(ActionEvent ae) {

            if (listStreamers == null) {
                return; // no streamers created yet so don't try to update them
            }

            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTimeMillis = currentTimeMillis - lastTimeMillis;
            double elapsedTimeSeconds = elapsedTimeMillis / 1000.0;
            lastTimeMillis = currentTimeMillis;

            for (Streamer s : listStreamers) {
                s.fPosition += s.fSpeed * elapsedTimeSeconds; // update position of streamer based upon elapsed time
                //System.out.println("numRows = " + numRows + " numCols = " + numCols + " fPosition: " + s.fPosition);
                
                if (s.fPosition - s.text.length() > numRows) {
                    // the tail of the streamer has gone past the bottom edge of the screen
                    // so reset the streamer
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
        if (useCharMatrixRendering) {
            /*clearCharMatrix();
            for (Streamer s : listStreamers) {
                addStreamerToMatrix(s);
            }
            drawCharMatrix(g,charWidth,charHeight);*/
        } else {
            // clear the char matrix
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    charMatrix[row][col] = false;
                }
            }
            for (Streamer s : listStreamers) {
                drawStreamer(g2d, s, metrics.getAscent(), charWidth, charHeight);
            }
        }
    }
    
    // ------- alternate rendering ----------
    
    private void drawStreamer(Graphics2D g2d, Streamer s, int ascent, 
            int charWidth, int charHeight) {
        
        for (int i = 0; i < s.text.length(); i++) {
            Color col = s.fSpeed < 15.0f ? DARK_GREEN : LIGHT_GREEN; // decide what
            // color to draw the streamer in based upon its speed
            if (i == 0) {
                col = Color.WHITE; // head of the streamer is white
            } else if (i <= 3) {
                col = Color.GRAY; // first few characters of streamer after the
                // head are grey
            }
            g2d.setColor(col);
            int nCharIndex = Math.abs( (i - ((int)s.fPosition)) ) % s.text.length();
            
            int row = (int) s.fPosition - i;
            
            if (row >= 0 && row < numRows && charMatrix[row][s.column] == false) {
                g2d.drawString("" + s.text.charAt(nCharIndex), 
                    s.column * charWidth, row * charHeight);
                charMatrix[row][s.column] = true;
            }
            
            /*if (charMatrix[row][s.column] != false) {
                return; // don't draw character in cell if there is already one present
            }
            
            //g2d.drawString("" + s.text.charAt(nCharIndex), 
                    //s.column * charWidth, (s.fPosition - i) * charHeight);
            g2d.drawString("" + s.text.charAt(nCharIndex), 
                    s.column * charWidth, row * charHeight);
            charMatrix[row][s.column] = true;*/
        }
    }
    
    private char randomCharacter() {
        //return (char) ((Math.random() * 0x1EF) + 0x00CO);
        //return (char) HelperMethods.mathRandom(33,128); // random ASCII character between 33 and 127
        return (char) HelperMethods.mathRandom(0x0400,0x0527); // character in the 'cryllic' character set
    }
    
    // ------------- char matrix methods -----------------
    
    /*private void clearCharMatrix() {
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
    }*/
}
