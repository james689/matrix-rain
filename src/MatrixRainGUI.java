
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
 * This panel is divided into a grid of rows and columns, the specific number of
 * rows and columns used depends upon the font size of the text to be displayed
 * and the dimensions of the panel. Each cell in the grid is large enough to display
 * a single character from the font being used.
 */
public class MatrixRainGUI extends JPanel {

    private static final Color DARK_GREEN = new Color(0, 100, 0); // colour used for slow moving streamers
    private static final Color LIGHT_GREEN = new Color(144, 238, 144); // colour used for fast moving streamers
    // the two constants below define the range of characters a streamer can contain
    private static final int MIN_CODE_POINT = 0x0400;
    private static final int MAX_CODE_POINT = 0x0527; 

    private List<Streamer> listStreamers = null; // list containing all the streamers
    private int nMaxStreamers = 200; // maximum number of streamers that can appear on screen
    private int numCols, numRows; // the panel is divided into a grid of size
    // numRows*numCols. Each cell in the grid has space to display a single character.
    private int cellWidth, cellHeight; // width and height of a cell in the grid.
    private Font font = new Font("Monospaced", Font.PLAIN, 15); // font used to display the characters on screen
    private Dimension oldPanelSize = new Dimension(400, 400); // used to detect whether there has been a change in the panel size.
    private boolean[][] cellsDrawnIn; // used to determine whether a cell in the grid already
    // contains a character, and thus prevent drawing multiple characters to the same position
    // on screen, which can result in an unpleasant looking overlapping effect.

    public MatrixRainGUI() {
        setPreferredSize(oldPanelSize);
        setBackground(Color.BLACK);
        Timer timer = new Timer(10, new TimerListener());
        timer.start();
    }

    /**
     * This class represents a falling stream of text displayed on screen, a
     * 'streamer'. Each Streamer instance has a position on screen (given in
     * columns/rows rather than pixels), a speed it moves/falls at and text the
     * streamer displays.
     */
    private class Streamer {

        int column = 0; // x coordinate of streamer (in columns)
        float row = 0.0f; // y coordinate of the head of the streamer (in rows)
        // (the head of the streamer is the character closest to the bottom of the screen)
        // the streamer can be positioned in between rows e.g. at row 5.45, to create a smoother animation
        // so that the streamer can move in smaller increments than 1 row at a time,
        // but it will always be drawn on an integer boundary. 
        double speed = 10.0; // speed the streamer falls at (in pixels per second)
        StringBuilder text = new StringBuilder(); // text of the streamer, use
        // StringBuilder instead of String to make appending random characters to the streamer
        // in the prepareStreamer() method more efficient.

        public Streamer() {
            prepareStreamer();
        }

        // resets the Streamer to a new random position, speed and text
        public void prepareStreamer() {
            // set position and speed of streamer
            column = HelperMethods.mathRandom(0, numCols); // start streamer at a random column/x coordinate
            row = 0.0f; // always start streamer at the top row of the screen
            speed = HelperMethods.mathRandom(5, 46); // random speed between 5 and 45
            // set text of streamer
            text.setLength(0); // clear the string builder's previous text (see https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder)
            //text.append("ABCDEFGHIJKLMNO"); // if you want the streamer to have fixed text for debugging
            int nStreamerLength = HelperMethods.mathRandom(10, 91); // random length between 10 and 90
            for (int i = 0; i < nStreamerLength; i++) {
                text.append(randomCharacter());
            }
        }
    }

    // called either when there are no existing streamers or when the panel is resized
    // and new streamers need to be created.
    private void createStreamers() {
        listStreamers = new ArrayList<>();
        double panelWidth = getWidth();
        double panelHeight = getHeight();
        //System.out.println("panel width = " + panelWidth + " panel height = " + panelHeight);
        // calculate how many rows and columns the panel can support
        // based upon the font size. Use Math.ceil() so that an extra row/column is used
        // to prevent having an unsightly gap at the edges of the screen.
        numCols = (int) Math.ceil(panelWidth / cellWidth);
        numRows = (int) Math.ceil(panelHeight / cellHeight);
        cellsDrawnIn = new boolean[numRows][numCols];
        //System.out.println("numCols = " + numCols + " numRows = " + numRows);
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
                s.row += s.speed * elapsedTimeSeconds; // update position of streamer based upon elapsed time
                //System.out.println("numRows = " + numRows + " numCols = " + numCols + " row: " + s.row);

                if (s.row - s.text.length() > numRows) {
                    // the tail of the streamer has gone past the bottom edge of the screen
                    // so reset the streamer
                    s.prepareStreamer();
                }
            }
            repaint(); // repaint will trigger a call to paintComponent()
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(font);
        Dimension currentPanelSize = getSize();
        // re-populate the list of streamers if either this is the first time
        // creating the streamers or the panel has been resized
        if (listStreamers == null || !currentPanelSize.equals(oldPanelSize)) {
            calculateCharacterSize(g);
            createStreamers();
            oldPanelSize = currentPanelSize;
        }

        // draw the streamers
        Graphics2D g2d = (Graphics2D) g;
        //drawGridLines(g2d);
        drawStreamers(g2d);
    }

    /** 
    * looks at every character between MIN_CODE_POINT and MAX_CODE_POINT
    * and finds the widest character. The width of the widest character will
    * be used for the width of a cell in the grid.
    */
    private void calculateCharacterSize(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        int minCharWidth = metrics.charWidth(MIN_CODE_POINT);
        int maxCharWidth = metrics.charWidth(MIN_CODE_POINT);
        
        for (int i = MIN_CODE_POINT + 1; i <= MAX_CODE_POINT; i++) {
            int charWidth = metrics.charWidth(i);
            if (charWidth < minCharWidth) {
                minCharWidth = charWidth;
            }
            if (charWidth > maxCharWidth) {
                maxCharWidth = charWidth;
            }
        }
        System.out.println("minCharWidth = " + minCharWidth + " maxCharWidth = " + maxCharWidth);
        cellWidth = maxCharWidth; // the width of a column/cell in the grid will be
        // equal to the maximum character width, to ensure that all characters
        // can be drawn without going outside the bounds of their respective column.
        cellHeight = metrics.getHeight();
    }

    private void drawGridLines(Graphics g) {
        g.setColor(Color.WHITE);
        // draw horizontal row lines
        for (int row = 0; row < numRows; row++) {
            g.drawLine(0, row * cellHeight, getWidth(), row * cellHeight);
        }
        // draw vertical column lines
        for (int col = 0; col < numCols; col++) {
            g.drawLine(col * cellWidth, 0, col * cellWidth, getHeight());
        }
    }

    private void drawStreamers(Graphics2D g2d) {
        // clear the cellsDrawnIn matrix
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                cellsDrawnIn[row][col] = false;
            }
        }
        for (Streamer streamer : listStreamers) {
            drawStreamer(g2d, streamer);
        }
    }

    private void drawStreamer(Graphics2D g2d, Streamer s) {

        // look at each character in the streamer's text
        for (int i = 0; i < s.text.length(); i++) {
            Color col = s.speed < 15.0f ? DARK_GREEN : LIGHT_GREEN; // decide what
            // color to draw the streamer in based upon its speed
            if (i == 0) {
                col = Color.WHITE; // head of the streamer is white
            } else if (i <= 3) {
                col = Color.GRAY; // first few characters of streamer after the
                // head are grey
            }
            g2d.setColor(col);
            int nCharIndex = Math.abs((i - ((int) s.row))) % s.text.length(); // magic to
            // give the illusion that characters in the streamer's text stay in the same
            // position on screen whilst the streamer moves down the screen.

            int rowDrawingIn = ((int) s.row) - i;
            // only draw a character at a given row/col position if a character has not already been drawn at that position
            if (rowDrawingIn >= 0 && rowDrawingIn < numRows && cellsDrawnIn[rowDrawingIn][s.column] == false) {
                g2d.drawString("" + s.text.charAt(nCharIndex),
                        s.column * cellWidth, rowDrawingIn * cellHeight);
                cellsDrawnIn[rowDrawingIn][s.column] = true;
            }
        }
    }

    // returns a random character between MIN_CODE_POINT and MAX_CODE_POINT
    private char randomCharacter() {
        //return (char) ((Math.random() * 0x1EF) + 0x00CO);
        //return (char) HelperMethods.mathRandom(33,128); // random ASCII character between 33 and 127
        return (char) HelperMethods.mathRandom(MIN_CODE_POINT, MAX_CODE_POINT+1); // character in the 'cryllic' character set
    }
}
