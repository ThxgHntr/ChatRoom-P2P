import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Client client;
            try {
                client = new Client();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent w) {
                    try {
                        client.sendLeavePacket();
                        System.exit(0);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            frame.setContentPane(client.getMainPanel());
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
