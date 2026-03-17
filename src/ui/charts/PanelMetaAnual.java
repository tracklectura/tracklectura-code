package ui.charts;

import javax.swing.JPanel;
import java.awt.*;

/**
 * Gráfico de Donut para la Meta Anual de Lectura.
 */
public class PanelMetaAnual extends JPanel {

    private final int terminados;
    private final int meta;
    private final boolean modoOscuro;

    public PanelMetaAnual(int terminados, int meta, boolean modoOscuro) {
        this.terminados = terminados;
        this.meta       = meta;
        this.modoOscuro = modoOscuro;
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radioExterior = 150;
        int radioInterior = 110;

        double porcentaje  = Math.min(1.0, (double) terminados / meta);
        int    anguloLleno = (int) (porcentaje * 360);

        // Fondo del anillo (gris)
        g2.setColor(modoOscuro ? new Color(60, 60, 60) : new Color(220, 220, 220));
        g2.fillOval(cx - radioExterior, cy - radioExterior, radioExterior * 2, radioExterior * 2);

        // Relleno del anillo (verde si meta alcanzada, azul si no)
        g2.setColor(terminados >= meta ? GraphChartColors.LINEA_META : new Color(70, 130, 180));
        g2.fillArc(cx - radioExterior, cy - radioExterior,
                radioExterior * 2, radioExterior * 2, 90, -anguloLleno);

        // Hueco central
        g2.setColor(getBackground());
        g2.fillOval(cx - radioInterior, cy - radioInterior, radioInterior * 2, radioInterior * 2);

        // Textos centrales
        Color colorTexto = GraphChartColors.textoContraste(modoOscuro);
        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        String txtCentro = terminados + " / " + meta;
        g2.drawString(txtCentro, cx - g2.getFontMetrics().stringWidth(txtCentro) / 2, cy + 15);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        String subTxt = "Libros Terminados";
        g2.drawString(subTxt, cx - g2.getFontMetrics().stringWidth(subTxt) / 2, cy + 45);
    }
}