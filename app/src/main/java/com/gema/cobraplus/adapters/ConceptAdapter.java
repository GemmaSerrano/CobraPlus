package com.gema.cobraplus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Concept;

import java.util.ArrayList;

public class ConceptAdapter extends RecyclerView.Adapter<ConceptAdapter.ConceptViewHolder> {

    private ArrayList<Concept> conceptList;
    private OnConceptDeleteListener listener;

    public ConceptAdapter(ArrayList<Concept> conceptList) {
        this.conceptList = conceptList;

    }

    @NonNull
    @Override
    public ConceptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_concept, parent, false);

        return new ConceptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConceptViewHolder holder, int position) {
        Concept concept = conceptList.get(position);

        holder.txtItemConceptName.setText(concept.getName());

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(concept);
            }
        });

        //Si es concepto base del administrador
        if (concept.isBaseConcept()) {
            holder.txtItemCompany.setVisibility(View.GONE);
            holder.txtItemAmount.setVisibility(View.GONE);
            holder.txtItemConceptCompany.setVisibility(View.GONE);
            holder.txtItemConceptAmount.setVisibility(View.GONE);
            holder.txtItemUnitCalculation.setVisibility(View.VISIBLE);
            holder.txtItemConceptUnitCalculation.setVisibility(View.VISIBLE);
            holder.txtItemConceptUnitCalculation.setText(concept.getUnitCalculation());

        } else {
            holder.txtItemUnitCalculation.setVisibility(View.GONE);
            holder.txtItemConceptUnitCalculation.setVisibility(View.GONE);
            holder.txtItemAmount.setVisibility(View.VISIBLE);
            holder.txtItemConceptAmount.setVisibility(View.VISIBLE);
            holder.txtItemCompany.setVisibility(View.VISIBLE);
            holder.txtItemConceptCompany.setVisibility(View.VISIBLE);
            holder.txtItemConceptCompany.setText(concept.getCompany());
            holder.txtItemConceptAmount.setText(concept.getAmount() + " €/" + concept.getUnitCalculation());
        }
    }

    @Override
    public int getItemCount() {
        return conceptList.size();
    }

    //ViewHolder del item
    public static class ConceptViewHolder extends RecyclerView.ViewHolder {
        TextView txtItemConceptName, txtItemConceptCompany, txtItemConceptAmount, txtItemConceptUnitCalculation,
                txtItemName, txtItemCompany, txtItemAmount, txtItemUnitCalculation;
        ImageView btnDelete;

        public ConceptViewHolder(@NonNull View itemView) {
            super(itemView);

            txtItemConceptName = itemView.findViewById(R.id.txtItemConceptName);
            txtItemConceptCompany = itemView.findViewById(R.id.txtItemConceptCompany);
            txtItemConceptAmount = itemView.findViewById(R.id.txtItemConceptAmount);
            txtItemConceptUnitCalculation = itemView.findViewById(R.id.txtItemConceptUnitCalculation);

            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtItemCompany = itemView.findViewById(R.id.txtItemCompany);
            txtItemUnitCalculation = itemView.findViewById(R.id.txtItemUnitCalculation);
            txtItemAmount = itemView.findViewById(R.id.txtItemAmount);

            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    //Interfaz para eliminar concepto
    public interface OnConceptDeleteListener {
        void onDeleteClick(Concept concept);
    }

    //Asigna listener de borrado
    public void setOnConceptDeleteListener(OnConceptDeleteListener listener) {
        this.listener = listener;
    }
}
