package com.lpi.trajets.ui.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.lpi.trajets.R;
import com.lpi.trajets.itineraire.Itineraire;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsTrajetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsTrajetFragment extends Fragment
{
	public static Fragment newInstance(final @NonNull Itineraire itineraire)
	{
		Fragment fragment = new DetailsTrajetFragment();
		Bundle bundle = new Bundle();
		itineraire.toBundle(bundle);
		fragment.setArguments(bundle);
		return fragment;
	}

	public DetailsTrajetFragment()
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
		View v = inflater.inflate(R.layout.fragment_details_trajet, container, false);
		return v;
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
				TextView tvDescription = view.findViewById(R.id.textViewDescription);
				tvDescription.setText(itineraire.getDetails(getContext(), null, null, null, null));

			}
		}
	}
}