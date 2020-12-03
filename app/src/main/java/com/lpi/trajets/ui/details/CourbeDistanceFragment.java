package com.lpi.trajets.ui.details;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.lpi.trajets.R;
import com.lpi.trajets.courbes.HistogramView;
import com.lpi.trajets.database.ItinerairesDatabase;
import com.lpi.trajets.itineraire.Itineraire;
import com.lpi.trajets.itineraire.Position;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourbeDistanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourbeDistanceFragment extends FragmentCourbe
{
	public static Fragment newInstance(final @NonNull Itineraire itineraire)
	{
		Fragment fragment = new CourbeDistanceFragment();
		Bundle bundle = new Bundle();
		itineraire.toBundle(bundle);
		fragment.setArguments(bundle);
		return fragment;
	}

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	public CourbeDistanceFragment()
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
		return inflater.inflate(R.layout.fragment_distance_temps, container, false);

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

				float distance = 0;
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

					if (precedente == null)
					{
						// Distance = 0
						distance = 0;
					}
					else
					{
						// Distance depuis la position precedente
						distance += position.distanceTo(precedente);
					}
					precedente = position;

					donnees[indice] = new HistogramView.Donnee();
					donnees[indice].x = position.getTime() - premierTemps;
					donnees[indice].y = distance;

					indice++;
				}

				HistogramView.Axe axeX = calculeAxeTemps(0, dernierTemps-premierTemps);
				HistogramView.Axe axeY = calculeAxeDistance( 0, distance) ;

				histogramView.setDonnees( axeX, axeY, donnees);
			}
	}

}