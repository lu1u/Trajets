package com.lpi.trajets;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lpi.trajets.database.ItinerairesDatabase;
import com.lpi.trajets.itineraire.Itineraire;
import com.lpi.trajets.itineraire.Position;
import com.lpi.trajets.ui.details.ClickPositionHandler;
import com.lpi.trajets.utils.FloatHolder;

public class MapsFragment extends Fragment implements ClickPositionHandler.positionRefreshListener
{
	final double GLOBE_WIDTH = 256; // a constant in Google's map projection
	final double LN2 = 0.6931471805599453;
	private Itineraire _itineraire;
	private final OnMapReadyCallback callback = new OnMapReadyCallback()
	{

		/**
		 * Manipulates the map once available.
		 * This callback is triggered when the map is ready to be used.
		 * This is where we can add markers or lines, add listeners or move the camera.
		 * In this case, we just add a marker near Sydney, Australia.
		 * If Google Play services is not installed on the device, the user will be prompted to
		 * install it inside the SupportMapFragment. This method will only be triggered once the
		 * user has installed Google Play services and returned to the app.
		 */
		@Override
		public void onMapReady(GoogleMap mMap)
		{
			mMap.getUiSettings().setCompassEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(true);
			mMap.getUiSettings().setAllGesturesEnabled(true);
			mMap.getUiSettings().setMapToolbarEnabled(true);

			if (_itineraire != null)
			{
				FloatHolder _vitesseMin = new FloatHolder();
				FloatHolder _vitesseMax = new FloatHolder();
				_itineraire.getDetails(getContext(), _vitesseMin, _vitesseMax, null, null);

				Cursor cursor = ItinerairesDatabase.getInstance(getContext()).getPositions(_itineraire.Id);
				if (null != cursor)
					if (cursor.getCount() > 0)
					{
						//PolylineOptions polyline = new PolylineOptions();
						double latMin = Double.MAX_VALUE;
						double latMax = Double.MIN_VALUE;
						double longMin = Double.MAX_VALUE;
						double longMax = Double.MIN_VALUE;
						Position precedente = null;
						while (cursor.moveToNext())
						{
							Position position = new Position(cursor);

							//polyline.add(position.toLatLng());

							if (position.getLatitude() < latMin) latMin = position.getLatitude();
							if (position.getLatitude() > latMax) latMax = position.getLatitude();
							if (position.getLongitude() < longMin)
								longMin = position.getLongitude();
							if (position.getLongitude() > longMax)
								longMax = position.getLongitude();

							Log.d("PROVIDER", position.getProvider());
							Marker m = mMap.addMarker(new MarkerOptions()

									.position(position.toLatLng())
									.title(position.getAltitude() + "m")
									.icon(BitmapDescriptorFactory.defaultMarker(position.getProvider().equals("gps") ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_ROSE)));
							m.setTag(position);
							mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
							{
								@Override
								public boolean onMarkerClick(final Marker marker)
								{
									Object o = marker.getTag();
									if (o instanceof Position)
									{
										ClickPositionHandler.handleClickPosition(marker, (Position) o, getContext(), MapsFragment.this);
										return true;
									}

									return false;
								}
							});
							if (precedente != null)
							{
								PolylineOptions po = new PolylineOptions();
								po.add(precedente.toLatLng());
								po.add(position.toLatLng());
								po.color(getColor(position.getSpeed(), _vitesseMin.getValeur(), _vitesseMax.getValeur(), Color.RED, Color.GREEN));
								mMap.addPolyline(po);
							}
							precedente = position;
						}
						//polyline.color(Color.BLUE);
						//polyline.width(7.5f);

						//_mapPolyline = googleMap.addPolyline(polyline);

						final LatLng centre = new LatLng((latMin + latMax) / 2.0, (longMin + longMax) / 2.0);
						float zoom = getNiveauZoom(latMin, longMin, latMax, longMax);//Preferences.getInstance(this).getNiveauZoom();
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centre, zoom));
					}
			}

		}
	};

	public static Fragment newInstance(final @NonNull Itineraire itineraire)
	{
		Fragment fragment = new MapsFragment();
		Bundle bundle = new Bundle();
		itineraire.toBundle(bundle);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_maps, container, false);
	}

	private int getColor(final float speed, final float vitesseMin, final float vitesseMax, final int colorMin, final int colorMax)
	{
		int r = composante(speed, vitesseMin, vitesseMax, Color.red(colorMin), Color.red(colorMax));
		int g = composante(speed, vitesseMin, vitesseMax, Color.green(colorMin), Color.green(colorMax));
		int b = composante(speed, vitesseMin, vitesseMax, Color.blue(colorMin), Color.blue(colorMax));

		return Color.rgb(r, g, b);
	}

	private int composante(final float speed, final float vitesseMin, final float vitesseMax, final int min, final int max)
	{
		int res = (int) (min + (speed - vitesseMin) / (vitesseMax - vitesseMin) * (max - min));
		if (res < 0)
			res = 0;
		if (res > 255)
			res = 255;
		return res;
	}

	/***
	 * Calculer un niveau de zoom adequat pour afficher la totalite du parcours
	 * https://stackoverflow.com/questions/6048975/google-maps-v3-how-to-calculate-the-zoom-level-for-a-given-bounds
	 * @param south
	 * @param west
	 * @param north
	 * @param east
	 * @return
	 */
	private float getNiveauZoom(final double south, final double west, final double north, final double east)
	{
		double zoom;

		double angle = east - west;
		double angle2 = north - south;
		double delta = 0;

		if (angle2 > angle)
		{
			angle = angle2;
			delta = 3;
		}

		while (angle < 0)
			angle += 360;

		zoom = Math.floor(Math.log(960 * 360 / angle / GLOBE_WIDTH) / LN2) - 2 - delta;
		if (zoom < 1)
			zoom = 1;
		if (zoom > 15)
			zoom = 15;

		return (float) zoom;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		SupportMapFragment mapFragment =
				(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

		Bundle b = getArguments();
		if (b != null)
		{
			_itineraire = new Itineraire(b);
		}

		if (mapFragment != null)
		{
			mapFragment.getMapAsync(callback);
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onClickPositionHandlerPositionSupprimee(Marker marker, Position posSupprimee)
	{
		marker.remove();
		//_mapPolyline.
	}
}