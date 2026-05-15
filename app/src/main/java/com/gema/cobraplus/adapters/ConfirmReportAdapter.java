package com.gema.cobraplus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.ConfirmReportItem;

import java.util.ArrayList;

public class ConfirmReportAdapter extends RecyclerView.Adapter<ConfirmReportAdapter.ConfirmReportViewHolder> {

    private ArrayList<ConfirmReportItem> itemList;
    private OnEditClickListener listener;

    public ConfirmReportAdapter(ArrayList<ConfirmReportItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ConfirmReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_confirm_reports, parent, false);

        return new ConfirmReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmReportViewHolder holder, int position) {
        ConfirmReportItem item = itemList.get(position);

        holder.txtDateConfirmListLabel.setText(item.getDate());
        holder.txtCompanyConfirmListLabel.setText(item.getCompany());
        holder.txtConceptConfirmListLabel.setText(item.getConcept());
        holder.txtQuantityConfirmListLabel.setText(item.getQuantity());
        holder.txtCommentConfirmListLabel.setText(item.getCommentary());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ConfirmReportViewHolder extends RecyclerView.ViewHolder {

        TextView txtDateConfirmListLabel, txtCompanyConfirmListLabel, txtConceptConfirmListLabel,
                txtQuantityConfirmListLabel, txtCommentConfirmListLabel;
        ImageView btnEdit;

        public ConfirmReportViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDateConfirmListLabel = itemView.findViewById(R.id.txtDateConfirmListLabel);
            txtCompanyConfirmListLabel = itemView.findViewById(R.id.txtCompanyConfirmListLabel);
            txtConceptConfirmListLabel = itemView.findViewById(R.id.txtConceptConfirmListLabel);
            txtQuantityConfirmListLabel = itemView.findViewById(R.id.txtQuantityConfirmListLabel);
            txtCommentConfirmListLabel = itemView.findViewById(R.id.txtCommentConfirmListLabel);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(ConfirmReportItem item);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.listener = listener;
    }
}