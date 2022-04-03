package com.github.jdubo1998.targetpracticesystemcontroller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SetupTargetsAdapter extends RecyclerView.Adapter<SetupTargetsAdapter.ViewHolder> {
    Context context;
    int targetCount = 0;
    int mActiveUserPosition;
    ArrayList<String> codes = new ArrayList<>();

    SetupTargetsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setup_target_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.targetLabel.setText("Target " + (position + 1)); // TODO: Use String resource
        holder.targetID = position;

        ArrayList<String> setTargets = new ArrayList<>();
        ArrayList<String> syncTargets = new ArrayList<>();

        for (int i = 1; i <= targetCount; i++) {
            if (i != position+1) {
                setTargets.add("" + i);
                syncTargets.add("" + i);
            }
        }

        setTargets.add("Random");
        setTargets.add("None");
        syncTargets.add("None");

        ArrayAdapter<String> nextTargetAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, setTargets);
        nextTargetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.nextTargetSpinner.setAdapter(nextTargetAdapter);

        ArrayAdapter<String> syncAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, syncTargets);
        syncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.syncSpinner.setAdapter(syncAdapter);

        holder.syncSpinner.setSelection(syncTargets.size()-1);
    }

    private void generateCode(ViewHolder holder) {
        codes.set(holder.targetID, holder.generateCode());
        Log.d("SetupTargetsAdapter", holder.generateCode());
    }

    public void setActiveUserPosition(int position) {
        if (mActiveUserPosition != position) {
            int oldPosition = mActiveUserPosition;
            mActiveUserPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return targetCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int targetID = 0;
        final TextView targetLabel;

        final Spinner nextTargetSpinner;

        final EditText minUpIntervalEditText;
        final EditText maxUpIntervalEditText;
        final EditText minDownIntervalEditText;
        final EditText maxDownIntervalEditText;

        final Spinner syncSpinner;

        public ViewHolder(View viewHolder) {
            super(viewHolder);
            targetLabel = viewHolder.findViewById(R.id.target_label);

            nextTargetSpinner = viewHolder.findViewById(R.id.nexttarget_spinner);
            minUpIntervalEditText = viewHolder.findViewById(R.id.minupinterval_edittext);
            maxUpIntervalEditText = viewHolder.findViewById(R.id.maxupinterval_edittext);
            minDownIntervalEditText = viewHolder.findViewById(R.id.mindowninterval_edittext);
            maxDownIntervalEditText = viewHolder.findViewById(R.id.maxdowninterval_edittext);
            syncSpinner = viewHolder.findViewById(R.id.sync_spinner);
        }

        public String generateCode() {
            StringBuilder code = new StringBuilder();
            String param = this.nextTargetSpinner.getSelectedItem().toString();

            if (param.equals("None")) {
                code.append("nt;").append(targetID).append(";-3");
            } else if (param.equals("Random")) {
                code.append("nt;").append(targetID).append(";-1");
            } else {
                int nextTarget = Integer.parseInt(param)-1;

                if (nextTarget != targetID) {
                    code.append("nt;").append(targetID).append(";").append(nextTarget);
                } else {
                    return "nextTarget==i";
                }
            }

            double minUpInterval = 0.0;
            double maxUpInterval = 0.0;
            double minDownInterval = 0.0;
            double maxDownInterval = 0.0;

            param = this.minUpIntervalEditText.getText().toString();
            if (!param.isEmpty()) {
                minUpInterval = Double.parseDouble(param);
            }
            param = this.maxUpIntervalEditText.getText().toString();
            if (!param.isEmpty()) {
                maxUpInterval = Double.parseDouble(param);
            }
            param = this.minDownIntervalEditText.getText().toString();
            if (!param.isEmpty()) {
                minDownInterval = Double.parseDouble(param);
            }
            param = this.maxDownIntervalEditText.getText().toString();
            if (!param.isEmpty()) {
                maxDownInterval = Double.parseDouble(param);
            }

            if (minUpInterval != 0 || maxUpInterval != 0 || minDownInterval != 0 || maxDownInterval != 0) {
                code.append(";ti;").append(targetID).append(";")
                        .append(Math.abs((int) (minUpInterval * 1000))).append(";")
                        .append(Math.abs((int) (minDownInterval * 1000))).append(";")
                        .append((int) (((maxUpInterval > minUpInterval) ? maxUpInterval - minUpInterval : 0 ) * 1000)).append(";")
                        .append((int) (((maxDownInterval > minDownInterval) ? maxDownInterval - minDownInterval : 0) * 1000));
            }

            param = this.syncSpinner.getSelectedItem().toString();
            if (!param.equals("None")) {
                code.append(";sy;").append(targetID).append(";").append(param);
            }

            return code.toString();
        }
    }

    public void update(int targetCount) {
        if (this.targetCount > targetCount) {
            codes.remove(codes.size() - 1); // TODO: Remove from middle, not just end.
        } else if (this.targetCount < targetCount) {
            codes.add("");
        }

        this.targetCount = targetCount;
        notifyDataSetChanged();
    }
}
