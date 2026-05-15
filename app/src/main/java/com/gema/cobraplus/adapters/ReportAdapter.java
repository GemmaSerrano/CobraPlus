package com.gema.cobraplus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder>{

    private ArrayList<Report> reportList;
    private OnReportOptionsListener listener;
    private String highlightReportId;
    private boolean isAdmin;

    public ReportAdapter(ArrayList<Report> reportList, boolean isAdmin) {
        this.reportList = reportList;
        this.isAdmin = isAdmin;

    }

    @NonNull
    @Override
    public ReportAdapter.ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);

        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.txtItemReportName.setText(report.getName());

        if (!isAdmin && report.getCreatedAt() != null) {
            SimpleDateFormat sdfCreated = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.txtItemReportCreatedAt.setVisibility(View.VISIBLE);
            holder.txtItemReportCreatedAt.setText("Creado: " + sdfCreated.format(report.getCreatedAt()));
        } else {
            holder.txtItemReportCreatedAt.setVisibility(View.GONE);
        }

        if (report.isBaseReport()) {
            holder.txtItemReportCompanyLabel.setVisibility(View.GONE);
            holder.txtItemReportCompanyValue.setVisibility(View.GONE);
            holder.txtItemReportPeriodLabel.setVisibility(View.GONE);
            holder.txtItemReportPeriodValue.setVisibility(View.GONE);
        } else {
            holder.txtItemReportCompanyLabel.setVisibility(View.VISIBLE);
            holder.txtItemReportCompanyValue.setVisibility(View.VISIBLE);
            holder.txtItemReportPeriodLabel.setVisibility(View.VISIBLE);
            holder.txtItemReportPeriodValue.setVisibility(View.VISIBLE);

            if (report.getCompany() != null && !report.getCompany().trim().isEmpty()) {
                holder.txtItemReportCompanyValue.setText(report.getCompany());
            } else {
                holder.txtItemReportCompanyValue.setText("Todas");
            }

            if (report.getStartDate() != null && report.getEndDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String period = sdf.format(report.getStartDate()) + " - " + sdf.format(report.getEndDate());
                holder.txtItemReportPeriodValue.setText(period);
            } else {
                holder.txtItemReportPeriodValue.setText("");
            }
        }

        // Resaltado visual
        if (highlightReportId != null && report.getId() != null && report.getId().equals(highlightReportId)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_report_highlight);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        if (isAdmin) {
            holder.btnOptions.setImageResource(R.drawable.ic_delete);

            holder.btnOptions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReport(report);
                }
            });

        } else {
            holder.btnOptions.setImageResource(R.drawable.ic_lista);

            holder.btnOptions.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.btnOptions);
                popupMenu.inflate(R.menu.report_options_menu);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == R.id.menuViewReport) {
                        if (listener != null) listener.onViewReport(report);
                        return true;
                    }

                    if (id == R.id.menuExportPdf) {
                        if (listener != null) listener.onExportPdf(report);
                        return true;
                    }

                    if (id == R.id.menuExportExcel) {
                        if (listener != null) listener.onExportExcel(report);
                        return true;
                    }

                    if (id == R.id.menuDeleteReport) {
                        if (listener != null) listener.onDeleteReport(report);
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            });
        }
    }


    @Override
    public int getItemCount() {
        return reportList.size();
    }



    //ViewHolder del item
    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView txtItemReportName,txtItemReportCreatedAt ;
        TextView txtItemReportCompanyLabel, txtItemReportCompanyValue;
        TextView txtItemReportPeriodLabel, txtItemReportPeriodValue;
        ImageView btnOptions;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            txtItemReportName = itemView.findViewById(R.id.txtItemReportName);
            txtItemReportCreatedAt = itemView.findViewById(R.id.txtItemReportCreatedAt);
            txtItemReportCompanyLabel = itemView.findViewById(R.id.txtItemReportCompanyLabel);
            txtItemReportCompanyValue = itemView.findViewById(R.id.txtItemReportCompanyValue);
            txtItemReportPeriodLabel = itemView.findViewById(R.id.txtItemReportPeriodLabel);
            txtItemReportPeriodValue = itemView.findViewById(R.id.txtItemReportPeriodValue);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }

    //Interfaz para opciones del informe
    public interface OnReportOptionsListener {
        void onViewReport(Report report);
        void onExportPdf(Report report);
        void onExportExcel(Report report);
        void onDeleteReport(Report report);
    }

    //Asigna listener de opciones
    public void setOnReportOptionsListener(OnReportOptionsListener listener) {
        this.listener = listener;
    }

    public void setHighlightReportId(String highlightReportId) {
        this.highlightReportId = highlightReportId;
    }
}
