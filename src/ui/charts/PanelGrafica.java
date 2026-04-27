package ui.charts;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Gráfico de barras verticales para páginas o PPM por sesión / día.
 */
public class PanelGrafica extends JPanel {

    private final List<String> fechas;
    private final List<Double> valores;
    private final String tituloEje;
    private final boolean modoOscuro;

    public PanelGrafica(List<String> fechas, List<Double> valores, String tituloEje, boolean modoOscuro) {
        this.fechas     = fechas;
        this.valores    = valores;
        this.tituloEje  = tituloEje;
        this.modoOscuro = modoOscuro;
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (valores.isEmpty()) {
            g.setColor(modoOscuro ? new Color(200, 200, 200) : Color.GRAY);
            g.drawString("No hay datos disponibles.", 50, 50);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color colorTexto = GraphChartColors.texto(modoOscuro);
        Color colorEjes  = GraphChartColors.ejes(modoOscuro);

        int h = getHeight(), mIzq = 80, mInf = 150, mSup = 60;
        double maxV    = valores.stream().max(Double::compare).orElse(10.0);
        double escalaY = (h - mInf - mSup) / (maxV * 1.15);

        for (int i = 0; i < valores.size(); i++) {
            int x    = mIzq + (i * 90) + 25;
            int barH = (int) (valores.get(i) * escalaY);
            int y    = h - mInf - barH;

            g2.setColor(tituloEje.contains("Pág")
                    ? GraphChartColors.BARRAS_PAGINAS
                    : GraphChartColors.BARRAS_PPM);
            g2.fillRect(x, y, 50, barH);

            g2.setColor(colorTexto);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            String formato = tituloEje.contains("Pág") ? "%.1f" : "%.2f";
            g2.drawString(String.format(formato, valores.get(i)), x + 5, y - 5);

            AffineTransform old = g2.getTransform();
            g2.translate(x + 25, h - mInf + 15);
            g2.rotate(Math.toRadians(45));
            String[] partes = fechas.get(i).split(";");
            for (int j = 0; j < partes.length; j++) {
                String texto = partes[j];
                if (j > 0) {
                    texto = utils.ReadingCalculator.acortar(texto, 25);
                }
                g2.setFont(new Font("SansSerif", Font.PLAIN, j == 0 ? 10 : 9));
                g2.drawString(texto, 0, j * 12);
            }
            g2.setTransform(old);
        }

        g2.setColor(colorEjes);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(mIzq, mSup, mIzq, h - mInf);
        g2.drawLine(mIzq, h - mInf, getWidth() - 50, h - mInf);

        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString(tituloEje, 10, mSup - 20);
    }
}