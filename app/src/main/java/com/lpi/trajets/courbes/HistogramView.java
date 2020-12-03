package com.lpi.trajets.courbes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.lpi.trajets.R;

/**
 * TODO: document your custom view class.
 */
public class HistogramView extends View implements ScaleGestureDetector.OnScaleGestureListener
{
	@Override public boolean onScale(final ScaleGestureDetector scaleGestureDetector)
	{
		mScaleFactor *= scaleGestureDetector.getScaleFactor();

		// Don't let the object get too small or too large.
		if ( mScaleFactor < _minZoom)
			mScaleFactor = _minZoom;
		if ( mScaleFactor > _maxZoom)
			mScaleFactor = _maxZoom;

		calculeCourbe();
		invalidate();
		return true;
	}

	@Override public boolean onScaleBegin(final ScaleGestureDetector scaleGestureDetector)
	{
		return true;
	}

	@Override public void onScaleEnd(final ScaleGestureDetector scaleGestureDetector)
	{

	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Axes
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static class LabelAxe
	{
		public float valeur;
		public String label;
	}

	public static class Axe
	{
		public float min;
		public float max;
		public LabelAxe[] labels;
	}

	private Axe _axeX, _axeY;

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Donnees
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static class Donnee
	{
		public float x, y;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Styleable attributes
	private int _couleurAxes = Color.BLACK;
	private int _couleurGrille = Color.LTGRAY;
	private int _couleurCourbe = Color.RED;
	private float _epaisseurAxes = 2;
	private float _epaisseurGrille = 1;
	private float _epaisseurCourbe = 2;
	private float _marqueAxes = 0.5f;
	private float _tailleTexteAxes = 32;
	private boolean _dessinerGrille = true;
	private float _minZoom = 0.5f;
	private float _maxZoom = 3.0f;

	////////////////////////////////////////////////////////////////////////////////////////////////
	Paint _paintCourbe, _paintAxes, _paintGrille;
	TextPaint _paintTexte;
	int paddingLeft;
	int paddingTop;
	int paddingRight;
	int paddingBottom;
	private float mLastTouchX;
	private float mLastTouchY;
	private float mPosX;
	private float mPosY;
	private float scalePointX;
	private float scalePointY;

	int contentWidth = getWidth() - paddingLeft - paddingRight;
	int contentHeight = getHeight() - paddingTop - paddingBottom;
	private Donnee[] _donnees;
	private Path _path;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	public HistogramView(Context context)
	{
		super(context);
		init(null, 0);

		mScaleDetector = new ScaleGestureDetector(context, this);
	}

	private void init(AttributeSet attrs, int defStyle)
	{
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.HistogramView, defStyle, 0);

		_couleurAxes = a.getColor(R.styleable.HistogramView_HV_couleurAxes, _couleurAxes);
		_couleurGrille = a.getColor(R.styleable.HistogramView_HV_couleurGrille, _couleurGrille);
		_couleurCourbe = a.getColor(R.styleable.HistogramView_HV_couleurCourbe, _couleurCourbe);
		_epaisseurAxes = a.getDimension(R.styleable.HistogramView_HV_tailleAxes, _epaisseurAxes);
		_epaisseurGrille = a.getDimension(R.styleable.HistogramView_HV_tailleGrille, _epaisseurGrille);
		_epaisseurCourbe = a.getDimension(R.styleable.HistogramView_HV_tailleCourbe, _epaisseurCourbe);
		_marqueAxes = a.getDimension(R.styleable.HistogramView_HV_marquesAxes, _marqueAxes);
		_tailleTexteAxes = a.getDimension(R.styleable.HistogramView_HV_tailleTexteAxes, _tailleTexteAxes);
		_dessinerGrille = a.getBoolean(R.styleable.HistogramView_HV_dessinerGrille, _dessinerGrille);
		_minZoom = a.getFloat( R.styleable.HistogramView_HV_minZoom, _minZoom);
		_maxZoom = a.getFloat( R.styleable.HistogramView_HV_maxZoom, _maxZoom);

//		if (a.hasValue(R.styleable.HistogramView_exampleDrawable))
//		{
//			mExampleDrawable = a.getDrawable(
//					R.styleable.HistogramView_exampleDrawable);
//			mExampleDrawable.setCallback(this);
//		}
//		mExampleString = a.getString(
//				R.styleable.HistogramView_exampleString);
		a.recycle();

		paddingLeft = getPaddingLeft();
		paddingTop = getPaddingTop();
		paddingRight = getPaddingRight();
		paddingBottom = getPaddingBottom();

		contentWidth = getWidth() - paddingLeft - paddingRight;
		contentHeight = getHeight() - paddingTop - paddingBottom;

		if (isInEditMode())
		{
			_axeX = new Axe();
			_axeX.max = 20;
			_axeX.min = 5;

			_axeX.labels = new LabelAxe[15];
			for (int i = 0; i < 15; i++)
			{
				_axeX.labels[i] = new LabelAxe();
				_axeX.labels[i].valeur = _axeX.min + i;
				_axeX.labels[i].label = String.format("%1$d", (int) (i + _axeX.min));
			}

			_axeY = new Axe();
			_axeY.max = 10;
			_axeY.min = 0;

			_axeY.labels = new LabelAxe[(int) _axeY.max];
			for (int i = 0; i < _axeY.labels.length; i++)
			{
				_axeY.labels[i] = new LabelAxe();
				_axeY.labels[i].valeur = i;
				_axeY.labels[i].label = String.format("%1$.2f", _axeY.labels[i].valeur);

			}

			_donnees = new Donnee[50];
			for (int i = 0; i < _donnees.length; i++)
			{
				_donnees[i] = new Donnee();
				_donnees[i].x = ((float) i * (_axeX.max - _axeX.min) / (float) _donnees.length) + _axeX.min;
				_donnees[i].y = (float) (Math.random() * (_axeY.max - _axeY.min)) + _axeY.min;
			}
		}

		calculeAttributs();
		calculeCourbe();
	}

	public HistogramView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
		mScaleDetector = new ScaleGestureDetector(context, this);
	}

	public HistogramView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
		mScaleDetector = new ScaleGestureDetector(context, this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(ev);

		final int action = ev.getAction();

		switch(action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {

				final float x = (ev.getX() - scalePointX)/mScaleFactor;
				final float y = (ev.getY() - scalePointY)/mScaleFactor;
				mLastTouchX = x;
				mLastTouchY = y;
				break;
			}
			case MotionEvent.ACTION_MOVE: {

				final float x = (ev.getX() - scalePointX)/mScaleFactor;
				final float y = (ev.getY() - scalePointY)/mScaleFactor;
				// Only move if the ScaleGestureDetector isn't processing a gesture.
				if (!mScaleDetector.isInProgress()) {
					final float dx = x - mLastTouchX; // change in X
					final float dy = y - mLastTouchY; // change in Y
					mPosX += dx * mScaleFactor;
					mPosY += dy * mScaleFactor;
					calculeCourbe();
				}

				mLastTouchX = x;
				mLastTouchY = y;
				break;

			}
			case MotionEvent.ACTION_UP: {
				final float x = (ev.getX() - scalePointX)/mScaleFactor;
				final float y = (ev.getY() - scalePointY)/mScaleFactor;
				mLastTouchX = 0;
				mLastTouchY = 0;
				calculeCourbe();
			}
		}
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
//		canvas.save();
//		canvas.scale(mScaleFactor, mScaleFactor);
		dessineGrille(canvas);

		traceRepereX(canvas);
		traceRepereY(canvas);
		if (_path != null)
			canvas.drawPath(_path, _paintCourbe);

//		canvas.restore();

	}

	/**
	 *
	 */
	private void dessineGrille(final Canvas canvas)
	{
		if (!_dessinerGrille)
			return;

		if (_axeX != null)
		{
			float x;
			final float YaxeMin = getY(_axeY.min);
			final float YaxeMax = getY(_axeY.max);

			for (int i = 0; i < _axeX.labels.length; i++)
			{
				x = getX(_axeX.labels[i].valeur);
				canvas.drawLine(x, YaxeMin, x, YaxeMax, _paintGrille);
			}
		}

		if (_axeY != null)
		{
			final float XaxeMax = getX(_axeX.max);
			final float XaxeMin = getX(_axeX.min);
			float y;
			for (int i = 0; i < _axeY.labels.length; i++)
			{
				y = getY(_axeY.labels[i].valeur);
				canvas.drawLine(XaxeMin, y, XaxeMax, y, _paintGrille);
			}
		}
	}

	/***
	 * Converti un X dans le systeme des donnees en un X dans le systeme de coordonnees du Canvas
	 * @param v
	 * @return
	 */
	private float getX(float v)
	{
		return mPosX + paddingLeft + mScaleFactor *(((v - _axeX.min) / (_axeX.max - _axeX.min)) * contentWidth);
	}

	private float getY(float v)
	{
		//return getTop() + (paddingTop + contentHeight) - mScaleFactor *(((v - _axeY.min) / (_axeY.max - _axeY.min)) * contentHeight);

		return mPosY + getBottom() - paddingBottom  - mScaleFactor *(((v - _axeY.min) / (_axeY.max - _axeY.min)) * contentHeight);
	}

	/***
	 * Tracer l'axe des X
	 * @param canvas
	 */
	private void traceRepereX(final Canvas canvas)
	{
		if (_axeX == null)
			return;

		_paintTexte.setTextAlign(Paint.Align.CENTER);
		Rect textBounds = new Rect();
		_paintTexte.getTextBounds("0123456789.,", 0, 12, textBounds);
		final float hauteurTexte = textBounds.height();
		final float Yaxe = getY(_axeY.min);
		canvas.drawLine(getX(_axeX.min), Yaxe, getX(_axeX.max), Yaxe, _paintAxes);

		if (_axeX.labels != null)
		{
			float x;
			for (int i = 0; i < _axeX.labels.length; i++)
			{
				x = getX(_axeX.labels[i].valeur);
				canvas.drawLine(x, Yaxe, x, Yaxe + _marqueAxes, _paintAxes);
				canvas.drawText(_axeX.labels[i].label, x, Yaxe + hauteurTexte + _marqueAxes, _paintTexte);
			}
		}
	}

	/***
	 * Tracer l'axe des Y
	 * @param canvas
	 */
	private void traceRepereY(@NonNull final Canvas canvas)
	{
		if (_axeY == null)
			return;

		_paintTexte.setTextAlign(Paint.Align.RIGHT);
		Rect textBounds = new Rect();
		_paintTexte.getTextBounds("0123456789.", 0, 11, textBounds);
		final float hauteurTexte = textBounds.height() / 2.0f;

		final float Xaxe = getX(_axeX.min);
		canvas.drawLine(Xaxe, getY(_axeY.min), Xaxe, getY(_axeY.max), _paintAxes);

		if (_axeY.labels != null)
		{
			float y;
			for (int i = 0; i < _axeY.labels.length; i++)
			{
				y = getY(_axeY.labels[i].valeur);
				canvas.drawLine(Xaxe, y, Xaxe - _marqueAxes, y, _paintAxes);
				canvas.drawText(_axeY.labels[i].label, Xaxe - _marqueAxes, y + hauteurTexte, _paintTexte);
			}
		}
	}

	public void setDonnees(@NonNull final Axe axeX, @NonNull final Axe axeY, @NonNull final Donnee[] donnees)
	{
		_axeX = axeX;
		_axeY = axeY;
		_donnees = donnees;
		calculeCourbe();
	}

	private void calculeAttributs()
	{
		_paintCourbe = new Paint();
		_paintCourbe.setColor(_couleurCourbe);
		_paintCourbe.setStrokeWidth(_epaisseurCourbe);
		_paintCourbe.setStyle(Paint.Style.STROKE);
		_paintCourbe.setAntiAlias(true);
		_paintCourbe.setStrokeJoin(Paint.Join.ROUND);

		_paintAxes = new Paint();
		_paintAxes.setColor(_couleurAxes);
		_paintAxes.setStrokeWidth(_epaisseurAxes);
		_paintAxes.setStyle(Paint.Style.STROKE);

		_paintGrille = new Paint();
		_paintGrille.setColor(_couleurGrille);
		_paintGrille.setStrokeWidth(_epaisseurGrille);
		_paintGrille.setStyle(Paint.Style.STROKE);

		_paintTexte = new TextPaint();
		_paintTexte.setColor(_couleurAxes);
		_paintTexte.setStyle(Paint.Style.FILL_AND_STROKE);
		_paintTexte.setTextSize(_tailleTexteAxes);
		_paintTexte.setTextAlign(Paint.Align.RIGHT);
		invalidate();
	}

	private void calculeCourbe()
	{
		_path = null;
		if (_donnees != null)
			if (_donnees.length > 1)
			{
				_path = new Path();
				_path.moveTo(getX(_donnees[0].x), getY(_donnees[0].y));
				for (int i = 1; i < _donnees.length; i++)
					_path.lineTo(getX(_donnees[i].x), getY(_donnees[i].y));
			}
		invalidate();
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
	{
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		contentWidth = getWidth() - paddingLeft - paddingRight;
		contentHeight = getHeight() - paddingTop - paddingBottom;
		calculeAttributs();
		calculeCourbe();
	}
}