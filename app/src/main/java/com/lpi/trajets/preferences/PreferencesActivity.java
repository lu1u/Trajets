package com.lpi.trajets.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lpi.trajets.R;

public class PreferencesActivity extends AppCompatActivity
{
	//Spinner _spinnerTheme;

	public static void start(final Activity context)
	{
		final AlertDialog dialogBuilder = new AlertDialog.Builder(context).create();


		//AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, Utils.getTheme(context));
		LayoutInflater inflater = context.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.activity_preferences, null);
		final Preferences preferences = Preferences.getInstance(context);

		initDelai( dialogView, preferences);
		initDistance( dialogView, preferences);
		initLocalisation(dialogView, preferences);



		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Afficher la fenetre
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		dialogBuilder.setView(dialogView);
		dialogBuilder.show();
	}


	private static void initLocalisation(@NonNull final View view, @NonNull Preferences preferences)
	{
		Context context = view.getContext();
		Switch _localisationGPS = view.findViewById(R.id.switchLocalisationGPS);
		Switch _localisationReseau = view.findViewById(R.id.switchLocalisationReseau);
		final Preferences pref = Preferences.getInstance(context);

		_localisationGPS.setChecked(pref.getLocalisationGPS());
		_localisationReseau.setChecked(pref.getLocalisationReseau());

		_localisationGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
			{
				pref.setLocalisationGPS(isChecked);
			}
		});

		_localisationReseau.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
			{
				pref.setLocalisationReseau(isChecked);
			}
		});
	}


	/***
	 * Initialisation de l'interface pour choisir la distance GPS min
	 */
	private static void initDistance(@NonNull final View view, Preferences preferences)
	{
		Context context = view.getContext();

		SeekBar _sbDistances = view.findViewById(R.id.seekBarDistance);
		TextView _textDistance = view.findViewById(R.id.textViewDistance);
		String[] _distances = context.getResources().getStringArray(R.array.distances);
		int[] _distancesValeurs = context.getResources().getIntArray(R.array.distances_valeurs);
		//_sbDistances.setMin(0);
		_sbDistances.setMax(_distances.length - 1);
		_sbDistances.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (progress < 0 || progress >= _distances.length)
					return;

				_textDistance.setText(_distances[progress]);
				if (fromUser)
					Preferences.getInstance(context).setGPSMinDistance(_distancesValeurs[progress]);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});
		_sbDistances.setMax(_distances.length - 1);
		_sbDistances.setProgress(getIndiceDistances(_distancesValeurs, preferences));
	}

	/***
	 * Initialisation de l'interface pour choisir le delai GPS minimum
	 */
	private static void initDelai(@NonNull final View view, Preferences preferences)
	{
		SeekBar _sbDelai = view.findViewById(R.id.seekBarDelai);
		TextView _textDelai = view.findViewById(R.id.textViewDelai);
		String[] _delais = view.getContext().getResources().getStringArray(R.array.delais);
		int[] _delaisValeurs = view.getContext().getResources().getIntArray(R.array.delais_valeurs);
		//_sbDelai.setMin(0);
		_sbDelai.setMax(_delais.length - 1);
		int[] _distancesValeurs = view.getContext().getResources().getIntArray(R.array.distances_valeurs);

		_sbDelai.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (progress < 0 || progress >= _delais.length)
					return;

				_textDelai.setText(_delais[progress]);
				if (fromUser)
				{
					Preferences.getInstance(view.getContext()).setGPSMinTime(_delaisValeurs[progress]);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});
		_sbDelai.setMax(_delais.length - 1);
		_sbDelai.setProgress(getIndiceDelai(_delaisValeurs, preferences));
	}

	private static int getIndiceDistances(int[] _distancesValeurs, Preferences prefs)
	{
		final int delai = prefs.getGPSMinDistance();
		for (int i = 0; i < _distancesValeurs.length; i++)
			if (_distancesValeurs[i] == delai)
				return i;

		return 0;
	}

	private static int getIndiceDelai(int[] delaisValeurs, Preferences prefs)
	{
		final int delai = prefs.getGPSMinTime();
		for (int i = 0; i < delaisValeurs.length; i++)
			if (delaisValeurs[i] == delai)
				return i;

		return 0;
	}

}
