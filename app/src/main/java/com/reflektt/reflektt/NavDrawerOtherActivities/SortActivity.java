package com.reflektt.reflektt.NavDrawerOtherActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.reflektt.reflektt.Fragments.ProductsFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.StringSingleton;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class SortActivity extends AppCompatActivity {
    @BindView(R.id.sort_view)
    RecyclerView recyclerView;
    @BindView(R.id.loading)
    ProgressBar loadingBar;
    String sortType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        sortType = getIntent().getStringExtra("sort");

        String url = "http://reflektt.16mb.com/";
        if (sortType.equals("companyName")) url += "Brands.txt";
        else if (sortType.equals("type")) url += "Types.txt";

        RequestQueue requestQueue = StringSingleton.getInstance(this).getRequestQueue();
        StringRequest counter = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String[] splittedItems = response.split("\n");
                loadingBar.setVisibility(GONE);
                recyclerView.setLayoutManager(new LinearLayoutManager(SortActivity.this, LinearLayoutManager.VERTICAL, false));
                recyclerView.setAdapter(new SimpleAdapter(SortActivity.this, splittedItems));
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SortActivity.this, "There was an error fetching the data,please try again later", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(counter);
    }

    class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleVH> {
        LayoutInflater inflater;
        String[] listItems;

        SimpleAdapter(Context c, String[] listItems) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.listItems = listItems;
        }

        @Override
        public SimpleVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.list_item, parent, false);
            return new SimpleVH(view);
        }

        @Override
        public void onBindViewHolder(SimpleVH holder, int position) {
            holder.text.setText(listItems[position]);
        }

        @Override
        public int getItemCount() {
            return listItems.length;
        }

        class SimpleVH extends RecyclerView.ViewHolder {
            @BindView(R.id.text)
            TextView text;

            SimpleVH(final View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String clause = sortType + "='" + listItems[getAdapterPosition()] + "'";
                        ProductsFragment newFragment = new ProductsFragment();
                        Bundle b = new Bundle();
                        b.putInt("callerId", R.id.fragment_frame);
                        b.putString("clause", clause);
                        newFragment.setArguments(b);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_frame, newFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            NavUtils.navigateUpFromSameTask(this);
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
