package ui.charts;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gráfico de dispersión (Scatter Plot) para Correlación Minutos vs PPM.
 */
public class PanelScatter extends JPanel {

    private final List<String> minutosStr;
    private final List<Double> ppm;
    private final boolean modoOscuro;

    public PanelScatter(List<String> minutos, List<Double> ppm, boolean modoOscuro) {
        this.minutosStr = minutos;
        this.ppm        = ppm;
        this.modoOscuro = modoOscuro;
        setBackground(GraphChartColors.fondo(modoOscuro));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (ppm.isEmpty()) {
            g2.setColor(modoOscuro ? Color.LIGHT_GRAY : Color.GRAY);
            g2.drawString("No hay suficientes datos para correlación.", 50, 50);
            return;
        }

        int w = getWidth(), h = getHeight();
        int mIzq = 70, mDer = 80, mSup = 50, mInf = 60;
        int areaW = w - mIzq - mDer, areaH = h - mSup - mInf;

        double maxMinutos = minutosStr.stream().mapToDouble(Double::parseDouble).max().orElse(10);
        double maxPpm     = ppm.stream().mapToDouble(Double::doubleValue).max().orElse(10);

        Color colorTexto = GraphChartColors.textoContraste(modoOscuro);


        int nGridY = 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= nGridY; i++) {
            int yGrid = h - mInf - (areaH * i / nGridY);
            g2.setColor(GraphChartColors.grid(modoOscuro));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4f}, 0));
            g2.drawLine(mIzq, yGrid, w - mDer, yGrid);
            String etiq = String.format("%.1f", maxPpm * i / nGridY);
            g2.setColor(colorTexto);
            g2.drawString(etiq, mIzq - g2.getFontMetrics().stringWidth(etiq) - 5, yGrid + 4);
        }


        int nGridX = 5;
        for (int i = 0; i <= nGridX; i++) {
            int xGrid = mIzq + (areaW * i / nGridX);
            g2.setColor(GraphChartColors.grid(modoOscuro));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4f}, 0));
            g2.drawLine(xGrid, mSup, xGrid, h - mInf);
            String etiq = String.valueOf((int) (maxMinutos * i / nGridX));
            g2.setColor(colorTexto);
            g2.drawString(etiq, xGrid - g2.getFontMetrics().stringWidth(etiq) / 2, h - mInf + 15);
        }


        g2.setColor(modoOscuro ? new Color(100, 100, 100) : Color.GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(mIzq, mSup, mIzq, h - mInf);
        g2.drawLine(mIzq, h - mInf, w - mDer, h - mInf);


        List<Point> puntos = new ArrayList<>();
        for (int i = 0; i < ppm.size(); i++) {
            double min = Double.parseDouble(minutosStr.get(i));
            double p   = ppm.get(i);
            int x = mIzq + (int) ((min / maxMinutos) * areaW);
            int y = h - mInf - (int) ((p / maxPpm) * areaH);
            puntos.add(new Point(x, y));
        }
        puntos.sort(Comparator.comparingInt(p -> p.x));


        if (puntos.size() > 1) {
            g2.setColor(new Color(100, 149, 237, 100));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < puntos.size() - 1; i++)
                g2.drawLine(puntos.get(i).x, puntos.get(i).y, puntos.get(i + 1).x, puntos.get(i + 1).y);
        }


        for (Point p : puntos) {
            g2.setColor(new Color(100, 149, 237, 180));
            g2.fillOval(p.x - 5, p.y - 5, 10, 10);
            g2.setColor(new Color(70, 130, 200));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawOval(p.x - 5, p.y - 5, 10, 10);
        }


        g2.setColor(colorTexto);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Duración de la Sesión (Minutos)", w / 2 - 80, h - 20);

        AffineTransform old = g2.getTransform();
        g2.translate(30, h / 2 + 40);
        g2.rotate(-Math.PI / 2);
        g2.drawString("Velocidad Lectora (PPM)", 0, 0);
        g2.setTransform(old);
    }
}