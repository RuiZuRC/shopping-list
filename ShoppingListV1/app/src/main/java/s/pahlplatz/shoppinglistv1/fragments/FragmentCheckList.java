package s.pahlplatz.shoppinglistv1.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import s.pahlplatz.shoppinglistv1.R;
import s.pahlplatz.shoppinglistv1.adapters.CheckListAdapter;
import s.pahlplatz.shoppinglistv1.utils.Database;

/**
 * Created by Stefan on 25-11-2016.
 *
 * Fragment for checking your items
 */

public class FragmentCheckList extends Fragment
{
    private static final String TAG = FragmentCheckList.class.getSimpleName();

    private Database db;
    private ListView lv_Products;
    private ArrayList<ArrayList> list;
    private CheckListAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_checklist, container, false);

        db = new Database(getContext().getResources().getString(R.string.ConnectionString)
                , getContext().getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("userid", -1));

        // Set up SwipeRefreshLayout
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new PopulateListView().execute(getContext());
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Configure ListView
        lv_Products = (ListView) view.findViewById(R.id.lv_Products);
        lv_Products.setLongClickable(true);
        lv_Products.setClickable(true);
        registerForContextMenu(lv_Products);
        new PopulateListView().execute(getContext());

        // Configure ActionButton
        FloatingActionButton actionButton = (FloatingActionButton) view.findViewById(R.id.checklist_btn_Delete);
        actionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ArrayList<Integer> itemsToDelete = adapter.getSelected();

                if (itemsToDelete.size() == 0)
                    Toast.makeText(getContext(), "Select 1 or more products first", Toast.LENGTH_SHORT).show();
                else {
                    for (int i = 0; i < itemsToDelete.size(); i++) {
                        int index = itemsToDelete.get(i);
                        for (int j = 0; j < list.size(); j++)
                        {
                            list.get(j).remove(index);
                        }
                        adapter = new CheckListAdapter(list.get(0), list.get(1), list.get(2),  getContext());
                        lv_Products.setAdapter(adapter);
                    }
                    Toast.makeText(getContext(), "Product removed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Fill the ListView with data from the database
    private class PopulateListView extends AsyncTask<Context, Void, CheckListAdapter>
    {
        @SuppressWarnings("unchecked")
        protected CheckListAdapter doInBackground(Context... params)
        {
            // Get context from param
            Context ctx = params[0];

            // Get list from database
            list = db.getInfoCheckList();

            // Pass the adapter to onPostExecute
            return new CheckListAdapter(list.get(0),list.get(1), list.get(2), ctx);
        }

        protected void onPostExecute(CheckListAdapter param)
        {
            // Assign the adapter
            adapter = param;
            lv_Products.setAdapter(adapter);

            if(swipeContainer.isRefreshing())
            {
                swipeContainer.setRefreshing(false);
            }
        }
    }
}
