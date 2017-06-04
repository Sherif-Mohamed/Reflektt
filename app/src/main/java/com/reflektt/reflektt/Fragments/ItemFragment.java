package com.reflektt.reflektt.Fragments;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.reflektt.reflektt.Adapters.CommentsAdapter;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.Constants;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.Tables.Comments;
import com.reflektt.reflektt.Tables.Products;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemFragment extends Fragment {
    @BindView(R.id.product_pic)ImageView productPicture;
    @BindView(R.id.photoLoad)ProgressBar photoProgress;
    @BindView(R.id.product_name)TextView productName;
    @BindView(R.id.product_brand)TextView productBrand;
    @BindView(R.id.rate)RatingBar rateBar;
    @BindView(R.id.loadRate)ProgressBar rateProgress;
    @BindView(R.id.submit_rate)Button submitRate;
    @BindView(R.id.comments)RecyclerView commentsRecycler;
    @BindView(R.id.comment)EditText comment;
    @BindView(R.id.sendComment)ImageView send;
    @BindView(R.id.more_products)TextView moreProducts;
    @BindView(R.id.refresh_item)SwipeRefreshLayout mRefresh;
    @BindView(R.id.addfav)View addFavorite;
    @BindView(R.id.addfav_image)ImageView addfavImage;
    @BindView(R.id.addfav_text)TextView addfavText;
    LayoutInflater inflater;
    private Products product;
    private CommentsAdapter adapter;
    private List<Comments> data;
    private boolean isFavorited = false;
    private int callerId;
    private boolean isRated=false;

    public ItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        View v = inflater.inflate(R.layout.fragment_item, container, false);
        ButterKnife.bind(this, v);
        callerId = getArguments().getInt("callerId");

        load();
        rateLoad();

        mRefresh.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_green_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ItemFragment t = new ItemFragment();
                t.setProduct(product);
                Bundle b = new Bundle();
                b.putInt("callerId", callerId);
                t.setArguments(b);
                ft.replace(callerId,t).commitAllowingStateLoss();
                mRefresh.setRefreshing(false);
            }
        });

        rateBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                LayerDrawable drawable = (LayerDrawable) ratingBar.getProgressDrawable();
                isRated = true;
                if(v == 1)
                    drawable.getDrawable(2).setColorFilter( Color.parseColor("#cc0000"), PorterDuff.Mode.SRC_ATOP);
                else if(v == 2)
                    drawable.getDrawable(2).setColorFilter( Color.parseColor("#cc6900"), PorterDuff.Mode.SRC_ATOP);
                else if(v == 3)
                    drawable.getDrawable(2).setColorFilter( Color.parseColor("#ccc800"), PorterDuff.Mode.SRC_ATOP);
                else if(v == 4)
                    drawable.getDrawable(2).setColorFilter( Color.parseColor("#70cc00"), PorterDuff.Mode.SRC_ATOP);
                else if(v == 5)
                    drawable.getDrawable(2).setColorFilter( Color.parseColor("#00cc0a"), PorterDuff.Mode.SRC_ATOP);
            }
        });

        submitRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentRate = (int)rateBar.getRating();
                if (isRated) {
                    submitRate.setEnabled(false);
                    rateProgress.setVisibility(View.VISIBLE);
                    switch (currentRate) {
                        case 1:
                            Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                                @Override
                                public void handleResponse(Products response) {
                                    BackendlessUser[] rates = response.getRates1();
                                    rateEdit(response, rates,1);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
                                    submitRate.setEnabled(true);
                                    rateProgress.setVisibility(View.GONE);
                                }
                            });
                            break;
                        case 2:
                            Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                                @Override
                                public void handleResponse(Products response) {
                                    BackendlessUser[] rates = response.getRates2();
                                    rateEdit(response, rates,2);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
                                    submitRate.setEnabled(true);
                                    rateProgress.setVisibility(View.GONE);
                                }
                            });
                            break;
                        case 3:
                            Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                                @Override
                                public void handleResponse(Products response) {
                                    BackendlessUser[] rates = response.getRates3();
                                    rateEdit(response, rates,3);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
                                    submitRate.setEnabled(true);
                                    rateProgress.setVisibility(View.GONE);
                                }
                            });
                            break;
                        case 4:
                            Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                                @Override
                                public void handleResponse(Products response) {
                                    BackendlessUser[] rates = response.getRates4();
                                    rateEdit(response, rates,4);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
                                    submitRate.setEnabled(true);
                                    rateProgress.setVisibility(View.GONE);
                                }
                            });
                            break;
                        case 5:
                            Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                                @Override
                                public void handleResponse(Products response) {
                                    BackendlessUser[] rates = response.getRates5();
                                    rateEdit(response, rates,5);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
                                    submitRate.setEnabled(true);
                                    rateProgress.setVisibility(View.GONE);
                                }
                            });
                            break;
                    }
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Comments newComment = new Comments();
                send.setEnabled(false);
                if(comment.getText() == null) return;
                newComment.setComment(comment.getText().toString());
                newComment.setCommentedUser(BackgroundService.getService().getUser());
                comment.setEnabled(false);
                Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                    @Override
                    public void handleResponse(Products response) {
                        LinkedList<Comments> comments = response.getComments();
                        comments.add(newComment);
                        response.setComments(comments);
                        Backendless.Data.of(Products.class).save(response, new AsyncCallback<Products>() {
                            @Override
                            public void handleResponse(Products response) {
                                comment.setEnabled(true);
                                send.setEnabled(true);
                                comment.setText("");
                                data = response.getComments();
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                            }
                        });

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        comment.setEnabled(true);
                        send.setClickable(true);
                        Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFavorited){
                    addfavImage.setColorFilter(Color.BLACK);
                    addfavText.setText(getResources().getString(R.string.add_favorite));
                    addfavText.setTextColor(Color.BLACK);
                }
                else{
                    addfavImage.setColorFilter(Color.rgb(255, 0, 0));
                    addfavText.setText(getResources().getString(R.string.favored));
                    addfavText.setTextColor(Color.rgb(255, 0, 0));
                }
                addFavorite.setEnabled(false);
                Backendless.Persistence.of(Products.class).findById(product, new AsyncCallback<Products>() {
                    @Override
                    public void handleResponse(final Products response) {
                        BackendlessUser user = BackgroundService.getService().getUser();
                        BackendlessUser[] users = response.getFavorites();
                        Object[] x = (Object[]) user.getProperty("favorite_items");
                        if (isFavorited) {
                            if (x.length != 0) {
                                Products[] newProducts = new Products[x.length - 1];
                                int i = 0;
                                for (Products temp : (Products[]) x) {
                                    if (temp.equals(product)) continue;
                                    newProducts[i++] = temp;
                                }
                                user.setProperty("favorite_items", newProducts);
                                BackgroundService.getService().setCurrentUser(user);
                            }
                            if (users.length != 0) {
                                BackendlessUser[] newUsers = new BackendlessUser[users.length - 1];
                                int i = 0;
                                for (BackendlessUser temp : users) {
                                    if (temp.getObjectId().equals(user.getObjectId())) continue;
                                    newUsers[i++] = temp;
                                }
                                response.setFavorites(newUsers);
                            }
                        } else {
                            Products[] newProducts;
                            newProducts = new Products[x.length + 1];
                            if (x.length != 0) {
                                Products[] xyz = (Products[])x;
                                System.arraycopy(xyz, 0, newProducts, 0, x.length);
                            }
                            newProducts[x.length] = product;
                            user.setProperty("favorite_items", newProducts);
                            BackgroundService.getService().setCurrentUser(user);
                            BackendlessUser[] newUsers = new BackendlessUser[users.length + 1];
                            System.arraycopy(newUsers, 0, users, 0, users.length);
                            newUsers[users.length] = user;
                            response.setFavorites(newUsers);
                        }
                        Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser userX) {
                                Backendless.Persistence.of(Products.class).save(response, new AsyncCallback<Products>() {
                                    @Override
                                    public void handleResponse(Products response2) {
                                        product = response2;
                                        getFragmentManager().beginTransaction()
                                                .detach(ItemFragment.this).attach(ItemFragment.this).commit();
                                        isFavorited = !isFavorited;
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {

                                    }
                                });
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                fault.getMessage();
                            }
                        });

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {

                    }
                });
            }
        });

        moreProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clause =  "companyName='" + product.getCompanyName() + "'";
                ProductsFragment newFragment = new ProductsFragment();
                Bundle b = new Bundle();
                b.putInt("callerId", callerId);
                b.putString("clause", clause);
                newFragment.setArguments(b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(callerId, newFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return v;
    }

    public void load() {
        //check if is favorited or not
        Object[] x = (Object[]) BackgroundService.getService().getUser().getProperty("favorite_items");
        if (x.length != 0) {
            Products[] y = (Products[]) x;
            for (Products mProduct : y) {
                if (mProduct.equals(product)) {
                    isFavorited = true;
                    addfavImage.setColorFilter(Color.rgb(255, 0, 0));
                    addfavText.setText(getResources().getString(R.string.favored));
                    addfavText.setTextColor(Color.rgb(255, 0, 0));
                    break;
                }
            }
        }
        //load photo
        String path = Constants.PRODUCTS_PATH + product.getParent() + "/" + product.getCompanyName() + "/" + product.getCategory() + "/";
        if (product.getCompanyName().equals("Oriflam"))
            path += product.getType() + "/";
        path += product.getName() + ".jpg";
        path = path.replaceAll(" ", "%20");
        Picasso.with(getContext()).load(path).into(productPicture, new Callback() {
            @Override
            public void onSuccess() {
                photoProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                photoProgress.setIndeterminate(false);
                photoProgress.setProgress(100);
            }
        });
        //load name and brand
        productName.setText(product.getName());
        productBrand.setText(product.getCompanyName());

        //RecyclerView loading
        data = product.getComments();
        LinearLayoutManager m = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setNestedScrollingEnabled(false);
        adapter = new CommentsAdapter(inflater, getActivity(), data, this, getArguments().getInt("callerId"));
        commentsRecycler.setAdapter(adapter);
        commentsRecycler.setLayoutManager(m);

        moreProducts.setText(String.format("%s%s",getResources().getString(R.string.more_products_from)
                ,product.getCompanyName()));

    }

    private void rateEdit(Products response, BackendlessUser[] rates, int i) {
        BackendlessUser[] newRates = new BackendlessUser[rates.length + 1];
        System.arraycopy(rates, 0, newRates, 0, rates.length);
        newRates[rates.length] = BackgroundService.getService().getUser();
        switch (i){
            case 1:
                response.setRates1(newRates);
                break;
            case 2:
                response.setRates2(newRates);
                break;
            case 3:
                response.setRates3(newRates);
                break;
            case 4:
                response.setRates4(newRates);
                break;
            case 5:
                response.setRates5(newRates);
                break;
        }

        Backendless.Persistence.of(Products.class).save(response, new AsyncCallback<Products>() {
            @Override
            public void handleResponse(Products response) {
                product = response;
                submitRate.setEnabled(true);
                rateProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(),getString(R.string.thanks_rate),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });


    }

    private void rateLoad(){
        BackendlessUser[] users;
        users = product.getRates1();
        for (BackendlessUser x:users) {
            if(x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())){
                rateBar.setRating(1);
                return;
            }
        }
        users = product.getRates2();
        for (BackendlessUser x:users) {
            if(x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())){
                rateBar.setRating(2);
                return;
            }
        }
        users = product.getRates3();
        for (BackendlessUser x:users) {
            if(x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())){
                rateBar.setRating(3);
                return;
            }
        }
        users = product.getRates4();
        for (BackendlessUser x:users) {
            if(x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())){
                rateBar.setRating(4);
                return;
            }
        }
        users = product.getRates5();
        for (BackendlessUser x:users) {
            if(x.getObjectId().equals(BackgroundService.getService().getUser().getObjectId())){
                rateBar.setRating(5);
                return;
            }
        }
    }

    public void setProduct(Products product) {
        this.product = product;
    }
}
