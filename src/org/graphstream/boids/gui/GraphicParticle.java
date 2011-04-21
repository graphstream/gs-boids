package org.graphstream.boids.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.miv.glutil.geom.Geometry;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;

/**
 * Particle representation.
 * 
 * @author Antoine Dutot
 */
public class GraphicParticle implements Geometry {
	// Attributes

	/**
	 * Particle identifier.
	 */
	public Object id;

	/**
	 * Position.
	 */
	public Point3 pos = new Point3();

	/**
	 * Colour.
	 */
	public Color color = Color.BLACK;

	/**
	 * Text label.
	 */
	public String label;

	/**
	 * The width.
	 */
	public float width = 4;

	/**
	 * Attributes.
	 */
	public HashMap<String, Object> attributes;

	/**
	 * The force vectors (if drawn).
	 */
	public Vector3 force0, force1, force2;

	/**
	 * Size of the trail behind the particle.
	 */
	public int positionsMemory = 0;

	/**
	 * The trail memory.
	 */
	public LinkedList<Point3> positions = new LinkedList<Point3>();

	// Constructors

	public GraphicParticle(Object id, double x, double y, double z) {
		this.id = id;
		pos.set(x, y, z);
	}

	// Access

	public Object getId() {
		return id;
	}

	public double getX() {
		return pos.x;
	}

	public double getY() {
		return pos.y;
	}

	public double getZ() {
		return pos.z;
	}

	// Commands

	public void setPosition(double x, double y, double z) {
		if (positionsMemory > 0 && positions.size() == positionsMemory)
			positions.removeLast();

		Point3 oldPos = pos;
		pos = new Point3(x, y, z);

		if (positionsMemory > 0)
			positions.addFirst(oldPos);
	}

	public void delete(GL gl) {
	}

	public void display(GL tgl) {
		GL2 gl = tgl.getGL2();

		double x = pos.x;
		double y = pos.y;
		double z = pos.z;

		gl.glPointSize(width);
		gl.glBegin(GL.GL_POINTS);
		gl.glColor3f(color.getRed() / 255f, color.getGreen() / 255f,
				color.getBlue() / 255f);
		gl.glVertex3d(x, y, z);
		gl.glEnd();

		float alpha = 1f;
		gl.glBegin(GL.GL_LINES);
		Point3 prev = pos;
		for (Point3 p : positions) {
			gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
					color.getBlue() / 255f, alpha * 0.1f);
			// gl.glColor4f( 0, 0, 0, alpha * 0.1f );
			gl.glVertex3d(prev.x, prev.y, prev.z);
			gl.glVertex3d(p.x, p.y, p.z);
			prev = p;
			alpha -= 1f / positionsMemory;
		}
		gl.glEnd();

		if (force0 != null && force1 != null && force2 != null) {
			gl.glBegin(GL.GL_LINES);
			gl.glColor4f(1, 0, 0, 0.7f);
			gl.glVertex3d(x, y, z);
			gl.glVertex3d(x + force0.data[0], y + force0.data[1], z
					+ force0.data[2]);
			gl.glColor4f(0, 1, 0, 0.7f);
			gl.glVertex3d(x, y, z);
			gl.glVertex3d(x + force1.data[0], y + force1.data[1], z
					+ force1.data[2]);
			gl.glColor4f(0, 0, 1, 0.7f);
			gl.glVertex3d(x, y, z);
			gl.glVertex3d(x + force2.data[0], y + force2.data[1], z
					+ force2.data[2]);
			gl.glEnd();
		}
	}

	public void setAttribute(String attribute, Object... values) {
		Object v;

		if (values == null || values.length == 0)
			v = true;
		else if (values.length == 1)
			v = values[0];
		else
			v = values;

		if (attributes == null)
			attributes = new HashMap<String, Object>();

		attributes.put(attribute, v);

		checkAttribute(attribute, v);
	}

	public void removeAttribute(String attribute) {
		if (attributes != null) {
			attributes.remove(attributes);
		}
	}

	public void checkAttribute(String attribute, Object value) {
		if (attribute.equals("force0") && value instanceof Vector3) {
			force0 = (Vector3) value;
		} else if (attribute.equals("force1") && value instanceof Vector3) {
			force1 = (Vector3) value;
		} else if (attribute.equals("force2") && value instanceof Vector3) {
			force2 = (Vector3) value;
		} else if (attribute.equals("color") && value instanceof Color) {
			color = (Color) value;
		} else if (attribute.equals("width") && value instanceof Number) {
			width = ((Number) value).floatValue();
		} else if (attribute.equals("trail") && value instanceof Number) {
			positionsMemory = ((Number) value).intValue();

			if (positions.size() > positionsMemory)
				positions.clear();
		}
	}
}