package com.reflektt.reflektt.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.backendless.BackendlessUser;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.Fragments.ItemFragment;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Comments;
import com.reflektt.reflektt.Tables.Products;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sherif on March,5th
 */

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_PROG = 0;
    //for adding infinite scrolling with progressBar
    static final int VIEW_ITEM = 1;
    private LayoutInflater inflater;
    private List<Products> data;
    private Context context;
    private Fragment fragment;
    private int callerId;

    public ProductAdapter(LayoutInflater inflater, List<Products> collection, Fragment callerFragment, int callerId, Context c) {
        context = c;
        this.inflater = inflater;
        data = collection;
        fragment = callerFragment;
        this.callerId = callerId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_ITEM) {
            View view = inflater.inflate(R.layout.product_item, parent, false);
            return new ProductsVH(view);
        }
        else{
            View view = inflater.inflate(R.layout.progress_bar, parent, false);
            return new ProgressVH(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder mainHolder, int position) {
        if (mainHolder instanceof ProductsVH) {
            final ProductsVH holder = (ProductsVH) mainHolder;
            Products property = data.get(position);
            holder.loadProgress.setVisibility(View.VISIBLE);
            holder.productName.setText(property.getName());
            holder.productBrand.setText(property.getCompanyName());
            //setting rates
            int usersNo = property.getRates1().length + property.getRates2().length + property.getRates3().length +
                    property.getRates3().length + property.getRates4().length;
            if (usersNo == 0)
                holder.rating.setText("0");
            else {
                float rate = property.getRates1().length + 2 * property.getRates2().length + 3 * property.getRates3().length +
                        4 * property.getRates3().length + 5 * property.getRates4().length;
                holder.rating.setText(String.valueOf(rate / usersNo));
            }
            List<Comments> comments = property.getComments();
            holder.comments.setText(String.format(new Locale("en"),"%d",comments.size()));
            BackendlessUser[] favorites = property.getFavorites();
            holder.favorites.setText(String.valueOf(favorites.length));
            String path = Constants.PRODUCTS_PATH + property.getParent() + "/" + property.getCompanyName() + "/" + property.getCategory() + "/";
            if (property.getCompanyName().equals("Oriflam"))
                path += property.getType() + "/";
            path += property.getName() + ".jpg";
            path = path.replaceAll(" ", "%20");
            Picasso.with(context).load(path).into(holder.productPicture, new Callback() {
                @Override
                public void onSuccess() {
                    holder.loadProgress.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    holder.loadProgress.setIndeterminate(false);
                    holder.loadProgress.setProgress(100);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    static class ProgressVH extends RecyclerView.ViewHolder {

        ProgressVH(View itemView) {
            super(itemView);
        }
    }

     class ProductsVH extends RecyclerView.ViewHolder {
        @BindView(R.id.item_picture)
        ImageView productPicture;
        @BindView(R.id.item_title)
        TextView productName;
        @BindView(R.id.item_brand)
        TextView productBrand;
        @BindView(R.id.rate_number)
        TextView rating;
        @BindView(R.id.fav_number)
        TextView favorites;
        @BindView(R.id.comment_number)
        TextView comments;
        @BindView(R.id.loadPhotoProgress)
        ProgressBar loadProgress;

        ProductsVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ItemFragment newFragment = new ItemFragment();
                    newFragment.setProduct(data.get(getAdapterPosition()));
                    Bundle b = new Bundle();
                    b.putInt("callerId", callerId);
                    newFragment.setArguments(b);
                    FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
                    ft.replace(callerId, newFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });

        }
    }
}
