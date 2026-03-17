package ui.charts;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Gráfico de línea de progreso acumulado de páginas.
 * Muestra una línea creciente con línea de meta (total del libro).
 */
public class PanelLineaAcumulada extends JPanel {

    private final List<String> fechas;
    private final List<Double> valores;
    private final int totalPaginas;
    private final boolean modoOscuro;

    public PanelLineaAcumulada(List<String> fechas, List<Double> valores, int totalPaginas, boolean modoOscuro) {
        this.fechas       = fechas;
        this.valores      = valores;
        this.totalPaginas = totalPaginas;
        this.modoOscuro   = modoOscuro;
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (valores.isEmpty()) {
            g.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.GRAY);
            g.drawString("No hay datos disponibles.", 50, 50);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color colorTexto   = GraphChartColors.texto(modoOscuro);
        Color colorEjes    = GraphChartColors.ejes(modoOscuro);
        Color colorLinea   = GraphChartColors.LINEA_PROGRESO;
        Color colorMeta    = GraphChartColors.LINEA_META;
        Color colorRelleno = new Color(70, 160, 210, 40);

        int w = getWidth(), h = getHeight();
        int mIzq = 70, mDer = 80, mSup = 50, mInf = 150;
        int areaW = w - mIzq - mDer;
        int areaH = h - mSup - mInf;
        int n     = valores.size();

        double maxVal = Math.max(
                valores.stream().mapToDouble(Double::doubleValue).max().orElse(1),
                totalPaginas > 0 ? totalPaginas : 1);

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

        // Línea de meta
        if (totalPaginas > 0) {
            int yMeta = h - mInf - (int) (areaH * totalPaginas / maxVal);
            g2.setColor(colorMeta);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8f, 4f}, 0));
            g2.drawLine(mIzq, yMeta, w - mDer, yMeta);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("META: " + totalPaginas + " págs", w - mDer - 120, yMeta - 5);
            g2.setStroke(new BasicStroke(1.5f));
        }

        // Coordenadas
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            px[i] = mIzq + (n > 1 ? i * areaW / (n - 1) : areaW / 2);
            py[i] = h - mInf - (int) (areaH * valores.get(i) / maxVal);
        }

        // Relleno bajo la línea
        Polygon relleno = new Polygon();
        relleno.addPoint(px[0], h - mInf);
        for (int i = 0; i < n; i++) relleno.addPoint(px[i], py[i]);
        relleno.addPoint(px[n - 1], h - mInf);
        g2.setColor(colorRelleno);
        g2.fillPolygon(relleno);

        // Línea principal
        g2.setColor(colorLinea);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++)
            g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);

        // Puntos y etiquetas
        for (int i = 0; i < n; i++) {
            g2.setColor(colorLinea);
            g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);

            g2.setColor(colorTexto);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            String val = String.valueOf((int) Math.round(valores.get(i)));
            g2.drawString(val, px[i] - g2.getFontMetrics().stringWidth(val) / 2, py[i] - 7);

            AffineTransform old = g2.getTransform();
            g2.translate(px[i], h - mInf + 12);
            g2.rotate(Math.toRadians(45));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            String[] partes = fechas.get(i).split(";");
            for (int j = 0; j < partes.length; j++)
                g2.drawString(partes[j], 0, j * 11);
            g2.setTransform(old);
        }

        // Ejes
        g2.setColor(colorEjes);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(mIzq, mSup, mIzq, h - mInf);
        g2.drawLine(mIzq, h - mInf, w - mDer, h - mInf);

        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString("Páginas acumuladas", mIzq + 5, mSup - 10);
    }
}