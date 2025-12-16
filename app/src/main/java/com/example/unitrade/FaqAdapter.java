package com.example.unitrade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private List<Faq> faqList;

    public FaqAdapter(List<Faq> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        Faq faq = faqList.get(position);
        holder.question.setText(faq.getQuestion());
        holder.answer.setText(faq.getAnswer());

        boolean isExpanded = faq.isExpanded();
        holder.answer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            faq.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    public void updateFaqs(List<Faq> newFaqs) {
        this.faqList = newFaqs;
        notifyDataSetChanged();
    }

    public static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView question, answer;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.tvQuestion);
            answer = itemView.findViewById(R.id.tvAnswer);
        }
    }
}
