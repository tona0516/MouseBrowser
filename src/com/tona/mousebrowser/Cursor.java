package com.tona.mousebrowser;

import android.graphics.Point;

public class Cursor {
	private float x, y;
	private float v;
	private float sizeRate;
	private float width, height;
	private Point displaySize;
	public static final float defalutWidth = 39, defaultHeight = 48;
	public static float defaultX, defaultY;

	public Cursor(int displayWidth, int displayHeight) {
		setDisplaySize(new Point(displayWidth, displayHeight));
		defaultX = displayWidth / 2;
		defaultY = displayHeight / 2;
		x = displayWidth / 2;
		y = displayHeight / 2;
		setV(1.0f);
		setWidth(defalutWidth);
		setHeight(defaultHeight);
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getV() {
		return v;
	}
	public void setV(float v) {
		this.v = v;
	}
	public double getSizeRate() {
		return sizeRate;
	}
	public void setSizeRate(float sizeRate) {
		this.sizeRate = sizeRate;
		setWidth(defalutWidth * sizeRate);
		setHeight(defaultHeight * sizeRate);
	}
	public float getWidth() {
		return width;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public float getHeight() {
		return height;
	}
	public void setHeight(float height) {
		this.height = height;
	}
	public Point getDisplaySize() {
		return displaySize;
	}
	public void setDisplaySize(Point displaySize) {
		this.displaySize = displaySize;
	}
}