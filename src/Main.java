
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Matrix Rain");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MatrixRainGUI content = new MatrixRainGUI();
        window.setContentPane(content);
        window.pack();
        window.setVisible(true);
    }
}
