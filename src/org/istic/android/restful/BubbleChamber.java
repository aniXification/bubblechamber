package org.istic.android.restful;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

final class BubbleChamber {
	private Particle[] particles;
	private Bitmap backbuffer;
	private Canvas canvas;
	private Random rng;
	
	private void set_backbuffer(int width, int height) {
		backbuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		backbuffer.eraseColor(0xffc0c0c0);
		if (canvas == null) {
			canvas = new Canvas();
		}
		canvas.setBitmap(backbuffer);
	}
	BubbleChamber(int width, int height) {
		set_backbuffer(width, height);
		canvas.translate(backbuffer.getWidth() / 2.0f, backbuffer.getHeight() / 2.0f);
		int max_dimension = Math.max(width, height);
		rng = new Random();
		
		float quark_frac = .3f;
		float muon_frac = .42f;
		float hadron_frac = .21f;
		int num_quarks = (int) (quark_frac / (quark_frac + muon_frac + hadron_frac) * max_dimension);
		int num_hadrons = (int) (hadron_frac / (quark_frac + muon_frac + hadron_frac) * max_dimension);
		
		particles = new Particle[max_dimension];
		int i;
		for (i = 0; i < num_quarks; ++i) {
			particles[i] = new Quark();
			particles[i].generate(rng);
		}
		for (; i < num_quarks + num_hadrons; ++i) {
			particles[i] = new Hadron();
			particles[i].generate(rng);
		}
		for (; i < particles.length; ++i ) {
			particles[i] = new Muon();
			particles[i].generate(rng);
		}
	}

	void resize(int width, int height) {
		set_backbuffer(width, height);
	}
	
	public void draw(Canvas output) {
		if (output.getWidth() != backbuffer.getWidth() || output.getHeight() != backbuffer.getHeight())
			resize(output.getWidth(), output.getHeight());
		
		output.drawBitmap(backbuffer, 0.0f, 0.0f, new Paint());
	}
	
	public void step_all() {
		AddPointCallback cb = new AddPointCallback();
		for (Particle p : particles) {
			cb.out_of_bounds = false;
			p.step(cb, rng);
			
			if (cb.out_of_bounds) {
				p.generate(rng);
			}
		}
	}
	
	private final class AddPointCallback extends Particle.StepCallback {
		boolean out_of_bounds;
		
		AddPointCallback() {
			this.out_of_bounds = false;
		}
		
		@Override
		void add_point(PointF position, int colour) {
			Rect region = new Rect(-(canvas.getWidth()+1)/2, -(canvas.getHeight()+1)/2, (canvas.getWidth()+1)/2, (canvas.getHeight()+1)/2);
			if (region.contains((int)position.x, (int)position.y)) {
				Paint paint = new Paint();
				paint.setColor(colour);
				canvas.drawPoint(position.x, position.y, paint);
			} else {
				out_of_bounds = true;
			}
		}
	}
}