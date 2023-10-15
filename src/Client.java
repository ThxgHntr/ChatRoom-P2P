import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.*;

public class Client {
    private JPanel mainPanel;
    private JTextField chatField;
    private JButton sendBtn;
    private JPanel clientsPanel;
    private JPanel chatPanel;

    private final DatagramSocket ds;
    private final MulticastSocket ms;
    private final InetSocketAddress inetSA;
    private final NetworkInterface netInt;
    private final InetAddress group = InetAddress.getByName("239.0.0.1");
    private final int port = 1111;
    private final ClientModel client;
    private ClientModel opponent;
    private final ClientModelList clientModelList = new ClientModelList();
    private final CardLayout cardLayout;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public Client() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();
        ds = new DatagramSocket();
        ms = new MulticastSocket(port);
        inetSA = new InetSocketAddress(group, port);
        netInt = NetworkInterface.getByInetAddress(group);
        ms.joinGroup(inetSA, netInt);

        client = new ClientModel(ip, ds.getLocalPort());
        opponent = client;

        cardLayout = new CardLayout();
        chatPanel.setLayout(cardLayout);
        chatPanel.setPreferredSize(new Dimension(600, 400));

        clientsPanel.setLayout(new BoxLayout(clientsPanel, BoxLayout.Y_AXIS));

        int marginSize = 10;
        chatPanel.setBorder(new EmptyBorder(marginSize, marginSize, marginSize, marginSize));

        sendClientToMS("J");
        receiveRequest().start();
        receiveMessage().start();
        chatField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        sendBtn.addActionListener(e -> sendMessage());
    }

    // Thông báo rời chat
    public void sendLeavePacket() throws Exception {
        sendClientToMS("L");
        ms.leaveGroup(inetSA, netInt);
    }

    // Thông báo vào chat
    public void sendClientToMS(String code) throws IOException {
        byte[] buffer = (client + code).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        ms.send(packet);
    }

    // Nhận tin nhắn từ MultiCastSocket và xác định đối tượng ra vào
    public Thread receiveRequest() {
        return new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    ms.receive(packet);
                    String address = new String(packet.getData(), 0, packet.getLength());
                    char lastChar = address.charAt(address.length() - 1);
                    address = address.substring(0, address.length() - 1);
                    String[] msgSplit = address.split(":");
                    ClientModel newCl = new ClientModel(InetAddress.getByName(msgSplit[0]), Integer.parseInt(msgSplit[1]));
                    switch (lastChar) {
                        // Gởi thông báo có 1 đối tượng vừa vào chat và thông báo update để nó update
                        case 'J':
                            if (!clientModelList.contains(newCl)) {
                                clientModelList.add(newCl);
                                addToChatPanel(newCl);
                            }
                            for (ClientModel cl : clientModelList) {
                                byte[] sendData = (cl + "U").getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, group, port);
                                ms.send(sendPacket);
                            }
                            break;
                        // Thông báo rời chat
                        case 'L':
                            clientModelList.removeIf(cl -> cl.equals(newCl));
                            break;
                        // Update danh sách
                        case 'U':
                            if (!clientModelList.contains(newCl)) {
                                clientModelList.add(newCl);
                                addToChatPanel(newCl);
                            }
                            break;
                    }
                    loadClients();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // Gửi tin nhắn đến đối tượng hiện tại
    public void sendMessage() {
        try {
            String msg = chatField.getText();
            byte[] data = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, opponent.ip(), opponent.port());
            ds.send(sendPacket);
            chatField.setText("");
            addSendMessageLabel(msg.trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Hiển thị tin nhắn mình vừa gửi
    public void addSendMessageLabel(String msg) {
        int i = clientModelList.getItemIndexByPort(opponent.port());
        JLabel msgLabel = new JLabel("Bạn đã nói: " + msg);
        JPanel panel = (JPanel) chatPanel.getComponent(i);
        panel.add(msgLabel);
        panel.revalidate();
    }

    // Thêm tin nhắn nhận được vào đúng panel
    public void addReceiveMessageLabel(int index, String msg) {
        ClientModel cl = clientModelList.get(index);
        if (cl.equals(client)) {
            return;
        }
        JLabel msgLabel = new JLabel(cl + " đã nói: " + msg);
        JPanel panel = (JPanel) chatPanel.getComponent(index);
        panel.add(msgLabel);
        panel.revalidate();
    }

    // Thread để nhận tin nhắn
    public Thread receiveMessage() {
        return new Thread(() -> {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    ds.receive(packet);
                    int i = clientModelList.getItemIndexByPort(packet.getPort());
                    String msg = new String(packet.getData());
                    addReceiveMessageLabel(i, msg.trim());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Load danh sách các đối tượng
    private void loadClients() {
        clientsPanel.removeAll();
        for (ClientModel cl : clientModelList) {
            JButton clientButton = new JButton(cl.toString());
            clientButton.addActionListener(e -> {
                opponent = cl;
                cardLayout.show(chatPanel, cl.toString());
                enableButtons();
                clientButton.setEnabled(false);
            });
            clientsPanel.add(clientButton);
            clientsPanel.revalidate();
            clientsPanel.repaint();
        }
    }

    //Thêm panel chat riêng với đối tượng vào chatPanel
    public void addToChatPanel(ClientModel cl) {
        JPanel panel = new JPanel();
        JScrollPane chatScroll = new JScrollPane(panel);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(cl.toString()));
        chatPanel.add(panel, cl.toString());
    }

    public void enableButtons() {
        Component[] components = clientsPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                component.setEnabled(true);
            }
        }
    }
}
