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
 * Use the {@link CourbeVitesseVertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourbeVitesseVertFragment extends FragmentCourbe
{
	/***
	 * Creation du fragment
	 * @param itineraire
	 * @return
	 */
	public static CourbeVitesseVertFragment newInstance(final @NonNull Itineraire itineraire)
	{
		CourbeVitesseVertFragment fragment = new CourbeVitesseVertFragment();
		Bundle bundle = new Bundle();
		itineraire.toBundle(bundle);
		fragment.setArguments(bundle);
		return fragment;
	}

	public CourbeVitesseVertFragment()
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
		return inflater.inflate(R.layout.fragment_vitesse_temps, container, false);

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
				HistogramView.Donnee[] donnees = new HistogramView.Donnee[cursor.getCount()-1];

				float vitesseMin = Float.MAX_VALUE;
				float vitesseMax = Float.MIN_VALUE;
				Position precedente = null;
				int indice = 0;
				long premierTemps=0;
				long dernierTemps = 0;
				while (cursor.moveToNext())
				{
					Position position = new Position(cursor);
					if ( precedente != null)
					{
						long temps = position.getTime() - precedente.getTime();
						float distance = (float)position.getVerticalDistance(precedente) ;
						float vitesse = temps == 0 ? 0 : distance / (float)(temps/1000.0f);
						if ( vitesse> vitesseMax) vitesseMax = vitesse;
						if ( vitesse< vitesseMin) vitesseMin = vitesse;

						donnees[indice] = new HistogramView.Donnee();
						donnees[indice].x = position.getTime() - premierTemps;
						donnees[indice].y = vitesse;

						indice++;
					}
					else
					{
						premierTemps = position.getTime();
					}

					dernierTemps = position.getTime();
					precedente = position;
				}

				HistogramView.Axe axeX = calculeAxeTemps( 0,  dernierTemps-premierTemps );
				HistogramView.Axe axeY=  calculeAxeVitesse( vitesseMin, vitesseMax) ;

				histogramView.setDonnees( axeX, axeY, donnees);
			}
	}


}