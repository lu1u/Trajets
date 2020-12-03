package com.lpi.trajets.ui.details;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;
import com.lpi.trajets.R;
import com.lpi.trajets.courbes.HistogramView;
import com.lpi.trajets.database.ItinerairesDatabase;
import com.lpi.trajets.itineraire.Itineraire;
import com.lpi.trajets.itineraire.Position;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourbeVitesseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourbeAltitudeFragment extends FragmentCourbe
{
	public static Fragment newInstance(final @NonNull Itineraire itineraire)
	{
		Fragment fragment = new CourbeAltitudeFragment();
		Bundle bundle = new Bundle();
		itineraire.toBundle(bundle);
		fragment.setArguments(bundle);
		return fragment;
	}


	public CourbeAltitudeFragment()
	{
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_altitude_temps, container, false);

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
			Itineraire itineraire = new Itineraire(b);
			if (itineraire != null)
			{
				HistogramView histogramView = view.findViewById( R.id.histogramView);
				initGraphique(histogramView, itineraire);
			}
		}
	}

	private void initGraphique(final HistogramView histogramView, final Itineraire itineraire)
	{
		Cursor cursor = ItinerairesDatabase.getInstance(getContext()).getPositions(itineraire.Id);
		if (null != cursor)
			if (cursor.getCount() > 0)
			{
				HistogramView.Donnee[] donnees = new HistogramView.Donnee[cursor.getCount()];

				float altitudeMin = Float.MAX_VALUE;
				float altitudeMax = Float.MIN_VALUE;
				Position precedente = null;
				int indice = 0;
				long premierTemps=0;
				long dernierTemps = 0;
				while (cursor.moveToNext())
				{
					Position position = new Position(cursor);
					if  (indice==0)
					{
						premierTemps = position.getTime();
					}

					dernierTemps = position.getTime();

					final float altitude = (float) position.getAltitude();
					if ( altitude> altitudeMax) altitudeMax = altitude;
					if ( altitude< altitudeMin) altitudeMin = altitude;

					donnees[indice] = new HistogramView.Donnee();
					donnees[indice].x = position.getTime() - premierTemps;
					donnees[indice].y = (float)altitude;

					indice++;
				}

				HistogramView.Axe axeX = calculeAxeTemps(0, dernierTemps-premierTemps);
				HistogramView.Axe axeY = calculeAxeDistance( altitudeMin, altitudeMax);

				histogramView.setDonnees( axeX, axeY, donnees);
			}
	}
}