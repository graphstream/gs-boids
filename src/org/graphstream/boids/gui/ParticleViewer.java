package org.graphstream.boids.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;

import org.graphstream.boids.Context;
import org.miv.glutil.Buffer;
import org.miv.glutil.BufferListener;
import org.miv.glutil.SwingBuffer;
import org.miv.glutil.geom.Cube;
import org.miv.pherd.ParticleBoxListener;
import org.miv.pherd.ParticleBoxListenerProxy;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.NTreeListener;
import org.miv.pherd.ntree.NTreeListenerProxy;

public class ParticleViewer extends JPanel implements ParticleBoxListener,
		NTreeListener, BufferListener {
	// Attributes

	private static final long serialVersionUID = 1L;

	/**
	 * Shared information.
	 */
	protected Context ctx;

	/**
	 * Listener for the particles.
	 */
	protected ParticleBoxListenerProxy pboxProxy;

	/**
	 * Listener for the n-tree.
	 */
	protected NTreeListenerProxy ntreeProxy;

	/**
	 * Set of particles representations.
	 */
	protected HashMap<Object, GraphicParticle> particles;

	/**
	 * The OpenGL canvas.
	 */
	protected Buffer buffer;

	// View

	protected float near = 1f;

	protected float radius = 1.03f, theta, phi;

	protected Point3 camera;

	protected Point3 lookAt;

	// Environment

	protected Cube env;

	protected Color background = new Color(0, 0, 0);

	// Constructors

	public ParticleViewer(Context ctx) {
		this.ctx = ctx;
		pboxProxy = new ParticleBoxListenerProxy(ctx.getPbox(), true);
		ntreeProxy = new NTreeListenerProxy(ctx.getPbox().getNTree(), true);
		particles = new HashMap<Object, GraphicParticle>();
		buffer = new SwingBuffer(this, "Million boids !", 512, 512,
				Buffer.OutputMode.CANVAS, true);
		camera = new Point3(0, 0, -radius);
		lookAt = new Point3(0, 0, 0);
		env = new Cube((float) (ctx.getPbox().getNTree().getRootCell()
				.getSpace().getHiAnchor().x - ctx.getPbox().getNTree()
				.getRootCell().getSpace().getLoAnchor().x));

		pboxProxy.addParticleBoxListener(this);
		ntreeProxy.addNTreeListener(this);
		setLayout(new BorderLayout());
		add((java.awt.Component) buffer.getComponent(), BorderLayout.CENTER);
	}

	// Commands

	/**
	 * Disconnect the view from the current particle box.
	 */
	public void disconnect() {
		pboxProxy.disconnect(ctx.getPbox());
		ntreeProxy.disconnect(ctx.getPbox().getNTree());
		particles.clear();
	}

	/**
	 * Re-connect the view to the current particle box.
	 */
	public void reconnect() {
		pboxProxy.connect(ctx.getPbox(), true);
		ntreeProxy.connect(ctx.getPbox().getNTree(), true);
	}

	public void display() {
		pboxProxy.checkEvents();
		ntreeProxy.checkEvents();
		buffer.display();
	}

	protected void setView() {
		GL2 gl = buffer.getGl().getGL2();
		GLU glu = buffer.getGlu();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum(-ctx.area, ctx.area, -ctx.area, ctx.area, near, 10);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		glu.gluLookAt(camera.x, camera.y, camera.z, lookAt.x, lookAt.y,
				lookAt.z, 0, 1, 0);
	}

	protected void displayEnv() {
		GL2 gl = buffer.getGl().getGL2();

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glColor4f(0, 0, 0, 0.5f);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		env.display(gl);
		gl.glDisable(GL.GL_LINE_SMOOTH);
	}

	public void displayParticles() {
		Iterator<? extends GraphicParticle> i = particles.values().iterator();
		GL gl = buffer.getGl();

		while (i.hasNext()) {
			GraphicParticle p = i.next();

			p.display(gl);
		}
	}

	// Particle box listener

	public void particleAdded(Object id, double x, double y, double z) {
		particles.put(id, new GraphicParticle(id, x, y, z));
	}

	public void particleAttributeChanged(Object id, String attribute,
			Object newValue, boolean removed) {
		GraphicParticle p = particles.get(id);

		if (p != null) {
			if (removed)
				p.removeAttribute(attribute);
			else
				p.setAttribute(attribute, newValue);
		}
	}

	public void particleMoved(Object id, double x, double y, double z) {
		GraphicParticle p = particles.get(id);

		if (p != null)
			p.setPosition(x, y, z);
	}

	public void particleRemoved(Object id) {
		particles.remove(id);
	}

	public void stepFinished(int time) {
	}

	// NTree Listener

	public void cellAdded(Object id, Object parentId, Anchor lo, Anchor hi,
			int depth, int index) {
	}

	public void cellData(Object id, String message, Object data) {
	}

	public void cellRemoved(Object id) {
	}

	// GL Event Listener

	public void close(Buffer buffer) {
	}

	public void display(Buffer buffer) {
		setView();
		// displayEnv();
		displayParticles();
	}

	public void init(Buffer buffer) {
		GL gl = buffer.getGl();

		gl.glClearColor(background.getRed() / 255f,
				background.getGreen() / 255f, background.getBlue() / 255f, 0);
		gl.glClearDepth(255f);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_POINT_SMOOTH);
	}

	public void key(Buffer buffer, int key, char unicode, boolean pressed) {
	}

	public void keyTyped(Buffer buffer, char unicode, int modifiers) {
	}

	public void mouse(Buffer buffer, int x, int y, int button) {
		/*
		 * if( ctx.getMouse() != null ) { switch( button ) { case 0:
		 * ctx.getMouse().mouseMoved( x, y ); break; case 4:
		 * ctx.getMouse().decreaseFearFactor(); break; case 5:
		 * ctx.getMouse().increaseFearFactor(); break; } }
		 */
	}

	public void reshape(Buffer buffer, int x, int y, int width, int height) {
	}
}