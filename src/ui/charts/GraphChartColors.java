package ui.charts;

import java.awt.Color;

/**
 * Constantes de color y helpers de tema compartidos por todos los paneles
 * de gráfica.
 * <p>
 * Centralizar aquí los colores evita duplicar la misma lógica
 * {@code modoOscuro ? colorA : colorB} en cada clase de renderizado.
 */
public final class GraphChartColors {

    private GraphChartColors() { /* no instanciable */ }


    public static Color fondo(boolean oscuro) {
        return oscuro ? new Color(30, 30, 30) : Color.WHITE;
    }


    public static Color texto(boolean oscuro) {
        return oscuro ? new Color(220, 220, 220) : new Color(60, 60, 60);
    }

    public static Color textoContraste(boolean oscuro) {
        return oscuro ? Color.WHITE : Color.BLACK;
    }

    public static Color ejes(boolean oscuro) {
        return oscuro ? new Color(80, 80, 80) : Color.LIGHT_GRAY;
    }

    public static Color grid(boolean oscuro) {
        return oscuro ? new Color(60, 60, 60) : new Color(220, 220, 220);
    }


    /** Azul para barras de páginas */
    public static final Color BARRAS_PAGINAS   = new Color(70, 130, 180);
    /** Verde para barras de PPM */
    public static final Color BARRAS_PPM       = new Color(85, 170, 85);
    /** Azul claro para líneas de progreso */
    public static final Color LINEA_PROGRESO   = new Color(70, 160, 210);
    /** Verde para líneas de meta */
    public static final Color LINEA_META       = new Color(46, 204, 113);
    /** Verde destacado para top-1 en comparativa */
    public static final Color BARRA_TOP        = new Color(46, 204, 113);
    /** Azul medio para comparativa normal */
    public static final Color BARRA_COMP       = new Color(70, 130, 200);
}