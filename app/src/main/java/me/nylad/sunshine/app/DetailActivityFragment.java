package me.nylad.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.nylad.sunshine.app.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String SHARE_HASHTAG = " #SunshineApp";
    public static final int DETAIL_LOADER = 1;
    public String mForecastStr;
    public TextView txtView;
    public String dataVal;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        txtView = (TextView) view.findViewById(R.id.weather_detail);
        Intent intent = getActivity().getIntent();
        if(intent != null)
        {
            mForecastStr = intent.getDataString();
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d("DetailActivityFragment", "Share Action Provider is null?");
        }
    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, dataVal + SHARE_HASHTAG);
        return shareIntent;
    }

    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(getActivity());
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastAdapter.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastAdapter.COL_WEATHER_MIN_TEMP)
        );

        return Utility.formatDate(cursor.getLong(ForecastAdapter.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastAdapter.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated( savedInstanceState );
    }

    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args )
    {
        Uri uri = Uri.parse( mForecastStr );
        return new CursorLoader(getActivity(),
                uri,
                ForecastFragment.FORECAST_COLUMNS,
                WeatherContract.WeatherEntry.COLUMN_DATE + " = ? AND" +
                        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[] {
                        ((Long) WeatherContract.WeatherEntry.getDateFromUri( uri )).toString(),
                        WeatherContract.WeatherEntry.getLocationSettingFromUri( uri )
                },
                null);
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor data )
    {
        data.moveToFirst();
        dataVal = convertCursorRowToUXFormat( data );
        txtView.setText( dataVal );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader )
    {
        txtView.setText( "" );
    }
}
