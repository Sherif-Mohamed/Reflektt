package com.reflektt.reflektt.NavigationDrawer;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.reflektt.reflektt.R;


/**
 * Created by Shiko on 08/11/2016.
 */

public class NavMenuAdapter extends RecyclerView.Adapter<NavMenuAdapter.NavVH> {
    private LayoutInflater inflater;
    String[] names;
    int[] Icons;
    ClickListener clickListener;

    public NavMenuAdapter(Context context, String[] data, int[] icons) {
        inflater = LayoutInflater.from(context);
        names = data;
        Icons = icons;
    }

    @Override
    public NavVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_menu_row, parent, false);
        NavVH navVH = new NavVH(view);
        return navVH;
    }

    @Override
    public void onBindViewHolder(NavVH holder, int position) {
        holder.text.setText(names[position]);

        holder.itemIcon.setImageResource(Icons[position]);
        holder.itemIcon.setColorFilter(Color.rgb(1, 1, 1));
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
    @Override
    public int getItemCount() {
        return names.length;
    }

    class NavVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView text;
        private ImageView itemIcon;
        private RippleView rippleView;

        public NavVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(R.id.row_name);
            itemIcon = (ImageView) itemView.findViewById(R.id.icon_sort_menu);
            rippleView = (RippleView) itemView.findViewById(R.id.ripple);
        }

        @Override
        public void onClick(final View v) {
            if (clickListener != null){
                rippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                    @Override
                    public void onComplete(RippleView rippleView) {
                        clickListener.onItemClick(v, getAdapterPosition());
                    }
                });
            }
        }
    }
    public interface ClickListener{
        public void onItemClick(View view,int position);
    }
}
