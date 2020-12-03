package com.lpi.trajets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lpi.trajets.itineraire.Itineraire;
import com.lpi.trajets.ui.details.SectionsPagerAdapter;

public class DetailsActivity extends AppCompatActivity
{
	public static final String EXTRA_RANDO_ID = DetailsActivity.class.getCanonicalName() + ".randoId";
	private Itineraire _itineraire;

	public static void start(final Context context, @NonNull final Itineraire itineraire)
	{
		Intent intent = new Intent(context, DetailsActivity.class);
		Bundle b = new Bundle();
		itineraire.toBundle(b);
		b.putString(EditItineraireActivity.EXTRA_OPERATION, EditItineraireActivity.EXTRA_OPERATION_MODIFIE);
		intent.putExtras(b);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		if (b != null)
		{
			_itineraire = new Itineraire(b);
			toolbar.setTitle(_itineraire.Nom);
			setTitle(_itineraire.Nom);
		}
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), _itineraire );
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// app icon in action bar clicked; go home
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}
}