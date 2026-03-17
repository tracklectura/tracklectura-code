package ui.charts;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Gráfico de líneas simples para Pág/Día, Pág/Hora y Pág/Mes, sin relleno
 * bajo la curva.
 */
public class PanelLineaSimple extends JPanel {

    private final List<String> fechas;
    private final List<Double> valores;
    private final boolean modoOscuro;

    public PanelLineaSimple(List<String> fechas, List<Double> valores, boolean modoOscuro) {
        this.fechas     = fechas;
        this.valores    = valores;
        this.modoOscuro = modoOscuro;
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (valores.isEmpty()) {
            g2.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.GRAY);
            g2.drawString("No hay datos para mostrar.", 50, 50);
            return;
        }

        Color colorTexto  = GraphChartColors.textoContraste(modoOscuro);
        Color colorEjes   = GraphChartColors.ejes(modoOscuro);
        Color colorLinea  = modoOscuro ? new Color(100, 149, 237) : new Color(41, 128, 185);
        Color colorPuntos = modoOscuro ? new Color(80, 129, 217) : new Color(31, 108, 165);

        int w = getWidth(), h = getHeight();
        int mIzq = 70, mDer = 80, mSup = 50, mInf = 100;
        int areaW = w - mIzq - mDer;
        int areaH = h - mSup - mInf;
        int n     = valores.size();

        double maxVal = Math.max(valores.stream().mapToDouble(Double::doubleValue).max().orElse(10), 10);

        // Grid horizontal
        int nGrid = 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= nGrid; i++) {
            int yGrid = h - mInf - (areaH * i / nGrid);
            g2.setColor(colorEjes);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4f}, 0));
            g2.drawLine(mIzq, yGrid, w - mDer, yGrid);
            String etiq = String.valueOf((int) (maxVal * i / nGrid));
            g2.setColor(colorTexto);
            g2.drawString(etiq, mIzq - g2.getFontMetrics().stringWidth(etiq) - 5, yGrid + 4);
        }
        g2.setStroke(new BasicStroke(1.5f));

        // Coordenadas
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            px[i] = mIzq + (n > 1 ? i * areaW / (n - 1) : areaW / 2);
            py[i] = h - mInf - (int) (areaH * valores.get(i) / maxVal);
        }

        // Línea conectora
        g2.setColor(colorLinea);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++)
            g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);

        // Puntos y etiquetas X
        for (int i = 0; i < n; i++) {
            // Fondo del punto
            g2.setColor(getBackground());
            g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);

            g2.setColor(colorPuntos);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(px[i] - 5, py[i] - 5, 10, 10);

            // Valor encima
            g2.setColor(colorTexto);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String val = String.valueOf((int) Math.round(valores.get(i)));
            g2.drawString(val, px[i] - g2.getFontMetrics().stringWidth(val) / 2, py[i] - 12);

            // Etiqueta eje X rotada
            AffineTransform old = g2.getTransform();
            g2.translate(px[i], h - mInf + 15);
            g2.rotate(Math.toRadians(45));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

            String labelStr = fechas.get(i);
            if (labelStr != null && labelStr.matches("\\d{4}-\\d{2}")) {
                String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
                int m = Integer.parseInt(labelStr.substring(5, 7));
                labelStr = meses[m - 1] + " " + labelStr.substring(0, 4);
            }
            g2.drawString(labelStr, 0, 0);
            g2.setTransform(old);
        }

        // Ejes
        g2.setColor(modoOscuro ? new Color(100, 100, 100) : Color.GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(mIzq, mSup, mIzq, h - mInf);
        g2.drawLine(mIzq, h - mInf, w - mDer, h - mInf);

        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString("Páginas Leídas", mIzq + 5, mSup - 10);
    }
}