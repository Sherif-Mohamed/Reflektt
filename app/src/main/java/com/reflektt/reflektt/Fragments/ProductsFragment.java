package com.reflektt.reflektt.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.reflektt.reflektt.Adapters.ProductAdapter;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Products;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.reflektt.reflektt.R.id.loadProducts;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductsFragment extends Fragment {
    @BindView(R.id.products_grid)
    RecyclerView recyclerView;
    @BindView(loadProducts)
    ProgressBar loadProgress;
    @BindView(R.id.empty)
    TextView empty;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    LayoutInflater inflater;
    private BackendlessCollection<Products> dataCollection;
    private List<Products> data;
    private boolean isDataSet = false;
    //for loading more data
    private boolean loading = true;
    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        View v = inflater.inflate(R.layout.fragment_products, container, false);
        ButterKnife.bind(this, v);
        final ProductAdapter[] adapter = new ProductAdapter[1];
        final GridLayoutManager mGrid = new GridLayoutManager(getContext(), Constants.calculateNoOfColumns(getContext(),160));

        recyclerView.setLayoutManager(mGrid);
        recyclerView.setHasFixedSize(true);
        mGrid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter[0].getItemViewType(position) == ProductAdapter.VIEW_PROG)
                    return 2; //number of columns of the grid
                else return 1;
            }
        });
        final int id = getArguments().getInt("callerId");
        if (!isDataSet) {
            String clause = getArguments().getString("clause");

            QueryOptions queryOptions = new QueryOptions();
            List<String> sortBy = new LinkedList<>();
            sortBy.add("updated DESC");
            queryOptions.setSortBy(sortBy);
            queryOptions.setPageSize(20);
            queryOptions.setOffset(0);
            BackendlessDataQuery query = new BackendlessDataQuery(queryOptions);
            if (clause != null)
                query.setWhereClause(clause);
            Backendless.Data.of(Products.class).find(query, new AsyncCallback<BackendlessCollection<Products>>() {
                @Override
                public void handleResponse(BackendlessCollection<Products> response) {
                    data = response.getData();
                    dataCollection = response;
                    loadProgress.setVisibility(View.GONE);
                    if (data.size() != 0) {
                        adapter[0] = new ProductAdapter(ProductsFragment.this.inflater, data,
                                ProductsFragment.this, id, getActivity());
                        recyclerView.setAdapter(adapter[0]);
                    } else empty.setVisibility(View.VISIBLE);

                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(getContext(), fault.getMessage(), Toast.LENGTH_SHORT).show();
                    loadProgress.setVisibility(View.GONE);
                }
            });
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = mGrid.getChildCount();
                        totalItemCount = mGrid.getItemCount();
                        pastVisiblesItems = mGrid.findFirstVisibleItemPosition();

                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && loading) {
                            //Do pagination.. i.e. fetch new data
                            data.add(null);
                            adapter[0].notifyItemInserted(data.size());
                            loading = false;
                            dataCollection.nextPage(new AsyncCallback<BackendlessCollection<Products>>() {
                                @Override
                                public void handleResponse(BackendlessCollection<Products> response) {
                                    data.remove(data.size() - 1);
                                    List<Products> list = response.getData();
                                    if (list.size() != 0)
                                        if (!data.get(data.size() - 1).equals((list.get(list.size() - 1)))) {
                                            data.addAll(response.getData());
                                            loading = true;
                                        }
                                    adapter[0].notifyDataSetChanged();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {

                                }
                            });
                        }
                    }
                }
            });
        } else {
            adapter[0] = new ProductAdapter(this.inflater, data, ProductsFragment.this, id, getActivity());
            recyclerView.setAdapter(adapter[0]);
            loadProgress.setVisibility(View.GONE);
        }
        return v;
    }

    public void setData(List<Products> data) {
        this.data = data;
        isDataSet = true;
    }
}
