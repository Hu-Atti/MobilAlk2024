package hu.mobilalkfejl.util;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import hu.mobilalkfejl.R;
import hu.mobilalkfejl.activities.product.ProductDetailsActivity;
import hu.mobilalkfejl.model.Product;
import hu.mobilalkfejl.model.Store;

public class ProductItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements Filterable {
    private ArrayList<Product> mProductItemData;
    private final ArrayList<Product> mProductItemDataAll;
    private final Context context;
    private int lastPos = -1;
    private String filterCategory = null;

    private CollectionReference mStores;

    public ProductItemAdapter(Context context, ArrayList<Product> itemsData) {
        this.mProductItemData = itemsData;
        this.mProductItemDataAll = itemsData;
        this.context = context;
        this.mStores = FirebaseFirestore.getInstance().collection("/Stores");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_product, parent, false));
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Product currentItem = mProductItemData.get(position);

        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bindTo(currentItem);
        }

        if (holder.getAdapterPosition() > lastPos) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPos = holder.getAdapterPosition();
        }
    }

    public void setFilterCategory(String category) {
        this.filterCategory = category;
        getFilter().filter("");
    }


    @Override
    public int getItemCount() {
        return mProductItemData.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Product> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            String filterPattern = charSequence == null ? null : charSequence.toString().toLowerCase().trim();

            for (Product product : mProductItemDataAll) {
                if ((filterPattern == null || product.getName().toLowerCase().contains(filterPattern)) &&
                        (filterCategory == null || product.getCategory().equals(filterCategory))) {
                    filteredList.add(product);
                }
            }

            results.count = filteredList.size();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            mProductItemData = (ArrayList<Product>) results.values;
            notifyDataSetChanged();
        }

    };

    public void sort(final int sortType) {
        Collections.sort(mProductItemData, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                switch (sortType) {
                    case 0: // A-Z
                        return p1.getName().compareTo(p2.getName());
                    case 1: // Z-A
                        return p2.getName().compareTo(p1.getName());
                    case 2: // Ár szerint növekvő
                        return Integer.compare(p1.getLowestPrice(), p2.getLowestPrice());
                    case 3: // Ár szerint csökkenő
                        return Integer.compare(p2.getLowestPrice(), p1.getLowestPrice());
                    default:
                        return 0;
                }
            }
        });
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        private static final String LOG_TAG = "ProductViewHolder";
        private final TextView productName;
        private final TextView category;
        private final TextView price;
        private final ImageView image;
        private final RatingBar ratingBar;

        private Product currentItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.product_name);
            category = itemView.findViewById(R.id.product_category);
            price = itemView.findViewById(R.id.lowest_price);
            image = itemView.findViewById(R.id.product_image);
            ratingBar = itemView.findViewById(R.id.product_rating);
        }

        public void bindTo(Product currentItem) {
            this.currentItem = currentItem;

            productName.setText(currentItem.getName());
            category.setText(currentItem.getCategory());
            ratingBar.setRating(currentItem.getRating());

            if (currentItem.getLowestPrice() != Integer.MAX_VALUE) {
                price.setText(String.valueOf(currentItem.getLowestPrice()) + " Ft");
            }
            if (currentItem.getLowestPrice() == Integer.MAX_VALUE) {
                price.setText("Termék nem elérhető");
            }

            Glide.with(context)
                    .load(currentItem.getImage())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.imagenotfound))
                    .into(image);

            Button productDetailsButton = itemView.findViewById(R.id.product_details_button);
            productDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProductDetailsActivity.class);
                    intent.putExtra("PRODUCT_ID", currentItem._getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}




