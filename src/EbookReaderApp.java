import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Clasa Book care definește atributele unei cărți
class Book {
    String title;
    String author;
    boolean isAvailable;
    Color color;
    String content;

    public Book(String title, String author, Color color, String content) {
        this.title = title;
        this.author = author;
        this.isAvailable = true;  // Implicit, cartea este disponibilă
        this.color = color;
        this.content = content; // Setăm conținutul cărții
    }

    public String toString() {
        return "'" + title + "' de " + author + " - " + (isAvailable ? "AVAILABLE" : "UNAVAILABLE");
    }

    public String toFileString() {
        return title + "|" + author + "|" + isAvailable + "|" + color.getRGB() + "|" + content.replace("\n", "\\n");
    }

    public static Book fromFileString(String str) {
        String[] parts = str.split("\\|");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Fișierul de date este corupt: " + str);
        }
        Book book = new Book(parts[0], parts[1], new Color(Integer.parseInt(parts[3])), parts[4].replace("\\n", "\n"));
        book.isAvailable = Boolean.parseBoolean(parts[2]);
        return book;
    }
}

// Clasa Library care gestionează cărțile din bibliotecă
class Library {
    private List<Book> books;
    private static final String FILE_NAME = "library_data.txt";

    public Library() {
        books = new ArrayList<>();
        loadBooks();
    }

    // Adăugarea unei cărți
    public void addBook(String title, String author, Color color, String content) {
        books.add(new Book(title, author, color, content));
        saveBooks();
    }

    // Împrumutarea unei cărți
    public String borrowBook(String title) {
        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                if (book.isAvailable) {
                    book.isAvailable = false;
                    saveBooks();
                    return "YOU BORROWED THE BOOK '" + title + "'.";
                } else {
                    return "BOOK '" + title + "' NOT AVAILABLE.";
                }
            }
        }
        return "BOOK '" + title + "' IT DOESN'T EXIST.";
    }

    // Returnarea unei cărți
    public String returnBook(String title) {
        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                if (!book.isAvailable) {
                    book.isAvailable = true;
                    saveBooks();
                    return "BOOK '" + title + "' IT HAS BEEN RETURNED.";
                } else {
                    return "BOOK '" + title + "' IT IS NOT BORROWED.";
                }
            }
        }
        return "BOOK '" + title + "' IT DOESN'T EXIST.";
    }

    // Setează conținutul unei cărți
    public void setContent(String title, String content) {
        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                book.content = content;
                saveBooks();
                return;
            }
        }
    }

    // Obține conținutul unei cărți
    public String getContent(String title) {
        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                return book.isAvailable ? "BOOK '" + title + "' NOT AVAILABLE." : book.content;
            }
        }
        return "BOOK '" + title + "' IT DOESN'T EXIST.";
    }

    // Șterge o carte
    public void deleteBook(String title) {
        books.removeIf(book -> book.title.equalsIgnoreCase(title));
        saveBooks();
    }

    // Returnează toate cărțile
    public List<Book> getBooks() {
        return books;
    }

    // Returnează doar cărțile împrumutate
    public List<Book> getBorrowedBooks() {
        List<Book> borrowedBooks = new ArrayList<>();
        for (Book book : books) {
            if (!book.isAvailable) {
                borrowedBooks.add(book);
            }
        }
        return borrowedBooks;
    }

    private void saveBooks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Book book : books) {
                writer.write(book.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBooks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    books.add(Book.fromFileString(line));
                } catch (IllegalArgumentException e) {
                    System.err.println("ERROR LOADING THE BOOK: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Clasa principală pentru aplicația GUI
public class EbookReaderApp extends JFrame {

    private Library library;
    private JPanel globalLibraryPanel;
    private JPanel personalLibraryPanel;
    private JTextField globalSearchField;
    private JTextField personalSearchField;

    public EbookReaderApp() {
        library = new Library();
        setupUI();
    }

    private void setupUI() {
        setBackground(Color.darkGray);
        setTitle("EBOOK READER");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Panou pentru librăria globală
        JPanel globalTab = new JPanel(new BorderLayout());
        globalSearchField = new JTextField();
        globalSearchField.setBackground(Color.lightGray);
        globalSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshLibrariesDisplay();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshLibrariesDisplay();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshLibrariesDisplay();
            }
        });

        globalTab.add(globalSearchField, BorderLayout.NORTH);
        globalLibraryPanel = new JPanel();
        globalLibraryPanel.setBackground(Color.darkGray);
        globalLibraryPanel.setLayout(new GridLayout(0, 4, 10, 10));
        JScrollPane globalScrollPane = new JScrollPane(globalLibraryPanel);
        globalScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        globalScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        globalTab.add(globalScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("\uD83C\uDF0D", globalTab);

        // Panou pentru librăria personală
        JPanel personalTab = new JPanel(new BorderLayout());
        personalSearchField = new JTextField();
        personalSearchField.setBackground(Color.lightGray);
        personalSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshLibrariesDisplay();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshLibrariesDisplay();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshLibrariesDisplay();
            }
        });

        personalTab.add(personalSearchField, BorderLayout.NORTH);
        personalLibraryPanel = new JPanel();
        personalLibraryPanel.setBackground(Color.darkGray);
        personalLibraryPanel.setLayout(new GridLayout(0, 4, 10, 10));
        JScrollPane personalScrollPane = new JScrollPane(personalLibraryPanel);
        personalScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        personalScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        personalTab.add(personalScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("\uD83D\uDCDA", personalTab);

        add(tabbedPane, BorderLayout.CENTER);

        // Panou pentru butoane
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 6, 1000, 1000));

        JButton addBookButton = new JButton("➕\uD83D\uDCD8");
        addBookButton.setBackground(Color.gray);

        addBookButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String title = JOptionPane.showInputDialog("TITLE:");
                String author = JOptionPane.showInputDialog("AUTHOR:");
                Color color = JColorChooser.showDialog(null, "COLOR", Color.WHITE);
                String content = JOptionPane.showInputDialog("TYPE THE STORY:");
                if (title != null && author != null && color != null && !title.isEmpty() && !author.isEmpty()) {
                    library.addBook(title, author, color, content != null ? content : "");
                    refreshLibrariesDisplay();
                }
            }
        });
        buttonPanel.add(addBookButton);

        JButton closeButton = new JButton("❌");
        closeButton.addActionListener(e -> System.exit(0));
        closeButton.setBackground(Color.gray);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        refreshLibrariesDisplay();
        setVisible(true);
    }

    private void refreshLibrariesDisplay() {
        String globalSearchText = globalSearchField.getText().toLowerCase();
        String personalSearchText = personalSearchField.getText().toLowerCase();

        globalLibraryPanel.removeAll();
        personalLibraryPanel.removeAll();

        for (Book book : library.getBooks()) {
            if (book.title.toLowerCase().contains(globalSearchText)) {
                JPanel bookPanel = createBookPanel(book);
                globalLibraryPanel.add(bookPanel);
            }
        }

        for (Book book : library.getBorrowedBooks()) {
            if (book.title.toLowerCase().contains(personalSearchText)) {
                JPanel bookPanel = createBookPanel(book);
                personalLibraryPanel.add(bookPanel);
            }
        }

        globalLibraryPanel.revalidate();
        globalLibraryPanel.repaint();
        personalLibraryPanel.revalidate();
        personalLibraryPanel.repaint();
    }

    private JPanel createBookPanel(Book book) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 300));
        panel.setBackground(book.color);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 35;
        gbc.gridy = 60;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("TITLE: " + book.title);
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        JLabel authorLabel = new JLabel("AUTHOR: " + book.author);
        panel.add(authorLabel, gbc);

        gbc.gridy++;
        JLabel statusLabel = new JLabel(book.isAvailable ? "AVAILABLE" : "UNAVAILABLE");
        panel.add(statusLabel, gbc);

        gbc.gridy++;
        JButton borrowButton = new JButton(book.isAvailable ? "\uD83C\uDFAB" : "\uD83D\uDD01");
        borrowButton.setBackground(Color.gray);
        borrowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = book.isAvailable ? library.borrowBook(book.title) : library.returnBook(book.title);
                JOptionPane.showMessageDialog(null, message);
                refreshLibrariesDisplay();
            }
        });
        panel.add(borrowButton, gbc);

        gbc.gridy++;
        JButton viewButton = new JButton("READ");
        viewButton.setBackground(Color.gray);
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String content = library.getContent(book.title);
                JTextArea textArea = new JTextArea(content);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                JOptionPane.showMessageDialog(null, scrollPane, "READ", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panel.add(viewButton, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        JButton deleteButton = new JButton("❌");
        deleteButton.setBackground(Color.gray);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                library.deleteBook(book.title);
                refreshLibrariesDisplay();
            }
        });
        panel.add(deleteButton, gbc);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EbookReaderApp app = new EbookReaderApp();
            app.setVisible(true);
        });
    }
}
