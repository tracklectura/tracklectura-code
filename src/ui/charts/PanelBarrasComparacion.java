package ui.charts;

import model.DataPoint;
import javax.swing.JPanel;
import java.awt.*;
import java.util.List;

/**
 * Gráfico de barras horizontales comparando PPM media de libros terminados.
 */
public class PanelBarrasComparacion extends JPanel {

    private final List<DataPoint> datos;
    private final boolean modoOscuro;

    public PanelBarrasComparacion(List<DataPoint> datos, boolean modoOscuro) {
        this.datos      = datos;
        this.modoOscuro = modoOscuro;
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (datos.isEmpty()) {
            g2.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.drawString("No hay libros terminados con sesiones registradas.", 40, 60);
            return;
        }

        Color colorTexto  = GraphChartColors.textoContraste(modoOscuro);
        Color colorGrid   = GraphChartColors.grid(modoOscuro);
        Color colorBarra  = GraphChartColors.BARRA_COMP;
        Color colorTop    = GraphChartColors.BARRA_TOP;

        int w = getWidth(), h = getHeight();
        int mIzq = 210, mDer = 90, mSup = 50, mInf = 45;
        int areaW = w - mIzq - mDer;
        int areaH = h - mSup - mInf;
        int n     = datos.size();
        int alturaBarra = Math.min(38, (areaH / Math.max(n, 1)) - 6);
        int espaciado   = Math.max(4, (areaH - alturaBarra * n) / (n + 1));

        double maxPpm = datos.stream().mapToDouble(DataPoint::getValor).max().orElse(1);


        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        String titulo = "⚡ PPM Media — Libros Terminados";
        g2.drawString(titulo, mIzq + (areaW - g2.getFontMetrics().stringWidth(titulo)) / 2, 32);


        int nGrid = 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= nGrid; i++) {
            int xGrid = mIzq + (areaW * i / nGrid);
            g2.setColor(colorGrid);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4f}, 0));
            g2.drawLine(xGrid, mSup, xGrid, h - mInf);
            String etiq = String.format("%.1f", maxPpm * i / nGrid);
            g2.setColor(colorTexto);
            g2.drawString(etiq, xGrid - g2.getFontMetrics().stringWidth(etiq) / 2, mSup - 8);
        }
        g2.setStroke(new BasicStroke(1.5f));


        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String labelEje = "PPM (páginas / minuto)";
        g2.drawString(labelEje, mIzq + (areaW - g2.getFontMetrics().stringWidth(labelEje)) / 2, h - 12);


        for (int i = 0; i < n; i++) {
            DataPoint p      = datos.get(i);
            int yBarra       = mSup + espaciado + i * (alturaBarra + espaciado);
            int anchoBarra   = (int) (areaW * p.getValor() / maxPpm);

            g2.setColor(i == 0 ? colorTop : colorBarra);
            g2.fillRoundRect(mIzq, yBarra, anchoBarra, alturaBarra, 6, 6);


            g2.setColor(colorTexto);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String nombre = utils.ReadingCalculator.acortar(p.getEtiqueta(), 30);
            g2.drawString(nombre,
                    mIzq - g2.getFontMetrics().stringWidth(nombre) - 8,
                    yBarra + alturaBarra / 2 + 5);


            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            g2.drawString(String.format("%.2f ppm", p.getValor()),
                    mIzq + anchoBarra + 6,
                    yBarra + alturaBarra / 2 + 5);
        }


        g2.setColor(colorTexto);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(mIzq, h - mInf, w - mDer, h - mInf);
    }
}