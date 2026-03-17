package ui.charts;

import javax.swing.JPanel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Heatmap de consistencia estilo GitHub.
 */
public class PanelHeatmap extends JPanel {

    private final java.util.Map<LocalDate, Double> datos = new java.util.HashMap<>();
    private final boolean modoOscuro;

    public PanelHeatmap(List<String> fechasStr, List<Double> valores, boolean modoOscuro) {
        this.modoOscuro = modoOscuro;
        for (int i = 0; i < fechasStr.size(); i++) {
            try {
                String f  = fechasStr.get(i).contains(";") ? fechasStr.get(i).split(";")[0] : fechasStr.get(i);
                LocalDate ld = GraphDateUtils.parsearFecha(f);
                if (ld != null) {
                    datos.put(ld, valores.get(i));
                }
            } catch (Exception e) {
                System.err.println("[PanelHeatmap] Error parsing date: " + e.getMessage());
            }
        }
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellSize = 15, gap = 3;
        int gridH    = 7 * (cellSize + gap);
        int legendH  = 50;
        int marginY  = (getHeight() - gridH - legendH) / 2;
        if (marginY < 40) marginY = 40;

        LocalDate hoy   = LocalDate.now();
        LocalDate inicio = hoy.minusMonths(6).with(java.time.DayOfWeek.MONDAY);

        long numDiasTotal    = java.time.temporal.ChronoUnit.DAYS.between(inicio, hoy) + 1;
        int  semanasVisibles = (int) Math.ceil(numDiasTotal / 7.0) + 1;
        int  gridW           = semanasVisibles * (cellSize + gap);
        int  marginX         = (getWidth() - gridW) / 2;
        if (marginX < 60) marginX = 60;

        // Etiquetas días de la semana
        String[] diasNames = { "Lun", "", "Mié", "", "Vie", "", "Dom" };
        g2.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i < 7; i++) {
            if (!diasNames[i].isEmpty())
                g2.drawString(diasNames[i], marginX - 40, marginY + i * (cellSize + gap) + 12);
        }

        int col = 0;
        LocalDate actual    = inicio;
        int lastMonth       = -1;

        while (!actual.isAfter(hoy)) {
            int fila = actual.getDayOfWeek().getValue() - 1;
            int xBox = marginX + col * (cellSize + gap);
            int yBox = marginY + fila * (cellSize + gap);

            Double pagsValue = datos.getOrDefault(actual, 0.0);
            g2.setColor(colorIntensidad(pagsValue));
            g2.fillRoundRect(xBox, yBox, cellSize, cellSize, 3, 3);

            if (actual.getMonthValue() != lastMonth) {
                String mesStr = actual.getMonth().getDisplayName(
                        java.time.format.TextStyle.SHORT, java.util.Locale.getDefault());
                g2.setColor(modoOscuro ? Color.GRAY : Color.DARK_GRAY);
                g2.drawString(mesStr, xBox, marginY - 10);
                lastMonth = actual.getMonthValue();
            }

            if (fila == 6) col++;
            actual = actual.plusDays(1);
        }

        // Leyenda
        int lx = marginX;
        int ly = marginY + 7 * (cellSize + gap) + 30;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String[] rangosText = { "0", "1-20", "21-50", "51-80", "80+" };
        for (int i = 0; i < rangosText.length; i++) {
            int rx = lx + i * 85;
            g2.setColor(colorLeyenda(i));
            g2.fillRoundRect(rx, ly, cellSize, cellSize, 3, 3);
            g2.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            g2.drawString(rangosText[i] + " págs", rx + cellSize + 5, ly + 12);
        }
    }

    private Color colorIntensidad(Double pags) {
        if (pags <= 0)  return modoOscuro ? new Color(45, 45, 45) : new Color(230, 230, 230);
        if (pags <= 20) return new Color(155, 200, 255);
        if (pags <= 50) return new Color(100, 160, 240);
        if (pags <= 80) return new Color(40, 100, 200);
        return new Color(15, 60, 150);
    }

    private Color colorLeyenda(int nivel) {
        return switch (nivel) {
            case 0  -> modoOscuro ? new Color(45, 45, 45) : new Color(230, 230, 230);
            case 1  -> new Color(155, 200, 255);
            case 2  -> new Color(100, 160, 240);
            case 3  -> new Color(40, 100, 200);
            default -> new Color(15, 60, 150);
        };
    }
}