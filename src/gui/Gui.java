package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.io.PrintWriter;

public class Gui {
    private static String username = null;
    private static JTextField nicknameField;
    private static boolean isNicknameSet;
    private static JFrame frame;
    private static JPanel nicknamePanel;

    public static void display() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Chatter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLayout(new BorderLayout());
            nicknamePanel = new JPanel(new GridBagLayout());
            nicknameField = new JTextField(20);
            frame.getContentPane().setBackground(Color.DARK_GRAY);

            nicknamePanel = new JPanel(new FlowLayout());
            nicknamePanel.setBackground(Color.DARK_GRAY);

            nicknameField = new JTextField(20);
            nicknameField.setBackground(Color.DARK_GRAY);
            nicknameField.setForeground(Color.GREEN);
            nicknameField.setFont(new Font("Monospaced", Font.PLAIN, 14));
            nicknameField.setCaretColor(Color.GREEN);
            nicknameField.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));

            nicknameField.setSelectionColor(new Color(0, 100, 0));
            nicknameField.setSelectedTextColor(Color.WHITE);

            JButton confirmButton = new JButton("OK");
            confirmButton.setBackground(Color.DARK_GRAY);
            confirmButton.setForeground(Color.GREEN);
            confirmButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
            confirmButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
            confirmButton.setFocusPainted(false);

            JLabel nicknameLabel = new JLabel("enter your nickname: ");
            nicknameLabel.setForeground(Color.GREEN);
            nicknameLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
            nicknameLabel.setBackground(Color.DARK_GRAY);

            nicknamePanel.add(nicknameLabel);
            nicknamePanel.add(nicknameField);
            nicknamePanel.add(confirmButton);
            frame.add(nicknamePanel, BorderLayout.CENTER);
            ActionListener confirmAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    confirmButton.setBackground(Color.GREEN);
                    try {
                        Thread.sleep(100);
                        confirmButton.setBackground(Color.DARK_GRAY);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    username = nicknameField.getText().trim();
                    isNicknameSet = true;
                    if (!username.isEmpty()) {
                        frame.remove(nicknamePanel);
                        TextInput textInput = new TextInput();
                        frame.add(new JScrollPane(TextInput.textArea), BorderLayout.CENTER);
                        TextInput.setOnSendListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                PrintWriter out = TextInput.getOut();
                                String message = e.getActionCommand();
                                System.out.println("CLIENT [" + username + "]: " + message);
                                if (out != null) {
                                    out.println("[" + username + "]: " + message);
                                }
                            }
                        });
                        frame.revalidate();
                        frame.repaint();
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                "blank nickname",
                                "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            nicknameField.addActionListener(confirmAction);
            confirmButton.addActionListener(confirmAction);
            frame.setVisible(true);
        });
    }

    public static String getName() {
        return username;
    }
    public static void setName(String name){username = name;}
    public static boolean isNicknameSet(){
        return isNicknameSet;
    }
    public static JFrame getFrame() {return frame;}
}
//TODO separate inputArea and respondArea