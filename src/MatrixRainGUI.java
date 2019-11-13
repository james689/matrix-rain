
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

    private List<Streamer> listStreamers = null;
    private int nMaxStreamers = 200;
    private int numCols, numRows;
    private Font monospaceFont = new Font("Courier New", Font.PLAIN, 10);
    private Timer timer;

    public MatrixRainGUI() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.BLACK);
        timer = new Timer(100, new TimerListener());
        timer.start();
    }

    private class Streamer {

        int column = 0; // x coordinate on screen (will be given a random column in future)
        float fPosition = 0.0f; // y coordinate, the head of the stream
        String text;

        public Streamer() {
            prepareStreamer();
        }

        public void prepareStreamer() {
            column = (int) (Math.random() * numCols);
            fPosition = 0.0f;
            text = "ABCDEFGHIJKLMNO";
        }
    }

    class TimerListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            // called when timer fires
            if (listStreamers == null) {
                return; // no streamers created yet
            }

            for (Streamer s : listStreamers) {
                s.fPosition += 1;
                //s.fPosition += 10.0 * fElapsedTime;
                if (s.fPosition > numRows) {
                    s.fPosition = 0;
                }
            }
            repaint();
        }
    }

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
        // the width off of
        System.out.println("charWidth = " + charWidth);
        int charHeight = metrics.getAscent() + metrics.getDescent();
        System.out.println("charHeight = " + charHeight);

        if (listStreamers == null) {
            // this is the first time we are creating the streamers
            listStreamers = new ArrayList<>();
            double panelWidth = getWidth();
            double panelHeight = getHeight();
            System.out.println("panel width = " + panelWidth + " panel height = " + panelHeight);
            // calculate how many rows and columns the panel can support
            // based upon the font size.
            numCols = (int) Math.ceil(panelWidth / charWidth);
            numRows = (int) Math.ceil(panelHeight / charHeight);
            System.out.println("numCols = " + numCols + " numRows = " + numRows);

            for (int i = 0; i < nMaxStreamers; i++) {
                Streamer s = new Streamer();
                listStreamers.add(s);
            }
        }

        // draw streamers on screen
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(Color.GREEN);
        for (Streamer s : listStreamers) {
            drawStreamer(g2d, s, metrics.getAscent(), charWidth, charHeight);
        }
    }

    private void drawStreamer(Graphics2D g2d, Streamer s, int ascent, int charWidth, int charHeight) {
        // see https://stackoverflow.com/questions/45227294/setting-drawstring-so-0-0-is-inside-the-draw-area
        //g2d.translate(0,ascent);
        for (int i = 0; i < s.text.length(); i++) {
            char c = s.text.charAt(i);
            g2d.drawString("" + c, s.column * charWidth, (s.fPosition - i) * charHeight);
        }
        //g2d.translate(0,0);
    }
}
