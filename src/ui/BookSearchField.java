package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente de búsqueda de libros con autocompletado en tiempo real.
 * Reemplaza el JComboBox con un campo de texto que filtra la lista de libros
 * conforme el usuario escribe.
 */
public class BookSearchField extends JPanel {

    private final JTextField searchField;
    private final JButton dropdownBtn;
    private final JPopupMenu popup;
    private final JList<String> resultList;
    private final DefaultListModel<String> listModel;

    private List<String> allBooks = new ArrayList<>();
    private String selectedBook = null;
    private boolean isAdjusting = false;
    private Consumer<String> onSelectionChanged;


    private Color bgColor = Color.WHITE;
    private Color fgColor = Color.BLACK;
    private Color selectionBg = new Color(70, 130, 180);

    public BookSearchField() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);


        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setToolTipText("Escribe para buscar un libro...");
        utils.ReadingCalculator.silenciarCampo(searchField);
        add(searchField, BorderLayout.CENTER);


        dropdownBtn = new JButton("▼");
        dropdownBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dropdownBtn.setFocusable(false);
        dropdownBtn.setMargin(new Insets(0, 4, 0, 4));
        dropdownBtn.setToolTipText("Ver todos los libros");
        add(dropdownBtn, BorderLayout.EAST);


        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setFixedCellHeight(28);
        resultList.setCellRenderer(new BookListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        popup.add(scrollPane, BorderLayout.CENTER);
        popup.setFocusable(false);

        configurarEventos();
    }

    private void configurarEventos() {

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }
        });


        dropdownBtn.addActionListener(e -> {
            if (popup.isVisible()) {
                popup.setVisible(false);
            } else {
                filtrarMostrandoTodos();
            }
        });


        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        if (!popup.isVisible())
                            filtrarMostrandoTodos();
                        int next = Math.min(resultList.getSelectedIndex() + 1, listModel.size() - 1);
                        resultList.setSelectedIndex(next);
                        resultList.ensureIndexIsVisible(next);
                    }
                    case KeyEvent.VK_UP -> {
                        int prev = Math.max(resultList.getSelectedIndex() - 1, 0);
                        resultList.setSelectedIndex(prev);
                        resultList.ensureIndexIsVisible(prev);
                    }
                    case KeyEvent.VK_ENTER -> {
                        String sel = resultList.getSelectedValue();
                        if (sel != null) {
                            seleccionarLibro(sel);
                        } else if (listModel.size() > 0) {
                            seleccionarLibro(listModel.get(0));
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> {
                        popup.setVisible(false);
                        if (selectedBook != null)
                            searchField.setText(selectedBook);
                    }
                }
            }
        });


        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String sel = resultList.getSelectedValue();
                if (sel != null)
                    seleccionarLibro(sel);
            }
        });


        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    filtrarMostrandoTodos();
                } else {
                    filtrar();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                Timer t = new Timer(200, ev -> {
                    if (!searchField.hasFocus() && !resultList.hasFocus()) {
                        popup.setVisible(false);
                        String current = searchField.getText().trim();
                        if (selectedBook != null && !current.equals(selectedBook) && !current.isEmpty()) {
                            searchField.setText(selectedBook);
                        }
                    }
                });
                t.setRepeats(false);
                t.start();
            }
        });
    }

    /** Filtra ocultando el popup si el campo está vacío (modo escritura). */
    private void filtrar() {
        if (isAdjusting)
            return;
        String query = searchField.getText().trim().toLowerCase();
        listModel.clear();

        if (query.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        List<String> startsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (String book : allBooks) {
            String lowerBook = book.toLowerCase();
            if (query.isEmpty() || lowerBook.startsWith(query)) {
                startsWith.add(book);
            } else if (lowerBook.contains(query)) {
                contains.add(book);
            }
        }

        for (String b : startsWith) listModel.addElement(b);
        for (String b : contains) listModel.addElement(b);

        boolean hayCoincidencias = !listModel.isEmpty();

        if (!hayCoincidencias) {
            popup.setVisible(false);
        } else {
            actualizarOShowPopup();
            if (selectedBook != null) {
                int idx = listModel.indexOf(selectedBook);
                if (idx >= 0)
                    resultList.setSelectedIndex(idx);
            }
        }
    }

    /** Muestra todos los libros sin filtro (modo desplegable del botón ▼). */
    private void filtrarMostrandoTodos() {
        if (isAdjusting)
            return;
        listModel.clear();
        for (String book : allBooks)
            listModel.addElement(book);
        if (listModel.isEmpty())
            return;

        if (selectedBook != null) {
            int idx = listModel.indexOf(selectedBook);
            if (idx >= 0) {
                resultList.setSelectedIndex(idx);
                resultList.ensureIndexIsVisible(idx);
            }
        }
        actualizarOShowPopup();
    }

    /**
     * Si el popup ya está visible, lo actualiza en sitio.
     * Si no, abre uno nuevo. Siempre en el EDT via invokeLater para
     * que el ancho del panel esté disponible.
     */
    private void actualizarOShowPopup() {
        if (!isShowing() || listModel.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (listModel.isEmpty() || !isShowing()) {
                popup.setVisible(false);
                return;
            }
            int ancho = getWidth();
            int alto = Math.min(listModel.size() * 28 + 4, 300);
            popup.setPreferredSize(new Dimension(ancho, alto));
            popup.pack();

            Component window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                Point p = new Point(0, 0);
                SwingUtilities.convertPointToScreen(p, this);
                GraphicsConfiguration gc = window.getGraphicsConfiguration();
                Rectangle screenBounds = gc.getBounds();
                
                int spaceBelow = screenBounds.y + screenBounds.height - (p.y + getHeight());
                
                if (spaceBelow < alto + 20 && p.y > alto) {
                    popup.show(this, 0, -alto);
                } else {
                    popup.show(this, 0, getHeight());
                }
            } else {
                popup.show(this, 0, getHeight());
            }
        });
    }

    private void seleccionarLibro(String libro) {
        isAdjusting = true;
        selectedBook = libro;
        searchField.setText(libro);
        popup.setVisible(false);
        isAdjusting = false;
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(libro);
        }
    }
    /** Carga la lista completa de libros disponibles */
    public void setBooks(List<String> books) {
        this.allBooks = new ArrayList<>(books);
    }

    /** Devuelve el libro actualmente seleccionado (puede ser null) */
    public String getSelectedBook() {
        return selectedBook;
    }

    /** Selecciona un libro por nombre sin disparar el evento */
    public void setSelectedBook(String libro) {
        this.selectedBook = libro;
        isAdjusting = true;
        searchField.setText(libro != null ? libro : "");
        isAdjusting = false;
    }

    /** Registra el callback que se llama al seleccionar un libro */
    public void setOnSelectionChanged(Consumer<String> callback) {
        this.onSelectionChanged = callback;
    }

    /** Aplica colores del tema claro/oscuro al componente */
    public void applyTheme(Color bg, Color fg, Color inputBg) {
        this.bgColor = inputBg;
        this.fgColor = fg;
        searchField.setBackground(inputBg);
        searchField.setForeground(fg);
        searchField.setCaretColor(fg);
        searchField.setBorder(BorderFactory.createLineBorder(
                fg.equals(Color.WHITE) ? Color.GRAY : Color.LIGHT_GRAY));

        dropdownBtn.setBackground(inputBg);
        dropdownBtn.setForeground(fg);
        dropdownBtn.setOpaque(true);
        dropdownBtn.setBorder(BorderFactory.createLineBorder(
                fg.equals(Color.WHITE) ? Color.GRAY : Color.LIGHT_GRAY));

        resultList.setBackground(inputBg);
        resultList.setForeground(fg);
        resultList.setSelectionBackground(selectionBg);
        resultList.setSelectionForeground(Color.WHITE);

        popup.setBackground(inputBg);
        SwingUtilities.updateComponentTreeUI(popup);
    }

    /** Renderer personalizado para los items de la lista */
    private class BookListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(4, 10, 4, 10));
            label.setText("📖 " + value);
            if (isSelected) {
                label.setBackground(selectionBg);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(bgColor);
                label.setForeground(fgColor);
            }
            return label;
        }
    }
}