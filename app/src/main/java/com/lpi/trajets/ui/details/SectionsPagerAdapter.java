package com.lpi.trajets.ui.details;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.lpi.trajets.MapsFragment;
import com.lpi.trajets.R;
import com.lpi.trajets.itineraire.Itineraire;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter
{
	private final Itineraire _itineraire;
	String[] tabTitles;

	public SectionsPagerAdapter(Context context, FragmentManager fm, @NonNull Itineraire itineraire)
	{
		super(fm);
		_itineraire = itineraire;
		tabTitles = context.getResources().getStringArray(R.array.tab_details);
	}

	@Override
	public Fragment getItem(int position)
	{
		switch (position)
		{
			case 0: return DetailsTrajetFragment.newInstance(_itineraire);
			case 1: return MapsFragment.newInstance(_itineraire);
			case 2: return CourbeDistanceFragment.newInstance( _itineraire);
			case 3: return CourbeVitesseFragment.newInstance( _itineraire);
			case 4: return CourbeVitesseVertFragment.newInstance(_itineraire);
			case 5: return CourbeAltitudeFragment.newInstance( _itineraire);

			default:
				return DetailsTrajetFragment.newInstance(_itineraire);
		}
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position)
	{
		return tabTitles[position];
	}

	@Override
	public int getCount()
	{
		return tabTitles.length;
	}
}