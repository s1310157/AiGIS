package aigis.gl;

import java.awt.Font;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;

import aigis.model.CameraInfo;

import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

/**
 * Draws a zoom-linked scale bar overlay in the bottom-left corner.
 * Units: model coordinates are assumed to be in km; scale bar shows meters or km.
 */
public class ScaleBar {

    private static final double[] NICE_METERS = {
        1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000
    };

    private Font font = new Font("Monospaced", Font.BOLD, 18);
    private TextRenderer tr = new TextRenderer(font, true, true);

    /** Pick the smallest "nice" value >= targetMeters. */
    private double roundToNice(double targetMeters) {
        for (double v : NICE_METERS) {
            if (v >= targetMeters) return v;
        }
        return NICE_METERS[NICE_METERS.length - 1];
    }

    private String formatLabel(double meters) {
        if (meters >= 1000) {
            return String.format("%.0f km", meters / 1000.0);
        }
        return String.format("%.0f m", meters);
    }

    /**
     * Draw the scale bar for the current viewport.
     *
     * @param gl     GL2 context
     * @param width  viewport width in pixels
     * @param height viewport height in pixels
     * @param camera active camera (provides distance and FOV)
     */
    public void draw(GL2 gl, int width, int height, CameraInfo camera) {
        if (width <= 0 || height <= 0) return;

        // Visible height at the model plane (perspective projection):
        //   visible_height_km = 2 * distance * tan(fov/2)
        double distKm = camera.getDistance();
        double fovRad = camera.fov;
        double visibleHeightKm = 2.0 * distKm * Math.tan(fovRad / 2.0);
        double visibleHeightM = visibleHeightKm * 1000.0;
        if (visibleHeightM <= 0) return;

        double metersPerPixel = visibleHeightM / height;

        // Target bar width = 1/5 of viewport width
        double targetMeters = metersPerPixel * (width / 5.0);
        if (targetMeters <= 0 || targetMeters > 1e7) return;

        double niceMeters = roundToNice(targetMeters);
        int barPixels = (int) (niceMeters / metersPerPixel);
        if (barPixels <= 0 || barPixels > width) return;

        int barX = 20;
        int barY = 20;
        int barH = 7;

        // --- Draw text label ---
        String label = formatLabel(niceMeters);
        tr.beginRendering(width, height);
        tr.setColor(1f, 1f, 1f, 1f);
        tr.draw(label, barX, barY + barH + 4);
        tr.endRendering();

        // --- Setup 2D orthographic projection ---
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1, 1);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL_DEPTH_TEST);
        gl.glDisable(GL_LIGHTING);

        // White filled bar
        gl.glColor4f(1f, 1f, 1f, 1f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2i(barX,             barY);
        gl.glVertex2i(barX + barPixels, barY);
        gl.glVertex2i(barX + barPixels, barY + barH);
        gl.glVertex2i(barX,             barY + barH);
        gl.glEnd();

        // Black outline
        gl.glColor4f(0f, 0f, 0f, 1f);
        gl.glLineWidth(1.5f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2i(barX,             barY);
        gl.glVertex2i(barX + barPixels, barY);
        gl.glVertex2i(barX + barPixels, barY + barH);
        gl.glVertex2i(barX,             barY + barH);
        gl.glEnd();

        // Tick marks at left and right ends
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2i(barX,             barY - 3);
        gl.glVertex2i(barX,             barY + barH + 3);
        gl.glVertex2i(barX + barPixels, barY - 3);
        gl.glVertex2i(barX + barPixels, barY + barH + 3);
        gl.glEnd();

        // Restore matrices
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glEnable(GL_DEPTH_TEST);
    }
}
