package com.anthonynahas.autocallrecorder.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.anthonynahas.autocallrecorder.R;
import com.anthonynahas.autocallrecorder.adapters.StatisticRecordsAdapter;
import com.anthonynahas.autocallrecorder.classes.Record;
import com.anthonynahas.autocallrecorder.classes.Resources;
import com.anthonynahas.autocallrecorder.providers.RecordDbContract;
import com.anthonynahas.autocallrecorder.providers.RecordDbHelper;
import com.anthonynahas.autocallrecorder.providers.RecordsContentProvider;
import com.anthonynahas.autocallrecorder.utilities.decorators.ActionBarDecorator;
import com.anthonynahas.autocallrecorder.utilities.support.ItemClickSupport;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Class that deals with the content provider (DB) in order to analyse the db and
 * push a statistic as GUI.
 * e.g: most called contacts... most outgoing calls...
 *
 * @author Anthony Nahas
 * @version 1.0
 * @since 16.05.2017
 */
public class StatisticActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = StatisticActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private StatisticRecordsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private final int mLoaderManagerID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        //Setup material action bar
        ActionBarDecorator actionBarDecorator = new ActionBarDecorator();
        actionBarDecorator.setup(this);
        actionBarDecorator.getActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDecorator.getActionBar().setTitle(getResources().getString(R.string.title_activity_statistic));

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new StatisticRecordsAdapter(null);
        mRecyclerView.setAdapter(mAdapter);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Bundle bundle = new Bundle();
                Record record = mAdapter.getItem(position);
                bundle.putParcelable(Resources.REC_PARC_KEY, record);
                startActivity(new Intent(getApplicationContext(),
                        SingleContactRecordActivity.class).putExtras(bundle));
            }
        });

        getSupportLoaderManager().initLoader(mLoaderManagerID, null, this);
    }

    /**
     * E.G: < button: finishes the current activity
     *
     * @param item - item in the toolbar
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        String[] customProjection = new String[]{
                "COUNT ("
                        + RecordDbContract.RecordItem.COLUMN_NUMBER
                        + ") AS " + RecordDbContract.Extended.COLUMN_TOTAL_CALLS,
                "SUM (CASE WHEN "
                        + RecordDbContract.RecordItem.COLUMN_IS_INCOMING
                        + " =  1 THEN 1 ELSE 0 END) AS "
                        + RecordDbContract.Extended.COLUMN_TOTAL_INCOMING_CALLS};

        String[] projection = ArrayUtils.addAll(RecordDbContract.RecordItem.ALL_COLUMNS, customProjection);
        String selection = null;

        String[] selectionArgs = null;
        String sort = "COUNT ( " + RecordDbContract.RecordItem.COLUMN_NUMBER + ") DESC";

        switch (id) {
            case 0:
                // TODO: 17.05.2017 offset and limit control
                Uri uri = RecordDbContract.CONTENT_URL
                        .buildUpon()
                        .appendQueryParameter(RecordsContentProvider.QUERY_PARAMETER_LIMIT,
                                String.valueOf(15))
                        .appendQueryParameter(RecordsContentProvider.QUERY_PARAMETER_OFFSET,
                                String.valueOf(0))
                        .appendQueryParameter(RecordsContentProvider.QUERY_PARAMETER_GROUP_BY,
                                RecordDbContract.RecordItem.COLUMN_NUMBER)
                        //.encodedQuery("mLimit=" + mLimit + "," + mOffset)
                        .build();
                return new CursorLoader(this, uri, projection, selection, selectionArgs, sort);

            default:
                throw new IllegalArgumentException("no loader id handled!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int rows = data.getCount();
        String[] columnNames = data.getColumnNames();
        int columnCount = data.getColumnCount();


        Log.d(TAG, "onLoadFinished with cursor size --> "
                + rows + " with columnNames --> "
                + Arrays.toString(columnNames)
                + " with columnCount --> "
                + columnCount);
        mAdapter = new StatisticRecordsAdapter(RecordDbHelper.convertCursorToContactRecordsList(data));
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        // TODO: 18.05.2017
        //empty for now
    }
}