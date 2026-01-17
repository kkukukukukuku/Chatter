package gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;

public class TextInput extends JFrame {
    static String username = "";
    public static JTextArea textArea = new JTextArea(username + ">", 100, 90);
    private static ActionListener sendListener;
    private static PrintWriter out;
    public TextInput() {

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.green);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    String message = getCurrentInput();
                    if (!message.trim().isEmpty()) {
                        if (username.isEmpty()) {
                            username = message;
                        }

                        try {
                            Document doc = textArea.getDocument();
                            int caretPos = textArea.getCaretPosition();
                            doc.insertString(caretPos, "\n" + username + ">", null);
                            textArea.setCaretPosition(caretPos + 2 + username.length());
                            if (sendListener != null) {
                                System.out.println("setting sendListener: '" + message + "'");
                                sendListener.actionPerformed(
                                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, message));
                            } else {
                                System.out.println("sendListener is not set");
                            }

                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("blank");
                        try {
                            Document doc = textArea.getDocument();
                            int caretPos = textArea.getCaretPosition();
                            doc.insertString(caretPos, "\n" + username + ">", null);
                            textArea.setCaretPosition(caretPos + 2 + username.length());
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        AbstractDocument doc = (AbstractDocument) textArea.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string,
                                     AttributeSet attr) throws BadLocationException {
                if (!isValidInsertPosition(offset, fb.getDocument())) {
                    offset = findCorrectInsertPosition(offset, fb.getDocument());
                }
                super.insertString(fb, offset, string, attr);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length)
                    throws BadLocationException {
                if (isDeletingPrompt(offset, length, fb.getDocument())) {
                    return;
                }
                super.remove(fb, offset, length);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                                AttributeSet attrs) throws BadLocationException {
                if (isDeletingPrompt(offset, length, fb.getDocument())) {
                    if (!isValidInsertPosition(offset, fb.getDocument())) {
                        offset = findCorrectInsertPosition(offset, fb.getDocument());
                    }
                    super.replace(fb, offset, 0, text, attrs);
                } else {
                    if (!isValidInsertPosition(offset, fb.getDocument())) {
                        offset = findCorrectInsertPosition(offset, fb.getDocument());
                    }
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isValidInsertPosition(int offset, Document document)
                    throws BadLocationException {
                String text = document.getText(0, document.getLength());
                int lineStart = text.lastIndexOf('\n', offset - 1);
                if (lineStart == -1) lineStart = 0;
                else lineStart++;

                return offset >= lineStart + 1;
            }

            private int findCorrectInsertPosition(int offset, Document document)
                    throws BadLocationException {
                String text = document.getText(0, document.getLength());
                int lineStart = text.lastIndexOf('\n', offset - 1);
                if (lineStart == -1) lineStart = 0;
                else lineStart++;

                return lineStart + 1;
            }

            private boolean isDeletingPrompt(int offset, int length, Document document)
                    throws BadLocationException {
                String text = document.getText(0, document.getLength());

                for (int i = 0; i < length; i++) {
                    int pos = offset + i;
                    int lineStart = text.lastIndexOf('\n', pos - 1);
                    if (lineStart == -1) lineStart = 0;
                    else lineStart++;

                    if (pos == lineStart + username.length()) {
                        return true;
                    }
                }
                return false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane);
    }
    public static String getCurrentInput() {
        String fullText = textArea.getText();
        String[] lines = fullText.split("\n");

        if (lines.length > 0) {
            String lastLine = lines[lines.length - 1];

            System.out.println("lastLine = '" + lastLine + "'");
            System.out.println("username = '" + username + "'");

            if (username != null && !username.isEmpty() && lastLine.startsWith(username + ">")) {
                String result = lastLine.substring(username.length() + 1);
                System.out.println("return (username): '" + result + "'");
                return result;
            } else if (lastLine.startsWith(">")) {
                String result = lastLine.substring(1);
                System.out.println("return (without username): '" + result + "'");
                return result;
            }
            System.out.println("return: '" + lastLine + "'");
            return lastLine;
        }

        System.out.println("no input");
        return "";
    }

    public static String getUserName() {
        if (username == null || username.isEmpty()) {
            String currentInput = getCurrentInput().trim();
            if (!currentInput.isEmpty()) {
                username = currentInput;
                System.out.println("TextInput: username is set: " + username);
            }
        }
        return username != null ? username : "";
    }

    public static void appendResponse(String name, String response) {
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> {
                textArea.append("\n" + name + ">" + response + "\n" + username + ">");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }
    public static void setOnSendListener(ActionListener listener) {
        sendListener = listener;
    }
    public static void setOut( PrintWriter writer) {
        out = writer;
    }

    public static PrintWriter getOut() {
        return out;
    }
}
