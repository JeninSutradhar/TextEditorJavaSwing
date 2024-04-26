package src;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Class for the Improved Text Editor application
public class ImprovedTextEditor extends JFrame {
    private JTextArea textArea;

    // Constructor to initialize the text editor
    public ImprovedTextEditor() {
        setTitle("Improved Text Editor"); // Set the title of the window
        setSize(600, 600); // Set the size of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set the close operation when the window is closed

        // Set the look and feel of the text area
        UIManager.put("TextArea.background", Color.BLACK); // Set background color
        UIManager.put("TextArea.foreground", Color.WHITE); // Set text color

        // Create a JTextArea for text input
        textArea = new JTextArea();
        textArea.setFont(new Font("Menlo", Font.PLAIN, 12)); // Set font style and size
        JScrollPane scrollPane = new JScrollPane(textArea); // Create a scroll pane to contain the text area
        add(scrollPane); // Add the scroll pane to the frame

        textArea.setCaretColor(Color.WHITE); // Set the text cursor color to white

        // Add line numbers
        JTextArea lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(Color.DARK_GRAY);
        lineNumbers.setForeground(Color.WHITE);
        lineNumbers.setEditable(false);
        scrollPane.setRowHeaderView(lineNumbers);
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int caretPosition = textArea.getDocument().getLength();
                Element root = textArea.getDocument().getDefaultRootElement();
                String text = "1\n";
                for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
                    text += i + "\n";
                }
                return text;
            }

            // Update line numbers when text is inserted
            @Override
            public void insertUpdate(DocumentEvent e) {
                lineNumbers.setText(getText());
            }

            // Update line numbers when text is removed
            @Override
            public void removeUpdate(DocumentEvent e) {
                lineNumbers.setText(getText());
            }

            // Update line numbers when text is changed
            @Override
            public void changedUpdate(DocumentEvent e) {
                lineNumbers.setText(getText());
            }
        });

        // Create menu bar and menus
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save-As");
        JMenuItem closeItem = new JMenuItem("Close");

        JMenu editMenu = new JMenu("Edit");
        JMenu fontMenu = new JMenu("Font");
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font : fonts) {
            fontMenu.add(new JMenuItem(font)).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    textArea.setFont(new Font(((JMenuItem) e.getSource()).getText(), Font.PLAIN, 12));
                }
            });
        }
        editMenu.add(fontMenu);

        // Search menu bar
        JMenu searchMenu = new JMenu("Search");
        final JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Type what to search and press Enter");
        searchMenu.add(searchField);
        JMenuItem findItem = new JMenuItem("Find");
        findItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textArea.getText();
                String searchText = searchField.getText().toLowerCase();
                int pos = 0;
                while ((pos = text.indexOf(searchText, pos)) >= 0) {
                    try {
                        textArea.getHighlighter().addHighlight(pos, pos + searchText.length(),
                                DefaultHighlighter.DefaultPainter);
                        pos += searchText.length();
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Help menu for documentation
        JMenu helpMenu = new JMenu("Help");
        JMenuItem docsItem = new JMenuItem("Documentation");

        docsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("https://github.com/JeninSutradhar"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add action listeners for file menu items
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Add items to file menu
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(closeItem);
        menuBar.add(fileMenu); // Add file menu to menu bar
        menuBar.add(editMenu); // Add edit menu to menu bar
        
        // Add search menu to menu bar
        searchMenu.add(findItem);
        menuBar.add(searchMenu);
        
        // Add help menu to menu bar
        helpMenu.add(docsItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar); // Set the menu bar for the frame

        // Apply syntax highlighting
        applySyntaxHighlighting();

        // Apply auto-indentation
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    autoIndent();
                }
            }
        });

        // Display word count
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateWordCount();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateWordCount();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateWordCount();
            }
        });
    }

    // Method to open a file
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                textArea.read(reader, null);
                reader.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to save a file
    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                textArea.write(writer);
                writer.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to apply syntax highlighting
    private void applySyntaxHighlighting() {
        SyntaxHighlighter.highlight(textArea);
    }

    // Method to auto-indent text
    private void autoIndent() {
        int caretPosition = textArea.getCaretPosition();
        int lineStart = caretPosition;
        while (lineStart > 0 && textArea.getText().charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        String lineText = textArea.getText().substring(lineStart, caretPosition);
        final int[] indentation = {0}; // Declare as final array to make it effectively final
        for (int i = 0; i < lineText.length(); i++) {
            if (Character.isWhitespace(lineText.charAt(i))) {
                indentation[0]++;
            } else {
                break;
            }
        }
        SwingUtilities.invokeLater(() -> {
            textArea.insert("\n" + " ".repeat(indentation[0]), caretPosition);
        });
    }

    // Method to update word count and display in the window title
    private void updateWordCount() {
        String text = textArea.getText();
        int words = text.isEmpty() ? 0 : text.split("\\s+").length;
        int characters = text.length();
        int lines = textArea.getLineCount();
        // Display word count, character count, and line count in the window title
        setTitle("Text Editor - Words: " + words + ", Characters: " + characters + ", Lines: " + lines);
    }

    // Main method to start the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImprovedTextEditor editor = new ImprovedTextEditor();
            editor.setVisible(true);
        });
    }
}

// Class for syntax highlighting
class SyntaxHighlighter {
    // Method to highlight keywords in the text area
    public static void highlight(JTextArea textArea) {
        DefaultHighlighter highlighter = (DefaultHighlighter) textArea.getHighlighter();
        DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        String[] keywords = {"if", "else", "for", "while", "switch", "case", "break", "continue", "return", "class", "public", "private", "protected"};
        for (String keyword : keywords) {
            highlightWord(textArea, keyword.toLowerCase(), painter); // Convert keyword to lowercase
        }
    }

    // Method to highlight occurrences of a word in the text area
    private static void highlightWord(JTextArea textArea, String word, DefaultHighlighter.DefaultHighlightPainter painter) {
        String text = textArea.getText().toLowerCase(); // Convert text to lowercase
        int pos = 0;
        while ((pos = text.indexOf(word, pos)) >= 0) {
            try {
                textArea.getHighlighter().addHighlight(pos, pos + word.length(), painter);
                textArea.setSelectionColor(Color.BLUE); // Change the foreground color
                pos += word.length();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}
