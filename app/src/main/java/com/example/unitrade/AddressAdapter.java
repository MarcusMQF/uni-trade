package com.example.unitrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final Context context;
    private final List<Address> addresses;
    private int selectedPosition = -1;

    public AddressAdapter(Context context, List<Address> addresses) {
        this.context = context;
        this.addresses = addresses;
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).isDefault()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.address_item, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.txtAddressItem.setText(address.getAddress());
        holder.rbDefault.setChecked(position == selectedPosition);

        holder.rbDefault.setOnClickListener(v -> {
            if (position != selectedPosition) {
                if (selectedPosition != -1) {
                    addresses.get(selectedPosition).setDefault(false);
                    notifyItemChanged(selectedPosition);
                }
                selectedPosition = position;
                addresses.get(selectedPosition).setDefault(true);
                notifyItemChanged(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public void addAddress(Address address) {
        addresses.add(address);
        notifyItemInserted(addresses.size() - 1);
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbDefault;
        TextView txtAddressItem;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            rbDefault = itemView.findViewById(R.id.rbDefault);
            txtAddressItem = itemView.findViewById(R.id.txtAddressItem);
        }
    }
}
